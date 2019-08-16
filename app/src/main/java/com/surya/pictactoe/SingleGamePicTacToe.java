package com.surya.pictactoe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;

public class SingleGamePicTacToe extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_pic_tac_toe);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FirebaseCrash.report(new Exception("My first Android non-fatal error"));
        FirebaseCrash.log("Single Player Game");

        okAIPlayer=0;

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        View c = findViewById(R.id.menu2);
        c.setVisibility(View.GONE);

        Bitmap bitmap = null;
        f=new File(save.file_path,"Yellow.jpg");
        ImageView imageView = findViewById(R.id.yellowcountimage);
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
        f=new File(save.file_path,"Red.jpg");
        imageView = findViewById(R.id.redcountimage);
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);

        TextView red = findViewById(R.id.redWinsView);
        red.setText(Integer.toString(redWins));

        TextView yellow = findViewById(R.id.yellowWinsView);
        yellow.setText(Integer.toString(yellowWins));



    }

    private FirebaseAnalytics mFirebaseAnalytics;


    ImageView singleImageView=null;
    //view of the second player

    boolean gameIsActive = true;
    //to check whether the game is active or not

    int playedCount=0;



    int currentPlayer=0;
    //to know whow has played

    //int ImageId[]={R.drawable.yellow, R.drawable.red};
    //id of the red player image

    int[] isItDone={2, 2, 2, 2, 2, 2, 2, 2, 2};
    //to check whether the particular location in occupied or not and to find whether yellow or red occupied the location
    // "0"= yellow  occupied     "1"= red occupied

    int possibleWinningCases[][]={{0,1,2},{3,4,5},{6,7,8},{0,4,8},{2,4,6},{0,3,6},{1,4,7},{2,5,8}};
    //possible winning loation

    int changePlayer=0;


    public void currentCounter(View view) throws FileNotFoundException {
        ImageView currentCounterImage = (ImageView)view;
        int currentTap=Integer.parseInt(currentCounterImage.getTag().toString());
        //grid layout current tag
        System.out.println("AIwithsurya currentCounter GamesIsActive "+gameIsActive);
        if(isItDone[currentTap]==2 && gameIsActive)//to get the current gridlocation tag to check whether its first time or not and avoid user to change the second time
        {
            try{
                placeUser(currentCounterImage, currentTap);
            }
            catch(FileNotFoundException ignored)
            {}
            System.out.println("AIwithsurya Before who Won currentTap"+currentTap);
            int j=whoWon();
            if(j==1){
                draw();
            }
        }
    }


    File f;
    SaveImage save = new SaveImage();


    //to add image at particular location
    public void placeUser(ImageView currentCounterImage, int currentTap) throws FileNotFoundException {
        System.out.println("playUser Entered");
        if(MainActivity.soundChecker[0])
        {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.press);
            mediaPlayer.start();
        }
        Bitmap b;
        System.out.println("After Save");
        selectPlayerImage(currentPlayer);
        b = BitmapFactory.decodeStream(new FileInputStream(f));
        //bitmap =save.readImage(b);
        System.out.println("out of if");

        currentCounterImage.setImageBitmap(b);
        isItDone[currentTap] = currentPlayer; //chages 0 for yellow and 1 for red occupied
        currentPlayer=(currentPlayer==0) ? 1 : 0;// to make next player to play

    }

    public void ai() throws FileNotFoundException {
        /*int[] random={5,0,2,3,7,8,1,4,6};
        int ok=1;
        for(int i=0;i<9;i++)
        {
            System.out.print(random[i]+ " ");
            if(isItDone[random[i]]== 2) {
                selectView(random[i]);
                currentCounter(singleImageView);
                break;
            }
            System.out.println("after if");
        }
        System.out.println("after for");*/
        int checker;
        Random random=new Random();
        int ok=1,count=0;
        for(int i=0;i<9;i++)
        {
            if(isItDone[i]== 2) {
                count+=1;
            }
        }
        for(; ok==1&&count!=0; )
        {
            checker=random.nextInt(9);
            if(isItDone[checker]== 2) {
                ok=0;
                final int a[] = {R.id.imageView0  , R.id.imageView1 , R.id.imageView2 , R.id.imageView3 , R.id.imageView4 , R.id.imageView5 , R.id.imageView6 , R.id.imageView7, R.id.imageView8};
                singleImageView = findViewById(a[checker]);
                if(singleImageView != null)
                {
                    currentCounter(singleImageView);
                }
            }
        }

    }


    public void selectPlayerImage(int a)
    {
        if(a==0)
        {
            f=new File(save.file_path,"Yellow.jpg");
        }
        else
        {
            f=new File(save.file_path,"Red.jpg");
        }
    }

    int winner;
    int okAIPlayer=0;
    //to check who has won
    public int whoWon() throws FileNotFoundException {
        for(int[] possibleWinningCasesTemp: possibleWinningCases)
        {
            if(isItDone[possibleWinningCasesTemp[0]]==isItDone[possibleWinningCasesTemp[1]] &&
                    isItDone[possibleWinningCasesTemp[1]] == isItDone[possibleWinningCasesTemp[2]] &&
                    isItDone[possibleWinningCasesTemp[0]] != 2)
            {
                okAIPlayer=1;
                System.out.println("AIwithsurya inWhoWon GamesIsActive "+gameIsActive);
                gameIsActive=false;
                System.out.println("AIwithsurya inWhoWon GamesIsActive "+gameIsActive);
                ProgressBar progressBar=findViewById(R.id.progressBar2);
                progressBar.setVisibility(View.GONE);
                winner=(isItDone[possibleWinningCasesTemp[0]]==0) ? 0 : 1;
                final int a[] = {R.id.imageView0  , R.id.imageView1 , R.id.imageView2 , R.id.imageView3 , R.id.imageView4 , R.id.imageView5 , R.id.imageView6 , R.id.imageView7, R.id.imageView8};

                Bitmap bitmap;

                if(winner == 0)
                {
                    yellowWins +=1;
                    TextView yellow = findViewById(R.id.yellowWinsView);
                    yellow.setText(Integer.toString(yellowWins));
                    selectPlayerImage(0);
                }
                else
                {
                    redWins +=1;
                    TextView red = findViewById(R.id.redWinsView);
                    red.setText(Integer.toString(redWins));
                    selectPlayerImage(1);
                }

                final ImageView imageView = findViewById(a[possibleWinningCasesTemp[0]]);
                final ImageView imageView1 = findViewById(a[possibleWinningCasesTemp[1]]);
                final ImageView imageView2 = findViewById(a[possibleWinningCasesTemp[2]]);
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f));


                Handler mHandler = new Handler();
                boolean b = mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println("AIwithsurya 1");
                        imageView.setImageDrawable(null);
                        imageView1.setImageDrawable(null);
                        imageView2.setImageDrawable(null);

                    }

                }, 200L);

                final Bitmap finalBitmap = bitmap;
                b = mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println("AIwithsurya 2");
                        imageView.setImageBitmap(finalBitmap);
                        imageView1.setImageBitmap(finalBitmap);
                        imageView2.setImageBitmap(finalBitmap);

                    }

                }, 400L);


                b = mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println("AIwithsurya 3");
                        imageView.setImageDrawable(null);
                        imageView1.setImageDrawable(null);
                        imageView2.setImageDrawable(null);

                    }

                }, 600L);

                b = mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println("AIwithsurya 4");
                        imageView.setImageBitmap(finalBitmap);
                        imageView1.setImageBitmap(finalBitmap);
                        imageView2.setImageBitmap(finalBitmap);
                    }

                }, 800L);

                b = mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            System.out.println("AIwithsurya before declareWinner");
                            declareWinner(winner);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }

                }, 1000L);

                return 0;

            }
        }
        if(okAIPlayer==0)
        {
            checkAI(null);

        }
        return 1;
    }


    //declare winner in Linear layout popup
    public void declareWinner(int winner) throws FileNotFoundException {
        if(MainActivity.soundChecker[0])
        {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.clap);
            mediaPlayer.start();
        }
        LinearLayout layout = findViewById(R.id.linearlayout);
        layout.setVisibility(View.VISIBLE);
        selectPlayerImage(winner);
        System.out.println(f);
        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
        ImageView winnerImage = findViewById(R.id.winnerImage);
        winnerImage.setImageBitmap(b);
        gameIsActive=false;
        abc=0;
        buttonInvisible(null);
        gamesPlayed(null);
        changePlayer +=1;
        Bundle params = new Bundle();
        String name = "Declare winner :- " + Integer.toString(winner);
        params.putString("Button", name);
        mFirebaseAnalytics.logEvent("Game_Activity", params);

    }



    public void declareDrawWinner(String winner) throws FileNotFoundException {
        LinearLayout layout = findViewById(R.id.linearlayoutdraw);
        layout.setVisibility(View.VISIBLE);
        TextView winnerText= findViewById(R.id.winner);
        winnerText.setText(winner);
        winnerText.setTextColor(Color.WHITE);
        gameIsActive=false;
        buttonInvisible(null);
        gamesPlayed(null);
        changePlayer +=1;
        abc=0;
        Bundle params = new Bundle();
        String name = "Declare draw";
        params.putString("Button", name);
        mFirebaseAnalytics.logEvent("Game_Activity", params);
    }

    public void playAgain(View view) throws FileNotFoundException {
        System.out.println("AIwithsurya PlayAgain");
        playedCount +=1;
        okAIPlayer=0;
        abc=0;
        gameIsActive = true;
        LinearLayout layout = findViewById(R.id.linearlayout);
        layout.setVisibility(View.INVISIBLE);
        LinearLayout layoutDraw = findViewById(R.id.linearlayoutdraw);
        layoutDraw.setVisibility(View.INVISIBLE);
        if(changePlayer%2 == 0)
        {
            currentPlayer=0;
        }
        else
        {
            currentPlayer=1;
        }
        Arrays.fill(isItDone, 2);
        GridLayout gridLayout =  findViewById(R.id.gridLay);
        for(int i=0;i<gridLayout.getChildCount();i++)
        {
            ((ImageView) gridLayout.getChildAt(i)).setImageResource(0);
        }
        buttonVisible(null);

        Bundle params = new Bundle();
        String name = "Play Again";
        params.putString("Button", name);
        mFirebaseAnalytics.logEvent("Game_Activity", params);
        checkAI(null);
        System.out.println("AIwithsurya end declareWinner");
    }
    int abc=0;
    public void checkAI(View view)
    {
        if(currentPlayer==1)
        {
            gameIsActive=false;
            final ProgressBar progressBar=findViewById(R.id.progressBar2);
            progressBar.setScaleY(2f);
            progressBar.setScaleX(2f);
            progressBar.setVisibility(View.VISIBLE);
            int xyz[]={Color.BLUE,Color.CYAN,Color.GRAY,Color.GREEN,Color.YELLOW};
            progressBar.getIndeterminateDrawable().setColorFilter(
                    xyz[abc], android.graphics.PorterDuff.Mode.SRC_IN);
            abc++;
            if(abc>4)
            {
                abc=0;
            }
            Handler mHandler = new Handler();
            boolean bi = mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    try {
                        progressBar.setVisibility(View.GONE);

                        gameIsActive=true;
                        ai();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                }

            }, 700L);

        }
    }


    static int yellowWins,redWins,draws;

    public void draw()
    {
        int j=0;

        for(int i : isItDone)
        {
            if(j==0)
            {
                j=(i==2)? 1 : 0;
            }
            else
            {
                break;
            }
        }
        if(j==0)
        {
            ProgressBar progressBar=findViewById(R.id.progressBar2);
            progressBar.setVisibility(View.GONE);
            Handler mHandler = new Handler();
            boolean b = mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        declareDrawWinner("Its Draw");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }, 500L);

            draws +=1;
            TextView draw = findViewById(R.id.drawCount);
            draw.setText(Integer.toString(draws));
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent i=new Intent(SingleGamePicTacToe.this,MainActivity.class);
        startActivity(i);

    }

    public void buttonInvisible(View view)
    {
        View b = findViewById(R.id.button5);
        b.setVisibility(View.GONE);

        View a = findViewById(R.id.button6);
        a.setVisibility(View.GONE);

        View c = findViewById(R.id.menu2);
        c.setVisibility(View.VISIBLE);

    }

    public void buttonVisible(View view)
    {
        View b = findViewById(R.id.button5);
        b.setVisibility(View.VISIBLE);

        View a = findViewById(R.id.button6);
        a.setVisibility(View.VISIBLE);

        View c = findViewById(R.id.menu2);
        c.setVisibility(View.GONE);

    }

    public void mainMenuButton(View view)
    {
        Intent i=new Intent(SingleGamePicTacToe.this,MainActivity.class);
        startActivity(i);
    }



    public void gamesPlayed(View view)
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        String gamesPlayed = "gamesPlayed";
        int gamesPlayedVariable =  pref.getInt(gamesPlayed, 0);
        gamesPlayedVariable += 1;
        editor.putInt("gamesPlayed", gamesPlayedVariable);
        editor.apply();

        Bundle params = new Bundle();
        String name = "Games Played" + Integer.toString(gamesPlayedVariable);
        params.putString("Button", name);
        mFirebaseAnalytics.logEvent("games_played_Game_Activity", params);
    }


}
