package com.ccojocea.aanews.data.local.dao;

import androidx.room.Insert;

import java.util.List;

import io.reactivex.Completable;

import static androidx.room.OnConflictStrategy.REPLACE;

public interface BaseDao<T> {

    @Insert(onConflict = REPLACE)
    Completable insert(T t);

    @Insert(onConflict = REPLACE)
    Completable insertAll(List<T> t);

}
