package com.kwanwaipang.rosandroid.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kwanwaipang.rosandroid.R;
import com.kwanwaipang.rosandroid.ROSTopicController;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.RosRepository;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.node.NodeMainExecutorService;
import com.kwanwaipang.rosandroid.ui.activity.MainActivity;
import com.kwanwaipang.rosandroid.ui.fragments.ssh.SshFragment;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapxus.map.mapxusmap.api.map.MapViewProvider;
import com.mapxus.map.mapxusmap.api.map.MapxusMap;
import com.mapxus.map.mapxusmap.api.map.MapxusMapContext;
import com.mapxus.map.mapxusmap.api.map.interfaces.OnMapxusMapReadyCallback;
import com.mapxus.map.mapxusmap.impl.MapboxMapViewProvider;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.Timer;
import java.util.TimerTask;

import geometry_msgs.Point;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;


public class indoormap extends Fragment {


    private static final String TAG = "test";
    // The ROSTopicController for managing the connection to the Robot
    private MainActivity context;
    private ROSTopicController controller;
    // NodeMainExecutor encapsulating the Robot's connection
    // The NodeConfiguration for the connection
    RosRepository rosRepo;
    double robotlocalization_x;
    double robotlocalization_y;
    private Timer timer = new Timer();

    public static indoormap newInstance() {
        return new indoormap();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_indoormap, container, false);



//  /      // Create the ROSTopicController
//        controller = new ROSTopicController(this);
        controller = new ROSTopicController(context);
//        controller.initialize(nodeMainExecutor, nodeConfiguration);//Ros_Repository
        this.rosRepo = RosRepository.getInstance(requireActivity().getApplication());//初始化
        controller.initialize(rosRepo.getNodeMainExecutorService(),rosRepo.getNodeConfiguration());
//        if (controller ==null){
//            Log.v(TAG, "onCreateView: ");
//        }else {
//            Log.v(TAG, "hhh: ");
//        }
        Point robotlocalization=controller.getOdometry().getPose().getPose().getPosition();
        robotlocalization_x=robotlocalization.getX()*100;//the result of X cm
        robotlocalization_y=robotlocalization.getY()*100;//the result of y cm

        timer.scheduleAtFixedRate(new TimerTask() {//每500ms.读取一次数据，确保定位数据更新
            @Override
            public void run() {
                //the result of slo-vlp
                try{

                    Log.v("robotlocalization_x",String.valueOf(robotlocalization_x));
                    Log.v("robotlocalization_y",String.valueOf(robotlocalization_y));
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


    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }


}
