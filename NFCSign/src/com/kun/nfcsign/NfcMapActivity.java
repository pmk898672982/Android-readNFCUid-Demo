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
	private TextView mLocationLatlngTextView;// 坐标信息
	private TextView mLocationTimeTextView;// 定位时间信息
	private TextView mLocationDesTextView;// 定位描述信息
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
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
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setZoomControlsEnabled(false);//设置默认的缩放按钮是否显示
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式：定位（AMap.LOCATION_TYPE_LOCATE）、跟随（AMap.LOCATION_TYPE_MAP_FOLLOW）
		// 地图根据面向方向旋转（AMap.LOCATION_TYPE_MAP_ROTATE）三种模式
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
		aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
		aMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件
		addMarkersToMap();// 往地图上添加marker
	}
	// 往地图上添加marker
	private void addMarkersToMap() {
		// TODO Auto-generated method stub

	}

	/*
	 * 检查设备是否支持NFC功能
	 */
	private Boolean ifNFCUse() {
		if (nfcAdapter == null) {
			Toast.makeText(getApplication(), "您的设备不支持NFC功能！", Toast.LENGTH_LONG).show();
			return false;
		}
		if(nfcAdapter != null && !nfcAdapter.isEnabled()){
			Toast.makeText(getApplication(), "请在系统设置中先启用NFC功能！", Toast.LENGTH_LONG).show();
			return true;	
		}
		return true;
	}
	/*
	 * 初始化
	 */
	private void init() {
		uid_info = (TextView) findViewById(R.id.txt_uid_info);
		
		mLocationLatlngTextView = (TextView) findViewById(R.id.txt_location_info);
		mLocationTimeTextView = (TextView) findViewById(R.id.txt_location_time);
		mLocationDesTextView = (TextView) findViewById(R.id.txt_location_context);
		//NFC适配器
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);//设备注册
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
				new String[] { NfcV.class.getName() } };// 允许扫描的标签类型
	}

	/* 
	 * 重写onResume回调函数的意义在于处理多次读取NFC标签时的情况
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// 前台分发系统,这里的作用在于第二次检测NFC标签时该应用有最高的捕获优先权.
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
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 此方法需存在
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {

		if (mListener != null && amapLocation != null) {
			if (amapLocation.getAMapException().getErrorCode() == 0) {
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
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
	 * 有必要要了解onNewIntent回调函数的调用时机,请自行上网查询
	 *  (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */

	@Override
	protected void onNewIntent(Intent intent) {
		//		super.onNewIntent(intent);
		byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
		uid_info.setText(Converter.getHexString(myNFCID, myNFCID.length));
		dialog("uid:"+Converter.getHexString(myNFCID, myNFCID.length));//读取uid
	}

	protected void dialog(String str) {
		new AlertDialog.Builder(this).setTitle("提示信息")
		.setIcon(android.R.drawable.btn_star).setMessage(str)
		.setPositiveButton("确定", null).show();
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			//此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			//注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
			//在定位结束后，在合适的生命周期调用destroy()方法     
			//其中如果间隔时间为-1，则定位只定一次
			mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 60*1000, 10, this);
		}
	}

	/**
	 * 停止定位
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
