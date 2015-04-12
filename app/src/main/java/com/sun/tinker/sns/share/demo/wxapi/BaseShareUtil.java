package com.sun.tinker.sns.share.demo.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sun.tinker.sns.share.demo.R;
import com.sun.tinker.sns.share.demo.utils.ImageUtil;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * @author tinker<sunting.bcwl@gmail.com>
 */
public abstract class BaseShareUtil {
    public static final String EXTRA_TEXT = "extra_text";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_BITMAP = "extra_bitmap";

    private static final float BITMAP_SCALE = 2.0f / 3;

    protected final static String TAG = "ShareUtil";

    protected final Context mContext;
    protected final WeakReference<Activity> mActivity;

    protected Callback mCallback;

    public BaseShareUtil(Activity activity) {
        mContext = activity.getApplicationContext();
        mActivity = new WeakReference<Activity>(activity);

        mCallback = new SimpleCallbackAdapter();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * 判断是本地文件路径还是网络url，目前暂认为网络url只包含http和https协议开头的
     *
     * @return
     */
    protected boolean isUrlFromNetwork(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith("http") || url.startsWith("https");
    }

    /**
     * 获取解析图片的建议宽度，取设备宽度的2/3
     *
     * @return
     */
    private int getRecommendedImageWidth() {
        return (int) (mContext.getResources().getDisplayMetrics().widthPixels * BITMAP_SCALE);
    }

    /**
     * 获取解析图片的建议高度，取设备高度的2/3
     *
     * @return
     */
    private int getRecommendedImageHeight() {
        return (int) (mContext.getResources().getDisplayMetrics().heightPixels * BITMAP_SCALE);
    }

    protected Bitmap decodeFile(String pictureUrl) {
        int reqWidth = getRecommendedImageWidth();
        int reqHeight = getRecommendedImageHeight();
        return ImageUtil.decodeSampledBitmapFromFile(pictureUrl, reqWidth, reqHeight);
    }

    protected Bitmap decodeStream(InputStream stream) {
        int reqWidth = getRecommendedImageWidth();
        int reqHeight = getRecommendedImageHeight();
        return ImageUtil.decodeSampledBitmapFromStream(stream, reqWidth, reqHeight);
    }

    protected Bitmap decodeResource(int imageRes) {
        int reqWidth = getRecommendedImageWidth();
        int reqHeight = getRecommendedImageHeight();
        return ImageUtil.decodeSampledBitmapFromResource(mContext.getResources(), imageRes, reqWidth, reqHeight);
    }

    protected void finish() {
        Activity activity = mActivity.get();
        if (activity != null) {
            activity.finish();
        }
    }

    /**
     * 跳转到分享界面
     *
     * @param context
     * @param imageUrl 分享的图片
     */
    public static void jumpToShareActivity(Context context, String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            Toast.makeText(context, R.string.tip_empty_image, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(context, WXEntryActivity.class);
        intent.putExtra(EXTRA_IMAGE_URL, imageUrl);
        context.startActivity(intent);
    }

    /**
     * 跳转到分享界面
     *
     * @param context
     * @param bitmap  分享的图片
     */
    public static void jumpToShareActivity(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(context, R.string.tip_empty_image, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(context, WXEntryActivity.class);
        intent.putExtra(EXTRA_BITMAP, bitmap);
        context.startActivity(intent);
    }

    public class SimpleCallbackAdapter implements Callback {

        @Override
        public void onError(String errorMsg) {
            Log.d(TAG, "Error on share: " + errorMsg);
            Toast.makeText(mContext, R.string.share_error, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess() {
            Toast.makeText(mContext, R.string.share_success, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(mContext, R.string.share_cancel, Toast.LENGTH_SHORT).show();
        }
    }

    public static interface Callback {
        public void onError(String errorMsg);

        public void onSuccess();

        public void onCancel();
    }

}
