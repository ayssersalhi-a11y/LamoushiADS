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
    private boolean rewardWaiting = false;
    private boolean adOpened = false;

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
    // LOAD
    // ============================================================

    @UsedByGodot
    public void loadRewardedAd() {

        activity.runOnUiThread(() -> {

            emitSignal(
                    "reward_debug",
                    "=================================================="
            );

            emitSignal(
                    "reward_debug",
                    "🚀 LOAD REWARDED START"
            );

            rewardGranted = false;
            rewardWaiting = false;
            adOpened = false;

            handler.removeCallbacksAndMessages(null);

            destroyPop();
            destroyHost();

            emitSignal(
                    "reward_debug",
                    "🌐 AD_SCRIPT_URL = " + AD_SCRIPT_URL
            );

            emitSignal(
                    "reward_debug",
                    "🌐 AD_BASE_URL = " + AD_BASE_URL
            );

            // ========================================================
            // COOKIES
            // ========================================================

            CookieManager cm =
                    CookieManager.getInstance();

            cm.setAcceptCookie(true);

            emitSignal(
                    "reward_debug",
                    "🍪 AcceptCookie = true"
            );

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.LOLLIPOP) {

                emitSignal(
                        "reward_debug",
                        "🍪 Third-party cookies will be enabled"
                );
            }

            // ========================================================
            // HOST WEBVIEW
            // ========================================================

            hostWebView =
                    new WebView(activity);

            WebSettings settings =
                    hostWebView.getSettings();

            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);

            settings.setSupportMultipleWindows(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);

            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );

            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);

            settings.setDatabaseEnabled(true);

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

            // ========================================================
            // HOST CHROME CLIENT
            // ========================================================

            hostWebView.setWebChromeClient(
                    new WebChromeClient() {

                        @Override
                        public boolean onConsoleMessage(
                                ConsoleMessage message
                        ) {

                            emitSignal(
                                    "reward_debug",
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

                        // ====================================================
                        // WINDOW.OPEN
                        // ====================================================

                        @Override
                        public boolean onCreateWindow(
                                WebView view,
                                boolean isDialog,
                                boolean isUserGesture,
                                android.os.Message resultMsg
                        ) {

                            emitSignal(
                                    "reward_debug",
                                    "=================================================="
                            );

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

                            adOpened = true;

                            // ------------------------------------------------
                            // إنشاء POP
                            // ------------------------------------------------

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

                            ps.setAllowFileAccess(true);
                            ps.setAllowContentAccess(true);

                            ps.setDatabaseEnabled(true);

                            if (Build.VERSION.SDK_INT >=
                                    Build.VERSION_CODES.LOLLIPOP) {

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

                                            emitSignal(
                                                    "reward_debug",
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

                                            emitSignal(
                                                    "reward_debug",
                                                    "🔵 POP PAGE START | "
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
                                                    "✅ POP PAGE FINISHED | "
                                                            + url
                                            );

                                            inspectPopDocument(view);
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
                                                WebResourceResponse response
                                        ) {

                                            if (request != null &&
                                                    response != null) {

                                                emitSignal(
                                                        "reward_debug",
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
                                                        "➡️ POP NAVIGATION | "
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

                                            logRequest(
                                                    "POP",
                                                    request
                                            );

                                            return super
                                                    .shouldInterceptRequest(
                                                            view,
                                                            request
                                                    );
                                        }
                                    }
                            );

                            // =================================================
                            // POP مخفي
                            // =================================================

                            FrameLayout.LayoutParams lp =
                                    new FrameLayout.LayoutParams(
                                            1,
                                            1
                                    );

                            activity.addContentView(
                                    popWebView,
                                    lp
                            );

                            // =================================================
                            // تمرير POP إلى window.open
                            // =================================================

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
                                    "reward_debug",
                                    "🎯 AD OPENED EVENT"
                            );

                            emitSignal(
                                    "reward_ad_opened"
                            );

                            scheduleReward();

                            emitSignal(
                                    "reward_debug",
                                    "=================================================="
                            );

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

                            installJavaScriptDiagnostics(view);

                            inspectHostDocument(view);
                        }

                        // ====================================================
                        // HOST ERROR
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

                        // ====================================================
                        // HOST HTTP ERROR
                        // ====================================================

                        @Override
                        public void onReceivedHttpError(
                                WebView view,
                                WebResourceRequest request,
                                WebResourceResponse response
                        ) {

                            if (request != null &&
                                    response != null) {

                                emitSignal(
                                        "reward_debug",
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
                                            + " | error="
                                            + error.getPrimaryError()
                                            + " | url="
                                            + error.getUrl()
                            );

                            sslHandler.cancel();
                        }

                        // ====================================================
                        // NAVIGATION
                        // ====================================================

                        @Override
                        public boolean shouldOverrideUrlLoading(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            if (request != null) {

                                emitSignal(
                                        "reward_debug",
                                        "➡️ HOST NAVIGATION | "
                                                + request.getUrl()
                                );
                            }

                            return false;
                        }

                        // ====================================================
                        // REQUEST LOGGER
                        // ====================================================

                        @Override
                        public WebResourceResponse
                        shouldInterceptRequest(
                                WebView view,
                                WebResourceRequest request
                        ) {

                            logRequest(
                                    "HOST",
                                    request
                            );

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
                            + "<meta charset='UTF-8'>"
                            + "<meta name='viewport' "
                            + "content='width=device-width,initial-scale=1.0'>"

                            + "<style>"
                            + "html,body{"
                            + "margin:0;"
                            + "padding:0;"
                            + "background:transparent;"
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
                            + "console.log("
                            + "'🔬 HTML loaded'"
                            + ");"

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

            emitSignal(
                    "reward_debug",
                    "📄 HTML الإعلان تم إنشاؤه"
            );

            emitSignal(
                    "reward_debug",
                    "📡 جاري طلب سكربت الإعلان: "
                            + AD_SCRIPT_URL
            );

            // ========================================================
            // LOAD DATA
            // ========================================================

            hostWebView.loadDataWithBaseURL(
                    AD_BASE_URL,
                    html,
                    "text/html",
                    "UTF-8",
                    null
            );

            // ========================================================
            // HOST مخفي
            // ========================================================

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
    // JAVASCRIPT DIAGNOSTICS
    // ============================================================

    private void installJavaScriptDiagnostics(
            WebView view
    ) {

        emitSignal(
                "reward_debug",
                "🔬 تثبيت JavaScript diagnostics"
        );

        String js =
                "(function(){"

                        + "if(window.__LAMOUSHI_DIAGNOSTICS__)"
                        + "return;"

                        + "window.__LAMOUSHI_DIAGNOSTICS__=true;"

                        // --------------------------------------------
                        // window.open
                        // --------------------------------------------

                        + "try{"
                        + "var oldOpen=window.open;"
                        + "window.open=function(){"

                        + "console.log("
                        + "'🚨 window.open CALLED'"
                        + ");"

                        + "try{"
                        + "console.log("
                        + "'window.open args=' + "
                        + "JSON.stringify("
                        + "Array.prototype.slice.call(arguments)"
                        + ")"
                        + ");"
                        + "}catch(e){"
                        + "console.log("
                        + "'window.open args serialization failed'"
                        + ");"
                        + "}"

                        + "var result=oldOpen.apply(this,arguments);"

                        + "console.log("
                        + "'window.open returned=' + "
                        + "(result ? 'OBJECT' : 'NULL')"
                        + ");"

                        + "return result;"
                        + "};"
                        + "}catch(e){"
                        + "console.log("
                        + "'❌ window.open hook failed: ' + e"
                        + ");"
                        + "}"

                        // --------------------------------------------
                        // click monitoring
                        // --------------------------------------------

                        + "document.addEventListener("
                        + "'click',"
                        + "function(e){"

                        + "console.log("
                        + "'🖱 CLICK'"
                        + " + ' target=' + "
                        + "(e.target ? e.target.tagName : 'null')"
                        + ");"

                        + "},"
                        + "true"
                        + ");"

                        // --------------------------------------------
                        // DOM mutation observer
                        // --------------------------------------------

                        + "try{"

                        + "var observer="
                        + "new MutationObserver(function(mutations){"

                        + "console.log("
                        + "'🔄 DOM MUTATION count=' + "
                        + "mutations.length"
                        + ");"

                        + "});"

                        + "observer.observe("
                        + "document.documentElement,"
                        + "{childList:true,subtree:true,attributes:true}"
                        + ");"

                        + "}catch(e){"

                        + "console.log("
                        + "'❌ MutationObserver failed: ' + e"
                        + ");"

                        + "}"

                        // --------------------------------------------
                        // location monitoring
                        // --------------------------------------------

                        + "console.log("
                        + "'🔬 diagnostics installed'"
                        + ");"

                        + "console.log("
                        + "'current href=' + location.href"
                        + ");"

                        + "})();";

        view.evaluateJavascript(
                js,
                value -> emitSignal(
                        "reward_debug",
                        "🔬 Diagnostics install result = "
                                + value
                )
        );
    }

    // ============================================================
    // HOST DOCUMENT INSPECTION
    // ============================================================

    private void inspectHostDocument(
            WebView view
    ) {

        String js =
                "(function(){"

                        + "var r={};"

                        + "try{"

                        + "r.href=location.href;"
                        + "r.origin=location.origin;"
                        + "r.readyState=document.readyState;"
                        + "r.title=document.title;"

                        + "r.bodyLength="
                        + "(document.body?"
                        + "document.body.innerHTML.length"
                        + ":-1);"

                        + "r.elementCount="
                        + "document.querySelectorAll('*').length;"

                        + "r.triggerExists="
                        + "!!document.getElementById('trigger');"

                        + "r.triggerTag="
                        + "(document.getElementById('trigger')?"
                        + "document.getElementById('trigger').tagName"
                        + ":null);"

                        + "r.iframes="
                        + "document.querySelectorAll('iframe').length;"

                        + "r.links="
                        + "document.querySelectorAll('a').length;"

                        + "r.buttons="
                        + "document.querySelectorAll('button').length;"

                        + "r.forms="
                        + "document.querySelectorAll('form').length;"

                        + "r.inputs="
                        + "document.querySelectorAll('input').length;"

                        + "r.scripts="
                        + "document.querySelectorAll('script').length;"

                        + "r.images="
                        + "document.querySelectorAll('img').length;"

                        + "r.bodyText="
                        + "(document.body?"
                        + "document.body.innerText.substring(0,500)"
                        + ":''"
                        + ");"

                        + "}catch(e){"
                        + "r.error=String(e);"
                        + "}"

                        + "return JSON.stringify(r);"

                        + "})();";

        view.evaluateJavascript(
                js,
                value -> emitSignal(
                        "reward_debug",
                        "🔬 HOST DOCUMENT INSPECTION = "
                                + value
                )
        );
    }

    // ============================================================
    // POP DOCUMENT INSPECTION
    // ============================================================

    private void inspectPopDocument(
            WebView view
    ) {

        String js =
                "(function(){"

                        + "var r={};"

                        + "try{"

                        + "r.href=location.href;"
                        + "r.origin=location.origin;"
                        + "r.readyState=document.readyState;"
                        + "r.title=document.title;"

                        + "r.bodyLength="
                        + "(document.body?"
                        + "document.body.innerHTML.length"
                        + ":-1);"

                        + "r.iframes="
                        + "document.querySelectorAll('iframe').length;"

                        + "r.links="
                        + "document.querySelectorAll('a').length;"

                        + "r.scripts="
                        + "document.querySelectorAll('script').length;"

                        + "r.elements="
                        + "document.querySelectorAll('*').length;"

                        + "}catch(e){"
                        + "r.error=String(e);"
                        + "}"

                        + "return JSON.stringify(r);"

                        + "})();";

        view.evaluateJavascript(
                js,
                value -> emitSignal(
                        "reward_debug",
                        "🔬 POP DOCUMENT INSPECTION = "
                                + value
                )
        );
    }

    // ============================================================
    // REQUEST LOGGER
    // ============================================================

    private void logRequest(
            String source,
            WebResourceRequest request
    ) {

        if (request == null) {
            return;
        }

        emitSignal(
                "reward_debug",
                "=================================================="
        );

        emitSignal(
                "reward_debug",
                "🌐 " + source
                        + " REQUEST | "
                        + request.getMethod()
                        + " "
                        + request.getUrl()
        );

        emitSignal(
                "reward_debug",
                source
                        + " isForMainFrame="
                        + request.isForMainFrame()
        );

        emitSignal(
                "reward_debug",
                source
                        + " hasGesture="
                        + request.hasGesture()
        );

        try {

            if (request.getRequestHeaders() != null) {

                for (
                        String key :
                        request.getRequestHeaders().keySet()
                ) {

                    String value =
                            request.getRequestHeaders().get(key);

                    if (key == null) {
                        continue;
                    }

                    // نعرض أهم الرؤوس فقط
                    if (
                            key.equalsIgnoreCase("Referer")
                                    ||
                            key.equalsIgnoreCase("User-Agent")
                                    ||
                            key.equalsIgnoreCase("Accept")
                    ) {

                        emitSignal(
                                "reward_debug",
                                source
                                        + " HEADER | "
                                        + key
                                        + " = "
                                        + value
                        );
                    }
                }
            }

        } catch (Exception e) {

            emitSignal(
                    "reward_debug",
                    "⚠️ Header logging error = "
                            + e.getMessage()
            );
        }

        emitSignal(
                "reward_debug",
                "=================================================="
        );
    }

    // ============================================================
    // SHOW
    // ============================================================

    @UsedByGodot
    public void showRewardedAd() {

        activity.runOnUiThread(() -> {

            emitSignal(
                    "reward_debug",
                    "=================================================="
            );

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

            if (rewardWaiting) {

                emitSignal(
                        "reward_debug",
                        "⚠️ Reward timer يعمل بالفعل"
                );

                return;
            }

            // ====================================================
            // PRE-CLICK INSPECTION
            // ====================================================

            String preJs =
                    "(function(){"
                            + "var r={};"

                            + "r.href=location.href;"
                            + "r.readyState=document.readyState;"

                            + "var el="
                            + "document.getElementById('trigger');"

                            + "r.triggerExists=!!el;"

                            + "if(el){"

                            + "r.triggerTag=el.tagName;"
                            + "r.triggerId=el.id;"
                            + "r.triggerClass=el.className;"

                            + "try{"
                            + "r.triggerRect="
                            + "JSON.stringify("
                            + "el.getBoundingClientRect()"
                            + ");"
                            + "}catch(e){}"

                            + "}"

                            + "r.iframes="
                            + "document.querySelectorAll('iframe').length;"

                            + "r.links="
                            + "document.querySelectorAll('a').length;"

                            + "r.scripts="
                            + "document.querySelectorAll('script').length;"

                            + "return JSON.stringify(r);"

                            + "})();";

            hostWebView.evaluateJavascript(
                    preJs,
                    value -> {

                        emitSignal(
                                "reward_debug",
                                "🔬 PRE-CLICK INSPECTION = "
                                        + value
                        );

                        dispatchRewardClick();
                    }
            );
        });
    }

    // ============================================================
    // DISPATCH CLICK
    // ============================================================

    private void dispatchRewardClick() {

        if (hostWebView == null) {

            emitSignal(
                    "reward_debug",
                    "❌ Cannot dispatch: HOST = null"
            );

            return;
        }

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
                        + "view:window,"
                        + "detail:1"

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

                    emitSignal(
                            "reward_debug",
                            "👆 CLICK DISPATCH RESULT = "
                                    + value
                    );

                    // ------------------------------------------------
                    // POST CLICK
                    // ------------------------------------------------

                    handler.postDelayed(
                            () -> inspectAfterClick(),
                            1500
                    );
                }
        );
    }

    // ============================================================
    // POST CLICK INSPECTION
    // ============================================================

    private void inspectAfterClick() {

        if (hostWebView == null) {
            return;
        }

        emitSignal(
                "reward_debug",
                "=================================================="
        );

        emitSignal(
                "reward_debug",
                "🔬 POST-CLICK INSPECTION START"
        );

        String js =
                "(function(){"

                        + "var r={};"

                        + "try{"

                        + "r.href=location.href;"
                        + "r.readyState=document.readyState;"
                        + "r.title=document.title;"

                        + "r.bodyLength="
                        + "(document.body?"
                        + "document.body.innerHTML.length"
                        + ":-1);"

                        + "r.elements="
                        + "document.querySelectorAll('*').length;"

                        + "r.iframes="
                        + "document.querySelectorAll('iframe').length;"

                        + "r.links="
                        + "document.querySelectorAll('a').length;"

                        + "r.buttons="
                        + "document.querySelectorAll('button').length;"

                        + "r.forms="
                        + "document.querySelectorAll('form').length;"

                        + "r.inputs="
                        + "document.querySelectorAll('input').length;"

                        + "r.scripts="
                        + "document.querySelectorAll('script').length;"

                        + "r.images="
                        + "document.querySelectorAll('img').length;"

                        + "r.triggerExists="
                        + "!!document.getElementById('trigger');"

                        + "r.bodyText="
                        + "(document.body?"
                        + "document.body.innerText.substring(0,1000)"
                        + ":''"
                        + ");"

                        + "}catch(e){"

                        + "r.error=String(e);"

                        + "}"

                        + "return JSON.stringify(r);"

                        + "})();";

        hostWebView.evaluateJavascript(
                js,
                value -> {

                    emitSignal(
                            "reward_debug",
                            "🔬 POST-CLICK DOM = "
                                    + value
                    );

                    emitSignal(
                            "reward_debug",
                            "🔬 POST-CLICK INSPECTION END"
                    );

                    emitSignal(
                            "reward_debug",
                            "=================================================="
                    );
                }
        );
    }

    // ============================================================
    // REWARD TIMER
    // ============================================================

    private void scheduleReward() {

        if (rewardWaiting || rewardGranted) {

            emitSignal(
                    "reward_debug",
                    "⚠️ scheduleReward ignored"
            );

            return;
        }

        rewardWaiting = true;

        emitSignal(
                "reward_debug",
                "⏳ بدء مؤقت المكافأة = "
                        + REWARD_DELAY_MS
                        + " ms"
        );

        handler.postDelayed(
                () -> {

                    rewardWaiting = false;

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

            emitSignal(
                    "reward_debug",
                    "🛑 closeRewardedAd()"
            );

            handler.removeCallbacksAndMessages(null);

            rewardWaiting = false;

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

            emitSignal(
                    "reward_debug",
                    "🧹 Destroying POP WebView"
            );

            try {

                if (popWebView.getParent() != null) {

                    ((ViewGroup)
                            popWebView.getParent())
                            .removeView(popWebView);
                }

                popWebView.stopLoading();
                popWebView.loadUrl("about:blank");
                popWebView.clearHistory();
                popWebView.destroy();

            } catch (Exception e) {

                emitSignal(
                        "reward_debug",
                        "⚠️ POP destroy error = "
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

            emitSignal(
                    "reward_debug",
                    "🧹 Destroying HOST WebView"
            );

            try {

                if (hostWebView.getParent() != null) {

                    ((ViewGroup)
                            hostWebView.getParent())
                            .removeView(hostWebView);
                }

                hostWebView.stopLoading();
                hostWebView.loadUrl("about:blank");
                hostWebView.clearHistory();
                hostWebView.destroy();

            } catch (Exception e) {

                emitSignal(
                        "reward_debug",
                        "⚠️ HOST destroy error = "
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

        rewardWaiting = false;

        destroyPop();
        destroyHost();
    }
}
