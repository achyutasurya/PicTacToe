package com.surya.pictactoe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveImage {



    private Context TheThis;
    static private String NameOfFolder = "/pictactoe";
    File file;
    static String file_path= Environment.getExternalStorageDirectory().getAbsolutePath()+NameOfFolder;




    public void SaveImage(Context context, Bitmap ImageToSave , int whosImage , int saveTom , int saveJerry)
    {
        TheThis = context;
        System.out.println("File path is" +file_path);
        File dir = new File(file_path);
        if(!dir.exists()){
            dir.mkdirs();
        }

        Bitmap savingImage = ImageToSave;








        if(whosImage == 2){
            file = new File(dir, "Yellow" + ".jpg");
        }
        else if (whosImage == 1)
        {
            file = new File(dir, "Red" + ".jpg");
        }
        else if(saveTom == 0)
        {
            file = new File(dir, "Yellow" + ".jpg");
            savingImage = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.yellow);
        }

        else if(saveJerry == 0)
        {
            System.out.println("save.savejerry()");
            file = new File(dir, "Red" + ".jpg");
            System.out.println("file name is given");
            savingImage = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.red);
            System.out.println("saving image from drawable");
        }



        System.out.println("File created");
        try{
            FileOutputStream fOut = new FileOutputStream(file);
            savingImage.compress(Bitmap.CompressFormat.JPEG, 85, fOut );
            fOut.flush();
            fOut.close();
            MakeSureFileWasCreatedThenMakeAvabile(file);
            AbleToSave();
        }
        catch (FileNotFoundException e){
            System.out.println("FNF exception");
            UnableToSave();
        }
        catch (IOException e){
            System.out.println("IO Excep");
            UnableToSave();
        }

    }





    private void MakeSureFileWasCreatedThenMakeAvabile(File file)
    {
        MediaScannerConnection.scanFile(TheThis,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener(){
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.e("ExternalStorage","Scanned"+ path + ":");
                        Log.e("ExternalStorage", "-> uri="+ uri);
                    }
                });
    }

    private static void isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
        }
    }

    private static void isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
        }
    }


    private void UnableToSave(){

        isExternalStorageAvailable();
        isExternalStorageReadOnly();
        UnableToSave2();

    }
    static int unablesecond;
    private void UnableToSave2(){

        if(unablesecond >2)
        {
            Toast.makeText(TheThis, "Picture Cannot to gallery", Toast.LENGTH_SHORT).show();
        }
        unablesecond +=1;
    }

    private static int secondTime;

    private void AbleToSave(){

        if(secondTime > 2)
        {
            Toast.makeText(TheThis, "Picture Saved Successfull", Toast.LENGTH_SHORT).show();
        }
        secondTime = secondTime + 1;
    }

    public Bitmap readImage(int whosImage)
    {

        Bitmap b = null;
        try {

            File f = null;
            if(whosImage == 2)
            {

                f=new File(file_path,"Yellow.jpg");
            }
            else if(whosImage == 1)
            {
                f=new File(file_path,"Red.jpg");
            }

            b = BitmapFactory.decodeStream(new FileInputStream(f));



        } catch (FileNotFoundException e) {
            UnableToSave();
        }
        return b;

    }
}
