package si.virag.bicikelj;

import com.flurry.android.FlurryAgent;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MainActivity extends FragmentActivity 
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
    
    
}