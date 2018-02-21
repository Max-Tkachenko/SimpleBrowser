package max.example.com.quickurl.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import max.example.com.quickurl.R;
import max.example.com.quickurl.adapters.SimplePagerAdapter;
import me.relex.circleindicator.CircleIndicator;


public class ManualActivity extends AppCompatActivity {

    Button toMainButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String parent = getIntent().getStringExtra("parent");

        setContentView(R.layout.manual_layout);
        getSupportActionBar().setTitle("Manual");

        LayoutInflater inflater = LayoutInflater.from(this);
        List<View> pages = new ArrayList<View>();

        View page = inflater.inflate(R.layout.manual_1, null);
        pages.add(page);
        page = inflater.inflate(R.layout.manual_2, null);
        pages.add(page);
        page = inflater.inflate(R.layout.manual_3, null);
        pages.add(page);
        page = inflater.inflate(R.layout.manual_4, null);
        pages.add(page);
        page = inflater.inflate(R.layout.manual_5, null);
        toMainButton = page.findViewById(R.id.to_main_button);
        pages.add(page);

        SimplePagerAdapter pagerAdapter = new SimplePagerAdapter(pages);
        ViewPager viewPager = findViewById(R.id.view_pager);
        CircleIndicator indicator = findViewById(R.id.indicator_1);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);

        indicator.setViewPager(viewPager);

        if(parent.equals("splash")) { toMainButton.setText("Start!"); }
        else { toMainButton.setText("Back"); }

        toMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(parent.equals("splash")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    ActivityCompat.finishAffinity(ManualActivity.this);
                }
                else { finish(); }
            }
        });
    }
}