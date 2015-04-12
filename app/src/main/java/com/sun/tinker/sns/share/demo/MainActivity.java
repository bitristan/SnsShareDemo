package com.sun.tinker.sns.share.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.sun.tinker.sns.share.demo.wxapi.BaseShareUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends ActionBarActivity {

    private String mShareImageUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.share);
        File temp = new File(getApplicationContext().getExternalCacheDir(), "share.jpg");
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(temp));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (temp.exists()) {
            mShareImageUrl = temp.getAbsolutePath();
        }

        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseShareUtil.jumpToShareActivity(MainActivity.this, mShareImageUrl);
            }
        });
    }

}
