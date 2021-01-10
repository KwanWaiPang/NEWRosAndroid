package com.kwanwaipang.rosandroid.model.repositories.rosRepo.message;

import sensor_msgs.CompressedImage;

public class ImageData extends RosData {
    public ImageData(Topic topic, CompressedImage message) {
        super(topic, message);
        topic.type = CompressedImage._TYPE;
    }
}