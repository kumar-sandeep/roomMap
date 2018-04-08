package com.example.sandeep.sqlitemap;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by sandeep on 07-04-2018.
 */

public class PlaceViewModel extends AndroidViewModel {
    private LiveData<List<PlaceModel>> placeModelList;
    private PlaceRepository placeRepository;

    public PlaceViewModel(@NonNull Application application) {
        super(application);
        placeRepository = new PlaceRepository(application);
        placeModelList = placeRepository.getPlaceModelList();
    }

    LiveData<List<PlaceModel>> getPlaceModelList() {
        return placeModelList;
    }

    public void insert(PlaceModel placeModel) {
        placeRepository.insert(placeModel);
    }
}
