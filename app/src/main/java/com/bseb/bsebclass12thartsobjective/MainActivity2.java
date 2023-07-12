package com.bseb.bsebclass12thartsobjective;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // WebView
    String url = "";
    String burl = "";
    String aurl = "";
    String title = "";
    WebView webView;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView textView;
    ImageView imageView;
    Intent intent;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    CustomTabsIntent customTabsIntent;

    Loading loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        webView = findViewById(R.id.webview1);
        swipeRefreshLayout = findViewById(R.id.swipe1);
        textView = findViewById(R.id.setTitle);
        imageView = findViewById(R.id.back_btn);
        loading = new Loading(this);
        loading.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });
        loadWebView();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true);
        builder.setInstantAppsEnabled(true);
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.tra));
        customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage("com.android.chrome");
    }

    private void loadWebView() {

        webView.loadUrl(getIntent().getStringExtra("links"));
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
  /*              contentView.setVisibility(View.GONE);
                nocontent.setVisibility(View.VISIBLE);*/
                webView.setVisibility(View.GONE);
                super.onReceivedError(view, request, error);
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webView.setVisibility(View.VISIBLE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                //    Toast.makeText(MainActivity.this, consoleMessage.message(), Toast.LENGTH_SHORT).show();


                Uri uri = Uri.parse(consoleMessage.message());

                if (url.equals("true")) {
                    Intent intent = new Intent(MainActivity2.this, MainActivity2.class);
                    intent.putExtra("links", consoleMessage.message());
                    url = "false";
                    startActivity(intent);
                }

                if (consoleMessage.message().equals("url")) {
                    url = "true";
                }

                if (aurl.equals("true")) {
                    intent = new Intent(MainActivity2.this, MainActivity3.class);
                    intent.putExtra("links", consoleMessage.message());
                    aurl = "false";
                }
                if (consoleMessage.message().equals("aurl")) {
                    aurl = "true";
                }
                if (title.equals("true"))
                {
                    intent.putExtra("titel",consoleMessage.message());
                    title = "false";
                    startActivity(intent);
                }
                if (consoleMessage.message().equals("title"))
                {
                    title = "true";
                }

                if (burl.equals("true")) {

                    customTabsIntent.launchUrl(MainActivity2.this, uri);
                    burl = "false";

                }

                if (consoleMessage.message().equals("burl")) {
                    burl = "true";
                }

                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                textView.setText(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    loading.show();
                }
                if (newProgress == 100) {
                    loading.dismiss();
                }
            }

        });

        swipeRefreshLayout.setOnRefreshListener(this);

    }


    @Override
    public void onRefresh() {
        webView.reload();
    }
}