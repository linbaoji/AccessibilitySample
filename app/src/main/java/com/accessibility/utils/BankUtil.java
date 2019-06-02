package com.accessibility.utils;

import android.app.Activity;
import android.util.Log;

import com.accessibility.card.model.BankCard;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import okhttp3.Response;

/**
 * <br/>@author linbaoji
 * <br/>@date 2019-06-02
 */
public class BankUtil {


    private static String readBankData(Activity activity) {
        InputStreamReader inputStreamReader;
        String resultString = null;
        try {
            inputStreamReader = new InputStreamReader(activity.getAssets().open("bankname.json"), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStreamReader.close();
            bufferedReader.close();
            resultString = stringBuilder.toString();
            Log.i("TAG", stringBuilder.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultString;
    }

    private static String getBankName(Activity activity, String bankCode) {

        JSONObject bankMap = JSONObject.parseObject(readBankData(activity));

        return bankMap.getString(bankCode);
    }


    public static void getBanckNameByCard(final Activity activity, String cardId, final CallBack callBack) {

        //{"cardType":"DC","bank":"CCB","key":"6217003240019272242","messages":[],"validated":true,"stat":"ok"}
        String url = String.format(Constants.ALIPAY_API_VALIDATEANDCACHECARDINFO_URL, cardId.trim().replace(" ", ""));
        OkHttpUtil.get(url, new OkHttpUtil.ResultCallback() {
            @Override
            public void onSuccess(Response response) {

                //得到服务器返回的具体内容
                try {
                    String responseData = response.body().string();
                    JSONObject bankJson = JSONObject.parseObject(responseData);

                    if (bankJson != null && bankJson.getString("stat").equals("ok") && bankJson.getBoolean("validated")) {
                        BankCard bankCard = new BankCard();
                        String bank = bankJson.getString("bank");
                        bankCard.setBank(bank);
                        bankCard.setCardType(bankJson.getString("cardType"));
                        bankCard.setBankName(getBankName(activity, bank));

                        if (callBack != null) {
                            callBack.getBankOk(bankCard);
                        }

                    }


                } catch (IOException e) {
                    AccessibilityLog.e(e);
                }


            }

            @Override
            public void onFailure(Exception e) {

            }
        });


    }

    /**
     * 请求回调接口
     */
    public interface CallBack {
        void getBankOk(BankCard bankCard);
    }


    public enum CardTypeEnum {
        DC("储蓄卡"), CC("信用卡"), SCC("准贷记卡"), PC("预付费卡");

        public String backName;

        CardTypeEnum(String name) {
            this.backName = name;
        }

    }

}