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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	private QueryManager mQueryManager;
	BroadcastReceiver contextReceiver;	//Receive context from Symphony.
	private int turnedOnBtnNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mQueryManager = new QueryManager();
		mQueryManager.bindPartitionService(this);
		
		initView();
		registerBR();
		turnedOnBtnNum = 0;
	}
	
	private void initView(){
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinner.setAdapter(adapter);
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
					Log.d("OpertorTester", "ContextType(QueryName):"+receivedContext);
				}else{
					Log.e("OpertorTester", "Received context in ContextReceiver is null.");
				}
				
				if (receivedResult != null){
					Log.d("OpertorTester", "Result:"+receivedResult);
				}else{
					Log.e("OpertorTester", "Received result in ContextReceiver is null.");
				}
				
				//checkSymphonyStatus();
			}
			
		};
		IntentFilter filter = new IntentFilter("com.nclab.partitioning.DEFAULT");
		registerReceiver(contextReceiver, filter);
	}
	
	public void onToggleClicked(View view){
		int btnId = view.getId();
		String[] query;
		switch (btnId){
		case R.id.toggleButton1:	//id 이름 바꿔야함.
			query = new String[]{ "SOUND 3780 10080 3000" };	//쿼리 적절한 것으로 바꿔야 함.
			break;
		/*case R.id.toggleButton2:
			query = new String[]{ "EVENT 945 1400 775" };
			break;*/
		default:
			Log.e("OpertorTester", "There is no such button with ID:"+btnId);
			return;
		}
		
		boolean on = ((ToggleButton)view).isChecked();
		if (on){
			turnedOnBtnNum++;
			if (turnedOnBtnNum == 1){
				Log.d("OperatorTester", "First button turned on!!");
				//mQueryManager.bindPartitionService(this);
			}
			mQueryManager.registerQuery(query);
		}else{
			mQueryManager.deregisterAllQueries();	//TODO deregister all the query.
			turnedOnBtnNum--;
			
			//mQueryManager.unbindPartitionService(this);
			//Toast.makeText(this, "All the queries are deregistered.", Toast.LENGTH_SHORT).show();
		}
	}
	
	// Refresh Symphony running status when clicked.
	public void onRefreshClicked(View view){
		checkSymphonyStatus();
	}
	
	// Check whether Symphony is running or not.
	private void checkSymphonyStatus() {
		ActivityManager am = (ActivityManager)((Context)MainActivity.this).getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> listOfApps = am.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo procinfo : listOfApps) {
			if (procinfo.processName.contains("com.nclab.partitioning.service")) {
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
		super.onDestroy();
		Log.e("OperatorTester", "onDestroyed() called.");
		mQueryManager.deregisterAllQueries();
		mQueryManager.unbindPartitionService(this);
		unregisterReceiver(contextReceiver);
	}
	
}
