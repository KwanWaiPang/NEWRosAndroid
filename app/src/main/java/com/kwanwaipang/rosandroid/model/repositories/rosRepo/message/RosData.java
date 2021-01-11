package com.kwanwaipang.rosandroid.model.repositories.rosRepo.message;

import org.ros.internal.message.Message;

/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 21.09.20
 * @updated on
 * @modified by
 */
public class RosData {

//    private final Topic topic;
//    private final Message message;
    private Topic topic;
    private Message message;


    public RosData(Topic topic, Message message) {
        this.topic = topic;
        this.message = message;
    }


    public Topic getTopic() {//获取topic，topic里面，定义了topic的name与topic的类型
        return this.topic;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
