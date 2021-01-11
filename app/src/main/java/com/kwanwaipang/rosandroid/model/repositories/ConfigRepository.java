package com.kwanwaipang.rosandroid.model.repositories;

import androidx.lifecycle.LiveData;

import com.kwanwaipang.rosandroid.model.entities.ConfigEntity;
import com.kwanwaipang.rosandroid.model.entities.MasterEntity;
import com.kwanwaipang.rosandroid.model.entities.SSHEntity;
import com.kwanwaipang.rosandroid.model.entities.BaseEntity;

import java.util.List;

/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.2
 * @created on 26.01.20
 * @updated on 11.04.20
 * @modified by Nils Rottmann
 * @updated on 04.06.20
 * @modified by Nils Rottmann
 * @updated on 27.07.20
 * @modified by Nils Rottmann
 */
public interface ConfigRepository {

    void chooseConfig(long configId);

    void createConfig(String configName);

    void removeConfig(long configId);

    void updateConfig(ConfigEntity config);


    LiveData<List<ConfigEntity>> getAllConfigs();

    LiveData<Long> getCurrentConfigId();

    LiveData<ConfigEntity> getConfig(long id);

    LiveData<ConfigEntity> getCurrentConfig();


    void updateMaster(MasterEntity master);

    LiveData<MasterEntity> getMaster(long configId);


    void createWidget(String widgetType);

    void deleteWidget(BaseEntity widget);

    void addWidget(BaseEntity widget);

    void updateWidget(BaseEntity widget);

    LiveData<List<BaseEntity>> getWidgets(long id);


    void updateSSH(SSHEntity ssh);

    void setSSH(SSHEntity ssh, String configId);

    LiveData<SSHEntity> getSSH(long configId);
}