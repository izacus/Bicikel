package si.virag.bicikelj.station_map;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import si.virag.bicikelj.MainActivity;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.events.FocusOnStationEvent;
import si.virag.bicikelj.events.LocationUpdatedEvent;
import si.virag.bicikelj.events.StationDataUpdatedEvent;
import si.virag.bicikelj.util.DisplayUtils;

public class StationMapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {
    private static final double MAP_CENTER_LAT = 46.051367;
    private static final double MAP_CENTER_LNG = 14.506542;

    @Nullable
    private GoogleMap map;

    @Nullable
    private Location location;

    private List<Station> stations;
    private Map<Marker, Station> markerMap;

    private int focusStationId = -1;

    @NonNull
    private MapView mapView;

    @Nullable
    private LocationCallback locationUpdateCallback;

    private int rightInset = 0;
    private int bottomInset = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("focusOnStation")) {
            this.focusStationId = getArguments().getInt("focusOnStation");
        } else if (savedInstanceState != null && savedInstanceState.containsKey(
                "focusOnStationId")) {
            this.focusStationId = savedInstanceState.getInt("focusOnStationId");
        }

        this.stations = new ArrayList<>();
        this.markerMap = new HashMap<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);
        view.setOnApplyWindowInsetsListener(this::applyWindowInsetsListener);
        mapView = view.findViewById(R.id.map_map);
        mapView.onCreate(savedInstanceState);
        return view;
    }

    private WindowInsets applyWindowInsetsListener(View view, WindowInsets windowInsets) {
        rightInset = windowInsets.getSystemWindowInsetRight();
        bottomInset = windowInsets.getSystemWindowInsetBottom();
        if (map != null) {
            map.setPadding(0, 0, rightInset, bottomInset);
        }
        return windowInsets;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                Activity activity = getActivity();
                if (activity != null) {
                    activity.overridePendingTransition(R.anim.slide_in_left,
                                                       R.anim.slide_out_right);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDirections(Station selectedStation) {
        Uri mapsUri;

        if (location == null) {
            mapsUri = Uri.parse(
                    "http://maps.google.com/maps?dirflg=w&daddr=" + selectedStation.getLocation()
                            .getLatitude() + "," + selectedStation.getLocation().getLongitude());
        } else {
            mapsUri = Uri.parse(
                    "http://maps.google.com/maps?dirflg=w&saddr=" + location.getLatitude() + ", " + location
                            .getLongitude() + "&daddr=" + selectedStation.getLocation()
                            .getLatitude() + "," + selectedStation.getLocation().getLongitude());
        }

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, mapsUri);
        // This prevents app selection pop-up
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("Bicikelj", "Google maps is not installed!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        mapView.getMapAsync(googleMap -> {
            map = googleMap;
            setupMap();
        });

        EventBus.getDefault().registerSticky(this);
    }

    private void setupMap() {
        if (map == null || getContext() == null) {
            return;
        }
        MapsInitializer.initialize(getContext());
        map.setPadding(0, 0, rightInset, bottomInset);
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_dark));
        }

        map.clear();
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);

        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            // Nothing TBD
        }

        map.setInfoWindowAdapter(new InformationAdapter());
        map.setOnInfoWindowClickListener(this);

        CameraUpdate update;

        if (focusStationId > 0) {
            Station station = null;
            for (Station s : stations) {
                if (s.getId() == focusStationId) {
                    station = s;
                    break;
                }
            }

            if (station == null || station.getLocation() == null) {
                update = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(MAP_CENTER_LAT, MAP_CENTER_LNG), 14.0f);
            } else {
                update = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(station.getLocation().getLatitude(),
                                   station.getLocation().getLongitude()), 16.0f);
            }
        } else {
            update = CameraUpdateFactory.newLatLngZoom(new LatLng(MAP_CENTER_LAT, MAP_CENTER_LNG),
                                                       14.0f);

            Context context = getContext();
            if (context != null && ContextCompat.checkSelfPermission(context,
                                                                     Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(
                        getContext());
                LocationRequest request = LocationRequest.create()
                        .setNumUpdates(1)
                        .setPriority(LocationRequest.PRIORITY_LOW_POWER);

                locationUpdateCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        if (location == null) {
                            return;
                        }
                        map.animateCamera(CameraUpdateFactory.newLatLng(
                                new LatLng(location.getLatitude(), location.getLongitude())));
                    }
                };

                client.requestLocationUpdates(request, locationUpdateCallback,
                                              getContext().getMainLooper());
            }
        }

        map.moveCamera(update);
        getActivity().invalidateOptionsMenu();
        new Handler().postDelayed(this::createMarkers, 300);
        mapView.postDelayed(() -> mapView.setVisibility(View.VISIBLE), 150);
    }

    private void createMarkers() {
        if (stations == null) {
            return;
        }

        for (Station station : stations) {
            if (station.getLocation() == null || map == null) {
                continue;
            }

            BitmapDescriptor marker = BitmapDescriptorFactory.defaultMarker(
                    ((float) station.getAvailableBikes() / (float) station.getTotalSpaces()) * 120.0f);
            Marker m = map.addMarker(new MarkerOptions().position(
                    new LatLng(station.getLocation().getLatitude(),
                               station.getLocation().getLongitude())).flat(true).icon(marker));

            markerMap.put(m, station);

            if (station.getId() == focusStationId) {
                m.showInfoWindow();
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Station s = markerMap.get(marker);
        if (s == null) {
            return;
        }

        showDirections(s);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("focusOnStationId", focusStationId);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationUpdateCallback != null) {
            Context context = getContext();
            if (context != null) {
                FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(
                        context);
                client.removeLocationUpdates(locationUpdateCallback);
                locationUpdateCallback = null;
            }
        }
    }

    public void onEventMainThread(LocationUpdatedEvent data) {
        this.location = data.location;
        if (location == null || stations == null) {
            return;
        }
        for (Station s : stations) {
            s.setDistance(location);
        }
    }

    public void onEventMainThread(StationDataUpdatedEvent data) {
        this.stations = data.getStations();
        setupMap();
    }

    public void onEventMainThread(FocusOnStationEvent focusData) {
        Marker targetMarker = null;
        Station targetStation = null;
        for (Map.Entry<Marker, Station> s : markerMap.entrySet()) {
            if (s.getValue().getId() == focusData.getId()) {
                targetStation = s.getValue();
                targetMarker = s.getKey();
                break;
            }
        }

        if (targetStation == null || targetMarker == null) {
            return;
        }

        targetMarker.showInfoWindow();
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
                new LatLng(targetStation.getLocation().getLatitude(),
                           targetStation.getLocation().getLongitude()), 16.0f);
        if (map == null) {
            return;
        }
        map.animateCamera(update);
    }

    private final class InformationAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            marker.setInfoWindowAnchor(0.5f, -0.2f);

            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.map_info, mapView, false);
            TextView tv = view.findViewById(R.id.map_info_text);
            Station s = markerMap.get(marker);
            if (s == null) {
                return tv;
            }

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(s.getName());
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append("\n");

            int startOfNumberHint = ssb.length();

            // Bikes hint text
            ssb.append(getContext().getString(R.string.map_hint_bikes));
            ssb.append(" ");
            String bikesStr = String.valueOf(s.getAvailableBikes());
            ssb.append(bikesStr);
            ssb.setSpan(new ForegroundColorSpan(getContext().getColor(R.color.station_number_full)),
                        ssb.length() - bikesStr.length(), ssb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), ssb.length() - bikesStr.length(),
                        ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Free hint text
            ssb.append(" ");
            ssb.append(getContext().getString(R.string.map_hint_free));
            ssb.append(" ");
            String freeStr = String.valueOf(s.getFreeSpaces());
            ssb.append(freeStr);
            ssb.setSpan(new ForegroundColorSpan(getContext().getColor(R.color.station_number_free)),
                        ssb.length() - freeStr.length(), ssb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), ssb.length() - freeStr.length(), ssb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            ssb.setSpan(new AbsoluteSizeSpan(
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14.0f,
                                                                getContext().getResources()
                                                                        .getDisplayMetrics())),
                        startOfNumberHint, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (s.getDistance() != null) {
                ssb.append("\n");
                String distanceString = DisplayUtils.formatDistance(s.getDistance());
                ssb.append(distanceString);
                ssb.setSpan(new AbsoluteSizeSpan(
                                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11.0f,
                                                                    getContext().getResources()
                                                                            .getDisplayMetrics())),
                            ssb.length() - distanceString.length(), ssb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new ForegroundColorSpan(Color.GRAY),
                            ssb.length() - distanceString.length(), ssb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                            ssb.length() - distanceString.length(), ssb.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }


            tv.setText(ssb);
            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
