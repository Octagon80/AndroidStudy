package com.CellIdTrack.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.telephony.SmsMessage;


public class PhoneIntentReceiver extends BroadcastReceiver {
	public static final String TAG = "PhoneIntentReceiver";
	private String filename = "cdata.txt";
	

    
	/************************************************************************
	Запись в файл указанной строки с добавлением текущего времени
	************************************************************************/
	private void saveDataToFile(String LocalFileWriteBufferStr) {
	        /* write measurement data to the output file */
	   	    try {
	   	       String sDTM  = DateFormat.getDateInstance().format(new Date()) + " ";
	   	       sDTM += DateFormat.getTimeInstance().format(new Date()) + ", ";
	   	 	
			    File root = Environment.getExternalStorageDirectory();
	            if (root.canWrite()){
	                File logfile = new File(root, filename);
	                FileWriter logwriter = new FileWriter(logfile, true); /* true = append */
	                BufferedWriter out = new BufferedWriter(logwriter);
	                

	                /* now save the data buffer into the file */
	                out.write(sDTM + " " + LocalFileWriteBufferStr+"\r\n");
	                out.close();
	            }
	        }    
	        catch (IOException e) {
	        /* don't do anything for the moment */
	        }
	        
	    }
	
	@Override
	public void onReceive(Context arg0, Intent intent) {
		Log.d( TAG, "Phone intent: "+intent.toString());
  
	    if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
	        Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
	        SmsMessage[] msgs = null;
	        String msg_from;
	        if (bundle != null){
	            //---retrieve the SMS message received---
	            try{
	                Object[] pdus = (Object[]) bundle.get("pdus");
	                msgs = new SmsMessage[pdus.length];
	                for(int i=0; i<msgs.length; i++){
	                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	                    msg_from = msgs[i].getOriginatingAddress();
	                    String msgBody = msgs[i].getMessageBody();
	                    saveDataToFile("SMS IN: "+msg_from+":"+msgBody);
	                    Log.d( TAG, "SMS IN: "+msg_from+":"+msgBody);
	                }
	            }catch(Exception e){
	                Log.d("Exception caught",e.getMessage());
	            }
	        }
	    } else 
	    
	    	//if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
	    	if(intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")){
	      
	    		String phoneNumber; 
	    		phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                saveDataToFile("CALL OUT: "+phoneNumber);
                Log.d( TAG, "CALL OUT: "+phoneNumber);	    		
	        }else
	        
	     	if(intent.getAction().equals("android.intent.action.PHONE_STATE")){
	    
		Bundle b = intent.getExtras();
		if( b != null ) {
			for( Iterator<String> i = b.keySet().iterator() ; i.hasNext() ;) {
				String key = i.next();
				Log.d( TAG, "CALL IN: "+key+":"+b.get( key ));
				saveDataToFile(/*intent.toString()+*/"CALL IN: "+key+":"+b.get( key ));
			}
		}
			
	    }
	}
	
	
 
}
