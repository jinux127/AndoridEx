package com.jointree.wifilist.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.jointree.wifilist.Entity.Address;

import java.util.List;

@Dao
public interface AddressDao {
    @Insert
    void insertAddress(Address... addresses);

    @Query("SELECT * FROM Address")
    LiveData<List<Address>> getAllAddress();

    @Query("SELECT * FROM Address")
    List<Address> getAddress();
}
