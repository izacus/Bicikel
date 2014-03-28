package si.virag.bicikelj;

import android.support.v7.app.ActionBarActivity;
import si.virag.bicikelj.stations.StationListFragment;
import android.os.Bundle;

public class MainActivity extends ActionBarActivity
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
    
    
}