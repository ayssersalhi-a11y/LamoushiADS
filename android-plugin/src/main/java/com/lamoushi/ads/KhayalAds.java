package com.khayal.ads;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
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
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.FrameLayout;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.HashSet;
import java.util.Set;

public class KhayalAds extends GodotPlugin {

    private Activity activity;
    private WebView webView;
    private String currentZoneId;

    public KhayalAds(Godot godot) {
        super(godot);
        this.activity = godot.getActivity();
    }

    @Override
    public String getPluginName() {
        return "KhayalAds";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();

        // أبقينا الـSignal حتى لا تتغير واجهة الـPlugin
        // لكنه لن يرسل مخرجات من هذا الكود
        signals.add(new SignalInfo("ad_debug", String.class));

        return signals;
    }

    @Override
    public void onMainDestroy() {
        removeBanner();
    }

    @UsedByGodot
    public void loadBanner(final String zoneId) {

        activity.runOnUiThread(() -> {

            if (webView != null && zoneId.equals(currentZoneId)) {
                webView.setVisibility(View.VISIBLE);
                return;
            }

            removeBanner();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            currentZoneId = zoneId;
            webView = new WebView(activity);

            WebSettings settings = webView.getSettings();

            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);

            settings.setMixedContentMode(
                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );

            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);

            String userAgent = settings.getUserAgentString();

            settings.setUserAgentString(
                userAgent.replace("; wv", "")
            );

            webView.setBackgroundColor(Color.TRANSPARENT);

            CookieManager cookieManager =
                CookieManager.getInstance();

            cookieManager.setAcceptCookie(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                cookieManager.setAcceptThirdPartyCookies(
                    webView,
                    true
                );
            }

            // ========================================================
            // WebChromeClient
            // لا توجد مخرجات
            // ========================================================

            webView.setWebChromeClient(new WebChromeClient() {

                @Override
                public boolean onConsoleMessage(
                    ConsoleMessage cm
                ) {

                    // تم حذف إخراج JS فقط.
                    // لا نغير سلوك WebView.

                    return true;
                }
            });

            // ========================================================
            // WebViewClient
            // ========================================================

            webView.setWebViewClient(new WebViewClient() {


                @Override
                public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
                ) {

                    // تم حذف الإخراج فقط.
                    // WebView نفسه يستمر في التعامل مع الخطأ.
                }

                @Override
                public void onReceivedHttpError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceResponse errorResponse
                ) {

                    // تم حذف الإخراج فقط.
                }

                @Override
                public void onReceivedSslError(
                    WebView view,
                    SslErrorHandler handler,
                    SslError error
                ) {

                    // السلوك الأصلي محفوظ:
                    // قبول شهادة SSL
                    handler.proceed();
                }

                @Override
                public void onPageFinished(
                    WebView view,
                    String url
                ) {

                    // الفحص الأصلي محفوظ
                    view.evaluateJavascript(
                        "(function(){ return document.body.innerHTML.length; })();",

                        value -> {

                            try {

                                if (
                                    Integer.parseInt(value) < 50
                                ) {

                                    // الصفحة شبه فارغة
                                    // لا يتم إخراج شيء
                                }

                            } catch (Exception ignored) {
                            }
                        }
                    );
                }
            });

            // ========================================================
            // HTML الإعلان
            // لم يتغير
            // ========================================================

            String html =
                "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' "
                + "content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "body{"
                + "margin:0;"
                + "padding:0;"
                + "background:transparent;"
                + "overflow:hidden;"
                + "}"
                + "</style>"
                + "</head><body>"
                + "<script>"
                + "atOptions = { "
                + "'key' : '"
                + zoneId
                + "', "
                + "'format' : 'iframe', "
                + "'height' : 50, "
                + "'width' : 320, "
                + "'params' : {} "
                + "};"
                + "</script>"
                + "<script src='https://www.highperformanceformat.com/"
                + zoneId
                + "/invoke.js'></script>"
                + "</body></html>";

            webView.loadDataWithBaseURL(
                "https://www.highperformanceformat.com/",
                html,
                "text/html",
                "UTF-8",
                null
            );

            int heightPx =
                (int)(
                    60 *
                    activity
                        .getResources()
                        .getDisplayMetrics()
                        .density
                );

            FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    heightPx
                );

            params.gravity = Gravity.TOP;

            activity.addContentView(
                webView,
                params
            );
        });
    }

    @UsedByGodot
    public void showBanner() {

        activity.runOnUiThread(() -> {

            if (webView != null) {
                webView.setVisibility(
                    View.VISIBLE
                );
            }
        });
    }

    @UsedByGodot
    public void hideBanner() {

        activity.runOnUiThread(() -> {

            if (webView != null) {
                webView.setVisibility(
                    View.GONE
                );
            }
        });
    }

    @UsedByGodot
    public void removeBanner() {

        activity.runOnUiThread(() -> {

            if (webView != null) {

                if (webView.getParent() != null) {

                    ((ViewGroup) webView.getParent())
                        .removeView(webView);
                }

                webView.destroy();

                webView = null;
                currentZoneId = null;
            }
        });
    }
}
