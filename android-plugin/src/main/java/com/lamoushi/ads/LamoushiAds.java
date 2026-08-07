package com.lamoushi.ads;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.webkit.ConsoleMessage;
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
    public String getPluginName() { return "LamoushiAds"; }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();
        signals.add(new SignalInfo("ad_debug", String.class));
        return signals;
    }

    @UsedByGodot
    public void loadBanner(final String zoneId) {
        activity.runOnUiThread(() -> {
            if (webView != null && zoneId.equals(currentZoneId)) {
                webView.setVisibility(View.VISIBLE);
                return;
            }

            if (webView != null) {
                ((ViewGroup) webView.getParent()).removeView(webView);
                webView.destroy();
                webView = null;
            }

            currentZoneId = zoneId;
            webView = new WebView(activity);
            
            // --- إعدادات المحرك الخارقة لمنع الحظر ---
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
            settings.setDatabaseEnabled(true);
            settings.setMediaPlaybackRequiresUserGesture(false);

            // تمويه الـ User-Agent (أهم خطوة لمنع كشف الـ App)
            String userAgent = settings.getUserAgentString();
            if (userAgent != null && userAgent.contains("; wv")) {
                settings.setUserAgentString(userAgent.replace("; wv", ""));
            }

            webView.setBackgroundColor(Color.TRANSPARENT);

            // تفعيل الكوكيز
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);

            // التقاط كل صغيرة وكبيرة في الـ Console
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage cm) {
                    emitSignal("ad_debug", "Console: " + cm.message() + " (Source: " + cm.sourceId() + ")");
                    return true;
                }
            });

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    emitSignal("ad_debug", "ERR: " + error.getDescription() + " | URL: " + request.getUrl());
                }

                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    emitSignal("ad_debug", "HTTP ERR: " + errorResponse.getStatusCode() + " | URL: " + request.getUrl());
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    emitSignal("ad_debug", "SSL ERR: Proceeding anyway.");
                    handler.proceed();
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    emitSignal("ad_debug", "Loaded: " + url);
                    // فحص نهائي للـ DOM
                    view.evaluateJavascript("(function(){ return 'iframes: ' + document.getElementsByTagName('iframe').length; })();", 
                        value -> emitSignal("ad_debug", "DOM Check: " + value));
                }
            });

            // كود HTML محسّن للتحميل
            String html = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"
                        + "<body style='margin:0;padding:0;background:transparent;'>"
                        + "<script>atOptions = {'key' : '" + zoneId + "','format' : 'iframe','height' : 50,'width' : 320,'params' : {}};</script>"
                        + "<script src='https://www.highperformanceformat.com/" + zoneId + "/invoke.js'></script>"
                        + "</body></html>";

            webView.loadDataWithBaseURL("https://www.highperformanceformat.com/", html, "text/html", "UTF-8", null);

            // عرض العنصر
            int heightPx = (int)(60 * activity.getResources().getDisplayMetrics().density);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
            params.gravity = Gravity.TOP;
            activity.addContentView(webView, params);
        });
    }

    @UsedByGodot
    public void showBanner() { activity.runOnUiThread(() -> { if (webView != null) webView.setVisibility(View.VISIBLE); }); }
    @UsedByGodot
    public void hideBanner() { activity.runOnUiThread(() -> { if (webView != null) webView.setVisibility(View.GONE); }); }
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
