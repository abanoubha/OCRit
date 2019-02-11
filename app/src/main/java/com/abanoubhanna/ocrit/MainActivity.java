package com.abanoubhanna.ocrit;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final int IMAGE_GALLERY_REQUEST = 20;
    public static final int CAMERA_REQUEST_CODE = 228;
    //public static final int CAMERA_PERMISSION_REQUEST_CODE = 4192;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OcrManager manager = new OcrManager();
        manager.initAPI();
        ((TextView)findViewById(R.id.textView))
                .setText(manager.startRecognize(
                        BitmapFactory.decodeResource(getResources(),
                                R.drawable.sample)));
    }
    public void takePhoto(View v){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }
    public void getImageFromGallery(View v){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        photoPickerIntent.setDataAndType(data,"image/*");
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_GALLERY_REQUEST){
                Uri imageUri = data.getData();
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    ((ImageView)findViewById(R.id.imageView)).setImageBitmap(image);
                    //OCR the image
                    OcrManager manager = new OcrManager();
                    manager.initAPI();
                    ((TextView)findViewById(R.id.textView)).setText(manager.startRecognize(image));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this,"unable to find the image", Toast.LENGTH_LONG).show();
                }
            }
            if (requestCode == CAMERA_REQUEST_CODE){
                Bitmap cameraImage = (Bitmap) data.getExtras().get("data");
                ((ImageView)findViewById(R.id.imageView)).setImageBitmap(cameraImage);
                //OCR the image
                OcrManager manager = new OcrManager();
                manager.initAPI();
                ((TextView)findViewById(R.id.textView)).setText(manager.startRecognize(cameraImage));
            }
        }
    }
    public void shareText(View v){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareBody = ((TextView)findViewById(R.id.textView)).getText().toString();
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent,"Share using"));
    }
    public void copyText(View v){
        copy2Clipboard(((TextView)findViewById(R.id.textView)).getText().toString());
    }
    void copy2Clipboard(CharSequence text){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copy text", text);
        if (clipboard != null){
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(this,"text copied", Toast.LENGTH_LONG).show();
    }
}
