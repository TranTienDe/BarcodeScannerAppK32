package com.grobestvietnam.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DBAdapter {

	public static final String TAG ="DBAdapter";
	
	public static final String KEY_ID 			= "_id";
	public static final String KEY_FID 			= "fid";
	public static final String KEY_FNAME 		= "fname";
	public static final String KEY_FADDRESS 	= "faddress";
	public static final String KEY_FUSER 		= "fusername";
	public static final String KEY_FAREA 		= "farea";	
	public static final String KEY_FIRSTLOAD 	= "firstload";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDB;
	
	private static final String TABLE_ACCOUNT = "create table tblAccount (_id integer primary key autoincrement," +
												"fid	    text not null," +	
												"fname  	text not null," +
												"faddress   text not null," +
												"fusername  text not null," +
												"farea  	text not null," +
												"firstload  text not null);";
	
	private static final String DATABASE_NAME = "Database_Account";	
	private static final String DATABASE_TABLE = "tblAccount";
	private static final int DATABASE_VERSION = 2;
	
	private final Context mContext;
	
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {			
			db.execSQL(TABLE_ACCOUNT);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {				
			Log.i(TAG, "Upgrading DB");			
			db.execSQL("DROP TABLE IF EXISTS tblAccount");
			onCreate(db);
		}
	}
	
	public DBAdapter(Context ctx){
		this.mContext = ctx;
	}
	
	public DBAdapter open()
	{
		mDbHelper = new DatabaseHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
		mDB = mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		mDbHelper.close();
	}
	
	public long createUser(String fid, String fname, String faddress, String fusername, String farea, String firstload){							
		ContentValues inititalValues = new ContentValues();
		inititalValues.put(KEY_FID, 	 fid);
		inititalValues.put(KEY_FNAME, 	 fname);
		inititalValues.put(KEY_FADDRESS, faddress);
		inititalValues.put(KEY_FUSER, 	 fusername);
		inititalValues.put(KEY_FAREA, 	 farea);	
		inititalValues.put(KEY_FIRSTLOAD,firstload);
		return mDB.insert(DATABASE_TABLE, null, inititalValues);
	}
	
	public boolean deleteUser(long rowId)
	{
		return mDB.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) >0;		
	}
	
	public boolean deleteAll()
	{	
		return mDB.delete(DATABASE_TABLE, null, null) >0;
	}
	
	public Cursor getAllUsers(){		
		return mDB.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_FID, KEY_FNAME, KEY_FADDRESS, KEY_FUSER, KEY_FAREA, KEY_FIRSTLOAD}, null, null, null, null, null);
	}
}
