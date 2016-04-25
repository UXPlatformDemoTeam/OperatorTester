package kr.ac.kaist.calab.operatortester;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	private QueryManager mQueryManager;
	BroadcastReceiver contextReceiver;	//Receive context from Symphony.
	private String[] selectedContextType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mQueryManager = new QueryManager();
		
		initView();
		registerBR();
	}
	
	private void initView(){
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinner.setAdapter(adapter);
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				selectedContextType = new String[] {(String) parent.getItemAtPosition(pos)};
				Toast.makeText(MainActivity.this, selectedContextType[0], Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		checkSymphonyStatus();
	}
	
	// Register Broadcast Receiver.
	private void registerBR(){
		if (contextReceiver != null)
			return;
		contextReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String receivedContext = intent.getStringExtra("context");
				String receivedResult = intent.getStringExtra("result");
		
				if (receivedContext != null){		//Send result to MainActivity.
					if (receivedContext.equals("PLACE"))	// PLACE query is always registered together.
						return;
					Log.d("OpertorTester", "ContextType(QueryName):"+receivedContext);
				}else{
					Log.e("OpertorTester", "Received context in ContextReceiver is null.");
				}
				
				if (receivedResult != null){
					Log.d("OpertorTester", "Result:"+receivedResult);
				}else{
					Log.e("OpertorTester", "Received result in ContextReceiver is null.");
				}
				
				if (receivedContext != null & receivedResult != null){
					TextView tv = (TextView)findViewById(R.id.logTextView);
					tv.append("플로우:"+receivedContext + "\t 결과값:"+receivedResult +"\n");
				}
				//checkSymphonyStatus();
			}
		};
		IntentFilter filter = new IntentFilter("com.nclab.partitioning.DEFAULT");
		registerReceiver(contextReceiver, filter);
	}
	
	public void onToggleClicked(View view){
		boolean on = ((ToggleButton)view).isChecked();
		if (on){
			mQueryManager.bindPartitionService(this, selectedContextType);
		}else{
			mQueryManager.deregisterAllQueries();	//TODO deregister all the query.
			mQueryManager.unbindPartitionService(this);
			Toast.makeText(this, "Unbind.", Toast.LENGTH_SHORT).show();
		}
	}
	
	// Refresh Symphony running status when clicked.
	public void onRefreshClicked(View view){
		checkSymphonyStatus();
	}
	
	// Erase log button clicked.
	public void onEraseLogClicked(View view){
		TextView tv = (TextView)findViewById(R.id.logTextView);
		tv.setText("");
	}

	// Check whether Symphony is running or not.
	private void checkSymphonyStatus() {
		ActivityManager am = (ActivityManager)((Context)MainActivity.this).getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> listOfApps = am.getRunningServices(Integer.MAX_VALUE);

		for (ActivityManager.RunningServiceInfo procinfo : listOfApps) {
			if (procinfo.service.getPackageName().contains("com.nclab.partitioning.service")) {
				setSymphonyStatus(true);
				return;
			}
		}
		setSymphonyStatus(false);
	}
	
	// Set TextView status.
	private void setSymphonyStatus(boolean isRunning){
		TextView symphonyStatus = (TextView)findViewById(R.id.symphonyStatus);
		if (isRunning){
			symphonyStatus.setText(R.string.on);
			symphonyStatus.setTextColor(Color.GREEN);
		}else {
			symphonyStatus.setText(R.string.off);
			symphonyStatus.setTextColor(Color.RED);
		}
	}
	
	@Override
	protected void onDestroy(){
		Log.e("OperatorTester", "onDestroyed() called.");
		super.onDestroy();
		ToggleButton btn = (ToggleButton)findViewById(R.id.toggleButton1);
		if (btn.isChecked()){
			mQueryManager.deregisterAllQueries();
			mQueryManager.unbindPartitionService(this);
		}
		unregisterReceiver(contextReceiver);
	}
	
}
