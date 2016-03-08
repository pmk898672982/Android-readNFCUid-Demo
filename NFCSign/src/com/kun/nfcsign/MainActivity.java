package com.kun.nfcsign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button btn_nfc_map;
	private Button btn_nfc_card;
	private Button btn_nfc_pay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {
		btn_nfc_map = (Button) findViewById(R.id.btn_nfc_map);
		btn_nfc_card = (Button) findViewById(R.id.btn_nfc_card);
		btn_nfc_pay = (Button) findViewById(R.id.btn_nfc_pay);
		btn_nfc_map.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent mapIntent = new Intent(MainActivity.this, NfcMapActivity.class);
				startActivity(mapIntent);
			}
		});
		btn_nfc_card.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent mapIntent = new Intent(MainActivity.this, NfcCardActivity.class);
				startActivity(mapIntent);
			}
		});
		btn_nfc_pay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent mapIntent = new Intent(MainActivity.this, NfcPayActivity.class);
				startActivity(mapIntent);
			}
		});
	}

}
