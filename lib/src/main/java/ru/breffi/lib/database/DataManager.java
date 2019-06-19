package ru.breffi.lib.database;

import android.util.Log;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DataManager extends RealmTransactionHelper {
    public static final String TAG = "DataManager";
    private Realm realm;

    public DataManager() {
//        openConnection();
    }

    public <T extends RealmModel> Observable<Boolean> insertOrUpdateAllDataAsync(List<T> collections) {
        Log.e(TAG, "insertOrUpdateAllDataAsync start: " + Thread.currentThread().toString());
        return executeTransactionAsync(realm, realmInstance -> {
            Log.e(TAG, "insertOrUpdateAllDataAsync execute: " + Thread.currentThread().toString());
            realmInstance.insertOrUpdate(realmInstance.copyToRealmOrUpdate(collections));
        });
    }

    public <T extends RealmModel> Observable<Boolean> insertOrUpdateObjectAsync(T object) {
        Log.e(TAG, "insertOrUpdateObjectAsync start: " + Thread.currentThread().toString());
        return executeTransactionAsync(realm, realmInstance -> {
            Log.e(TAG, "insertOrUpdateObjectAsync execute: " + Thread.currentThread().toString());
            realmInstance.insertOrUpdate(realmInstance.copyToRealmOrUpdate(object));
        });
    }

    public <T extends RealmModel> void insertOrUpdateObject(T object) {
        Log.e(TAG, "insertOrUpdateObject start: " + Thread.currentThread().toString());
        executeTransaction(realmInstance -> {
            Log.e(TAG, "insertOrUpdateObject execute: " + Thread.currentThread().toString());
            realmInstance.insertOrUpdate(realmInstance.copyToRealmOrUpdate(object));
        });
    }

    public <T extends RealmModel> Observable<Boolean> deleteObjectByIdAsync(Class<T> clazz, Integer objectId) {
        Log.e(TAG, "deleteObjectByIdAsync start: " + Thread.currentThread().toString());
        return executeTransactionAsync(realm, realmInstance -> {
            Log.e(TAG, "deleteObjectByIdAsync execute: " + Thread.currentThread().toString());
            RealmResults<T> realmResults = realmInstance.where(clazz).equalTo("id", objectId).findAll();
            realmResults.deleteFirstFromRealm();
        });
    }

    public <T extends RealmModel> void deleteObjectByFieldSync(Class<T> clazz, String field, String value) {
        Log.e(TAG, "deleteObjectByFieldSync start: " + Thread.currentThread().toString());
        executeTransaction(realmInstance -> {
            Log.e(TAG, "deleteObjectByFieldSync execute: " + Thread.currentThread().toString());
            RealmResults<T> realmResults = realmInstance.where(clazz).equalTo(field, value).findAll();
            realmResults.deleteFirstFromRealm();
        });
    }

    public <T extends RealmModel> Observable<Boolean> deleteObjectByIdAsync(Class<T> clazz, String objectId) {
        Log.e(TAG, "deleteObjectByIdAsync start: " + Thread.currentThread().toString());
        return executeTransactionAsync(realm, realmInstance -> {
            Log.e(TAG, "deleteObjectByIdAsync execute: " + Thread.currentThread().toString());
            RealmResults<T> realmResults = realmInstance.where(clazz).equalTo("id", objectId).findAll();
            realmResults.deleteFirstFromRealm();
        });
    }

    public <T extends RealmModel> void deleteObjectsSync(Class<T> clazz) {
        Log.e(TAG, "deleteObjectByIdAsync start: " + Thread.currentThread().toString());
        executeTransactionSync(realmInstance -> {
            Log.e(TAG, "deleteObjectByIdAsync execute: " + Thread.currentThread().toString());
            RealmResults<T> realmResults = realmInstance.where(clazz).findAll();
            realmResults.deleteAllFromRealm();
        });
    }

    public <T extends RealmModel> Observable<Boolean> deleteObjectsAsync(Class<T> clazz) {
        Log.e(TAG, "deleteObjectByIdAsync start: " + Thread.currentThread().toString());
        return executeTransactionAsync(realm, realmInstance -> {
            Log.e(TAG, "deleteObjectByIdAsync execute: " + Thread.currentThread().toString());
            RealmResults<T> realmResults = realmInstance.where(clazz).findAll();
            realmResults.deleteAllFromRealm();
        });
    }

    public <T extends RealmModel> void insertOrUpdateAllDataSync(List<T> collections) {
        Log.e(TAG, "insertOrUpdateAllDataSync start: " + Thread.currentThread().toString());
        executeTransactionSync(realmInstance -> {
            Log.e(TAG, "insertOrUpdateAllDataSync execute: " + Thread.currentThread().toString());
            realmInstance.insertOrUpdate(realmInstance.copyToRealmOrUpdate(collections));
        });
    }

    /**
     * If you want to get simple data call copyFromRealm
     */
    public <T extends RealmModel> Observable<RealmResults<T>> selectAllRealmData(Class<T> clazz) {
        Log.e(TAG, "selectAllRealmData start: " + Thread.currentThread().toString());
        return asObservable(realm.where(clazz).findAll());
    }

    @SuppressWarnings("SameParameterValue")
    public <T extends RealmModel> List<T> copyFromRealm(RealmResults<T> data, boolean fromAnotherThread) {
        if (fromAnotherThread) {
            closeConnection();
            try {
                setRealm(Realm.getDefaultInstance());
                return getRealm().copyFromRealm(data);
            } finally {
                if (!getRealm().isClosed()) {
                    getRealm().close();
                }
            }
        } else {
            return getRealm().copyFromRealm(data);
        }
    }

    public <T extends RealmModel> T copyFromRealm(T data, boolean fromAnotherThread) {
        if (fromAnotherThread) {
            setRealm(null);
            try {
                setRealm(Realm.getDefaultInstance());
                return getRealm().copyFromRealm(data);
            } finally {
                if (!getRealm().isClosed()) {
                    getRealm().close();
                }
            }
        } else {
            return getRealm().copyFromRealm(data);
        }
    }

    public Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Realm openConnection() {
        realm = Realm.getDefaultInstance();
        return realm;
    }

    public void closeConnection() {
        closeConnection(realm);
    }
}
