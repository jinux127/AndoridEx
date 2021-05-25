package com.jointree.wifilist;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothActivity extends AppCompatActivity implements AutoPermissionsListener {

    private final static String TAG = "블루투스";
    // 스캔 주기 설정
    private final static int SCAN_PERIOD = 5000;
    //    gatt 접속 지연 시간
    private final static int GATT_PERIOD = 500;

    IntentFilter intentFilter = new IntentFilter();
    private int count = 0;
    private int flag = 0;

    public Button btn_bluetoothScan;

    //블루투스 매니저는 기본적으로 있어야하기때문에 여기서는 생략합니다.
    private BluetoothManager bluetoothManager;
    //블루투스 어댑터에서 탐색, 연결을 담당하니 여기서는 어댑터가 주된 클래스입니다.
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    // used to identify adding bluetooth names
    private final static int REQUEST_ENABLE_BT = 1;
    // used to request fine location permission
    private final static int REQUEST_FINE_LOCATION = 2;
    // flag for scanning
    private boolean scanFlag = false;
    // flag for connection
    private boolean connectedFlag = false;
    // scan results
    private Map<String, BluetoothDevice> scanResults;
    // scan callback
    private ScanCallback scanCallback;
    // ble scanner
    private BluetoothLeScanner bluetoothLeScanner;
    // scan handler
    private Handler scanHandler;
    // delay handler
    private Handler delayHandler;


    private LineChart lineChart;
    private Thread thread;

    private Context context;
    SharedPreferences sf;
    Intent intent;

    @Override
    protected void onResume() {
        super.onResume();

        // BLE 지원하지 않으면 종료 설정
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        count = 0;
        flag = 0;
        super.onBackPressed();
        if (isFinishing()){
            overridePendingTransition(R.anim.none,R.anim.vertical_exit);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        overridePendingTransition(R.anim.vertical_enter,R.anim.none);

        lineChart = (LineChart) findViewById(R.id.chart);

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.animateXY(2000, 2000);
        lineChart.invalidate();


        LineData lineData = new LineData();
        lineChart.setData(lineData);

        context = this;
        btn_bluetoothScan = findViewById(R.id.btn_bluetoothScan);
        Log.d(TAG, "onCreate");


        sf = context.getSharedPreferences("sessionCookie", Context.MODE_PRIVATE);

        intent = getIntent();

        //onCreate내부
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //권한에 대한 자동 허가 요청 및 설명
        AutoPermissions.Companion.loadAllPermissions(this, 101);

        //Wifi Scan 관련


    }

    private void feedMultiple() {
        if (thread != null) thread.interrupt();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
            }
        };
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void addEntry(int y) {
        LineData data = lineChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(), y), 0);
            data.notifyDataChanged();
            YAxis yAxis = lineChart.getAxisLeft();
            yAxis.setAxisMaximum(0);
            yAxis.setAxisMinimum(-100);
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(100);
            lineChart.moveViewToX(data.getEntryCount());
        }

    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "rssi");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#d7e7fa"));
        set.setColor(Color.parseColor("#0B80C9"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleColorHole(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);
        set.setLineWidth(2);
        set.setCircleRadius(60);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        set.setHighLightColor(Color.rgb(244, 117, 117));
        return set;

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(thread != null){
            thread.interrupt();
        }
    }

    //    BLE 스캔 조건 점검 시작
    private void startScan(View view) {
        Log.d(TAG, "스캔 시작!!!");

        // 권한 검사
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            Log.d(TAG, "스캔 실패, 권한 없음");
            return;
        }

        //실행전 혹시 있을 서버를 해제하고 시작
        Log.d(TAG, "혹시 있을 서버 연결 종료");
        disconnectGattServer();

//        스캔 주기 설정, 5초뒤 멈춤
//        scanHandler = new Handler();
//        scanHandler.postDelayed(this::stopScan, SCAN_PERIOD);

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            requestEnableBLE();
            Log.d(TAG, "스캔 실패 , ble not enabled");
            return;
        }

        String MAC_ADDR = "40:C7:11:12:E8:57";
//        스캔필터 설정
//        스캔 필터 리스트
        List<ScanFilter> filters = new ArrayList<>();
//        MAC주소로 스캔필터 생성
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setDeviceAddress(MAC_ADDR)
                .build();
//        필터에 추가
        filters.add(scanFilter);
//        스캔 저전력 모드 설정
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
//        콜백 설정
        scanResults = new HashMap<>();
        scanCallback = new BLEScanCallback(scanResults);

//        준비 끝 스캔 시작
        bluetoothLeScanner.startScan(filters, settings, scanCallback);

//        스캐닝 flag 설정
        scanFlag = true;


    }

    private void stopScan() {
//        상태 체크
        if (scanFlag && bluetoothAdapter != null && bluetoothAdapter.isEnabled() && bluetoothLeScanner != null) {
            // 스캔 멈춤
            if (bluetoothLeScanner == null)
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.stopScan(scanCallback);
            scanComplete();
        }
        // 플래그 리셋
        scanCallback = null;
        scanFlag = false;
        scanHandler = null;

        Log.d(TAG, "스캔 멈춰!!!");

    }

    private void requestEnableBLE() {//    BLE 사용 검사
        Intent ble_enable_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(ble_enable_intent, REQUEST_ENABLE_BT);
    }

    private void requestLocationPermission() {//  퍼미션 검사
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    //    BLE 스캔 콜백
    private class BLEScanCallback extends ScanCallback {
        private Map<String, BluetoothDevice> callbackScanResults;

        BLEScanCallback(Map<String, BluetoothDevice> scanResults) {
            callbackScanResults = scanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "스캔 실패 코드 : " + errorCode);
        }

        //        스캔결과 추가
        private void addScanResult(ScanResult scanResult) {
            // get scanned device
            BluetoothDevice device = scanResult.getDevice();
//            세기
            int rssi = scanResult.getRssi();
            // 맥 주소
            String deviceAddress = device.getAddress();
//            디바이스 이름
            String deviceName = device.getName();

            callbackScanResults.put(deviceAddress, device);

            // add the device to the result list
//            cb_scan_results_.put( device_address, device );
            // log
            Log.d(TAG, "scan results device: " + device);
            Log.d(TAG, "deviceName: " + deviceName);
            Log.d(TAG, "rssi: " + rssi);

            addEntry(rssi);

        }
    }

    private void scanComplete() {
//        스캔 결과 없을 경우
        if (scanResults.isEmpty()) {
            Log.d(TAG, "스캔 결과 없음");
            return;
        }
        for (String deviceAddr : scanResults.keySet()) {
            Log.d(TAG, "찾은 장치: " + deviceAddr);
            // 찾은 장치 이용, 찾을 때 맥주소로 지정해서 찾기때문에 지정된 장치만 이용함
            Log.d(TAG, "device : " + String.valueOf(scanResults.get(deviceAddr)));
            Log.d(TAG, "deviceAddr : " + deviceAddr);
            Log.d(TAG, String.valueOf(scanResults.get(deviceAddr)));
            BluetoothDevice device = scanResults.get(deviceAddr);
//            if (device.equals(deviceAddr)){
            Log.d(TAG, "장치 연결 중 : " + deviceAddr);

//          연결 딜레이
            delayHandler = new Handler();
            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectDevice(device);
                }
            }, 60);


//            }
        }
    }

    private void connectDevice(BluetoothDevice device) {
        // 상태 업데이트
        Log.d(TAG, "연결 중 :" + device.getAddress());
        GattClientCallback gattClientCallback = new GattClientCallback();
        bluetoothGatt = device.connectGatt(this, false, gattClientCallback, BluetoothDevice.TRANSPORT_LE);
    }

    //    Gatt 콜백함수
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "블루투스 연결 상태 변화");
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.d(TAG, "GATT 연결 실패");
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "GATT 연결 에러 상태 : " + status);
                disconnectGattServer();
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                상태 업데이트
                Log.d(TAG, "GATT 연결성공");
//                flag 설정
                connectedFlag = true;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "연결 해제 : " + newState);
                disconnectGattServer();
            }
        }
    }

    //    gatt server 해제 함수
    public void disconnectGattServer() {
        Log.d(TAG, "Gatt 연결 종료");
//        flag 설정
        connectedFlag = false;
//        gatt 해제 및 종료
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

//    BLE 스캔 조건 점검 끝

    //버튼을 눌렀을 때
    public void clickBluetoothScan(View view) {
        Toast.makeText(BluetoothActivity.this, "블루투스 클릭", Toast.LENGTH_SHORT).show();
        if (bluetoothLeScanner == null)
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        startScan(view);

//        bluetoothAdapter.startLeScan(leScanCallback);//탐색시작

    }// clickBluetoothScan()..

    //버튼을 눌렀을 때
    public void clickScanStop(View view) {
        Toast.makeText(BluetoothActivity.this, "스캔 멈춤", Toast.LENGTH_SHORT).show();
        // 스캔 멈춤
        Log.d(TAG, "버튼눌렀음 스캔 멈춰!!!");
        if (bluetoothLeScanner == null)
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.stopScan(scanCallback);

//      플래그 리셋
        scanCallback = null;
        scanFlag = false;
        scanHandler = null;

    }// clickBluetoothScan()..


    //Permission에 관한 메소드
    @Override
    public void onDenied(int i, String[] strings) {
        Toast.makeText(this, "onDenied~~", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGranted(int i, String[] strings) {
        Toast.makeText(this, "onGranted~~", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }
    //Permission에 관한 메소드


    //    키보드밖 터치 메소드
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}
