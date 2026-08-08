package com.lamoushi.ads;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ConsoleMessage;
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

    private Activity activity;

    private WebView hostWebView;
    private WebView popWebView;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean rewardGranted = false;
    private boolean loading = false;
    private boolean clickInProgress = false;

    private static final int REWARD_DELAY_MS = 10000;

    private static final String AD_SCRIPT_URL =
            "https://pl30736587.effectivecpmnetwork.com/25/46/ff/2546ffc01c441637dc550c6584facfe8.js";

    private static final String AD_BASE_URL =
            "https://pl30736587.effectivecpmnetwork.com/";

    public LamoushiRewarded(Godot godot) {
        super(godot);
        this.activity = godot.getActivity();
    }

    // ============================================================
    // PLUGIN
    // ============================================================

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
    // DEBUG LOGGER
    // ============================================================

    private void log(String message) {

        emitSignal(
                "reward_debug",
                message
        );
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

            loading = true;
            clickInProgress = false;
            rewardGranted = false;

            destroyPop();
            destroyHost();

            log(
                    "🌐 AD_SCRIPT_URL = "
                            + AD_SCRIPT_URL
            );

            log(
                    "🌐 AD_BASE_URL = "
                            + AD_BASE_URL
            );

            // ========================================================
            // COOKIE
            // ========================================================

            CookieManager cm =
                    CookieManager.getInstance();

            cm.setAcceptCookie(true);

            log(
                    "🍪 AcceptCookie = true"
            );

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.LOLLIPOP) {

                log(
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

            settings.setJavaScriptCanOpenWindowsAutomatically(
                    true
            );

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

                log(
                        "🍪 Third-party cookies = true"
                );
            }

            // ========================================================
            // CHROME CLIENT
            // ========================================================

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

                            log(
                                    "📍 HOST current URL = "
                                            + view.getUrl()
                            );

                            // =================================================
                            // POP WEBVIEW
                            // =================================================

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

                            if (Build.VERSION.SDK_INT >=
                                    Build.VERSION_CODES.LOLLIPOP) {

                                CookieManager
                                        .getInstance()
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
                                                android.os.Message resultMsg
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
                                                Bitmap favicon
                                        ) {

                                            log(
                                                    "🔵 POP PAGE START"
                                                            + " | "
                                                            + url
                                            );
                                        }

                                        @Override
                                        public void onPageFinished(
                                                WebView view,
                                                String url
                                        ) {

                                            log(
                                                    "✅ POP PAGE FINISHED"
                                                            + " | "
                                                            + url
                                            );

                                            inspectWebView(
                                                    view,
                                                    "POP PAGE INSPECTION"
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

                                                log(
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

                                                log(
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

                                            log(
                                                    "🔐 POP SSL ERROR"
                                                            + " | code="
                                                            + error.getPrimaryError()
                                                            + " | url="
                                                            + error.getUrl()
                                            );

                                            handler.cancel();
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

                                                log(
                                                        "➡️ POP gesture="
                                                                + request.hasGesture()
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
                                                        "🌐 POP REQUEST"
                                                                + " | "
                                                                + request.getMethod()
                                                                + " "
                                                                + request.getUrl()
                                                );

                                                logHeaders(
                                                        "POP",
                                                        request
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

                            // =================================================
                            // ADD POP
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

                            WebView.WebViewTransport transport =
                                    (WebView.WebViewTransport)
                                            resultMsg.obj;

                            transport.setWebView(
                                    popWebView
                            );

                            resultMsg.sendToTarget();

                            log(
                                    "✅ window.open() تم تسليمه إلى Pop WebView"
                            );

                            emitSignal(
                                    "reward_ad_opened"
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
                                Bitmap favicon
                        ) {

                            log(
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

                            log(
                                    "✅ HOST PAGE FINISHED | "
                                            + url
                            );

                            loading = false;

                            installDiagnostics(view);

                            inspectWebView(
                                    view,
                                    "HOST DOCUMENT INSPECTION"
                            );

                            separator();
                        }

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

                            log(
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
                                WebResourceResponse response
                        ) {

                            if (request != null &&
                                    response != null) {

                                log(
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

                            log(
                                    "🔐 HOST SSL ERROR"
                                            + " | code="
                                            + error.getPrimaryError()
                                            + " | url="
                                            + error.getUrl()
                            );

                            handler.cancel();
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

                                log(
                                        "➡️ HOST gesture="
                                                + request.hasGesture()
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

                                separator();

                                log(
                                        "🌐 HOST REQUEST"
                                                + " | "
                                                + request.getMethod()
                                                + " "
                                                + request.getUrl()
                                );

                                log(
                                        "HOST isForMainFrame="
                                                + request.isForMainFrame()
                                );

                                log(
                                        "HOST hasGesture="
                                                + request.hasGesture()
                                );

                                logHeaders(
                                        "HOST",
                                        request
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

                            + "</head>"

                            + "<body>"

                            + "<div id='trigger'></div>"

                            + "<script>"
                            + "console.log('🔬 HTML loaded');"
                            + "console.log('trigger موجود = ' + !!document.getElementById('trigger'));"
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
                    "📡 جاري طلب سكربت الإعلان: "
                            + AD_SCRIPT_URL
            );

            // ========================================================
            // LOAD
            // ========================================================

            hostWebView.loadDataWithBaseURL(
                    AD_BASE_URL,
                    html,
                    "text/html",
                    "UTF-8",
                    null
            );

            // ========================================================
            // HOST HIDDEN
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

            log(
                    "✅ HOST WebView تمت إضافته إلى Activity"
            );

            separator();
        });
    }

    // ============================================================
    // INSTALL DEEP JAVASCRIPT DIAGNOSTICS
    // ============================================================

    private void installDiagnostics(
            WebView view
    ) {

        log(
                "🔬 تثبيت JavaScript diagnostics"
        );

        String js =

                "(function(){"

                        // =================================================
                        // منع التثبيت مرتين
                        // =================================================

                        + "if(window.__LAMOUSHI_DIAG){"
                        + "console.log('diagnostics already installed');"
                        + "return;"
                        + "}"
                        + "window.__LAMOUSHI_DIAG=true;"

                        // =================================================
                        // window.open
                        // =================================================

                        + "try{"
                        + "var originalOpen=window.open;"
                        + "window.open=function(){"
                        + "console.log('🚨 window.open CALLED');"
                        + "console.log('window.open args='+arguments.length);"
                        + "for(var i=0;i<arguments.length;i++){"
                        + "try{console.log('window.open arg['+i+']='+String(arguments[i]));}catch(e){}"
                        + "}"
                        + "var result=originalOpen.apply(this,arguments);"
                        + "console.log('🚨 window.open RETURN='+result);"
                        + "return result;"
                        + "};"
                        + "}catch(e){"
                        + "console.log('window.open hook ERROR='+e);"
                        + "}"

                        // =================================================
                        // location.assign
                        // =================================================

                        + "try{"
                        + "var originalAssign=window.location.assign;"
                        + "window.location.assign=function(url){"
                        + "console.log('🚨 location.assign='+url);"
                        + "return originalAssign.call(this,url);"
                        + "};"
                        + "}catch(e){"
                        + "console.log('location.assign hook ERROR='+e);"
                        + "}"

                        // =================================================
                        // location.replace
                        // =================================================

                        + "try{"
                        + "var originalReplace=window.location.replace;"
                        + "window.location.replace=function(url){"
                        + "console.log('🚨 location.replace='+url);"
                        + "return originalReplace.call(this,url);"
                        + "};"
                        + "}catch(e){"
                        + "console.log('location.replace hook ERROR='+e);"
                        + "}"

                        // =================================================
                        // appendChild
                        // =================================================

                        + "try{"
                        + "var originalAppend=Node.prototype.appendChild;"
                        + "Node.prototype.appendChild=function(node){"
                        + "try{"
                        + "if(node){"
                        + "console.log('🧩 appendChild tag='+node.tagName);"
                        + "if(node.tagName==='IFRAME'){"
                        + "console.log('🚨 IFRAME APPENDED src='+node.src);"
                        + "}"
                        + "if(node.tagName==='A'){"
                        + "console.log('🔗 A APPENDED href='+node.href);"
                        + "}"
                        + "}"
                        + "}catch(e){}"
                        + "return originalAppend.call(this,node);"
                        + "};"
                        + "}catch(e){"
                        + "console.log('appendChild hook ERROR='+e);"
                        + "}"

                        // =================================================
                        // createElement
                        // =================================================

                        + "try{"
                        + "var originalCreate=document.createElement;"
                        + "document.createElement=function(name){"
                        + "var el=originalCreate.call(this,name);"
                        + "try{"
                        + "if(String(name).toLowerCase()==='iframe'){"
                        + "console.log('🧩 createElement(IFRAME)');"
                        + "}"
                        + "if(String(name).toLowerCase()==='a'){"
                        + "console.log('🔗 createElement(A)');"
                        + "}"
                        + "}catch(e){}"
                        + "return el;"
                        + "};"
                        + "}catch(e){"
                        + "console.log('createElement hook ERROR='+e);"
                        + "}"

                        // =================================================
                        // click diagnostics
                        // =================================================

                        + "document.addEventListener('click',function(e){"
                        + "try{"
                        + "console.log('🖱 CLICK target='+e.target.tagName);"
                        + "console.log('🖱 CLICK trusted='+e.isTrusted);"
                        + "console.log('🖱 CLICK defaultPrevented='+e.defaultPrevented);"
                        + "console.log('🖱 CLICK client='+e.clientX+','+e.clientY);"
                        + "}catch(x){}"
                        + "},true);"

                        // =================================================
                        // visibility
                        // =================================================

                        + "document.addEventListener('visibilitychange',function(){"
                        + "console.log('👁 visibility='+document.visibilityState);"
                        + "},true);"

                        // =================================================
                        // mutation observer
                        // =================================================

                        + "try{"
                        + "var observer=new MutationObserver(function(list){"
                        + "console.log('🔄 DOM MUTATION count='+list.length);"
                        + "for(var i=0;i<list.length;i++){"
                        + "var m=list[i];"
                        + "console.log('🔄 mutation type='+m.type+' added='+m.addedNodes.length+' removed='+m.removedNodes.length);"
                        + "}"
                        + "});"
                        + "observer.observe(document.documentElement,{"
                        + "subtree:true,"
                        + "childList:true,"
                        + "attributes:true"
                        + "});"
                        + "}catch(e){"
                        + "console.log('MutationObserver ERROR='+e);"
                        + "}"

                        // =================================================
                        // error
                        // =================================================

                        + "window.addEventListener('error',function(e){"
                        + "console.log('💥 WINDOW ERROR='+e.message);"
                        + "console.log('💥 ERROR FILE='+e.filename);"
                        + "console.log('💥 ERROR LINE='+e.lineno);"
                        + "},true);"

                        // =================================================
                        // unhandled rejection
                        // =================================================

                        + "window.addEventListener('unhandledrejection',function(e){"
                        + "console.log('💥 UNHANDLED PROMISE='+e.reason);"
                        + "},true);"

                        + "console.log('🔬 diagnostics installed');"
                        + "console.log('current href='+location.href);"

                + "})();";

        view.evaluateJavascript(
                js,
                value -> log(
                        "🔬 Diagnostics install result = "
                                + value
                )
        );
    }

    // ============================================================
    // INSPECT WEBVIEW
    // ============================================================

    private void inspectWebView(
            WebView view,
            String label
    ) {

        if (view == null) {
            log(
                    "❌ "
                            + label
                            + " | WebView=null"
            );
            return;
        }

        String js =

                "(function(){"

                        + "function esc(v){"
                        + "try{return JSON.stringify(v);}catch(e){return 'ERR';}"
                        + "}"

                        + "var t=document.getElementById('trigger');"

                        + "var r=null;"

                        + "if(t){"
                        + "try{"
                        + "var x=t.getBoundingClientRect();"
                        + "r={"
                        + "x:x.x,"
                        + "y:x.y,"
                        + "width:x.width,"
                        + "height:x.height,"
                        + "top:x.top,"
                        + "right:x.right,"
                        + "bottom:x.bottom,"
                        + "left:x.left"
                        + "};"
                        + "}catch(e){"
                        + "r='ERROR';"
                        + "}"
                        + "}"

                        + "return JSON.stringify({"

                        + "href:location.href,"
                        + "origin:location.origin,"
                        + "readyState:document.readyState,"
                        + "title:document.title,"
                        + "visibility:document.visibilityState,"

                        + "bodyLength:document.body"
                        + "?document.body.innerHTML.length:-1,"

                        + "elementCount:document.getElementsByTagName('*').length,"

                        + "triggerExists:!!t,"
                        + "triggerTag:t?t.tagName:null,"
                        + "triggerId:t?t.id:null,"
                        + "triggerClass:t?t.className:null,"
                        + "triggerRect:r,"

                        + "iframes:document.getElementsByTagName('iframe').length,"
                        + "links:document.getElementsByTagName('a').length,"
                        + "buttons:document.getElementsByTagName('button').length,"
                        + "forms:document.getElementsByTagName('form').length,"
                        + "inputs:document.getElementsByTagName('input').length,"
                        + "scripts:document.getElementsByTagName('script').length,"
                        + "images:document.getElementsByTagName('img').length,"

                        + "bodyText:document.body"
                        + "?document.body.innerText.slice(0,500):''"

                        + "});"

                + "})();";

        view.evaluateJavascript(
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
    // REQUEST HEADERS
    // ============================================================

    private void logHeaders(
            String prefix,
            WebResourceRequest request
    ) {

        if (request == null) {
            return;
        }

        try {

            java.util.Map<String, String> headers =
                    request.getRequestHeaders();

            if (headers == null ||
                    headers.isEmpty()) {

                log(
                        prefix
                                + " HEADERS = EMPTY"
                );

                return;
            }

            for (
                    java.util.Map.Entry<String, String> entry
                    : headers.entrySet()
            ) {

                log(
                        prefix
                                + " HEADER | "
                                + entry.getKey()
                                + " = "
                                + entry.getValue()
                );
            }

        } catch (Exception e) {

            log(
                    prefix
                            + " HEADER ERROR = "
                            + e.getMessage()
            );
        }
    }

    // ============================================================
    // SHOW
    // ============================================================

    @UsedByGodot
    public void showRewardedAd() {

        activity.runOnUiThread(() -> {

            separator();

            log(
                    "🎯 showRewardedAd() بدأ"
            );

            if (hostWebView == null) {

                log(
                        "❌ HOST WebView = null"
                );

                return;
            }

            if (clickInProgress) {

                log(
                        "⚠️ CLICK ALREADY IN PROGRESS"
                );

                return;
            }

            clickInProgress = true;

            // ========================================================
            // PRE CLICK
            // ========================================================

            inspectWebView(
                    hostWebView,
                    "PRE-CLICK INSPECTION"
            );

            // ========================================================
            // CLICK
            // ========================================================

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

                            + "console.log('dispatchEvent result='+result);"

                            + "return result;"

                            + "})();";

            hostWebView.evaluateJavascript(
                    js,
                    value -> {

                        log(
                                "👆 CLICK DISPATCH RESULT = "
                                        + value
                        );

                        // =================================================
                        // بعد الضغط مباشرة
                        // =================================================

                        handler.postDelayed(
                                () -> {

                                    log(
                                            "🔬 POST-CLICK INSPECTION START"
                                    );

                                    inspectWebView(
                                            hostWebView,
                                            "POST-CLICK DOM"
                                    );

                                    log(
                                            "🔬 POST-CLICK INSPECTION END"
                                    );

                                },
                                1000
                        );

                        // =================================================
                        // بعد 3 ثوان
                        // =================================================

                        handler.postDelayed(
                                () -> {

                                    if (hostWebView != null) {

                                        inspectWebView(
                                                hostWebView,
                                                "POST-CLICK +3 SEC"
                                        );
                                    }

                                },
                                3000
                        );

                        // =================================================
                        // بعد 7 ثوان
                        // =================================================

                        handler.postDelayed(
                                () -> {

                                    if (hostWebView != null) {

                                        inspectWebView(
                                                hostWebView,
                                                "POST-CLICK +7 SEC"
                                        );
                                    }

                                    clickInProgress = false;

                                },
                                7000
                        );
                    }
            );
        });
    }

    // ============================================================
    // REWARD
    // ============================================================

    private void scheduleReward() {

        log(
                "⏱ scheduleReward()"
        );

        handler.postDelayed(
                () -> {

                    if (!rewardGranted) {

                        rewardGranted = true;

                        log(
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

            log(
                    "🛑 closeRewardedAd()"
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
                popWebView.destroy();

            } catch (Exception e) {

                log(
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

            log(
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
                hostWebView.destroy();

            } catch (Exception e) {

                log(
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

        log(
                "💀 LamoushiRewarded onMainDestroy()"
        );

        handler.removeCallbacksAndMessages(null);

        destroyPop();
        destroyHost();
    }
}
