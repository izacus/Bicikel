package si.virag.bicikelj.util;

import android.app.Activity;
import android.app.Dialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GPSUtil {
    public static final int GPS_FAIL_DIALOG_REQUEST_CODE = 9584;

    private static boolean hasPlayServices = false;

    public static boolean checkPlayServices(Activity context) {
        if (hasPlayServices) {
            return true;
        }

        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int error = availability.isGooglePlayServicesAvailable(context);

        if (error == ConnectionResult.SUCCESS) {
            hasPlayServices = true;
            return true;
        }

        if (availability.isUserResolvableError(error)) {
            Dialog errorDialog = availability.getErrorDialog(context, error,
                                                             GPS_FAIL_DIALOG_REQUEST_CODE);
            errorDialog.show();
            return false;
        }

        return false;
    }
}
