package com.lamoushi.ads;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
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

            // ==== تفعيل الكوكيز (ضروري جداً لتسليم إعلان Adsterra الفعلي) ====
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);
            emitSignal("ad_debug", "تم تفعيل الكوكيز (أولى + طرف ثالث) ✅");
            // ==================================================================

            // التقاط رسائل console.log من JavaScript (بما فيها سكربت invoke.js نفسه)
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage cm) {
                    String level = cm.messageLevel() != null ? cm.messageLevel().toString() : "UNKNOWN";
                    emitSignal("ad_debug", "JS Console [" + level + "]: " + cm.message()
                            + " (المصدر: " + cm.sourceId() + ", السطر: " + cm.lineNumber() + ")");
                    return true;
                }
            });

            // التقاط أخطاء تحميل الموارد + تفاصيل إضافية لكل طلب يمر عبر الـ WebView
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    emitSignal("ad_debug", "خطأ تحميل: " + error.getDescription() + " - URL: " + request.getUrl());
                }

                @Override
                public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                    emitSignal("ad_debug", "بدأ تحميل الصفحة: " + url);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    emitSignal("ad_debug", "انتهى تحميل الصفحة: " + url);

                    // فحص إضافي: هل تم إنشاء الـ iframe الخاص بالإعلان فعلياً داخل الصفحة؟
                    view.evaluateJavascript(
                        "(function(){ " +
                        "  var iframes = document.getElementsByTagName('iframe');" +
                        "  return 'عدد الـ iframes: ' + iframes.length + ' | محتوى body: ' + document.body.innerHTML.length + ' حرف';" +
                        "})();",
                        value -> emitSignal("ad_debug", "فحص DOM: " + value)
                    );
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    emitSignal("ad_debug", "طلب تنقل/تحميل: " + request.getUrl());
                    return false; // نسمح للطلب يكمل عادي
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
