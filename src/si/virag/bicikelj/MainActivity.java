package si.virag.bicikelj;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import de.greenrobot.event.EventBus;
import si.virag.bicikelj.events.LocationUpdatedEvent;
import si.virag.bicikelj.station_map.StationMapFragment;
import si.virag.bicikelj.stations.StationListFragment;
import si.virag.bicikelj.util.GPSUtil;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String LOG_TAG = "Bicikelj.MainActivity";

    private boolean isTablet = false;
    private GoogleApiClient apiClient;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo_padded);

        // Fragments use actionbar to show loading status
        setContentView(R.layout.main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), icon, getResources().getColor(R.color.primary)));
        }

        apiClient = new GoogleApiClient.Builder(this)
                                       .addApi(LocationServices.API)
                                       .addConnectionCallbacks(this)
                                       .addOnConnectionFailedListener(this)
                                       .build();

        isTablet = (findViewById(R.id.map_container) != null);
        if (isTablet)
        {
            if (!GPSUtil.checkPlayServices(this))
                return;

            setupMapFragment();
        }
    }

	@Override
	public boolean onSearchRequested() 
	{
		StationListFragment slFragment = (StationListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_station_list);
		slFragment.searchRequested();
		return super.onSearchRequested();
	}

    private void setupMapFragment()
    {
        if (getSupportFragmentManager().findFragmentByTag("MapFragment") != null)
            return;

        StationMapFragment fragment = new StationMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.map_container, fragment, "MapFragment").commit();
    }

    public boolean isTabletLayout()
    {
        return isTablet;
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data)
    {
        // Decide what to do based on the original request code
        switch (requestCode)
        {
            case GPSUtil.GPS_FAIL_DIALOG_REQUEST_CODE :

                switch (resultCode)
                {
                    case Activity.RESULT_OK :
                        setupMapFragment();
                        break;
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        apiClient.disconnect();
    }


    @Override
    public void onConnected(Bundle info) {
        LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Location unavailable.");
    }

    @Override
    public void onLocationChanged(Location location) {
        EventBus.getDefault().postSticky(new LocationUpdatedEvent(location));
    }
}