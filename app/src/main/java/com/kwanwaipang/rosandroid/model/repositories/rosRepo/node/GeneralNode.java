package com.kwanwaipang.rosandroid.model.repositories.rosRepo.node;
import android.util.Log;

import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.ImageData;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.OdometryData;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.RosData;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.TwistData;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.Timer;
import java.util.TimerTask;

import geometry_msgs.Twist;
import nav_msgs.Odometry;
import sensor_msgs.CompressedImage;

public class GeneralNode implements NodeMain {//此节点，既可以发布消息，也可以订阅消息

    private static final String TAG = "GeneralNode";

    private ConnectedNode connectedNode; // save for deletion later

    private Publisher<Twist> twistPublisher;//
    private Subscriber<Odometry> odometrySubscriber;
    private Subscriber<CompressedImage> imageSubscriber;

    // publisher param
    private Timer publishTimer;
    private long publishPeriod = 100L;
    private boolean immediatePublish = true;

    // listeners for subscriber callback
    private final GeneralNode.NodeListener listener;

    // topic/data
//    private MovementTopic movementTopic;
//    private OdometryTopic odometryTopic;
//    private ImageTopic imageTopic;
    private TwistData twistData;
    private OdometryData odometryData;
    private ImageData imageData;

//    private Twist speedCommand;
//    private Odometry odometry;
//    private CompressedImage image;

//    // The Robot's starting position
//    private static Point startPos;
//    // The Robot's last recorded position
//    private static Point currentPos;
//    // The Robot's last recorded orientation
//    private static Quaternion rotation;
//    // The Robot's last recorded speed
//    private static double speed;
//    // The Robot's last recorded turn rate
//    private static double turnRate;

    public GeneralNode(NodeListener listener) {  //构造函数
        this.listener = listener;
    }

    @Override
    public GraphName getDefaultNodeName() {///返回节点的默认名字。该名字会一直被使用，除非在NodeConfiguration中被重新设置
        return GraphName.of(TAG);//当rosjava指向node，topic或者parameters的时候，会采用GraphName
    }

    @Override
    public void onStart(ConnectedNode parentNode) {///此处是输入代码的，就是开始节点了。参数ConnectedNode是用于构建发布者与订阅者
        connectedNode = parentNode;

        // publishers
        twistPublisher = parentNode.newPublisher(twistData.getTopic().name , Twist._TYPE);////定义发布的话题的名字以及其消息类型
        twistData.setMessage(twistPublisher.newMessage());//设置数据
        //此处还没发布

        //
        createAndStartSchedule();

        // subscribers
        try {
            odometrySubscriber = parentNode.newSubscriber(odometryData.getTopic().name, Odometry._TYPE);//创建一个订阅者，对应的要填入话题的名称以及消息的类型
            odometrySubscriber.addMessageListener(new MessageListener<Odometry>() {
                @Override
                public void onNewMessage(Odometry odometry) {
                    odometryData.setMessage(odometry);
                    listener.onOdometryUpdate(odometryData);
                }
            });

            imageSubscriber = parentNode.newSubscriber(imageData.getTopic().name, CompressedImage._TYPE);
            imageSubscriber.addMessageListener(new MessageListener<CompressedImage>() {
                @Override
                public void onNewMessage(CompressedImage image) {
                    imageData.setMessage(image);
                    listener.onImageUpdate(imageData);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutdown(Node node) {//离开代码，应该用于关闭所有的发布者订阅者
        Log.i(TAG, "On Shutdown:  " + TAG);

        if(publishTimer != null) {
            publishTimer.cancel();
        }

        if (twistPublisher != null) {
            twistPublisher.shutdown();
        }

        if(odometrySubscriber != null){
            odometrySubscriber.shutdown();
        }

        if (imageSubscriber != null) {
            imageSubscriber.shutdown();
        }
    }

    @Override
    public void onShutdownComplete(Node node) {//最后的退出程序的点
        this.connectedNode = null;
    }

    @Override
    public void onError(Node node, Throwable throwable) {//报错的时候调用
        Log.e(TAG, "", throwable);
    }


    /**
     * Call this method to publish a ROS message.
     *
     * @param twist Data to publish
     */
    public void setTwistData(Twist twist) {
        this.twistData.setMessage(twist);

        if (immediatePublish) {
            publish();//发布消息
        }
    }

    /**
     * Set publishing frequency.
     * E.g. With a value of 10 the node will publish 10 times per second.
     *
     * @param hz Frequency in hertz
     */
    public void setFrequency(float hz) {
        this.publishPeriod = (long) (1000 / hz);
    }

    /**
     * Enable or disable immediate publishing.
     * In the enabled state the node will create und send a ros message as soon as
     * @link #setData(Object) is called.
     *
     * @param flag Enable immediate publishing
     */
    public void setImmediatePublish(boolean flag) {
        this.immediatePublish = flag;
    }

    private void createAndStartSchedule() {
        if (publishTimer != null) {
            publishTimer.cancel();
        }

        if (immediatePublish) {
            return;
        }

        publishTimer = new Timer();
        publishTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                publish();
            }
        }, publishPeriod, publishPeriod);
    }

    private void publish() {
        // movementTopic
        if (twistData != null && twistPublisher.hasSubscribers()) {
            twistPublisher.publish((Twist) twistData.getMessage());
        }

        // other data
    }

    public interface NodeListener  {
        void onNewMessage(RosData message);
        void onOdometryUpdate(OdometryData data);
        void onImageUpdate(ImageData data);
    }
}
