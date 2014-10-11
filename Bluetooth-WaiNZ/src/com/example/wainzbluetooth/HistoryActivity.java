package com.example.wainzbluetooth;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.example.wainzbluetooth.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

public class HistoryActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		ListView listView = (ListView) findViewById(R.id.list);

		Context c = getApplicationContext();

		SubmissionSaver s = new SubmissionSaver();

		@SuppressWarnings("static-access")
		JSONArray json = s.getJsonArray(c);

		// json = new JSONArray();

		if (json != null) {

			List<RiverData> models = new ArrayList<RiverData>(json.length());

			for (int i = 0; i < json.length(); i++) {
				try {
					models.add(new RiverData(c, json.getJSONObject(i)));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			HistoryAdapter adapter = new HistoryAdapter(this, models);

			listView.setAdapter(adapter);

			adapter.notifyDataSetChanged();
		}

	}

	public void onBackPressed(){
	     Intent i = new Intent(HistoryActivity.this, MainActivity.class);
	     startActivity(i);
	}
}
