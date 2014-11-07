package si.virag.bicikelj;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import si.virag.bicikelj.station_map.StationMapFragment;
import si.virag.bicikelj.stations.StationListFragment;
import si.virag.bicikelj.util.GPSUtil;

public class MainActivity extends ActionBarActivity
{
    private boolean isTablet = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo);

        // Fragments use actionbar to show loading status
        setContentView(R.layout.main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), icon, getResources().getColor(R.color.primary)));
        }

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
}