package com.namseoul.sa.tab;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    EditText adress, rangeET;
    SeekBar rangeBar;
    int rangeint = 100;
    Double latitude, longitude;
    Geocoder geocoder;
    Button geobtn, addbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);

        adress = findViewById(R.id.geotext);
        rangeET = findViewById(R.id.seekEt);
        rangeBar = findViewById(R.id.rangeBar);
        geobtn = findViewById(R.id.geobtn);
        addbtn = findViewById(R.id.addend);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        rangeBar.setProgress(rangeint);
        rangeET.setText(Integer.toString(rangeint));

        geocoder = new Geocoder(this);

        LatLng SEOUL = new LatLng(37.56,126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        googleMap.addMarker(markerOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        geobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(adress.getText().length() < 0){

                }

                googleMap.clear();
                String str = adress.getText().toString();
                List<Address> addressList = null;
                try{
                    addressList = geocoder.getFromLocationName(str,10);
                }catch (IOException e){
                    e.printStackTrace();
                }

                String[]splitstr = addressList.get(0).toString().split(",");
                String addressstr = splitstr[0].substring(splitstr[0].indexOf("\"")+1,splitstr[0].length()-2);

                latitude = Double.parseDouble(splitstr[10].substring(splitstr[10].indexOf("=")+1));
                longitude = Double.parseDouble(splitstr[12].substring(splitstr[12].indexOf("=")+1));

                MarkerSet(googleMap,latitude,longitude,rangeint,addressstr);

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15));

            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                MarkerSet(googleMap,latLng.latitude,latLng.longitude,rangeint);
            }
        });

        rangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(latitude == null){
                    rangeET.setText(Integer.toString(i));
                    rangeint = 1;
                }else{
                    rangeint = i;
                    rangeET.setText(Integer.toString(i));
                    MarkerSet(googleMap,latitude,longitude,rangeint);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder ad = new AlertDialog.Builder(MapActivity.this);
                ad.setTitle("안전지대 이름설정")
                        .setMessage("안전지대의 이름을 설정해주세요");

                final EditText et = new EditText(MapActivity.this);
                ad.setView(et);

                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent = new Intent(MapActivity.this,SafeZoneStart.class);
                        intent.putExtra("latitude",latitude)
                                .putExtra("longitude",longitude)
                                .putExtra("range",rangeint)
                                .putExtra("name",et.getText().toString());
                        startActivity(intent);
                    }
                });
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                ad.show();
            }
        });
    }

    public void MarkerSet(final GoogleMap Map, double latitude, double longitude, int range){

        LatLng point = new LatLng(latitude,longitude);
        MarkerOptions marker = new MarkerOptions();
        CircleOptions circle = new CircleOptions();

        Map.clear();

        marker.title("선택 좌표");
        marker.snippet(Double.toString(latitude) + ", " + Double.toString(longitude));
        marker.position(point);

        circle.center(point);
        circle.radius(range);

        Map.addMarker(marker);
        Map.addCircle(circle);
    }

    public void MarkerSet(final GoogleMap Map, double latitude, double longitude, int range, String name){

        LatLng point = new LatLng(latitude,longitude);
        MarkerOptions marker = new MarkerOptions();
        CircleOptions circle = new CircleOptions();

        Map.clear();

        marker.title(name);
        marker.snippet(Double.toString(latitude) + ", " + Double.toString(longitude));
        marker.position(point);

        circle.center(point);
        circle.radius(range);

        Map.addMarker(marker);
        Map.addCircle(circle);
    }

    public void mapend(View v){

    }
}
