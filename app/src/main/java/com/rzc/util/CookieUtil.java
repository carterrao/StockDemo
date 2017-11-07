package com.rzc.util;

import android.app.Activity;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by rzc on 17/11/7.
 */

public class CookieUtil {
    public static void getCookie(Activity activity, final String url,
                                 final OnCookieLoadedListener cookieLoadedListener) {
        final WebView webView = new WebView(activity);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (cookieLoadedListener != null) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    String cookie = cookieManager.getCookie(url);
                    if (!TextUtils.isEmpty(cookie)) {
                        webView.setWebViewClient(null);
                        cookieLoadedListener.onCookieLoaded(cookie);
                    }
                }
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    public interface OnCookieLoadedListener {
        void onCookieLoaded(String cookie);
    }
}
