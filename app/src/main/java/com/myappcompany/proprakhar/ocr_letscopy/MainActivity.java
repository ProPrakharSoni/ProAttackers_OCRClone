package com.myappcompany.proprakhar.ocr_letscopy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.VoiceInteractor;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Session2Command;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button capture,detect;
    private TextView textView;
    private Bitmap imageBitmap;
    private Bitmap bitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int pickImage=1;
    int time;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView);
        capture=findViewById(R.id.capture);
        detect=findViewById(R.id.detect);
        textView=findViewById(R.id.textView);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             dispatchTakePictureIntent();
             textView.setText("");
            }
        });
       try {
           detect.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   detectTextFromImage();
               }
           });
       }catch(Exception e){
           e.printStackTrace();
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
    switch(item.getItemId()){
        case R.id.choose:
            textView.setText("");
            Intent gallery=new Intent();
                         gallery.setType("image/*");
                         gallery.setAction(Intent.ACTION_GET_CONTENT);
                         startActivityForResult(Intent.createChooser(gallery,"Select Picture"),pickImage);
                         time=1;
            return true;
        default: return false;
    }
    }

    private void dispatchTakePictureIntent() {
        time=0;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(time==1&&requestCode==pickImage&& resultCode== RESULT_OK){
            imageUri=data.getData();
            try{
                bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                imageView.setImageBitmap(bitmap);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if (time==0 && requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }

    }
    private void detectTextFromImage() {
       try {
           if (time == 0) {
               FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
               FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
               firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                   @Override
                   public void onSuccess(FirebaseVisionText firebaseVisionText) {
                       displayTextFromImage(firebaseVisionText);
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                       Log.d("Error", e.getMessage());
                   }
               });
           } else if (time == 1) {
               FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
               FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
               firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                   @Override
                   public void onSuccess(FirebaseVisionText firebaseVisionText) {
                       displayTextFromImage(firebaseVisionText);
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                       Log.d("Error", e.getMessage());
                   }
               });
           }
       }catch (Exception e){
           Toast.makeText(this, "Please select an Image", Toast.LENGTH_SHORT).show();
           e.printStackTrace();
       }
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block> blockList=firebaseVisionText.getBlocks();
        if(blockList.size()==0){
            Toast.makeText(this, "No text found in Image", Toast.LENGTH_SHORT).show();
        }
        else{
            for(FirebaseVisionText.Block block : firebaseVisionText.getBlocks() ){
             String text =block.getText();
             textView.setText(text);
            }
        }
    }
}