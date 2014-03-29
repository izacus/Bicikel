package si.virag.bicikelj.station_map;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.*;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import si.virag.bicikelj.MainActivity;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
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


    private MenuItem directionsItem = null;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_maps, menu);

        directionsItem = menu.findItem(R.id.menu_directions);
        if (stations.size() > 1)
        {
            directionsItem.setVisible(false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        this.stations = getArguments().getParcelableArrayList("stations");
        locationClient = new LocationClient(getActivity(), this, null);
        this.markerMap = new HashMap<Marker, Station>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return super.onCreateView(inflater, container, savedInstanceState);
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
            case R.id.menu_directions:
                showDirections(stations.get(0));
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
        startActivity(intent);
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (map == null)
        {
            map = getMap();
            if (map != null)
            {
                setupMap();
            }
        }
    }



    private void setupMap()
    {
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);

        CameraUpdate update;
        if (stations.size() == 1)
        {
            Station station = stations.get(0);
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

        map.setMyLocationEnabled(true);
        map.moveCamera(update);
        createMarkers();

        map.setInfoWindowAdapter(new InformationAdapter());
        map.setOnInfoWindowClickListener(this);

        if (stations.size() == 1)
        {
            markerMap.keySet().iterator().next().showInfoWindow();
        }

    }

    private void createMarkers()
    {
        for (Station station : stations)
        {
            BitmapDescriptor marker = BitmapDescriptorFactory.defaultMarker(((float) station.getAvailableBikes() / (float) station.getTotalSpaces()) * 120.0f);
            Marker m = map.addMarker(new MarkerOptions()
                    .position(new LatLng(station.getLocation().getLatitude(), station.getLocation().getLongitude()))
                    .flat(true)
                    .icon(marker));

            markerMap.put(m, station);
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
        if (loc == null)
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

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
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
            ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.station_red)), ssb.length() - bikesStr.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), ssb.length() - bikesStr.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Free hint text
            ssb.append(" ");
            ssb.append(getString(R.string.map_hint_free));
            ssb.append(" ");
            String freeStr = String.valueOf(s.getFreeSpaces());
            ssb.append(freeStr);
            ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.station_green)), ssb.length() - freeStr.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), ssb.length() - freeStr.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            ssb.setSpan(new AbsoluteSizeSpan((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0f, getResources().getDisplayMetrics())),
                    startOfNumberHint,
                    ssb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (s.getDistance() != null)
            {
                ssb.append("\n");
                String distanceString = DisplayUtils.formatDistance(s.getDistance());
                ssb.append(distanceString);
                ssb.setSpan(new AbsoluteSizeSpan((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10.0f, getResources().getDisplayMetrics())),
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