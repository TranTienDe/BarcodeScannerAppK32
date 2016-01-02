package com.grobestvietnam.barcodescanner;

import java.io.Serializable;

import android.app.Notification.Extender;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;

public class Products implements Parcelable {
	
	public String get_fLotNo() {
		return _fLotNo;
	}
	public void set_fLotNo(String _fLotNo) {
		this._fLotNo = _fLotNo;
	}
	public String get_fShortID() {
		return _fShortID;
	}
	public void set_fShortID(String _fShortID) {
		this._fShortID = _fShortID;
	}
	public String get_fName() {
		return _fName;
	}
	public void set_fName(String _fName) {
		this._fName = _fName;
	}
	public String get_fConvertionRate() {
		return _fConvertionRate;
	}
	public void set_fConvertionRate(String _fConvertionRate) {
		this._fConvertionRate = _fConvertionRate;
	}
	public String get_fSecQty() {
		return _fSecQty;
	}
	public void set_fSecQty(String _fSecQty) {
		this._fSecQty = _fSecQty;
	}
	public String get_fQty() {
		return _fQty;
	}
	public void set_fQty(String _fQty) {
		this._fQty = _fQty;
	}
	public String get_fConvertionRateUnit() {
		return _fConvertionRateUnit;
	}
	public void set_fConvertionRateUnit(String _fConvertionRateUnit) {
		this._fConvertionRateUnit = _fConvertionRateUnit;
	}
	public String get_fSecQtyUnit() {
		return _fSecQtyUnit;
	}
	public void set_fSecQtyUnit(String _fSecQtyUnit) {
		this._fSecQtyUnit = _fSecQtyUnit;
	}
	public String get_fQtyUnit() {
		return _fQtyUnit;
	}
	public void set_fQtyUnit(String _fQtyUnit) {
		this._fQtyUnit = _fQtyUnit;
	}	
	public String get_fUnitID() {
		return _fUnitID;
	}
	public void set_fUnitID(String _fUnitID) {
		this._fUnitID = _fUnitID;
	}
	public String get_fItemID() {
		return _fItemID;
	}
	public void set_fItemID(String _fItemID) {
		this._fItemID = _fItemID;
	}
	public Products( 
			String fLotNo,
			String fShortID,
			String fName,
			String fConvertionRate,
			String fSecQty,
			String fQty,
			String fConvertionRateUnit, 
			String fSecQtyUnit,
			String fQtyUnit,			
			String fUnitID,
			String fItemID 
			) {		
		
		_fLotNo 			 = fLotNo;
		_fShortID 			 = fShortID;
		_fName 				 = fName;
		_fConvertionRate 	 = fConvertionRate;
		_fSecQty 		 	 = fSecQty;
		_fQty			 	 = fQty;
		_fConvertionRateUnit = fConvertionRateUnit;
		_fSecQtyUnit 		 = fSecQtyUnit;
		_fQtyUnit 			 = fQtyUnit;		
		_fUnitID 			 = fUnitID;
		_fItemID 			 = fItemID;
	}	
	
	String _fLotNo;
	String _fShortID;
	String _fName;
	String _fConvertionRate;
	String _fSecQty;
	String _fQty; 
	String _fConvertionRateUnit;
	String _fSecQtyUnit;
	String _fQtyUnit;	
	String _fUnitID; 
	String _fItemID;
	
	private Products(Parcel in) {		
		_fLotNo		= in.readString();
		_fShortID	= in.readString();
		_fName		= in.readString();
		_fConvertionRate= in.readString();
		_fSecQty	= in.readString();
		_fQty		= in.readString();
		_fConvertionRateUnit= in.readString();
		_fSecQtyUnit= in.readString();
		_fQtyUnit	= in.readString();		
		_fUnitID	= in.readString();
		_fItemID	= in.readString();
	}
	
	@Override
	public int describeContents() {		
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(_fLotNo);
		dest.writeString(_fShortID);
		dest.writeString(_fName);	
		dest.writeString(_fConvertionRate);
		dest.writeString(_fSecQty);
		dest.writeString(_fQty);
		dest.writeString(_fConvertionRateUnit);
		dest.writeString(_fSecQtyUnit);
		dest.writeString(_fQtyUnit);		
		dest.writeString(_fUnitID);
		dest.writeString(_fItemID);		
	}
	
	public static final Parcelable.Creator<Products> CREATOR = new Parcelable.Creator<Products>() {

		@Override
		public Products createFromParcel(Parcel source) {			
			return new Products(source);
		}

		@Override
		public Products[] newArray(int size) {			
			return new Products[size];
		}
	};
}
