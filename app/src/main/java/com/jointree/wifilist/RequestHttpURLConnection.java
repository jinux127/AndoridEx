package com.jointree.wifilist;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.webkit.CookieManager;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHttpURLConnection {
    String TAG = "RequestHttpURLConnection";

    public String jsonPost(String url, String ... params){
        Log.d(TAG,"jsonPost start");

        InputStream is = null;
        String result = "";
        HttpURLConnection huc = null;

        try {
            URL urlCon = new URL(url);
            huc = (HttpURLConnection) urlCon.openConnection();

            huc.setRequestProperty("Accept", "application/json");
            huc.setRequestProperty("Content-type", "application/json");

            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            huc.setDoOutput(true);

            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            huc.setDoInput(true);
            Log.d(TAG,"json:"+params[0]);
            OutputStream os = huc.getOutputStream();
            os.write(params[0].getBytes("UTF-8"));
            os.flush();
            os.close();
            try {
                is = huc.getInputStream();
                // convert inputstream to string
                if (is != null){
                    result = convertInputStreamToString(is);
                    Log.d(TAG,"result : "+result);
                }else{
                    result = "Did not work!";
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                huc.disconnect();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public String loginPost(String url, String userId, String password,Context context){
        Log.d(TAG,"loginPost start");

        InputStream is = null;
        String result = "";
        HttpURLConnection huc = null;

        try {
            URL urlCon = new URL(url);
            Log.d(TAG,url);
            huc = (HttpURLConnection) urlCon.openConnection();


            huc.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            huc.setDoOutput(true);

            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            huc.setDoInput(true);

            setCookieHeader(huc,context);

            String sendMsg = "userId="+userId +"&password=" + password;
            Log.d(TAG,"sendMsg : "+sendMsg);
            OutputStream os = huc.getOutputStream();
            os.write(sendMsg.getBytes("UTF-8"));
            os.flush();
            os.close();
            try {
                getCookieHeader(huc,context);
//                is = huc.getInputStream();
//                // convert inputstream to string
//                if (is != null){
//                    result = convertInputStreamToString(is);
//                    Log.d(TAG,"result : "+result);
//                }else{
//                    result = "Did not work!";
//                }
            } finally {
                huc.disconnect();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    private void getCookieHeader(HttpURLConnection con,Context context){//Set-Cookie에 배열로 돼있는 쿠키들을 스트링 한줄로 변환
        Log.d("getCookieheader","start");
        List<String> cookies = con.getHeaderFields().get("Set-Cookie");
        //cookies -> [JSESSIONID=D3F829CE262BC65853F851F6549C7F3E; Path=/smartudy; HttpOnly] -> []가 쿠키1개임.
        //Path -> 쿠키가 유효한 경로 ,/smartudy의 하위 경로에 위의 쿠키를 사용 가능.
        if (cookies != null) {
            for (String cookie : cookies) {
                String sessionid = cookie.split(";\\s*")[0];
                //JSESSIONID=FB42C80FC3428ABBEF185C24DBBF6C40를 얻음.
                //세션아이디가 포함된 쿠키를 얻었음.
                setSessionIdInSharedPref(sessionid,context);

            }
        }
    }
    private void setSessionIdInSharedPref(String sessionid,Context context){
        Log.d("setSessionIdInSharedPre","start");
        SharedPreferences pref = context.getSharedPreferences("sessionCookie", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        if(pref.getString("sessionid",null) == null){ //처음 로그인하여 세션아이디를 받은 경우
            Log.d("LOG","처음 로그인하여 세션 아이디를 pref에 넣었습니다."+sessionid);
        }else if(!pref.getString("sessionid",null).equals(sessionid)){ //서버의 세션 아이디 만료 후 갱신된 아이디가 수신된경우
            Log.d("LOG","기존의 세션 아이디"+pref.getString("sessionid",null)+"가 만료 되어서 "
                    +"서버의 세션 아이디 "+sessionid+" 로 교체 되었습니다.");
        }
        edit.putString("sessionid",sessionid);
        edit.apply(); //비동기 처리
    }

    private void setCookieHeader(HttpURLConnection con,Context context){
        Log.d("setCookie","start");
        SharedPreferences pref = context.getSharedPreferences("sessionCookie",Context.MODE_PRIVATE);
        String sessionid = pref.getString("sessionid",null);
        if(sessionid!=null) {
            Log.d("LOG","세션 아이디"+sessionid+"가 요청 헤더에 포함 되었습니다.");
            con.setRequestProperty("Cookie", sessionid);
        }
    }




}
