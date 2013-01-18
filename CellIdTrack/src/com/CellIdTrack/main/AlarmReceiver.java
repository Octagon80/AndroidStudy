package com.CellIdTrack.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver 
{
//test
	public static final String ACTION_REFRESH_ALARM = "com.CellIdTrack.main.ACTION_REFRESH_ALARM";
    @Override
    public void onReceive(Context context,Intent intent) 
    {
        Intent myService = new Intent(context,CellIdService.class);
        context.startService(myService);
        
    }
}

