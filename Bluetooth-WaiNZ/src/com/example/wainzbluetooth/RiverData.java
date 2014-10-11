package com.example.wainzbluetooth;

import org.json.*;

import android.content.Context;

public class RiverData {

	Float lat, lon, temperature, conductivity;

	String readingDate;

	String id, session;

	Context context;

	public RiverData(Context c, JSONObject j){
		this.context = c;
		this.PopulateModel(j);
	}

	public void PopulateModel(JSONObject j){

		try{
			//Get strings from JSON
			//String status = j.getString("status");//assuming test status will be in data packet
			//id = j.getString("id");
			session = j.getString("session");
			readingDate = j.getString("time");
		    conductivity = Float.parseFloat(j.getString("ec"));
			temperature = Float.parseFloat(j.getString("temp"));
			String[] gps = j.getString("gps").split(" - ");
			if(gps.length > 1){
				lat = Float.parseFloat(gps[0]);
				lon = Float.parseFloat(gps[1]);
			} else {
				//get current location
				UserLocationTracker l = new UserLocationTracker(context);
				l.getLocation();
				lat = l.getLat();
				lon = l.getLon();
			}
			System.out.printf("Date: %s, Conductivity: %f, Temperature: %f \n", readingDate, conductivity, temperature);

		}
		catch (JSONException e){
			e.printStackTrace();
		}
	}

	public int CompareConductivity(){
		if (conductivity > 1500.0){
			return 2;
			}
		else if(conductivity > 1000.0){
			return 23;
		}
		else if (conductivity != Float.NaN){
			return 46;
			}
		return 0;
	}

	public int CompareTemperature(){
		if (temperature < 25.0){
			return 46;
		}
		else if (temperature < 30.0){
			return 23;
		}
		else if (temperature != Float.NaN){
			return 2;
		}
		return 0;
	}

	public int CompareRiver(){
		int temp = CompareTemperature();
		int cond = CompareConductivity();
		return (temp+cond);
	}

	public float getTemperature() {
		return temperature;
	}

	public float getConductivity() {
		return conductivity;
	}

	public float getLat() {
		return lat;
	}

	public float getLon() {
		return lon;
	}

	public String getReadingDate() {
		return readingDate;
	}

	public String getSession() {
		return session;
	}

	public JSONObject toJson(){
		JSONObject jason = new JSONObject();
		try {
			//jason.put("id", id);
			jason.put("session", session);
			jason.put("gps", lat.toString() +" " + lon.toString());
			jason.put("time", readingDate);
			jason.put("ec", conductivity);
			jason.put("temp", temperature);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jason;
	}
}
