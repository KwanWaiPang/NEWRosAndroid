package com.kwanwaipang.rosandroid.widgets.costmap;

import android.view.View;

import androidx.annotation.NonNull;

import com.kwanwaipang.rosandroid.ui.fragments.details.WidgetChangeListener;
import com.kwanwaipang.rosandroid.ui.views.BaseDetailSubscriberVH;

import java.util.Collections;
import java.util.List;

import nav_msgs.OccupancyGrid;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.1
 * @created on 14.09.2020
 * @updated on 23.10.2020
 * @modified by Nico Studt
 */
public class CostMapDetailVH extends BaseDetailSubscriberVH<CostMapEntity> {


    public CostMapDetailVH(@NonNull View view, WidgetChangeListener updateListener) {
        super(view, updateListener);
    }


    @Override
    protected void initView(View parentView) {

    }

    @Override
    protected void bindEntity(CostMapEntity entity) {

    }

    @Override
    protected void updateEntity() {

    }

    @Override
    public List<String> getTopicTypes() {
        return Collections.singletonList(OccupancyGrid._TYPE);
    }

}