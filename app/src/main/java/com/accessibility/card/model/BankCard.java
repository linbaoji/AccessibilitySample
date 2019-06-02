package com.accessibility.card.model;

import com.accessibility.utils.Constants;

/**
 * <br/>@author linbaoji
 * <br/>@date 2019-06-02
 */
public class BankCard {
 //{"cardType":"DC","bank":"CCB","key":"6217003240019272242","messages":[],"validated":true,"stat":"ok"}

    private String cardType;
    private String bank;
    private String bankName;

    private String bankLogo;


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

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankLogo() {


        return Constants.ALIPAY_API_BANK_LOGO_URL +  bank;
    }

    public void setBankLogo(String bankLogo) {
        this.bankLogo = bankLogo;
    }


}