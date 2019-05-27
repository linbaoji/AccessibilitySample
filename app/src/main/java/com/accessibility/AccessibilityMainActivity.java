package com.accessibility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AccessibilityMainActivity extends Activity implements View.OnClickListener {

    private View mOpenSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility_main);
        initView();
        AccessibilityOperator.getInstance().init(this);
    }

    private void initView() {
        mOpenSetting = findViewById(R.id.open_accessibility_setting);
        mOpenSetting.setOnClickListener(this);
        findViewById(R.id.accessibility_find_and_click).setOnClickListener(this);
        findViewById(R.id.accessibility_sxf).setOnClickListener(this);
        findViewById(R.id.accessibility_signature).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.open_accessibility_setting:
                OpenAccessibilitySettingHelper.jumpToSettingPage(this);
                break;
            case R.id.accessibility_find_and_click:
                startActivity(new Intent(this, AccessibilityNormalSampleActivity.class));
                break;

            case R.id.accessibility_sxf:
                Intent intent = new Intent();
                //cn.vbill.operations.ad.StartUpAdvertisementActivity
                //cn.vbill.operations.MainPlusActivity
                intent.setClassName("com.vbill.shoushua.biz", "cn.vbill.operations.xinaliance.ThirdPayActivity");
                startActivity(intent);
                break;


            case R.id.accessibility_signature:
                startActivity(new Intent(AccessibilityMainActivity.this,SignatureViewActivity.class));
                break;

        }
    }
}
