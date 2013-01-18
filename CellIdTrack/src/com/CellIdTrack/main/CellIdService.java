package com.CellIdTrack.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Calendar;
import android.os.SystemClock;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.Binder;
import android.os.IBinder;
//import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;
import android.app.PendingIntent;
import android.app.AlarmManager;
import java.lang.Thread;
import android.text.format.Time;


	
public class CellIdService extends Service {
	private static final String TAG = "CellIdService";
	Thread thr;
	public boolean ThreadMustStop = false;
	
	 AlarmManager alarms;
     PendingIntent alarmIntent; 
	 int updateFreq = 60;//сек
	  public static final String NEW_FOUND = "New_Found";
	  
		private String filename = "ldata.txt";
		
	    /* These variables need to be global, so we can used them onResume and onPause method to
	    stop the listener */
	 private TelephonyManager Tel;
	 private MyPhoneStateListener MyListener;
	 
	 //private boolean isListenerActive = false;

	 /*  These variables need to be global so they can be saved when the activity exits
	  *  and reloaded upon restart.
	  */

	 private long LastCellId = 0;
	 
	 private long LastLacId = 0;

	 private long PreviousCells[] = new long [4];
	 private int  PreviousCellsIndex = 0;
	 
	 
	  private Location CurrentLocation = null;

	 private Location PrevLocation = null;

  	 long NewCellId = 0; 
  	 long NewLacId = 0;
  	 double DistanceToLastCell = 0;
  	 boolean NeedToWrite = false;
  	 
  	 String outputText; 
	 
  //	private MyPhoneStateListener MyListener;
  	
	 
	 IBinder mBinder = new LocalBinder();

	 @Override
	 public IBinder onBind(Intent intent) {
	  return mBinder;
	 }

	 public class LocalBinder extends Binder {
	  public CellIdService getServerInstance() {
	   return CellIdService.this;
	  }
	 }
	
	@Override
	public void onCreate() {
		//Toast.makeText(this, "Сервис onCreate", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onCreate");
	    
		/* Initialize PreviousCells Array to defined values */
		for (int x = 0; x < PreviousCells.length; x++)	PreviousCells[x] = 0;
        /* Get a handle to the telephony manager service */
        /* A listener will be installed in the object from the onResume() method */
        MyListener = new MyPhoneStateListener();
	    Tel = (TelephonyManager) getSystemService( TELEPHONY_SERVICE);
	    Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	  
	    //long when = System.currentTimeMillis();
	    alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		String ALARM_ACTION = AlarmReceiver.ACTION_REFRESH_ALARM;
		Intent intentToFire = new Intent(ALARM_ACTION);
		alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
	
	    int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
	    long timeToRefresh = SystemClock.elapsedRealtime() + updateFreq*1000;
	    //alarms.set(alarmType, timeToRefresh, alarmIntent);
	    alarms.setRepeating(alarmType, timeToRefresh,updateFreq*1000, alarmIntent);
				
        MakeThread();
  	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Toast.makeText(this, "Сервис onStartCommand", Toast.LENGTH_SHORT).show();
	    //handleCommand(intent);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}
	
	
	public void MakeThread(){
		//Toast.makeText(CellIdService.this, "MakeThread begin", Toast.LENGTH_SHORT).show();
	    thr = new Thread(null, backgroundRefresh, "ServiceCellIdHandler");
		thr.start();
	}
	


    
	@Override
	public void onDestroy() {
		//Toast.makeText( this, "Сервис остановлен", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onDestroy");
		Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
		ThreadMustStop = true;
		thr.stop();
		alarms.cancel(alarmIntent);
	}
	
	private Runnable backgroundRefresh = new Runnable() {
		  public void run() {
			 // Toast.makeText(CellIdService.this, "Runnable-Run begin", Toast.LENGTH_SHORT).show();
			  while ( ThreadMustStop != true ) {
			  
			 //Текущая позиция обновляется в обработчике изменения позиции.	  
			  
			  CheckChangePosition();
			  
			  SystemClock.sleep(3 * 1000);
		     }   
		  }		  
			  
		};
		
		
public void CheckChangePosition(){
  if ( (NewCellId != LastCellId) || (NewLacId != LastLacId)  ) {
	LastCellId = NewCellId; 
	LastLacId  = NewLacId; 
	outputText  = DateFormat.getDateInstance().format(new Date()) + " ";
	outputText += DateFormat.getTimeInstance().format(new Date()) + ", ";

	//Формать записи в файл DTM,mcc+mnc,lac,cellid, 1/0 (1 инфо по вышке достоверна),расстояние
	outputText += Tel.getNetworkOperator()+','+String.valueOf(NewLacId)+','+String.valueOf(NewCellId);
	outputText += "\r\n";
	                  
	saveDataToFile(outputText);
  }
}		
	
	@Override
	public void onStart(Intent intent, int startid) {
		//Toast.makeText( this, "Service onStart", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onStart");
	}
	




private class MyPhoneStateListener extends PhoneStateListener {
	 public void onSignalStrengthsChanged(SignalStrength signalStrength) {
	   GetAndWriteCellId();
   }
 
  };

	private void saveDataToFile(String LocalFileWriteBufferStr) {
        /* write measurement data to the output file */
   	    try {
		    File root = Environment.getExternalStorageDirectory();
            if (root.canWrite()){
                File logfile = new File(root, filename);
                FileWriter logwriter = new FileWriter(logfile, true); /* true = append */
                BufferedWriter out = new BufferedWriter(logwriter);
                

                /* now save the data buffer into the file */
                out.write(LocalFileWriteBufferStr);
                out.close();
            }
        }    
        catch (IOException e) {
        /* don't do anything for the moment */
        }
        
    }
    
	 public void GetAndWriteCellId() {
	  	 Log.d(TAG,"GetAndWriteCellId");
 	  	 try {
	  		 outputText = "";
	  		 //Формат запроса OpenCellId.org  mnc=%d&mcc=%d&lac=%d&cellid=%d
	           GsmCellLocation myLocation = (GsmCellLocation) Tel.getCellLocation();
	           NewCellId = myLocation.getCid();  
	           NewLacId = myLocation.getLac();
	  	 }
	  	 catch (Exception e) {
	  		 outputText = "No network information available..."; 
	  	 }


	    }
	 
}

