package com.jointree.wifilist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

//  토큰 생성
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e("New_Token",token);

    }



    /*알람표시*/
    private void sendTopNotification(String title, String content,String url) {
        Log.d("Noti","떴나");
        final String CHANNEL_DEFAULT_IMPORTANCE = "channel_id";
        final int ONGOING_NOTIFICATION_ID = 1;
//        앱안에서 A에서 B 기능을 열기
        Intent notificationIntent = new Intent(this, WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("url",url);
        notificationIntent.putExtras(bundle);

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        외부에서 이 intent를 포함하고 있는 PendingIntent 선언하여 사용
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSmallIcon(R.drawable.common_full_open_on_phone)
                        .setContentIntent(pendingIntent) // 이 부분이 이벤트
                        .build();
        //실제 사용자에게 UI를 그리라고 알려주는(notify) 역할
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            오레오 이상을 target하는 앱이라면 필수적으로 구현해야하는 푸시 전용 채널이며, 이 채널은 그룹핑할 수 있어 사용자가 직접 푸시 그룹들로 묶인 채널을 받을지 안받을지 제어할 수 있도록 하는 역할
            NotificationChannel channel = new NotificationChannel(CHANNEL_DEFAULT_IMPORTANCE,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
    }

    /**
     * 메시지 수신받는 메소드
     * @param msg
     */
    @Override
    public void onMessageReceived(RemoteMessage msg) {

        if (msg.getNotification() != null) {
            Log.i("### notification : ", " msg: "+msg.getNotification().getBody());
            sendTopNotification(msg.getNotification().getTitle(), msg.getNotification().getBody(), String.valueOf(msg.getNotification().getImageUrl()));
        }
    }




}
