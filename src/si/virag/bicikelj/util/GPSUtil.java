package si.virag.bicikelj.util;

import android.app.Activity;
import android.app.Dialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by jernej on 29/03/14.
 */
public class GPSUtil
{
    public static final int GPS_FAIL_DIALOG_REQUEST_CODE = 9584;

    public static boolean checkPlayServices(Activity context)
    {
        int error = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        if (error == ConnectionResult.SUCCESS)
            return true;

        if (GooglePlayServicesUtil.isUserRecoverableError(error))
        {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(error, context, GPS_FAIL_DIALOG_REQUEST_CODE);
            errorDialog.show();
            return false;
        }

        return false;
    }
}
