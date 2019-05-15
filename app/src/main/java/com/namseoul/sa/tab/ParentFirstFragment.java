package com.namseoul.sa.tab;

import android.content.Intent;
import android.net.Network;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ParentFirstFragment extends Fragment {

    Button safezonebtn, mapstartbtn, helpbtn;

    public static ParentFirstFragment newInstance(){
        ParentFirstFragment fragment = new ParentFirstFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.parent_fragment1,container,false);

        safezonebtn = view.findViewById(R.id.safezonebtn);
        mapstartbtn = view.findViewById(R.id.mapstartbtn);
        helpbtn = view.findViewById(R.id.helpbtn);

        safezonebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(view.getContext(),SafeZoneSetting.class);
                startActivity(i);
            }
        });

        mapstartbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(view.getContext(),MapServiceActivity.class);
                startActivity(i);

            }
        });

        helpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(),MainActivity2.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
