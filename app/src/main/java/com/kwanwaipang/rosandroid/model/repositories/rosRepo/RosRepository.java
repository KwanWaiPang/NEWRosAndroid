package com.kwanwaipang.rosandroid.model.repositories.rosRepo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kwanwaipang.rosandroid.model.entities.MasterEntity;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.connection.ConnectionCheckTask;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.connection.ConnectionListener;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.connection.ConnectionType;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.ImageData;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.OdometryData;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.RosData;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.TwistData;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.node.AbstractWidgetNode;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.node.GeneralNode;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.node.NodeMainExecutorService;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.node.NodeMainExecutorServiceListener;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.node.PubWidgetNode;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.node.SubWidgetNode;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.Topic;
import com.kwanwaipang.rosandroid.model.repositories.rosRepo.node.BaseData;
import com.kwanwaipang.rosandroid.model.entities.BaseEntity;
import com.kwanwaipang.rosandroid.model.entities.PublisherEntity;
import com.kwanwaipang.rosandroid.model.entities.SubscriberEntity;

import org.ros.address.InetAddressFactory;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.master.client.TopicType;
import org.ros.namespace.GraphName;
import org.ros.node.NodeConfiguration;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import geometry_msgs.Twist;


/**
 * The ROS repository (资料库) is responsible for connecting to the ROS master
 * and creating nodes depending on the respective widgets.
 *ROS存储库负责连接到ROS主节点并根据相应的小部件创建节点。
 * @author Nico Studt
 * @version 1.1.3
 * @created on 16.01.20
 * @updated on 20.05.20
 * @modified by Nico Studt
 * @updated on 24.09.20
 * @modified by Nico Studt
 * @updated on 16.11.2020
 * @modified by Nils Rottmann
 */
public class RosRepository implements SubWidgetNode.NodeListener, GeneralNode.NodeListener  {

    private static final String TAG = RosRepository.class.getSimpleName();// use for debug
    private static RosRepository instance;//本实例

    private final WeakReference<Context> contextReference;
    private MasterEntity master;//要连接的master

    // original stuff in the app
    private final List<BaseEntity> currentWidgets;
    private final HashMap<Topic, AbstractWidgetNode> currentNodes;//当前话题与节点 (它存储的内容是键值对(key-value)映射)
    private final MutableLiveData<ConnectionType> rosConnected;//此处定义了连接的类型
    private final MutableLiveData<RosData> receivedData;//ROS数据，包括了话题与消息
    private NodeMainExecutorService nodeMainExecutorService;
    private NodeConfiguration nodeConfiguration;

    // for liphy ros project
    private GeneralNode liphyNode;
    private final MutableLiveData<ImageData> receivedImageData;
    private final MutableLiveData<OdometryData> receivedOdometryData;


    /**
     * Default private constructor. Initialize empty lists and maps of intern widgets and nodes.
     */
    private RosRepository(Context context) {
        this.contextReference = new WeakReference<>(context);
        this.currentWidgets = new ArrayList<>();
        this.currentNodes = new HashMap<>();
        this.rosConnected = new MutableLiveData<>(ConnectionType.DISCONNECTED);
        this.receivedData = new MutableLiveData<>();

        // liphy ros project
        this.receivedImageData = new MutableLiveData<>();
        this.receivedOdometryData = new MutableLiveData<>();
        this.liphyNode = new GeneralNode(this);//将节点在此处创建了
    }


    /**
     * Return the singleton instance of the repository.
     * @return Instance of this Repository
     */
    public static RosRepository getInstance(final Context context){
        if(instance == null){
            instance = new RosRepository(context);
        }

        return instance;
    }


    @Override
    public void onNewMessage(RosData message) {
        this.receivedData.postValue(message);
    }

    @Override
    public void onOdometryUpdate(OdometryData data) {
        this.receivedOdometryData.postValue(data);

    }

    @Override
    public void onImageUpdate(ImageData data) {
        this.receivedImageData.postValue(data);

    }

    private void registerLiphyNode() {//将自己建立的节点注册
        if (rosConnected.getValue() != ConnectionType.CONNECTED) {
            Log.w(TAG, "Not connected with master");
            return;
        }
        nodeMainExecutorService.execute(liphyNode, nodeConfiguration);
    }

    public void publishTwistData(TwistData data) {//此处发布消息
        liphyNode.setTwistData((Twist) data.getMessage());
    }


    /**
     * Find the associated node and inform it about the changed data.
     * @param data Widget data that has changed
     */
//    public void publishData(BaseData data) {
//        AbstractWidgetNode node = currentNodes.get(data.getTopic());//according to the data中的topic名字,获取当前的节点
//
//        if(node instanceof PubWidgetNode) {
//            ((PubWidgetNode)node).setData(data);//发布者发布数据
//        }
//    }
    public void publishWidgetData(BaseData data) {
        AbstractWidgetNode node = currentNodes.get(data.getTopic());//获取当前数据对应的话题对应的节点。

        if(node instanceof PubWidgetNode) {
            ((PubWidgetNode)node).setData(data);//发布者发布数据
        }
    }

    /**
     * Connect all registered nodes and establish a connection to the ROS master with
     * the connection details given by the already delivered master entity.
     */
    public void connectToMaster() {
        Log.i(TAG, "Connect to Master");

        ConnectionType connectionType = rosConnected.getValue();
        if (connectionType == ConnectionType.CONNECTED || connectionType == ConnectionType.PENDING) {
            return;
        }

        rosConnected.setValue(ConnectionType.PENDING);

        // Check connection
        new ConnectionCheckTask(new ConnectionListener() {

            @Override
            public void onSuccess() {
                bindService();
            }

            @Override
            public void onFailed() {
                rosConnected.postValue(ConnectionType.FAILED);
            }
        }).execute(master);
    }

    /**
     * Disconnect all running nodes and cut the connection to the ROS master.
     */
    public void disconnectFromMaster() {
        Log.i(TAG, "Disconnect from Master");
        if (nodeMainExecutorService == null) {
            return;
        }

        this.unregisterAllNodes();
        nodeMainExecutorService.shutdown();
    }


    /**
     * Change the connection details to the ROS master like the IP or port.
     * @param master Master data
     */
    public void updateMaster(MasterEntity master) {
        Log.i(TAG, "Update Master");

        if(master == null) {
            Log.i(TAG, "Master is null");
            return;
        }

        this.master = master;

        // nodeConfiguration = NodeConfiguration.newPublic(master.deviceIp, getMasterURI());
    }

    /**
     * Set the master device IP in the Nodeconfiguration
     */
    public void setMasterDeviceIp(String deviceIp) {
        nodeConfiguration = NodeConfiguration.newPublic(deviceIp, getMasterURI());
    }


    private URI getMasterURI() {
        String masterString = String.format("http://%s:%s/", master.ip, master.port);
        return URI.create(masterString);
    }

    private String getDefaultHostAddress() {
        return InetAddressFactory.newNonLoopback().getHostAddress();
    }

    public LiveData<RosData> getData() {
        return receivedData;
    }

    public LiveData<ImageData> getImageData() {
        return receivedImageData;
    }

    public LiveData<OdometryData> getOdometryData() {
        return receivedOdometryData;
    }

    /**
     * Get a list from the ROS Master with all available topics.
     * @return Topic list
     */
    public List<Topic> getTopicList() {
        ArrayList<Topic> topicList = new ArrayList<>();
        if (nodeMainExecutorService == null || nodeConfiguration == null) {
            return topicList;
        }

        MasterClient masterClient = new MasterClient(nodeMainExecutorService.getMasterUri());
        GraphName graphName = GraphName.newAnonymous();
        Response<List<TopicType>> responseList = masterClient.getTopicTypes(graphName);

        for (TopicType result: responseList.getResult()) {
            String name = result.getName();
            String type = result.getMessageType();

            Topic rosTopic = new Topic(name, type);
            topicList.add(rosTopic);
        }

        return topicList;
    }


    private final class RosServiceConnection implements ServiceConnection {

        NodeMainExecutorServiceListener serviceListener;
        URI customMasterUri;


        RosServiceConnection(URI customUri) {
            customMasterUri = customUri;
        }


        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            nodeMainExecutorService = ((NodeMainExecutorService.LocalBinder) binder).getService();
            nodeMainExecutorService.setMasterUri(customMasterUri);
            nodeMainExecutorService.setRosHostname(getDefaultHostAddress());

            serviceListener = nodeMainExecutorService ->
                    rosConnected.postValue(ConnectionType.DISCONNECTED);

            nodeMainExecutorService.addListener(serviceListener);
            rosConnected.setValue(ConnectionType.CONNECTED);

            registerAllNodes();//注册所有节点
            registerLiphyNode();//将自己建立的节点注册
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            nodeMainExecutorService.removeListener(serviceListener);
        }
    }

    public NodeMainExecutorService getNodeMainExecutorService(){
        return nodeMainExecutorService;
    }

    public NodeConfiguration getNodeConfiguration(){
        return nodeConfiguration;
    }



    //////////////////////////////////  for widgets  ///////////////////////////////////
    /**
     * React on a widget change. If at least one widget is added, deleted or changed this method
     * should be called.
     * @param newWidgets Current list of widgets
     */
    public void updateWidgets(List<BaseEntity> newWidgets) {
        // Compare old and new widget lists

        // Create widget check with ids
        HashMap<Long, Boolean> widgetCheckMap = new HashMap<>();
        HashMap<Long, BaseEntity> widgetEntryMap = new HashMap<>();

        for (BaseEntity oldWidget: currentWidgets) {
            widgetCheckMap.put(oldWidget.id, false);
            widgetEntryMap.put(oldWidget.id, oldWidget);
        }

        for (BaseEntity newWidget: newWidgets) {
            if (widgetCheckMap.containsKey(newWidget.id)) {
                // Node included in old and new list

                widgetCheckMap.put(newWidget.id, true);

                // Check if widget has changed
                BaseEntity oldWidget = widgetEntryMap.get(newWidget.id);
                updateNode(oldWidget, newWidget);

            } else{
                // Node not included in old list
                addNode(newWidget);
            }
        }

        // Delete unused widgets
        for (Long id: widgetCheckMap.keySet()) {
            if (!widgetCheckMap.get(id)) {
                // Node not included in new list
                removeNode(widgetEntryMap.get(id));
            }
        }

        this.currentWidgets.clear();
        this.currentWidgets.addAll(newWidgets);

    }

    /**
     * Get the current connection status of the ROS service as a live data.
     * @return Connection status
     */
    public LiveData<ConnectionType> getRosConnectionStatus() {
        return rosConnected;
    }


    private void bindService() {
        Context context = contextReference.get();
        if (context == null) {
            return;
        }

        RosServiceConnection serviceConnection = new RosServiceConnection(getMasterURI());

        // Create service intent
        Intent serviceIntent = new Intent(context, NodeMainExecutorService.class);
        serviceIntent.setAction(NodeMainExecutorService.ACTION_START);

        // Start service and check state
        context.startService(serviceIntent);
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Create a node for a specific widget entity.
     * The node will be created and subsequently registered.
     * @param widget Widget to be added
     */

    //添加节点
    //并且直接定义为是发布者还是订阅者
    private AbstractWidgetNode addNode(BaseEntity widget) {
        // Create a new node from widget
        AbstractWidgetNode node;

        if (widget instanceof PublisherEntity) {
            node = new PubWidgetNode();//创建发布者节点，发布者没有参数

        } else if (widget instanceof SubscriberEntity) {
            node = new SubWidgetNode(this);//创建订阅者节点

        }else {
            Log.i(TAG, "Widget is either publisher nor subscriber.");
            return null;
        }

        // Set node topic, add to node list and register it
        node.setWidget(widget);
        currentNodes.put(node.getTopic(), node);//所有节点加入currentNodes

        return node;
    }


    /**
     * Update a widget and its associated Node by ID in the ROS graph.
     * @param oldWidget Old version of the widget
     * @param widget Widget to update
     */
    private void updateNode(BaseEntity oldWidget, BaseEntity widget) {//更新currentNodes
        Log.i(TAG, "Update Widget: " + oldWidget.name);

        if (oldWidget.equalRosState(widget)){
            AbstractWidgetNode node = this.currentNodes.get(widget.topic);
            node.setWidget(widget);

        } else{
            this.removeNode(oldWidget);
            AbstractWidgetNode node = this.addNode(widget);
            this.registerNode(node);
        }

    }

    /**
     * Remove a widget and its associated Node in the ROS graph.
     * @param widget Widget to remove
     */
    private void removeNode(BaseEntity widget) {
        AbstractWidgetNode node = this.currentNodes.remove(widget.topic);
        this.unregisterNode(node);
    }

    /**
     * Connect the node to ROS node graph if a connection to the ROS master is running.
     * @param node Node to connect
     */
    private void registerNode(AbstractWidgetNode node) {//注册节点
        Log.i(TAG, "register Node");

        if (rosConnected.getValue() != ConnectionType.CONNECTED) {
            Log.w(TAG, "Not connected with master");
            return;
        }

        nodeMainExecutorService.execute(node, nodeConfiguration);
    }

    /**
     * Disconnect the node from ROS node graph if a connection to the ROS master is running.
     * @param node Node to disconnect
     */
    private void unregisterNode(AbstractWidgetNode node) {
        Log.i(TAG, "unregister Node");

        if (rosConnected.getValue() != ConnectionType.CONNECTED) {
            Log.w(TAG, "Not connected with master");
            return;
        }

        nodeMainExecutorService.shutdownNodeMain(node);
    }

    private void registerAllNodes() {
        for (AbstractWidgetNode node: currentNodes.values()) {
            this.registerNode(node);//注册节点
        }
    }

    private void unregisterAllNodes() {
        for (AbstractWidgetNode node: currentNodes.values()) {
            this.unregisterNode(node);
        }
    }

    /**
     * Result of a change in the internal data of a node header. Therefore it has to be
     * unregistered from the service and reregistered due to the implementation of ROS.
     * @param node Node main to be reregistered
     */
    private void reregisterNode(AbstractWidgetNode node) {
        Log.i(TAG, "Reregister Node");

        unregisterNode(node);
        registerNode(node);
    }

}