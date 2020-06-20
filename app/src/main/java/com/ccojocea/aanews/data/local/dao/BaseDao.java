package com.ccojocea.aanews.data.local.dao;

import androidx.room.Insert;

import java.util.List;

import io.reactivex.Completable;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

public interface BaseDao<T> {

    //TODO ASK - Used IGNORE instead of REPLACE so articles don't update from network if there's a conflict (isSaved override)

    @Insert(onConflict = REPLACE)
    Completable insert(T t);

    @Insert(onConflict = REPLACE)
    Completable insertAll(List<T> t);

}
