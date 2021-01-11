package com.kwanwaipang.rosandroid.model.repositories.rosRepo.message;

import nav_msgs.Odometry;

public class OdometryData extends RosData {
    public OdometryData(Topic topic, Odometry message) {
        super(topic, message);
        topic.type = Odometry._TYPE;
        topic.name="/odometry/filtered";
    }
}
