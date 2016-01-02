package com.grobestvietnam.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;

import com.grobestvietnam.barcodescanner.R;
import com.grobestvietnam.barcodescanner.Resource;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class Utils {
	Context _context;
	ProgressDialog progressDialog;
	public Utils(Context context){
		_context = context;		
	}	
	
	/**
	 * Function Dialog have a button
	 * @param context
	 * @param title
	 * @param message
	 * @param strButton
	 * @return
	 */
	public AlertDialog showDialogOneButton(final Context context, CharSequence title,
			CharSequence message, CharSequence strButton, Integer statusMsg) {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);	
		dialog.setTitle(title);
		dialog.setMessage(message);
		if( statusMsg == Resource.R_WARNING)
			dialog.setIcon(R.drawable.warning);
		if( statusMsg == Resource.R_COMP)
			dialog.setIcon(R.drawable.comp);
		dialog.setNegativeButton(strButton, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try
				{														
				}catch(ActivityNotFoundException ex) {					
				}					
			}
		});		
		return dialog.show();
	}
	
	/**
	 * Function get Date, time follow input format
	 * @param dateFormat
	 * @return
	 */
	public String now(String dateFormat)
	{   
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime()); 
	}
	
	/**
	 * Function 
	 * @param tile
	 * @param message
	 * @return
	 */
	public ProgressDialog progessDialog(String tile, String message) {
		progressDialog = new ProgressDialog(_context);
		progressDialog.setIndeterminate(true);
		progressDialog.setTitle(tile);
		progressDialog.setMessage(message);
		progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
		return progressDialog;
	}
	
	/**
	 * Function ProgressBar
	 * @param timeRange
	 * @param message
	 */
	public void loadProgressbar (final long timeRange, String message) {		
		final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(_context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(true);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();        
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {                
                while(progressDialog.getProgress() <= progressDialog.getMax())
                {
                    try
                    {                       	
                    	int i = (int)(timeRange/100);                    	
                        progressDialog.incrementProgressBy(i);
                        Thread.sleep(100);
                        if(progressDialog.getProgress()== progressDialog.getMax())
                        progressDialog.cancel();                                
                    }
                    catch(Exception ex)
                    {}
                }                
            }
        }).start();
	}
	
	/**
	 * Function get IMEI
	 * @param phonyManager
	 * @return
	 */
	public String getDeviceID(TelephonyManager phonyManager){
		 String id = phonyManager.getDeviceId();
		 if (id == null){
		  id = "not available";
		 }
		 int phoneType = phonyManager.getPhoneType();
		 switch(phoneType){
			 //case TelephonyManager.PHONE_TYPE_NONE:
				 //return "NONE: " + id;
	
			 case TelephonyManager.PHONE_TYPE_GSM:
				 return id;
	
			 /*case TelephonyManager.PHONE_TYPE_CDMA:
				 return "CDMA: MEID/ESN=" + id;			
			 case TelephonyManager.PHONE_TYPE_SIP:
			     return "SIP";
			  */	
			 default:
			  return "UNKNOWN: ID=" + id;
		 }
	}
	
}
