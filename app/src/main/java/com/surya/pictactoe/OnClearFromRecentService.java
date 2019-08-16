package com.surya.pictactoe;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;

public class OnClearFromRecentService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("ClearFromRecentService  Service Started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("ClearFromRecentService  Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {


        System.out.println("ClearFromRecentService  END");



        File fdelete = new File(SaveImage.file_path,"Yellow.jpg");
        System.out.println(fdelete);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted 1 :" );
            } else {
                System.out.println("file not Deleted 1 :" );
            }
        }

        fdelete = new File(SaveImage.file_path,"Red.jpg");
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted 2:" );
            } else {
                System.out.println("file not Deleted 2 :" );
            }
        }
        ;




        //stopSelf();
    }
}