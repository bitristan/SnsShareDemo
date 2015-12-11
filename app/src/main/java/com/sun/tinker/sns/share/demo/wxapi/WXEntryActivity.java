package com.sun.tinker.sns.share.demo.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sun.tinker.sns.share.demo.R;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * <p>
 * 直接调起此Activity用于分享操作
 * </p>
 * <p>
 * 因为微信分享必须在应用包名下建立.wxapi.WXEntryActivity来接收分享结果，所以使用这个特定的Activity
 * </p>
 *
 * @author tinker<sunting.bcwl@gmail.com>
 */
public class WXEntryActivity extends Activity implements WeiboShareUtil.WeiboCallback,
        WechatShareUtil.WechatCallback, View.OnClickListener {

    private ImageButton mShareToWechatFriends;
    private ImageButton mShareToWechatTimeline;
    private ImageButton mShareToSinaWeibo;
    private ImageButton mShareToTencentQq;
    private Button mCancelBtn;

    private TencentShareUtil mTencentShareUtil;
    private WeiboShareUtil mWeiboShareUtil;
    private WechatShareUtil mWechatShareUtil;

    private String mText;
    private String mImageUrl;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);
        setContentView(R.layout.activity_sns_share);

        mTencentShareUtil = new TencentShareUtil(this);
        mWeiboShareUtil = new WeiboShareUtil(this);
        mWechatShareUtil = new WechatShareUtil(this);

        mWeiboShareUtil.onCreate(this, savedInstanceState);
        mWechatShareUtil.onCreate(this, savedInstanceState);

        initUi();
        initData();
    }

    private void initUi() {
        mShareToSinaWeibo = (ImageButton) findViewById(R.id.iv_sina_weibo);
        mShareToTencentQq = (ImageButton) findViewById(R.id.iv_tencent_qq);
        mShareToWechatFriends = (ImageButton) findViewById(R.id.iv_wechat_friend);
        mShareToWechatTimeline = (ImageButton) findViewById(R.id.iv_wechat_timeline);
        mCancelBtn = (Button) findViewById(R.id.btn_cancel);

        mShareToSinaWeibo.setOnClickListener(this);
        mShareToTencentQq.setOnClickListener(this);
        mShareToWechatFriends.setOnClickListener(this);
        mShareToWechatTimeline.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);

        findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mText = intent.getStringExtra(BaseShareUtil.EXTRA_TEXT);
            mImageUrl = intent.getStringExtra(BaseShareUtil.EXTRA_IMAGE_URL);
            mBitmap = intent.getParcelableExtra(BaseShareUtil.EXTRA_BITMAP);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        mWeiboShareUtil.onNewIntent(intent);
        mWechatShareUtil.onNewIntent(intent);
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp baseResp) {
        mWechatShareUtil.onResponse(baseResp);
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        mWeiboShareUtil.onResponse(baseResponse);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_sina_weibo:
                shareToSinaWeibo();
                break;
            case R.id.iv_wechat_friend:
                shareToWechat(false);
                break;
            case R.id.iv_wechat_timeline:
                shareToWechat(true);
                break;
            case R.id.iv_tencent_qq:
                shareToTencentQq();
                break;
            case R.id.btn_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    private void shareToWechat(boolean isTimeline) {
        if (mBitmap != null) {
            mWechatShareUtil.sendImageMessage(mBitmap, isTimeline);
        } else if (!TextUtils.isEmpty(mImageUrl)) {
            mWechatShareUtil.sendImageMessage(mImageUrl, isTimeline);
        } else {
            Toast.makeText(getApplicationContext(), R.string.tip_empty_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToSinaWeibo() {
        if (mBitmap != null) {
            mWeiboShareUtil.sendMessage(mText, mBitmap);
        } else if (!TextUtils.isEmpty(mImageUrl)) {
            mWeiboShareUtil.sendMessage(mText, mImageUrl);
        } else {
            Toast.makeText(getApplicationContext(), R.string.tip_empty_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToTencentQq() {
        if (!TextUtils.isEmpty(mImageUrl)) {
            mTencentShareUtil.sendImageMessage(mImageUrl);
        } else if (mBitmap != null) {
            // TODO maybe ANR in this place. Should better not put IO operation on main thread.
            File f = new File(getExternalCacheDir(), "tencent_share_temp.png");
            try {
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(f));
            } catch (FileNotFoundException e) {
            }
            if (f.exists()) {
                mTencentShareUtil.sendImageMessage(f.getAbsolutePath());
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.tip_empty_image, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}