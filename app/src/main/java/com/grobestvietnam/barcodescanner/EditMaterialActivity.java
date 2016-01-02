package com.grobestvietnam.barcodescanner;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditMaterialActivity extends Activity implements  android.view.View.OnClickListener{

	private TextView txtFName, txtQtyKg, txtQtyBao, txtTotalTitle, txtTotal;
	private EditText edtQtyKg, edtQtyBao;
	private Button btSave;
	private int _fPosition;	
	
	private String _fLotNo, _fShortID, _fName,_fConvertionRate, _fSecQty, _fQty,
	_fConvertionRateUnit, _fSecQtyUnit,	_fQtyUnit, _fUnitID, _fItemID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		getWidgets();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		 
		_fLotNo 	= getIntent().getStringExtra("fLotNo");
		_fShortID 	= getIntent().getStringExtra("fShortID");
		_fName 		= getIntent().getStringExtra("fName");
		_fConvertionRate = getIntent().getStringExtra("fConvertionRate");
		_fSecQty 	= getIntent().getStringExtra("fSecQty");
		_fQty 		= getIntent().getStringExtra("fQty");
		_fConvertionRateUnit = getIntent().getStringExtra("fConvertionRateUnit");
		_fSecQtyUnit = getIntent().getStringExtra("fSecQtyUnit");
		_fQtyUnit 	= getIntent().getStringExtra("fQtyUnit");		
		_fUnitID 	= getIntent().getStringExtra("fUnitID");
		_fItemID 	= getIntent().getStringExtra("fItemID");				
		_fPosition  = getIntent().getIntExtra("fPosition", 0);		
		
		txtFName .setText(_fName);		
		txtQtyKg .setText("Conver..");
		txtQtyBao.setText("SecQty");
		
		int QtyKg = Integer.parseInt(_fConvertionRate);
		int QtyBao = Integer.parseInt(_fSecQty);
		edtQtyKg .setText("" + QtyKg);
		edtQtyBao.setText("" + QtyBao);
		
		txtTotalTitle.setText("Qty");			
		txtTotal.setText("" + QtyKg * QtyBao );	
		//Toast.makeText(this, "Vitri: " + _fPosition , Toast.LENGTH_SHORT).show();
	} 
	
	private void getWidgets() {
		txtFName  = (TextView)findViewById(R.id.txtFName);
		txtQtyKg  = (TextView)findViewById(R.id.txtQtyKg);
		txtQtyBao = (TextView)findViewById(R.id.txtQtyBao);
		txtTotalTitle = (TextView)findViewById(R.id.txtTotalTitle);
		txtTotal = (TextView)findViewById(R.id.txtTotal);
		
		edtQtyKg  = (EditText)findViewById(R.id.edtQtyKg);
		edtQtyKg.addTextChangedListener(txtWQtyKg);
		edtQtyBao = (EditText)findViewById(R.id.edtQtyBao);
		edtQtyBao.addTextChangedListener(txtWQtyBao);
		edtQtyBao.requestFocus();
		btSave	  = (Button)findViewById(R.id.btSave);
		btSave.setOnClickListener(this);		
	}
	
	private TextWatcher txtWQtyKg = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			String strQtyKg = edtQtyKg.getText().toString().trim();
			String strQtyBao = edtQtyBao.getText().toString().trim();
			
			if( !strQtyKg.isEmpty()) {
				int qtyKg = Integer.parseInt(strQtyKg);
				if(qtyKg <= 0) {
					edtQtyKg.setError("Enter a value > 0");
				} else {
					if( !strQtyBao.isEmpty()) {
						int qtyBao = Integer.parseInt(strQtyBao);
						if(qtyBao > 0) {
							int total = qtyKg*qtyBao;
							txtTotal.setText("" + total);
						}
					}
				}				
			} else {
				edtQtyKg.setError("Enter a value.");
			}				
		}
	}; 
	
	private TextWatcher txtWQtyBao = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			String strQtyKg = edtQtyKg.getText().toString().trim();
			String strQtyBao = edtQtyBao.getText().toString().trim();
			
			if( !strQtyBao.isEmpty()) {
				int qtyBao = Integer.parseInt(strQtyBao);
				if(qtyBao <= 0) {
					edtQtyBao.setError("Enter a value > 0");
				} else {
					if( !strQtyKg.isEmpty()) {
						int qtyKg = Integer.parseInt(strQtyKg);
						if(qtyKg > 0) {
							int total = qtyKg*qtyBao;
							txtTotal.setText("" + total);
						}
					}
				}				
			} else {
				edtQtyBao.setError("Enter a value.");
			}			
		}
	};

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btSave: {
				String strQtyKg = edtQtyKg.getText().toString().trim();
				String strQtyBao = edtQtyBao.getText().toString().trim();
				if( strQtyKg.isEmpty())
					edtQtyKg.setError("Enter a value");
				if( strQtyBao.isEmpty())
					edtQtyBao.setError("Enter a value");
				if( !strQtyKg.isEmpty() && !strQtyBao.isEmpty()) {
					Intent intent = new Intent();					
					intent.putExtra("fLotNo"    			, _fLotNo)	;
					intent.putExtra("fShortID"  			, _fShortID);
					intent.putExtra("fName"	    			, _fName)	;
					intent.putExtra("fConvertionRate"		, edtQtyKg.getText().toString());
					intent.putExtra("fSecQty"				, edtQtyBao.getText().toString());
					intent.putExtra("fQty"					, txtTotal.getText().toString());
					intent.putExtra("fConvertionRateUnit"	, _fConvertionRateUnit);					
					intent.putExtra("fSecQtyUnit"	    	, _fSecQtyUnit);
					intent.putExtra("fQtyUnit"				, _fQtyUnit);					
					intent.putExtra("fUnitID"	, _fUnitID)	;
					intent.putExtra("fItemID"	, _fItemID)	;					
					intent.putExtra("fPosition"	, _fPosition);	
					setResult(Resource.EDIT_REQUESTCODE,intent);				
					finish();
				}
			}
			break;
		}		
	}

	
	

}
