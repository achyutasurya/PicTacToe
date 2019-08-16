package com.surya.pictactoe;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.IOException;
import java.util.ArrayList;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

public class MainActivity extends AppCompatActivity {

    DatabaseReference ref;
    GeoFire geoFire;

    private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    assert pCur != null;
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i("ABC", "Name: " + name);
                        Log.i("ABC", "Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }

    static int  firstTime = 5;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static boolean hasPermission(Context context, String permission) {

        int res = context.checkCallingOrSelfPermission(permission);

        Log.v("Tag", "permission: " + permission + " = \t\t" +
                (res == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

        return res == PackageManager.PERMISSION_GRANTED;

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

   public void saveImagesAtStart()
   {
       SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
       final String prefOpendCount = "prefOpenedCount";
       int openedCount =  pref.getInt(prefOpendCount, 0);
       save.SaveImage(this, null , 5 , 0 , 1);
       save.SaveImage(this, null , 5 , 1 , 0);
       firstTime = 1;
       SaveImage.unablesecond=3;
       openedCount += 1;
       SharedPreferences.Editor editor = pref.edit();
       editor.putInt("prefOpenedCount", openedCount);
       editor.apply();

       //intertitial ads
       MobileAds.initialize(this, "ca-app-pub-3520590809173899~3626605275");
       final InterstitialAd mInterstitialAd = new InterstitialAd(this);
       mInterstitialAd.setAdUnitId("ca-app-pub-3520590809173899/7545668987");
       mInterstitialAd.loadAd(new AdRequest.Builder().build());
       mInterstitialAd.setAdListener(new AdListener() {
           public void onAdLoaded() {
               if (mInterstitialAd.isLoaded()) {
                   mInterstitialAd.show();
                   AdRequest adRequest = new AdRequest.Builder().build();
                   mInterstitialAd.loadAd(adRequest);
               }
           }
       });
   }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        ref = FirebaseDatabase.getInstance().getReference("path/to/geofire");
        geoFire = new GeoFire(ref);

        getContactList();

        geoFire.getLocation("firebase-hq", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                } else {
                    System.out.println(String.format("There is no location for key for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
                System.out.println(String.format(" location for key for key in GeoFirev  error"));
            }
        });


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        gamesPlayed(null);//to display the number of games played

        startService(new Intent(getBaseContext(), OnClearFromRecentService.class));

        String permissions = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

        boolean hasAndroidPermissions = hasPermission(this, permissions);

        Log.i("hasAndroidPermissions", String.valueOf(hasAndroidPermissions));


        AppRate.with(this)
                .setInstallDays(2) // default 10, 0 means install day.
                .setLaunchTimes(5) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);



        ConnectivityManager mgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert mgr != null;
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();




        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        final String prefOpendCount = "prefOpenedCount";
        int openedCount =  pref.getInt(prefOpendCount, 0);

        if(firstTime == 5 && hasAndroidPermissions)
        {
            saveImagesAtStart();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    uploadUriSharedPreferences();

                }
            }
        }



        Bundle params = new Bundle();
        String name = "Games Played" + Integer.toString(openedCount);
        params.putString("Button", name);
        mFirebaseAnalytics.logEvent("Opened_count_main_Activity", params);




        if(yellowColor == 1)
        {
            ImageView img1= findViewById(R.id.yellowImageView);
            bitmap =save.readImage(2);
            img1.setImageBitmap(bitmap);
        }
        if(redcolor == 1)
        {
            ImageView img2= findViewById(R.id.redImageView);
            bitmap =save.readImage(1);
            img2.setImageBitmap(bitmap);
        }

        if(soundChecker[0])
        {
            ImageView imageOncreate =  findViewById(R.id.soundicon);//sound image icon id
            imageOncreate.setImageResource(R.drawable.soundon);
        }
        else
        {
            ImageView imageOncreate =  findViewById(R.id.soundicon);//sound image icon id
            imageOncreate.setImageResource(R.drawable.soundoff);
        }

        final ImageView image =  findViewById(R.id.soundicon);//sound image icon id
        image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if(soundChecker[0])
                {
                    image.setImageResource(R.drawable.soundoff);
                    soundChecker[0] = false;
                }
                else
                {
                    image.setImageResource(R.drawable.soundon);
                    soundChecker[0] = true;
                }
            }

        });

        final ImageView rating =  findViewById(R.id.rateIcon);//sound image icon id
        rating.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {


                AppRate.with(MainActivity.this).showRateDialog(MainActivity.this);
            }

        });

        final ImageView yellowImage =  findViewById(R.id.yellowImageView);//sound image icon id
        yellowImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view)
            {
                letsCrop(view);
            }
        });


        final ImageView redImage =  findViewById(R.id.redImageView);//sound image icon id
        redImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view)
            {
                letsCrop(view);
            }
        });



        permissionsChecker(null);


    }
    final static boolean[] soundChecker = {true};//to get detail of sound on off

    static int currentTap;
    SaveImage save=new SaveImage();

    Bitmap bitmap;
    static int yellowColor,redcolor;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if(firstTime == 5)
                    {
                        saveImagesAtStart();
                        ConnectivityManager mgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
                        assert mgr != null;
                        NetworkInfo netInfo = mgr.getActiveNetworkInfo();
                        if (netInfo != null) {
                            if (netInfo.isConnected()) {
                                uploadUriSharedPreferences();
                            }
                        }
                    }


                } else {
                    //not granted
                    ActivityCompat.requestPermissions(this , new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://pic-tac-toe-a7086.appspot.com");



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void permissionsChecker(View view)
    {

        Context context = this;
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                context.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this , new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
    }



    public void letsCrop(View view)
    {
        Intent intent=new Intent(MainActivity.this,CropActivity.class);
        currentTap=Integer.parseInt(view.getTag().toString());

        intent.putExtra("currenTap", currentTap);
        startActivity(intent);
        finish();
    }



    public void playNow(View view)
    {

        Bundle params = new Bundle();
        String name = "play now";
        params.putString("Button", name);
        mFirebaseAnalytics.logEvent("MainActivity", params);


        Intent intent=new Intent(MainActivity.this,GamePicTacToe.class);
        startActivity(intent);
        finish();
    }


    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;

        }
    }



    public void gamesPlayed(View view)
    {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        final String gamesPlayed = "gamesPlayed";
        TextView gamesPlayedText = findViewById(R.id.gamesPlayedMainActivity);
        gamesPlayedText.setText(getString(R.string.noofgameplayed) + Integer.toString(pref.getInt(gamesPlayed, 0)));
    }

    public void singleGame(View view)
    {

        Intent intent=new Intent(MainActivity.this,SingleGamePicTacToe.class);
        startActivity(intent);
        finish();
    }


}
