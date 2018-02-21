package max.example.com.quickurl.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import max.example.com.quickurl.R;

public class SplashActivity extends Activity {

    SharedPreferences sPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        String firstLoad = sPref.getString("load", "");

        if(firstLoad.length() == 0) {
            startTimer();
        }
        else {

        }
    }

    private void startTimer() {
        final Intent intent = new Intent(this, ManualActivity.class);
        intent.putExtra("parent", "splash");
        Thread splash_time = new Thread() {
            public void run() {
                try {
                    int SplashTimer = 0;
                    while (SplashTimer < 3000) {
                        sleep(100);
                        SplashTimer = SplashTimer + 100;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                    startActivity(intent);
                }

            }
        };
        splash_time.start();
    }
}
