package com.example.scarb.firebasetest;

import java.util.Calendar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MapStyleOptions;


/**
 * A simple {@link Fragment} subclass.
 */
public class HotSpotFragment extends android.app.Fragment implements OnMapReadyCallback{

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_hot_spot, container, false);
        } catch (Exception e) {
        /* map is already there, just return view as it is */
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Calendar calendar = Calendar.getInstance();
        int amPm = calendar.get(Calendar.AM_PM);
        int currentHour = calendar.get(Calendar.HOUR);
        //Makes the map look dark if it is past 6PM
        if (amPm == Calendar.AM){
            if (currentHour == 12 || (currentHour >= 1 && currentHour < 6)){
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));
            }
        }else if (amPm == Calendar.PM){
            if (currentHour >= 6 && currentHour <= 11){
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));
            }
        }
    }
}
