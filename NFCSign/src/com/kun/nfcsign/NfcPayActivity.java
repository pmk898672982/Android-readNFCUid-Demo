package com.kun.nfcsign;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class NfcPayActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ����ʾ����ı�����
		setContentView(R.layout.activity_nfc_pay);
	}
}