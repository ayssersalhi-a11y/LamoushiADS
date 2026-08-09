package com.lamoushi.ads;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LamoushiRewarded extends GodotPlugin {

    private Activity activity;

    private WebView hostWebView;
    private WebView popWebView;

    private FrameLayout hostContainer;
    private FrameLayout popContainer;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean rewardGranted = false;

    /*
     * هذه مجرد أوقات تشخيص.
     * لا تعتبر إثباتاً لمشاهدة الإعلان.
     */
    private static final int DEBUG_DELAY_1 = 1000;
    private static final int DEBUG_DELAY_3 = 3000;
    private static final int DEBUG_DELAY_7 = 7000;
    private static final int DEBUG_DELAY_10 = 10000;

    private static final String AD_SCRIPT_URL =
            "https://pl30736587.effectivecpmnetwork.com/25/46/ff/2546ffc01c441637dc550c6584facfe8.js";

    private static final String AD_BASE_URL =
            "https://pl30736587.effectivecpmnetwork.com/";

    public LamoushiRewarded(Godot godot) {
        super(godot);
        activity = godot.getActivity();
    }

    @Override
    public String getPluginName() {
        return "LamoushiRewarded";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {

        Set<SignalInfo> signals =
                new HashSet<>();

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
    // DEBUG
    // ============================================================

    private void log(String message) {

        try {
            emitSignal(
                    "reward_debug",
                    message
            );
        } catch (Exception ignored) {
        }
    }

    private void separator() {
        log("==================================================");
    }

    // ============================================================
    // LOAD
    // ============================================================

    @UsedByGodot
    public void loadRewardedAd() {

        if (activity == null) {
            return;
        }

        activity.runOnUiThread(() -> {

            rewardGranted = false;

            handler.removeCallbacksAndMessages(null);

            destroyPop();
            destroyHost();

            separator();

            log("🚀 LOAD REWARDED START");

            log(
                    "🌐 AD_SCRIPT_URL = "
                            + AD_SCRIPT_URL
            );

            log(
                    "🌐 AD_BASE_URL = "
                            + AD_BASE_URL
            );

            // ----------------------------------------------------
            // COOKIES
            // ----------------------------------------------------

            try {

                CookieManager cookieManager =
                        CookieManager.getInstance();

                cookieManager.setAcceptCookie(true);

                if (Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.LOLLIPOP) {

                    log(
                            "🍪 Third-party cookies enabled"
                    );
                }

            } catch (Exception e) {

                log(
                        "⚠️ Cookie configuration error: "
                                + e.getMessage()
                );
            }

            // ----------------------------------------------------
            // HOST CONTAINER
            // ----------------------------------------------------

            hostContainer =
                    new FrameLayout(activity);

            hostContainer.setBackgroundColor(
                    Color.TRANSPARENT
            );

            // ----------------------------------------------------
            // HOST WEBVIEW
            // ----------------------------------------------------

            hostWebView =
                    new WebView(activity);

            configureWebView(
                    hostWebView,
                    "HOST"
            );

            // ----------------------------------------------------
            // HOST CHROME
            // ----------------------------------------------------

            hostWebView.setWebChromeClient(
                    new WebChromeClient() {

                        @Override
                        public boolean onConsoleMessage(
                                ConsoleMessage message
                        ) {

                            if (message != null) {

                                log(
                                        "HOST JS"
                                                + " | level="
                                                + message.messageLevel()
                                                + " | message="
                                                + message.message()
                                                + " | source="
                                                + message.sourceId()
                                                + " | line="
                                                + message.lineNumber()
                                );
                            }

                            return true;
                        }

                        @Override
                        public boolean onCreateWindow(
                                WebView view,
                                boolean isDialog,
                                boolean isUserGesture,
                                android.os.Message resultMsg
                        ) {

                            separator();

                            log(
                                    "🪟 HOST onCreateWindow()"
                            );

                            log(
                                    "🖱 isUserGesture = "
                                            + isUserGesture
                            );

                            log(
                                    "📦 isDialog = "
                                            + isDialog
                            );

                            createPopWebView(
                                    resultMsg
                            );

                            return true;
                        }

                        @Override
                        public void onCloseWindow(
                                WebView window
                        ) {

                            log(
                                    "🪟 HOST onCloseWindow()"
                            );

                            destroyPop();
                        }
                    }
            );

            // ----------------------------------------------------
            // HOST CLIENT
            // ----------------------------------------------------

            hostWebView.setWebViewClient(
                    new WebViewClient() {

                        @Override
                        public void onPageStarted(
                                WebView view,
                                String url,
                                Bitmap favicon
                        ) {

                            separator();

                            log(
                                    "🔵 HOST PAGE START | "
                                            + url
                            );

                            inspectSize(
                                    view,
                                    "HOST PAGE START"
                            );
                        }

                        @Override
                        public void onPageFinished(
                                WebView view,
                                String url
                        ) {

                            separator();

                            log(
                                    "✅ HOST PAGE FINISHED | "
                                            + url
                            );

                            inspectWebView(
                                    view,
                                    "HOST PAGE FINISHED"
                            );

                            inspectSize(
                                    view,
                                    "HOST PAGE FINISHED"
                            );

                            installDiagnostics();

                            emitSignal(
                                    "reward_ready"
                            );
                        }

                        @Override
                        public void onReceivedError(
                                WebView view,
                                WebResourceRequest request,
                                WebResourceError error
                        ) {

                            if (error == null) {
                                return;
                            }

                            log(
                                    "❌ HOST ERROR"
                                            + " | code="
                                            + error.getErrorCode()
                                            + " | description="
                                            + error.getDescription()
                                            + " | url="
                                            + (
                                            request != null
                                                    ? request.getUrl()
                                                    : "unknown"
                                    )
                            );
                        }

                        @Override
                        public void onReceivedHttpError(
                                WebView view,
                                WebResourceRequest request,
                                WebResourceResponse response
                        ) {

                            if (request != null &&
                                    response != null) {

                                log(
                                        "⚠️ HOST HTTP ERROR"
                                                + " | status="
                                                + response.getStatusCode()
                                                + " | mime="
                                                + response.getMimeType()
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

                            if (error != null) {

                                log(
                                        "🔐 HOST SSL ERROR"
                                                + " | error="
                                                + error.getPrimaryError()
                                                + " | url="
                                                + error.getUrl()
                                );
                            }

                            /*
                             * لا نتجاوز أخطاء SSL.
                             */
                            if (sslHandler != null) {
                                sslHandler.cancel();
                            }
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                log(
                                        "➡️ HOST NAVIGATION"
                                                + " | "
                                                + request.getMethod()
                                                + " "
                                                + request.getUrl()
                                );
                            }

                            return false;
                        }

                        @Override
                        public WebResourceResponse
                        shouldInterceptRequest(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                logRequest(
                                        "HOST",
                                        request
                                );
                            }

                            return super.shouldInterceptRequest(
                                    view,
                                    request
                            );
                        }
                    }
            );

            // ----------------------------------------------------
            // HTML
            // ----------------------------------------------------

            String html =
                    "<!DOCTYPE html>"
                            + "<html>"
                            + "<head>"

                            + "<meta name='viewport' "
                            + "content='width=device-width,"
                            + "initial-scale=1.0,"
                            + "maximum-scale=1.0,"
                            + "user-scalable=no'>"

                            + "<style>"

                            + "html,body{"
                            + "margin:0;"
                            + "padding:0;"
                            + "width:100%;"
                            + "height:100%;"
                            + "background:#222;"
                            + "overflow:auto;"
                            + "}"

                            + "#debugTitle{"
                            + "position:fixed;"
                            + "top:0;"
                            + "left:0;"
                            + "right:0;"
                            + "z-index:999999;"
                            + "background:#000;"
                            + "color:#fff;"
                            + "padding:8px;"
                            + "font-size:13px;"
                            + "}"

                            + "#trigger{"
                            + "position:fixed;"
                            + "top:45px;"
                            + "left:0;"
                            + "width:100%;"
                            + "height:200px;"
                            + "background:rgba(255,255,255,0.05);"
                            + "z-index:999998;"
                            + "}"

                            + "</style>"

                            + "</head>"

                            + "<body>"

                            + "<div id='debugTitle'>"
                            + "LAMOUSHI REWARDED TEST"
                            + "</div>"

                            + "<div id='trigger'></div>"

                            + "<script>"

                            + "console.log('🔬 HTML loaded');"

                            + "console.log("
                            + "'trigger موجود = ' + "
                            + "!!document.getElementById('trigger')"
                            + ");"

                            + "</script>"

                            + "<script src='"
                            + AD_SCRIPT_URL
                            + "'></script>"

                            + "</body>"
                            + "</html>";

            log(
                    "📄 HTML الإعلان تم إنشاؤه"
            );

            log(
                    "📡 جاري تحميل سكربت الإعلان: "
                            + AD_SCRIPT_URL
            );

            // ----------------------------------------------------
            // LOAD
            // ----------------------------------------------------

            hostWebView.loadDataWithBaseURL(
                    AD_BASE_URL,
                    html,
                    "text/html",
                    "UTF-8",
                    null
            );

            // ----------------------------------------------------
            // FULL SCREEN HOST
            // ----------------------------------------------------

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    );

            hostContainer.addView(
                    hostWebView,
                    params
            );

            activity.addContentView(
                    hostContainer,
                    params
            );

            log(
                    "👁 HOST أصبح مرئيًا بحجم الشاشة"
            );

            inspectSize(
                    hostWebView,
                    "HOST AFTER ADD"
            );

            inspectViewTree(
                    hostWebView,
                    "HOST AFTER ADD"
            );

            separator();
        });
    }

    // ============================================================
    // WEBVIEW CONFIGURATION
    // ============================================================

    private void configureWebView(
            WebView webView,
            String name
    ) {

        if (webView == null) {
            return;
        }

        WebSettings settings =
                webView.getSettings();

        settings.setJavaScriptEnabled(true);

        settings.setDomStorageEnabled(true);

        settings.setDatabaseEnabled(true);

        settings.setSupportMultipleWindows(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(
                true
        );

        settings.setAllowFileAccess(true);

        settings.setAllowContentAccess(true);

        settings.setLoadWithOverviewMode(false);

        settings.setUseWideViewPort(false);

        settings.setBuiltInZoomControls(false);

        settings.setDisplayZoomControls(false);

        settings.setSupportZoom(false);

        settings.setMediaPlaybackRequiresUserGesture(
                false
        );

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.LOLLIPOP) {

            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );

            CookieManager
                    .getInstance()
                    .setAcceptThirdPartyCookies(
                            webView,
                            true
                    );
        }

        webView.setVisibility(
                View.VISIBLE
        );

        webView.setAlpha(
                1.0f
        );

        webView.setBackgroundColor(
                Color.TRANSPARENT
        );

        log(
                "⚙️ "
                        + name
                        + " WebView configured"
        );
    }

    // ============================================================
    // CREATE POP
    // ============================================================

    private void createPopWebView(
            android.os.Message resultMsg
    ) {

        activity.runOnUiThread(() -> {

            destroyPop();

            separator();

            log(
                    "🚀 إنشاء POP WebView"
            );

            popContainer =
                    new FrameLayout(activity);

            popContainer.setBackgroundColor(
                    Color.TRANSPARENT
            );

            popWebView =
                    new WebView(activity);

            configureWebView(
                    popWebView,
                    "POP"
            );

            // ----------------------------------------------------
            // POP CHROME
            // ----------------------------------------------------

            popWebView.setWebChromeClient(
                    new WebChromeClient() {

                        @Override
                        public boolean onConsoleMessage(
                                ConsoleMessage message
                        ) {

                            if (message != null) {

                                log(
                                        "POP JS"
                                                + " | level="
                                                + message.messageLevel()
                                                + " | message="
                                                + message.message()
                                                + " | source="
                                                + message.sourceId()
                                                + " | line="
                                                + message.lineNumber()
                                );
                            }

                            return true;
                        }

                        @Override
                        public boolean onCreateWindow(
                                WebView view,
                                boolean isDialog,
                                boolean isUserGesture,
                                android.os.Message message
                        ) {

                            log(
                                    "🪟 POP onCreateWindow()"
                            );

                            log(
                                    "🖱 POP isUserGesture = "
                                            + isUserGesture
                            );

                            return super.onCreateWindow(
                                    view,
                                    isDialog,
                                    isUserGesture,
                                    message
                            );
                        }

                        @Override
                        public void onCloseWindow(
                                WebView window
                        ) {

                            log(
                                    "🪟 POP onCloseWindow()"
                            );

                            destroyPop();
                        }
                    }
            );

            // ----------------------------------------------------
            // POP CLIENT
            // ----------------------------------------------------

            popWebView.setWebViewClient(
                    new WebViewClient() {

                        @Override
                        public void onPageStarted(
                                WebView view,
                                String url,
                                Bitmap favicon
                        ) {

                            separator();

                            log(
                                    "🔵 POP PAGE START | "
                                            + url
                            );

                            inspectSize(
                                    view,
                                    "POP PAGE START"
                            );
                        }

                        @Override
                        public void onPageFinished(
                                WebView view,
                                String url
                        ) {

                            separator();

                            log(
                                    "✅ POP PAGE FINISHED | "
                                            + url
                            );

                            inspectWebView(
                                    view,
                                    "POP PAGE FINISHED"
                            );

                            inspectSize(
                                    view,
                                    "POP PAGE FINISHED"
                            );

                            installPopDiagnostics(
                                    view
                            );
                        }

                        @Override
                        public void onReceivedError(
                                WebView view,
                                WebResourceRequest request,
                                WebResourceError error
                        ) {

                            if (error != null) {

                                log(
                                        "❌ POP ERROR"
                                                + " | code="
                                                + error.getErrorCode()
                                                + " | description="
                                                + error.getDescription()
                                                + " | url="
                                                + (
                                                request != null
                                                        ? request.getUrl()
                                                        : "unknown"
                                        )
                                );
                            }
                        }

                        @Override
                        public void onReceivedHttpError(
                                WebView view,
                                WebResourceRequest request,
                                WebResourceResponse response
                        ) {

                            if (request != null &&
                                    response != null) {

                                log(
                                        "⚠️ POP HTTP ERROR"
                                                + " | status="
                                                + response.getStatusCode()
                                                + " | mime="
                                                + response.getMimeType()
                                                + " | url="
                                                + request.getUrl()
                                );
                            }
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                log(
                                        "➡️ POP NAVIGATION"
                                                + " | "
                                                + request.getMethod()
                                                + " "
                                                + request.getUrl()
                                );
                            }

                            return false;
                        }

                        @Override
                        public WebResourceResponse
                        shouldInterceptRequest(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                logRequest(
                                        "POP",
                                        request
                                );
                            }

                            return super.shouldInterceptRequest(
                                    view,
                                    request
                            );
                        }
                    }
            );

            // ----------------------------------------------------
            // FULL SCREEN POP
            // ----------------------------------------------------

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    );

            popContainer.addView(
                    popWebView,
                    params
            );

            activity.addContentView(
                    popContainer,
                    params
            );

            popWebView.bringToFront();

            popWebView.setVisibility(
                    View.VISIBLE
            );

            popWebView.setAlpha(
                    1.0f
            );

            log(
                    "👁 POP أصبح مرئيًا بحجم الشاشة"
            );

            inspectSize(
                    popWebView,
                    "POP AFTER ADD"
            );

            inspectViewTree(
                    popWebView,
                    "POP AFTER ADD"
            );

            // ----------------------------------------------------
            // TRANSPORT
            // ----------------------------------------------------

            if (resultMsg != null &&
                    resultMsg.obj instanceof
                            WebView.WebViewTransport) {

                WebView.WebViewTransport transport =
                        (WebView.WebViewTransport)
                                resultMsg.obj;

                transport.setWebView(
                        popWebView
                );

                resultMsg.sendToTarget();

                log(
                        "✅ WebViewTransport تم تسليمه"
                );
            } else {

                log(
                        "⚠️ WebViewTransport غير صالح"
                );
            }

            emitSignal(
                    "reward_ad_opened"
            );

            scheduleRewardTest();
        });
    }

    // ============================================================
    // SHOW
    // ============================================================

    @UsedByGodot
    public void showRewardedAd() {

        if (activity == null) {
            return;
        }

        activity.runOnUiThread(() -> {

            separator();

            log(
                    "🎯 SHOW REWARDED START"
            );

            if (hostWebView == null) {

                log(
                        "❌ HOST WebView = null"
                );

                return;
            }

            inspectWebView(
                    hostWebView,
                    "PRE CLICK"
            );

            inspectSize(
                    hostWebView,
                    "PRE CLICK"
            );

            inspectViewTree(
                    hostWebView,
                    "PRE CLICK"
            );

            String js =
                    "(function(){"

                            + "var el="
                            + "document.getElementById('trigger');"

                            + "if(!el){"

                            + "console.log("
                            + "'❌ trigger غير موجود'"
                            + ");"

                            + "return false;"
                            + "}"

                            + "console.log("
                            + "'🖱 dispatch click'"
                            + ");"

                            + "console.log("
                            + "'rect=' + "
                            + "JSON.stringify("
                            + "el.getBoundingClientRect()"
                            + ")"
                            + ");"

                            + "var ev="
                            + "new MouseEvent('click',{"
                            + "bubbles:true,"
                            + "cancelable:true,"
                            + "view:window,"
                            + "clientX:1,"
                            + "clientY:1"
                            + "});"

                            + "var result="
                            + "el.dispatchEvent(ev);"

                            + "console.log("
                            + "'dispatch result=' + result"
                            + ");"

                            + "return result;"

                            + "})();";

            hostWebView.evaluateJavascript(
                    js,
                    value -> {

                        log(
                                "👆 CLICK RESULT = "
                                        + value
                        );

                        inspectWebView(
                                hostWebView,
                                "POST CLICK"
                        );

                        inspectSize(
                                hostWebView,
                                "POST CLICK"
                        );

                        scheduleDiagnostics();
                    }
            );
        });
    }

    // ============================================================
    // DIAGNOSTIC TIMERS
    // ============================================================

    private void scheduleDiagnostics() {

        handler.postDelayed(
                () -> {

                    separator();

                    log(
                            "🔬 AFTER 1 SEC | delay="
                                    + DEBUG_DELAY_1
                    );

                    inspectAll(
                            "AFTER 1 SEC"
                    );

                },
                DEBUG_DELAY_1
        );

        handler.postDelayed(
                () -> {

                    separator();

                    log(
                            "🔬 AFTER 3 SEC | delay="
                                    + DEBUG_DELAY_3
                    );

                    inspectAll(
                            "AFTER 3 SEC"
                    );

                },
                DEBUG_DELAY_3
        );

        handler.postDelayed(
                () -> {

                    separator();

                    log(
                            "🔬 AFTER 7 SEC | delay="
                                    + DEBUG_DELAY_7
                    );

                    inspectAll(
                            "AFTER 7 SEC"
                    );

                },
                DEBUG_DELAY_7
        );

        handler.postDelayed(
                () -> {

                    separator();

                    log(
                            "🔬 AFTER 10 SEC | delay="
                                    + DEBUG_DELAY_10
                    );

                    inspectAll(
                            "AFTER 10 SEC"
                    );

                },
                DEBUG_DELAY_10
        );
    }

    private void inspectAll(
            String label
    ) {

        if (hostWebView != null) {

            inspectWebView(
                    hostWebView,
                    "HOST " + label
            );

            inspectSize(
                    hostWebView,
                    "HOST " + label
            );

            inspectViewTree(
                    hostWebView,
                    "HOST " + label
            );
        }

        if (popWebView != null) {

            inspectWebView(
                    popWebView,
                    "POP " + label
            );

            inspectSize(
                    popWebView,
                    "POP " + label
            );

            inspectViewTree(
                    popWebView,
                    "POP " + label
            );
        } else {

            log(
                    "📭 "
                            + label
                            + " | POP WebView = null"
            );
        }
    }

    // ============================================================
    // INSPECT DOM
    // ============================================================

    private void inspectWebView(
            WebView webView,
            String label
    ) {

        if (webView == null) {

            log(
                    "🔬 "
                            + label
                            + " | WebView = NULL"
            );

            return;
        }

        String js =
                "(function(){"

                        + "var t="
                        + "document.getElementById('trigger');"

                        + "var body="
                        + "document.body;"

                        + "var doc="
                        + "document.documentElement;"

                        + "return JSON.stringify({"

                        + "href:location.href,"

                        + "origin:location.origin,"

                        + "readyState:document.readyState,"

                        + "visibility:"
                        + "getComputedStyle(body).visibility,"

                        + "display:"
                        + "getComputedStyle(body).display,"

                        + "opacity:"
                        + "getComputedStyle(body).opacity,"

                        + "bodyWidth:"
                        + "(body?body.offsetWidth:-1),"

                        + "bodyHeight:"
                        + "(body?body.offsetHeight:-1),"

                        + "documentWidth:"
                        + "(doc?doc.offsetWidth:-1),"

                        + "documentHeight:"
                        + "(doc?doc.offsetHeight:-1),"

                        + "scrollX:"
                        + "window.scrollX,"

                        + "scrollY:"
                        + "window.scrollY,"

                        + "innerWidth:"
                        + "window.innerWidth,"

                        + "innerHeight:"
                        + "window.innerHeight,"

                        + "devicePixelRatio:"
                        + "window.devicePixelRatio,"

                        + "elements:"
                        + "document.getElementsByTagName('*').length,"

                        + "iframes:"
                        + "document.getElementsByTagName('iframe').length,"

                        + "links:"
                        + "document.getElementsByTagName('a').length,"

                        + "buttons:"
                        + "document.getElementsByTagName('button').length,"

                        + "scripts:"
                        + "document.getElementsByTagName('script').length,"

                        + "images:"
                        + "document.getElementsByTagName('img').length,"

                        + "triggerExists:"
                        + "!!t,"

                        + "triggerDisplay:"
                        + "(t?getComputedStyle(t).display:null),"

                        + "triggerVisibility:"
                        + "(t?getComputedStyle(t).visibility:null),"

                        + "triggerOpacity:"
                        + "(t?getComputedStyle(t).opacity:null),"

                        + "triggerRect:"
                        + "(t?JSON.stringify("
                        + "t.getBoundingClientRect()"
                        + "):null),"

                        + "activeElement:"
                        + "(document.activeElement?"
                        + "document.activeElement.tagName:"
                        + "null),"

                        + "bodyText:"
                        + "(body?"
                        + "body.innerText.substring(0,500):"
                        + "'')"

                        + "});"

                        + "})();";

        webView.evaluateJavascript(
                js,
                value -> {

                    log(
                            "🔬 "
                                    + label
                                    + " = "
                                    + value
                    );
                }
        );
    }

    // ============================================================
    // SIZE
    // ============================================================

    private void inspectSize(
            WebView webView,
            String label
    ) {

        if (webView == null) {

            log(
                    "📐 "
                            + label
                            + " | WebView=NULL"
            );

            return;
        }

        activity.runOnUiThread(() -> {

            ViewGroup.LayoutParams lp =
                    webView.getLayoutParams();

            String layoutInfo;

            if (lp != null) {

                layoutInfo =
                        "lpWidth="
                                + lp.width
                                + " lpHeight="
                                + lp.height;

            } else {

                layoutInfo =
                        "layoutParams=null";
            }

            log(
                    "📐 "
                            + label
                            + " | width="
                            + webView.getWidth()
                            + " height="
                            + webView.getHeight()
                            + " x="
                            + webView.getX()
                            + " y="
                            + webView.getY()
                            + " translationX="
                            + webView.getTranslationX()
                            + " translationY="
                            + webView.getTranslationY()
                            + " scaleX="
                            + webView.getScaleX()
                            + " scaleY="
                            + webView.getScaleY()
                            + " alpha="
                            + webView.getAlpha()
                            + " visibility="
                            + webView.getVisibility()
                            + " shown="
                            + webView.isShown()
                            + " attached="
                            + webView.isAttachedToWindow()
                            + " "
                            + layoutInfo
            );
        });
    }

    // ============================================================
    // VIEW TREE
    // ============================================================

    private void inspectViewTree(
            View view,
            String label
    ) {

        if (view == null) {

            log(
                    "🌳 "
                            + label
                            + " | view=null"
            );

            return;
        }

        activity.runOnUiThread(() -> {

            int count = 0;

            View current = view;

            while (current != null &&
                    count < 20) {

                log(
                        "🌳 "
                                + label
                                + " | level="
                                + count
                                + " class="
                                + current.getClass()
                                .getSimpleName()
                                + " width="
                                + current.getWidth()
                                + " height="
                                + current.getHeight()
                                + " x="
                                + current.getX()
                                + " y="
                                + current.getY()
                                + " visibility="
                                + current.getVisibility()
                                + " alpha="
                                + current.getAlpha()
                );

                if (current.getParent()
                        instanceof View) {

                    current =
                            (View)
                                    current.getParent();

                } else {

                    current = null;
                }

                count++;
            }
        });
    }

    // ============================================================
    // REQUEST LOG
    // ============================================================

    private void logRequest(
            String prefix,
            WebResourceRequest request
    ) {

        if (request == null) {
            return;
        }

        log(
                "🌐 "
                        + prefix
                        + " REQUEST"
                        + " | "
                        + request.getMethod()
                        + " "
                        + request.getUrl()
        );

        log(
                prefix
                        + " isForMainFrame="
                        + request.isForMainFrame()
        );

        log(
                prefix
                        + " hasGesture="
                        + request.hasGesture()
        );

        Map<String, String> headers =
                request.getRequestHeaders();

        if (headers != null) {

            for (Map.Entry<String, String> entry :
                    headers.entrySet()) {

                log(
                        prefix
                                + " HEADER | "
                                + entry.getKey()
                                + " = "
                                + entry.getValue()
                );
            }
        }
    }

    // ============================================================
    // HOST JS DIAGNOSTICS
    // ============================================================

    private void installDiagnostics() {

        if (hostWebView == null) {
            return;
        }

        String js =
                "(function(){"

                        + "if(window.__lamoushi_diag)"
                        + "return 'already';"

                        + "window.__lamoushi_diag=true;"

                        + "console.log("
                        + "'🔬 diagnostics installed'"
                        + ");"

                        // CLICK
                        + "document.addEventListener("
                        + "'click',"
                        + "function(e){"

                        + "console.log("
                        + "'🖱 CLICK target=' + "
                        + "(e.target?"
                        + "e.target.tagName:"
                        + "'null')"
                        + ");"

                        + "console.log("
                        + "'🖱 trusted=' + "
                        + "e.isTrusted"
                        + ");"

                        + "console.log("
                        + "'🖱 defaultPrevented=' + "
                        + "e.defaultPrevented"
                        + ");"

                        + "console.log("
                        + "'🖱 client=' + "
                        + "e.clientX + ',' + "
                        + "e.clientY"
                        + ");"

                        + "},true);"

                        // POINTER
                        + "document.addEventListener("
                        + "'pointerdown',"
                        + "function(e){"

                        + "console.log("
                        + "'👆 POINTERDOWN target=' + "
                        + "(e.target?"
                        + "e.target.tagName:"
                        + "'null')"
                        + " + ' x=' + "
                        + "e.clientX"
                        + " + ' y=' + "
                        + "e.clientY"
                        + ");"

                        + "},true);"

                        // TOUCH
                        + "document.addEventListener("
                        + "'touchstart',"
                        + "function(e){"

                        + "console.log("
                        + "'📱 TOUCHSTART'"
                        + ");"

                        + "},true);"

                        // SCROLL
                        + "window.addEventListener("
                        + "'scroll',"
                        + "function(){"

                        + "console.log("
                        + "'📜 SCROLL x=' + "
                        + "window.scrollX"
                        + " + ' y=' + "
                        + "window.scrollY"
                        + ");"

                        + "},true);"

                        // RESIZE
                        + "window.addEventListener("
                        + "'resize',"
                        + "function(){"

                        + "console.log("
                        + "'📐 RESIZE ' + "
                        + "window.innerWidth"
                        + " + 'x' + "
                        + "window.innerHeight"
                        + ");"

                        + "},true);"

                        // MUTATION
                        + "var observer="
                        + "new MutationObserver("
                        + "function(list){"

                        + "console.log("
                        + "'🔄 DOM MUTATION count=' + "
                        + "list.length"
                        + ");"

                        + "list.forEach(function(m){"

                        + "console.log("
                        + "'🔄 mutation type=' + "
                        + "m.type"
                        + " + ' added=' + "
                        + "m.addedNodes.length"
                        + " + ' removed=' + "
                        + "m.removedNodes.length"
                        + ");"

                        + "});"

                        + "});"

                        + "observer.observe("
                        + "document.documentElement,"
                        + "{"
                        + "childList:true,"
                        + "subtree:true,"
                        + "attributes:true"
                        + "}"
                        + ");"

                        + "return 'installed';"

                        + "})();";

        hostWebView.evaluateJavascript(
                js,
                value ->
                        log(
                                "🔬 Diagnostics result = "
                                        + value
                        )
        );
    }

    // ============================================================
    // POP JS DIAGNOSTICS
    // ============================================================

    private void installPopDiagnostics(
            WebView webView
    ) {

        if (webView == null) {
            return;
        }

        String js =
                "(function(){"

                        + "if(window.__lamoushi_pop_diag)"
                        + "return 'already';"

                        + "window.__lamoushi_pop_diag=true;"

                        + "console.log("
                        + "'🔬 POP diagnostics installed'"
                        + ");"

                        + "document.addEventListener("
                        + "'click',"
                        + "function(e){"

                        + "console.log("
                        + "'🖱 POP CLICK target=' + "
                        + "(e.target?"
                        + "e.target.tagName:"
                        + "'null')"
                        + " + ' trusted=' + "
                        + "e.isTrusted"
                        + ");"

                        + "},true);"

                        + "window.addEventListener("
                        + "'scroll',"
                        + "function(){"

                        + "console.log("
                        + "'📜 POP SCROLL x=' + "
                        + "window.scrollX"
                        + " + ' y=' + "
                        + "window.scrollY"
                        + ");"

                        + "},true);"

                        + "return 'installed';"

                        + "})();";

        webView.evaluateJavascript(
                js,
                value ->
                        log(
                                "🔬 POP diagnostics result = "
                                        + value
                        )
        );
    }

    // ============================================================
    // TEST TIMER
    // ============================================================

    private void scheduleRewardTest() {

        log(
                "⏱ بدأ مؤقت الاختبار "
                        + DEBUG_DELAY_10
                        + "ms"
        );

        handler.postDelayed(
                () -> {

                    log(
                            "⏱ انتهى مؤقت الاختبار"
                    );

                    log(
                            "⚠️ المؤقت لا يعني أن الإعلان شوهد"
                    );

                },
                DEBUG_DELAY_10
        );
    }

    // ============================================================
    // CLOSE
    // ============================================================

    @UsedByGodot
    public void closeRewardedAd() {

        if (activity == null) {
            return;
        }

        activity.runOnUiThread(() -> {

            separator();

            log(
                    "🛑 CLOSE REWARDED"
            );

            handler.removeCallbacksAndMessages(
                    null
            );

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

            log(
                    "🗑 destroy POP WebView"
            );

            if (popWebView.getParent()
                    instanceof ViewGroup) {

                ((ViewGroup)
                        popWebView.getParent())
                        .removeView(
                                popWebView
                        );
            }

            try {
                popWebView.stopLoading();
            } catch (Exception ignored) {
            }

            try {
                popWebView.loadUrl(
                        "about:blank"
                );
            } catch (Exception ignored) {
            }

            popWebView.destroy();

            popWebView = null;
        }

        if (popContainer != null) {

            if (popContainer.getParent()
                    instanceof ViewGroup) {

                ((ViewGroup)
                        popContainer.getParent())
                        .removeView(
                                popContainer
                        );
            }

            popContainer = null;
        }
    }

    // ============================================================
    // DESTROY HOST
    // ============================================================

    private void destroyHost() {

        if (hostWebView != null) {

            log(
                    "🗑 destroy HOST WebView"
            );

            if (hostWebView.getParent()
                    instanceof ViewGroup) {

                ((ViewGroup)
                        hostWebView.getParent())
                        .removeView(
                                hostWebView
                        );
            }

            try {
                hostWebView.stopLoading();
            } catch (Exception ignored) {
            }

            try {
                hostWebView.loadUrl(
                        "about:blank"
                );
            } catch (Exception ignored) {
            }

            hostWebView.destroy();

            hostWebView = null;
        }

        if (hostContainer != null) {

            if (hostContainer.getParent()
                    instanceof ViewGroup) {

                ((ViewGroup)
                        hostContainer.getParent())
                        .removeView(
                                hostContainer
                        );
            }

            hostContainer = null;
        }
    }

    // ============================================================
    // GODOT DESTROY
    // ============================================================

    @Override
    public void onMainDestroy() {

        handler.removeCallbacksAndMessages(
                null
        );

        if (activity == null) {
            return;
        }

        activity.runOnUiThread(() -> {

            destroyPop();

            destroyHost();
        });
    }
}
