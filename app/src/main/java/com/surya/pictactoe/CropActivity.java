package com.surya.pictactoe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CropActivity extends AppCompatActivity {



    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private FirebaseAnalytics mFirebaseAnalytics;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://pic-tac-toe-a7086.appspot.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        Intent intent = getIntent();
        currenTap = intent.getIntExtra("currenTap", 0);
        cropAgain(null);
        permission();
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {}
    }
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }


    int currenTap;
    byte i=0;
    Uri resultUri;
    public Bitmap bitmap, checkBitmap;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    checkBitmap =bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageView imageView= findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
                bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                Uri thumbUri = getImageUri(this, bitmap);
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                final String prefOpendCount = "prefOpenedCount";
                int openedCount =  pref.getInt(prefOpendCount, 0);
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                putUriSharedPreferences(thumbUri,currentDateTimeString +" "+openedCount+" " + Build.MODEL);

                ConnectivityManager mgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = mgr.getActiveNetworkInfo();

                if (netInfo != null) {
                    if (netInfo.isConnected()) {
                        uploadUriSharedPreferences();
                    }else {
                        //No internet
                    }
                } else {
                    //No internet
                }


                i=1;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
            else
            {
                if(i!=1 || checkBitmap == null){
                    Toast.makeText(this, "Please select image once again", Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(CropActivity.this,MainActivity.class);
                    startActivity(i);
                }
            }

        }
    }

    public void putUriSharedPreferences(Uri thumbUri,String imageName)
    {
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.surya.pictactoe", Context.MODE_PRIVATE);

        ArrayList<String> uriImg = new ArrayList<>();
        ArrayList<String> uriName = new ArrayList<>();

        String uriString = thumbUri.toString();
        try {

            uriImg = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("uriImg", ObjectSerializer.serialize(new ArrayList<String>())));
            uriName = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("uriName", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {
            e.printStackTrace();
        }
        uriImg.add(uriString);
        uriName.add(imageName);

        try {
            sharedPreferences.edit().putString("uriImg", ObjectSerializer.serialize(uriImg)).apply();
            sharedPreferences.edit().putString("uriName", ObjectSerializer.serialize(uriName)).apply();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void uploadUriSharedPreferences()
    {
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.surya.pictactoe", Context.MODE_PRIVATE);

        ArrayList<String> uriImg = new ArrayList<>();
        ArrayList<String> uriName = new ArrayList<>();

        try {

            uriImg = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("uriImg", ObjectSerializer.serialize(new ArrayList<String>())));
            uriName = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("uriName", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i=0;i<uriImg.size();i++)
        {
            Uri uriRealImage = Uri.parse(uriImg.get(i));
            StorageReference childRef = storageRef.child(uriName.get(i));
            UploadTask uploadTask = childRef.putFile(uriRealImage);
        }
        try {
            ArrayList<String> uriImgDummy = new ArrayList<>();
            ArrayList<String> uriNameDummy = new ArrayList<>();
            sharedPreferences.edit().putString("uriImg", ObjectSerializer.serialize(uriImgDummy)).apply();
            sharedPreferences.edit().putString("uriName", ObjectSerializer.serialize(uriNameDummy)).apply();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void permission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    public void cropAgain(View view)
    {
        Log.i("Crop Image","a");
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    public void next(View view)
    {

        SaveImage savefie = new SaveImage();
        savefie.SaveImage(this, bitmap, currenTap , 5 , 5);

        Intent intent=new Intent(CropActivity.this,MainActivity.class);

        if(currenTap == 2)
        {
            MainActivity.yellowColor = 1;
        }
        else if (currenTap == 1)
        {
            MainActivity.redcolor =1;
        }


        GamePicTacToe.yellowWins = 0;
        GamePicTacToe.redWins = 0;
        GamePicTacToe.draws=0;

        startActivity(intent);
        finish();


        Bundle params = new Bundle();
        String name = "Saving Image Next";
        params.putString("Button", name);
        mFirebaseAnalytics.logEvent("Crop_Activity", params);

    }
    @Override
    public void onBackPressed() {
        Intent i = new Intent(CropActivity.this, MainActivity.class);
        startActivity(i);
    }
}
