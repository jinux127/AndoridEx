package com.jointree.wifilist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {

    IntentFilter intentFilter = new IntentFilter();
    WifiManager wifiManager;
    private int count = 0;
    private int flag = 0;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    public TextView tv_count;
    public EditText et_roomName;
    public EditText et_personName;
    public EditText et_personTel;
    public Button btn_wifiScan;
    public Button btn_locationScan;


    private Context context;
    SharedPreferences sf;
    Intent intent;




    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {   // wifiManager.startScan(); 시  발동되는 메소드 ( 예제에서는 버튼을 누르면 startScan()을 했음. )

            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false); //스캔 성공 여부 값 반환
            if (success && count < 5) {
                Toast.makeText(MainActivity.this, "와이파이 스캔", Toast.LENGTH_SHORT).show();
                scanSuccess(flag);
                flag = 0;
            }else {
                scanFailure();
                flag= 0;
            }
        }// onReceive()..
    };

    private void scanSuccess(int flag) {    // Wifi검색 성공

        Log.d("CCC","scan success");

        List<ScanResult> results = wifiManager.getScanResults();
        String json = "";

        if (flag <2){
            ++count;
            tv_count.setText("횟수: "+count +"/4");

            if (count==4){
                btn_wifiScan.setEnabled(false);
            }
        }

        try {
            json = scanResultsTojson(et_roomName.getText().toString(),et_personName.getText().toString(),et_personTel.getText().toString(),results);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//      화면에 표시
        mAdapter = new MyAdapter(results); //테스트 1넣어놈
        recyclerView.setAdapter(mAdapter);

        Data myData = new Data.Builder()
                .putString("json",json)
                .putInt("flag",flag)
                .build();

        OneTimeWorkRequest scanPOSTWorker = new OneTimeWorkRequest.Builder(scanPostWorker.class)
                .setInputData(myData)
                .build();
        WorkManager workManager = null;

        workManager.getInstance().beginWith(scanPOSTWorker).enqueue();


//        workManager.getInstance().getWorkInfoByIdLiveData(scanPOSTWorker.getId())
//                .observe(this, new Observer<WorkInfo>() {
//                    @Override
//                    public void onChanged(WorkInfo workInfo) {
//                        if (workInfo != null && workInfo.getState().isFinished()){
//                                String output = workInfo.getOutputData().getString("strBody");
//                                Log.d("Retrofit","output: "+ output);
//                        }
//                    }
//                });



        getApplicationContext().unregisterReceiver(wifiScanReceiver);
    }


    private void scanFailure() {    // Wifi검색 실패
        Toast.makeText(MainActivity.this, "오류", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        count = 0;
        flag =0;
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        recyclerView = findViewById(R.id.rv_recyclerview);
        et_roomName = findViewById(R.id.et_roomName);
        et_personName = findViewById(R.id.et_personName);
        et_personTel = findViewById(R.id.et_personTel);
        btn_wifiScan = findViewById(R.id.btn_bluetoothScan);
        btn_locationScan = findViewById(R.id.btn_locationScan);
        Log.d("반복 테스트","반복 테스트6");
        tv_count = findViewById(R.id.tv_count);
        tv_count.setText("횟수:"+count+"/4");

        et_personTel.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        sf = context.getSharedPreferences("sessionCookie",Context.MODE_PRIVATE);

        intent = getIntent();

        //권한에 대한 자동 허가 요청 및 설명
        AutoPermissions.Companion.loadAllPermissions(this, 101);

        //Wifi Scan 관련
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);


    }

    @Override
    protected void onDestroy() {
        Log.d("반복 테스트","반복 테스트7");

        super.onDestroy();
    }

    //버튼을 눌렀을 때
    public void clickLocationScan(View view){
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        flag = 2;
        boolean success = wifiManager.startScan();
        if (!success)
            Toast.makeText(MainActivity.this, "횟수 초과", Toast.LENGTH_SHORT).show();
    }
    //버튼을 눌렀을 때
    public void clickLocationScan2(View view){
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        flag = 3;
        boolean success = wifiManager.startScan();
        if (!success)
            Toast.makeText(MainActivity.this, "횟수 초과", Toast.LENGTH_SHORT).show();
    }

    //버튼을 눌렀을 때
    public void clickWifiScan(View view) {
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        if (TextUtils.isEmpty(et_roomName.getText())){
            Toast.makeText(MainActivity.this, "방이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(et_personTel.getText())){
            Toast.makeText(MainActivity.this, "전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }else{
            flag = 1;

            boolean success = wifiManager.startScan();
            if (!success)
                Toast.makeText(MainActivity.this, "횟수 초과", Toast.LENGTH_SHORT).show();
        }

    }// clickWifiScan()..

    //버튼을 눌렀을 때
    public void clickReset(View view) {
        flag = 0;
        et_roomName.setText("");
        count = 0;
        btn_wifiScan.setEnabled(true);
        tv_count.setText("횟수:"+count+"/4");
        Toast.makeText(MainActivity.this, "횟수 리셋.", Toast.LENGTH_SHORT).show();
    }// clickWifiScan()..

    public String scanResultsTojson(String roomName, String personName, String personTel, List<ScanResult>... results) throws JSONException {

        String json = "";
        JSONArray jsonArray = new JSONArray();
        if (flag==1){
            for (int i=0; i<results[0].size();i++){//임시
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("SSID",results[0].get(i).SSID);
                jsonObject.accumulate("BSSID",results[0].get(i).BSSID);
                jsonObject.accumulate("level",results[0].get(i).level);
                jsonObject.accumulate("roomName",roomName);
                jsonObject.accumulate("targetTel",personTel);
                jsonObject.accumulate("count",count);
                jsonArray.put(jsonObject);

            }
            json = jsonArray.toString();
        }else if (flag ==2){
            for (int i=0; i<results[0].size();i++){//임시
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate(results[0].get(i).BSSID,results[0].get(i).level);
                jsonObject.accumulate("targetTel",personTel);
                jsonArray.put(jsonObject);

            }
            json = jsonArray.toString();
        }else if (flag ==3){
            for (int i=0; i<results[0].size();i++){//임시
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("SSID",results[0].get(i).SSID);
                jsonObject.accumulate("BSSID",results[0].get(i).BSSID);
                jsonObject.accumulate("level",results[0].get(i).level);
                jsonObject.accumulate("roomName",roomName);
                jsonObject.accumulate("targetTel",personTel);
                jsonObject.accumulate("count",count);
                jsonArray.put(jsonObject);

            }
            json = jsonArray.toString();
        }

        return json;
    }

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
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}
