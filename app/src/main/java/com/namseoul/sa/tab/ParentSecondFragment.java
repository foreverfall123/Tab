package com.namseoul.sa.tab;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ParentSecondFragment extends Fragment {

    Button safezonsetbtn, helpersetbtn, anothersetbtn;

    public static ParentSecondFragment newInstance(){
        ParentSecondFragment fragment= new ParentSecondFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    //@Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.parent_fragment2,container,false);

        safezonsetbtn = view.findViewById(R.id.safesetbtn);
        helpersetbtn = view.findViewById(R.id.helpersetbtn);
        anothersetbtn = view.findViewById(R.id.anothersetbtn);

        safezonsetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(view.getContext(),SafeZoneStart.class);
                startActivity(i);
            }
        });

        helpersetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(),MainActivity3.class);
                startActivity(intent);
            }
        });

        anothersetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }
}
