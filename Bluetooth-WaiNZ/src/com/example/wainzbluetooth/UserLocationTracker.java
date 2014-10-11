package com.example.wainzbluetooth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

public class UserLocationTracker extends Service implements LocationListener{

	private final Context context;

	boolean gpsEnabled;
	boolean networkEnabled;
	boolean systemError;
	boolean canGetLocation;

	Location location;

	float lat, lon;

	private static final float minChangeForUpdate = 10;
	private static final long minTimeBetweenUpdates = 60000;

	protected LocationManager locationManager;

	public UserLocationTracker(Context context){
		this.context = context;
		getLocation();
	}

	@SuppressLint("InlinedApi")
	public Location getLocation(){
		locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

		gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if(!gpsEnabled && !networkEnabled){
			//something has gone terribly wrong.
		} else {
			this.canGetLocation = true;
			if(gpsEnabled){
				if (location == null){
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeBetweenUpdates, minChangeForUpdate, this);
					if(locationManager != null){
						location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if(location != null){
							lat = (float)location.getLatitude();
							lon = (float)location.getLongitude();
						}
					}
				}
			}

			if(networkEnabled){
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeBetweenUpdates, minChangeForUpdate, this);
				if(locationManager != null){
					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if(location != null){
						lat = (float)location.getLatitude();
						lon = (float)location.getLongitude();
					}
				}
			}
		}
		return location;
	}

	public float getLat(){
		return (location != null) ? (float)location.getLatitude() : lat;
	}

	public float getLon(){
		return (location != null) ? (float)location.getLongitude() : lon;
	}

	public boolean canGetLocation(){
		return this.canGetLocation;
	}

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will launch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
