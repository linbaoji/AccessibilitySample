package com.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.accessibility.utils.AccessibilityLog;

import java.util.List;

/**
 * Created by popfisher on 2017/7/6.
 */

@TargetApi(16)
public class AccessibilitySampleService extends AccessibilityService {

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
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 此方法是在主线程中回调过来的，所以消息是阻塞执行的
        // 获取包名
        String pkgName = event.getPackageName().toString();
        int eventType = event.getEventType();
        AccessibilityOperator.getInstance().updateEvent(this, event);

        String className = event.getClassName().toString();
        String eventText = null;

        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventText = "Clicked: ";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventText = "Focused: ";
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: //窗口的内容发生变化，或者更具体的子树根布局变化事件
                eventText = "TYPE_WINDOW_CONTENT_CHANGED";
                // 窗口在这里未必 渲染完全

                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                eventText = "TYPE_NOTIFICATION_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://新的弹出层导致的窗口变化（dialog、menu、popupwindow）
                eventText = "TYPE_WINDOW_STATE_CHANGED";

                setPay(event);

                break;


        }

        AccessibilityLog.printLog("eventType: " + eventType + " pkgName: " + pkgName + "  eventText: " + eventText);
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 首页 设置支付金额开始
     * @param event
     */
    private void setPay(AccessibilityEvent event){


        String className = event.getClassName().toString();

        AccessibilityLog.printLog("className:" + className);

        if (className.equals("cn.vbill.operations.MainPlusActivity")) {

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
    }
}

///cn.vbill.pay.proceeds.swipe.SwipingCardPrepareActivity  "开始刷卡"