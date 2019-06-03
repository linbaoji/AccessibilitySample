package com.accessibility.utils;

/**
 * Created by Pavneet_Singh on 12/31/17.
 */

final public class Constants {
    private Constants() {
    }

    public static final String DB_NAME ="bankCardsdb.db";

    public static final String TABLE_NAME_CARDS ="cards";


    public  static final String ALIPAY_API_VALIDATEANDCACHECARDINFO_URL = "https://ccdcapi.alipay.com/validateAndCacheCardInfo.json?_input_charset=utf-8&cardNo=%s&cardBinCheck=true";
    public  static final String ALIPAY_API_BANK_LOGO_URL = " https://apimg.alipay.com/combo.png?d=cashier&t=";

}
