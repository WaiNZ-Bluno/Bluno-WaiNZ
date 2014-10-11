package com.example.wainzbluetooth;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.wainzbluetooth.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ResultsActivity extends FragmentActivity {

	//BUTTONS
	private Button submitData;
	private Button discardData;

	//DATA
	private static JSONObject jason;

	//private Map<String, String> data = new HashMap<String, String>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);

		Intent intent = getIntent();
		RiverData data = null;
		try {
			jason = new JSONObject(intent.getStringExtra("RiverData"));
			data = new RiverData(this, jason);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ProgressBar condPb = (ProgressBar) this.findViewById(R.id.conductivity_progress);
		ProgressBar tempPb = (ProgressBar) this.findViewById(R.id.temperature_progress);

		int cond = data.CompareConductivity();

		if (cond !=0){
		    condPb.setProgress(cond);
		}
		else{
			condPb.setProgress(0);
		}
		int tmpr = data.CompareTemperature();
		if (tmpr !=0){
		    tempPb.setProgress(tmpr);
		}
		else{
			tempPb.setProgress(0);
		}

		TextView condTxt = (TextView) this.findViewById(R.id.cond_val);
		condTxt.setText("Conductivity: " + data.getConductivity()+"μS/cm");

		TextView tmprTxt = (TextView) this.findViewById(R.id.tmpr_val);
		tmprTxt.setText("Temperature: " + data.getTemperature()+"°C");

		TextView latlon = (TextView) this.findViewById(R.id.lat_long);
		latlon.setText("Lat/Long: " + data.getLat()  + ", " + data.getLon());

		TextView time = (TextView) this.findViewById(R.id.date_time);
		time.setText("Time and date: " + data.getReadingDate());


		//MAP
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		/*
		UserLocationTracker t = new UserLocationTracker(getApplicationContext());
		t.getLocation();
		*/
		MapFragment map = new MapFragment();

		map.lat = data.getLat();
		map.lon = data.getLon();

		fragmentTransaction.add(R.id.results_map_frame, map);
		fragmentTransaction.commit();

		//SUBMIT BUTTON LISTENER
		submitData = (Button) findViewById(R.id.submit_data_button);
		submitData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				execute();
				Intent i = new Intent(ResultsActivity.this, HistoryActivity.class);
				startActivity(i);
			}
		});

		//DISCARD BUTTON LISTENER
		discardData = (Button) findViewById(R.id.discard_data_button);
		discardData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println(SubmissionSaver.removeEntry(jason, ResultsActivity.this));
				Intent i = new Intent(ResultsActivity.this, HistoryActivity.class);
				startActivity(i);
			}
		});


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.results, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		return super.onOptionsItemSelected(item);
	}

	public static void execute() {
		HttpResponse response = makeRequest("http://www.wainz.org.nz/api/image", jason.toString());
		if(response != null)
		System.out.println(response.getParams());
		else
		System.out.println("something went wrong");
	}

	public static HttpResponse makeRequest(String uri, String json) {
	    try {
	        HttpPost httpPost = new HttpPost(uri);
	        httpPost.setEntity(new StringEntity(json));
	        httpPost.setHeader("Accept", "application/json");
	        httpPost.setHeader("Content-type", "application/json");
	        return new DefaultHttpClient().execute(httpPost);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public void onBackPressed(){
	     Intent i = new Intent(ResultsActivity.this, MainActivity.class);
	     startActivity(i);
	}

}
