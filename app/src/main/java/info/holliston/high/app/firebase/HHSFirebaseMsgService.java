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

    /**
     * Processes cloud notifications received when the app is in the FOREGROUND
     */
    private static final String TAG = "MyAndroidFCMService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Make a system notification out of this cloud notification
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            //Log data to Log Cat
            Log.d(TAG, "Notification From: " + remoteMessage.getFrom());
            Log.d(TAG, "Notification Message: " + remoteMessage.getNotification().getBody());
            //create notification
            createNotification(remoteMessage.getNotification().getBody());
        }

        // Process the data portion of the cloud notification
        Map<String, String> data = remoteMessage.getData();
        if ((data != null) && (data.size()>0)) {
            if (data.containsKey("update_data")) {
                updateData();
            }
        }
    }

    /**
     * Creates a system notification
     *
     * @param messageBody the notification message to show
     */
    private void createNotification( String messageBody) {

        // Creates an intent so the app opens when the user clicks the notification
        Intent intent = new Intent( this , MainActivity.class );
        intent.putExtra("newsTitle", messageBody);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity( this , 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Creates the notification with the intent
        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder( this, "HHS_ID")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New Post from Holliston High School")
                .setContentText(messageBody)
                .setAutoCancel( true )
                .setSound(notificationSoundURI)
                .setContentIntent(resultIntent);

        // Cancels past notifications and posts this new one
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         if (notificationManager != null) {
            notificationManager.cancelAll();
            notificationManager.notify(0, mNotificationBuilder.build());
        }
    }

    /**
     * Calls a new Async data refresh. The MainActivity and/or fragment listeners should
     * respond when the refresh is done.
     */
    private void updateData() {
        DownloaderAsyncTask task = new DownloaderAsyncTask(getApplicationContext());
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}

