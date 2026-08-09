package com.lamoushi.ads;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
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
import java.util.Set;

public class LamoushiRewarded extends GodotPlugin {

    private final Activity activity;

    private WebView hostWebView;
    private WebView popWebView;

    private FrameLayout hostContainer;
    private FrameLayout popContainer;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private static final String AD_SCRIPT_URL =
            "https://pl30736587.effectivecpmnetwork.com/25/46/ff/2546ffc01c441637dc550c6584facfe8.js";

    private static final String AD_BASE_URL =
            "https://pl30736587.effectivecpmnetwork.com/";

    private static final int DEBUG_DELAY_1 = 1000;
    private static final int DEBUG_DELAY_3 = 3000;
    private static final int DEBUG_DELAY_5 = 5000;
    private static final int DEBUG_DELAY_10 = 10000;

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
    // DEBUG
    // ============================================================

    private void log(String message) {
        emitSignal("reward_debug", message);
    }

    private void separator() {
        log("==================================================");
    }

    // ============================================================
    // LOAD
    // ============================================================

    @UsedByGodot
    public void loadRewardedAd() {

        activity.runOnUiThread(() -> {

            separator();
            log("🚀 LOAD REWARDED START");

            handler.removeCallbacksAndMessages(null);

            destroyPop();
            destroyHost();

            CookieManager cookieManager =
                    CookieManager.getInstance();

            cookieManager.setAcceptCookie(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(
                        null,
                        true
                );
            }

            log("🍪 Cookies enabled");

            // ----------------------------------------------------
            // HOST CONTAINER
            // ----------------------------------------------------

            hostContainer =
                    new FrameLayout(activity);

            hostContainer.setBackgroundColor(
                    Color.rgb(30, 30, 30)
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
            // TOUCH DIAGNOSTICS
            // ----------------------------------------------------

            hostWebView.setOnTouchListener(
                    new View.OnTouchListener() {

                        @Override
                        public boolean onTouch(
                                View v,
                                MotionEvent event
                        ) {

                            String action;

                            switch (event.getActionMasked()) {

                                case MotionEvent.ACTION_DOWN:
                                    action = "DOWN";
                                    break;

                                case MotionEvent.ACTION_MOVE:
                                    action = "MOVE";
                                    break;

                                case MotionEvent.ACTION_UP:
                                    action = "UP";
                                    break;

                                case MotionEvent.ACTION_CANCEL:
                                    action = "CANCEL";
                                    break;

                                default:
                                    action = String.valueOf(
                                            event.getActionMasked()
                                    );
                            }

                            log(
                                    "👆 REAL TOUCH"
                                            + " | action=" + action
                                            + " | x=" + event.getX()
                                            + " | y=" + event.getY()
                                            + " | rawX=" + event.getRawX()
                                            + " | rawY=" + event.getRawY()
                            );

                            return false;
                        }
                    }
            );

            // ----------------------------------------------------
            // CHROME CLIENT
            // ----------------------------------------------------

            hostWebView.setWebChromeClient(
                    new WebChromeClient() {

                        @Override
                        public boolean onConsoleMessage(
                                ConsoleMessage message
                        ) {

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

                            log("🪟 HOST onCreateWindow");

                            log(
                                    "🖱 isUserGesture="
                                            + isUserGesture
                            );

                            log(
                                    "📦 isDialog="
                                            + isDialog
                            );

                            createPopWebView(resultMsg);

                            return true;
                        }

                        @Override
                        public void onCloseWindow(
                                WebView window
                        ) {

                            log(
                                    "🛑 HOST onCloseWindow"
                            );

                            destroyPop();
                        }
                    }
            );

            // ----------------------------------------------------
            // WEBVIEW CLIENT
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
                                    "🔵 HOST PAGE START"
                                            + " | " + url
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
                                    "✅ HOST PAGE FINISHED"
                                            + " | " + url
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

                            log(
                                    "🔐 HOST SSL ERROR"
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

                                log(
                                        "➡️ HOST NAVIGATION"
                                                + " | method="
                                                + request.getMethod()
                                                + " | url="
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

                                log(
                                        "🌐 HOST REQUEST"
                                                + " | "
                                                + request.getMethod()
                                                + " "
                                                + request.getUrl()
                                );

                                log(
                                        "HOST mainFrame="
                                                + request.isForMainFrame()
                                );

                                log(
                                        "HOST gesture="
                                                + request.hasGesture()
                                );

                                if (request.getRequestHeaders()
                                        != null) {

                                    for (String key :
                                            request.getRequestHeaders()
                                                    .keySet()) {

                                        String value =
                                                request.getRequestHeaders()
                                                        .get(key);

                                        log(
                                                "HOST HEADER"
                                                        + " | "
                                                        + key
                                                        + "="
                                                        + value
                                        );
                                    }
                                }
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
                            + "min-height:100%;"
                            + "background:#222;"
                            + "overflow:auto;"
                            + "}"

                            + "#debugTitle{"
                            + "position:fixed;"
                            + "top:0;"
                            + "left:0;"
                            + "right:0;"
                            + "height:32px;"
                            + "line-height:32px;"
                            + "z-index:999999;"
                            + "background:#000;"
                            + "color:#fff;"
                            + "font-size:12px;"
                            + "padding-left:8px;"
                            + "}"

                            + "#trigger{"
                            + "position:relative;"
                            + "display:block;"
                            + "width:100%;"
                            + "height:300px;"
                            + "min-height:300px;"
                            + "background:rgba(255,255,255,0.08);"
                            + "border:2px solid rgba(255,255,255,0.25);"
                            + "z-index:999998;"
                            + "}"

                            + "</style>"

                            + "</head>"

                            + "<body>"

                            + "<div id='debugTitle'>"
                            + "LAMOUSHI REWARDED DEBUG"
                            + "</div>"

                            + "<div id='trigger'>"
                            + "</div>"

                            + "<script>"

                            + "console.log('🔬 HTML loaded');"

                            + "console.log("
                            + "'viewport=' + "
                            + "window.innerWidth + 'x' + "
                            + "window.innerHeight"
                            + ");"

                            + "console.log("
                            + "'devicePixelRatio=' + "
                            + "window.devicePixelRatio"
                            + ");"

                            + "console.log("
                            + "'trigger exists=' + "
                            + "!!document.getElementById('trigger')"
                            + ");"

                            + "</script>"

                            + "<script src='"
                            + AD_SCRIPT_URL
                            + "'></script>"

                            + "</body>"

                            + "</html>";

            log("📄 HTML CREATED");

            log(
                    "📡 Loading ad script:"
                            + AD_SCRIPT_URL
            );

            // ----------------------------------------------------
            // LOAD HTML
            // ----------------------------------------------------

            hostWebView.loadDataWithBaseURL(
                    AD_BASE_URL,
                    html,
                    "text/html",
                    "UTF-8",
                    null
            );

            // ----------------------------------------------------
            // FULL SCREEN
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

            hostWebView.bringToFront();

            hostWebView.requestLayout();

            log("👁 HOST ADDED FULL SCREEN");

            inspectSize(
                    hostWebView,
                    "HOST AFTER ADD"
            );

            separator();
        });
    }

    // ============================================================
    // WEBVIEW CONFIG
    // ============================================================

    private void configureWebView(
            WebView webView,
            String name
    ) {

        WebSettings settings =
                webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        settings.setDatabaseEnabled(true);

        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

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

        webView.setBackgroundColor(
                Color.rgb(30, 30, 30)
        );

        webView.setVisibility(
                View.VISIBLE
        );

        webView.setAlpha(1.0f);

        webView.setLayerType(
                View.LAYER_TYPE_HARDWARE,
                null
        );

        log(
                "⚙️ "
                        + name
                        + " CONFIGURED"
        );
    }

    // ============================================================
    // POP WEBVIEW
    // ============================================================

    private void createPopWebView(
            android.os.Message resultMsg
    ) {

        activity.runOnUiThread(() -> {

            destroyPop();

            separator();

            log("🚀 CREATE POP WEBVIEW");

            popContainer =
                    new FrameLayout(activity);

            popContainer.setBackgroundColor(
                    Color.BLACK
            );

            popWebView =
                    new WebView(activity);

            configureWebView(
                    popWebView,
                    "POP"
            );

            popWebView.setWebChromeClient(
                    new WebChromeClient() {

                        @Override
                        public boolean onConsoleMessage(
                                ConsoleMessage message
                        ) {

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
                                    "🪟 POP requested another window"
                            );

                            log(
                                    "POP gesture="
                                            + isUserGesture
                            );

                            return false;
                        }

                        @Override
                        public void onCloseWindow(
                                WebView window
                        ) {

                            log(
                                    "🛑 POP CLOSE WINDOW"
                            );

                            destroyPop();
                        }
                    }
            );

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
                                    "🔵 POP PAGE START"
                                            + " | "
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
                                    "✅ POP PAGE FINISHED"
                                            + " | "
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

                            installPopDiagnostics();
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
                                                + request.getUrl()
                                );
                            }

                            return false;
                        }

                        @Override
                        public void onReceivedError(
                                WebView view,
                                WebResourceRequest request,
                                WebResourceError error
                        ) {

                            log(
                                    "❌ POP ERROR"
                                            + " | code="
                                            + error.getErrorCode()
                                            + " | description="
                                            + error.getDescription()
                            );
                        }

                        @Override
                        public void onReceivedHttpError(
                                WebView view,
                                WebResourceRequest request,
                                WebResourceResponse response
                        ) {

                            if (response != null &&
                                    request != null) {

                                log(
                                        "⚠️ POP HTTP ERROR"
                                                + " | status="
                                                + response.getStatusCode()
                                                + " | url="
                                                + request.getUrl()
                                );
                            }
                        }

                        @Override
                        public WebResourceResponse
                        shouldInterceptRequest(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                log(
                                        "🌐 POP REQUEST"
                                                + " | "
                                                + request.getMethod()
                                                + " "
                                                + request.getUrl()
                                );

                                log(
                                        "POP mainFrame="
                                                + request.isForMainFrame()
                                );

                                log(
                                        "POP gesture="
                                                + request.hasGesture()
                                );
                            }

                            return super.shouldInterceptRequest(
                                    view,
                                    request
                            );
                        }
                    }
            );

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

            popWebView.requestLayout();

            inspectSize(
                    popWebView,
                    "POP AFTER ADD"
            );

            WebView.WebViewTransport transport =
                    (WebView.WebViewTransport)
                            resultMsg.obj;

            transport.setWebView(
                    popWebView
            );

            resultMsg.sendToTarget();

            log(
                    "✅ POP TRANSPORT SENT"
            );

            emitSignal(
                    "reward_ad_opened"
            );
        });
    }

    // ============================================================
    // SHOW
    // ============================================================

    @UsedByGodot
    public void showRewardedAd() {

        activity.runOnUiThread(() -> {

            separator();

            log("🎯 SHOW REWARDED");

            if (hostWebView == null) {

                log(
                        "❌ HOST WebView NULL"
                );

                return;
            }

            inspectWebView(
                    hostWebView,
                    "PRE REAL TOUCH"
            );

            inspectSize(
                    hostWebView,
                    "PRE REAL TOUCH"
            );

            log(
                    "👆 الآن يجب إجراء نقرة حقيقية داخل الإعلان"
            );

            handler.postDelayed(
                    () -> inspectWebView(
                            hostWebView,
                            "AFTER 1 SEC
                    ),
                    DEBUG_DELAY_1
            );

            handler.postDelayed(
                    () -> {

                        inspectWebView(
                                hostWebView,
                                "AFTER 3 SEC"
                        );

                        inspectSize(
                                hostWebView,
                                "AFTER 3 SEC"
                        );

                        if (popWebView != null) {

                            inspectWebView(
                                    popWebView,
                                    "POP AFTER 3 SEC"
                            );

                            inspectSize(
                                    popWebView,
                                    "POP AFTER 3 SEC"
                            );
                        }

                    },
                    DEBUG_DELAY_3
            );

            handler.postDelayed(
                    () -> {

                        inspectWebView(
                                hostWebView,
                                "AFTER 5 SEC"
                        );

                        if (popWebView != null) {

                            inspectWebView(
                                    popWebView,
                                    "POP AFTER 5 SEC"
                            );
                        }

                    },
                    DEBUG_DELAY_5
            );

            handler.postDelayed(
                    () -> {

                        inspectWebView(
                                hostWebView,
                                "AFTER 10 SEC"
                        );

                        if (popWebView != null) {

                            inspectWebView(
                                    popWebView,
                                    "POP AFTER 10 SEC"
                            );
                        }

                    },
                    DEBUG_DELAY_10
            );
        });
    }

    // ============================================================
    // DOM INSPECTION
    // ============================================================

    private void inspectWebView(
            WebView webView,
            String label
    ) {

        if (webView == null) {

            log(
                    "🔬 "
                            + label
                            + " | NULL"
            );

            return;
        }

        String js =
                "(function(){"

                        + "var t=document.getElementById('trigger');"

                        + "var b=document.body;"

                        + "var d=document.documentElement;"

                        + "var r=t?"
                        + "t.getBoundingClientRect():null;"

                        + "return JSON.stringify({"

                        + "href:location.href,"

                        + "origin:location.origin,"

                        + "readyState:document.readyState,"

                        + "visibility:"
                        + "getComputedStyle(document.body).visibility,"

                        + "display:"
                        + "getComputedStyle(document.body).display,"

                        + "opacity:"
                        + "getComputedStyle(document.body).opacity,"

                        + "windowWidth:window.innerWidth,"

                        + "windowHeight:window.innerHeight,"

                        + "screenWidth:screen.width,"

                        + "screenHeight:screen.height,"

                        + "devicePixelRatio:"
                        + "window.devicePixelRatio,"

                        + "scrollX:window.scrollX,"

                        + "scrollY:window.scrollY,"

                        + "bodyWidth:b?b.offsetWidth:-1,"

                        + "bodyHeight:b?b.offsetHeight:-1,"

                        + "documentWidth:d?d.offsetWidth:-1,"

                        + "documentHeight:d?d.offsetHeight:-1,"

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

                        + "triggerRect:"
                        + "(r?JSON.stringify({"
                        + "x:r.x,"
                        + "y:r.y,"
                        + "width:r.width,"
                        + "height:r.height,"
                        + "top:r.top,"
                        + "bottom:r.bottom,"
                        + "left:r.left,"
                        + "right:r.right"
                        + "}):null),"

                        + "bodyText:"
                        + "(b?b.innerText.substring(0,500):'')"

                        + "});"

                        + "})();";

        webView.evaluateJavascript(
                js,
                value -> log(
                        "🔬 "
                                + label
                                + " = "
                                + value
                )
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
                            + " | NULL"
            );

            return;
        }

        activity.runOnUiThread(() -> {

            ViewGroup.LayoutParams lp =
                    webView.getLayoutParams();

            log(
                    "📐 "
                            + label
                            + " | width="
                            + webView.getWidth()
                            + " | height="
                            + webView.getHeight()
                            + " | x="
                            + webView.getX()
                            + " | y="
                            + webView.getY()
                            + " | visibility="
                            + webView.getVisibility()
                            + " | alpha="
                            + webView.getAlpha()
                            + " | lpWidth="
                            + (
                            lp != null
                                    ? lp.width
                                    : -999
                    )
                            + " | lpHeight="
                            + (
                            lp != null
                                    ? lp.height
                                    : -999
                    )
            );
        });
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

                        + "if(window.__lamoushi_diag){"
                        + "return 'already';"
                        + "}"

                        + "window.__lamoushi_diag=true;"

                        + "console.log("
                        + "'🔬 HOST diagnostics installed'"
                        + ");"

                        // CLICK
                        + "document.addEventListener("
                        + "'click',"
                        + "function(e){"

                        + "console.log("
                        + "'🖱 CLICK target='"
                        + "+(e.target&&e.target.tagName)"
                        + ");"

                        + "console.log("
                        + "'🖱 trusted='"
                        + "+e.isTrusted"
                        + ");"

                        + "console.log("
                        + "'🖱 defaultPrevented='"
                        + "+e.defaultPrevented"
                        + ");"

                        + "},true);"

                        // TOUCH
                        + "document.addEventListener("
                        + "'touchstart',"
                        + "function(e){"
                        + "console.log("
                        + "'👆 JS TOUCHSTART'"
                        + ");"
                        + "},true);"

                        + "document.addEventListener("
                        + "'touchend',"
                        + "function(e){"
                        + "console.log("
                        + "'👆 JS TOUCHEND'"
                        + ");"
                        + "},true);"

                        // POINTER
                        + "document.addEventListener("
                        + "'pointerdown',"
                        + "function(e){"
                        + "console.log("
                        + "'👉 POINTERDOWN trusted='"
                        + "+e.isTrusted"
                        + ");"
                        + "},true);"

                        + "document.addEventListener("
                        + "'pointerup',"
                        + "function(e){"
                        + "console.log("
                        + "'👉 POINTERUP trusted='"
                        + "+e.isTrusted"
                        + ");"
                        + "},true);"

                        // SCROLL
                        + "window.addEventListener("
                        + "'scroll',"
                        + "function(){"
                        + "console.log("
                        + "'📜 SCROLL x='"
                        + "+window.scrollX"
                        + "+' y='"
                        + "+window.scrollY"
                        + ");"
                        + "},true);"

                        // RESIZE
                        + "window.addEventListener("
                        + "'resize',"
                        + "function(){"
                        + "console.log("
                        + "'📐 RESIZE '"
                        + "+window.innerWidth"
                        + "+'x'"
                        + "+window.innerHeight"
                        + ");"
                        + "},true);"

                        // MUTATION
                        + "var observer="
                        + "new MutationObserver("
                        + "function(list){"

                        + "console.log("
                        + "'🔄 DOM MUTATION count='"
                        + "+list.length"
                        + ");"

                        + "list.forEach(function(m){"

                        + "console.log("
                        + "'🔄 mutation type='"
                        + "+m.type"
                        + "+' added='"
                        + "+m.addedNodes.length"
                        + "+' removed='"
                        + "+m.removedNodes.length"
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

                        // PERIODIC STATE
                        + "window.__lamoushi_state=setInterval("
                        + "function(){"

                        + "var t=document.getElementById('trigger');"

                        + "if(t){"

                        + "var r=t.getBoundingClientRect();"

                        + "console.log("
                        + "'📦 TRIGGER RECT '"
                        + "+JSON.stringify({"
                        + "x:r.x,"
                        + "y:r.y,"
                        + "w:r.width,"
                        + "h:r.height"
                        + "})"
                        + ");"

                        + "}"

                        + "},2000);"

                        + "return 'installed';"

                        + "})();";

        hostWebView.evaluateJavascript(
                js,
                value -> log(
                        "🔬 HOST DIAGNOSTICS = "
                                + value
                )
        );
    }

    // ============================================================
    // POP JS DIAGNOSTICS
    // ============================================================

    private void installPopDiagnostics() {

        if (popWebView == null) {
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
                        + "'🖱 POP CLICK target='"
                        + "+(e.target&&e.target.tagName)"
                        + "+' trusted='"
                        + "+e.isTrusted"
                        + ");"

                        + "},true);"

                        + "window.addEventListener("
                        + "'load',"
                        + "function(){"
                        + "console.log('🔵 POP WINDOW LOAD');"
                        + "});"

                        + "return 'installed';"

                        + "})();";

        popWebView.evaluateJavascript(
                js,
                value -> log(
                        "🔬 POP DIAGNOSTICS = "
                                + value
                )
        );
    }

    // ============================================================
    // CLOSE
    // ============================================================

    @UsedByGodot
    public void closeRewardedAd() {

        activity.runOnUiThread(() -> {

            separator();

            log("🛑 CLOSE REWARDED");

            handler.removeCallbacksAndMessages(null);

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
                    "🗑 DESTROY POP"
            );

            if (popWebView.getParent()
                    instanceof ViewGroup) {

                ((ViewGroup)
                        popWebView.getParent())
                        .removeView(
                                popWebView
                        );
            }

            popWebView.stopLoading();

            popWebView.clearHistory();

            popWebView.clearCache(false);

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
                    "🗑 DESTROY HOST"
            );

            if (hostWebView.getParent()
                    instanceof ViewGroup) {

                ((ViewGroup)
                        hostWebView.getParent())
                        .removeView(
                                hostWebView
                        );
            }

            hostWebView.stopLoading();

            hostWebView.clearHistory();

            hostWebView.clearCache(false);

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

        handler.removeCallbacksAndMessages(null);

        activity.runOnUiThread(() -> {

            destroyPop();
            destroyHost();
        });
    }
}
