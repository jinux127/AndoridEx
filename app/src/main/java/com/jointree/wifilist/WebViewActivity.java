package com.jointree.wifilist;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.iid.FirebaseInstanceId;
import com.jointree.wifilist.Dao.AddressDao;
import com.jointree.wifilist.Database.Databases;
import com.jointree.wifilist.Entity.Address;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    String SERVER_URL = BuildConfig.SERVER_URL;

    String target_url = SERVER_URL; // 메인 주소

    public static FrameLayout mContainer;
    public WebView mWebView; // 웹뷰 선언
    public WebView newWebView; // 웹뷰 선언
    private WebSettings mWebSettings;  //웹뷰세팅


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        //기본 세팅
        mContainer = (FrameLayout) findViewById(R.id.webview_frame);
        mWebView = (WebView) findViewById(R.id.webView);
        newWebView = findViewById(R.id.webView);

        mWebSettings = mWebView.getSettings(); //세부 세팅 등록


//        스크립트 허용여부 필수
//        다중 윈도우 허용
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스크립트 허용 여부
        mWebSettings.setSupportMultipleWindows(true); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 자바스크립트에서 자동으로 창을 열도록 지시, ex) window.open()

        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(true); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(true); // 화면 확대 축소 허용 여부
        mWebSettings.setDisplayZoomControls(true); //
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기

        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        //        자동로그인 하기 위해서  override >> onResume, onPause 설정
        CookieSyncManager.createInstance(this);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if( bundle != null){
            if(bundle.getString("url") != null && !bundle.getString("url").equalsIgnoreCase("")) {
                target_url = bundle.getString("url");
            }
        }

//        스크립트 사용하기 위한 설정, 호출 시 이름은 Android.뭐시기
        mWebView.addJavascriptInterface(new WebViewJavascriptInterface(), "Android");

        mWebView.loadUrl(target_url); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작



        mWebView.setWebViewClient(new WebViewClientClass());



//        WebChromeClient 설정
        mWebView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                Log.d("확인","새창 띄움");
                newWebView =  new WebView(WebViewActivity.this);
                WebSettings webSettings = newWebView.getSettings();


                webSettings.setJavaScriptEnabled(true);
                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                webSettings.setSupportMultipleWindows(true);
                //// Sets whether the DOM storage API is enabled.
                webSettings.setDomStorageEnabled(true);
                ////

                final Dialog dialog = new Dialog(WebViewActivity.this);
                dialog.setContentView(newWebView);

                ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
                dialog.setCancelable(false);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        Log.d("확인","dialog.setOnKeyListener 키 : " + keyCode);
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            newWebView.loadUrl("javascript:self.close();");
                            return true;
                        }
                        return false;
                    }

                });

                dialog.show();

                newWebView.setWebChromeClient(new WebChromeClient(){
                    @Override
                    public void onCloseWindow(WebView window) {
//                        super.onCloseWindow(window);
//                        window.removeView(newWebView);
                        Log.d("확인","새창 끔 캐치");
                        dialog.dismiss();
                    }

                });

                newWebView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        return false;
                    }
                });

                ((WebView.WebViewTransport)resultMsg.obj).setWebView(newWebView);
                resultMsg.sendToTarget();

                return true;
            }

        });

    }



//    키눌렸을 때
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("확인","onKeyDown 키 : " + keyCode);
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()){
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode,event);

    }

    private class WebViewClientClass extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

    }


//  웹, 자바스크립트에서 호출할 수 있게 설정함
    public class WebViewJavascriptInterface {
        @JavascriptInterface
        public String getAndroidToken() {
            String AndroidToken = FirebaseInstanceId.getInstance().getToken();
            return AndroidToken;
        }

        @JavascriptInterface
        public void getWifiList() {
            Intent intent = new Intent(WebViewActivity.this, MainActivity.class);
            startActivity(intent);
        }

        @JavascriptInterface
        public void getBluetooth() {
            Intent intent = new Intent(WebViewActivity.this, BluetoothActivity.class);
            startActivity(intent);
        }

        @JavascriptInterface
        public void getRecyclerViewTest() {
            Intent intent = new Intent(WebViewActivity.this, TestActivity.class);
            startActivity(intent);
        }

        @JavascriptInterface
        public void insertAddress(String address1) {
            if (address1 == null){
                Toast.makeText(WebViewActivity.this, "주소 입력해주시기 바랍니다", Toast.LENGTH_SHORT).show();
                Log.d("로컬 DB 작업 과정","address 없음");
            }else {
                Databases db = Databases.getDatabases(WebViewActivity.this);
                new InsertAsyncTask(db.addressDao()).execute(new Address(address1));
                Log.d("로컬 DB 작업 과정","address 있음 "+address1);
            }
        }
    }

    public static class InsertAsyncTask extends AsyncTask<Address,Void,Void>{
        private AddressDao addressDao;

        public InsertAsyncTask(AddressDao addressDao){
            this.addressDao = addressDao;
        }

        @Override
        protected Void doInBackground(Address... addresses) {
            Log.d("로컬 DB 작업", String.valueOf(addresses[0].address));
            addressDao.insertAddress(addresses[0]);
            return null;
        }
    }

//  어플이꺼지면 쿠키,세션 삭제
/*    @Override
    protected void onDestroy() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }*/
//  자동로그인 설정
    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();
    }



}