package si.virag.bicikelj.stations;

import java.util.ArrayList;

import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class StationListFragment extends ListFragment implements LoaderCallbacks<StationInfo>
{
	private static final int STATION_LOADER_ID = 0;
	
	private StationListAdapter adapter = null;
	
	private Animation fadeOut;
	private Animation fadeIn;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		
		fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
		fadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
		
		adapter = new StationListAdapter(getActivity(), R.layout.stationlist_item, new ArrayList<Station>());
		this.setListAdapter(adapter);
		getLoaderManager().initLoader(STATION_LOADER_ID, null, this);
	}

	@Override
	public void onStart() 
	{
		super.onStart();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, 
							 ViewGroup container,
							 Bundle savedInstanceState) 
	{
		return inflater.inflate(R.layout.stationlist_fragment, container);
	}

	@Override
	public Loader<StationInfo> onCreateLoader(int id, Bundle args) 
	{
		return new JSONInformationDataLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<StationInfo> loader, StationInfo data) 
	{
		// Clear animation listener
		fadeOut.setAnimationListener(null);
		getListView().startAnimation(fadeIn);
		
		// Update data in-place when already available
		if (adapter.getCount() > 0)
		{
			adapter.updateData(data);
		}
		else
		{
			for (Station station : data.getStations())
			{
				adapter.add(station);
			}
		}
		
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<StationInfo> loader) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		inflater.inflate(R.menu.menu_stationlist, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
			case R.id.menu_refresh: 
				getLoaderManager().initLoader(STATION_LOADER_ID, null, StationListFragment.this).forceLoad();
				getListView().startAnimation(fadeOut);
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
