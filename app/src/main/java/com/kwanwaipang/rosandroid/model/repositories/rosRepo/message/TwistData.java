package com.kwanwaipang.rosandroid.model.repositories.rosRepo.message;

import geometry_msgs.Twist;

public class TwistData extends RosData {
    public TwistData(Topic topic, Twist message) {
        super(topic, message);//继承了ROSData
        topic.type = Twist._TYPE;//只是消息类型确认了为Twist._TYPE
        topic.name="/cmd_vel";
    }
}
