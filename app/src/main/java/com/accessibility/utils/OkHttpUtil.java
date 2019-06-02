package com.accessibility.utils;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * <br/>@author linbaoji
 * <br/>@date 2019-06-02
 */
public class OkHttpUtil {
    private static final String TAG = "OkHttpManager";
    //提交json数据
    private static final MediaType JSON = MediaType.parse(
            "application/json;charset=utf-8");
    //提交字符串数据
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse(
            "text/x-markdown;charset=utf-8");
    public final static int CONNECT_TIMEOUT = 60;
    public final static int READ_TIMEOUT = 100;
    public final static int WRITE_TIMEOUT = 60;

    private static OkHttpUtil mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler handler;


    private OkHttpUtil() {

        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
        handler = new Handler(Looper.getMainLooper());//主线程处理
    }

    public synchronized static OkHttpUtil getInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpUtil();
        }
        return mInstance;
    }


    public void getRequest(String url, final ResultCallback callback) {
        Request request = new Request.Builder().url(url).build();
        deliveryResult(callback, request);
    }

    private void postRequest(String url, final ResultCallback callback,
                             List<OkHttpParam> params) {
        Request request = buildPostRequest(url, params);
    }

    /**
     * post异步请求
     * josn参数
     *
     * @param url
     * @param callback
     * @param json
     */
    private void postRequest(String url, final ResultCallback callback,
                             String json) {
        Request request = buildPostRequest(url, json);
        deliveryResult(callback, request);
    }


    private void deliveryResult(final ResultCallback callback, Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                sendFailCallback(callback, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    sendSuccessCallBack(callback, response);
                } catch (final Exception e) {
                    sendFailCallback(callback, e);
                }
            }
        });
    }


    /**
     * 异步请求
     *
     * @param url
     * @param callBack
     */
    public void getUrl(String url, final HttpCallBack callBack) {
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callBack.onSuccess(response);
            }
        });
    }


    /**
     * 上传文件
     *
     * @param url
     * @param file
     * @param callback
     */
    public void postFile(String url, File file, Callback callback) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody requestBody = new MultipartBody.Builder().addFormDataPart("filename", file.getName(), fileBody).build();

        Request requestPostFile = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        mOkHttpClient.newCall(requestPostFile).enqueue(callback);
    }


    /**
     * 多个参数请求
     *
     * @param url
     * @param params
     * @return
     */
    private Request buildPostRequest(String url, List<OkHttpParam> params) {
        FormBody.Builder builder = new FormBody.Builder();
        for (OkHttpParam param : params) {
            builder.add(param.key, param.value);
        }
        RequestBody formBody = builder.build();
        return new Request.Builder().url(url).post(formBody).build();
    }

    /**
     * json参数
     *
     * @param url
     * @param json
     * @return
     */
    private Request buildPostRequest(String url, String json) {
        RequestBody requestBody = RequestBody.create(JSON, json);
        return new Request.Builder().url(url).post(requestBody).build();
    }


    private void sendFailCallback(final ResultCallback callback, final Exception e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }


    private void sendSuccessCallBack(final ResultCallback callback,
                                     final Response response) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }
        });
    }

    /**
     * get请求
     *  @param url      请求url
     * @param callback 请求回调
     */
    public static void get(String url, ResultCallback callback) {
        getInstance().getRequest(url, callback);
    }

    public static void get(String url) {
        get(url, new ResultCallback() {
            @Override
            public void onSuccess(Response response) {

            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }


    /**
     * post请求
     *
     * @param url      请求url
     * @param callback 请求回调
     * @param params   请求参数
     */
    public static void post(String url, List<OkHttpParam> params,
                            final ResultCallback callback) {
        getInstance().postRequest(url, callback, params);
    }

    /**
     * post请求
     *
     * @param url      请求url
     * @param callback 请求回调
     * @param json     请求json
     */
    public static void post(String url, String json,
                            final ResultCallback callback) {
        getInstance().postRequest(url, callback, json);
    }


    /**
     * http请求回调类,回调方法在UI线程中执行
     */
    public static abstract class ResultCallback {

        /**
         * 请求成功回调
         *
         * @param response
         */
        public abstract void onSuccess(Response response);

        /**
         * 请求失败回调
         *
         * @param e
         */
        public abstract void onFailure(Exception e);
    }

    /**
     * post请求参数类
     */
    public static class OkHttpParam {

        String key;
        String value;

        public OkHttpParam() {
        }

        public OkHttpParam(String key, String value) {
            this.key = key;
            this.value = value;
        }

    }

    /**
     * 请求回调接口
     */
    public interface HttpCallBack {
        void onSuccess(Response response);

        void onFailure(Exception e);
    }
}