package com.kwanwaipang.rosandroid.model.repositories.rosRepo.node;

import com.kwanwaipang.rosandroid.model.repositories.rosRepo.message.RosData;

import org.ros.internal.message.Message;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 16.09.20
 */
public class SubWidgetNode extends AbstractWidgetNode {//订阅者节点

    private final NodeListener listener;


    public SubWidgetNode(NodeListener listener) {
        this.listener = listener;
    }


    @Override
    public void onStart(ConnectedNode parentNode) {
        super.onStart(parentNode);

        try {
            this.widget.validMessage = true;

            Subscriber<? extends Message> subscriber = parentNode.newSubscriber(topic.name, topic.type);//创建订阅者，包含了话题的名字，消息的类型

            subscriber.addMessageListener(data -> {
                listener.onNewMessage(new RosData(topic, data));
            });

        } catch(Exception e) {
            this.widget.validMessage = false;
            e.printStackTrace();
        }

    }

    public interface NodeListener  {//
        void onNewMessage(RosData message);
    }
}
