package com.kwanwaipang.rosandroid.utility;

import android.os.Handler;

import com.liphy.navigation.IndoorLocation;
import com.liphy.navigation.LatLngTranslator;
import com.liphy.navigation.LiphyState;
import com.liphy.navigation.network.LiphyCloudException;
import com.liphy.navigation.network.LiphyCloudManager;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.ImageSource;
import com.mapxus.map.mapxusmap.api.map.MapxusMap;

import timber.log.Timber;

public class MapUtils {

    public static void generateMapLayout(Style style, LatLng startPoint, double width, double height, double angle,
                                   String imageSourceId, String imageLayerId, int resourceId, int index) {
        if (style.getLayer(imageLayerId) != null)
            style.removeLayer(style.getLayer(imageLayerId));

        if (style.getSource(imageSourceId) != null)
            style.removeSource(imageSourceId);

        LatLngQuad quad = getMapCornerLatLng(new LatLng(startPoint.getLatitude(), startPoint.getLongitude()), width, height, angle);

        ImageSource imageSource = new ImageSource(imageSourceId, quad, resourceId);
        RasterLayer rasterLayer = new RasterLayer(imageLayerId, imageSourceId);

        style.addSource(imageSource);
        style.addLayerAt(rasterLayer, index);
    }

    public static LatLngQuad getMapCornerLatLng(LatLng startPoint, double width, double height, double angle) {
        LatLngTranslator translator;

        translator = new LatLngTranslator(startPoint.getLatitude(), startPoint.getLongitude(), 90.0 + angle, width);
        translator.calFinalLatLng();
        LatLng secondPoint = new LatLng(translator.getFinalLat(), translator.getFinalLng());

        translator = new LatLngTranslator(secondPoint.getLatitude(), secondPoint.getLongitude(), 180.0 + angle, height);
        translator.calFinalLatLng();
        LatLng thirdPoint = new LatLng(translator.getFinalLat(), translator.getFinalLng());

        translator = new LatLngTranslator(thirdPoint.getLatitude(), thirdPoint.getLongitude(), 270.0 + angle, width);
        translator.calFinalLatLng();
        LatLng forthPoint = new LatLng(translator.getFinalLat(), translator.getFinalLng());

        return new LatLngQuad(startPoint, secondPoint, thirdPoint, forthPoint);
    }


    public static void cameraZoomToUser(MapboxMap mapboxMap, MapxusMap mapxusMap, LiphyState.ZoomCase zoomCase, IndoorLocation location) {
        switch (zoomCase) {
            case FIRST_TIME:
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                        location.getLongitude()), 21), 800);
//                new Handler().postDelayed(() -> {
//                    mapxusMap.setFollowUserMode(FollowUserMode.FOLLOW_USER_AND_HEADING);
//                }, 1200);
                break;
            case SWITCH_FLOOR_OR_BUILDING:
                mapxusMap.switchFloor(location.getFloor());
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                        location.getLongitude()), 19));

                // switch floor
//                if (location.getFloor() != null && !location.getFloor().equals("")) {
//                    new Handler().postDelayed(() ->
//                            mapxusMap.switchFloor(location.getFloor())
//                    , 2500);
//                }
                break;
            case CLOSE_ZOOM:
//                mapxusMap.switchFloor(location.getFloor());
                //mapboxMap.getProjection().getVisibleRegion().latLngBounds.contains()

                Timber.d("EnteredCloseZoom");
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                        location.getLongitude()), 21));
//                new Handler().postDelayed(() -> {
//                }, 500);


                new Handler().postDelayed(() -> {
                    try {
                        LiphyCloudManager cloudManager = LiphyCloudManager.getInstance();

                        // mapxus map then we switch floor too
                        if (!cloudManager.isSelfDefinedMap(location.getBuildingName())) {
                            // switch floor
                            mapxusMap.switchBuilding(location.getBuildingId());
                            mapxusMap.switchFloor(location.getFloor());
                            Timber.d("EnteredCloseZoomSwitchedBuidling");

                        }
                    } catch (LiphyCloudException e) {
                        // TODO: 14/8/2020 decide whether we should keep this
//                        showDebugToast(e.getMessage());
                    }
                }, 1500);

                break;
        }
    }
}
