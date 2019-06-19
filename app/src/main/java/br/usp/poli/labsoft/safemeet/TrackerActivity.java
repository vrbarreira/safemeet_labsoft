package br.usp.poli.labsoft.safemeet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrackerActivity extends AppCompatActivity implements OnMapReadyCallback{

    private MapView mapView;
    private GoogleMap gmap;
    private Button btnLoc, btnRegLoc, btnClearReg;
    private Button btnTrkStart, btnTrkEnd;
    private EditText etAddress, etPort;
    private Button btnConnect;
    private ListView listView;
    private TextView tvTrkCount;
    private Switch switchLocTeste;

    private GpsTracker gt;
    private LatLng pos;
    private List<LocationGPS> locList;
    private ArrayAdapter<LocationGPS> locAdapter;

    private static final double latDefault = -23.5, lngDefault = -46;

    private Intent intentTimer;
    private int trkCount;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    class Message{
        public void displayMessage(int msgType, Bundle resultData){

            switch (msgType){
                case TimerLocation.MSG_END_SERV:
                    String message = resultData.getString("message");
                    Log.v("Timer",message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    break;
                case TimerLocation.MSG_REG_LOCAL:
                    if (gt == null)
                        gt = new GpsTracker(getApplicationContext());

                    Location l = gt.getLocation();

                    if(switchLocTeste.isChecked()){
                        locAdapter.add(new LocationGPS(new Date(), latDefault, lngDefault));
                        connectServer(etAddress.getText().toString(), etPort.getText().toString());
                    } else if (l != null){
                        locAdapter.add(new LocationGPS(new Date(), l.getLatitude(), l.getLongitude()));
                        connectServer(etAddress.getText().toString(), etPort.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(),"Falha ao obter a localização", Toast.LENGTH_SHORT).show();
                    }

                    trkCount++;
                    atualizaTvTrkCount();

                    break;
            }
        }
    }

    class ClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        LocationGPS msgToServer;

        ClientTask(String addr, int port, LocationGPS msgTo) {
            dstAddress = addr;
            dstPort = port;
            msgToServer = msgTo;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;
            //DataOutputStream dataOutputStream = null;
            ObjectOutputStream objOutputStream = null;
            DataInputStream dataInputStream = null;

            try {
                socket = new Socket(dstAddress, dstPort);
                //dataOutputStream = new DataOutputStream(socket.getOutputStream());
                objOutputStream = new ObjectOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                if(msgToServer != null){
                    //dataOutputStream.writeUTF(msgToServer);
                    objOutputStream.writeObject(msgToServer);
                }

                response = dataInputStream.readUTF();

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (objOutputStream != null) {
                    try {
                        objOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //textResponse.setText(response);
            //Toast.makeText(getApplicationContext(),response, Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(TrackerActivity.this);
            builder.setMessage(response);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });
            AlertDialog alerta = builder.create();
            alerta.show();
            super.onPostExecute(result);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getApplicationContext().getFilesDir().getPath());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });
        AlertDialog alerta = builder.create();
        alerta.show();

        trkCount = 0;
        tvTrkCount = (TextView) findViewById(R.id.tvTrkCount);
        atualizaTvTrkCount();

        MessageReceiver msgReceiver = new MessageReceiver(new Message());
        intentTimer = new Intent(getApplicationContext(), TimerLocation.class);
        intentTimer.putExtra("receiver", msgReceiver);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        btnLoc = (Button) findViewById(R.id.btnLoc);
        ActivityCompat.requestPermissions(TrackerActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gt == null)
                    gt = new GpsTracker(getApplicationContext());

                Location l = gt.getLocation();
                if( l == null){
                    Toast.makeText(getApplicationContext(),"Falha ao obter a localização", Toast.LENGTH_SHORT).show();
                }else {
                    atualizaMapa(new Date(), l.getLatitude(), l.getLongitude(), 15);
                }
            }
        });

        btnRegLoc = (Button) findViewById(R.id.btnRegLoc);
        btnRegLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gt == null)
                    gt = new GpsTracker(getApplicationContext());

                Location l = gt.getLocation();
                if( l == null){
                    Toast.makeText(getApplicationContext(),"Falha ao obter a localização", Toast.LENGTH_SHORT).show();
                }else {
                    locAdapter.add(new LocationGPS(new Date(), l.getLatitude(), l.getLongitude()));
                    atualizaMapa(new Date(), l.getLatitude(), l.getLongitude(), 15);
                    Toast.makeText(getApplicationContext(),"Adicionado à lista", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnClearReg = (Button) findViewById(R.id.btnClearReg);
        btnClearReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locList.clear();
                locAdapter.clear();
                gmap.clear();
                trkCount = 0;
                atualizaTvTrkCount();
                Toast.makeText(getApplicationContext(),"Lista de registros limpa", Toast.LENGTH_LONG).show();
            }
        });

        btnTrkStart = (Button) findViewById(R.id.btnTrkStart);
        btnTrkStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentTimer.putExtra("maxCount", 30);
                startService(intentTimer);
            }
        });

        btnTrkEnd = (Button) findViewById(R.id.btnTrkEnd);
        btnTrkEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentTimer.putExtra("stopServ", 1);
            }
        });

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectServer(etAddress.getText().toString(), etPort.getText().toString());
            }
        });

        etAddress = (EditText) findViewById(R.id.etAddr);
        etPort = (EditText) findViewById(R.id.etPort);

        switchLocTeste = (Switch) findViewById(R.id.switchLocTeste);

        locList = new ArrayList<LocationGPS>();
        //loadListaLoc();

        locAdapter = new ArrayAdapter<LocationGPS>(
                this, android.R.layout.simple_list_item_1, locList);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(locAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationGPS loc = locList.get(i);
                atualizaMapa(loc.getDatetime(), loc.getLatitude(), loc.getLongitude(), 17);
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

    private void atualizaMapa(Date datetime, double latitude, double longitude, int zoomLevel){
        Toast.makeText(getApplicationContext(),"Latitude: "+latitude
                +"\nLongitude: "+longitude,Toast.LENGTH_SHORT).show();

        MarkerOptions mko = new MarkerOptions();

        pos = new LatLng(latitude, longitude);
        mko.position(pos);

        if(datetime != null)
            mko.title("Registrado em: "
                    + DateFormat.getDateTimeInstance().format(datetime).toString());

        gmap.clear();
        gmap.addMarker(mko);
        gmap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        gmap.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel));
    }

    private void atualizaTvTrkCount(){
        tvTrkCount.setText("Count: " + trkCount);
    }

    private void connectServer(String addr, String port){
        double lat, lng;

        lat = latDefault;
        lng = lngDefault;

        String tMsg = "Msg teste";

        if (gt == null)
            gt = new GpsTracker(getApplicationContext());
        Location l = gt.getLocation();

        if (!switchLocTeste.isChecked() && l != null) {
            lat = l.getLatitude();
            lng = l.getLongitude();
        } else {
            lat = latDefault;
            lng = lngDefault;
        }

        if(tMsg.equals("")){
            tMsg = null;
            Toast.makeText(TrackerActivity.this, "No Welcome Msg sent", Toast.LENGTH_SHORT).show();
        } else if (addr.equals("") || port.equals("")) {
            Toast.makeText(TrackerActivity.this, "Fill addr and port data", Toast.LENGTH_SHORT).show();
            //} else if (l == null){
            //    Toast.makeText(getApplicationContext(),"Falha ao obter a localização", Toast.LENGTH_SHORT).show();
        } else {
            ClientTask myClientTask = new ClientTask(
                    addr,
                    Integer.parseInt(port),
                    new LocationGPS(new Date(), lat, lng)
                    //tMsg
            );
            myClientTask.execute();
        }
    }

    private void loadListaLoc(){

        if(locList.isEmpty()) {
            locList.add(new LocationGPS(-23.556537, -46.731204));
            locList.add(new LocationGPS(-23.556594, -46.729465));
            locList.add(new LocationGPS(-23.557741, -46.728489));
        }
        /**/
    }
}
