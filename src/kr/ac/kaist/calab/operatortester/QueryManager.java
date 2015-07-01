package kr.ac.kaist.calab.operatortester;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * QueryManager for Symphony.
 * 
 * To connect to the Symphony.
 * 1. Bind SymphonyService(PartitioningService)
 * 2. Register Query
 * 
 * To disconnect from the Symphony
 * 1. Unbind SymphonyService(PartitioningService). This will deregister all the queries.
 * 
 * @author yulistic
 *
 */
public class QueryManager implements ServiceConnection {
	
	public List<Integer> mQueryIds;	//Registered query IDs.
	public List<String> mRegisteredContexts;	//Registered contexts.
	public String[] selectedQuery;
	
	public QueryManager(){
		
		mQueryIds = new ArrayList<Integer>();	//Registered query IDs.
		mRegisteredContexts = new ArrayList<String>();	//Registered contexts.
	}
	
	/**
	 * Register query.
	 * @param queries A new query.
	 * @param queryIdsList List of Integer to which a new query id will be stored.
	 */
	public void registerQuery(String[] queries) {
		if (queries == null || queries.length==0)
			Log.e("OperatorTester", "[QueryManager] There is no query.");
		try {
			for (String query : queries) {
				final String[] queryTokens = query.split(" ");
				final String _query = "CONTEXT " + queryTokens[0]
						+ " INTERVAL " + queryTokens[1] + " " + queryTokens[2]
						+ " DELAY " + queryTokens[3];

				final int queryId = SymphonyService.getInstance()
						.registerQuery(_query);

				// Add to query id list and context list.
				System.out.println("[JYKIM] register queryID:"+queryId);
				mQueryIds.add(queryId);
				mRegisteredContexts.add(queryTokens[0]);
				
				// Logging.
				Log.d("OperatorTester", "[QueryManager] registerQuery:" + _query + " to "
						+ "queryID:"+queryId);
			}
		} catch (Exception e) {
			Log.d("OperatorTester", e.toString());
		}
	}
	
	/**
	 * Deregister all the queries in the query ID list.
	 * @param queryIdsList
	 */
	public void deregisterAllQueries() {
		try {
			for (int queryId : mQueryIds) {
				SymphonyService.getInstance().deregisterQuery(queryId);
				// Logging.
				Log.d("OperatorTester", "[JYKIM] Deregistered queryID:" + queryId);
			}
			mQueryIds.clear();
		} catch (Exception e) {
			Log.d("OperatorTester","[QueryManager]"+ e.toString());
		}
	}
	
	public void bindPartitionService(Context context, String[] query) {
		selectedQuery = query;
		
		Toast.makeText(context, selectedQuery[0]+" is binded", Toast.LENGTH_SHORT).show();
		
		// 심포니 서비스의 연결 객체를 설정한다.
		SymphonyService.getInstance().setServiceConnection(this);
		// 심포니 서비스를 시작한다.
		SymphonyService.getInstance().startService(context);
	}
	
	public void unbindPartitionService(Context context) {
		SymphonyService.getInstance().stopLogging();

		// 심포니 정보 서비스의 연결 객체를 초기화한다.
		SymphonyService.getInstance().setServiceConnection(null);

		// 심포니 서비스를 중단한다.
		SymphonyService.getInstance().stopService(context);
		
		Log.d("OperatorTester", "unbindPartitionService finished");
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// 심포니 서비스가 바인드 되어있지 않다면 함수를 종료한다.
		if (SymphonyService.getInstance().isBinded() == false)
			return;

		Log.d("OperatorTester", "[QueryManager] onServiceConnected");

		// 작업 형식을 갱신한다. MW or W or....
		// SymphonyService.getInstance().updateTaskType("MW");

		SymphonyService.getInstance().startLogging("/sdcard/calab_test");
		mRegisteredContexts.clear();
		mQueryIds.clear();

		final String[] soundQuery = { "PLACE 10000 10000 6000" }; 
		registerQuery(soundQuery);
		registerQuery(selectedQuery);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d("OperatorTester", "[QueryManager] ServiceDisconnected.");
	}
}
