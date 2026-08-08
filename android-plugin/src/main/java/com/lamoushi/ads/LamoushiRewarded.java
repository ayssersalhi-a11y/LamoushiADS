package com.lamoushi.ads;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.CookieManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.FrameLayout;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.HashSet;
import java.util.Set;

public class LamoushiRewarded extends GodotPlugin {

    private Activity activity;

    private WebView hostWebView;   // الصفحة التي تحمل سكربت الـ Popunder
    private WebView popWebView;    // النافذة المنبثقة الفعلية (window.open)

    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean rewardGranted = false;

    private static final int REWARD_DELAY_MS = 10000; // مدة الانتظار قبل منح المكافأة

    private static final String AD_SCRIPT_URL =
        "https://pl30736587.effectivecpmnetwork.com/25/46/ff/2546ffc01c441637dc550c6584facfe8.js";

    private static final String AD_BASE_URL =
        "https://pl30736587.effectivecpmnetwork.com/";

    public LamoushiRewarded(Godot godot) {
        super(godot);
        this.activity = godot.getActivity();
    }

    @Override
    public String getPluginName() {
        return "LamoushiRewarded";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();

        signals.add(new SignalInfo("reward_debug", String.class));
        signals.add(new SignalInfo("reward_ad_opened", new Class<?>[]{}));
        signals.add(new SignalInfo("reward_ready", new Class<?>[]{}));
        signals.add(new SignalInfo("reward_ad_closed", new Class<?>[]{}));

        return signals;
    }

    // ============================================================
    // تحميل الإعلان مسبقاً
    // ============================================================

    @UsedByGodot
    public void loadRewardedAd() {

        activity.runOnUiThread(() -> {

            rewardGranted = false;

            destroyHost();

            emitSignal(
                "reward_debug",
                "🚀 بدء تحميل Rewarded Ad"
            );

            emitSignal(
                "reward_debug",
                "🌐 AD_SCRIPT_URL = " + AD_SCRIPT_URL
            );

            emitSignal(
                "reward_debug",
                "🌐 AD_BASE_URL = " + AD_BASE_URL
            );

            CookieManager cm = CookieManager.getInstance();

            cm.setAcceptCookie(true);

            emitSignal(
                "reward_debug",
                "🍪 AcceptCookie = true"
            );

            hostWebView = new WebView(activity);

            WebSettings s = hostWebView.getSettings();

            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);

            s.setSupportMultipleWindows(true);
            s.setJavaScriptCanOpenWindowsAutomatically(true);

            s.setMixedContentMode(
                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                cm.setAcceptThirdPartyCookies(
                    hostWebView,
                    true
                );

                emitSignal(
                    "reward_debug",
                    "🍪 Third-party cookies = true"
                );
            }

            // ====================================================
            // WebChromeClient
            // ====================================================

            hostWebView.setWebChromeClient(new WebChromeClient() {

                @Override
                public boolean onConsoleMessage(ConsoleMessage cm2) {

                    String message =
                        "JS: "
                        + cm2.message()
                        + " | source="
                        + cm2.sourceId()
                        + " | line="
                        + cm2.lineNumber();

                    emitSignal(
                        "reward_debug",
                        message
                    );

                    return true;
                }

                @Override
                public boolean onCreateWindow(
                    WebView view,
                    boolean isDialog,
                    boolean isUserGesture,
                    android.os.Message resultMsg
                ) {

                    emitSignal(
                        "reward_debug",
                        "🪟 onCreateWindow()"
                    );

                    emitSignal(
                        "reward_debug",
                        "🖱 isUserGesture = " + isUserGesture
                    );

                    emitSignal(
                        "reward_debug",
                        "📦 isDialog = " + isDialog
                    );

                    emitSignal(
                        "reward_debug",
                        "✅ window.open() انطلقت — النافذة المنبثقة فُتحت فعلياً"
                    );

                    popWebView = new WebView(activity);

                    WebSettings ps =
                        popWebView.getSettings();

                    ps.setJavaScriptEnabled(true);
                    ps.setDomStorageEnabled(true);

                    ps.setSupportMultipleWindows(true);
                    ps.setJavaScriptCanOpenWindowsAutomatically(true);

                    ps.setMixedContentMode(
                        WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    );

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        CookieManager.getInstance()
                            .setAcceptThirdPartyCookies(
                                popWebView,
                                true
                            );
                    }

                    // ====================================================
                    // WebChromeClient للـ Pop WebView
                    // ====================================================

                    popWebView.setWebChromeClient(
                        new WebChromeClient() {

                            @Override
                            public boolean onConsoleMessage(
                                ConsoleMessage consoleMessage
                            ) {

                                emitSignal(
                                    "reward_debug",
                                    "POP JS: "
                                    + consoleMessage.message()
                                    + " | source="
                                    + consoleMessage.sourceId()
                                    + " | line="
                                    + consoleMessage.lineNumber()
                                );

                                return true;
                            }
                        }
                    );

                    // ====================================================
                    // WebViewClient للـ Pop WebView
                    // ====================================================

                    popWebView.setWebViewClient(
                        new WebViewClient() {

                            @Override
                            public void onPageStarted(
                                WebView v,
                                String url,
                                android.graphics.Bitmap favicon
                            ) {

                                emitSignal(
                                    "reward_debug",
                                    "🔵 POP بدأ تحميل الصفحة: "
                                    + url
                                );
                            }

                            @Override
                            public void onPageFinished(
                                WebView v,
                                String url
                            ) {

                                emitSignal(
                                    "reward_debug",
                                    "🔗 محتوى النافذة المنبثقة انتهى تحميله: "
                                    + url
                                );
                            }

                            @Override
                            public void onReceivedError(
                                WebView view,
                                WebResourceRequest request,
                                WebResourceError error
                            ) {

                                if (request != null && request.isForMainFrame()) {

                                    String description =
                                        error.getDescription() != null
                                        ? error.getDescription().toString()
                                        : "unknown";

                                    emitSignal(
                                        "reward_debug",
                                        "❌ POP WebView ERROR"
                                        + " | code="
                                        + error.getErrorCode()
                                        + " | description="
                                        + description
                                        + " | url="
                                        + request.getUrl()
                                    );
                                }
                            }

                            @Override
                            public void onReceivedHttpError(
                                WebView view,
                                WebResourceRequest request,
                                android.webkit.WebResourceResponse errorResponse
                            ) {

                                if (request != null && errorResponse != null) {

                                    emitSignal(
                                        "reward_debug",
                                        "⚠️ POP HTTP ERROR"
                                        + " | status="
                                        + errorResponse.getStatusCode()
                                        + " "
                                        + errorResponse.getReasonPhrase()
                                        + " | url="
                                        + request.getUrl()
                                    );
                                }
                            }

                            @Override
                            public void onReceivedSslError(
                                WebView view,
                                SslErrorHandler handler,
                                SslError error
                            ) {

                                emitSignal(
                                    "reward_debug",
                                    "🔐 POP SSL ERROR"
                                    + " | primaryError="
                                    + error.getPrimaryError()
                                    + " | url="
                                    + error.getUrl()
                                );

                                // لا نتجاوز خطأ SSL تلقائياً.
                                handler.cancel();
                            }

                            @Override
                            public boolean shouldOverrideUrlLoading(
                                WebView view,
                                WebResourceRequest request
                            ) {

                                if (request != null) {

                                    emitSignal(
                                        "reward_debug",
                                        "➡️ POP URL:"
                                        + " "
                                        + request.getUrl()
                                    );
                                }

                                return false;
                            }

                            @Override
                            public android.webkit.WebResourceResponse
                            shouldInterceptRequest(
                                WebView view,
                                WebResourceRequest request
                            ) {

                                if (request != null) {

                                    String url =
                                        request.getUrl().toString();

                                    emitSignal(
                                        "reward_debug",
                                        "🌐 POP REQUEST:"
                                        + " "
                                        + request.getMethod()
                                        + " "
                                        + url
                                    );
                                }

                                return super.shouldInterceptRequest(
                                    view,
                                    request
                                );
                            }
                        }
                    );

                    // WebView مخفي (1x1)
                    // فقط لاستضافة النافذة الناتجة عن window.open

                    FrameLayout.LayoutParams lp =
                        new FrameLayout.LayoutParams(1, 1);

                    activity.addContentView(
                        popWebView,
                        lp
                    );

                    WebView.WebViewTransport transport =
                        (WebView.WebViewTransport) resultMsg.obj;

                    transport.setWebView(popWebView);

                    resultMsg.sendToTarget();

                    emitSignal(
                        "reward_ad_opened"
                    );

                    scheduleReward();

                    return true;
                }
            });

            // ====================================================
            // WebViewClient للـ Host WebView
            // ====================================================

            hostWebView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageStarted(
                        WebView view,
                        String url,
                        android.graphics.Bitmap favicon
                    ) {

                        emitSignal(
                            "reward_debug",
                            "🔵 HOST بدأ تحميل الصفحة: "
                            + url
                        );
                    }

                    @Override
                    public void onPageFinished(
                        WebView view,
                        String url
                    ) {

                        emitSignal(
                            "reward_debug",
                            "✅ صفحة الإعلان جاهزة، بانتظار ضغطة المستخدم"
                        );

                        emitSignal(
                            "reward_debug",
                            "📍 HOST page URL = "
                            + url
                        );
                    }

                    // ====================================================
                    // أخطاء WebView
                    // ====================================================

                    @Override
                    public void onReceivedError(
                        WebView view,
                        WebResourceRequest request,
                        WebResourceError error
                    ) {

                        String url =
                            request != null
                            ? request.getUrl().toString()
                            : "unknown";

                        String description =
                            error.getDescription() != null
                            ? error.getDescription().toString()
                            : "unknown";

                        emitSignal(
                            "reward_debug",
                            "❌ HOST WebView ERROR"
                            + " | code="
                            + error.getErrorCode()
                            + " | description="
                            + description
                            + " | url="
                            + url
                        );
                    }

                    // ====================================================
                    // HTTP status errors
                    // ====================================================

                    @Override
                    public void onReceivedHttpError(
                        WebView view,
                        WebResourceRequest request,
                        android.webkit.WebResourceResponse errorResponse
                    ) {

                        if (request != null && errorResponse != null) {

                            emitSignal(
                                "reward_debug",
                                "⚠️ HOST HTTP ERROR"
                                + " | status="
                                + errorResponse.getStatusCode()
                                + " "
                                + errorResponse.getReasonPhrase()
                                + " | url="
                                + request.getUrl()
                            );
                        }
                    }

                    // ====================================================
                    // SSL
                    // ====================================================

                    @Override
                    public void onReceivedSslError(
                        WebView view,
                        SslErrorHandler sslHandler,
                        SslError error
                    ) {

                        emitSignal(
                            "reward_debug",
                            "🔐 HOST SSL ERROR"
                            + " | primaryError="
                            + error.getPrimaryError()
                            + " | url="
                            + error.getUrl()
                        );

                        // لا نتجاوز SSL error تلقائياً
                        sslHandler.cancel();
                    }

                    // ====================================================
                    // تسجيل التحويلات
                    // ====================================================

                    @Override
                    public boolean shouldOverrideUrlLoading(
                        WebView view,
                        WebResourceRequest request
                    ) {

                        if (request != null) {

                            emitSignal(
                                "reward_debug",
                                "➡️ HOST URL:"
                                + " "
                                + request.getUrl()
                            );
                        }

                        return false;
                    }

                    // ====================================================
                    // تسجيل كل طلبات الموارد
                    // ====================================================

                    @Override
                    public android.webkit.WebResourceResponse
                    shouldInterceptRequest(
                        WebView view,
                        WebResourceRequest request
                    ) {

                        if (request != null) {

                            String url =
                                request.getUrl().toString();

                            emitSignal(
                                "reward_debug",
                                "🌐 HOST REQUEST:"
                                + " "
                                + request.getMethod()
                                + " "
                                + url
                            );
                        }

                        return super.shouldInterceptRequest(
                            view,
                            request
                        );
                    }
                }
            );

            // ====================================================
            // HTML الذي يحمل سكربت الإعلان
            // ====================================================

            String html =
                "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' "
                + "content='width=device-width, "
                + "initial-scale=1.0'>"
                + "<style>"
                + "html,body{"
                + "margin:0;"
                + "padding:0;"
                + "background:transparent;"
                + "}"
                + "#trigger{"
                + "position:fixed;"
                + "top:0;"
                + "left:0;"
                + "width:100%;"
                + "height:100%;"
                + "}"
                + "</style>"
                + "</head><body>"
                + "<div id='trigger'></div>"
                + "<script src='"
                + AD_SCRIPT_URL
                + "'></script>"
                + "</body></html>";

            emitSignal(
                "reward_debug",
                "📄 HTML الإعلان تم إنشاؤه"
            );

            emitSignal(
                "reward_debug",
                "📡 جاري طلب سكربت الإعلان:"
                + " "
                + AD_SCRIPT_URL
            );

            hostWebView.loadDataWithBaseURL(
                AD_BASE_URL,
                html,
                "text/html",
                "UTF-8",
                null
            );

            // WebView مخفي 1x1
            FrameLayout.LayoutParams hp =
                new FrameLayout.LayoutParams(1, 1);

            activity.addContentView(
                hostWebView,
                hp
            );

            emitSignal(
                "reward_debug",
                "✅ HOST WebView تمت إضافته إلى Activity"
            );
        });
    }

    // ============================================================
    // تشغيل الإعلان عند ضغط المستخدم
    // ============================================================

    @UsedByGodot
    public void showRewardedAd() {

        activity.runOnUiThread(() -> {

            if (hostWebView == null) {

                emitSignal(
                    "reward_debug",
                    "⚠ لم يُحمَّل الإعلان بعد — نادِ load_rewarded_ad أولاً"
                );

                return;
            }

            emitSignal(
                "reward_debug",
                "🎯 showRewardedAd() بدأ"
            );

            hostWebView.evaluateJavascript(
                "(function(){ "
              + "var el = document.getElementById('trigger'); "

              + "if (!el) { "
              + "console.error('trigger element غير موجود'); "
              + "return false; "
              + "} "

              + "var ev = new MouseEvent("
              + "'click', "
              + "{"
              + "bubbles:true,"
              + "cancelable:true,"
              + "view:window"
              + "}"
              + "); "

              + "el.dispatchEvent(ev); "

              + "return true; "
              + "})();",

                value -> emitSignal(
                    "reward_debug",
                    "👆 تم تمرير ضغطة المستخدم لتفعيل الإعلان"
                    + " | JS result="
                    + value
                )
            );
        });
    }

    // ============================================================
    // جدولة المكافأة
    // ============================================================

    private void scheduleReward() {

        emitSignal(
            "reward_debug",
            "⏳ بدأ عداد المكافأة: "
            + REWARD_DELAY_MS
            + " ms"
        );

        handler.postDelayed(() -> {

            if (!rewardGranted) {

                rewardGranted = true;

                emitSignal(
                    "reward_debug",
                    "🎁 انتهت مدة الانتظار — المكافأة جاهزة للمنح"
                );

                emitSignal(
                    "reward_ready"
                );
            }

        }, REWARD_DELAY_MS);
    }

    // ============================================================
    // إغلاق الإعلان
    // ============================================================

    @UsedByGodot
    public void closeRewardedAd() {

        activity.runOnUiThread(() -> {

            emitSignal(
                "reward_debug",
                "🧹 إغلاق Rewarded Ad"
            );

            destroyPop();
            destroyHost();

            emitSignal(
                "reward_ad_closed"
            );
        });
    }

    // ============================================================
    // تدمير Pop WebView
    // ============================================================

    private void destroyPop() {

        if (popWebView != null) {

            emitSignal(
                "reward_debug",
                "🗑 تدمير POP WebView"
            );

            if (popWebView.getParent() != null) {

                ((ViewGroup) popWebView.getParent())
                    .removeView(popWebView);
            }

            popWebView.destroy();

            popWebView = null;
        }
    }

    // ============================================================
    // تدمير Host WebView
    // ============================================================

    private void destroyHost() {

        if (hostWebView != null) {

            emitSignal(
                "reward_debug",
                "🗑 تدمير HOST WebView"
            );

            if (hostWebView.getParent() != null) {

                ((ViewGroup) hostWebView.getParent())
                    .removeView(hostWebView);
            }

            hostWebView.destroy();

            hostWebView = null;
        }
    }

    // ============================================================
    // إنهاء Plugin
    // ============================================================

    @Override
    public void onMainDestroy() {

        emitSignal(
            "reward_debug",
            "💀 onMainDestroy()"
        );

        destroyPop();
        destroyHost();
    }
}
