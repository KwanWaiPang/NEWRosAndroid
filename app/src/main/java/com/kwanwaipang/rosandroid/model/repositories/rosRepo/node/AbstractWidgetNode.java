package com.kwanwaipang.rosandroid.model.repositories.rosRepo.node;

import android.util.Log;

import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.Topic;
import com.kwanwaipang.rosandroid.model.entities.BaseEntity;

import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 15.09.20
 */
public class AbstractWidgetNode implements NodeMain {
    //NodeMain(extends NodeListener)是创建节点所需的一个NodeListener。 应该使用NodeListener.onStart（ConnectedNode）来设置程序的发布者，订阅者等。

    public static final String TAG = AbstractWidgetNode.class.getSimpleName();

    protected Topic topic;
    protected BaseEntity widget;


    @Override
    public void onStart(ConnectedNode parentNode) {
        Log.i(TAG, "On Start:  " + topic.name);
    }

    @Override
    public void onShutdown(Node node) {
        Log.i(TAG, "On Shutdown:  " + topic.name);
    }

    @Override
    public void onShutdownComplete(Node node) {
        Log.i(TAG, "On Shutdown Complete: " + topic.name);
    }

    @Override
    public void onError(Node node, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(topic.name);
    }
//    返回值：
//    如果未在节点的关联NodeConfiguration中指定名称，将使用的节点名称


    public Topic getTopic() {
        return this.topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public void setWidget(BaseEntity widget) {
        this.widget = widget;
        this.setTopic(widget.topic);
    }

}
