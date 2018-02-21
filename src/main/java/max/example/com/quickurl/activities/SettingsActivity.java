package max.example.com.quickurl.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import max.example.com.quickurl.R;
import max.example.com.quickurl.adapters.TimeSplashAdapter;

public class SettingsActivity extends AppCompatActivity {

    Context context;

    String homePage;
    int loadHomePage, showMusicWidget, showSplash;

    Button back, save;
    TextView timeTv;
    FloatingActionButton editHome;
    Spinner timeSpinner;
    Switch loadHome, showWidget, splash;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        getSupportActionBar().setTitle("Settings");

        context = this;
        timeTv = findViewById(R.id.time_tv_settings);
        back = findViewById(R.id.settings_back);
        save = findViewById(R.id.settings_save);
        editHome = findViewById(R.id.edit_home_page);
        timeSpinner = findViewById(R.id.time_spinner);
        TimeSplashAdapter adapter = new TimeSplashAdapter(this);
        timeSpinner.setAdapter(adapter);
        splash = findViewById(R.id.splash_switch);
        checkSpinner();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        splash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSpinner();
            }
        });

        editHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Home page");
                builder.setView(LayoutInflater.from(context).inflate(R.layout.open_user_link, null));
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void checkSpinner() {
        if(splash.isChecked()) {
            timeSpinner.setVisibility(View.VISIBLE);
            timeTv.setTextColor(getResources().getColor(R.color.colorGroupText));
        }
        else {
            timeSpinner.setVisibility(View.INVISIBLE);
            timeTv.setTextColor(getResources().getColor(R.color.colorPressedTab));
        }
    }
}
