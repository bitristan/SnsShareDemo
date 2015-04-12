package com.sun.tinker.sns.share.demo.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;

/**
 * 分享内容到新浪微博
 * <p>
 * 成功分享需要以下配置
 * </p>
 * <ul>
 * <li>
 * AndroidManifest.xml中需要配置相应的权限和{@link com.sina.weibo.sdk.component.WeiboSdkBrowser}
 * </li>
 * <li>
 * 若需要关注分享结果，则分享的Activity需要配置Action(com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY)
 * </li>
 * <li>
 * 若需要关注分享结果，需要分别在Activity的onCreate和onNewIntent中注册回调
 * </li>
 * <li>
 * 回调的IWeiboHandler.Response必须是一个Activity实例，否则无法成功回调
 * </li>
 * <li>
 * APP_KEY，包名和签名必须在开发者后台注册过且信息匹配才能调用成功
 * </li>
 * <li>
 * 使用微博分享不能finish掉分享的Activity，如需finish要在回调中finish，否则即使finish了之分享完成
 * 也会重新打开分享的Activity，并且没有分享结果的回调
 * </li>
 * </ul>
 *
 * @author tinker<sunting.bcwl@gmail.com>
 */
public class WeiboShareUtil extends BaseShareUtil {
    private static final String APP_KEY = "2045436852";
    private static final String REDIRECT_URL = "";
    private static final String SCOPE = "";

    private final IWeiboShareAPI mApi;

    public WeiboShareUtil(Activity activity) {
        super(activity);
        mApi = WeiboShareSDK.createWeiboAPI(mContext, APP_KEY);
        mApi.registerApp();
    }

    /**
     * 发送文字和图片消息到微博
     *
     * @param text       分享的文字内容
     * @param pictureUrl 分享的图片内容
     */
    public void sendMessage(String text, String pictureUrl) {
        Bitmap bitmap = null;
        if (!TextUtils.isEmpty(pictureUrl)) {
            bitmap = decodeFile(pictureUrl);
        }
        sendMessage(text, bitmap);
    }

    /**
     * 发送文字和图片消息到微博
     *
     * @param text   分享的文字内容
     * @param bitmap 分享的图片内容
     */
    public void sendMessage(String text, Bitmap bitmap) {
        Activity activity = mActivity.get();
        if (activity == null) {
            return;
        }

        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        boolean hasContent = false;

        if (!TextUtils.isEmpty(text)) {
            TextObject textObject = new TextObject();
            textObject.text = text;
            weiboMessage.textObject = textObject;
            hasContent = true;
        }

        if (bitmap != null) {
            ImageObject imageObject = new ImageObject();
            imageObject.setImageObject(bitmap);
            weiboMessage.imageObject = imageObject;
            hasContent = true;
        }

        if (!hasContent) {
            throw new IllegalArgumentException("Must have either text or image.");
        }

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        AuthInfo authInfo = new AuthInfo(mContext, APP_KEY, REDIRECT_URL, SCOPE);
        Oauth2AccessToken accessToken = WeiboAccessTokenKeeper.readAccessToken(mContext);
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        }
        mApi.sendRequest(activity, request, authInfo, token, new WeiboAuthListener() {

            @Override
            public void onWeiboException(WeiboException arg0) {
            }

            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                WeiboAccessTokenKeeper.writeAccessToken(mContext, newToken);
            }

            @Override
            public void onCancel() {
            }
        });
    }

    /**
     * <p>
     * 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
     * 需要调用 {@link com.sina.weibo.sdk.api.share.IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
     * 执行成功，返回 true，并调用 {@link com.sina.weibo.sdk.api.share.IWeiboHandler.Response#onResponse}；
     * 失败返回 false，不调用上述回调;
     * <p/>
     * <p>
     * 如果关注分享结果，需要在调用分享的Activity的onCreate方法中调用此方法
     * </p>
     */
    public void onCreate(Activity activity, Bundle savedInstanceState) {
        IWeiboHandler.Response response = getCallback();
        if (savedInstanceState != null && response != null) {
            mApi.handleWeiboResponse(activity.getIntent(), response);
        }
    }

    /**
     * <p>
     * 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
     * 来接收微博客户端返回的数据；执行成功，返回 true，并调用
     * {@link com.sina.weibo.sdk.api.share.IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调;
     * <p/>
     * <p>
     * 如果关注分享结果，需要在调用分享的Activity的onNewIntent中调用此方法
     * </p>
     */
    public void onNewIntent(Intent intent) {
        IWeiboHandler.Response response = getCallback();
        if (response != null) {
            mApi.handleWeiboResponse(intent, response);
        }
    }

    private IWeiboHandler.Response getCallback() {
        Activity activity = mActivity.get();
        if (activity instanceof IWeiboHandler.Response) {
            return (IWeiboHandler.Response) activity;
        }
        return null;
    }

    /**
     * 分享完成回调
     */
    public void onResponse(BaseResponse baseResponse) {
        switch (baseResponse.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                mCallback.onSuccess();
                finish();
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                mCallback.onCancel();
                finish();
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                mCallback.onError(baseResponse.errMsg);
                finish();
                break;
            default:
                finish();
                break;
        }
    }

    public static interface WeiboCallback extends IWeiboHandler.Response {
    }
}
