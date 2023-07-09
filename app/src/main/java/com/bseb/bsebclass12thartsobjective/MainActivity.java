package com.bseb.bsebclass12thartsobjective;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener {

    // WebView
    String url = "";
    WebView webView;
    SwipeRefreshLayout swipeRefreshLayout;
    Loading loading;
    // Drawer Menu
    ImageView imageBtn;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseRemoteConfig firebaseRemoteConfig;
    private SharedPreferences sharedPreferences;

    ReviewManager manager;
    ReviewInfo reviewInfo;
    private Date FutureDate, future;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipe);
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigation);
        imageBtn = findViewById(R.id.menu_btn);
        loading = new Loading(this);
        loading.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        sharedPreferences = this.getSharedPreferences("MyMain", MODE_PRIVATE);
        HashMap<String, Object> map = new HashMap<>();
        map.put(RemoteUtil.bseb_class_12th_arts_objective, BuildConfig.VERSION_CODE);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(map);
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                     DialogShow();
                    }
                });


        //RETRIVE THE DATE
        long millis = sharedPreferences.getLong("THE_FUTURE_DATE", 0);
        FutureDate = new Date(millis);

        final Handler handler = new Handler();

        Calendar calendar1 = Calendar.getInstance();
        Date today = calendar1.getTime();

        if (today.after(FutureDate)) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    StartReviewFlow();
                }
            }, 5000);

        }

        RunOnlyOnce();
        ActivateReViewInfo();


        OneSignal.setNotificationOpenedHandler(new OneSignal.OSNotificationOpenedHandler() {
            @Override
            public void notificationOpened(OSNotificationOpenedResult osNotificationOpenedResult) {
                OSNotification notification = osNotificationOpenedResult.getNotification();
                JSONObject jsonObject = notification.getAdditionalData();

                if (jsonObject != null)
                {
                    String urls = jsonObject.optString("url","None");
                    Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                    intent.putExtra("links",urls);
                    startActivity(intent);
                }
                else {

                }

            }
        });


        loadWebView();
        navigationDrawer();
    }

    private void DialogShow() {
        if (firebaseRemoteConfig.getLong(RemoteUtil.bseb_class_12th_arts_objective) <= BuildConfig.VERSION_CODE) return;
        CustomUpdateDialog dialog = new CustomUpdateDialog(MainActivity.this, firebaseRemoteConfig);
        dialog.show();
    }


    private void RunOnlyOnce() {

        String FirstTime = sharedPreferences.getString("FirstTimeInstall", "");
        assert FirstTime != null;
        if (FirstTime.equals("")) {

            Log.d("TAG", "onCreate: FUT1 : ");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 2);
            future = calendar.getTime();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("THE_FUTURE_DATE", future.getTime());
            editor.apply();
            //Log.d(TAG, "onCreate: FUT : " + FutureDate);
            SharedPreferences.Editor editor1 = sharedPreferences.edit();
            editor1.putString("FirstTime Install", "Yes");
            editor1.apply();
        }

    }



    private void ActivateReViewInfo() {

        manager = ReviewManagerFactory.create(this);
        com.google.android.play.core.tasks.Task<ReviewInfo> reviewInfoTask = manager.requestReviewFlow();
        reviewInfoTask.addOnCompleteListener(new com.google.android.play.core.tasks.OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(@NonNull com.google.android.play.core.tasks.Task<ReviewInfo> task) {


                if (task.isSuccessful()) {
                    reviewInfo = task.getResult();
                } else {

                }

            }
        });


    }



    private void StartReviewFlow() {

        if (reviewInfo != null) {

            com.google.android.play.core.tasks.Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
            flow.addOnCompleteListener(new com.google.android.play.core.tasks.OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull com.google.android.play.core.tasks.Task<Void> task) {
                    Toast.makeText(MainActivity.this, "Thanks for your Review..", Toast.LENGTH_SHORT).show();
                }
            });

            //ADD TWO DAYS TO THE PRESENT DAY TO BRING UP THE IN APP DIALOG IN THE FUTURE AFTER TWO DAYS
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 2);
            future = calendar.getTime();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("THE_FUTURE_DATE", future.getTime());
            editor.apply();

        }

    }




    private void loadWebView() {

        webView.loadUrl("https://bsebtarget.com/BSEB/Class%2012th%20Arts/Objective/Objective%20Question.php");
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


                if (url.equals("true")) {
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    intent.putExtra("links", consoleMessage.message());
                    url = "false";
                    startActivity(intent);
                }

                if (consoleMessage.message().equals("url")) {
                    url = "true";
                }
                return super.onConsoleMessage(consoleMessage);
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


    private void navigationDrawer() {

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        //  navigationView.setCheckedItem(R.id.share);

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    // Navigation Drawer item click
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {

            case R.id.more:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=8534749914765371345"));
                startActivity(intent);
                break;

            case R.id.share:
                Intent intent1 = new Intent(Intent.ACTION_SEND);
                intent1.setType("text/plain");
                intent1.putExtra(Intent.EXTRA_TEXT,"Download this app using this link..\n\n https://play.google.com/store/apps/details?id="+ getPackageName());
                startActivity(intent1);
                break;

            case R.id.rate:

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));

                }catch (ActivityNotFoundException e)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
                }

                break;

            case R.id.privacy:
                Intent intent2 = new Intent(Intent.ACTION_VIEW,Uri.parse("https://howtodoanythinka.blogspot.com/2023/06/12th-arts-question-bank.html"));
                startActivity(intent2);
                break;

            case R.id.exit:
                finishAffinity();
                break;

        }

        return true;
    }

    // It is for close navigation drawer
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerVisible(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else
            super.onBackPressed();
    }

}