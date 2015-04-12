package com.sun.tinker.sns.share.demo.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;

import com.sun.tinker.sns.share.demo.utils.ImageUtil;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

import java.io.IOException;
import java.net.URL;

/**
 * 分享内容到微信好友或朋友圈
 * <p>
 * 成功分享需要一下配置
 * </p>
 * <ul>
 * <li>
 * 若要得到回调必须在当前包名下建立.wxapi.WXEntryActivity，并在其中注册相应回调
 * </li>
 * <li>
 * APP_KEY，包名和签名必须在开发者后台注册过且信息匹配才能调用成功
 * </li>
 * </ul>
 *
 * @author tinker<sunting.bcwl@gmail.com>
 */
public class WechatShareUtil extends BaseShareUtil {
    private static final String APP_ID = "wxd930ea5d5a258f4f";

    private static final int THUMB_SIZE = 150;

    private final IWXAPI mApi;

    public WechatShareUtil(Activity activity) {
        super(activity);
        mApi = WXAPIFactory.createWXAPI(mContext, APP_ID, false);
        mApi.registerApp(APP_ID);
    }

    /**
     * 发送文字内容
     *
     * @param text 文字内容
     * @param isTimeline true为分享到朋友圈，false为微信好友
     */
    public void sendTextMessage(String text, boolean isTimeline) {
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = isTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        // 调用api接口发送数据到微信
        mApi.sendReq(req);
    }

    /**
     * 发送图片内容
     *
     * @param imageUrl 图片地址
     * @param isTimeline true为分享到朋友圈，false为微信好友
     */
    public void sendImageMessage(String imageUrl, boolean isTimeline) {
        Bitmap bitmap = null;
        if (!TextUtils.isEmpty(imageUrl)) {
            if (isUrlFromNetwork(imageUrl)) {
                try {
                    bitmap = decodeStream(new URL(imageUrl).openStream());
                } catch (IOException e) {
                }
            } else {
                bitmap = decodeFile(imageUrl);
            }
        }
        sendImageMessage(bitmap, isTimeline);
    }

    /**
     * 发送图片内容
     *
     * @param imgRes 图片资源id
     * @param isTimeline true为分享到朋友圈，false为微信好友
     */
    public void sendImageMessage(int imgRes, boolean isTimeline) {
        Bitmap bitmap = decodeResource(imgRes);
        sendImageMessage(bitmap, isTimeline);
    }

    /**
     * 发送图片内容
     *
     * @param bitmap 图片
     * @param isTimeline true为分享到朋友圈，false为微信好友
     */
    public void sendImageMessage(Bitmap bitmap, boolean isTimeline) {
        if (bitmap == null) {
            throw new IllegalArgumentException("Image must not be empty.");
        }

        WXImageObject imgObj = new WXImageObject(bitmap);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
        bitmap.recycle();

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        msg.thumbData = ImageUtil.bitmap2Bytes(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = isTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        mApi.sendReq(req);
    }

    /**
     * <p>
     * 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），需要重新
     * 注册回调
     * <p/>
     * <p>
     * 如果关注分享结果，需要在调用分享的Activity的onCreate方法中调用此方法
     * </p>
     */
    public void onCreate(Activity activity, Bundle savedInstanceState) {
        IWXAPIEventHandler callback = getCallback();
        if (callback != null) {
            mApi.handleIntent(activity.getIntent(), callback);
        }
    }

    /**
     * <p>
     * 从当前应用唤起微信并进行分享后，返回到当前应用时，需要在此处调用该函数
     * 来接收微信客户端返回的数据
     * <p/>
     * <p>
     * 如果关注分享结果，需要在调用分享的Activity的onNewIntent中调用此方法
     * </p>
     */
    public void onNewIntent(Intent intent) {
        IWXAPIEventHandler callback = getCallback();
        if (callback != null) {
            mApi.handleIntent(intent, callback);
        }
    }

    private IWXAPIEventHandler getCallback() {
        Activity activity = mActivity.get();
        if (activity instanceof IWXAPIEventHandler) {
            return (IWXAPIEventHandler) activity;
        }
        return null;
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * 默认的分享完成回调
     */
    public void onResponse(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                mCallback.onSuccess();
                finish();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                mCallback.onCancel();
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                mCallback.onError(resp.errStr);
                finish();
                break;
            default:
                finish();
                break;
        }
    }

    public static interface WechatCallback extends IWXAPIEventHandler {
    }
}