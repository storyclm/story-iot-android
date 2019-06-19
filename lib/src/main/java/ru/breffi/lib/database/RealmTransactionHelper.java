package ru.breffi.lib.database;

import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;

@SuppressWarnings({"WeakerAccess", "unused"})
public class RealmTransactionHelper {

    /**
     * Use this method for INSERT, UPDATE, DELETE queries
     */
    protected Observable<Boolean> executeTransactionAsync(Realm realm, Realm.Transaction transaction) {
        PublishSubject<Boolean> subject = PublishSubject.create();
        Realm.getDefaultInstance().executeTransactionAsync(transaction, () -> subject.onNext(true), subject::onError);
        return subject.doOnNext(v -> Realm.getDefaultInstance().close());
    }

    /**
     * Use this method for INSERT, UPDATE, DELETE queries
     */
    protected void executeTransactionSync(Realm.Transaction transaction) {
        PublishSubject<Boolean> subject = PublishSubject.create();
        Realm.getDefaultInstance().executeTransaction(transaction);
        Realm.getDefaultInstance().close();
    }

    protected Observable executeTransaction(Realm.Transaction transaction) {
        PublishSubject subject = PublishSubject.create();
        try {
            Realm.getDefaultInstance().executeTransaction(transaction);
            subject.onComplete();
        } catch (Exception e) {
            subject.onError(e);
            e.printStackTrace();
        }
        finally {
            Realm.getDefaultInstance().close();
        }
        return subject;
    }

    protected void closeConnection(Realm realm) {
        if (Realm.getDefaultInstance().isClosed()) {
            return;
        }
        Realm.getDefaultInstance().close();
    }

    /**
     * Wrapper for RxJava2 Observable
     * Use this method for SELECT queries
     */
    public <T> Observable<T> asObservable(T data) {
        return Observable.create(e -> {
            e.onNext(data);
            Log.e(DataManager.TAG, "selectAllRealmData execute: " + Thread.currentThread().toString());
            e.onComplete();
        });
    }
}
