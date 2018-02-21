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

public class CustomSpinnerAdapter extends BaseAdapter {

    Context context;
    List<GroupItem> groups;
    LayoutInflater inflater;

    public CustomSpinnerAdapter(Context applicationContext, List<GroupItem> list) {
        this.context = applicationContext;
        this.groups = list;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return groups.size();
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
        view = inflater.inflate(R.layout.spinner_group_item, null);
        TextView name = view.findViewById(R.id.tv_name_group);
        name.setText(groups.get(i).getName());
        return view;
    }
}

