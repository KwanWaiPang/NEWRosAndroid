package com.kwanwaipang.rosandroid.model.repositories.rosRepo.message;

import geometry_msgs.Twist;

public class TwistData extends RosData {
    public TwistData(Topic topic, Twist message) {
        super(topic, message);
        topic.type = Twist._TYPE;
    }
}
