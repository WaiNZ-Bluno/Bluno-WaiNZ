package com.example.wainzbluetooth;

import java.util.List;
import com.example.wainzbluetooth.R;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HistoryAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<RiverData> RiverDataItems;

    public HistoryAdapter(Activity activity, List<RiverData> RiverDataItems) {
        this.activity = activity;
        this.RiverDataItems = RiverDataItems;
    }

    @Override
    public int getCount() {
        return RiverDataItems.size();
    }

    @Override
    public Object getItem(int location) {
        return RiverDataItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        //Get the riverdata at the current position
        RiverData data = RiverDataItems.get(position);

        ProgressBar health = (ProgressBar) convertView.findViewById(R.id.progressBar2);
        //If the android version is less then 12 we can't use animations when populating the list, then again the app doesn't actually support
        //anything lower the 18/19 cause LE, so yeah yay for android support we don't need.
        if(android.os.Build.VERSION.SDK_INT >= 11){
            ObjectAnimator animation = ObjectAnimator.ofInt(health, "progress", 0, data.CompareRiver());
            animation.setDuration(1000);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
        else
        	health.setProgress(data.CompareRiver());
        //Links to the results page when the list item is clicked.
        convertView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent i = new Intent(activity, ResultsActivity.class);
				i.putExtra("RiverData", RiverDataItems.get(position).toJson().toString());
				activity.startActivity(i);
			}});
        
        TextView date = (TextView) convertView.findViewById(R.id.date);

        if(date != null)

        date.setText("Reading taken on: " + data.readingDate);

        return convertView;
    }

}