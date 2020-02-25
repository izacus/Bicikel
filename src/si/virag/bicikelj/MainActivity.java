package si.virag.bicikelj;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import de.greenrobot.event.EventBus;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import si.virag.bicikelj.events.LocationUpdatedEvent;
import si.virag.bicikelj.station_map.StationMapFragment;
import si.virag.bicikelj.stations.StationListFragment;
import si.virag.bicikelj.util.GPSUtil;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String LOG_TAG = "Bicikelj.MainActivity";

    private static final int REQUEST_CODE_PERMISSIONS = 123321;

    private boolean isTablet = false;
    private FirebaseAnalytics firebaseAnalytics;

    @Nullable
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("CheckResult")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        super.onCreate(savedInstanceState);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.logo_padded);
        }

        // Fragments use actionbar to show loading status
        setContentView(R.layout.main);
        Bitmap icon = getBitmapFromVectorDrawable(this, R.drawable.logo);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), icon,
                                                               getColor(R.color.primary)));

        isTablet = (findViewById(R.id.map_container) != null);
        if (isTablet) {
            if (!GPSUtil.checkPlayServices(this)) {
                return;
            }

            setupMapFragment();
        }

        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(this, REQUEST_CODE_PERMISSIONS,
                                              Manifest.permission.ACCESS_FINE_LOCATION,
                                              Manifest.permission.ACCESS_COARSE_LOCATION).setRationale(
                        "Dostop do lokacije potreben za prikaz vaše lokacije na zemljevidu.")
                        .build());
    }

    @Override
    public boolean onSearchRequested() {
        StationListFragment slFragment = (StationListFragment) getSupportFragmentManager().findFragmentById(
                R.id.fragment_station_list);
        if (slFragment != null) {
            slFragment.searchRequested();
        }
        return super.onSearchRequested();
    }

    private void setupMapFragment() {
        if (getSupportFragmentManager().findFragmentByTag("MapFragment") != null) {
            return;
        }

        StationMapFragment fragment = new StationMapFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.map_container, fragment, "MapFragment")
                .commit();
    }

    public boolean isTabletLayout() {
        return isTablet;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Decide what to do based on the original request code
        if (requestCode == GPSUtil.GPS_FAIL_DIALOG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setupMapFragment();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void updateLocation() {
        if (fusedLocationClient == null) {
            return;
        }

        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER);
        try {
            fusedLocationClient.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    EventBus.getDefault()
                            .postSticky(new LocationUpdatedEvent(locationResult.getLastLocation()));
                }
            }, getMainLooper());
        } catch (SecurityException e) {
            Log.w(LOG_TAG, "No permission for location, skipping location updates.");
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                                            drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                    MainActivity.this);
            updateLocation();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Nothing TBD, lack of permissions is fine.
    }
}