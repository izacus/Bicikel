package si.virag.bicikelj.stations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import si.virag.bicikelj.MainActivity;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.events.FocusOnStationEvent;
import si.virag.bicikelj.events.ListItemSelectedEvent;
import si.virag.bicikelj.events.LocationUpdatedEvent;
import si.virag.bicikelj.events.StationDataUpdatedEvent;
import si.virag.bicikelj.station_map.StationMapActivity;
import si.virag.bicikelj.stations.api.CityBikesApi;
import si.virag.bicikelj.stations.api.CityBikesApiClient;
import si.virag.bicikelj.util.DividerItemDecoration;
import si.virag.bicikelj.util.FavoritesManager;
import si.virag.bicikelj.util.GPSUtil;
import si.virag.bicikelj.util.ShowKeyboardRunnable;

public class StationListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;

    private StationListAdapter adapter = null;
    private MenuItem searchActionView;
    private StationInfo data;

    private boolean isTablet;
    private Location location;

    private View emptyView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FavoritesManager fm = new FavoritesManager(getActivity());
        adapter = new StationListAdapter(getActivity(), fm, new ArrayList<>(), null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) {
            return;
        }
        isTablet = ((MainActivity) getActivity()).isTabletLayout();
        refresh();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.stationlist_fragment, container);
        RecyclerView listView = v.findViewById(R.id.stationlist_list);
        listView.setAdapter(adapter);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.addItemDecoration(
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        swipeRefreshLayout = v.findViewById(R.id.stationlist_swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary_dark,
                                                   R.color.secondary, R.color.primary_dark);


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Context context = getContext();
            if (context != null) {
                final TypedArray styledAttributes = context.getTheme()
                        .obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
                int actionBarSize = (int) styledAttributes.getDimension(0, 0);
                styledAttributes.recycle();

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) swipeRefreshLayout.getLayoutParams();
                params.topMargin = actionBarSize;
                swipeRefreshLayout.setLayoutParams(params);
            }
        }

        emptyView = v.findViewById(R.id.stationlist_emptyview);
        emptyView.setOnClickListener(v1 -> refresh());

        swipeRefreshLayout.setRefreshing(true);
        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stationlist, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        this.searchActionView = searchItem;

        final EditText searchBox = searchItem.getActionView().findViewById(R.id.search_box);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                if (StationListFragment.this.data == null) {
                    return false;
                }

                searchBox.post(new ShowKeyboardRunnable(getActivity(), searchBox));
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                adapter.updateData(data);
                if (location != null) {
                    adapter.updateLocation(location);
                }

                Activity activity = getActivity();
                if (activity != null) {
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                    }
                }

                return true;
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterStations(s.toString());
            }
        });
    }

    public void searchRequested() {
        if (this.searchActionView != null) {
            if (searchActionView.isActionViewExpanded()) {
                searchActionView.collapseActionView();
            } else {
                searchActionView.expandActionView();
            }
        }
    }

    private void filterStations(String text) {
        if (text.trim().length() > 0) {
            StationInfo filteredInfo = data.getFilteredInfo(text);

            if (filteredInfo.getStations().size() > 0) {
                adapter.updateData(filteredInfo);
            } else {
                adapter.updateData(data);
            }

        } else {
            adapter.updateData(data);
        }

        if (location != null) {
            adapter.updateLocation(location);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.data == null) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_map:
                showFullMap();
                break;
            default:
                return false;
        }

        return true;
    }

    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        this.data = null;
        CityBikesApi api = CityBikesApiClient.getBicikeljApi();
        api.getStationData().enqueue(new Callback<StationInfo>() {
            @Override
            public void onResponse(Call<StationInfo> call, Response<StationInfo> response) {
                if (!response.isSuccessful()) {
                    showRequestFailed(response.message());
                    return;
                }

                swipeRefreshLayout.setRefreshing(false);
                StationListFragment.this.data = response.body();
                // Update data in-place when already available
                adapter.updateData(data);

                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.findViewById(R.id.stationlist_emptyview).setVisibility(View.INVISIBLE);
                }

                if (location != null) {
                    adapter.updateLocation(location);
                }

                if (data == null) {
                    showError();
                    return;
                }

                EventBus.getDefault().postSticky(new StationDataUpdatedEvent(data.getStations()));
                swipeRefreshLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(Call<StationInfo> call, Throwable t) {
                showRequestFailed(t.getMessage());
            }
        });
    }

    private void showRequestFailed(String error) {
        swipeRefreshLayout.setRefreshing(false);
        Log.e("Bicikelj", "Load failed: " + error);
        showError();
    }

    private void showError() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        swipeRefreshLayout.setVisibility(View.INVISIBLE);
        emptyView.setVisibility(View.VISIBLE);
        TextView text = activity.findViewById(R.id.stationlist_loading_error);
        text.setVisibility(View.VISIBLE);
        TextView loadingText = activity.findViewById(R.id.stationlist_loading_text);
        loadingText.setVisibility(View.INVISIBLE);
        ProgressBar progress = activity.findViewById(R.id.stationlist_loading_progress);
        progress.setVisibility(View.INVISIBLE);
        text.setText(R.string.stationlist_load_error);
    }

    private void showFullMap() {
        if (!GPSUtil.checkPlayServices(getActivity())) {
            return;
        }

        if (this.data == null) {
            return;
        }

        Intent intent = new Intent(getActivity(), StationMapActivity.class);
        startActivity(intent);

        Activity activity = getActivity();
        if (activity != null) {
            activity.overridePendingTransition(R.anim.slide_in_right, 0);
        }
    }


    @Override
    public void onRefresh() {
        refresh();
    }

    public void onEventMainThread(ListItemSelectedEvent e) {
        if (!GPSUtil.checkPlayServices(getActivity())) {
            return;
        }

        if (isTablet) {
            EventBus.getDefault().post(new FocusOnStationEvent(e.stationId));
        } else {
            Intent intent = new Intent(getActivity(), StationMapActivity.class);
            intent.putExtra("focusOnStation", e.stationId);
            startActivity(intent);

            Activity activity = getActivity();
            if (activity != null) {
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }
    }

    public void onEventMainThread(LocationUpdatedEvent event) {
        this.location = event.location;
        if (adapter != null) {
            adapter.updateLocation(location);
        }
    }
}
