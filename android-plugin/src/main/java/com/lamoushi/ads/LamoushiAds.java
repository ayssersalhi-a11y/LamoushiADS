package com.lamoushi.ads;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ConsoleMessage;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.widget.FrameLayout;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.HashSet;
import java.util.Set;

public class LamoushiAds extends GodotPlugin {
    private Activity activity;
    private WebView webView;
    private String currentZoneId;

    public LamoushiAds(Godot godot) {
        super(godot);
        this.activity = godot.getActivity();
    }

    @Override
    public String getPluginName() {
        return "LamoushiAds";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();
        signals.add(new SignalInfo("ad_debug", String.class));
        return signals;
    }

    @UsedByGodot
    public void loadBanner(final String zoneId) {
        activity.runOnUiThread(() -> {
            // إذا كان الإعلان موجوداً بنفس الـ zone نعيد إظهاره فقط
            if (webView != null && zoneId.equals(currentZoneId)) {
                webView.setVisibility(View.VISIBLE);
                return;
            }

            // إذا كان هناك إعلان قديم نزيله أولاً
            if (webView != null) {
                ((ViewGroup) webView.getParent()).removeView(webView);
                webView.destroy();
                webView = null;
            }

            currentZoneId = zoneId;
            webView = new WebView(activity);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.setBackgroundColor(Color.TRANSPARENT);

            // التقاط رسائل console.log من JavaScript
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage cm) {
                    emitSignal("ad_debug", "JS Console: " + cm.message() + " (line " + cm.lineNumber() + ")");
                    return true;
                }
            });

            // التقاط أخطاء تحميل الموارد (مثل فشل تحميل invoke.js)
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    emitSignal("ad_debug", "خطأ تحميل: " + error.getDescription() + " - URL: " + request.getUrl());
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    emitSignal("ad_debug", "انتهى تحميل الصفحة: " + url);
                }
            });

            String html = "<html><body style='margin:0;padding:0;display:flex;justify-content:center;'>"
                        + "<script type='text/javascript'>"
                        + "atOptions = { 'key' : '" + zoneId + "', 'format' : 'iframe', 'height' : 50, 'width' : 320, 'params' : {} };"
                        + "</script>"
                        + "<script type='text/javascript' src='//www.highperformanceformat.com/" + zoneId + "/invoke.js'></script>"
                        + "</body></html>";

            webView.loadDataWithBaseURL("https://www.highperformanceformat.com", html, "text/html", "UTF-8", null);

            // تحويل dp إلى px للتوافق مع جميع الشاشات
            int heightDp = 60;
            int heightPx = (int)(heightDp * activity.getResources().getDisplayMetrics().density);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightPx
            );
            params.gravity = Gravity.TOP;

            activity.addContentView(webView, params);
        });
    }

    @UsedByGodot
    public void showBanner() {
        activity.runOnUiThread(() -> {
            if (webView != null) {
                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    @UsedByGodot
    public void hideBanner() {
        activity.runOnUiThread(() -> {
            if (webView != null) {
                webView.setVisibility(View.GONE);
            }
        });
    }

    @UsedByGodot
    public void removeBanner() {
        activity.runOnUiThread(() -> {
            if (webView != null) {
                ((ViewGroup) webView.getParent()).removeView(webView);
                webView.destroy();
                webView = null;
                currentZoneId = null;
            }
        });
    }
}
