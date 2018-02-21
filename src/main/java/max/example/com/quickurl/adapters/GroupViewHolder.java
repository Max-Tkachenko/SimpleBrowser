package max.example.com.quickurl.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import max.example.com.quickurl.R;

public class GroupViewHolder {
    public TextView text;
    public ImageView imageview;

    public GroupViewHolder(View v) {
        this.text = v.findViewById(R.id.group_text);
        this.imageview =  v.findViewById(R.id.group_image);
    }
}