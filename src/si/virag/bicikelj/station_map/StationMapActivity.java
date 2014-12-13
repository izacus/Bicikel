package si.virag.bicikelj.station_map;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;

import si.virag.bicikelj.R;

public class StationMapActivity extends ActionBarActivity
{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null)
            return;

        StationMapFragment fragment = new StationMapFragment();
        Bundle arguments = new Bundle();

        if (getIntent().hasExtra("focusOnStation"))
            arguments.putInt("focusOnStation", getIntent().getIntExtra("focusOnStation", 0));
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction().add(R.id.map_container, fragment, "MapFragment").commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_out_right);
    }
}
