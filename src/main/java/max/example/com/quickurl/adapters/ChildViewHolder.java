package max.example.com.quickurl.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import max.example.com.quickurl.R;

public class ChildViewHolder {
    public TextView text;
    public ImageView imageview;

    public ChildViewHolder(View v) {
        this.text = v.findViewById(R.id.child_text);
        this.imageview =  v.findViewById(R.id.child_image);
    }
}