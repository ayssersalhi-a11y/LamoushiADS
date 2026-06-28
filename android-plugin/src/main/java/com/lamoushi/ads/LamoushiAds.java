package com.lamoushi.ads;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;

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
            webView.setWebViewClient(new WebViewClient());

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
