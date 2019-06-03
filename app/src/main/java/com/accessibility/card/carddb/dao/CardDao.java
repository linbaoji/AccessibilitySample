package com.accessibility.card.carddb.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.accessibility.card.carddb.model.Card;
import com.accessibility.utils.Constants;

import java.util.List;

/**
 * Created by Pavneet_Singh on 12/31/17.
 */

@Dao
public interface CardDao {

    @Query("SELECT * FROM " + Constants.TABLE_NAME_CARDS)
    List<Card> getCards();

    @Query("SELECT * FROM " + Constants.TABLE_NAME_CARDS + " WHERE STATE = 1")
    List<Card> getState1Cards();


    /*
     * Insert the object in database
     * @param Card, object to be inserted
     */
    @Insert
    long insertCard(Card card);

    /*
     * update the object in database
     * @param Card, object to be updated
     */
    @Update
    void updateCard(Card repos);

    /*
     * delete the object from database
     * @param Card, object to be deleted
     */
    @Delete
    void deleteCard(Card card);

    // Card... is varargs, here Card is an array
    /*
     * delete list of objects from database
     * @param Card, array of oject to be deleted
     */
    @Delete
    void deleteCards(Card... cards);

}
