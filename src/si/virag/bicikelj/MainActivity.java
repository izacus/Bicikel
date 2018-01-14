package si.virag.bicikelj;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.tbruyelle.rxpermissions.RxPermissions;

import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;
import rx.Subscriber;
import si.virag.bicikelj.events.LocationUpdatedEvent;
import si.virag.bicikelj.station_map.StationMapFragment;
import si.virag.bicikelj.stations.StationListFragment;
import si.virag.bicikelj.util.GPSUtil;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String LOG_TAG = "Bicikelj.MainActivity";

    private boolean isTablet = false;

    @Nullable
    private GoogleApiClient apiClient;
    public SystemBarTintManager tintManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.logo_padded);
        }

        // Fragments use actionbar to show loading status
        setContentView(R.layout.main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), icon, getResources().getColor(R.color.primary)));
        }

        tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.primary);

        isTablet = (findViewById(R.id.map_container) != null);
        if (isTablet) {
            if (!GPSUtil.checkPlayServices(this))
                return;

            setupMapFragment();
        }
    }

    @Override
    public boolean onSearchRequested() {
        StationListFragment slFragment = (StationListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_station_list);
        slFragment.searchRequested();
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
        switch (requestCode) {
            case GPSUtil.GPS_FAIL_DIALOG_REQUEST_CODE:

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        setupMapFragment();
                        break;
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean granted) {
                        if (!granted) return;
                        apiClient = new GoogleApiClient.Builder(MainActivity.this)
                                .addApi(LocationServices.API)
                                .addConnectionCallbacks(MainActivity.this)
                                .addOnConnectionFailedListener(MainActivity.this)
                                .build();
                        apiClient.connect();
                    }
                });

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (apiClient != null) apiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle info) {
        LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);
        } catch (SecurityException | IllegalStateException e) {
            Log.w(LOG_TAG, "No permission for location, skipping location updates.");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Location unavailable.");
    }

    @Override
    public void onLocationChanged(Location location) {
        EventBus.getDefault().postSticky(new LocationUpdatedEvent(location));
    }
}