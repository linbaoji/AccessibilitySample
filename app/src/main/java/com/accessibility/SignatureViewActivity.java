package com.accessibility;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.accessibility.utils.AccessibilityLog;
import com.accessibility.utils.TimedPoint;
import com.accessibility.views.SignatureView;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SignatureViewActivity extends AppCompatActivity {
    public static final String DATABASE = "signature_list";

    private String creatTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature_view);
        gainPermission();
        gainCurrenTime();
        initView();
    }

    private void gainPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(SignatureViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SignatureViewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
            } else {
                Toast.makeText(this, "已开启权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void gainCurrenTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        creatTime = formatter.format(curDate);
    }

    private void initView() {
        final Button mClearButton = (Button) findViewById(R.id.clear_button);
        final Button mSaveButton = (Button) findViewById(R.id.save_button);
        final Button mSaveJsonButton = (Button) findViewById(R.id.saven_json_button);
        final Button mWriteButton = (Button) findViewById(R.id.write_button);

        final SignatureView mSignaturePad = (SignatureView) findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignatureView.OnSignedListener() {
            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
                mSaveJsonButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
                mSaveJsonButton.setEnabled(false);
            }
        });

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                Bitmap bitmap = compressScale(signatureBitmap);
                if (addSignatureToGallery(bitmap)) {
                    Toast.makeText(SignatureViewActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignatureViewActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSaveJsonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取SharedPreferences对象
                SharedPreferences sp = getSharedPreferences(DATABASE, Activity.MODE_MULTI_PROCESS);
                // 获取Editor对象
                SharedPreferences.Editor editor = sp.edit();
                // 获取界面中的信息
                String key = "Signature";
                String value = null;
                List<List<TimedPoint>> pointList = mSignaturePad.getStrokeList();
                String jsonString = JSONArray.toJSONString(pointList);

                AccessibilityLog.printLog("TimedPoint :" + jsonString);

                //List<List> strokeList = JSONArray.parseArray(jsonString,List.class);

                value = jsonString;
                editor.remove(key);
                editor.putString(key, value);
                editor.commit();


            }
        });

        mWriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AccessibilityLog.printLog("mWriteButton clicked " );

            }
        });

    }

    public File getAlbumStorageDir(String albumName) {
        File file = new File(Environment.getExternalStorageDirectory(), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public boolean addSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            final File photo = new File(getAlbumStorageDir("draw"), String.format(creatTime + ".jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photo);
            mediaScanIntent.setData(contentUri);
            SignatureViewActivity.this.sendBroadcast(mediaScanIntent);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *  
     *  * 图片按比例大小压缩方法 
     *  * 
     *  * @param image （根据Bitmap图片压缩） 
     *  * @return 
     *  
     */
    public static Bitmap compressScale(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //newOpts.inPreferredConfig = Bitmap.Config.RGB_565;//降低图片从ARGB888到RGB565
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        // return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 4:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "已打开权限！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请打开权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onResume() {


        super.onResume();

        // TODO Auto-generated method stub
        /**
         * 设置为横屏
         */
        if (false && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        AccessibilityLog.printLog("SignatureViewActivity onTouchEvent x " + event.getRawX() + " y" + event.getRawY());
        return super.onTouchEvent(event);
    }
}
