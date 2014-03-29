package si.virag.bicikelj;

import android.support.v7.app.ActionBarActivity;
import si.virag.bicikelj.station_map.StationMapFragment;
import si.virag.bicikelj.stations.StationListFragment;
import android.os.Bundle;

public class MainActivity extends ActionBarActivity
{
    private boolean isTablet = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        // Fragments use actionbar to show loading status
        setContentView(R.layout.main);

        isTablet = (findViewById(R.id.map_container) != null);
        if (isTablet)
        {
            if (savedInstanceState != null)
                return;

            StationMapFragment fragment = new StationMapFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.map_container, fragment, "MapFragment").commit();
        }
    }

	@Override
	protected void onStart() 
	{
		super.onStart();
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
	}

	@Override
	public boolean onSearchRequested() 
	{
		StationListFragment slFragment = (StationListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_station_list);
		slFragment.searchRequested();
		return super.onSearchRequested();
	}
    
    public boolean isTabletLayout()
    {
        return isTablet;
    }
}