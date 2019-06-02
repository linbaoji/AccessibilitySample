package com.accessibility.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.accessibility.AccessibilityOperator;
import com.accessibility.AccessibilitySXFOperator;
import com.accessibility.card.model.TimedPoint;
import com.accessibility.utils.AccessibilityLog;
import com.accessibility.utils.ToastTool;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by popfisher on 2017/7/6.
 */

@TargetApi(16)
public class AccessibilitySampleService extends AccessibilityService {

    public static final String DATABASE = "signature_list";

    public static Boolean isInSwipingCardPrepareActivity = false;// 是否在 "开始刷卡"页面

    public static Boolean isInSwipingCardReadCardActivity = false;// 是否在 "刷卡"页面


    public static Boolean isInMposPaySignatureActivity = false;// 是否在 "签名"页面

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();
        // 通过代码可以动态配置，但是可配置项少一点
//        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
//        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED
//                | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
//                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
//                | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
//        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
//        accessibilityServiceInfo.notificationTimeout = 0;
//        accessibilityServiceInfo.flags = AccessibilityServiceInfo.DEFAULT;
//        setServiceInfo(accessibilityServiceInfo);


        AccessibilitySXFOperator.getInstance().init(this);

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 此方法是在主线程中回调过来的，所以消息是阻塞执行的
        // 获取包名
        String pkgName = event.getPackageName().toString();
        int eventType = event.getEventType();

        AccessibilityOperator.getInstance().updateEvent(this, event);
        AccessibilitySXFOperator.getInstance().updateEvent(this, event);


        String className = event.getClassName().toString();
        String eventText = null;
        String id = null;

        AccessibilityLog.printLog("eventType 1: " + eventType + " pkgName: " + pkgName + "  className: " + className);

        AccessibilityNodeInfo nodeInfo = event.getSource();

        if (nodeInfo != null) {

            id = nodeInfo.getViewIdResourceName();

            AccessibilityLog.printLog("eventType 2: " + eventType + " pkgName: " + pkgName + "  id: " + id);
        }

        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventText = "Clicked: ";

                if ("com.accessibility:id/write_button".equals(id)) {
                    AccessibilitySXFOperator.getInstance().printSignature();
                }


                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventText = "Focused: ";
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: //窗口的内容发生变化，或者更具体的子树根布局变化事件
                eventText = "TYPE_WINDOW_CONTENT_CHANGED";
                // 窗口在这里未必 渲染完全
                if (isInSwipingCardPrepareActivity && className.equals("android.widget.ScrollView"))// 开始刷卡
                {


                    AccessibilityLog.printLog("SwipingCardPrepareActivity in content go :");
                    AccessibilitySXFOperator.getInstance().swipingCard(event);


                }else if(isInMposPaySignatureActivity && className.equals("android.widget.Button")
                && id.equals("com.vbill.shoushua.biz:id/btn_commit_pay")
                ){
                    AccessibilitySXFOperator.getInstance().commitPay(event);
                }

                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                eventText = "TYPE_NOTIFICATION_STATE_CHANGED";

                // 窗口在这里未必 渲染完全
                if (isInSwipingCardReadCardActivity && className.equals("android.widget.Toast"))// 开始刷卡
                {


                    List<AccessibilityNodeInfo> infosMeg = event.getSource().findAccessibilityNodeInfosByText("请挥卡,插卡或刷卡");
                    List<AccessibilityNodeInfo> infosPwd = event.getSource().findAccessibilityNodeInfosByText("请输入密码");

                    AccessibilityLog.printLog("SwipingCardPrepareActivity notification_state   infosMeg:" + infosMeg.size());

                    AccessibilityLog.printLog("SwipingCardPrepareActivity notification_state   infosPwd:" + infosPwd.size());

                }


                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://新的弹出层导致的窗口变化（dialog、menu、popupwindow）
                eventText = "TYPE_WINDOW_STATE_CHANGED";

                AccessibilityLog.printLog("className:" + className);

                isInSwipingCardPrepareActivity = false;
                isInSwipingCardReadCardActivity = false;
                isInMposPaySignatureActivity =  false;
                if (className.equals("cn.vbill.operations.MainPlusActivity")) {
                    AccessibilitySXFOperator.getInstance().setPay();
                } else if (className.equals("com.accessibility.card.SignatureViewActivity")) {

                    //drawSignature(event);
                } else if (className.equals("cn.vbill.pay.proceeds.swipe.SwipingCardPrepareActivity"))// 开始刷卡
                {
                    isInSwipingCardPrepareActivity = true;

                    TimerTask task = new TimerTask() {
                        public void run() {
                            AccessibilityLog.printLog("SwipingCardPrepareActivity go :");
                            //AccessibilitySXFOperator.getInstance().swipingCard();
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(task, 1000);


                } else if (className.equals("cn.vbill.pay.proceeds.swipe.SwipingCardReadCardActivity"))// 开始刷卡
                {
                    ToastTool.show(this, "通知设备刷卡");

                    isInSwipingCardReadCardActivity = true;
                } else if (className.equals("cn.vbill.pay.proceeds.result.MposPaySignatureActivity"))// 开始刷卡
                {


                    isInMposPaySignatureActivity =  true;
                    AccessibilityLog.printLog("MposPaySignatureActivity go :");
                    AccessibilitySXFOperator.getInstance().printSignature();
                }

                break;


        }

        AccessibilityLog.printLog("eventType 3: " + eventType + "  eventText: " + eventText);
    }


    @Override
    public void onInterrupt() {

    }

    /**
     * 首页 设置支付金额开始
     *
     * @param event
     */
    private void setPay(AccessibilityEvent event) {

        AccessibilityNodeInfo nodeInfoRoot = getRootInActiveWindow();


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
                    ClipData clip = ClipData.newPlainText("label", "1000");
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
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
    private void drawSignature(AccessibilityEvent event) {


        AccessibilityNodeInfo nodeInfoRoot = getRootInActiveWindow();


        if (nodeInfoRoot != null) {

            List<AccessibilityNodeInfo> infos = nodeInfoRoot.findAccessibilityNodeInfosByViewId("com.accessibility:id/signature_pad");

            AccessibilityLog.printLog("infos :" + infos.size());

            nodeInfoRoot.recycle();
            for (AccessibilityNodeInfo item : infos) {

                AccessibilityLog.printLog("infos className:" + item.getClassName() + " id:" + item.getViewIdResourceName());


                Rect rect = new Rect();
                item.getBoundsInScreen(rect);
                GestureDescription.Builder builder = new GestureDescription.Builder();

                AccessibilityLog.printLog(" Rect x:" + rect.left + " y:" + rect.top);
                AccessibilityLog.printLog(" Rect centerX:" + rect.centerX() + " centerY:" + rect.centerY());
                float x = rect.left + 395;
                float y = rect.top + 330;

                Path path = new Path();
                path.moveTo(x, y);
                x += 100;
                y += 100;

                path.lineTo(x, y);

                x += 100;
                //y = 100;

                path.lineTo(x, y);


                x += 100;
                //y += 100;

                path.lineTo(x, y);


                //x += 100;
                y += 100;

                path.lineTo(x, y);

                //x += 100;
                y += 100;

                path.lineTo(x, y);


                AccessibilityLog.printLog(" path x:" + x + " y:" + y);

                GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, 0, 500);

                builder.addStroke(strokeDescription);

                Path path1 = new Path();
                path1.moveTo(x, y);
                x += 100;
                y += 100;

                path1.lineTo(x, y);

                x += 100;
                //y = 100;

                path1.lineTo(x, y);


                x += 100;
                //y += 100;

                path1.lineTo(x, y);


                //x += 100;
                y += 100;

                path1.lineTo(x, y);

                //x += 100;
                y += 100;

                path1.lineTo(x, y);


                AccessibilityLog.printLog(" path x:" + x + " y:" + y);

                GestureDescription.StrokeDescription strokeDescription1 = new GestureDescription.StrokeDescription(path1, 1000, 500);

                builder.addStroke(strokeDescription1);


                GestureDescription gestureDescription = builder.build();


                AccessibilityLog.printLog("drawSignature: begin x:" + x + " y:" + y);

                boolean isDispatched = dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);
                    }
                }, null);


                //Toast.makeText(AccessibilitySampleService.this, "Was it dispatched? " + isDispatched, Toast.LENGTH_LONG).show();
                AccessibilityLog.printLog("Was it dispatched?: " + isDispatched);
            }
        }
    }

    @TargetApi(24)
    public void printSignature(AccessibilityEvent event) {

        // 获取SharedPreferences对象
        SharedPreferences sp = getSharedPreferences(DATABASE, Activity.MODE_MULTI_PROCESS);

        // 获取界面中的信息
        String key = "Signature";

        String jsonString = sp.getString(key, "");

        AccessibilityLog.printLog("TimedPoint :" + jsonString);

        final List<List> strokeList = JSONObject.parseArray(jsonString, List.class);

        if (strokeList == null || strokeList.size() == 0) {
            return;
        }


        AccessibilityNodeInfo nodeInfoRoot = getRootInActiveWindow();


        List<AccessibilityNodeInfo> infos = nodeInfoRoot.findAccessibilityNodeInfosByViewId("com.accessibility:id/signature_pad");

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

                duration = lastOne != null ? lastOne.timestamp + firstOne.timestamp : 0;
                duration = duration < 1 ? 1 : duration;


                GestureDescription.Builder builder = new GestureDescription.Builder();

                GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, startTime, duration);

                AccessibilityLog.printLog("builder.addStroke :  startTime: " + startTime + " duration:" + duration);


                builder.addStroke(strokeDescription);

                GestureDescription gestureDescription = builder.build();


                boolean isDispatched = dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
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


    @TargetApi(24)
    public void printSignature_old(AccessibilityEvent event) {

        // 获取SharedPreferences对象
        SharedPreferences sp = getSharedPreferences(DATABASE, Activity.MODE_MULTI_PROCESS);

        // 获取界面中的信息
        String key = "Signature";

        String jsonString = sp.getString(key, "");

        AccessibilityLog.printLog("TimedPoint :" + jsonString);
        List<TimedPoint> pointList = JSONObject.parseArray(jsonString, TimedPoint.class);

        if (pointList == null || pointList.size() == 0) {
            return;
        }


        AccessibilityNodeInfo nodeInfoRoot = getRootInActiveWindow();


        List<AccessibilityNodeInfo> infos = nodeInfoRoot.findAccessibilityNodeInfosByViewId("com.accessibility:id/signature_pad");

        AccessibilityLog.printLog("infos :" + infos.size());

        AccessibilityNodeInfo signaturePad = null;

        nodeInfoRoot.recycle();
        for (AccessibilityNodeInfo item : infos) {
            signaturePad = item;
        }

        Rect rect = new Rect();
        signaturePad.getBoundsInScreen(rect);


        AccessibilityLog.printLog(" signaturePad.getBoundsInScreen(rect) x:" + rect.left + " y:" + rect.top);

        TimedPoint lastOne = null;
        TimedPoint nextOne = null;
        TimedPoint firstOne = pointList != null ? pointList.get(0) : null;

        float x = rect.left;
        float y = rect.top;


        for (int i = 0; i < pointList.size(); i++) {

            TimedPoint timedPoint = pointList.get(i);
            if (i + 1 < pointList.size()) {
                nextOne = pointList.get(i + 1);
            } else {
                nextOne = null;
            }

            Path path = new Path();

            float mx = 0;
            float my = 0;

            float lx = 0;
            float ly = 0;


            if (timedPoint.action == MotionEvent.ACTION_DOWN) {

                continue;

            } else {

                lastOne = pointList.get(i + 1);

                mx = lastOne.x + x;
                my = lastOne.y + y;

                path.moveTo(mx, my);

                lx = timedPoint.x + x;
                ly = timedPoint.y + y;

                path.lineTo(lx, ly);


            }

            long duration = 1;  // 滑动持续时间
            long startTime = 0;// 滑动的开始时间
            duration = (lastOne != null ? timedPoint.timestamp + lastOne.timestamp : 0);
            startTime = firstOne != null ? lastOne.timestamp + firstOne.timestamp : 0;

            GestureDescription.Builder builder = new GestureDescription.Builder();

            AccessibilityLog.printLog("builder.addStroke : path mx:" + mx + " my:" + my + " line to mx:" + lx + " my:" + ly + " startTime: " + startTime + " duration:" + duration);


            builder.addStroke(new GestureDescription.StrokeDescription(path, startTime, duration));

            GestureDescription gestureDescription = builder.build();


            boolean isDispatched = dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                }
            }, null);


            //Toast.makeText(AccessibilitySampleService.this, "Was it dispatched? " + isDispatched, Toast.LENGTH_LONG).show();
            AccessibilityLog.printLog("Was it dispatched?: " + isDispatched);


        }


    }


}

///cn.vbill.pay.proceeds.swipe.SwipingCardPrepareActivity  "开始刷卡"