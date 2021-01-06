package com.kwanwaipang.rosandroid;

import android.preference.PreferenceManager;
import android.util.Log;

import com.kwanwaipang.rosandroid.model.entities.PublisherEntity;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.Topic;
import com.kwanwaipang.rosandroid.ui.activity.MainActivity;
import com.kwanwaipang.rosandroid.ui.fragments.Utils;

import geometry_msgs.Point;
import geometry_msgs.Pose;
import geometry_msgs.Quaternion;
import geometry_msgs.Twist;
import nav_msgs.Odometry;
import sensor_msgs.CompressedImage;
import sensor_msgs.LaserScan;
import sensor_msgs.NavSatFix;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ROSTopicController extends PublisherEntity implements NodeMain {

    // Logcat Tag
    private static final String TAG = "ROSTopicController";

    // The parent Context
    private MainActivity context;

    // The node connected to the Robot on which data can be sent and received
    private ConnectedNode connectedNode;

    // Whether the RobotController has been initialized
    private boolean initialized;

    // Timer for periodically publishing velocity commands
    private Timer publisherTimer;
    // Indicates when a velocity command should be published
    private boolean publishVelocity;//机器人速度的控制
    // Publisher for velocity commands
    private Publisher<Twist> movePublisher;
    // Contains the current velocity plan to be published
    private Twist currentVelocityCommand;


    // Subscriber to Odometry data
    private Subscriber<Odometry> odometrySubscriber;//此处跟slo-vlp输出的结果的数据类型一样
    private Odometry odometry;
    // Lock for synchronizing accessing and receiving the current Odometry
    private final Object odometryMutex = new Object();
    // Listener for Odometry
    private ArrayList<MessageListener<Odometry>> odometryListeners;



    private Subscriber<CompressedImage> imageSubscriber;
    private CompressedImage image;
    private final Object imageMutex = new Object();
    private MessageListener<CompressedImage> imageMessageReceived;


    // The Robot's starting position
    private static Point startPos;
    // The Robot's last recorded position
    private static Point currentPos;
    // The Robot's last recorded orientation
    private static Quaternion rotation;
    // The Robot's last recorded speed
    private static double speed;
    // The Robot's last recorded turn rate
    private static double turnRate;

    public ROSTopicController (MainActivity context){
        this.context = context;
        this.initialized = false;
        this.odometryListeners = new ArrayList<>();


        startPos = null;
        currentPos = null;
        rotation = null;
    }

    /**
     * Adds an Odometry listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addOdometryListener(MessageListener<Odometry> l) {
        return odometryListeners.add(l);
    }

    /**
     * Initializes the ROSTopicController.      初始化机器人控制器,控制不同topic的订阅
     * @param nodeMainExecutor The NodeMainExecutor on which to execute the NodeConfiguration.
     * @param nodeConfiguration The NodeConfiguration to execute
     */
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {

        nodeMainExecutor.execute(this, nodeConfiguration.setNodeName("android/robot_controller2"));////更换手机时，最好修改此处。改变控制器的名称
    }

    /**
     * Sets the next values of the next velocity to publish.
     * @param linearVelocityX Linear velocity in the x direction
     * @param linearVelocityY Linear velocity in the y direction
     * @param angularVelocityZ Angular velocity about the z axis
     */
    public void publishVelocity(double linearVelocityX, double linearVelocityY, double angularVelocityZ) {
        if (currentVelocityCommand != null) {

            float scale = 0.25f;//0.1f;//1.0f;///将速度加一个系数


            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefs_invert_x_axis_key), false)){
                linearVelocityX *= -1;
            }

            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefs_invert_y_axis_key), false)){
                linearVelocityY *= -1;
            }

            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefs_invert_angular_velocity_key), false)){
                angularVelocityZ *= -1;
            }

            currentVelocityCommand.getLinear().setX(linearVelocityX * scale);
            currentVelocityCommand.getLinear().setY(-linearVelocityY * scale);
            currentVelocityCommand.getLinear().setZ(0.0);
            //angular velocity
            currentVelocityCommand.getAngular().setX(0.0);
            currentVelocityCommand.getAngular().setY(0.0);
            currentVelocityCommand.getAngular().setZ(-angularVelocityZ * scale);
        } else {
            Log.w("Emergency Stop", "currentVelocityCommand is null");
        }
    }

    /**
     * Same as above, but forces the velocity to be published.
     * @param linearVelocityX Linear velocity in the x direction
     * @param linearVelocityY Linear velocity in the y direction
     * @param angularVelocityZ Angular velocity about the z axis
     */
    public void forceVelocity(double linearVelocityX, double linearVelocityY,
                              double angularVelocityZ) {
        publishVelocity = true;
        publishVelocity(linearVelocityX, linearVelocityY, angularVelocityZ);
    }



    /**
     * @return The default node name for the RobotController
     */
    @Override
    public GraphName getDefaultNodeName() {
        return null;
    }

    /**
     * Callback for when the RobotController is connected.
     * @param connectedNode The ConnectedNode the RobotController is connected through
     */
    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        initialize();
    }

    /*
     * Initializes the RobotController.
     */
    public void initialize() {
        if (!initialized && connectedNode != null) {

            // Start the topics
            refreshTopics();

            initialized = true;
        }
    }

    /**
     * Refreshes all topics, recreating them if there topic names have been changed.
     */
    public void refreshTopics() {

        // Get the correct topic names
        // Get the correct topic names
        String moveTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_joystick_topic_edittext_key),
                        context.getString(R.string.joy_topic));


        String odometryTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_odometry_topic_edittext_key),
                        context.getString(R.string.odometry_topic));


        String imageTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_camera_topic_edittext_key),
                        context.getString(R.string.camera_topic));


        // Refresh the Move Publisher
        if (movePublisher == null
                || !moveTopic.equals(movePublisher.getTopicName().toString())) {

            if (publisherTimer != null) {
                publisherTimer.cancel();
            }

            if (movePublisher != null) {
                movePublisher.shutdown();
            }

            // Start the move publisher
            movePublisher = connectedNode.newPublisher(moveTopic, Twist._TYPE);
            currentVelocityCommand = movePublisher.newMessage();

            publisherTimer = new Timer();
            publisherTimer.schedule(new TimerTask() {
                @Override
                public void run() { if (publishVelocity) {
                    movePublisher.publish(currentVelocityCommand);
                }
                }
            }, 0, 80);
            publishVelocity = false;
        }


        // Refresh the Odometry Subscriber
        if (odometrySubscriber == null
                || !odometryTopic.equals(odometrySubscriber.getTopicName().toString())) {

            if (odometrySubscriber != null)
                odometrySubscriber.shutdown();

            // Start the Odometry subscriber
            odometrySubscriber = connectedNode.newSubscriber(odometryTopic, Odometry._TYPE);//定义一个订阅者
            odometrySubscriber.addMessageListener(new MessageListener<Odometry>() {
                @Override
                public void onNewMessage(Odometry odometry) {
                    setOdometry(odometry);//将话题设置
                }
            });
        }

        // Refresh the image Subscriber

        if(imageSubscriber == null || !imageTopic.equals(imageSubscriber.getTopicName().toString())){
            if(imageSubscriber != null)
                imageSubscriber.shutdown();

            imageSubscriber = connectedNode.newSubscriber(imageTopic, CompressedImage._TYPE);

            imageSubscriber.addMessageListener(new MessageListener<CompressedImage>() {
                @Override
                public void onNewMessage(CompressedImage image) {
                    setImage(image);
                    synchronized (imageMutex) {
                        if (imageMessageReceived != null) {
                            imageMessageReceived.onNewMessage(image);
                        }
                    }
                }
            });
        }
    }

    /**
     * Callback for when the RobotController is shutdown.
     * @param node The Node
     */
    @Override
    public void onShutdown(Node node) {
        shutdownTopics();
    }

    /**
     * Shuts down all topics.
     */
    public void shutdownTopics() {

        if(publisherTimer != null) {
            publisherTimer.cancel();
        }

        if (movePublisher != null) {
            movePublisher.shutdown();
        }

        if(odometrySubscriber != null){
            odometrySubscriber.shutdown();
        }

    }


    /**
     * Callback for when the shutdown is complete.
     * @param node The Node
     */
    @Override
    public void onShutdownComplete(Node node) {
        this.connectedNode = null;
    }

    /**
     * Callback indicating an error has occurred.
     * @param node The Node
     * @param throwable The error
     */
    @Override
    public void onError(Node node, Throwable throwable) {
        Log.e(TAG, "", throwable);
    }


//**********************************************************************************
    /**
     * @return The most recently received Odometry.
     */
    @SuppressWarnings("unused")
    public Odometry getOdometry() {
        synchronized (odometryMutex) {
            return odometry;
        }
    }

    /**
     * Sets the current Odometry.
     * @param odometry The Odometry
     */
    //设置当前的odometry话题
    protected void setOdometry(Odometry odometry) {
        synchronized (odometryMutex) {
            this.odometry = odometry;

            // Call the listener callbacks
            for (MessageListener<Odometry> listener: odometryListeners) {
                listener.onNewMessage(odometry);
            }

            // Record position TODO this should be moved to setPose() but that's not being called for some reason
            if (startPos == null) {
                startPos = odometry.getPose().getPose().getPosition();
            } else {
                currentPos = odometry.getPose().getPose().getPosition();
            }
            rotation = odometry.getPose().getPose().getOrientation();

            // Record speed and turnrate
            speed = odometry.getTwist().getTwist().getLinear().getX();
            turnRate = odometry.getTwist().getTwist().getAngular().getZ();
        }
    }

    /**
     * @return The Robot's last reported x position
     */
    public static double getX() {
        if (currentPos == null)
            return 0.0;
        else
            return currentPos.getX() - startPos.getX();
    }

    /**
     * @return The Robot's last reported y position
     */
    public static double getY() {
        if (currentPos == null)
            return 0.0;
        else
            return currentPos.getY() - startPos.getY();
    }

    /**
     * @return The Robot's last reported heading in radians
     */
    public static double getHeading() {
        if (rotation == null)
            return 0.0;
        else
            return Utils.getHeading(org.ros.rosjava_geometry.Quaternion.fromQuaternionMessage(rotation));
    }

    /**
     * @return The Robot's last reported speed in the range [-1, 1].
     */
    public static double getSpeed() {
        return speed;
    }

    /**
     * @return The Robot's last reported turn rate in the range[-1, 1].
     */
    public static double getTurnRate() {
        return turnRate;
    }


    public void setCameraMessageReceivedListener(MessageListener<CompressedImage> cameraMessageReceived) {
        this.imageMessageReceived = cameraMessageReceived;
    }

    public void setImage(CompressedImage image) {
        synchronized (imageMutex) {
            this.image = image;
        }
    }

    public CompressedImage getImage(){
        synchronized (imageMutex) {
            return this.image;
        }
    }

}
