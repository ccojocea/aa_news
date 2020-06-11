package com.ccojocea.aanews.webview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ccojocea.aanews.common.BaseActivity;

public class WebViewActivity extends BaseActivity {

    //TODO Add Bookmark option here

    public static final String KEY_TITLE = "Title";
    public static final String KEY_URL = "Url";
    public static final String KEY_BACK = "GoBackToApp";
    public static final String URL_PRIVACY = "https://www.kia.com/us/en/privacy";
    public static final String URL_CONTACT_US = "https://ksupport.kiausa.com/ConsumerAffairs";
    public static final String URL_KIA_COM = "https://www.kia.com/us/en";
    //    public static final String URL_OTHER_SITES = "https://www.kia.com/us/en/content/other-kia-sites/kia-media";
    //    public static final String URL_MPG = "https://kiampginfo.com/ ";
    //    public static final String URL_DISCLAIMER = "https://www.kia.com/us/en/content/global-disclaimers/msrp";
    public static final String URL_TERMS = "https://www.kia.com/us/en/terms-of-service";
    public static final String URL_GENERAL_PRIVACY_LEGAL = "https://www.kia.com/us/en/privacy";
    public static final String URL_UVO_PRIVACY_LEGAL = "https://owners.kia.com/us/en/privacy-policy.html";
    public static final String URL_UVO_PRIVACY_LEGAL_TWELVE = "https://owners.kia.com/us/en/privacy-policy.html#twelve";
    public static final String URL_UVO_TERMS_OF_SERVICE = "https://owners.kia.com/us/en/terms-of-service.html";
    public static final String URL_GENERAL_TERMS_OF_USE = "https://www.kia.com/us/en/terms-of-service";
    public static final String URL_FINANCE_MAKE_PAYMENT = " https://www.kmfusa.com/";
    public static final String URL_FINANCE_KMFA_PROFILE = " https://www.kmfusa.com/account-profile";
    public static final String URL_FINANCE_KMFA_PROFILE_LOGIN = "https://www.kmfusa.com/Login/Index?ReturnUrl=%2faccount-profile";
    public static final String URL_LINK_FAQ = " https://owners.kia.com/us/en/faqs.html";
    public static final String URL_LINK_TROUBLESHOOTING = "https://owners.kia.com/us/en/uvo-troubleshooting.html";
    public static final String URL_KIA_OWNERS = "https://owners.kia.com";


    //    public static final String URL_SOCIAL_FACEBOOK = "https://www.facebook.com/kia";
    //    public static final String URL_SOCIAL_TWITTER = "https://twitter.com/Kia";
    //    public static final String URL_SOCIAL_PINTERST = "https://www.pinterest.com/kiamotorsusa/";
    //    public static final String URL_SOCIAL_INSTAGRAM = "https://www.instagram.com/kiamotorsusa/?hl=en";
    //    public static final String URL_SOCIAL_YOUTUBE = "https://www.youtube.com/user/KiaMotorsAmerica";

    private static final int DELAY = 500;

    public static final int REQUEST_WEB_WITH_BACK_RESULT = 1112;
    public static final int RESULT_BACK = 1113;

//    @Extra(KEY_BACK)
    protected boolean goBackToApp;

//    @Extra(KEY_TITLE)
    protected Integer resId;

//    @Extra(KEY_URL)
    protected String url;

//    @ViewById
    WebView webView;

//    @ViewById
    View blockingView;

    @SuppressLint("SetJavaScriptEnabled")
//    @AfterViews
    protected void init() {
        // enable javascript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewController());
        if (url != null && resId != null) {
//            setToolbarType(ToolbarType.BACK, resId, this);
            onBlockUserInteraction();
            webView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.copyBackForwardList().getCurrentIndex() > 0) {
            webView.goBack();
        } else {
            setResult(RESULT_BACK);
            finish();
        }
    }

//    @Override
    public void onBackButtonClicked() {
        if (goBackToApp) {
            setResult(RESULT_BACK);
            finish();
        } else {
            onBackPressed();
        }
    }

//    @Override
    public void onAllowUserInteraction() {
//        if (toolbar != null) {
//            toolbar.onAllowUserInteraction();
//        }
        blockingView.setVisibility(View.GONE);
    }

//    @Override
    public void onBlockUserInteraction() {
//        if (toolbar != null) {
//            toolbar.onBlockUserInteraction();
//        }
        blockingView.setVisibility(View.VISIBLE);
    }

    public class WebViewController extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            Logger.debug(getClass(), "WebViewDebug - shouldOverrideUrlLoading url: " + request.getUrl().toString());
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        //inProgress = true
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            Logger.debug(getClass(), "WebViewDebug - onPageStarted url: " + url);
            super.onPageStarted(view, url, favicon);
        }

        //inProgress = false - spinner loading
        @Override
        public void onPageFinished(WebView view, String url) {
//            Logger.debug(getClass(), "WebViewDebug - onPageFinished url: " + url);
            super.onPageFinished(view, url);
            //This is called way too early in the case of appointment requests since there are several redirects until the main page is reached
            //            new Handler().postDelayed(WebViewActivity.this::onAllowUserInteraction, delay);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//            Logger.debug(getClass(), "WebViewDebug - onReceivedError url: " + request.getUrl().toString());
            new Handler().postDelayed(WebViewActivity.this::onAllowUserInteraction, DELAY);
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
//            Logger.debug(getClass(), "WebViewDebug - onPageCommitVisible - url: " + url);
            super.onPageCommitVisible(view, url);
            new Handler().postDelayed(WebViewActivity.this::onAllowUserInteraction, DELAY);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
            String message = "SSL Certificate error.";
            switch (error.getPrimaryError()) {
                case SslError.SSL_UNTRUSTED:
                    message = "The certificate authority is not trusted.";
                    break;
                case SslError.SSL_EXPIRED:
                    message = "The Server certificate has expired.";
                    break;
                case SslError.SSL_IDMISMATCH:
                    message = "The certificate domain doesn't match.";
                    break;
                case SslError.SSL_NOTYETVALID:
                    message = "The certificate is not yet valid.";
                    break;
            }
            message += " Do you want to continue anyway?";
            builder.setTitle("SSL Certificate Error");
            builder.setMessage(message);
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                    onAllowUserInteraction();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if (view.getProgress() > 0) {
//                Logger.debug(getClass(), "WebViewDebug - onLoadResource - progress: " + view.getProgress());
            }
        }

    }

}
