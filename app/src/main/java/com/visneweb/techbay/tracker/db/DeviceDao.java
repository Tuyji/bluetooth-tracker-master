package com.visneweb.techbay.tracker.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by riskactive on 19.03.2018.
 */

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM device")
    List<MyDevice> getAll();

    @Query("SELECT * FROM device WHERE tracked = 1")
    List<MyDevice> getTracking();

    @Query("SELECT * FROM device WHERE near = 1")
    List<MyDevice> getNearDevices();

    @Query("SELECT * FROM device WHERE macAddress = :ad LIMIT 1")
    MyDevice findDevice(String ad);

    @Query("UPDATE device set near = 0")
    void setAllFar();

    @Query("UPDATE device set near = 1 WHERE macAddress = :address")
    void setNear(String address);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MyDevice d);

    @Delete
    void delete(MyDevice myDevice);

    @Update
    void update(MyDevice myDevice);
}
