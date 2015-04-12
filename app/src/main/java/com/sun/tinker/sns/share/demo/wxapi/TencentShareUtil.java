package com.sun.tinker.sns.share.demo.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.File;

/**
 * 分享内容给QQ好友
 * <p>
 * 文档参见http://wiki.connect.qq.com/com-tencent-tauth-tencent-sharetoqq;
 * 成功分享需要以下配置
 * </p>
 * <ul>
 * <li>
 * 配置正确的APP_ID
 * </li>
 * <li>
 * 在AndroidManifest.xml中配置AuthActivity和AssistActivity
 * </li>
 * </ul>
 *
 * @author tinker<sunting.bcwl@gmail.com>
 */
public class TencentShareUtil extends BaseShareUtil {
    private static final String APP_ID = "222222";

    private final Tencent mApi;

    private IUiListener qqShareListener = new IUiListener() {
        @Override
        public void onCancel() {
            mCallback.onCancel();
        }

        @Override
        public void onComplete(Object response) {
            mCallback.onSuccess();
        }

        @Override
        public void onError(UiError e) {
            mCallback.onError(e.errorMessage);
        }
    };

    public TencentShareUtil(Activity activity) {
        super(activity);
        mApi = Tencent.createInstance(APP_ID, mContext);
    }

    /**
     * 分享图文消息
     *
     * @param title     标题，不能为空
     * @param targetUrl 消息跳转链接，不能为空
     * @param summary   分享的消息摘要，最长50个字
     * @param imageUrl  消息缩略图，支持本地图片和远程图片
     */
    public void sendTextMessage(String title, String targetUrl, String summary, String imageUrl) {
        Activity activity = mActivity.get();
        if (activity == null) {
            return;
        }

        if (TextUtils.isEmpty(title)) {
            throw new IllegalArgumentException("Title can not be empty.");
        }

        if (TextUtils.isEmpty(targetUrl)) {
            throw new IllegalArgumentException("Target url can not be empty.");
        }

        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        if (isUrlFromNetwork(imageUrl)) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        } else {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageUrl);
        }
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "测试应用222222");
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);

        mApi.shareToQQ(activity, params, qqShareListener);
    }

    /**
     * 分享纯图片
     * <p>
     * 只支持本地图片
     * </p>
     *
     * @param imageUrl 图片本地路径
     */
    public void sendImageMessage(String imageUrl) {
        Activity activity = mActivity.get();
        if (activity == null) {
            return;
        }

        if (TextUtils.isEmpty(imageUrl)) {
            throw new IllegalArgumentException("Image url can not be empty.");
        }

        File file = new File(imageUrl);
        if (!file.exists() || file.length() <= 0) {
            throw new IllegalArgumentException("Image must exist on local storage and be valid.");
        }

        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageUrl);
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);

        mApi.shareToQQ(activity, params, qqShareListener);
    }

    /**
     * 分享音乐
     *
     * @param title     音乐标题，不能为空
     * @param targetUrl 消息跳转链接，不能为空
     * @param summary   摘要
     * @param audioUrl  音乐文件的远程链接, 以URL的形式传入, 不支持本地音乐。
     * @param imageUrl  分享的图片URL
     */
    public void sendAudioMessage(String title, String targetUrl, String summary, String audioUrl, String imageUrl) {
        Activity activity = mActivity.get();
        if (activity == null) {
            return;
        }

        if (TextUtils.isEmpty(title)) {
            throw new IllegalArgumentException("Title can not be empty.");
        }

        if (TextUtils.isEmpty(targetUrl)) {
            throw new IllegalArgumentException("Target url can not be empty.");
        }

        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, audioUrl);
        if (isUrlFromNetwork(imageUrl)) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        } else {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageUrl);
        }
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);

        mApi.shareToQQ(activity, params, qqShareListener);
    }

}