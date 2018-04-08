package com.example.sandeep.sqlitemap;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.google.android.gms.location.places.Place;

import java.util.List;

/**
 * Created by sandeep on 07-04-2018.
 */

@Dao
public interface PlaceDao {

    @Query("SELECT * FROM PlaceModel")
    LiveData<List<PlaceModel>> getAllPlaces();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PlaceModel placeModels);
}
