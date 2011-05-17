package si.virag.bicikel;

import si.virag.bicikel.data.StationInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<StationInfo>
{
	private static final int INFO_LOADER_ID = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        // Load data from server
        getSupportLoaderManager().initLoader(INFO_LOADER_ID, null, this);
    }

	@Override
	public Loader<StationInfo> onCreateLoader(int id, Bundle args)
	{
		InformationDataLoader infoLoader = new InformationDataLoader(this);
		return infoLoader;
	}

	@Override
	public void onLoadFinished(Loader<StationInfo> arg0, StationInfo arg1)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<StationInfo> arg0)
	{
		// TODO Auto-generated method stub
		
	}
    
    
}