package nova.typoapp.firebase;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import nova.typoapp.CommentActivity;
import nova.typoapp.MainActivity;
import nova.typoapp.R;
import nova.typoapp.SubCommentActivity;


public class MyFireBaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService  {
    private static final String TAG = "FirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());





        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {


            //fcm 메세지 payload 에서 필요한 데이터를 추출한다.

            //메시지 내용, 어떤 액티비티로 가야 하는 지, 어떤 뉴스피드에 대한 알림인지를
            //알 수 있다.

            String messageBody = remoteMessage.getData().get("message");

            String activityString = remoteMessage.getData().get("activity");

            int feedID = Integer.parseInt(remoteMessage.getData().get("feedID"));

            sendNotification(messageBody, activityString, feedID);

            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        }

//        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//
//            sendNotification(remoteMessage.getNotification().getBody());
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }

//        //추가한것
//        sendNotification(remoteMessage.getData().get("message"));
    }

    private void sendNotification(String messageBody, String activityString, int feedID ) {
        Log.d("MyFirebaseIIDService", "received messasge : " + messageBody);



        Intent intent = null;
        //activityString 값에 따라 인텐트의 액티비티를 다르게 설정해준다.
        switch (activityString){

            case "MainActivity":

                intent = new Intent(this, MainActivity.class);

                intent.putExtra("feedIDFromFcm", feedID);

                break;


            case "CommentActivity":

                intent = new Intent(this, CommentActivity.class);

                intent.putExtra("feedIDFromFcm", feedID);

                break;


            case "SubCommentActivity":

                intent = new Intent(this, SubCommentActivity.class);
                break;

        }



        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        //아래의 인텐트를 통해, 알림을 누르면 가는 인텐트를 결정한다.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);


//        //이미지 추가를 위한 파트. 글자수로 인해 줄이 바뀌므로 주석처리해둔다.
//        String url = "http://115.68.231.13/project/android/profileimage/jamsya@naver.commVgcI6yAbH.png";
//        Bitmap theBitmap = null; // Width and height
//        try {
//            theBitmap = Glide.with(this)
//                    .asBitmap()
//                    .load(url)
//                    .into(50, 50)
//                    . get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }


        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name)
//                .setLargeIcon(theBitmap)



//                .setContentTitle(messageBody)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);



        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakeLock.acquire(5000);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
