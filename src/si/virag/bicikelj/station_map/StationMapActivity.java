package si.virag.bicikelj.station_map;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import si.virag.bicikelj.R;

public class StationMapActivity extends AppCompatActivity {

    private SystemBarTintManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        manager = new SystemBarTintManager(this);
        manager.setStatusBarTintEnabled(true);
        manager.setStatusBarTintResource(R.color.primary);

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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_out_right);
    }

    public SystemBarTintManager getTintManager() {
        return manager;
    }
}
