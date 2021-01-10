package com.kwanwaipang.rosandroid.model.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.1
 * @created on 30.01.20
 * @updated on 31.01.20
 * @modified by
 */
@Entity(tableName = "master_table")
public class MasterEntity {//master实体

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long configId;
    public String ip = "10.79.138.249";//默认的IP
    public int port = 11311;
}
