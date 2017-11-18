package info.holliston.high.app.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Boilerplate class for handling Firebase Instances
 *
 */

public class HHSFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "HHSFCMIIDService";

    @Override
    public void onTokenRefresh() {
        //Get hold of the registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log the token
        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }
}
