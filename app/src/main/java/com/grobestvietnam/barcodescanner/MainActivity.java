package com.grobestvietnam.barcodescanner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.grobestvietnam.barcodescanner.R.raw;
import com.grobestvietnam.mediaplayer.Player;
import com.grobestvietnam.mediaplayer.SoundList;
import com.grobestvietnam.wcfservice.MyJsonReader;
import com.grobestvietnam.utils.Networks;
import com.grobestvietnam.utils.Utils;
import com.grobestvietnam.database.DBAdapter;

import android.R.anim;
import android.R.drawable;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;


public class MainActivity extends Activity implements OnClickListener {
	
	
	private CameraPreview mPreview;	
	private CameraManager mCameraManager;	
	ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;	    
    
    // WCF Service
    private ProgressDialog progressDialogFarmHD, progressDialogFarmScan;
	private String barcodeResult;
	private String date;
	private String time;
	
	
	private TableLayout tbProducts;
	ArrayList<Products> arrProducts = new ArrayList<Products>();	
	ArrayList<String>   arrBarcode = new ArrayList<String>();
        
    // Mediaplayer
    private Player player;
    private MediaPlayer mediaplayer;       
    private Networks network;
    private Utils util;
	
    private Button btSave, btSetting;
    private TextView txtDocNo, txtDate, txtFarmID, txtFarmName, txtFarmAdd, txtStatusScan;
    private String _fInterID = "";
    private String _strDocNo="", _strDate="", _strFarmID="", 
    			   _strFarmName="", _strFarmAdd="", _strFIDArea="", _strFUserName="", _strFirstLoad="";
    private DBAdapter 	mDB;
	private Cursor 		mCursor;	
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if( !isCameraAvailable()) {
        	return;
        }   
       
        setContentView(R.layout.activity_main); 
        getWidgets();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        
        /* Mediaplyer */
        player = new Player(this);
        mediaplayer = player.getPlayerInstance(SoundList.SOUND_BEEP);        

        // Create and configure the ImageScanner;
        setupScanner();

        mCameraManager = new CameraManager(this);       
        mPreview = new CameraPreview(this, mCameraManager.getCamera(), previewCb);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview); 
        
       if(savedInstanceState == null || !savedInstanceState.containsKey("Data") ) {			
			initTable();			
		} else {
			arrProducts = savedInstanceState.getParcelableArrayList("Data");
			NotifyDataChanged();			
		}           	
		arrBarcode.clear();		
		_fInterID 	  = generateDocNo(false); // generate fInterID
		_strDocNo     = getUnlineText(generateDocNo(true)).toString();	 // lấy số DocNo	
		_strDate      = getUnlineText(getDateSystem()).toString();
		txtDocNo.setText( _strDocNo );
		txtDate.setText( _strDate );
		addTblFarmHD();
		network = new Networks(this);
		util = new Utils(this);
		getActionBar().hide();			
			
		// Load data on local
        getDataLocal();
        // Load lan dau
       /* if(!_strFirstLoad.equals("N")) {
        	mPreview.onPause();            
            gotoSettingScreen();
        }*/
    }  
    
    private void gotoSettingScreen() {
    	 Intent intent = new Intent(this, SettingActivity.class);
         intent.putExtra("strFarmID", _strFarmID);
		 intent.putExtra("strFarmName", _strFarmName);
		 intent.putExtra("strFarmAdd", _strFarmAdd);
		 intent.putExtra("strFUserName", _strFUserName);
		 intent.putExtra("strFIDArea", _strFIDArea);		
         startActivityForResult(intent, Resource.SETTING_REQUESTCODE);
    }
    
    private void getDataLocal() {
		mDB = new DBAdapter(this);
	    mDB.open();
	    loadData();
	    mDB.close();
	}
    
	/**
	 * Function loadData on Database local
	 */
	private void loadData() {
		mCursor = mDB.getAllUsers();			
		if (mCursor.moveToFirst()){
			while(!mCursor.isAfterLast()){		    	
				_strFarmID 	  = mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_FID));
				_strFarmName  = mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_FNAME));
				_strFarmAdd   = mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_FADDRESS));
				_strFUserName = mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_FUSER));
				_strFIDArea   = mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_FAREA)); 
				_strFirstLoad = mCursor.getString(mCursor.getColumnIndex(DBAdapter.KEY_FIRSTLOAD)); 
		    	mCursor.moveToNext();
			   }
			}
		mCursor.close(); 
	}	
	
	/**
	 * Function Check Remember me
	 */
	private void saveAccount() {	
		mDB.open();
		mDB.deleteAll();
		if(!_strFarmID.isEmpty())
			_strFirstLoad = "N";
		mDB.createUser(_strFarmID, _strFarmName, _strFarmAdd, _strFUserName, _strFIDArea, _strFirstLoad );
		mDB.close();
	}
	
	 public void setupScanner() {
		 scanner = new ImageScanner();
		 scanner.setConfig(0, Config.X_DENSITY, 3);
		 scanner.setConfig(0, Config.Y_DENSITY, 3);

        int[] symbols = { 
        		Symbol.CODABAR, Symbol.CODE128, Symbol.CODE39, 
        		Symbol.CODE93, Symbol.DATABAR, Symbol.DATABAR_EXP, 
        		Symbol.EAN13, Symbol.EAN8, Symbol.I25, Symbol.ISBN10,
        		Symbol.ISBN13, Symbol.PARTIAL, Symbol.PDF417,Symbol.QRCODE,
        		Symbol.UPCA,Symbol.UPCE
        		};        
        if (symbols != null) {
        	scanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
            for (int symbol : symbols) {
            	scanner.setConfig(symbol, Config.ENABLE, 1);
            }
        }
	}
	 
	@Override
	protected void onResume() {		
		super.onResume();	
		if((_strFarmID.isEmpty() || _strFUserName.isEmpty()) && 
				!_strFirstLoad.equals("N")) {// Lan dau tien load				
			gotoSettingScreen();			
		} 
		addTblFarmHD();
		mCameraManager.onResume();		
		mPreview.setCamera(mCameraManager.getCamera());
		mPreview.onResume();		
	}
	
	@Override
	protected void onPause() {
        super.onPause();
        mPreview.onPause(); 
        mCameraManager.onPause();    
     }   

	@Override
	protected void onSaveInstanceState(Bundle outState) {		
		outState.putParcelableArrayList("Data", arrProducts);	
		super.onSaveInstanceState(outState);
	}
	
    PreviewCallback previewCb = new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);

                int result = scanner.scanImage(barcode);
                
                if (result != 0) {
                    mPreview.onPause();
                    
                    SymbolSet syms = scanner.getResults();                   
                    for (Symbol sym : syms) {
                    	barcodeResult = sym.getData();
                    	txtStatusScan.setText(" Barcode result: " + barcodeResult);
                        findShortID(barcodeResult);                       
                    }
                } else {
                	txtStatusScan.setText(" Status: scanning...");
                }
                	
            }			
        }; 
        
    private void findShortID ( String str_Result) {
    	
    	String[] arrResult = str_Result.split("\\.");
    	if(arrResult.length <= 1) { 
    		 mPreview.onResume();
    		return;
    	}
    	mediaplayer.start();	
		
    	//FGID
		String strLotNo = arrResult[0] + "." + arrResult[1] + "." +
						  arrResult[2] +  "." + arrResult[3] + "." + arrResult[4] + "." + arrResult[5];
		String strShortID   = arrResult[1];
		
		arrBarcode.add(strShortID);
		arrBarcode.add(strLotNo);
		
		if(network.haveNetworkConnection()) {
			progressDialogFarmHD = util.progessDialog(null,"Waiting...");					 
			 String URI = Resource.URI + "/GetMaterialByID/?shortID=" + strShortID;
			 new getMaterialonService().execute(URI);							 
		} else {			
			 util.showDialogOneButton(MainActivity.this, "Warning", getResources().getString(R.string.check_network), "OK",Resource.R_WARNING);			
		} 		
    }
    
    private void getWidgets() {
    	tbProducts = (TableLayout)findViewById(R.id.tbProducts);
    	btSave = (Button)findViewById(R.id.btSave);
		btSave.setOnClickListener(this);
		
		btSetting = (Button)findViewById(R.id.btSetting);
		btSetting.setOnClickListener(this);
		btSetting.setVisibility(0x00000004);
		
		txtDocNo 	= (TextView)findViewById(R.id.txtDocNo);
		txtDate		= (TextView)findViewById(R.id.txtDate);
		txtFarmID 	= (TextView)findViewById(R.id.txtFarmID);
		txtFarmName = (TextView)findViewById(R.id.txtFarmName);
		txtStatusScan = (TextView)findViewById(R.id.txtStatusScan);
		
		txtFarmID.setOnClickListener(this);
		txtFarmName.setOnClickListener(this);
    }
        
    public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }      
   
    
    @Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.btSave: {
				if(arrProducts.size() > 0) {
					 if(network.haveNetworkConnection()) {						 				 
						 String URI = Resource.URI + "/InsertFarmHeader";
						 new saveFarmHeader().execute(URI);							
					 } else {			
						 util.showDialogOneButton(MainActivity.this, "Warning", getResources().getString(R.string.check_network), "OK",Resource.R_WARNING);			
					 } 
				} else {					
				}
			}
			break;
			case R.id.btSetting:
			case R.id.txtFarmID:
			case R.id.txtFarmName:
			{
				mPreview.onPause(); 	           
	            gotoSettingScreen();
			}
			break;
			default: 
			break;
		}
		
	}
    
    private class saveFarmHeader extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected void onPreExecute() {			
			super.onPreExecute();
			progressDialogFarmHD = util.progessDialog(null,"Saving farmHeader...");	
			progressDialogFarmHD.show();
		}

		@Override
		protected JSONObject doInBackground(String... params) {			
					
			String URI = params[0];			
			HttpPost httpPost = new HttpPost(URI);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			JSONObject objResult = new JSONObject();			
			try {		
				JSONObject objValues = new JSONObject();
				objValues.put("fInterID"	, _fInterID);
				objValues.put("fDocNo"		, _strDocNo);
				objValues.put("fDate"		, _strDate);
				objValues.put("fFarmID"		, _strFarmID);
				objValues.put("fFarmName"	, _strFarmName);
				objValues.put("fFarmAdd"	, _strFarmAdd);
				objValues.put("fUserName"	, _strFUserName);
				objValues.put("fAreaID"		, _strFIDArea);
				
				StringEntity entity = new StringEntity(objValues.toString(),"UTF-8");
				httpPost.setEntity(entity);

				Log.d("BarcodeScanner:", objValues.toString());				

				HttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(httpPost);

				Log.d("WebInvoke", "Saving:"+ response.getStatusLine().getStatusCode());
				
				//get value form service
				HttpEntity httpentity = response.getEntity();
			    String responseText = EntityUtils.toString(httpentity).trim();
			    
			    Log.d("","responseText: " + responseText);
			    objResult.put("responseText",responseText);
			} catch (Exception ex) {				
				ex.printStackTrace();
			}				
			return objResult;
		}

		@Override
		protected void onProgressUpdate(Void... values) {			
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(JSONObject result) {			
			super.onPostExecute(result);
			progressDialogFarmHD.dismiss();
			String responseText= "";			
			try {					
				if(result.has("responseText")) 
					responseText = result.getString("responseText");				
					if( responseText.replace('"',' ').trim().equals("1")) {
						// Insert FarmerScanner
						 String URI = Resource.URI + "/InsertFarmScanner";
						 new saveFarmScanner().execute(URI);												
					}
					else {						
						util.showDialogOneButton(MainActivity.this, "Cannot save farmHeader", "Error: " + responseText, "OK",Resource.R_WARNING);
					}				
				} catch (Exception ex) {				
					ex.printStackTrace();
				}			
		}
	}
    
    private class saveFarmScanner extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected void onPreExecute() {			
			super.onPreExecute();
			progressDialogFarmScan = util.progessDialog(null,"Saving farmScan...");	
			progressDialogFarmScan.show();
		}

		@Override
		protected JSONObject doInBackground(String... params) {			
					
			String URI = params[0];
			HttpPost httpPost = new HttpPost(URI);
			JSONObject objResult = new JSONObject();
			try {					
				JSONArray  jsonArr = new JSONArray();
				JSONObject jsonObj;
				for(int i = 0; i < arrProducts.size(); i++) {				
					jsonObj = new  JSONObject();					
					jsonObj.put("fEntryID"				, i + 1);
					jsonObj.put("fLotNo"				, arrProducts.get(i)._fLotNo);
					jsonObj.put("fShortID"				, arrProducts.get(i)._fShortID);
					jsonObj.put("fName"					, arrProducts.get(i)._fName);
					jsonObj.put("fConvertionRate"		, arrProducts.get(i)._fConvertionRate);
					jsonObj.put("fSecQty"				, arrProducts.get(i)._fSecQty);
					jsonObj.put("fQty"					, arrProducts.get(i)._fQty);
					jsonObj.put("fConvertionRateUnit"	, arrProducts.get(i)._fConvertionRateUnit);
					jsonObj.put("fSecQtyUnit"			, arrProducts.get(i)._fSecQtyUnit);
					jsonObj.put("fQtyUnit"				, arrProducts.get(i)._fQtyUnit);
					jsonObj.put("fInterID"				, _fInterID);
					jsonObj.put("fUnitID"				, arrProducts.get(i)._fUnitID);
					jsonObj.put("fItemID"				, arrProducts.get(i)._fItemID);					
					jsonArr.put(jsonObj);							
				}
				
				StringEntity entity = new StringEntity(jsonArr.toString(),"UTF-8");
				httpPost.setEntity(entity);

				Log.d("BarcodeScanner:", jsonArr.toString());				

				HttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(httpPost);

				Log.d("WebInvoke", "Saving:"+ response.getStatusLine().getStatusCode());
				
				//get value form service
				HttpEntity httpentity = response.getEntity();
			    String responseText = EntityUtils.toString(httpentity).trim();
			    
			    Log.d("","responseText: " + responseText);
			    objResult.put("responseText",responseText);
			} catch (Exception ex) {				
				ex.printStackTrace();
			}				
			return objResult;
		}

		@Override
		protected void onProgressUpdate(Void... values) {			
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(JSONObject result) {			
			super.onPostExecute(result);
			progressDialogFarmScan.dismiss();
			String responseText= "";			
			try {					
				if(result.has("responseText")) 
					responseText = result.getString("responseText");				
					if( responseText.replace('"',' ').trim().equals("1")) {							
						showDialogSave(MainActivity.this, "Information", "Save successful.", "OK",Resource.R_COMP);	
						_fInterID = generateDocNo(false); // generate fInterID
						_strDocNo = getUnlineText(generateDocNo(true)).toString();
						txtDocNo.setText( _strDocNo );						
						arrProducts.clear();
						NotifyDataChanged();
					}
					else {						
						util.showDialogOneButton(MainActivity.this, "Cannot save farmScanner", "Error: " + responseText, "OK",Resource.R_WARNING);
					}				
				} catch (Exception ex) {				
					ex.printStackTrace();
				}			
		}
	}
    
    
    public AlertDialog showDialogSave(final Context context, CharSequence title,
			CharSequence message, CharSequence strButton, Integer statusMsg) {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);	
		dialog.setTitle(title);
		dialog.setMessage(message);
		if( statusMsg == Resource.R_WARNING)
			dialog.setIcon(R.drawable.warning);
		if( statusMsg == Resource.R_COMP)
			dialog.setIcon(R.drawable.comp);
		dialog.setPositiveButton(strButton, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try
				{		
					mPreview.onResume();
				}catch(ActivityNotFoundException ex) {					
				}					
			}
		});		
		return dialog.show();
	}
    
	
	/**
	 * Class get Dono list
	 * @author Tran Tien De
	 *
	 */
	private class getMaterialonService extends AsyncTask<String, JSONObject, Boolean> {	
			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialogFarmHD.show();
			}
			@Override
			protected Boolean doInBackground(String... params) {
				boolean connectStatus = false;
				String url = params[0];		
				Log.d(Resource.TAG,"URL: " + url);
				try {					
					JSONObject jsonObjs = MyJsonReader.readJsonFromUrl(url);					
					if( jsonObjs.has("GetMaterialsResult")) {
						JSONArray jsonArr = jsonObjs.getJSONArray("GetMaterialsResult"); 
						if(jsonArr.length() > 0) { 
							connectStatus = true;
							publishProgress(jsonObjs);
						}						
					}
				} catch (IOException e) {					
					e.printStackTrace();
				} catch (JSONException e) {					
					e.printStackTrace();
				}				
				return connectStatus;
			}
			@Override
			protected void onProgressUpdate(JSONObject... values) {
				super.onProgressUpdate(values);
				JSONObject jsonObjs = values[0];
				String fConvertionRate ="",	fItemID="",	fName="", 
				fQty="", fQtyUnit="", fSecQty="", fSecQtyUnit="", fUnitID="";
				
				try {
					if( jsonObjs.has("GetMaterialsResult")) {
						Log.d(Resource.TAG," jsonObj: " + jsonObjs);
						JSONArray jsonArr = jsonObjs.getJSONArray("GetMaterialsResult"); 
						for(int i = 0; i < jsonArr.length(); i++){
							JSONObject jsonObj = jsonArr.getJSONObject(i);							
							
							if (jsonObj.has("fName")) {
								fName = jsonObj.getString("fName");
							}
							if (jsonObj.has("fConvertionRate")) {
								fConvertionRate = jsonObj.getString("fConvertionRate");
							}							
							if (jsonObj.has("fSecQty")) {
								fSecQty = jsonObj.getString("fSecQty");
							}
							if (jsonObj.has("fSecQtyUnit")) {
								fSecQtyUnit = jsonObj.getString("fSecQtyUnit");		
							}
							if (jsonObj.has("fQty")) {
								fQty = jsonObj.getString("fQty");
							}
							if (jsonObj.has("fQtyUnit")) {
								fQtyUnit = jsonObj.getString("fQtyUnit");
							}
							if (jsonObj.has("fItemID")) {
								fItemID = jsonObj.getString("fItemID");
							}
							if (jsonObj.has("fUnitID")) {
								fUnitID = jsonObj.getString("fUnitID");
							}														
							String strShortID = arrBarcode.get(0);
							String strLotNo   = arrBarcode.get(1);
							
							String _fConvertionRateUnit = fQtyUnit + "/" + fSecQtyUnit;								
							Products pro = new Products(strLotNo, strShortID, fName, fConvertionRate, 
									"1", fConvertionRate, _fConvertionRateUnit, fSecQtyUnit, fQtyUnit, fUnitID, fItemID);							
													
							arrProducts.add(pro);
							NotifyDataChanged();
						}
					}
				} catch (JSONException e) {					
					e.printStackTrace();
				}
			}
			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				progressDialogFarmHD.dismiss();
				mPreview.onResume();// reset camera
				arrBarcode.clear();
				if(result) {						
				} else {
					Toast.makeText(MainActivity.this, "Data not found.", Toast.LENGTH_SHORT).show();
					
				}				
			}	
	}
	
	private void addTblFarmHD() {			
		txtFarmID.setText( getUnlineText(_strFarmID) );
		txtFarmName.setText( getUnlineText(_strFarmName) );		
	}
	
	private SpannableString getUnlineText(String str) {		
		SpannableString content = new SpannableString(str);
		content.setSpan(new UnderlineSpan(), 0, str.length(), 0);
		return content;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {       
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {      
        int id = item.getItemId();
        if (id == R.id.action_submit) {        	
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * showDialog barcode found
     * @param act
     * @param title
     * @param message
     * @param str_barcode
     * @param btPositive
     * @param btNegative
     * @return
     */
    private AlertDialog showDialog( final Activity act, CharSequence title,
			CharSequence message, final String str_Result, CharSequence btPositive, CharSequence btNegative) {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(act);
		dialog.setIcon(R.drawable.ic_launcher);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setPositiveButton(btPositive, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				try
				{		
					String[] arrResult = str_Result.split("\\.");					
					String strShortID = arrResult[0];
					String strLotNo   = arrResult[1];
					
					arrBarcode.add(strShortID);
					arrBarcode.add(strLotNo);
					
					if(network.haveNetworkConnection()) {
						progressDialogFarmHD = util.progessDialog(null,"Waiting...");					 
						 String URI = Resource.URI + "/GetMaterialByID/?shortID=" + strShortID;
						 new getMaterialonService().execute(URI);							 
					} else {			
						 util.showDialogOneButton(MainActivity.this, "Warning", getResources().getString(R.string.check_network), "OK",Resource.R_WARNING);			
					} 					
					mPreview.onResume();
				}catch(Exception ex) {	
					Toast.makeText(act, ex.toString(), Toast.LENGTH_LONG).show();
				}					
			}
		});
		
		dialog.setNegativeButton(btNegative, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				mPreview.onResume();				
			}
		});		
		
		return dialog.show();
	}
    
    private String getDateSystem() {
    	Calendar cal = Calendar.getInstance();
		SimpleDateFormat dft = null;
		dft = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
		String datecurr = dft.format(cal.getTime());
		return datecurr;
    }
    
    private String generateDocNo( boolean blDocNo) {    	
    	Date dateCurr = new Date();    	
    	Calendar cal = Calendar.getInstance();
		SimpleDateFormat dft = null;
		dft = new SimpleDateFormat("dd/MM/yy",Locale.getDefault());
		String datecurr = dft.format(cal.getTime());
		String[] arrDate = datecurr.split("\\/");
		String strDate  = arrDate[0];
		String strMonth = arrDate[1];
		String stryear  = arrDate[2];
		
		String strDocNo;
		if(blDocNo)
			strDocNo = "SP" + dateCurr.getHours() + dateCurr.getMinutes() + dateCurr.getSeconds();
		else 
			strDocNo = stryear + strMonth + strDate + dateCurr.getHours() + dateCurr.getMinutes() + dateCurr.getSeconds();
		
		return strDocNo;
    }
    /***
     * User touch on row
     * @param act
     * @param title
     * @param message
     * @param str_barcode
     * @param btPositive
     * @param btNegative
     * @return
     */
    private AlertDialog showDialogTable( final Activity act, CharSequence title,
			CharSequence message, final int position, CharSequence btPositive, CharSequence btNegative) {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(act);
		dialog.setIcon(R.drawable.ic_launcher);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setPositiveButton(btPositive, new DialogInterface.OnClickListener() {
			
			// Edit
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				try
				{						
					String _fLotNo   	= 	arrProducts.get(position)._fLotNo;
					String _fShortID 	= 	arrProducts.get(position)._fShortID;
					String _fName    	= 	arrProducts.get(position)._fName;
					String _fConvertionRate  = arrProducts.get(position)._fConvertionRate;
					String _fSecQty  	= 	arrProducts.get(position)._fSecQty;
					String _fQty 	 	= 	arrProducts.get(position)._fQty;					
					String _fConvertionRateUnit = arrProducts.get(position)._fConvertionRateUnit;
					String _fSecQtyUnit = 	arrProducts.get(position)._fSecQtyUnit;
					String _fQtyUnit 	= 	arrProducts.get(position)._fQtyUnit;					
					String _fUnitID 	= 	arrProducts.get(position)._fUnitID;
					String _fItemID 	= 	arrProducts.get(position)._fItemID;					
										
					Intent intent = new Intent(MainActivity.this, EditMaterialActivity.class);
					intent.putExtra("fLotNo"    , _fLotNo);
					intent.putExtra("fShortID"  , _fShortID);
					intent.putExtra("fName"	    , _fName);
					intent.putExtra("fConvertionRate"		, _fConvertionRate);
					intent.putExtra("fSecQty"	, _fSecQty);
					intent.putExtra("fQty"		, _fQty);
					intent.putExtra("fConvertionRateUnit"	, _fConvertionRateUnit);					
					intent.putExtra("fSecQtyUnit"	    	, _fSecQtyUnit);
					intent.putExtra("fQtyUnit"	, _fQtyUnit);					
					intent.putExtra("fUnitID"	, _fUnitID);
					intent.putExtra("fItemID"	, _fItemID);					
					intent.putExtra("fPosition"	, position);					
					startActivityForResult(intent, Resource.EDIT_REQUESTCODE);					
				}catch(Exception ex) {	
					Toast.makeText(act, ex.toString(), Toast.LENGTH_LONG).show();
				}					
			}
		});
		
		dialog.setNegativeButton(btNegative, new DialogInterface.OnClickListener() {
			
			//Delete
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(arrProducts.size() > 0)
				{
					arrProducts.remove(position);					
					NotifyDataChanged();
					if(arrProducts.size() <= 0 ) {
						btSave.setVisibility(0x00000004); // Ẩn button save
						btSetting.setVisibility(0x00000004); // Ẩn button setting
					}
					//Toast.makeText(act, "Xoa Vitri: " + position + " Size: " + arrProducts.size(), Toast.LENGTH_SHORT).show();
				}							
				mPreview.onResume();
			}
		});		
		
		return dialog.show();
	}
    
	 
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);		
		if (resultCode == Resource.EDIT_REQUESTCODE) {
				//Toast.makeText(MainActivity.this, "Screen Edit", Toast.LENGTH_SHORT).show();
				String _fLotNo, _fShortID, _fName,_fConvertionRate, _fSecQty, _fQty,
				_fConvertionRateUnit, _fSecQtyUnit,	_fQtyUnit, _fUnitID, _fItemID;
				
				int _fPosition;
				_fLotNo 	= data.getStringExtra("fLotNo");
				_fShortID 	= data.getStringExtra("fShortID");
				_fName 		= data.getStringExtra("fName");
				_fConvertionRate = data.getStringExtra("fConvertionRate");
				_fSecQty 	= data.getStringExtra("fSecQty");
				_fQty 		= data.getStringExtra("fQty");
				_fConvertionRateUnit = data.getStringExtra("fConvertionRateUnit");
				_fSecQtyUnit = data.getStringExtra("fSecQtyUnit");
				_fQtyUnit 	= data.getStringExtra("fQtyUnit");				
				_fUnitID 	= data.getStringExtra("fUnitID");
				_fItemID 	= data.getStringExtra("fItemID");				
				_fPosition  = data.getIntExtra("fPosition", 0);
				
				Products pro = new Products(_fLotNo, _fShortID, _fName, 
						_fConvertionRate, _fSecQty, _fQty, _fConvertionRateUnit, _fSecQtyUnit, _fQtyUnit, _fUnitID, _fItemID);
				
				//Toast.makeText(MainActivity.this, "Vitri: " + _fPosition + " Size: " + arrProducts.size(), Toast.LENGTH_SHORT).show();
				if(arrProducts.size() > 0)
				{
					arrProducts.set(_fPosition, pro);				
					NotifyDataChanged();
				} else {
					Toast.makeText(MainActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
				}
				mPreview.onResume();
		}
		//Setting
		if(resultCode == Resource.SETTING_REQUESTCODE) {
			//Toast.makeText(MainActivity.this, "Screen setting", Toast.LENGTH_SHORT).show();	
			_strFarmID    = data.getStringExtra("strFarmID");
			_strFarmName  = data.getStringExtra("strFarmName");
			_strFarmAdd   = data.getStringExtra("strFarmAdd");
			_strFUserName = data.getStringExtra("strFUserName");
			_strFIDArea   = data.getStringExtra("strFIDArea");
			
			//Toast.makeText(MainActivity.this, "ID: " + _strFarmID + " Name: " + _strFarmName + 
			//		" Add: " + _strFarmAdd + " User: " + _strFUserName + " Area: " + _strFIDArea , Toast.LENGTH_SHORT).show();
			if(!_strFarmID.isEmpty() && !_strFUserName.isEmpty()) {
				addTblFarmHD();
				saveAccount();	
			}
		}				
	}
    
	 /**
	  * Table list products
	  */
	private void initTable() {
		String[] strHeader = {"SN", "FGID&Name", "Con.Rate", "SecQty", "Qty"};
		
		TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.setMargins(0, 0, 2, 2);	
		
		int colorBg = getResources().getColor(R.color.background_japfa);
		int paddingleft = 15;
		int paddingright = 15;
		float title_size = Resource.R_TEXT_SIZE_TITLE;
		TableRow trHead = new TableRow(this);			
		
		for( int i=0; i< strHeader.length; i++) {
			String strItem = strHeader[i];
			if( i == 1) {
				strItem = "FGID&Name";
			}
			if( i == 2) {
				strItem = "Con.Rate";
			}
			TextView lbHeader = new TextView(this);
			lbHeader.setText(strItem);
			lbHeader.setBackgroundColor(colorBg);
			lbHeader.setTextColor(Color.WHITE);
			lbHeader.setPadding(paddingleft, 0, paddingright, 0);	
			lbHeader.setTextSize(title_size);
			trHead.addView(lbHeader,params);	
		}
		
		// add trHead to tableLayout				
		tbProducts.setPadding(2, 4, 0, 0);
		tbProducts.setBackgroundColor(Color.GRAY);
		tbProducts.addView(trHead, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));	
	}	
	
	private void setTbRowsText(int index, Products pro) {
		
		int paddingleft = 15;
		int paddingright = 15;
		int paddingtop = 10;
		int paddingbottom = 10;
		float title_size = Resource.R_TEXT_SIZE;		
		int colorBg;
		final TableRow tr = new TableRow(this);
		tr.setId(index);			
		tr.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Log.d("","--index: " + v.getId());					
				int position = v.getId();
				
				/*Intent intent = new Intent();
				intent.setData(Uri.parse(arrDoNo.get(position)));
				setResult(RESULT_OK, intent);		
				finish();	*/
				showDialogTable(MainActivity.this,"Row ID: " + position,"What do you want ?", position-1, "Edit", "Delete");			
			}
		});
		if(index%2!=0){
			tr.setBackgroundColor(Color.GRAY);
			colorBg = getResources().getColor(R.color.background_japfa2);
		} else {
			colorBg = getResources().getColor(R.color.background_japfa3);
		}			
		
		TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.setMargins(1, 0, 2, 2);	
		
		//Title ID
		TextView lbID = new TextView(this);
		String strNo = ""+ index;
		lbID.setText(strNo);
		lbID.setBackgroundColor(colorBg);
		lbID.setTextColor(Color.BLACK);
		lbID.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);	
		lbID.setTextSize(title_size);
		tr.addView(lbID,params);
		
		//Title fName
		TextView lbfName = new TextView(this);
		lbfName.setText(pro._fLotNo + "\n" + pro._fName);
		lbfName.setBackgroundColor(colorBg);
		lbfName.setTextColor(Color.BLACK);
		lbfName.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);
		lbfName.setTextSize(title_size);
		tr.addView(lbfName,params);			
		
		//Title ConversionRate
		TextView txtQtyKg = new TextView(this);
		txtQtyKg.setText(pro._fConvertionRate + " " + pro._fConvertionRateUnit.toLowerCase());
		txtQtyKg.setBackgroundColor(colorBg);
		txtQtyKg.setTextColor(Color.BLACK);
		txtQtyKg.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);
		txtQtyKg.setTextSize(title_size);		
		tr.addView(txtQtyKg,params);
		
		// Title SecQty
		TextView txtQty = new TextView(this);
		txtQty.setText( pro._fSecQty + " " + pro._fSecQtyUnit.toLowerCase());
		txtQty.setBackgroundColor(colorBg);
		txtQty.setTextColor(Color.BLACK);
		txtQty.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);
		txtQty.setTextSize(title_size);
		
		tr.addView(txtQty,params);
		
		//Title Qty
		TextView lbTotal = new TextView(this);
		lbTotal.setText(pro._fQty + " " + pro._fQtyUnit.toLowerCase());
		lbTotal.setBackgroundColor(colorBg);
		lbTotal.setTextColor(Color.BLACK);
		lbTotal.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);
		lbTotal.setTextSize(title_size);
		tr.addView(lbTotal,params);	
		
		// add trHead to tableLayout	
		tbProducts.addView(tr, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));			
	}
	
	private void setTbRowsTextSumCol( String strCoRate, String strSecQty, String strQty, Products pro) {
			
			int paddingleft = 15;
			int paddingright = 15;
			int paddingtop = 10;
			int paddingbottom = 10;
			float title_size = Resource.R_TEXT_SIZE;		
			int colorBg;
			final TableRow tr = new TableRow(this);			
			tr.setBackgroundColor(Color.GRAY);
			tr.setPadding(0, 2, 2, 2);
			colorBg = getResources().getColor(R.color.background_japfa2);
					
			
			TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			params.setMargins(1, 0, 2, 2);	
			
			//Title ID
			TextView lbID = new TextView(this);			
			lbID.setText("*");
			lbID.setBackgroundColor(colorBg);
			lbID.setTextColor(Color.BLACK);
			lbID.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);	
			lbID.setTextSize(title_size);
			tr.addView(lbID,params);
			
			//Title fName
			TextView lbfName = new TextView(this);
			String strTotal = "Total";
			lbfName.setText(strTotal.toUpperCase());
			lbfName.setBackgroundColor(colorBg);
			lbfName.setTextColor(Color.BLACK);
			lbfName.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);
			lbfName.setTextSize(title_size);
			tr.addView(lbfName,params);			
			
			//Title ConversionRate
			TextView txtQtyKg = new TextView(this);
			txtQtyKg.setText(strCoRate + " " + pro._fConvertionRateUnit.toLowerCase());
			txtQtyKg.setBackgroundColor(colorBg);
			txtQtyKg.setTextColor(Color.BLACK);
			txtQtyKg.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);
			txtQtyKg.setTextSize(title_size);		
			tr.addView(txtQtyKg,params);
			
			// Title SecQty
			TextView txtQty = new TextView(this);
			txtQty.setText( strSecQty + " " + pro._fSecQtyUnit.toLowerCase());
			txtQty.setBackgroundColor(colorBg);
			txtQty.setTextColor(Color.BLACK);
			txtQty.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);
			txtQty.setTextSize(title_size);
			
			tr.addView(txtQty,params);
			
			//Title Qty
			TextView lbTotal = new TextView(this);
			lbTotal.setText( strQty + " " + pro._fQtyUnit.toLowerCase());
			lbTotal.setBackgroundColor(colorBg);
			lbTotal.setTextColor(Color.BLACK);
			lbTotal.setPadding(paddingleft, paddingtop, paddingright, paddingbottom);
			lbTotal.setTextSize(title_size);
			tr.addView(lbTotal,params);	
			
			// add trHead to tableLayout	
			tbProducts.addView(tr, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));			
		}
		
	
	/**
	 * Function Clear all tablerow
	 */
	private void deleteAllDataRow() {		
		int count = tbProducts.getChildCount();			
		if(count > 0) {
			tbProducts.removeAllViewsInLayout();			
		}		
		initTable();
	}
	
	private void NotifyDataChanged() {
		deleteAllDataRow();	
		/*int index = 0;		
		for(Products pro: arrProducts){
			index++;
			setTbRowsText(index, pro);
		}*/		
		for(int i = arrProducts.size(); i > 0; i--) {
			setTbRowsText(i, arrProducts.get(i-1));
		}
		sumValueCol();
		if(arrProducts.size() > 0) {
			btSave.setVisibility(0x00000000);
			btSetting.setVisibility(0x00000000);
		} else {
			btSave.setVisibility(0x00000004);
			btSetting.setVisibility(0x00000004);
		}
	}
	
	private void sumValueCol() {
			
		if( arrProducts.size() > 0) {
			int iSumConRate = 0,iSumSecQty = 0, iSumQty = 0;
			for(Products pro: arrProducts) {
				iSumConRate += Integer.parseInt(pro._fConvertionRate);
				iSumSecQty  += Integer.parseInt(pro._fSecQty);
				iSumQty     += Integer.parseInt(pro._fQty);
			}		
			setTbRowsTextSumCol( "" + iSumConRate, "" + iSumSecQty, "" + iSumQty, arrProducts.get(0));
		}			
	}
	
}
