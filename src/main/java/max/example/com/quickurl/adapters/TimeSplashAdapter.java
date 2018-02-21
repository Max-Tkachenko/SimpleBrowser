package max.example.com.quickurl.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import max.example.com.quickurl.R;
import max.example.com.quickurl.model.GroupItem;

public class TimeSplashAdapter extends BaseAdapter {

    Context context;
    String[] groups = { "2.0 s", "3.5 s", "5.0 s"};
    LayoutInflater inflater;

    public TimeSplashAdapter(Context applicationContext) {
        this.context = applicationContext;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return groups.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.splash_spinner_time, null);
        TextView name = view.findViewById(R.id.time_tv);
        name.setText("   " + groups[i] + "   ");
        return view;
    }
}