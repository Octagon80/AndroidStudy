package com.CellIdTrack.main;

import com.CellIdTrack.main.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.widget.Toast;
import android.os.IBinder;
import android.content.IntentFilter;
import com.CellIdTrack.main.CellIdService.LocalBinder;

public class CellIdStart  extends Activity implements OnClickListener {
	  private static final String TAG = "CellIdStart";
	  Button buttonStart, buttonStop;
	  private WakeLock mWakelock = null;
	  boolean mBounded;
	  CellIdService mServer;
	  AlarmReceiver receiver;
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  //System.out.println(TAG + "onCreate event");
		  /*
		     mediaController.setMediaPlayer(this);
  LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
  View layout = inflater.inflate(R.layout.main, (ViewGroup) findViewById(R.id.main_program_view));
  mediaController.setAnchorView(layout); 
		    */
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    buttonStart = (Button) findViewById(R.id.buttonStart);
	    buttonStop = (Button) findViewById(R.id.buttonStop);

	    buttonStart.setOnClickListener(this);
	    buttonStop.setOnClickListener(this);
	    
        
        IntentFilter filter = new IntentFilter(CellIdService.NEW_FOUND);
        receiver = new AlarmReceiver();
        registerReceiver(receiver, filter);
        
        Intent intent = new Intent(this, CellIdService.class); 
        bindService(intent, mConnection, BIND_AUTO_CREATE);
	  }

	    @Override
	    protected void onResume()
	    {
	    	Log.d(TAG, "onResume event");
	        super.onResume();
	    }
	    
	    @Override
	    protected void onStart()
	    {
	    	Log.d(TAG, "onStart event");
	    	
	    	PowerManager pwrMgr = (PowerManager) this.getSystemService(POWER_SERVICE);
	        mWakelock = pwrMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Accel");
	        mWakelock.acquire();
	        
	        super.onStart();
	    }	    

	    

	    @Override
	    protected void onPause()
	    {
	    	Log.d(TAG, "onPause event");
	    	
	        super.onPause();
	    }

	    @Override
	    protected void onDestroy()
	    {
	    	Log.d(TAG, "onDestroy event");
	    	mWakelock.release();
	    	  if(mBounded) {
	    		   unbindService(mConnection);
	    		   mBounded = false;
	    		  }
	    	 unregisterReceiver(receiver);
	        super.onDestroy();

	    }
	    
	    ServiceConnection mConnection = new ServiceConnection() {
	    	  
	    	  public void onServiceDisconnected(ComponentName name) {
	    	   mBounded = false;
	    	   mServer = null;
	    	  }
	    	  
	    	  public void onServiceConnected(ComponentName name, IBinder service) {
	    	   //Toast.makeText(CellIdStart.this, "Service is connected", Toast.LENGTH_SHORT ).show();
	    	   mBounded = true;
	    	   LocalBinder mLocalBinder = (LocalBinder)service;
	    	   mServer = mLocalBinder.getServerInstance();
	    	  }
	    	 };
	    	 
	    
	  public void onClick(View src) {
		//  Log.d(TAG, "onClick event");
	  switch (src.getId()) {
	    case R.id.buttonStart:
	      Log.d(TAG, "onClick: starting srvice");
	     // Toast.makeText( this, "Запускаем сервис", Toast.LENGTH_SHORT).show();
	      Intent intent = new Intent(this, CellIdService.class);
	      //startService(intent);
	      bindService(intent, mConnection, BIND_AUTO_CREATE);
	      break;
	    case R.id.buttonStop:
	      Log.d(TAG, "onClick: stopping srvice");
	      //System.out.println(TAG + "onClick: stopping srvice");
	      if(mBounded) {
	    	   unbindService(mConnection);
	    	   mBounded = false;
	    	  }
	      break;
	    }
	  }
	}
