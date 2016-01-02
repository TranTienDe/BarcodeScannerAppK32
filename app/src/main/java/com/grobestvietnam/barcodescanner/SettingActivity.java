package com.grobestvietnam.barcodescanner;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.grobestvietnam.database.DBAdapter;
import com.grobestvietnam.utils.Networks;
import com.grobestvietnam.utils.Utils;
import com.grobestvietnam.wcfservice.MyJsonReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingActivity extends Activity implements OnClickListener {

	private Button bt_Save;
	private EditText edtID, edtUserName, edtArea, edtPassWord;
	private Networks network;
	private Utils util;
	private ProgressDialog progressDialog;
	private String _strFarmID = "", _strFarmName = "", _strFarmAdd = "", _strFUserName = "", _strFIDArea = "", _strFPass;
	private JSONObject jsonFarmID = new JSONObject();
	private JSONObject jsonSaleInfo = new JSONObject();
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		getWidgets();
		network = new Networks(this);
		util = new Utils(this);
		progressDialog = util.progessDialog(null, "Waiting...");

		_strFarmID = getIntent().getStringExtra("strFarmID");
		_strFarmName = getIntent().getStringExtra("strFarmName");
		_strFarmAdd = getIntent().getStringExtra("strFarmAdd");
		_strFUserName = getIntent().getStringExtra("strFUserName");
		_strFIDArea = getIntent().getStringExtra("strFIDArea");

		edtID.setText(_strFarmID);
		edtUserName.setText(_strFUserName);
		edtArea.setText(_strFIDArea);

		//Toast.makeText(this, "ID: " + _strFarmID + " Name: " + _strFarmName + 
		//		" Add: " + _strFarmAdd + " User: " + _strFUserName + " Area: " + _strFIDArea , Toast.LENGTH_SHORT).show();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	private void getWidgets() {
		bt_Save = (Button) findViewById(R.id.btSave);
		bt_Save.setOnClickListener(this);
		edtID = (EditText) findViewById(R.id.edtID);
		edtUserName = (EditText) findViewById(R.id.edtUserName);
		edtArea = (EditText) findViewById(R.id.edtArea);
		edtPassWord = (EditText) findViewById(R.id.edtPassWord);
		edtID.addTextChangedListener(ext_FarmID);
		edtUserName.addTextChangedListener(edt_UserName);
	}

	private TextWatcher ext_FarmID = new TextWatcher() {

		public void afterTextChanged(Editable s) {
			if (!edtID.getText().toString().isEmpty())
				getFarmByID(edtID.getText().toString());
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};

	private TextWatcher edt_UserName = new TextWatcher() {

		public void afterTextChanged(Editable s) {
			if (!edtUserName.getText().toString().isEmpty())
				getScaleUserInfo(edtUserName.getText().toString());
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btSave: {
				if (edtID.getText().toString().isEmpty()) {
					edtID.setError("FarmID null or empty");
					return;
				}
				if (edtUserName.getText().toString().isEmpty()) {
					edtUserName.setError("UserName null or empty");
					return;
				}
				// Check value
				if (!NotifyGetFarmID(jsonFarmID)) {
					edtID.requestFocus();
					return;
				}
				if (!NotifySaleInfo(jsonSaleInfo)) {
					edtUserName.requestFocus();
					return;
				}
				if (edtPassWord.getText().toString().isEmpty()) {
					Toast.makeText(this, "Enter password.", Toast.LENGTH_SHORT).show();
					edtPassWord.requestFocus();
					return;
				}
				// Check password
				String strPass = getHashMD5(edtPassWord.getText().toString() + Resource.MD5PLUS);
				if (_strFPass.equals(strPass)) {
					Intent intent = new Intent();
					intent.putExtra("strFarmID", _strFarmID);
					intent.putExtra("strFarmName", _strFarmName);
					intent.putExtra("strFarmAdd", _strFarmAdd);
					intent.putExtra("strFUserName", _strFUserName);
					intent.putExtra("strFIDArea", _strFIDArea);
					setResult(Resource.SETTING_REQUESTCODE, intent);
					finish();
				} else {
					Toast.makeText(this, "Password incorrect!", Toast.LENGTH_SHORT).show();
					edtPassWord.requestFocus();
				}

			}
			break;
			default:
				break;
		}
	}

	public static String getHashMD5(String string) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			BigInteger bi = new BigInteger(1, md.digest(string.getBytes()));
			return bi.toString(16);
		} catch (NoSuchAlgorithmException ex) {
			return "";
		}
	}

	private void getFarmByID(String farmID) {
		if (network.haveNetworkConnection()) {
			String URI = Resource.URI + "/GetFarmerByID/?FarmID=" + farmID;
			new getFarmIDonService().execute(URI);
		} else {
			util.showDialogOneButton(this, "Warning", getResources().getString(R.string.check_network), "OK", Resource.R_WARNING);
		}
	}

	private void getScaleUserInfo(String fUsername) {
		if (network.haveNetworkConnection()) {
			String URI = Resource.URI + "/GetSaleUserInfo/?FUsername=" + fUsername;
			new getSaleUserInfoonService().execute(URI);
		} else {
			util.showDialogOneButton(this, "Warning", getResources().getString(R.string.check_network), "OK", Resource.R_WARNING);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Setting Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.grobestvietnam.barcodescanner/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Setting Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.grobestvietnam.barcodescanner/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}

	/**
	 * Class get FarmID
	 *
	 * @author Tran Tien De
	 */
	private class getFarmIDonService extends AsyncTask<String, JSONObject, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			boolean blConnectStatus = false;
			String strErrIOEX = "", strErrJSONEX = "";
			JSONObject jsonResult = new JSONObject();
			String url = params[0];
			Log.d(Resource.TAG, "URL: " + url);
			try {
				JSONObject jsonObjs = MyJsonReader.readJsonFromUrl(url);
				if (jsonObjs.has("GetFarmerByIDResult")) {
					JSONArray jsonArr = jsonObjs.getJSONArray("GetFarmerByIDResult");
					if (jsonArr.length() > 0) {
						blConnectStatus = true;
						publishProgress(jsonObjs);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				strErrIOEX = e.toString();
				if (Resource.DEBUG)
					Log.d(Resource.TAG, "doInBackground FarmID: " + e.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				strErrJSONEX = e.toString();
				if (Resource.DEBUG)
					Log.d(Resource.TAG, "doInBackground FarmID JSON: " + e.toString());
			}

			try {
				jsonResult.put("blConnectStatus", blConnectStatus);
				jsonResult.put("strErrIOEX", strErrIOEX);
				jsonResult.put("strErrJSONEX", strErrJSONEX);
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
			return jsonResult;
		}

		@Override
		protected void onProgressUpdate(JSONObject... values) {
			super.onProgressUpdate(values);
			JSONObject jsonObjs = values[0];
			try {
				if (jsonObjs.has("GetFarmerByIDResult")) {
					Log.d(Resource.TAG, " jsonObj: " + jsonObjs);
					JSONArray jsonArr = jsonObjs.getJSONArray("GetFarmerByIDResult");
					for (int i = 0; i < jsonArr.length(); i++) {
						JSONObject jsonObj = jsonArr.getJSONObject(i);

						if (jsonObj.has("fID")) {
							_strFarmID = jsonObj.getString("fID");
						}
						if (jsonObj.has("fName")) {
							_strFarmName = jsonObj.getString("fName");
						}
						if (jsonObj.has("fAddress")) {
							_strFarmAdd = jsonObj.getString("fAddress");
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute(JSONObject jsonResult) {
			super.onPostExecute(jsonResult);
			progressDialog.dismiss();
			jsonFarmID = jsonResult;
		}
	}

	private boolean NotifyGetFarmID(JSONObject jsonResult) {
		boolean blFID = false;
		String strErrIOEX = "", strErrJSONEX = "";
		try {
			if (jsonResult.has("blConnectStatus")) {
				blFID = jsonResult.getBoolean("blConnectStatus");
			}
			if (jsonResult.has("strErrIOEX")) {
				strErrIOEX = jsonResult.getString("strErrIOEX");
			}
			if (jsonResult.has("strErrJSONEX")) {
				strErrJSONEX = jsonResult.getString("strErrJSONEX");
			}
			if (!blFID) {
				if (!strErrIOEX.isEmpty())
					util.showDialogOneButton(SettingActivity.this, "Warning", "Cannot Connect to server. Please check network.", "OK", Resource.R_WARNING);
				else
					util.showDialogOneButton(SettingActivity.this, "Warning", "FarmID not found.", "OK", Resource.R_WARNING);
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		return blFID;
	}

	/**
	 * Class get SaleUserInfo
	 *
	 * @author Tran Tien De
	 */
	private class getSaleUserInfoonService extends AsyncTask<String, JSONObject, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			boolean blConnectStatus = false;
			String strErrIOEX = "", strErrJSONEX = "";
			JSONObject jsonResult = new JSONObject();
			String url = params[0];
			Log.d(Resource.TAG, "URL: " + url);
			try {
				JSONObject jsonObjs = MyJsonReader.readJsonFromUrl(url);
				if (jsonObjs.has("GetSaleUserInfoResult")) {
					JSONArray jsonArr = jsonObjs.getJSONArray("GetSaleUserInfoResult");
					if (jsonArr.length() > 0) {
						blConnectStatus = true;
						publishProgress(jsonObjs);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				strErrIOEX = e.toString();
			} catch (JSONException e) {
				e.printStackTrace();
				strErrJSONEX = e.toString();
			}
			try {
				jsonResult.put("blConnectStatus", blConnectStatus);
				jsonResult.put("strErrIOEX", strErrIOEX);
				jsonResult.put("strErrJSONEX", strErrJSONEX);
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
			return jsonResult;
		}

		@Override
		protected void onProgressUpdate(JSONObject... values) {
			super.onProgressUpdate(values);
			JSONObject jsonObjs = values[0];

			try {
				if (jsonObjs.has("GetSaleUserInfoResult")) {
					Log.d(Resource.TAG, " jsonObj: " + jsonObjs);
					JSONArray jsonArr = jsonObjs.getJSONArray("GetSaleUserInfoResult");
					for (int i = 0; i < jsonArr.length(); i++) {
						JSONObject jsonObj = jsonArr.getJSONObject(i);

						if (jsonObj.has("fIDArea")) {
							_strFIDArea = jsonObj.getString("fIDArea");
							edtArea.setText(_strFIDArea);
						}
						if (jsonObj.has("fUsername")) {
							_strFUserName = jsonObj.getString("fUsername");
						}
						if (jsonObj.has("fPass")) {
							_strFPass = jsonObj.getString("fPass");
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute(JSONObject jsonResult) {
			super.onPostExecute(jsonResult);
			progressDialog.dismiss();
			jsonSaleInfo = jsonResult;
		}
	}

	private boolean NotifySaleInfo(JSONObject jsonResult) {
		boolean blFUserName = false;
		String strErrIOEX = "", strErrJSONEX = "";
		try {
			if (jsonResult.has("blConnectStatus")) {
				blFUserName = jsonResult.getBoolean("blConnectStatus");
			}
			if (jsonResult.has("strErrIOEX")) {
				strErrIOEX = jsonResult.getString("strErrIOEX");
			}
			if (jsonResult.has("strErrJSONEX")) {
				strErrJSONEX = jsonResult.getString("strErrJSONEX");
			}
			if (!blFUserName) {
				if(!strErrIOEX.isEmpty())
					util.showDialogOneButton(SettingActivity.this, "Warning", "Cannot Connect to server. Please check network.", "OK", Resource.R_WARNING);
				else{
					util.showDialogOneButton(SettingActivity.this, "Warning", "SaleInfo not found.", "OK", Resource.R_WARNING);
					edtArea.setText("");
				}
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		return  blFUserName;
	}


}
