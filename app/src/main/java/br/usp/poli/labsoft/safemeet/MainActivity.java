package br.usp.poli.labsoft.safemeet;

import android.Manifest;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private MapView mapView;
    private GoogleMap gmap;
    Button btnLoc;
    private ListView listView;

    private GpsTracker gt;
    private LatLng pos;
    private List<LocationGPS> locList;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        btnLoc = (Button) findViewById(R.id.btnLoc);
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gt = new GpsTracker(getApplicationContext());
                Location l = gt.getLocation();
                if( l == null){
                    Toast.makeText(getApplicationContext(),"Falha ao obter a localização", Toast.LENGTH_SHORT).show();
                }else {
                    atualizaMapa(l.getLatitude(), l.getLongitude());
                }
            }
        });

        locList = new ArrayList<LocationGPS>();
        carregarListaLoc();

        ArrayAdapter<LocationGPS> locAdapter = new ArrayAdapter<LocationGPS>(
                this, android.R.layout.simple_list_item_1, locList);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(locAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationGPS loc = locList.get(i);
                atualizaMapa(loc.getLatitude(), loc.getLongitude(), 17);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        //gmap.setMinZoomPreference(12);
        gmap.setIndoorEnabled(true);

        UiSettings uiSettings = gmap.getUiSettings();
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        //pos = new LatLng(-34, 151);
        //gmap.addMarker(new MarkerOptions().position(pos).title("Marker in Sydney"));
        //gmap.moveCamera(CameraUpdateFactory.newLatLng(pos));

        mapView.onResume();
    }

    private void atualizaMapa(double latitude, double longitude){
        Toast.makeText(getApplicationContext(),"Latitude: "+latitude
                +"\nLongitude: "+longitude,Toast.LENGTH_SHORT).show();

        pos = new LatLng(latitude, longitude);
        gmap.clear();
        gmap.addMarker(new MarkerOptions().position(pos));
        gmap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        gmap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void atualizaMapa(double latitude, double longitude, int zoomLevel){
        atualizaMapa(latitude,longitude);
        gmap.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel));
    }

    private void carregarListaLoc(){
        if(locList.isEmpty()) {
            locList.add(new LocationGPS(-23.556537f, -46.731204f));
            locList.add(new LocationGPS(-23.556594f, -46.729465f));
            locList.add(new LocationGPS(-23.557741f, -46.728489f));
        }
    }
}
