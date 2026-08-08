package com.lamoushi.ads;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.SslError;
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
    // DEBUG SIGNAL
    // ============================================================

    private void debug(String message) {
        emitSignal("reward_debug", message);
    }

    private void separator() {
        debug("==================================================");
    }

    // ============================================================
    // LOAD
    // ============================================================

    @UsedByGodot
    public void loadRewardedAd() {

        activity.runOnUiThread(() -> {

            separator();

            debug("🚀 LOAD REWARDED START");

            rewardGranted = false;

            destroyPop();
            destroyHost();

            debug("🌐 AD_SCRIPT_URL = " + AD_SCRIPT_URL);
            debug("🌐 AD_BASE_URL = " + AD_BASE_URL);

            // --------------------------------------------------------
            // COOKIES
            // --------------------------------------------------------

            CookieManager cookies =
                    CookieManager.getInstance();

            cookies.setAcceptCookie(true);

            debug("🍪 AcceptCookie = true");

            // --------------------------------------------------------
            // HOST WEBVIEW
            // --------------------------------------------------------

            hostWebView =
                    new WebView(activity);

            WebSettings settings =
                    hostWebView.getSettings();

            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);

            settings.setSupportMultipleWindows(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);

            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.LOLLIPOP) {

                settings.setMixedContentMode(
                        WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                );

                cookies.setAcceptThirdPartyCookies(
                        hostWebView,
                        true
                );

                debug("🍪 Third-party cookies = true");
            }

            // ========================================================
            // HOST CHROME
            // ========================================================

            hostWebView.setWebChromeClient(
                    new WebChromeClient() {

                        @Override
                        public boolean onConsoleMessage(
                                ConsoleMessage message
                        ) {

                            debug(
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

                            separator();

                            debug("🪟 HOST onCreateWindow()");

                            debug(
                                    "🖱 isUserGesture = "
                                            + isUserGesture
                            );

                            debug(
                                    "📦 isDialog = "
                                            + isDialog
                            );

                            debug(
                                    "📍 current URL = "
                                            + view.getUrl()
                            );

                            /*
                             * هنا نعرف أن WebView نفسه طلب إنشاء
                             * نافذة جديدة.
                             *
                             * لا نقوم بإخفائها أو إجبارها على العمل
                             * خارج سلوك WebView الطبيعي.
                             */

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

                            ps.setAllowFileAccess(true);
                            ps.setAllowContentAccess(true);

                            if (Build.VERSION.SDK_INT >=
                                    Build.VERSION_CODES.LOLLIPOP) {

                                ps.setMixedContentMode(
                                        WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                );

                                CookieManager.getInstance()
                                        .setAcceptThirdPartyCookies(
                                                popWebView,
                                                true
                                        );
                            }

                            // =================================================
                            // POP CHROME
                            // =================================================

                            popWebView.setWebChromeClient(
                                    new WebChromeClient() {

                                        @Override
                                        public boolean onConsoleMessage(
                                                ConsoleMessage message
                                        ) {

                                            debug(
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
                                                android.os.Message resultMsg
                                        ) {

                                            debug(
                                                    "🪟 POP requested another window"
                                            );

                                            debug(
                                                    "🖱 POP gesture = "
                                                            + isUserGesture
                                            );

                                            return false;
                                        }
                                    }
                            );

                            // =================================================
                            // POP CLIENT
                            // =================================================

                            popWebView.setWebViewClient(
                                    new WebViewClient() {

                                        @Override
                                        public void onPageStarted(
                                                WebView view,
                                                String url,
                                                android.graphics.Bitmap favicon
                                        ) {

                                            separator();

                                            debug(
                                                    "🔵 POP PAGE START | "
                                                            + url
                                            );
                                        }

                                        @Override
                                        public void onPageFinished(
                                                WebView view,
                                                String url
                                        ) {

                                            debug(
                                                    "✅ POP PAGE FINISHED | "
                                                            + url
                                            );

                                            inspectWebView(
                                                    view,
                                                    "POP"
                                            );
                                        }

                                        @Override
                                        public boolean shouldOverrideUrlLoading(
                                                WebView view,
                                                WebResourceRequest request
                                        ) {

                                            if (request != null) {

                                                debug(
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

                                            if (request != null &&
                                                    request.isForMainFrame()) {

                                                debug(
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
                                                WebResourceResponse response
                                        ) {

                                            if (request != null &&
                                                    response != null) {

                                                debug(
                                                        "⚠️ POP HTTP"
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
                                                SslErrorHandler handler,
                                                SslError error
                                        ) {

                                            debug(
                                                    "🔐 POP SSL ERROR"
                                                            + " | error="
                                                            + error.getPrimaryError()
                                                            + " | url="
                                                            + error.getUrl()
                                            );

                                            handler.cancel();
                                        }

                                        @Override
                                        public WebResourceResponse
                                        shouldInterceptRequest(
                                                WebView view,
                                                WebResourceRequest request
                                        ) {

                                            if (request != null) {

                                                debug(
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
                            // وضع WebView مرئي مؤقتًا للتشخيص
                            // ------------------------------------------------

                            FrameLayout.LayoutParams lp =
                                    new FrameLayout.LayoutParams(
                                            -1,
                                            400
                                    );

                            activity.addContentView(
                                    popWebView,
                                    lp
                            );

                            // ------------------------------------------------
                            // تسليم النافذة إلى WebView
                            // ------------------------------------------------

                            WebView.WebViewTransport transport =
                                    (WebView.WebViewTransport)
                                            resultMsg.obj;

                            transport.setWebView(
                                    popWebView
                            );

                            resultMsg.sendToTarget();

                            debug(
                                    "✅ window.open() تم استقباله"
                            );

                            emitSignal(
                                    "reward_ad_opened"
                            );

                            scheduleReward();

                            return true;
                        }
                    }
            );

            // ========================================================
            // HOST CLIENT
            // ========================================================

            hostWebView.setWebViewClient(
                    new WebViewClient() {

                        @Override
                        public void onPageStarted(
                                WebView view,
                                String url,
                                android.graphics.Bitmap favicon
                        ) {

                            debug(
                                    "🔵 HOST PAGE START | "
                                            + url
                            );
                        }

                        @Override
                        public void onPageFinished(
                                WebView view,
                                String url
                        ) {

                            separator();

                            debug(
                                    "✅ HOST PAGE FINISHED | "
                                            + url
                            );

                            inspectWebView(
                                    view,
                                    "HOST"
                            );

                            installDiagnostics(view);
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                debug(
                                        "➡️ HOST NAVIGATION"
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

                            debug(
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

                                debug(
                                        "⚠️ HOST HTTP"
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
                                SslErrorHandler handler,
                                SslError error
                        ) {

                            debug(
                                    "🔐 HOST SSL ERROR"
                                            + " | error="
                                            + error.getPrimaryError()
                                            + " | url="
                                            + error.getUrl()
                            );

                            handler.cancel();
                        }

                        @Override
                        public WebResourceResponse
                        shouldInterceptRequest(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                debug(
                                        "🌐 HOST REQUEST"
                                                + " | "
                                                + request.getMethod()
                                                + " "
                                                + request.getUrl()
                                );

                                debug(
                                        "HOST isForMainFrame="
                                                + request.isForMainFrame()
                                );

                                debug(
                                        "HOST hasGesture="
                                                + request.hasGesture()
                                );

                                if (request.getRequestHeaders() != null) {

                                    String ua =
                                            request.getRequestHeaders()
                                                    .get("User-Agent");

                                    String referer =
                                            request.getRequestHeaders()
                                                    .get("Referer");

                                    if (ua != null) {

                                        debug(
                                                "HOST HEADER User-Agent = "
                                                        + ua
                                        );
                                    }

                                    if (referer != null) {

                                        debug(
                                                "HOST HEADER Referer = "
                                                        + referer
                                        );
                                    }
                                }
                            }

                            return super
                                    .shouldInterceptRequest(
                                            view,
                                            request
                                    );
                        }
                    }
            );

            // ========================================================
            // HTML
            // ========================================================

            String html =
                    "<!DOCTYPE html>"
                            + "<html>"
                            + "<head>"
                            + "<meta name='viewport' "
                            + "content='width=device-width,initial-scale=1'>"

                            + "<style>"
                            + "html,body{"
                            + "margin:0;"
                            + "padding:0;"
                            + "width:100%;"
                            + "height:100%;"
                            + "overflow:hidden;"
                            + "}"
                            + "#trigger{"
                            + "position:fixed;"
                            + "left:0;"
                            + "top:0;"
                            + "width:100%;"
                            + "height:100%;"
                            + "}"
                            + "</style>"

                            + "</head>"

                            + "<body>"

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

            debug("📄 HTML الإعلان تم إنشاؤه");

            debug(
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

            // --------------------------------------------------------
            // HOST حجم صغير للتشخيص
            // --------------------------------------------------------

            FrameLayout.LayoutParams lp =
                    new FrameLayout.LayoutParams(
                            1,
                            1
                    );

            activity.addContentView(
                    hostWebView,
                    lp
            );

            debug(
                    "✅ HOST WebView تمت إضافته إلى Activity"
            );
        });
    }

    // ============================================================
    // JAVASCRIPT DIAGNOSTICS
    // ============================================================

    private void installDiagnostics(WebView view) {

        debug("🔬 تثبيت JavaScript diagnostics");

        String js =
                "(function(){"

                        // ------------------------------------------------
                        // window.open
                        // ------------------------------------------------

                        + "if(window.__lamoushiDiagInstalled)return;"
                        + "window.__lamoushiDiagInstalled=true;"

                        + "console.log('🔬 diagnostics installed');"

                        + "var oldOpen=window.open;"

                        + "window.open=function(){"

                        + "console.log("
                        + "'🪟 window.open CALLED'"
                        + ");"

                        + "console.log("
                        + "'window.open args=' + arguments.length"
                        + ");"

                        + "try{"
                        + "console.log("
                        + "'window.open url=' + arguments[0]"
                        + ");"
                        + "}catch(e){}"

                        + "var result=oldOpen.apply(this,arguments);"

                        + "console.log("
                        + "'window.open result=' + "
                        + "(result ? 'OBJECT' : 'NULL')"
                        + ");"

                        + "return result;"
                        + "};"

                        // ------------------------------------------------
                        // location
                        // ------------------------------------------------

                        + "console.log("
                        + "'current href=' + location.href"
                        + ");"

                        // ------------------------------------------------
                        // MutationObserver
                        // ------------------------------------------------

                        + "try{"

                        + "var observer="
                        + "new MutationObserver(function(list){"

                        + "console.log("
                        + "'🔄 DOM MUTATION count=' + list.length"
                        + ");"

                        + "list.forEach(function(m){"

                        + "if(m.addedNodes){"

                        + "for(var i=0;"
                        + "i<m.addedNodes.length;i++){"

                        + "var n=m.addedNodes[i];"

                        + "if(n.nodeType===1){"

                        + "console.log("
                        + "'➕ NODE ADDED tag=' + n.tagName"
                        + "+' id=' + (n.id||'')"
                        + "+' src=' + (n.src||'')"
                        + "+' href=' + (n.href||'')"
                        + ");"

                        + "}"

                        + "}"

                        + "}"

                        + "});"

                        + "});"

                        + "observer.observe("
                        + "document.documentElement,"
                        + "{childList:true,subtree:true}"
                        + ");"

                        + "}catch(e){"

                        + "console.log("
                        + "'❌ MutationObserver error=' + e"
                        + ");"

                        + "}"

                        // ------------------------------------------------
                        // click listener
                        // ------------------------------------------------

                        + "document.addEventListener("
                        + "'click',"
                        + "function(e){"

                        + "console.log("
                        + "'🖱 CLICK target=' + "
                        + "(e.target ? e.target.tagName : 'null')"
                        + ");"

                        + "console.log("
                        + "'🖱 CLICK href=' + "
                        + "(e.target ? (e.target.href||'') : '')"
                        + ");"

                        + "},"
                        + "true"
                        + ");"

                        + "})();";

        view.evaluateJavascript(
                js,
                value -> debug(
                        "🔬 Diagnostics install result = "
                                + value
                )
        );
    }

    // ============================================================
    // INSPECTION
    // ============================================================

    private void inspectWebView(
            WebView view,
            String name
    ) {

        String js =
                "(function(){"

                        + "function esc(s){"
                        + "return String(s||'')"
                        + ".replace(/\\\\/g,'\\\\\\\\')"
                        + ".replace(/\"/g,'\\\\\"')"
                        + ".replace(/\\n/g,' ');"
                        + "}"

                        + "var trigger="
                        + "document.getElementById('trigger');"

                        + "var result={"

                        + "href:location.href,"

                        + "origin:location.origin,"

                        + "readyState:document.readyState,"

                        + "title:document.title,"

                        + "bodyLength:"
                        + "(document.body?"
                        + "document.body.innerHTML.length:-1),"

                        + "elementCount:"
                        + "document.getElementsByTagName('*').length,"

                        + "triggerExists:!!trigger,"

                        + "triggerTag:"
                        + "(trigger?trigger.tagName:''),"

                        + "triggerId:"
                        + "(trigger?trigger.id:''),"

                        + "triggerClass:"
                        + "(trigger?trigger.className:''),"

                        + "iframes:"
                        + "document.getElementsByTagName('iframe').length,"

                        + "links:"
                        + "document.getElementsByTagName('a').length,"

                        + "buttons:"
                        + "document.getElementsByTagName('button').length,"

                        + "forms:"
                        + "document.getElementsByTagName('form').length,"

                        + "inputs:"
                        + "document.getElementsByTagName('input').length,"

                        + "scripts:"
                        + "document.getElementsByTagName('script').length,"

                        + "images:"
                        + "document.getElementsByTagName('img').length,"

                        + "bodyText:"
                        + "esc(document.body?"
                        + "document.body.innerText:'')"

                        + "};"

                        + "if(trigger){"

                        + "var r=trigger.getBoundingClientRect();"

                        + "result.triggerRect=JSON.stringify({"
                        + "x:r.x,"
                        + "y:r.y,"
                        + "width:r.width,"
                        + "height:r.height,"
                        + "top:r.top,"
                        + "right:r.right,"
                        + "bottom:r.bottom,"
                        + "left:r.left"
                        + "});"

                        + "}"

                        + "return JSON.stringify(result);"

                        + "})();";

        view.evaluateJavascript(
                js,
                value -> {

                    debug(
                            "🔬 "
                                    + name
                                    + " INSPECTION = "
                                    + value
                    );
                }
        );
    }

    // ============================================================
    // SHOW
    // ============================================================

    @UsedByGodot
    public void showRewardedAd() {

        activity.runOnUiThread(() -> {

            separator();

            debug("🎯 SHOW REWARDED START");

            if (hostWebView == null) {

                debug(
                        "❌ HOST WebView = null"
                );

                return;
            }

            // --------------------------------------------------------
            // قبل الضغط
            // --------------------------------------------------------

            inspectWebView(
                    hostWebView,
                    "PRE-CLICK"
            );

            // --------------------------------------------------------
            // تسجيل click فقط
            // --------------------------------------------------------

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
                            + "'trigger موجود — dispatching click'"
                            + ");"

                            + "var ev="
                            + "new MouseEvent('click',{"
                            + "bubbles:true,"
                            + "cancelable:true,"
                            + "view:window"
                            + "});"

                            + "var result="
                            + "el.dispatchEvent(ev);"

                            + "console.log("
                            + "'dispatchEvent result=' + result"
                            + ");"

                            + "return result;"

                            + "})();";

            hostWebView.evaluateJavascript(
                    js,
                    value -> {

                        debug(
                                "👆 CLICK DISPATCH RESULT = "
                                        + value
                        );

                        // ------------------------------------------------
                        // بعد الضغط مباشرة
                        // ------------------------------------------------

                        new Handler(
                                Looper.getMainLooper()
                        ).postDelayed(
                                () -> {

                                    separator();

                                    debug(
                                            "🔬 POST-CLICK INSPECTION START"
                                    );

                                    inspectWebView(
                                            hostWebView,
                                            "POST-CLICK"
                                    );

                                    debug(
                                            "🔬 POST-CLICK INSPECTION END"
                                    );

                                },
                                1500
                        );
                    }
            );
        });
    }

    // ============================================================
    // REWARD TIMER
    // ============================================================

    private void scheduleReward() {

        handler.removeCallbacksAndMessages(null);

        handler.postDelayed(
                () -> {

                    if (!rewardGranted) {

                        rewardGranted = true;

                        debug(
                                "🎁 REWARD TIMER FINISHED"
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

            debug(
                    "🛑 CLOSE REWARDED"
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

            try {

                if (popWebView.getParent() != null) {

                    ((ViewGroup)
                            popWebView.getParent())
                            .removeView(popWebView);
                }

                popWebView.stopLoading();
                popWebView.destroy();

            } catch (Exception e) {

                debug(
                        "⚠️ destroyPop error = "
                                + e.getMessage()
                );
            }

            popWebView = null;
        }
    }

    // ============================================================
    // DESTROY HOST
    // ============================================================

    private void destroyHost() {

        if (hostWebView != null) {

            try {

                if (hostWebView.getParent() != null) {

                    ((ViewGroup)
                            hostWebView.getParent())
                            .removeView(hostWebView);
                }

                hostWebView.stopLoading();
                hostWebView.destroy();

            } catch (Exception e) {

                debug(
                        "⚠️ destroyHost error = "
                                + e.getMessage()
                );
            }

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
