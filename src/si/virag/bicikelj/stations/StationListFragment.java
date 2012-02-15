package si.virag.bicikelj.stations;

import java.util.ArrayList;

import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class StationListFragment extends ListFragment implements LoaderCallbacks<StationInfo>
{
	private StationListAdapter adapter = null;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		adapter = new StationListAdapter(getActivity(), R.layout.stationlist_item, new ArrayList<Station>());
		this.setListAdapter(adapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onStart() 
	{
		super.onStart();
		
		// Show loading progress in the Action bar
		getActivity().setSupportProgress(Window.PROGRESS_END);
		getActivity().setSupportProgressBarIndeterminateVisibility(true);
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
		// TODO Error checking
		adapter.clear();
		adapter.addAll(data.getStations());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<StationInfo> loader) 
	{
		// TODO Auto-generated method stub
		
	}
}
