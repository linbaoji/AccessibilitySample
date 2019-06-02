package com.accessibility.card.carddb;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.accessibility.card.carddb.dao.CardDao;
import com.accessibility.card.carddb.model.Card;
import com.accessibility.utils.Constants;
import com.accessibility.card.util.DateRoomConverter;

/**
 * Created by Pavneet_Singh on 12/31/17.
 */

@Database(entities = { Card.class}, version = 1)
@TypeConverters({DateRoomConverter.class})
public abstract class CardDatabase extends RoomDatabase {

    private static CardDatabase noteDB;

    // synchronized is use to avoid concurrent access in multithred environment
    public static /*synchronized*/ CardDatabase getInstance(Context context) {
        if (null == noteDB) {
            noteDB = buildDatabaseInstance(context);
        }
        return noteDB;
    }

    private static CardDatabase buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context,
                CardDatabase.class,
                Constants.DB_NAME).allowMainThreadQueries().build();
    }

    public abstract CardDao getCardDao();
    public  void cleanUp(){
        noteDB = null;
    }
}