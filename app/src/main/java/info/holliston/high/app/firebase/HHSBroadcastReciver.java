package info.holliston.high.app.firebase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Starts the messaging service when the phone reboots
 */

public class HHSBroadcastReciver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, HHSFirebaseMsgService.class);
        context.startService(startServiceIntent);
    }
}
