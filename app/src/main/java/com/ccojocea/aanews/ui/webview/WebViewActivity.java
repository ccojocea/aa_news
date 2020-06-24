package com.ccojocea.aanews.ui.webview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.ui.common.BaseActivity;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.databinding.ActivityWebviewBinding;
import com.google.android.material.snackbar.Snackbar;

import timber.log.Timber;

public class WebViewActivity extends BaseActivity {

    private ActivityWebviewBinding binding;

    public static final String KEY_SOURCE_NAME = "Source";
    public static final String KEY_URL = "Url";
    public static final String KEY_SAVED = "Saved";

    private static final int WEB_DELAY = 200;
    private static final int DELAY = 500;
    private long lastClickTime;

    private String articleUrl;
    private boolean isSaved;

    private WebViewModel viewModel;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWebviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WebViewModel.class);

        // enable javascript
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        binding.webView.getSettings().setDomStorageEnabled(true);

        binding.webView.setWebViewClient(new WebViewController());

        Intent intent = getIntent();
        isSaved = intent.getBooleanExtra(KEY_SAVED, false);
        articleUrl = intent.getStringExtra(KEY_URL);
        String title = intent.getStringExtra(KEY_SOURCE_NAME);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (title != null) {
                setTitle(title);
            }
        }

        if (articleUrl != null) {
            binding.webView.loadUrl(articleUrl);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview_menu, menu);
        return true;
    }

    @Nullable
    private MenuItem share;

    @Nullable
    private MenuItem bookmark;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        share = menu.getItem(0);
        bookmark = menu.getItem(1);
        if (share != null && bookmark != null) {
            bookmark.setVisible(false);
            share.setVisible(false);
            if (isSaved) {
                bookmark.setIcon(R.drawable.ic_bookmark_selected);
            } else {
                bookmark.setIcon(R.drawable.ic_bookmark);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (SystemClock.elapsedRealtime() - lastClickTime < DELAY) {
            return true;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_bookmark:
                if (isSaved) {
                    isSaved = false;
                    item.setIcon(R.drawable.ic_bookmark);
                    viewModel.removeBookmarkedArticle();
                } else {
                    isSaved = true;
                    item.setIcon(R.drawable.ic_bookmark_selected);
                    viewModel.bookmarkArticle();
                }
                return true;
            case R.id.action_share:
                Utils.shareLink(this, articleUrl);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.webView.copyBackForwardList().getCurrentIndex() > 0) {
            binding.webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    public View getRoot() {
        return binding.getRoot();
    }

    public void allowUserInteraction() {
        if (!isFinishing() || !isDestroyed()) {
            //        binding.blockingView.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.INVISIBLE);
            if (share != null && bookmark != null) {
                share.setVisible(true);
                bookmark.setVisible(true);
            }
        }
    }

    public void blockUserInteraction() {
        if (!isFinishing() || !isDestroyed()) {
            //        binding.blockingView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
            if (share != null && bookmark != null) {
                share.setVisible(false);
                bookmark.setVisible(false);
            }
        }
    }

    public class WebViewController extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
//            view.loadUrl(request.getUrl().toString());
//            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            blockUserInteraction();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            allowUserInteraction();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (!WebViewActivity.this.isFinishing() || !WebViewActivity.this.isDestroyed()) {
                Utils.showSnackBar(
                        binding.getRoot(),
                        String.format(getString(R.string.error_code_description),
                                error.getErrorCode(),
                                error.getDescription()),
                        Snackbar.LENGTH_LONG, true);
            }
            new Handler().postDelayed(WebViewActivity.this::allowUserInteraction, WEB_DELAY);
        }



        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
            String message = getString(R.string.ssl_error);
            switch (error.getPrimaryError()) {
                case SslError.SSL_UNTRUSTED:
                    message = getString(R.string.ssl_error_untrusted);
                    break;
                case SslError.SSL_EXPIRED:
                    message = getString(R.string.ssl_error_expired);
                    break;
                case SslError.SSL_IDMISMATCH:
                    message = getString(R.string.ssl_error_mismatch);
                    break;
                case SslError.SSL_NOTYETVALID:
                    message = getString(R.string.ssl_error_not_yet_valid);
                    break;
            }
            message += getString(R.string.ssl_error_continue);
            builder.setTitle(getString(R.string.ssl_error));
            builder.setMessage(message);
            builder.setPositiveButton(R.string.ssl_button_continue, (dialog, which) -> handler.proceed());
            builder.setNegativeButton(R.string.ssl_button_cancel, (dialog, which) -> {
                handler.cancel();
                allowUserInteraction();
            });
            final AlertDialog dialog = builder.create();
            if (!WebViewActivity.this.isFinishing() || !WebViewActivity.this.isDestroyed()) {
                dialog.show();
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if (view.getProgress() > 0) {
                Timber.d("WebViewDebug - onLoadResource - progress: %s", view.getProgress());
            }
        }

    }

}
