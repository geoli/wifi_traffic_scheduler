package com.example.wifistatedetector;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiStateActivity extends Activity {

/*ping should start after the screen is shut down. Give the WiFi some time to get properly enabled before start pinging, 15 secs should be enough*/	
private final long WAIT_BEFORE_PING = 17000;	// in millsec
private final int MY_SCREEN_OFF_TIMEOUT = 15000;  // in millsec
	
private WifiManager myWifiManager = null;
private WifiLock wifiLock = null;
private Spinner modeSpinner =null;
private int initialScreenTimeout =0;
private boolean connected;
TextView logger;
String logState="";

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_wi_fi_state);
      
      /*suspend all phone connectivity holistically, bluetooth, GPS, 3G, GSM, even phone calls and SMS. WiFi can then be enable it solely,
       *as happens later when users sets the custom WiFI mode by pressing the button of the app screen*/
      setAirplaneModeOn();
      /*get the initial screen timeout to return to that after the app exits and for running the measurements set a custom screen timeout - */
      initialScreenTimeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
      Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, MY_SCREEN_OFF_TIMEOUT); 
      
  
      logger = (TextView)findViewById(R.id.logger);
      myWifiManager =  (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
      modeSpinner = (Spinner) findViewById(R.id.spinner1);
      wifiLock = myWifiManager.createWifiLock("WifiLock created");
     
    
     
  }

  
  /** Called when the activity is destroyed. */
  @Override
  protected void onDestroy(){
	  super.onDestroy();
	  
	  /*release the wifi lock - if is still on hold*/
	  if(wifiLock!=null && wifiLock.isHeld())
		  wifiLock.release();
	  /*reset previous phone connectivity*/
	  setAirplaneModeOff();
	  /*reset previous phone screen timeout*/
	  Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, initialScreenTimeout);
  }
  
  
  
  public void setWiFiMode(View button){
	  
	  String mode = modeSpinner.getSelectedItem().toString();

	  if(mode.equalsIgnoreCase("wifi off")){
		  
		  if(wifiLock!=null && wifiLock.isHeld()){
			  wifiLock.release();
			  logState += getTime() + ": WiFi LOCK released\n"; 
			  logger.setText(logState);
		  }
			  
		  
		  if(myWifiManager.isWifiEnabled()){
			  myWifiManager.setWifiEnabled(false);
			  logState += getTime() + ": WiFi set to OFF\n"; 
			  logger.setText(logState);
			  
			  
		  }
		  else{			 
			  logState +=  getTime() + ": WiFi is already disabled\n"; 
			  logger.setText(logState);
		      
		  }
	  }
	  else if(mode.equalsIgnoreCase("wifi sleep")){
		  
		  
		  /*enable WiFi before you acquire a lock on it*/
		  if(!myWifiManager.isWifiEnabled()){
			  
			  myWifiManager.setWifiEnabled(true);				  
			  wifiLock.acquire();
			  logState += getTime() + ": WiFi set to ENABLED -> SLEEP\n"; 
			  logger.setText(logState);
			  
		  }
		  /*acquire the lock immediately*/
		  else{
			  wifiLock.acquire();
			  logState += getTime() + ": WiFi set to SLEEP\n"; 
			  logger.setText(logState);
		  }
		  
	  }
	  else if(mode.equalsIgnoreCase("wifi ping")){
		  
		  if(wifiLock!=null && wifiLock.isHeld()){
			  wifiLock.release();
			  logState += getTime() + ": WiFi LOCK released\n"; 
			  logger.setText(logState);
		  }
		  
          if(!myWifiManager.isWifiEnabled()){
			  
			  myWifiManager.setWifiEnabled(true);
			  logState += getTime() + ": WiFi set to ENABLED\n"; 
			  logger.setText(logState);
          }          

          logState += getTime() + ": PING starts in"+ WAIT_BEFORE_PING/1000 + " sec\n"; 
		  logger.setText(logState);
          ping(this).execute("google.com");   

		  
	  }
	  
	  
  }// end setWiFiMode()
  
  
  
  private AsyncTask<String, String, String> ping(final Context ctx){
	  	  
	  AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {
		 
			@Override
			protected String doInBackground(String... urls) {
				
								
				try {/*delay the execution of the ping so that device inner activities are suspended - screen, keyboard, etc*/
					  try {
						Thread.sleep(WAIT_BEFORE_PING);// so that the screen goes off - we suppose the screen will go off after 15 sec
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  if(!isConnected(ctx))		  
					  	 connected = false;					  
					  else{
						  connected = true;	  
						  new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100).startTone(ToneGenerator.TONE_PROP_BEEP);
					      runAsRoot("ping -w 60 -f "+urls[0]); 
					      /*"busybox wget http://www.kernel.org/pub/linux/kernel/v3.0/linux-3.5.1.tar.bz2 -O /sdcard/testFile"*/
					  }
					    
				   } catch (Exception e) {
				   e.printStackTrace();
				  }
				
				new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100).startTone(ToneGenerator.TONE_PROP_BEEP);
				return getTime()+": "+"PING has stopped";
			}
			
			
			
			
			
			@Override
			protected void onProgressUpdate(String... progress) {
				logState += progress[0]+"\n"; 
				logger.setText(logState);
		     }

			
			@Override
			protected void onPostExecute(String result) {
				logState += result+"\n"; 
				logger.setText(logState);	
				
				if(!connected){
					AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
					  builder.setTitle("WiFi Connectivity problems")
					         .setMessage("Possible reasons:\n\nPoor signal or absent AP " )
					         .setCancelable(false)
					         .setNeutralButton("Try later", new DialogInterface.OnClickListener() {
					             public void onClick(DialogInterface dialog, int id) {
					                  /*just exit*/
					             }
					         });
					  AlertDialog alert = builder.create();
					  alert.show();
					
				}
		     }
			
			
			
			
			public void runAsRoot(String cmd) throws IOException {
			    //perform su to attempt to get root privileges
			    Process p = Runtime.getRuntime().exec("su");
			   
			    //to write to a root-only location or file
			    DataOutputStream os = new DataOutputStream(p.getOutputStream());			    
			    //perform the commands as root			    
			    os.writeBytes(cmd + "\n");
			    
			    os.writeBytes("exit\n");  
			    os.flush();
			    
			    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {						 
				
				     publishProgress(line);
				 }// end while
			}// end runAsRoot()
			
					
		};// end AsyncTask
		
		return task;
	  
  }// end ping
  
  
  
  
  
  public void setAirplaneModeOn() {

	    
	        boolean isEnabled = Settings.System.getInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0) == 1;
	        
	        if(!isEnabled){
	        	
	        	Settings.System.putInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 1);

		        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);

		        intent.putExtra("state", !isEnabled);

		        sendBroadcast(intent);
		        
		        Toast.makeText(this, "Airplane mode set to ON ", Toast.LENGTH_LONG).show();
	        }
	        else
	        	Toast.makeText(this, "Airplane mode is already ON ", Toast.LENGTH_LONG).show();

	    
	}
  
  
  /*Checks if the WiFi is active - it can possibly be enabled but not active (poor signal, no AP nearby, etc)
   *Code provided from Henrique Rocha, StackOevrflow. */
  private static boolean isConnected(Context context) {
	    ConnectivityManager connectivityManager = (ConnectivityManager)
	        context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = null;
	    if (connectivityManager != null) {
	        networkInfo =
	            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    }
	    return networkInfo == null ? false : networkInfo.isConnected();
	}

  
  
  
  public void setAirplaneModeOff() {

	  boolean isEnabled = Settings.System.getInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0) == 1;
      
      if(isEnabled){
      	
      	Settings.System.putInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0);

	        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);

	        intent.putExtra("state", isEnabled);

	        sendBroadcast(intent);
	        
	        Toast.makeText(this, "Airplane mode set to OFF ", Toast.LENGTH_LONG).show();
      }
      else
      	Toast.makeText(this, "Airplane mode is already OFF ", Toast.LENGTH_LONG).show();


	}
  
  
  
  
  private String getTime(){
	  
	  SimpleDateFormat sdfDateTime = new SimpleDateFormat("HH:mm:ss", Locale.US);
	  String newtime =  sdfDateTime.format(new Date(System.currentTimeMillis()));
	  return newtime;
  }// end getTime
  
}
