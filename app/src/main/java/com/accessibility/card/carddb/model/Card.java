package com.accessibility.card.carddb.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.accessibility.utils.Constants;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by linbaoji 2019年05月29日
 */

@Entity(tableName = Constants.TABLE_NAME_CARDS)
public class Card implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long card_id;

    @ColumnInfo(name = "note_content")
    // column name will be "note_content" instead of "content" in table
    private String holder;

    private String number;

    private String bank;

    private String cardType;

    private String password;


    private Integer payment;

    private Float poundage;

    private String signature;

    private Date date;

    public Card(String number, String bank, String cardType, String holder, String password, Integer payment, Float poundage, String signature, Date date) {
        //this.card_id = card_id;
        this.bank = bank;
        this.cardType = cardType;
        this.holder = holder;
        this.number = number;
        this.password = password;
        this.payment = payment;
        this.poundage = poundage;
        this.signature = signature;
        this.date = date;
    }

//
//    public Card( String holder, String number) {
//        this.holder = holder;
//        this.number = number;
//
//        this.date = new Date(System.currentTimeMillis());
//
//    }


    @Ignore
    public Card() {
    }

    public long getCard_id() {
        return card_id;
    }

    public void setCard_id(long card_id) {
        this.card_id = card_id;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPayment() {
        return payment;
    }

    public void setPayment(Integer payment) {
        this.payment = payment;
    }

    public Float getPoundage() {
        return poundage;
    }

    public void setPoundage(Float poundage) {
        this.poundage = poundage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;

        Card note = (Card) o;

        if (card_id != note.card_id) return false;
        return number != null ? number.equals(note.number) : note.number == null;
    }


    @Override
    public int hashCode() {
        int result = (int) card_id;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }

}
