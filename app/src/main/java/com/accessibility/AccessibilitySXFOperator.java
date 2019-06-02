package com.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.accessibility.card.carddb.model.Card;
import com.accessibility.card.model.TimedPoint;
import com.accessibility.utils.AccessibilityLog;
import com.accessibility.utils.Constants;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Created by popfisher on 2017/7/11.
 */

@TargetApi(16)
public class AccessibilitySXFOperator {

    private Context mContext;
    private static AccessibilitySXFOperator mInstance = new AccessibilitySXFOperator();
    private AccessibilityEvent mAccessibilityEvent;
    private AccessibilityService mAccessibilityService;

    private Card card;


    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String actionDataKey;//action_data_key


    private AccessibilitySXFOperator() {


    }

    public static AccessibilitySXFOperator getInstance() {
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;

        //获取SharedPreferences对象
        sp = context.getSharedPreferences(Constants.SHARE_PREFERENCES_DATABASE_NAME, Activity.MODE_MULTI_PROCESS);
        // 获取Editor对象
        editor = sp.edit();

        actionDataKey = Constants.SHARE_PREFERENCES_DATA_KEY;

        AccessibilityLog.printLog("AccessibilitySXFOperator.init: " + actionDataKey);


    }

    public void updateEvent(AccessibilityService service, AccessibilityEvent event) {
        if (service != null && mAccessibilityService == null) {
            mAccessibilityService = service;
        }
        if (event != null) {
            mAccessibilityEvent = event;
        }
    }

    public boolean isServiceRunning() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(Short.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : services) {
            if (info.service.getClassName().equals(mContext.getPackageName() + ".AccessibilitySampleService")) {
                return true;
            }
        }
        return false;
    }

    private AccessibilityNodeInfo getRootNodeInfo() {
        AccessibilityEvent curEvent = mAccessibilityEvent;
        AccessibilityNodeInfo nodeInfo = null;
        if (Build.VERSION.SDK_INT >= 16) {
            // 建议使用getRootInActiveWindow，这样不依赖当前的事件类型
            if (mAccessibilityService != null) {
                nodeInfo = mAccessibilityService.getRootInActiveWindow();
                AccessibilityLog.printLog("nodeInfo: " + nodeInfo);
                //nodeInfo.recycle();
            }
            // 下面这个必须依赖当前的AccessibilityEvent
//            nodeInfo = curEvent.getSource();
        } else {
            nodeInfo = curEvent.getSource();
        }
        return nodeInfo;
    }

    /**
     * 根据Text搜索所有符合条件的节点, 模糊搜索方式
     */
    public List<AccessibilityNodeInfo> findNodesByText(String text) {
        AccessibilityNodeInfo nodeInfo = getRootNodeInfo();
        if (nodeInfo != null) {
            return nodeInfo.findAccessibilityNodeInfosByText(text);
        }
        return null;
    }

    /**
     * 根据View的ID搜索符合条件的节点,精确搜索方式;
     * 这个只适用于自己写的界面，因为ID可能重复
     * api要求18及以上
     *
     * @param viewId
     */
    public List<AccessibilityNodeInfo> findNodesById(String viewId) {
        AccessibilityNodeInfo nodeInfo = getRootNodeInfo();
        if (nodeInfo != null) {
            if (Build.VERSION.SDK_INT >= 18) {
                return nodeInfo.findAccessibilityNodeInfosByViewId(viewId);
            }
        }
        return null;
    }

    public boolean clickByText(String text) {
        return performClick(findNodesByText(text));
    }

    /**
     * 根据View的ID搜索符合条件的节点,精确搜索方式;
     * 这个只适用于自己写的界面，因为ID可能重复
     * api要求18及以上
     *
     * @param viewId
     * @return 是否点击成功
     */
    public boolean clickById(String viewId) {
        return performClick(findNodesById(viewId));
    }

    private boolean performClick(List<AccessibilityNodeInfo> nodeInfos) {
        if (nodeInfos != null && !nodeInfos.isEmpty()) {
            AccessibilityNodeInfo node;
            for (int i = 0; i < nodeInfos.size(); i++) {
                node = nodeInfos.get(i);
                // 获得点击View的类型
                AccessibilityLog.printLog("View类型：" + node.getClassName());
                // 进行模拟点击
                if (node.isEnabled()) {
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
        return false;
    }

    public boolean clickBackKey() {
        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    private boolean performGlobalAction(int action) {
        return mAccessibilityService.performGlobalAction(action);
    }


    /**
     * 首页 设置支付金额开始
     */
    public void setPay() {


        String jsonString = sp.getString(actionDataKey, "");

        AccessibilityLog.printLog("card :" + jsonString);

        if (jsonString == null || "".equals(jsonString)) {
            return;
        }
        card = JSONObject.parseObject(jsonString, Card.class);


        AccessibilityNodeInfo nodeInfoRoot = getRootNodeInfo();


        if (nodeInfoRoot != null) {
            //收款金额
            List<AccessibilityNodeInfo> infos = nodeInfoRoot.findAccessibilityNodeInfosByViewId("com.vbill.shoushua.biz:id/id_money_et");
            //立即收款
            List<AccessibilityNodeInfo> infosClick = nodeInfoRoot.findAccessibilityNodeInfosByViewId("com.vbill.shoushua.biz:id/id_home_swipe_pay_iv");
            //知道了
            List<AccessibilityNodeInfo> infosIKnow = nodeInfoRoot.findAccessibilityNodeInfosByViewId("com.vbill.shoushua.biz:id/left_btn");

            AccessibilityLog.printLog("infos :" + infos.size());

            nodeInfoRoot.recycle();
            for (AccessibilityNodeInfo item : infos) {
                AccessibilityLog.printLog("infos className:" + item.getClassName() + " id:" + item.getViewIdResourceName());


                //item.performAction(AccessibilityNodeInfo.FOCUS_INPUT);
                if ("android.widget.EditText".equals(item.getClassName())) {
                    android.util.Log.i("maptrix", "==================");
                    Bundle arguments = new Bundle();
                    arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                            AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                    arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                            true);
                    item.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                            arguments);
                    item.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                    ClipData clip = ClipData.newPlainText("label", card.getPayment().toString());
                    ClipboardManager clipboardManager = (ClipboardManager) mAccessibilityService.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(clip);
                    item.performAction(AccessibilityNodeInfo.ACTION_PASTE);

                }

            }

            for (AccessibilityNodeInfo item : infosClick) {
                AccessibilityLog.printLog("infosClick className:" + item.getClassName());

                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            for (AccessibilityNodeInfo item : infosIKnow) {
                AccessibilityLog.printLog("infosClick className:" + item.getClassName());

                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);

            }
        }
        //

    }


    @TargetApi(24)
    public void printSignature() {

        //com.vbill.shoushua.biz:id/pv_signature
        //[0,415][1080,1959]
        //com.vbill.shoushua.biz:id/btn_up_sig
        //com.vbill.shoushua.biz:id/btn_commit_pay


        String jsonString = card.getSignature();

        AccessibilityLog.printLog("TimedPoint :" + jsonString);

        final List<List> strokeList = JSONObject.parseArray(jsonString, List.class);


        if (strokeList == null || strokeList.size() == 0) {
            return;
        }


        AccessibilityNodeInfo nodeInfoRoot = getRootNodeInfo();


        List<AccessibilityNodeInfo> infos = nodeInfoRoot.findAccessibilityNodeInfosByViewId("com.vbill.shoushua.biz:id/pv_signature");

        AccessibilityLog.printLog("infos :" + infos.size());

        AccessibilityNodeInfo signaturePad = null;

        nodeInfoRoot.recycle();
        for (AccessibilityNodeInfo item : infos) {
            signaturePad = item;
        }

        Rect rect = new Rect();
        signaturePad.getBoundsInScreen(rect);


        AccessibilityLog.printLog(" signaturePad.getBoundsInScreen(rect) x:" + rect.left + " y:" + rect.top);

        final float x = rect.left;
        final float y = rect.top;

//        final float x = 0;
//        final float y = 0;

        class DoDispatchGesture {

            TimedPoint firstOne = null;
            TimedPoint lastOne = null;

            int i = 0;


            public void doDispatchGesture() {

                AccessibilityLog.printLog("doDispatchGesture i : " + i + " mstrokeList.size:" + strokeList.size());


                if (i >= strokeList.size()) {

                    upLoadSignature();
                    return;
                }

                List<JSONObject> pointList = strokeList.get(i);

                Path path = new Path();


                for (int ni = 0; ni < pointList.size(); ni++) {


                    TimedPoint timedPoint = pointList.get(ni).toJavaObject(TimedPoint.class);


                    AccessibilityLog.printLog("timedPoint type:" + timedPoint.action + "  x: " + timedPoint.x + " y:" + timedPoint.y + " type:" + timedPoint.action + " time:" + timedPoint.timestamp);


                    if (timedPoint.action == MotionEvent.ACTION_DOWN) {

                        float mx = timedPoint.x + x;
                        float my = timedPoint.y + y;

                        path.moveTo(mx, my);

                        AccessibilityLog.printLog(" path.moveTo :  mx: " + mx + " my:" + my);


                    } else {
                        float lx = timedPoint.x + x;
                        float ly = timedPoint.y + y;

                        path.lineTo(lx, ly);
                        AccessibilityLog.printLog(" path.lineTo :  lx: " + lx + " ly:" + ly);
                    }

                    if (ni == 0) {
                        firstOne = timedPoint;
                    }


                    lastOne = timedPoint;

                }


                long duration = 1;  // 滑动持续时间
                long startTime = 0;// 滑动的开始时间

                duration = lastOne != null ? lastOne.timestamp - firstOne.timestamp : 0;
                duration = duration < 1 ? 1 : duration;


                GestureDescription.Builder builder = new GestureDescription.Builder();

                GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, startTime, duration);

                AccessibilityLog.printLog("builder.addStroke :  startTime: " + startTime + " duration:" + duration);


                builder.addStroke(strokeDescription);

                GestureDescription gestureDescription = builder.build();


                boolean isDispatched = mAccessibilityService.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);

                        AccessibilityLog.printLog("dispatchGesture:onCompleted i:" + i);

                        i++;
                        doDispatchGesture();
                    }
                }, null);


                //Toast.makeText(AccessibilitySampleService.this, "Was it dispatched? " + isDispatched, Toast.LENGTH_LONG).show();
                AccessibilityLog.printLog("Was it dispatched?: " + isDispatched);
            }
        }
        ;

        new DoDispatchGesture().doDispatchGesture();


    }

    public void swipingCard(AccessibilityEvent event) {
        //android:id="@id/btn_start_pay" //开始刷卡
        //  android:id="@id/tv_mcc_info"  //选择商品
//cn.vbill.pay.proceeds.swipe.SwipingCardPrepareActivity


        // List<AccessibilityNodeInfo>   infos= findNodesByText("开始刷卡");
        //List<AccessibilityNodeInfo> infos = findNodesById("com.vbill.shoushua.biz:id/btn_start_pay");


        List<AccessibilityNodeInfo> infos = event.getSource().findAccessibilityNodeInfosByViewId("com.vbill.shoushua.biz:id/btn_start_pay");

        AccessibilityNodeInfo nodeInfoRoot = getRootNodeInfo();
// 0 0 0 0 1 3

        AccessibilityLog.printLog("swipingCard nodeInfoRoot :" + (nodeInfoRoot != null ? nodeInfoRoot.getChildCount() : "null"));

        if (infos != null) {
            //收款金额
            // List<AccessibilityNodeInfo> infos = nodeInfoRoot.findAccessibilityNodeInfosByViewId("com.vbill.shoushua.biz:id/btn_start_pay");

            AccessibilityLog.printLog("swipingCard infos :" + (infos != null ? infos.size() : "null"));
            nodeInfoRoot.recycle();

            for (AccessibilityNodeInfo item : infos) {
                AccessibilityLog.printLog("swipingCard infos className:" + item.getClassName() + " id:" + item.getViewIdResourceName());
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);

            }

        }

    }

    private void upLoadSignature() {

        //com.vbill.shoushua.biz:id/btn_up_sig
        //com.vbill.shoushua.biz:id/btn_commit_pay

        //知道了
        List<AccessibilityNodeInfo> infos = getRootNodeInfo().findAccessibilityNodeInfosByViewId("com.vbill.shoushua.biz:id/btn_up_sig");

        AccessibilityLog.printLog("infos :" + infos.size());

        for (AccessibilityNodeInfo item : infos) {
            AccessibilityLog.printLog("infos className:" + item.getClassName() + " id:" + item.getViewIdResourceName());

            item.performAction(AccessibilityNodeInfo.ACTION_PASTE);


        }
    }

    public  void  commitPay(AccessibilityEvent event){
        event.getSource().performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }
}
