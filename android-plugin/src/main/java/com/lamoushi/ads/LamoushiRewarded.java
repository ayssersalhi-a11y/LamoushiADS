package com.lamoushi.ads;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

    private WebView hostWebView;
    private WebView popWebView;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean rewardGranted = false;

    private static final int REWARD_DELAY_MS = 10000;

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

        signals.add(
                new SignalInfo(
                        "reward_debug",
                        String.class
                )
        );

        signals.add(
                new SignalInfo(
                        "reward_ad_opened",
                        new Class<?>[]{}
                )
        );

        signals.add(
                new SignalInfo(
                        "reward_ready",
                        new Class<?>[]{}
                )
        );

        signals.add(
                new SignalInfo(
                        "reward_ad_closed",
                        new Class<?>[]{}
                )
        );

        return signals;
    }

    // ============================================================
    // LOAD REWARDED
    // ============================================================

    @UsedByGodot
    public void loadRewardedAd() {

        activity.runOnUiThread(() -> {

            rewardGranted = false;

            destroyPop();
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

            // ----------------------------------------------------
            // Cookies
            // ----------------------------------------------------

            CookieManager cm =
                    CookieManager.getInstance();

            cm.setAcceptCookie(true);

            emitSignal(
                    "reward_debug",
                    "🍪 AcceptCookie = true"
            );

            // ----------------------------------------------------
            // WebView
            // ----------------------------------------------------

            hostWebView =
                    new WebView(activity);

            WebSettings s =
                    hostWebView.getSettings();

            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);

            s.setSupportMultipleWindows(true);
            s.setJavaScriptCanOpenWindowsAutomatically(true);

            s.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );

            s.setAllowFileAccess(true);
            s.setAllowContentAccess(true);

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.LOLLIPOP) {

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
            // HOST CHROME CLIENT
            // ====================================================

            hostWebView.setWebChromeClient(
                    new WebChromeClient() {

                        @Override
                        public boolean onConsoleMessage(
                                ConsoleMessage cm2
                        ) {

                            emitSignal(
                                    "reward_debug",
                                    "HOST JS"
                                            + " | level="
                                            + cm2.messageLevel()
                                            + " | message="
                                            + cm2.message()
                                            + " | source="
                                            + cm2.sourceId()
                                            + " | line="
                                            + cm2.lineNumber()
                            );

                            return true;
                        }

                        // ------------------------------------------------
                        // window.open()
                        // ------------------------------------------------

                        @Override
                        public boolean onCreateWindow(
                                WebView view,
                                boolean isDialog,
                                boolean isUserGesture,
                                android.os.Message resultMsg
                        ) {

                            emitSignal(
                                    "reward_debug",
                                    "🪟 HOST onCreateWindow()"
                            );

                            emitSignal(
                                    "reward_debug",
                                    "🖱 isUserGesture = "
                                            + isUserGesture
                            );

                            emitSignal(
                                    "reward_debug",
                                    "📦 isDialog = "
                                            + isDialog
                            );

                            // --------------------------------------------
                            // إنشاء Pop WebView
                            // --------------------------------------------

                            destroyPop();

                            popWebView =
                                    new WebView(activity);

                            WebSettings ps =
                                    popWebView.getSettings();

                            ps.setJavaScriptEnabled(true);
                            ps.setDomStorageEnabled(true);

                            ps.setSupportMultipleWindows(true);
                            ps.setJavaScriptCanOpenWindowsAutomatically(
                                    true
                            );

                            ps.setMixedContentMode(
                                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            );

                            if (Build.VERSION.SDK_INT >=
                                    Build.VERSION_CODES.LOLLIPOP) {

                                CookieManager.getInstance()
                                        .setAcceptThirdPartyCookies(
                                                popWebView,
                                                true
                                        );
                            }

                            // =================================================
                            // POP CHROME CLIENT
                            // =================================================

                            popWebView.setWebChromeClient(
                                    new WebChromeClient() {

                                        @Override
                                        public boolean onConsoleMessage(
                                                ConsoleMessage consoleMessage
                                        ) {

                                            emitSignal(
                                                    "reward_debug",
                                                    "POP JS"
                                                            + " | level="
                                                            + consoleMessage.messageLevel()
                                                            + " | message="
                                                            + consoleMessage.message()
                                                            + " | source="
                                                            + consoleMessage.sourceId()
                                                            + " | line="
                                                            + consoleMessage.lineNumber()
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
                                                    "🪟 POP onCreateWindow()"
                                            );

                                            emitSignal(
                                                    "reward_debug",
                                                    "🖱 POP isUserGesture = "
                                                            + isUserGesture
                                            );

                                            return super.onCreateWindow(
                                                    view,
                                                    isDialog,
                                                    isUserGesture,
                                                    resultMsg
                                            );
                                        }
                                    }
                            );

                            // =================================================
                            // POP WEBVIEW CLIENT
                            // =================================================

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
                                                    "🔵 POP PAGE START"
                                                            + " | "
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
                                                    "✅ POP PAGE FINISHED"
                                                            + " | "
                                                            + url
                                            );
                                        }

                                        @Override
                                        public void onReceivedError(
                                                WebView view,
                                                WebResourceRequest request,
                                                WebResourceError error
                                        ) {

                                            if (request != null &&
                                                    request.isForMainFrame()) {

                                                emitSignal(
                                                        "reward_debug",
                                                        "❌ POP ERROR"
                                                                + " | code="
                                                                + error.getErrorCode()
                                                                + " | description="
                                                                + error.getDescription()
                                                                + " | url="
                                                                + request.getUrl()
                                                );
                                            }
                                        }

                                        @Override
                                        public void onReceivedHttpError(
                                                WebView view,
                                                WebResourceRequest request,
                                                WebResourceResponse errorResponse
                                        ) {

                                            if (request != null &&
                                                    errorResponse != null) {

                                                emitSignal(
                                                        "reward_debug",
                                                        "⚠️ POP HTTP ERROR"
                                                                + " | status="
                                                                + errorResponse.getStatusCode()
                                                                + " | mime="
                                                                + errorResponse.getMimeType()
                                                                + " | url="
                                                                + request.getUrl()
                                                );
                                            }
                                        }

                                        @Override
                                        public void onReceivedSslError(
                                                WebView view,
                                                SslErrorHandler sslHandler,
                                                SslError error
                                        ) {

                                            emitSignal(
                                                    "reward_debug",
                                                    "🔐 POP SSL ERROR"
                                                            + " | error="
                                                            + error.getPrimaryError()
                                                            + " | url="
                                                            + error.getUrl()
                                            );

                                            sslHandler.cancel();
                                        }

                                        @Override
                                        public boolean shouldOverrideUrlLoading(
                                                WebView view,
                                                WebResourceRequest request
                                        ) {

                                            if (request != null) {

                                                emitSignal(
                                                        "reward_debug",
                                                        "➡️ POP NAVIGATION"
                                                                + " | "
                                                                + request.getUrl()
                                                );
                                            }

                                            return false;
                                        }

                                        // ------------------------------------
                                        // تسجيل الطلبات فقط
                                        //
                                        // مهم:
                                        // لا نعيد تحميل الطلب.
                                        // لا نستخدم HttpURLConnection.
                                        // لا نعدل الاستجابة.
                                        // ------------------------------------

                                        @Override
                                        public WebResourceResponse
                                        shouldInterceptRequest(
                                                WebView view,
                                                WebResourceRequest request
                                        ) {

                                            if (request != null) {

                                                emitSignal(
                                                        "reward_debug",
                                                        "🌐 POP REQUEST"
                                                                + " | "
                                                                + request.getMethod()
                                                                + " "
                                                                + request.getUrl()
                                                );
                                            }

                                            return super
                                                    .shouldInterceptRequest(
                                                            view,
                                                            request
                                                    );
                                        }
                                    }
                            );

                            // ------------------------------------------------
                            // WebView مخفي
                            // ------------------------------------------------

                            FrameLayout.LayoutParams lp =
                                    new FrameLayout.LayoutParams(
                                            1,
                                            1
                                    );

                            activity.addContentView(
                                    popWebView,
                                    lp
                            );

                            // ------------------------------------------------
                            // تمرير WebView إلى window.open()
                            // ------------------------------------------------

                            WebView.WebViewTransport transport =
                                    (WebView.WebViewTransport)
                                            resultMsg.obj;

                            transport.setWebView(
                                    popWebView
                            );

                            resultMsg.sendToTarget();

                            emitSignal(
                                    "reward_debug",
                                    "✅ window.open() تم تسليمه إلى Pop WebView"
                            );

                            emitSignal(
                                    "reward_ad_opened"
                            );

                            scheduleReward();

                            return true;
                        }
                    }
            );

            // ============================================================
            // HOST WEBVIEW CLIENT
            // ============================================================

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
                                    "🔵 HOST PAGE START | "
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
                                    "✅ HOST PAGE FINISHED | "
                                            + url
                            );

                            emitSignal(
                                    "reward_debug",
                                    "📍 HOST URL = "
                                            + url
                            );

                            // ---------------------------------------------
                            // معلومات الصفحة من داخل JavaScript
                            // ---------------------------------------------

                            view.evaluateJavascript(
                                    "(function(){"
                                            + "return JSON.stringify({"
                                            + "bodyLength: document.body ? document.body.innerHTML.length : -1,"
                                            + "href: location.href,"
                                            + "readyState: document.readyState,"
                                            + "title: document.title,"
                                            + "trigger: !!document.getElementById('trigger')"
                                            + "});"
                                            + "})();",
                                    value -> emitSignal(
                                            "reward_debug",
                                            "📄 HOST DOCUMENT INFO = "
                                                    + value
                                    )
                            );
                        }

                        // ====================================================
                        // تسجيل أخطاء HOST
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

                            emitSignal(
                                    "reward_debug",
                                    "❌ HOST ERROR"
                                            + " | code="
                                            + error.getErrorCode()
                                            + " | description="
                                            + error.getDescription()
                                            + " | url="
                                            + url
                            );
                        }

                        @Override
                        public void onReceivedHttpError(
                                WebView view,
                                WebResourceRequest request,
                                WebResourceResponse errorResponse
                        ) {

                            if (request != null &&
                                    errorResponse != null) {

                                emitSignal(
                                        "reward_debug",
                                        "⚠️ HOST HTTP ERROR"
                                                + " | status="
                                                + errorResponse.getStatusCode()
                                                + " | mime="
                                                + errorResponse.getMimeType()
                                                + " | url="
                                                + request.getUrl()
                                );
                            }
                        }

                        @Override
                        public void onReceivedSslError(
                                WebView view,
                                SslErrorHandler sslHandler,
                                SslError error
                        ) {

                            emitSignal(
                                    "reward_debug",
                                    "🔐 HOST SSL ERROR"
                                            + " | error="
                                            + error.getPrimaryError()
                                            + " | url="
                                            + error.getUrl()
                            );

                            // لا نتجاوز SSL
                            sslHandler.cancel();
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                emitSignal(
                                        "reward_debug",
                                        "➡️ HOST NAVIGATION"
                                                + " | "
                                                + request.getUrl()
                                );
                            }

                            return false;
                        }

                        // ====================================================
                        // تسجيل الطلبات فقط
                        //
                        // لا اعتراض.
                        // لا HttpURLConnection.
                        // لا إعادة بناء للاستجابة.
                        // ====================================================

                        @Override
                        public WebResourceResponse
                        shouldInterceptRequest(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                emitSignal(
                                        "reward_debug",
                                        "🌐 HOST REQUEST"
                                                + " | "
                                                + request.getMethod()
                                                + " "
                                                + request.getUrl()
                                );

                                emitSignal(
                                        "reward_debug",
                                        "HOST isForMainFrame="
                                                + request.isForMainFrame()
                                );

                                emitSignal(
                                        "reward_debug",
                                        "HOST hasGesture="
                                                + request.hasGesture()
                                );
                            }

                            // ------------------------------------------------
                            // النقطة المهمة:
                            // نترك WebView يتولى الطلب بنفسه.
                            // ------------------------------------------------

                            return super.shouldInterceptRequest(
                                    view,
                                    request
                            );
                        }
                    }
            );

            // ============================================================
            // HTML
            // ============================================================

            String html =
                    "<!DOCTYPE html>"
                            + "<html>"
                            + "<head>"
                            + "<meta name='viewport' "
                            + "content='width=device-width, initial-scale=1.0'>"
                            + "<style>"
                            + "html,body{"
                            + "margin:0;"
                            + "padding:0;"
                            + "background:transparent;"
                            + "overflow:hidden;"
                            + "}"
                            + "#trigger{"
                            + "position:fixed;"
                            + "top:0;"
                            + "left:0;"
                            + "width:100%;"
                            + "height:100%;"
                            + "}"
                            + "</style>"
                            + "</head>"
                            + "<body>"
                            + "<div id='trigger'></div>"
                            + "<script>"
                            + "console.log('trigger موجود = ' + "
                            + "!!document.getElementById('trigger'));"
                            + "</script>"
                            + "<script src='"
                            + AD_SCRIPT_URL
                            + "'></script>"
                            + "</body>"
                            + "</html>";

            emitSignal(
                    "reward_debug",
                    "📄 HTML الإعلان تم إنشاؤه"
            );

            emitSignal(
                    "reward_debug",
                    "📡 جاري طلب سكربت الإعلان: "
                            + AD_SCRIPT_URL
            );

            hostWebView.loadDataWithBaseURL(
                    AD_BASE_URL,
                    html,
                    "text/html",
                    "UTF-8",
                    null
            );

            // ============================================================
            // HOST مخفي
            // ============================================================

            FrameLayout.LayoutParams hp =
                    new FrameLayout.LayoutParams(
                            1,
                            1
                    );

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
    // SHOW REWARDED
    // ============================================================

    @UsedByGodot
    public void showRewardedAd() {

        activity.runOnUiThread(() -> {

            emitSignal(
                    "reward_debug",
                    "🎯 showRewardedAd() بدأ"
            );

            if (hostWebView == null) {

                emitSignal(
                        "reward_debug",
                        "❌ HOST WebView = null"
                );

                return;
            }

            // --------------------------------------------------------
            // التحقق من وجود trigger
            // --------------------------------------------------------

            String js =
                    "(function(){"
                            + "var el=document.getElementById('trigger');"
                            + "if(!el){"
                            + "console.log('❌ trigger غير موجود');"
                            + "return false;"
                            + "}"
                            + "console.log('trigger موجود — dispatching click');"
                            + "var ev=new MouseEvent('click',{"
                            + "bubbles:true,"
                            + "cancelable:true,"
                            + "view:window"
                            + "});"
                            + "var result=el.dispatchEvent(ev);"
                            + "console.log('dispatchEvent result=' + result);"
                            + "return result;"
                            + "})();";

            hostWebView.evaluateJavascript(
                    js,
                    value -> {

                        emitSignal(
                                "reward_debug",
                                "👆 تم تمرير ضغطة المستخدم"
                                        + " | JS result="
                                        + value
                        );
                    }
            );
        });
    }

    // ============================================================
    // REWARD TIMER
    // ============================================================

    private void scheduleReward() {

        handler.postDelayed(
                () -> {

                    if (!rewardGranted) {

                        rewardGranted = true;

                        emitSignal(
                                "reward_debug",
                                "🎁 انتهت مدة الانتظار — المكافأة جاهزة"
                        );

                        emitSignal(
                                "reward_ready"
                        );
                    }

                },
                REWARD_DELAY_MS
        );
    }

    // ============================================================
    // CLOSE
    // ============================================================

    @UsedByGodot
    public void closeRewardedAd() {

        activity.runOnUiThread(() -> {

            destroyPop();
            destroyHost();

            emitSignal(
                    "reward_ad_closed"
            );
        });
    }

    // ============================================================
    // DESTROY POP
    // ============================================================

    private void destroyPop() {

        if (popWebView != null) {

            if (popWebView.getParent() != null) {

                ((ViewGroup) popWebView.getParent())
                        .removeView(popWebView);
            }

            popWebView.destroy();
            popWebView = null;
        }
    }

    // ============================================================
    // DESTROY HOST
    // ============================================================

    private void destroyHost() {

        if (hostWebView != null) {

            if (hostWebView.getParent() != null) {

                ((ViewGroup) hostWebView.getParent())
                        .removeView(hostWebView);
            }

            hostWebView.destroy();
            hostWebView = null;
        }
    }

    // ============================================================
    // GODOT DESTROY
    // ============================================================

    @Override
    public void onMainDestroy() {

        handler.removeCallbacksAndMessages(null);

        destroyPop();
        destroyHost();
    }
}
