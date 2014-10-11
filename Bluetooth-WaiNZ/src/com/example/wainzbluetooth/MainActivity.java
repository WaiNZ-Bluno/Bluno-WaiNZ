package com.example.wainzbluetooth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.wainzbluetooth.R;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends BlunoLibrary {
	public static final String JSON_MESSAGE = "INTENT_MESSAGE_COLLECTED_DATA";
	private Button buttonScan;
	private Button historyButton;
	private Button testWaterQuality;
	private TextView connectionUpdates;
	private WizardState wizardState;

	public enum WizardState {
		initial, idle, error, complete
	};

	public enum ResponseState {
		idle, fatal, bt4le, temp, ec, ph, water, busy
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		onCreateProcess();

		serialBegin(115200);

		TextView connectionUpdate = (TextView) findViewById(R.id.connection_updates);
		connectionUpdate.setText("Please connect to device");
		connectionUpdate.setAlpha(0.5f);
		connectionUpdates = connectionUpdate;

		wizardState = WizardState.initial;

		testWaterQuality = (Button) findViewById(R.id.start_button);
		//Here we are sending the start test command to the bluetooth device, because one of the teams wanted to save the location
		//and time to a sd card we include this information in the start message
		testWaterQuality.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				JSONObject j = new JSONObject();
				UserLocationTracker t = new UserLocationTracker(
						getApplicationContext());
				t.getLocation();

				try {
					j.put("cmd", "test");
					j.put("session", "AD");
					String gpsData = String.valueOf(t.getLat()) + " "
							+ String.valueOf(t.getLon());
					j.put("gps", gpsData);
					String currTime = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss", Locale.getDefault())
							.format(Calendar.getInstance().getTime());

					j.put("time", currTime);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				serialSend(j.toString());
				connectionUpdates.setText("Initialising device");
				testWaterQuality.setEnabled(false);
				testWaterQuality.setBackgroundColor(getResources().getColor(
						R.color.button_pressed));

			}
		});

		buttonScan = (Button) findViewById(R.id.scan_button);

		buttonScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonScanOnClickProcess();
			}
		});

		historyButton = (Button) findViewById(R.id.history_button);

		historyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(MainActivity.this, HistoryActivity.class);

				startActivity(i);

			}

		});
	}

	protected void onResume() {
		super.onResume();
		onResumeProcess(); // onResume Process by BlunoLibrary
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);
		// onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		onPauseProcess();
		// onPause Process by BlunoLibrary
	}

	protected void onStop() {
		super.onStop();
		onStopProcess();
		// onStop Process by BlunoLibrary
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		onDestroyProcess();
		// onDestroy Process by BlunoLibrary
	}

	String message = "";

	@Override
	public void onSerialReceived(String data) {
		// Once connection data received, this function will be called
		// We need to keep listening until we get the complete message (data is recieved 20bytes at a time)
		// Once it is fully read we will choose the correct action to take (move to results or set the device to idle)
		if (!data.contains("}")) {
			message += data;
		} else {

			message += data;

			message = message.split("\r")[0];

			JSONObject j;

			try {
				j = new JSONObject(message);

				if (WizardState.initial == wizardState) {
					String status = j.getString("status");

					if (status.equalsIgnoreCase("fatal")) {
						wizardState = WizardState.error;
						connectionUpdates
								.setText("Error in initialising device");
					} else if (status.equalsIgnoreCase("idle")) {
						wizardState = WizardState.idle;
						connectionUpdates
								.setText("Device is ready, please start the test");
					} else {
						throw new AssertionError("UNKNOWN STATE");
					}
				} else if (WizardState.idle == wizardState) {
					String status = j.getString("status");

					String message = "";

					if (status.equalsIgnoreCase("complete")) {

						wizardState = WizardState.idle;

						SubmissionSaver.saveSubmission(j, this);

						Intent i = new Intent(this, ResultsActivity.class);

						i.putExtra("RiverData", j.toString());

						testWaterQuality.setEnabled(true);
						testWaterQuality.setBackgroundResource(R.drawable.riverwatch_button);
						startActivity(i);

					} else if (status.equalsIgnoreCase("busy")) {
						message = "Please wait for test results";
					} else {

						wizardState = WizardState.error;

						if (status.equalsIgnoreCase("fatal")) {
							message = "A fatal error has occured";
						} else if (status.equalsIgnoreCase("bt4le")) {
							message = "There's a Bluetooth connection issue, please check device settings";
						} else if (status.equalsIgnoreCase("temp")) {
							message = "Temperature is to high/low please remove Bluetooth device from water";
						} else if (status.equalsIgnoreCase("ec")) {
							message = "Issue occured while measuring the electrical conductivity, please remove from water";
						} else if (status.equalsIgnoreCase("water level low")) {
							message = "Device not submerged in water deeply enough, please restart test";
						} else {
							message = "An unknown exception has occurred, please restart the test";
						}
					}

					 connectionUpdates.setText(message);


				} else {
					// TODO do we want the user to see this or remove after
					// debugging?
					String message = "Unrecognised data received from device";
					connectionUpdates.setText(message);
					// Reset the device when it sends us unknown data
					JSONObject n = new JSONObject();
					n.put("cmd", "reset");
					serialSend(n.toString());
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			message = "";

		}

	}

	@Override
	public void onConectionStateChange(
			connectionStateEnum theconnectionStateEnum) {
		switch (theconnectionStateEnum) { // Four connection state

		case isConnected:
			buttonScan.setText("Connected");
			JSONObject jason = new JSONObject();
			try {
				jason.put("dev", "AD");
				jason.put("cmd", "init");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			serialSend(jason.toString());
			buttonScan.setVisibility(View.GONE);
			testWaterQuality.setVisibility(View.VISIBLE);
			testWaterQuality.setEnabled(true);
			testWaterQuality.setBackgroundResource(R.drawable.riverwatch_button);


			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Scan");
			break;
		case isScanning:
			buttonScan.setText("Scanning");
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
			buttonScan.setVisibility(View.VISIBLE);
			testWaterQuality.setVisibility(View.GONE);
			testWaterQuality.setBackgroundColor(getResources().getColor(
					R.color.button_pressed));

			break;
		default:
			break;
		}
	}
}