package si.virag.bicikelj;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.tbruyelle.rxpermissions2.RxPermissions;

import de.greenrobot.event.EventBus;
import si.virag.bicikelj.events.LocationUpdatedEvent;
import si.virag.bicikelj.station_map.StationMapFragment;
import si.virag.bicikelj.stations.StationListFragment;
import si.virag.bicikelj.util.GPSUtil;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "Bicikelj.MainActivity";

    private boolean isTablet = false;

    public SystemBarTintManager tintManager;

    @Nullable
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("CheckResult")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.logo_padded);
        }

        // Fragments use actionbar to show loading status
        setContentView(R.layout.main);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), icon, getColor(R.color.primary)));

        tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.primary);

        isTablet = (findViewById(R.id.map_container) != null);
        if (isTablet) {
            if (!GPSUtil.checkPlayServices(this))
                return;

            setupMapFragment();
        }

        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(granted -> {
                    if (!granted) return;
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                    updateLocation();
                });
    }

    @Override
    public boolean onSearchRequested() {
        StationListFragment slFragment = (StationListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_station_list);
        if (slFragment != null) {
            slFragment.searchRequested();
        }
        return super.onSearchRequested();
    }

    private void setupMapFragment() {
        if (getSupportFragmentManager().findFragmentByTag("MapFragment") != null)
            return;

        StationMapFragment fragment = new StationMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.map_container, fragment, "MapFragment").commit();
    }

    public boolean isTabletLayout() {
        return isTablet;
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
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

    private void updateLocation() {
        if (fusedLocationClient == null) {
            return;
        }

        LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
        try {
            fusedLocationClient.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    EventBus.getDefault().postSticky(new LocationUpdatedEvent(locationResult.getLastLocation()));
                }
            }, getMainLooper());
        } catch (SecurityException e) {
            Log.w(LOG_TAG, "No permission for location, skipping location updates.");
        }
    }
}