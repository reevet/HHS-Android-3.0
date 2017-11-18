package info.holliston.high.app.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import info.holliston.high.app.MainActivity;
import info.holliston.high.app.R;
import info.holliston.high.app.datamodel.DownloaderAsyncTask;

/**
 * Handles Notifications sent from the Firebase System
 *
 * @author Tom Reeve
 */

public class HHSFirebaseMsgService extends FirebaseMessagingService {

    private static final String TAG = "MyAndroidFCMService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {

            //Log data to Log Cat
            Log.d(TAG, "Notification From: " + remoteMessage.getFrom());
            Log.d(TAG, "Notification Message: " + remoteMessage.getNotification().getBody());
            //create notification
            createNotification(remoteMessage.getNotification().getBody());
        }

        Map<String, String> data = remoteMessage.getData();
        if ((data != null) && (data.size()>0)) {
            if (data.containsKey("update_data")) {
                updateData(data.get("update_data"));
            }
        }
    }

    private void createNotification( String messageBody) {

        Intent intent = new Intent( this , MainActivity.class );
        intent.putExtra("showNewNews", "true");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity( this , 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder( this, "HHS_ID")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New Post from Holliston High School")
                .setContentText(messageBody)
                .setAutoCancel( true )
                .setSound(notificationSoundURI)
                .setContentIntent(resultIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.cancelAll();
            notificationManager.notify(0, mNotificationBuilder.build());

        }
    }

    private void updateData(String source) {
        if (source == null) { source = "";}
        DownloaderAsyncTask task = new DownloaderAsyncTask(getApplicationContext());
        task.setSource(source);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}

