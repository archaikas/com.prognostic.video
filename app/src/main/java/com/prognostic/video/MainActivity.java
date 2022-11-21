package com.prognostic.video;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener,
        View.OnTouchListener, RewardedVideoAdListener {

    VideoView videoView;
    int pos = 0;
    int curnt_pos = 0;
    String videos[]=new String[3];
    String NEW_DB,DB_PATH;
    Dbhelper dbhelper;
    ArrayList<VideoModel> list;
    Button btn_ok;
    private String link="";

    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {


            }
        });

        setupIntertestail();

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(MainActivity.this);
        mRewardedVideoAd.setRewardedVideoAdListener(MainActivity.this);
        loadRewardedVideoAd();

        performDBCopy();

        list = new ArrayList<>();
        dbhelper=new Dbhelper(MainActivity.this);
        list = dbhelper.getVideos();
        link = dbhelper.link();

        videos[0] = list.get(0).video_path;
        videos[1] = list.get(1).video_path;
        videos[2] = list.get(2).video_path;

        videoView = (VideoView)findViewById(R.id.videoview);
        setVideoView(videos[pos]);
        videoView.setOnCompletionListener(this);
        videoView.setOnTouchListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(curnt_pos == pos)
        {
            pos++;
        }
        if(pos == 1)
        {
            setVideoView(videos[pos]);
            curnt_pos++;
        }
        else if(pos == 2)
        {
            setVideoView(videos[pos]);
            curnt_pos++;
        }
        else
        {
            openDialog();
        }

        Log.i("Pos= ", pos+"");
        Log.i("Current_Pos= ", curnt_pos+"");
    }


    public void setVideoView(String url)
    {
        if(videoView!=null)
        {
            Uri videoUri = Uri.parse(url);
            videoView.setVideoURI(videoUri);
            videoView.start();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(pos == 1)
        {
            if(videoView.isPlaying())
            {
                videoView.pause();

                if (getString(R.string.enable_rewarded).equals("true") & mRewardedVideoAd.isLoaded()){
                    mRewardedVideoAd.show();
                }else if(getString(R.string.enable_intertestail).equals("true") & mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                }else {
                    setVideoView(videos[2]);
                    pos=2;
                    curnt_pos = 2;

                }

                return true;
            }
        }
            hideSystemUI();
        return false;
    }

    public void openDialog() {
        final Dialog dialog = new Dialog(MainActivity.this); // Context, this, etc.
        dialog.setContentView(R.layout.dialog_verification);
        dialog.setCancelable(false);
        btn_ok = (Button) dialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(link));
                startActivity(intent);
                finish();
            }
        });
        dialog.show();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void performDBCopy() {

        this.NEW_DB = Dbhelper.DATABASE_NAME;
        this.DB_PATH = new StringBuilder(String.valueOf(getFilesDir().getParent())).append("/databases/").toString();
        try {
            if (!new File(this.DB_PATH, this.NEW_DB).exists()) {
                CopyDatabase(this.NEW_DB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CopyDatabase(String DB_NAME) throws IOException {
        File f1_path = new File(this.DB_PATH);
        if (!f1_path.exists()) {
            f1_path.mkdir();
        }
        String CopyFileName = this.DB_PATH + DB_NAME;
        InputStream fin = getAssets().open(DB_NAME);
        OutputStream fout = new FileOutputStream(CopyFileName);
        byte[] Buffer = new byte[1048];
        while (true) {
            int length = fin.read(Buffer);
            if (length <= 0) {
                fout.flush();
                fout.close();
                fin.close();
                return;
            }
            fout.write(Buffer, 0, length);
        }
    }

    public void setupIntertestail(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_intertestial_ad));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());

                setVideoView(videos[2]);
                pos=2;
                curnt_pos = 2;

            }
        });
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.admob_rewarded_ad),
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewarded(RewardItem reward) {
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();

        setVideoView(videos[2]);
        pos=2;
        curnt_pos = 2;
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
    }

    @Override
    public void onRewardedVideoAdLoaded() {
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    @Override
    public void onRewardedVideoCompleted() {
    }
}
