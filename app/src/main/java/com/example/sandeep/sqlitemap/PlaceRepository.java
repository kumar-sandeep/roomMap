package com.example.sandeep.sqlitemap;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by sandeep on 08-04-2018.
 */

public class PlaceRepository {

    private PlaceDao placeDao;
    private LiveData<List<PlaceModel>> placeModelList;
    private Long returnedValue;

    PlaceRepository(Application application) {
        AppDatabase appDatabase = AppDatabase.getDatabase(application);
        placeDao = appDatabase.placeDao();
        placeModelList = placeDao.getAllPlaces();
    }

    LiveData<List<PlaceModel>> getPlaceModelList() {
        return placeModelList;
    }

    public Long insert(PlaceModel placeModel) {
        try {
            returnedValue = new insertAsyncTask(placeDao).execute(placeModel).get();
            Log.d("Inside Insert()", " retunedValue = " + returnedValue);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return returnedValue;
    }

    private static class insertAsyncTask extends AsyncTask<PlaceModel, Void, Long> {

        private PlaceDao mAsyncTaskDao;

        insertAsyncTask(PlaceDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Long doInBackground(final PlaceModel... params) {
            Long returnValue = mAsyncTaskDao.insert(params[0]);
            Log.d("Async", " retunedValue = " + returnValue);
            /*try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            return returnValue;
        }

        @Override
        protected void onPostExecute(Long returnedValue) {
            super.onPostExecute(returnedValue);
        }
    }

}
