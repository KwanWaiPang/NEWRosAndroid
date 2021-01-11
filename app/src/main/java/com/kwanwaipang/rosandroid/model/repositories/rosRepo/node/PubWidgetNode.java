package com.kwanwaipang.rosandroid.model.repositories.rosRepo.node;

import com.kwanwaipang.rosandroid.model.entities.BaseEntity;
import com.kwanwaipang.rosandroid.model.entities.PublisherEntity;

import org.ros.internal.message.Message;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import java.util.Timer;
import java.util.TimerTask;

/**
 * ROS Node for publishing Messages on a specific topic.   //ROS节点，用于发布有关特定主题的消息
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 16.09.20
 * @updated on
 * @modified by
 */
public class PubWidgetNode extends AbstractWidgetNode {//发布者节点

    private Publisher<Message> publisher;
    private BaseData lastData;
    private Timer pubTimer;
    private long pubPeriod = 100L;
    private boolean immediatePublish = true;


    @Override
    public void onStart(ConnectedNode parentNode) {
        publisher = parentNode.newPublisher(topic.name, topic.type);

        this.createAndStartSchedule();
    }

    /**
     * Call this method to publish a ROS message.
     *
     * @param data Data to publish
     */
    public void setData(BaseData data) {//发布
        this.lastData = data;//发布者获取数据

        if (immediatePublish) {
            publish();//发布者发布数据
        }
    }

    /**
     * Set publishing frequency.
     * E.g. With a value of 10 the node will publish 10 times per second.
     *
     * @param hz Frequency in hertz
     */
    public void setFrequency(float hz) {
        this.pubPeriod = (long) (1000 / hz);
    }//设置了发布的频率

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
        if (pubTimer != null) {
            pubTimer.cancel();
        }

        if (immediatePublish) {
            return;
        }

        pubTimer = new Timer();
        pubTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                publish();
            }
        }, pubPeriod, pubPeriod);
    }

    private void publish() {
        if (lastData == null || !publisher.hasSubscribers()) {
            return;
        }

        Message message = lastData.toRosMessage(publisher, widget);

        publisher.publish(message);
    }

    @Override
    public void setWidget(BaseEntity widget) {
        super.setWidget(widget);

        if (!(widget instanceof PublisherEntity)) {
            return;
        }

        PublisherEntity pubEntity = (PublisherEntity) widget;

        this.setImmediatePublish(pubEntity.immediatePublish);
        this.setFrequency(pubEntity.publishRate);
    }
}
