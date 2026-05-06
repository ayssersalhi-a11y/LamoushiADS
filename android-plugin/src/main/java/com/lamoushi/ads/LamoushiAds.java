package com.lamoushi.ads;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
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
            if (webView != null) return;

            webView = new WebView(activity);
            
            // إعدادات المتصفح ليعمل الإعلان بشكل صحيح
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setBackgroundColor(Color.TRANSPARENT); // جعل الخلفية شفافة
            webView.setWebViewClient(new WebViewClient());

            // كود HTML لعرض إعلان Adsterra باستخدام الـ Zone ID
            String html = "<html><body style='margin:0;padding:0;display:flex;justify-content:center;'>"
                        + "<script type='text/javascript'>"
                        + "atOptions = { 'key' : '" + zoneId + "', 'format' : 'iframe', 'height' : 50, 'width' : 320, 'params' : {} };"
                        + "</script>"
                        + "<script type='text/javascript' src='//www.highperformanceformat.com/" + zoneId + "/invoke.js'></script>"
                        + "</body></html>";

            webView.loadDataWithBaseURL("https://www.highperformanceformat.com", html, "text/html", "UTF-8", null);

            // تحديد مكان الإعلان (في الأعلى)
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                150 // ارتفاع تقريبي ليناسب البانر
            );
            params.gravity = Gravity.TOP;

            activity.addContentView(webView, params);
        });
    }
}
