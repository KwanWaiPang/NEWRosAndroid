package com.kwanwaipang.rosandroid.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kwanwaipang.rosandroid.R;
import com.kwanwaipang.rosandroid.ROSTopicController;
import com.kwanwaipang.rosandroid.ui.fragments.ssh.SshFragment;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapxus.map.mapxusmap.api.map.MapViewProvider;
import com.mapxus.map.mapxusmap.api.map.MapxusMap;
import com.mapxus.map.mapxusmap.api.map.MapxusMapContext;
import com.mapxus.map.mapxusmap.api.map.interfaces.OnMapxusMapReadyCallback;
import com.mapxus.map.mapxusmap.impl.MapboxMapViewProvider;

import geometry_msgs.Point;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;


public class indoormap extends Fragment implements OnMapReadyCallback, OnMapxusMapReadyCallback {
    private boolean isMapxusMapReady = false;
    private MapView mapView;
    private MapViewProvider mapViewProvider;
    private MapboxMap mapboxMap;
    private MapxusMap mapxusMap;

    // The ROSTopicController for managing the connection to the Robot
    private ROSTopicController controller;
    double robotlocalization_x;
    double robotlocalization_y;

    public static indoormap newInstance() {
        return new indoormap();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_indoormap, container, false);

        // mapxus/mapbox
//        MapxusMapContext.init(requireActivity().getApplicationContext());
        MapxusMapContext.init(getApplicationContext(),"com.liphy.light.android.app","/Iy3eV0lc");
        mapView = v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mapViewProvider = new MapboxMapViewProvider(requireActivity(), mapView);
        mapViewProvider.getMapxusMapAsync(this);


////        // Create the ROSTopicController
//        controller = new ROSTopicController(this);
//        controller.initialize(nodeMainExecutor, nodeConfiguration);
//        Point robotlocalization=controller.getOdometry().getPose().getPose().getPosition();
//        robotlocalization_x=robotlocalization.getX()*100;//the result of X cm
//        robotlocalization_y=robotlocalization.getY()*100;//the result of y cm




        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
        if (mapxusMap != null) {
            mapxusMap.onResume();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }


    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();
        if (mapxusMap != null) {
            mapxusMap.onPause();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mapView.onDestroy();
        if (mapViewProvider != null) {
            mapViewProvider.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }

    @Override
    public void onMapxusMapReady(MapxusMap mapxusMap) {
        this.mapxusMap = mapxusMap;
        isMapxusMapReady = true;
    }
}
