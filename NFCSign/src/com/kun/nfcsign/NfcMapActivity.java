package com.kun.nfcsign;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.location.Location;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.kum.nfcsign.data.Converter;

public class NfcMapActivity extends Activity implements LocationSource,AMapLocationListener,
                OnMapLoadedListener, OnMarkerClickListener, InfoWindowAdapter{

	private TextView uid_info;
	private NfcAdapter nfcAdapter;
	private IntentFilter[] mNdefExchangeFilters;
	private PendingIntent pendingIntent;
	private String[][] mTechLists;

	private MapView mapView;
	private AMap aMap;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;
	private TextView mLocationLatlngTextView;// ������Ϣ
	private TextView mLocationTimeTextView;// ��λʱ����Ϣ
	private TextView mLocationDesTextView;// ��λ������Ϣ
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ����ʾ����ı�����
		setContentView(R.layout.activity_nfc_map);

		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		init();
		setMap();
	}

	private void setMap() {
		if (aMap == null) {
			aMap = mapView.getMap();
			aMap.getUiSettings();
			aMap.moveCamera(CameraUpdateFactory.zoomTo(10));
			setUpMap();
		}
	}

	private void setUpMap() {
		aMap.setLocationSource(this);// ���ö�λ����
		aMap.getUiSettings().setZoomControlsEnabled(false);//����Ĭ�ϵ����Ű�ť�Ƿ���ʾ
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// ����Ĭ�϶�λ��ť�Ƿ���ʾ
		aMap.setMyLocationEnabled(true);// ����Ϊtrue��ʾ��ʾ��λ�㲢�ɴ�����λ��false��ʾ���ض�λ�㲢���ɴ�����λ��Ĭ����false
		// ���ö�λ������Ϊ��λģʽ����λ��AMap.LOCATION_TYPE_LOCATE�������棨AMap.LOCATION_TYPE_MAP_FOLLOW��
		// ��ͼ������������ת��AMap.LOCATION_TYPE_MAP_ROTATE������ģʽ
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		aMap.setOnMapLoadedListener(this);// ����amap���سɹ��¼�������
		aMap.setOnMarkerClickListener(this);// ���õ��marker�¼�������
		aMap.setInfoWindowAdapter(this);// �����ʾinfowindow�����¼�
		addMarkersToMap();// ����ͼ�����marker
	}
	// ����ͼ�����marker
	private void addMarkersToMap() {
		// TODO Auto-generated method stub

	}

	/*
	 * ����豸�Ƿ�֧��NFC����
	 */
	private Boolean ifNFCUse() {
		if (nfcAdapter == null) {
			Toast.makeText(getApplication(), "�����豸��֧��NFC���ܣ�", Toast.LENGTH_LONG).show();
			return false;
		}
		if(nfcAdapter != null && !nfcAdapter.isEnabled()){
			Toast.makeText(getApplication(), "����ϵͳ������������NFC���ܣ�", Toast.LENGTH_LONG).show();
			return true;	
		}
		return true;
	}
	/*
	 * ��ʼ��
	 */
	private void init() {
		uid_info = (TextView) findViewById(R.id.txt_uid_info);
		
		mLocationLatlngTextView = (TextView) findViewById(R.id.txt_location_info);
		mLocationTimeTextView = (TextView) findViewById(R.id.txt_location_time);
		mLocationDesTextView = (TextView) findViewById(R.id.txt_location_context);
		//NFC������
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);//�豸ע��
		if(!ifNFCUse()){
			return;
		}

		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndef = new IntentFilter(
				NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("text/plain");
		} catch (MalformedMimeTypeException e) {
		}
		mNdefExchangeFilters = new IntentFilter[] { ndef };
		mTechLists = new String[][] { 
				new String[] { NfcA.class.getName() },
				new String[] { NfcF.class.getName() },
				new String[] { NfcB.class.getName() },
				new String[] { NfcV.class.getName() } };// ����ɨ��ı�ǩ����
	}

	/* 
	 * ��дonResume�ص��������������ڴ����ζ�ȡNFC��ǩʱ�����
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// ǰ̨�ַ�ϵͳ,������������ڵڶ��μ��NFC��ǩʱ��Ӧ������ߵĲ�������Ȩ.
		nfcAdapter.enableForegroundDispatch(this, pendingIntent, mNdefExchangeFilters,
				mTechLists);

		mapView.onResume();
	}


	@Override
	protected void onPause() {
		super.onPause();
		nfcAdapter.disableForegroundDispatch(this);

		mapView.onPause();
		deactivate();
	}

	/**
	 * ����������д
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * �˷��������
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * ��λ�ɹ���ص�����
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {

		if (mListener != null && amapLocation != null) {
			if (amapLocation.getAMapException().getErrorCode() == 0) {
				mListener.onLocationChanged(amapLocation);// ��ʾϵͳС����
				mLocationLatlngTextView.setText(amapLocation.getLatitude() + "  "
						+ amapLocation.getLongitude());
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date(amapLocation.getTime());
				
				mLocationTimeTextView.setText(df.format(date));
				mLocationDesTextView.setText(amapLocation.getAddress());
			}
			
		}
	}


	/*
	 * NFC
	 * �б�ҪҪ�˽�onNewIntent�ص������ĵ���ʱ��,������������ѯ
	 *  (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */

	@Override
	protected void onNewIntent(Intent intent) {
		//		super.onNewIntent(intent);
		byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
		uid_info.setText(Converter.getHexString(myNFCID, myNFCID.length));
		dialog("uid:"+Converter.getHexString(myNFCID, myNFCID.length));//��ȡuid
	}

	protected void dialog(String str) {
		new AlertDialog.Builder(this).setTitle("��ʾ��Ϣ")
		.setIcon(android.R.drawable.btn_star).setMessage(str)
		.setPositiveButton("ȷ��", null).show();
	}

	/**
	 * ���λ
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			//�˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
			//ע�����ú��ʵĶ�λʱ��ļ���������ں���ʱ�����removeUpdates()������ȡ����λ����
			//�ڶ�λ�������ں��ʵ��������ڵ���destroy()����     
			//����������ʱ��Ϊ-1����λֻ��һ��
			mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 60*1000, 10, this);
		}
	}

	/**
	 * ֹͣ��λ
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub
		
	}

	
}
