package si.virag.bicikelj;

import si.virag.bicikelj.stations.StationListFragment;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.flurry.android.FlurryAgent;

public class MainActivity extends SherlockFragmentActivity 
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        // Fragments use actionbar to show loading status
        setContentView(R.layout.main);
    }

	@Override
	protected void onStart() 
	{
		super.onStart();
		FlurryAgent.setUseHttps(true);	// Don't send users data in plain text
		FlurryAgent.setReportLocation(false);	// Don't report users location for stats, not needed
		FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	public boolean onSearchRequested() 
	{
		StationListFragment slFragment = (StationListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_station_list);
		slFragment.searchRequested();
		return super.onSearchRequested();
	}
    
    
}