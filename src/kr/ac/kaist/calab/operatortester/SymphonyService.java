package kr.ac.kaist.calab.operatortester;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nclab.partitioning.interfaces.IPartitioningInterface;

public final class SymphonyService
{
	//=========================================================================
	// PUBLIC 메소드
	//=========================================================================
	private ServiceConnection conn;
	
	public static SymphonyService getInstance()
	{
		// 인스턴스가 존재하지 않으면 처리한다.
		if (s_instance == null)
		{
			// 인스턴스를 생성한다.
			s_instance = new SymphonyService();
		}

		// 인스턴스를 반환한다.
		return s_instance;
	}

	public void startService(Context context)
	{
		// 서비스 인텐트를 생성한다.
		final Intent serviceIntent = new Intent(SERVICE_ACTION);
		serviceIntent.setPackage("com.nclab.partitioning.service");

		// 서비스를 연결한다.
		conn = new InnerServiceConnection();
		context.bindService(serviceIntent, conn, Activity.BIND_AUTO_CREATE);
	}

	public void stopService(Context context)
	{
		//	(jungi) stopService --> unbindeService
		if (conn != null) {
			context.unbindService(conn);
		}
	}

	public boolean isBinded()
	{
		// 인터페이스가 존재하는지 여부를 반환한다.
		return m_interface != null;
	}

	public void setServiceConnection(ServiceConnection serviceConnection)
	{
		// 서비스 연결 객체를 설정한다.
		m_serviceConnection = serviceConnection;
	}

	public void updateTaskType(String taskType)
	{
		// 인터페이스가 존재하면 처리한다.
		if (m_interface != null)
		{
			try
			{
				// 기능을 처리한다.
				m_interface.updateTaskType(taskType);
			}
			catch (RemoteException e)
			{
				// 예외를 로그에 기록한다.
				Log.d("SymphoneyService",e.toString());
			}
		}
	}

	public int registerQuery(String string)
	{
		// 인터페이스가 존재하면 처리한다.
		if (m_interface != null)
		{
			try
			{
				// 기능을 처리한다.
				return m_interface.registerQuery(string);
			}
			catch (RemoteException e)
			{
				// 예외를 로그에 기록한다.
				Log.d("SymphoneyService",e.toString());
			}
		}

		// 빈 값을 반환한다.
		return -1;
	}

	public int deregisterQuery(int queryId)
	{
		// 인터페이스가 존재하면 처리한다.
		if (m_interface != null)
		{
			try
			{
				// 기능을 처리한다.
				return m_interface.deregisterQuery(queryId);
			}
			catch (RemoteException e)
			{
				// 예외를 로그에 기록한다.
				Log.d("SymphoneyService",e.toString());
			}
		}

		// 빈 값을 반환한다.
		return -1;
	}

	public void startLogging(String string)
	{
		// 인터페이스가 존재하면 처리한다.
		if (m_interface != null)
		{
			try
			{
				// 기능을 처리한다.
				m_interface.startLogging(string);
			}
			catch (RemoteException e)
			{
				// 예외를 로그에 기록한다.
				Log.d("SymphoneyService",e.toString());
			}
		}
	}

	public void stopLogging()
	{
		// 인터페이스가 존재하면 처리한다.
		if (m_interface != null)
		{
			try
			{
				// 기능을 처리한다.
				m_interface.stopLogging();
			}
			catch (RemoteException e)
			{
				// 예외를 로그에 기록한다.
				Log.d("SymphoneyService",e.toString());
			}
		}
	}

	//=========================================================================
	// 서비스 연결
	//=========================================================================

	private class InnerServiceConnection implements ServiceConnection
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			// 인터페이스를 얻는다.
			m_interface = IPartitioningInterface.Stub.asInterface(service);

			// 서비스 연결 객체가 존재하면 처리한다.
			if (m_serviceConnection != null)
			{
				// 서비스 연결을 처리한다.
				m_serviceConnection.onServiceConnected(name, service);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			// 인터페이스를 초기화한다.
			m_interface = null;

			// 서비스 연결 객체가 존재하면 처리한다.
			if (m_serviceConnection != null)
			{
				// 서비스 연결 해제를 처리한다.
				m_serviceConnection.onServiceDisconnected(name);
			}
		}
	}

	//=========================================================================
	// 상수 선언
	//=========================================================================

	// 서비스 액션
	private static final String SERVICE_ACTION = "com.nclab.partitioning.DEFAULT";

	//=========================================================================
	// 변수 선언
	//=========================================================================

	// 인스턴스
	private static SymphonyService s_instance;

	// 인터페이스
	private IPartitioningInterface m_interface;

	// 서비스 연결 객체
	private ServiceConnection m_serviceConnection;
}