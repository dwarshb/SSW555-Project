package com.dwarsh.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Thread thr=new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(3000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally {
                    Intent intent=new Intent(Splash.this,LoginActivity.class);
                    startActivity(intent);
                }
            }
        };
        thr.start();
    }
    protected void onPause()
    {
        super.onPause();
        finish();
    }
}
