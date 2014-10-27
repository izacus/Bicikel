package si.virag.bicikelj.station_map;

import android.content.ActivityNotFoundException;
import android.content.Intent;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import de.greenrobot.event.EventBus;
import si.virag.bicikelj.MainActivity;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.events.FocusOnStationEvent;
import si.virag.bicikelj.events.StationDataUpdatedEvent;
import si.virag.bicikelj.util.DisplayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StationMapFragment extends SupportMapFragment implements GooglePlayServicesClient.ConnectionCallbacks, GoogleMap.OnInfoWindowClickListener
{
    private static final double MAP_CENTER_LAT = 46.051367;
    private static final double MAP_CENTER_LNG = 14.506542;

    private GoogleMap map;
    private LocationClient locationClient;

    private ArrayList<Station> stations;
    private Map<Marker, Station> markerMap;


    private int focusStationId = -1;


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("focusOnStation"))
        {
            this.focusStationId = getArguments().getInt("focusOnStation");
        }
        else if (savedInstanceState != null && savedInstanceState.containsKey("focusOnStationId"))
            this.focusStationId = savedInstanceState.getInt("focusOnStationId");

        this.stations = new ArrayList<Station>();
        locationClient = new LocationClient(getActivity(), this, null);
        this.markerMap = new HashMap<Marker, Station>();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDirections(Station selectedStation)
    {
        Uri mapsUri;

        Location myLocation = locationClient.getLastLocation();
        if (myLocation == null)
        {
            mapsUri = Uri.parse("http://maps.google.com/maps?dirflg=w&daddr=" + selectedStation.getLocation().getLatitude() + "," + selectedStation.getLocation().getLongitude());
        }
        else
        {
            mapsUri = Uri.parse("http://maps.google.com/maps?dirflg=w&saddr=" + myLocation.getLatitude() + ", " +
                    myLocation.getLongitude() + "&daddr=" + selectedStation.getLocation().getLatitude() + "," +
                    selectedStation.getLocation().getLongitude());
        }

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, mapsUri);
        // This prevents app selection pop-up
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        try
        {
            startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            Log.e("Bicikelj", "Google maps is not installed!");
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (map == null)
        {
            map = getMap();
        }

        EventBus.getDefault().registerSticky(this);
    }

    private void setupMap()
    {
        map.clear();
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.setMyLocationEnabled(true);
        map.setInfoWindowAdapter(new InformationAdapter());
        map.setOnInfoWindowClickListener(this);

        CameraUpdate update;

        if (focusStationId > 0)
        {
            Station station = null;
            for (Station s : stations)
            {
                if (s.getId() == focusStationId)
                {
                    station = s;
                    break;
                }
            }

            update = CameraUpdateFactory.newLatLngZoom(new LatLng(station.getLocation().getLatitude(), station.getLocation().getLongitude()), 16.0f);
        }
        else
        {
            update = CameraUpdateFactory.newLatLngZoom(new LatLng(MAP_CENTER_LAT, MAP_CENTER_LNG), 14.0f);

            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
            {
                @Override
                public void onMyLocationChange(Location location)
                {
                    map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                    map.setOnMyLocationChangeListener(null);
                }
            });

        }

        map.moveCamera(update);
        getActivity().supportInvalidateOptionsMenu();

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                createMarkers();
            }
        }, 300);
    }

    private void createMarkers()
    {
        if (stations == null)
            return;

        for (Station station : stations)
        {
            BitmapDescriptor marker = BitmapDescriptorFactory.defaultMarker(((float) station.getAvailableBikes() / (float) station.getTotalSpaces()) * 120.0f);
            Marker m = map.addMarker(new MarkerOptions()
                    .position(new LatLng(station.getLocation().getLatitude(), station.getLocation().getLongitude()))
                    .flat(true)
                    .icon(marker));

            markerMap.put(m, station);

            if (station.getId() == focusStationId)
                m.showInfoWindow();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        locationClient.connect();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        locationClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        Location loc = locationClient.getLastLocation();
        if (loc == null || stations == null)
            return;

        for (Station s : stations)
            s.setDistance(loc);
    }

    @Override
    public void onDisconnected()
    {

    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        Station s = markerMap.get(marker);
        if (s == null)
            return;

        showDirections(s);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("focusOnStationId", focusStationId);
    }

    public void onEventMainThread(StationDataUpdatedEvent data)
    {
        this.stations = data.getStations();
        setupMap();
    }

    public void onEventMainThread(FocusOnStationEvent focusData)
    {
        Marker targetMarker = null;
        Station targetStation = null;
        for (Map.Entry<Marker, Station> s : markerMap.entrySet())
        {
            if (s.getValue().getId() == focusData.getId())
            {
                targetStation = s.getValue();
                targetMarker = s.getKey();
                break;
            }
        }

        if (targetStation == null || targetMarker == null)
            return;

        targetMarker.showInfoWindow();
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(targetStation.getLocation().getLatitude(), targetStation.getLocation().getLongitude()), 16.0f);
        map.animateCamera(update);
    }

    private final class InformationAdapter implements GoogleMap.InfoWindowAdapter
    {

        @Override
        public View getInfoWindow(Marker marker)
        {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker)
        {
            TextView tv = new TextView(getActivity());
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            Station s = markerMap.get(marker);
            if (s == null)
                return tv;

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(s.getName());
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append("\n");

            int startOfNumberHint = ssb.length();

            // Bikes hint text
            ssb.append(getString(R.string.map_hint_bikes));
            ssb.append(" ");
            String bikesStr = String.valueOf(s.getAvailableBikes());
            ssb.append(bikesStr);
            ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary)), ssb.length() - bikesStr.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), ssb.length() - bikesStr.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Free hint text
            ssb.append(" ");
            ssb.append(getString(R.string.map_hint_free));
            ssb.append(" ");
            String freeStr = String.valueOf(s.getFreeSpaces());
            ssb.append(freeStr);
            ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primary_dark)), ssb.length() - freeStr.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), ssb.length() - freeStr.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            ssb.setSpan(new AbsoluteSizeSpan((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14.0f, getResources().getDisplayMetrics())),
                    startOfNumberHint,
                    ssb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (s.getDistance() != null)
            {
                ssb.append("\n");
                String distanceString = DisplayUtils.formatDistance(s.getDistance());
                ssb.append(distanceString);
                ssb.setSpan(new AbsoluteSizeSpan((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11.0f, getResources().getDisplayMetrics())),
                        ssb.length() - distanceString.length(),
                        ssb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new ForegroundColorSpan(Color.GRAY), ssb.length() - distanceString.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), ssb.length() - distanceString.length(), ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }


            tv.setText(ssb);
            return tv;
        }
    }
}
