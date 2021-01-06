package com.kwanwaipang.rosandroid.ui.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kwanwaipang.rosandroid.utility.MapUtils;
import com.kwanwaipang.rosandroid.utility.MapxusConstant;
import com.liphy.navigation.LatLngTranslator;
import com.liphy.navigation.LiphyState;
import com.liphy.navigation.bluetooth.BluetoothInfo;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapxus.map.mapxusmap.api.map.MapViewProvider;
import com.mapxus.map.mapxusmap.api.map.MapxusMap;
import com.mapxus.map.mapxusmap.api.map.interfaces.OnMapxusMapReadyCallback;
import com.mapxus.map.mapxusmap.impl.MapboxMapViewProvider;
import com.kwanwaipang.rosandroid.R;

import com.liphy.navigation.LiphyLocationService;
import com.parse.ParseObject;

import java.util.Timer;
import java.util.TimerTask;


public class MapFragment extends Fragment implements OnMapReadyCallback, OnMapxusMapReadyCallback, LiphyLocationService.OnLiphyServiceListener,LiphyLocationService.OnLiphyDeviceListener {


    private boolean isMapxusMapReady = false;
    private MapView mapView;
    private MapViewProvider mapViewProvider;
    private MapboxMap mapboxMap;
    private MapxusMap mapxusMap;
    private SymbolManager symbolManager;

    // liphy nav sdk
    private TextureView lightFlyTextureView;
    private LiphyLocationService liphyLocationService;
    private com.liphy.navigation.IndoorLocation lastLiphyLocation;
    private boolean isLiphyServiceBound = false;
    private static final float LIGHT_SIZE = 17.5f;

    private Timer timer = new Timer();


    //    private Pair<Double, Double> coordinateInCm;
    private double phone_x;
    private double phone_y;
    int phone_xx,phone_yy;




    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_indoormap, container, false);

        // mapxus/mapbox
//        MapxusMapContext.init(requireActivity().getApplicationContext());
//        MapxusMapContext.init(getApplicationContext(),"com.liphy.light.android.app","/Iy3eV0lc");
        mapView = v.findViewById(R.id.mapView);
        mapView.getMapAsync(this);

        mapViewProvider = new MapboxMapViewProvider(requireActivity(), mapView);
        mapViewProvider.getMapxusMapAsync(this);


        lightFlyTextureView = v.findViewById(R.id.liphy_texture_view);
        Intent liphyLocationServiceIntent = new Intent(requireActivity(), LiphyLocationService.class);
        requireActivity().bindService(liphyLocationServiceIntent, connection, Context.BIND_AUTO_CREATE);

        timer.scheduleAtFixedRate(new TimerTask() {//每500ms.读取一次数据，确保定位数据更新
            @Override
            public void run() {
                try{

                    //PDR的结果
                    phone_xx=(int) phone_x;
                    phone_yy=(int) phone_y;



                }
                catch (Exception e)
                {
//                    Toast.makeText(getActivity(), "Robot's position is not available", Toast.LENGTH_SHORT).show();
                    Log.v("robotlocalization","Robot's position is not available");
                }
            }
        }, 0, 500);




        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
//        if (mapxusMap != null) {
//            mapxusMap.onResume();
//        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        if (isLiphyServiceBound) {
            if (!liphyLocationService.isBluetoothRunning()) {
                liphyLocationService.startBluetoothSearch();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        if (isLiphyServiceBound) {
            liphyLocationService.stopLocationProviders();
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();
//        if (mapxusMap != null) {
//            mapxusMap.onPause();
//        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        timer.cancel();//防止内存泄漏
        timer=null;

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

        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                symbolManager = new SymbolManager(mapView, mapboxMap, style);
                symbolManager.setIconAllowOverlap(true);
                symbolManager.setIconIgnorePlacement(true);
                symbolManager.setIconOptional(false);

                style.addImage(MapxusConstant.IMAGE_ID_LIPHY_MARKER,
                        BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.marker_100)));

                style.addImage(MapxusConstant.IMAGE_ID_LIPHY_PRIVATE,
                        BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.liphy_private)),
                        false);

                /*
                Reference:
                mapxus blue layer: layer 15
                roads: layer 31
                 */

                // example
//                MapUtils.generateMapLayout(style, new LatLng(34.661519, 135.501344), 15.4, 16.2, 0.0,
//                        MapxusConstant.ID_IMAGE_SOURCE_DAIKAN, MapxusConstant.ID_IMAGE_LAYER_DAIKAN, R.drawable.daikan_showroom_floorplan, 1);

                MapUtils.generateMapLayout(style, new LatLng(22.334983, 114.263727), 15, 21, 90.0,
                        MapxusConstant.ID_IMAGE_SOURCE_ICDC, MapxusConstant.ID_IMAGE_LAYER_ICDC, R.drawable.icdc_map1, 1);
                //输入经纬度
                //图的实际大小

            }
        });
    }



    @Override
    public void onMapxusMapReady(MapxusMap mapxusMap) {
        this.mapxusMap = mapxusMap;
        isMapxusMapReady = true;
    }



    @Override
    public void onLatestLightIdReady(String s, ParseObject parseObject) {

    }

    @Override
    public void onLatestBluetoothInfoLoaded(BluetoothInfo bluetoothInfo) {
        Toast.makeText(getActivity(), bluetoothInfo.getBluetoothName(), Toast.LENGTH_LONG).show();

    }

    @Override
    public void onLocationUpdate(com.liphy.navigation.IndoorLocation location) {
        if (location.getProvider().equals(getString(R.string.provider_bluetooth))) {
            // enable pdr
            LiphyState.setFirstSignalReceived(true);
//            Timber.tag("location").d("LIPHY & PDR ENABLED BY BLUETOOTH");
            return;
        } else if (location.getProvider().equals(getString(R.string.provider_liphy))) {
            // first liphy location set as origin
            if (!LiphyState.isFirstLiphySignalReceived()) {
//                coordinateInCm = new Pair<>(0.0, 0.0);
                phone_x = 0;
                phone_y = 0;
                LiphyState.setFirstLiphySignalReceived(true);
            }

            /*
            delete this after the hardcoded table is done
             */
            phone_x = 0;
            phone_y = 0;

            lastLiphyLocation = new com.liphy.navigation.IndoorLocation(getString(R.string.provider_liphy), location.getLatitude(),
                    location.getLongitude(), location.getFloor(), location.getBuildingId(),
                    location.getBuildingName(), location.getBearing(), location.getTime());

        } else if (location.getProvider().equals(getString(R.string.provider_pdr))) {
//            Timber.tag("location").d("bearing %f", location.getBearing());
//            Timber.tag("location").d("length %f", Math.sin(Math.toRadians(location.getBearing())));

            phone_x += LatLngTranslator.DEFAULT_STEP_DIST * 100 * Math.sin(Math.toRadians(location.getBearing()));
            phone_y += LatLngTranslator.DEFAULT_STEP_DIST * 100 * Math.cos(Math.toRadians(location.getBearing()));
//            Toast.makeText(getActivity(), ""+phone_x +""+phone_y, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onOrientationUpdate(float v) {

    }

    @Override
    public void onCompassAccuracyChanged(int i) {

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LiphyLocationService.LocalBinder binder = (LiphyLocationService.LocalBinder) service;
            liphyLocationService = binder.getService();
            liphyLocationService.getActivityAndTextureView(getActivity(), lightFlyTextureView);

            // locaiton info callback
            liphyLocationService.registerOnLiphyServiceListener(MapFragment.this);
            // liphy & bluetooth callback
            liphyLocationService.registerOnLiphyDeviceListener(MapFragment.this);

//            liphyLocationService.setAccessKeyForLiphySdk();

//            // start bluetooth (liphy will be started automatically)
//            if (EasyPermissions.hasPermissions(RobotChooser.this, Manifest.permission.CAMERA,
//            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            if (LiphyState.isBluetoothSearch() && !liphyLocationService.isBluetoothRunning())
                liphyLocationService.startBluetoothSearch();
//            }

//            Timber.tag("liphysdk").d("version: %s", liphyLocationService.liphySdkVersion());
//            Timber.tag("liphysdk").d("build no.: %s", liphyLocationService.liphySdkBuildNumebr());
//            Timber.tag("liphysdk").d("expiry date: %s", liphyLocationService.liphySdkExpiryDate());

            isLiphyServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // see on destroyed
            liphyLocationService.unregisterOnLiphyServiceListener();
            liphyLocationService = null;
            isLiphyServiceBound = false;
        }
    };
}
