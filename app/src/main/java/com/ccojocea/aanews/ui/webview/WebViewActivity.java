package com.ccojocea.aanews.ui.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.IntentCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ccojocea.aanews.R;
import com.ccojocea.aanews.common.Utils;
import com.ccojocea.aanews.databinding.ActivityWebviewBinding;
import com.ccojocea.aanews.models.entity.ArticleEntity;
import com.ccojocea.aanews.ui.common.BaseActivity;
import com.google.android.material.snackbar.Snackbar;

import timber.log.Timber;

public class WebViewActivity extends BaseActivity {

    private static final String KEY_ARTICLE = "Article";

    private static final int DELAY = 500;
    private long lastClickTime;

    private ActivityWebviewBinding binding;
    private WebViewModel viewModel;

    @Nullable
    private ArticleEntity article;

    @Nullable
    private MenuItem bookmark;

    public static Intent newIntent(@NonNull Context context, @NonNull ArticleEntity article) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(KEY_ARTICLE, article);
        return intent;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWebviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets(binding.getRoot(), binding.appBar, binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WebViewModel.class);

        // enable javascript
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        binding.webView.getSettings().setDomStorageEnabled(true);

        binding.webView.setWebViewClient(new WebViewController());

        article = IntentCompat.getSerializableExtra(getIntent(), KEY_ARTICLE, ArticleEntity.class);
        if (article == null) {
            Timber.e("WebViewActivity started without an article");
            finish();
            return;
        }
        viewModel.setArticle(article);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(article.getSource().getName());
        }

        // the bookmark state is read from the database, so it is correct even if it was changed somewhere else
        viewModel.getSavedData().observe(this, isSaved -> updateBookmarkIcon());

        if (savedInstanceState == null) {
            binding.webView.loadUrl(article.getUrl());
        } else {
            binding.webView.restoreState(savedInstanceState);
        }

        // navigate back through the web history first, then leave the screen
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            binding.webView.saveState(outState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview_menu, menu);
        bookmark = menu.findItem(R.id.action_bookmark);
        updateBookmarkIcon();
        return true;
    }

    private void updateBookmarkIcon() {
        if (bookmark != null) {
            bookmark.setIcon(viewModel.isSaved() ? R.drawable.ic_bookmark_selected : R.drawable.ic_bookmark);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        if (SystemClock.elapsedRealtime() - lastClickTime < DELAY) {
            return true;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        if (itemId == R.id.action_bookmark) {
            viewModel.toggleBookmark();
            return true;
        } else if (itemId == R.id.action_share) {
            if (article != null) {
                Utils.shareLink(this, article.getUrl());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding.webView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public View getRoot() {
        return binding.getRoot();
    }

    private boolean isAlive() {
        return !isFinishing() && !isDestroyed();
    }

    private void showProgress(boolean isLoading) {
        if (isAlive()) {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public class WebViewController extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showProgress(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            showProgress(false);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            // errors for images, ads, trackers etc. are reported here too, only the page itself matters
            if (!request.isForMainFrame()) {
                return;
            }
            showProgress(false);
            if (isAlive()) {
                Utils.showSnackBar(
                        binding.getRoot(),
                        getString(R.string.error_code_description, error.getErrorCode(), error.getDescription()),
                        Snackbar.LENGTH_LONG, true);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // never proceed on an invalid certificate, the page could be served by anyone
            handler.cancel();
            showProgress(false);
            if (!isAlive()) {
                return;
            }
            String message;
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
                default:
                    message = getString(R.string.ssl_error);
                    break;
            }
            Utils.showSnackBar(binding.getRoot(), message, Snackbar.LENGTH_LONG, true);
        }

    }

}
