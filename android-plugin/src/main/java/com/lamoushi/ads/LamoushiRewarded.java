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

    private final Activity activity;

    private WebView hostWebView;
    private WebView popWebView;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean rewardGranted = false;
    private boolean rewardScheduled = false;

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
    // DEBUG
    // ============================================================

    private void debug(String message) {

        emitSignal(
                "reward_debug",
                message
        );
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
            rewardScheduled = false;

            destroyPop();
            destroyHost();

            debug(
                    "🌐 AD_SCRIPT_URL = "
                            + AD_SCRIPT_URL
            );

            debug(
                    "🌐 AD_BASE_URL = "
                            + AD_BASE_URL
            );

            // ========================================================
            // COOKIE
            // ========================================================

            CookieManager cookieManager =
                    CookieManager.getInstance();

            cookieManager.setAcceptCookie(true);

            debug("🍪 AcceptCookie = true");

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

            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.LOLLIPOP) {

                settings.setMixedContentMode(
                        WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                );

                cookieManager.setAcceptThirdPartyCookies(
                        hostWebView,
                        true
                );

                debug(
                        "🍪 Third-party cookies = true"
                );
            }

            debug(
                    "🌐 UserAgent = "
                            + settings.getUserAgentString()
            );

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

                        @Override
                        public boolean onCreateWindow(
                                WebView view,
                                boolean isDialog,
                                boolean isUserGesture,
                                android.os.Message resultMsg
                        ) {

                            separator();

                            debug(
                                    "🪟 HOST onCreateWindow()"
                            );

                            debug(
                                    "🖱 isUserGesture = "
                                            + isUserGesture
                            );

                            debug(
                                    "📦 isDialog = "
                                            + isDialog
                            );

                            createPopWebView(
                                    resultMsg,
                                    isUserGesture,
                                    isDialog
                            );

                            return true;
                        }

                        @Override
                        public void onCloseWindow(
                                WebView window
                        ) {

                            debug(
                                    "🔴 HOST onCloseWindow()"
                            );

                            if (window == popWebView) {
                                destroyPop();
                            }
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

                            debug(
                                    "✅ HOST PAGE FINISHED | "
                                            + url
                            );

                            debug(
                                    "📍 HOST URL = "
                                            + url
                            );

                            installDiagnostics();

                            inspectDocument(
                                    "INITIAL"
                            );

                            scheduleDiagnostics();
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

                            debug(
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

                            if (request == null ||
                                    response == null) {
                                return;
                            }

                            debug(
                                    "⚠️ HOST HTTP ERROR"
                                            + " | status="
                                            + response.getStatusCode()
                                            + " | mime="
                                            + response.getMimeType()
                                            + " | url="
                                            + request.getUrl()
                            );
                        }

                        @Override
                        public void onReceivedSslError(
                                WebView view,
                                SslErrorHandler handlerSsl,
                                SslError error
                        ) {

                            debug(
                                    "🔐 HOST SSL ERROR"
                                            + " | error="
                                            + error.getPrimaryError()
                                            + " | url="
                                            + error.getUrl()
                            );

                            handlerSsl.cancel();
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
                                                + request.getMethod()
                                                + " "
                                                + request.getUrl()
                                                + " | gesture="
                                                + request.hasGesture()
                                                + " | main="
                                                + request.isForMainFrame()
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

                                debug(
                                        "🌐 HOST REQUEST"
                                                + " | "
                                                + request.getMethod()
                                                + " "
                                                + request.getUrl()
                                );

                                debug(
                                        "HOST main="
                                                + request.isForMainFrame()
                                                + " gesture="
                                                + request.hasGesture()
                                );

                                logHeaders(
                                        "HOST",
                                        request
                                );
                            }

                            // مهم:
                            // لا نعترض الطلب.
                            // WebView يتولى التحميل بنفسه.

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
                            + "content='width=device-width,initial-scale=1.0'>"

                            + "<style>"
                            + "html,body{"
                            + "margin:0;"
                            + "padding:0;"
                            + "width:100%;"
                            + "height:100%;"
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
                            + "console.log('🔬 HTML loaded');"
                            + "console.log('trigger موجود = ' + "
                            + "!!document.getElementById('trigger'));"
                            + "</script>"

                            + "<script src='"
                            + AD_SCRIPT_URL
                            + "'></script>"

                            + "</body>"
                            + "</html>";

            debug(
                    "📄 HTML الإعلان تم إنشاؤه"
            );

            debug(
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
            // HOST SIZE
            // ========================================================

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(
                            1,
                            1
                    );

            activity.addContentView(
                    hostWebView,
                    params
            );

            debug(
                    "✅ HOST WebView تمت إضافته إلى Activity"
            );

            debug(
                    "HOST physical size = 1x1"
            );
        });
    }

    // ============================================================
    // INSTALL JAVASCRIPT DIAGNOSTICS
    // ============================================================

    private void installDiagnostics() {

        if (hostWebView == null) {
            return;
        }

        debug(
                "🔬 تثبيت JavaScript diagnostics"
        );

        String js =
                "(function(){"

                        // =================================================
                        // Prevent duplicate installation
                        // =================================================

                        + "if(window.__LAMOUSHI_DIAG){"
                        + "console.log('🔬 diagnostics already installed');"
                        + "return 'already';"
                        + "}"
                        + "window.__LAMOUSHI_DIAG=true;"

                        // =================================================
                        // Basic info
                        // =================================================

                        + "console.log('🔬 diagnostics installed');"
                        + "console.log('current href=' + location.href);"

                        // =================================================
                        // Window open
                        // =================================================

                        + "try{"
                        + "var oldOpen=window.open;"
                        + "window.open=function(){"
                        + "console.log('🚨 WINDOW.OPEN CALLED');"
                        + "console.log('window.open args=' + arguments.length);"
                        + "try{"
                        + "console.log('window.open url=' + arguments[0]);"
                        + "}catch(e){}"
                        + "try{"
                        + "console.log('window.open target=' + arguments[1]);"
                        + "}catch(e){}"
                        + "try{"
                        + "console.log('window.open features=' + arguments[2]);"
                        + "}catch(e){}"
                        + "var result=oldOpen.apply(this,arguments);"
                        + "console.log('🚨 WINDOW.OPEN RESULT=' + result);"
                        + "return result;"
                        + "};"
                        + "}catch(e){"
                        + "console.log('❌ window.open hook error=' + e);"
                        + "}"

                        // =================================================
                        // Errors
                        // =================================================

                        + "window.addEventListener('error',function(e){"
                        + "console.log('🚨 JS ERROR');"
                        + "console.log('message=' + e.message);"
                        + "console.log('source=' + e.filename);"
                        + "console.log('line=' + e.lineno);"
                        + "console.log('column=' + e.colno);"
                        + "},true);"

                        + "window.addEventListener("
                        + "'unhandledrejection',function(e){"
                        + "console.log('🚨 UNHANDLED PROMISE REJECTION');"
                        + "try{console.log('reason=' + e.reason);}catch(x){}"
                        + "},true);"

                        // =================================================
                        // Event diagnostics
                        // =================================================

                        + "['click','mousedown','mouseup',"
                        + "'pointerdown','pointerup','touchstart','touchend']"
                        + ".forEach(function(type){"
                        + "document.addEventListener(type,function(e){"
                        + "console.log("
                        + "'🖱 EVENT ' + type"
                        + " + ' target=' + "
                        + "(e.target && e.target.tagName)"
                        + " + ' trusted=' + e.isTrusted"
                        + " + ' defaultPrevented=' + e.defaultPrevented"
                        + " + ' x=' + e.clientX"
                        + " + ' y=' + e.clientY"
                        + ");"
                        + "},true);"
                        + "});"

                        // =================================================
                        // Visibility
                        // =================================================

                        + "document.addEventListener("
                        + "'visibilitychange',function(){"
                        + "console.log("
                        + "'👁 visibility=' + document.visibilityState"
                        + ");"
                        + "});"

                        // =================================================
                        // Focus
                        // =================================================

                        + "window.addEventListener('focus',function(){"
                        + "console.log('🎯 WINDOW FOCUS');"
                        + "});"

                        + "window.addEventListener('blur',function(){"
                        + "console.log('🎯 WINDOW BLUR');"
                        + "});"

                        // =================================================
                        // History
                        // =================================================

                        + "try{"

                        + "var oldPush=history.pushState;"
                        + "history.pushState=function(){"
                        + "console.log('🧭 history.pushState');"
                        + "try{console.log('url=' + arguments[2]);}catch(e){}"
                        + "return oldPush.apply(this,arguments);"
                        + "};"

                        + "var oldReplace=history.replaceState;"
                        + "history.replaceState=function(){"
                        + "console.log('🧭 history.replaceState');"
                        + "try{console.log('url=' + arguments[2]);}catch(e){}"
                        + "return oldReplace.apply(this,arguments);"
                        + "};"

                        + "}catch(e){"
                        + "console.log('❌ history hook error=' + e);"
                        + "}"

                        // =================================================
                        // Location assignment diagnostics
                        // =================================================

                        + "window.addEventListener('hashchange',function(){"
                        + "console.log('🧭 HASH CHANGE=' + location.href);"
                        + "});"

                        // =================================================
                        // createElement
                        // =================================================

                        + "try{"

                        + "var oldCreate=document.createElement;"

                        + "document.createElement=function(tag){"

                        + "var el=oldCreate.call(document,tag);"

                        + "console.log("
                        + "'🧩 createElement(' + "
                        + "String(tag).toUpperCase() + ')'"
                        + ");"

                        + "return el;"
                        + "};"

                        + "}catch(e){"
                        + "console.log('❌ createElement hook error=' + e);"
                        + "}"

                        // =================================================
                        // appendChild
                        // =================================================

                        + "try{"

                        + "var oldAppend=Node.prototype.appendChild;"

                        + "Node.prototype.appendChild=function(node){"

                        + "try{"

                        + "console.log("
                        + "'🧩 appendChild tag=' + "
                        + "(node && node.tagName)"
                        + ");"

                        + "if(node && node.tagName){"

                        + "var tag=node.tagName.toLowerCase();"

                        + "if(tag==='iframe' || tag==='a' || "
                        + "tag==='script' || tag==='form'){"

                        + "console.log("
                        + "'🚨 IMPORTANT ELEMENT APPENDED tag=' + tag"
                        + ");"

                        + "try{console.log('src=' + node.src);}catch(e){}"
                        + "try{console.log('href=' + node.href);}catch(e){}"
                        + "try{console.log('target=' + node.target);}catch(e){}"
                        + "try{console.log('action=' + node.action);}catch(e){}"

                        + "}"
                        + "}"

                        + "}catch(e){"
                        + "console.log('❌ append inspection=' + e);"
                        + "}"

                        + "return oldAppend.call(this,node);"
                        + "};"

                        + "}catch(e){"
                        + "console.log('❌ append hook error=' + e);"
                        + "}"

                        // =================================================
                        // Mutation observer
                        // =================================================

                        + "try{"

                        + "var observer=new MutationObserver(function(list){"

                        + "console.log("
                        + "'🔄 DOM MUTATION count=' + list.length"
                        + ");"

                        + "list.forEach(function(m){"

                        + "if(m.type==='childList'){"

                        + "console.log("
                        + "'🔄 mutation childList added='"
                        + "+m.addedNodes.length"
                        + "+' removed='"
                        + "+m.removedNodes.length"
                        + ");"

                        + "for(var i=0;i<m.addedNodes.length;i++){"

                        + "var n=m.addedNodes[i];"

                        + "if(n && n.tagName){"

                        + "console.log("
                        + "'🧩 MUTATION ADDED='"
                        + "+n.tagName"
                        + ");"

                        + "try{console.log('src=' + n.src);}catch(e){}"
                        + "try{console.log('href=' + n.href);}catch(e){}"

                        + "}"

                        + "}"

                        + "}"

                        + "});"

                        + "});"

                        + "observer.observe(document.documentElement,{"
                        + "subtree:true,"
                        + "childList:true,"
                        + "attributes:true"
                        + "});"

                        + "}catch(e){"
                        + "console.log('❌ MutationObserver error=' + e);"
                        + "}"

                        + "return 'installed';"

                        + "})();";

        hostWebView.evaluateJavascript(
                js,
                value -> debug(
                        "🔬 Diagnostics install result = "
                                + value
                )
        );
    }

    // ============================================================
    // DOCUMENT INSPECTION
    // ============================================================

    private void inspectDocument(
            String label
    ) {

        if (hostWebView == null) {
            return;
        }

        String js =
                "(function(){"
                        + "function rect(el){"
                        + "if(!el)return null;"
                        + "var r=el.getBoundingClientRect();"
                        + "return {"
                        + "x:r.x,y:r.y,"
                        + "width:r.width,height:r.height,"
                        + "top:r.top,right:r.right,"
                        + "bottom:r.bottom,left:r.left"
                        + "};"
                        + "}"

                        + "var t=document.getElementById('trigger');"

                        + "return JSON.stringify({"

                        + "href:location.href,"
                        + "origin:location.origin,"
                        + "readyState:document.readyState,"
                        + "title:document.title,"
                        + "visibility:document.visibilityState,"
                        + "hasFocus:document.hasFocus(),"

                        + "bodyLength:"
                        + "(document.body ? document.body.innerHTML.length : -1),"

                        + "elementCount:"
                        + "document.getElementsByTagName('*').length,"

                        + "triggerExists:!!t,"
                        + "triggerTag:t?t.tagName:null,"
                        + "triggerId:t?t.id:null,"
                        + "triggerClass:t?t.className:null,"
                        + "triggerRect:rect(t),"

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
                        + "document.body ? document.body.innerText : ''"

                        + "});"

                        + "})();";

        hostWebView.evaluateJavascript(
                js,
                value -> {

                    debug(
                            "🔬 "
                                    + label
                                    + " DOCUMENT INSPECTION = "
                                    + value
                    );
                }
        );
    }

    // ============================================================
    // DIAGNOSTIC TIMERS
    // ============================================================

    private void scheduleDiagnostics() {

        handler.postDelayed(
                () -> inspectDocument(
                        "POST-LOAD +1 SEC"
                ),
                1000
        );

        handler.postDelayed(
                () -> inspectDocument(
                        "POST-LOAD +3 SEC"
                ),
                3000
        );

        handler.postDelayed(
                () -> inspectDocument(
                        "POST-LOAD +7 SEC"
                ),
                7000
        );

        handler.postDelayed(
                () -> inspectDocument(
                        "POST-LOAD +12 SEC"
                ),
                12000
        );
    }

    // ============================================================
    // SHOW
    // ============================================================

    @UsedByGodot
    public void showRewardedAd() {

        activity.runOnUiThread(() -> {

            separator();

            debug(
                    "🎯 showRewardedAd() بدأ"
            );

            if (hostWebView == null) {

                debug(
                        "❌ HOST WebView = null"
                );

                return;
            }

            // --------------------------------------------------------
            // PRE CLICK
            // --------------------------------------------------------

            inspectDocument(
                    "PRE-CLICK"
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
                            + "'trigger موجود — dispatching click'"
                            + ");"

                            // ------------------------------------------
                            // mouse down
                            // ------------------------------------------

                            + "try{"

                            + "el.dispatchEvent("
                            + "new MouseEvent('mousedown',{"
                            + "bubbles:true,"
                            + "cancelable:true,"
                            + "view:window,"
                            + "clientX:1,"
                            + "clientY:1"
                            + "})"
                            + ");"

                            + "}catch(e){"
                            + "console.log('mousedown error=' + e);"
                            + "}"

                            // ------------------------------------------
                            // mouse up
                            // ------------------------------------------

                            + "try{"

                            + "el.dispatchEvent("
                            + "new MouseEvent('mouseup',{"
                            + "bubbles:true,"
                            + "cancelable:true,"
                            + "view:window,"
                            + "clientX:1,"
                            + "clientY:1"
                            + "})"
                            + ");"

                            + "}catch(e){"
                            + "console.log('mouseup error=' + e);"
                            + "}"

                            // ------------------------------------------
                            // click
                            // ------------------------------------------

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

                        separator();

                        debug(
                                "🔬 POST-CLICK INSPECTION START"
                        );

                        inspectDocument(
                                "POST-CLICK"
                        );

                        handler.postDelayed(
                                () -> inspectDocument(
                                        "POST-CLICK +3 SEC"
                                ),
                                3000
                        );

                        handler.postDelayed(
                                () -> inspectDocument(
                                        "POST-CLICK +7 SEC"
                                ),
                                7000
                        );

                        handler.postDelayed(
                                () -> inspectDocument(
                                        "POST-CLICK +12 SEC"
                                ),
                                12000
                        );

                        debug(
                                "🔬 POST-CLICK INSPECTION SCHEDULED"
                        );
                    }
            );
        });
    }

    // ============================================================
    // CREATE POP WEBVIEW
    // ============================================================

    private void createPopWebView(
            android.os.Message resultMsg,
            boolean isUserGesture,
            boolean isDialog
    ) {

        destroyPop();

        popWebView =
                new WebView(activity);

        WebSettings settings =
                popWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(
                true
        );

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.LOLLIPOP) {

            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );

            CookieManager.getInstance()
                    .setAcceptThirdPartyCookies(
                            popWebView,
                            true
                    );
        }

        debug(
                "🪟 Creating POP WebView"
        );

        debug(
                "POP isUserGesture = "
                        + isUserGesture
        );

        debug(
                "POP isDialog = "
                        + isDialog
        );

        // ========================================================
        // POP CHROME
        // ========================================================

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
                            boolean dialog,
                            boolean gesture,
                            android.os.Message msg
                    ) {

                        debug(
                                "🪟 POP onCreateWindow()"
                        );

                        debug(
                                "POP gesture="
                                        + gesture
                        );

                        debug(
                                "POP dialog="
                                        + dialog
                        );

                        return super.onCreateWindow(
                                view,
                                dialog,
                                gesture,
                                msg
                        );
                    }

                    @Override
                    public void onCloseWindow(
                            WebView window
                    ) {

                        debug(
                                "🔴 POP onCloseWindow()"
                        );

                        if (window == popWebView) {
                            destroyPop();
                        }
                    }
                }
        );

        // ========================================================
        // POP CLIENT
        // ========================================================

        popWebView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageStarted(
                            WebView view,
                            String url,
                            Bitmap favicon
                    ) {

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
                    }

                    @Override
                    public void onReceivedError(
                            WebView view,
                            WebResourceRequest request,
                            WebResourceError error
                    ) {

                        if (request == null) {
                            return;
                        }

                        if (!request.isForMainFrame()) {
                            return;
                        }

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

                    @Override
                    public void onReceivedHttpError(
                            WebView view,
                            WebResourceRequest request,
                            WebResourceResponse response
                    ) {

                        if (request == null ||
                                response == null) {
                            return;
                        }

                        debug(
                                "⚠️ POP HTTP ERROR"
                                        + " | status="
                                        + response.getStatusCode()
                                        + " | mime="
                                        + response.getMimeType()
                                        + " | url="
                                        + request.getUrl()
                        );
                    }

                    @Override
                    public void onReceivedSslError(
                            WebView view,
                            SslErrorHandler handlerSsl,
                            SslError error
                    ) {

                        debug(
                                "🔐 POP SSL ERROR"
                                        + " | error="
                                        + error.getPrimaryError()
                                        + " | url="
                                        + error.getUrl()
                        );

                        handlerSsl.cancel();
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
                                            + request.getMethod()
                                            + " "
                                            + request.getUrl()
                                            + " | gesture="
                                            + request.hasGesture()
                                            + " | main="
                                            + request.isForMainFrame()
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

        // ========================================================
        // ADD POP
        // ========================================================

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        1,
                        1
                );

        activity.addContentView(
                popWebView,
                params
        );

        // ========================================================
        // TRANSPORT
        // ========================================================

        if (resultMsg == null ||
                resultMsg.obj == null) {

            debug(
                    "❌ window.open resultMsg/obj = null"
            );

            return;
        }

        WebView.WebViewTransport transport =
                (WebView.WebViewTransport)
                        resultMsg.obj;

        transport.setWebView(
                popWebView
        );

        resultMsg.sendToTarget();

        debug(
                "✅ window.open() WebViewTransport delivered"
        );

        emitSignal(
                "reward_ad_opened"
        );

        scheduleReward();
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

                debug(
                        prefix
                                + " HEADER = <none>"
                );

                return;
            }

            for (
                    java.util.Map.Entry<String, String> entry
                    : headers.entrySet()
            ) {

                debug(
                        prefix
                                + " HEADER | "
                                + entry.getKey()
                                + " = "
                                + entry.getValue()
                );
            }

        } catch (Exception e) {

            debug(
                    prefix
                            + " HEADER ERROR = "
                            + e.getClass().getSimpleName()
                            + " | "
                            + e.getMessage()
            );
        }
    }

    // ============================================================
    // REWARD
    // ============================================================

    private void scheduleReward() {

        if (rewardScheduled) {

            debug(
                    "⚠️ reward timer already scheduled"
            );

            return;
        }

        rewardScheduled = true;

        debug(
                "⏱ Reward timer scheduled: "
                        + REWARD_DELAY_MS
                        + " ms"
        );

        handler.postDelayed(
                () -> {

                    if (rewardGranted) {
                        return;
                    }

                    rewardGranted = true;

                    debug(
                            "🎁 Reward timer finished"
                    );

                    emitSignal(
                            "reward_ready"
                    );

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
                    "🔴 closeRewardedAd()"
            );

            destroyPop();
            destroyHost();

            rewardScheduled = false;

            emitSignal(
                    "reward_ad_closed"
            );
        });
    }

    // ============================================================
    // DESTROY POP
    // ============================================================

    private void destroyPop() {

        if (popWebView == null) {
            return;
        }

        try {

            if (popWebView.getParent() != null) {

                ViewGroup parent =
                        (ViewGroup)
                                popWebView.getParent();

                parent.removeView(
                        popWebView
                );
            }

            popWebView.stopLoading();
            popWebView.loadUrl("about:blank");
            popWebView.clearHistory();
            popWebView.removeAllViews();
            popWebView.destroy();

        } catch (Exception e) {

            debug(
                    "⚠️ destroyPop error = "
                            + e.getClass().getSimpleName()
                            + " | "
                            + e.getMessage()
            );
        }

        popWebView = null;
    }

    // ============================================================
    // DESTROY HOST
    // ============================================================

    private void destroyHost() {

        if (hostWebView == null) {
            return;
        }

        try {

            if (hostWebView.getParent() != null) {

                ViewGroup parent =
                        (ViewGroup)
                                hostWebView.getParent();

                parent.removeView(
                        hostWebView
                );
            }

            hostWebView.stopLoading();
            hostWebView.loadUrl("about:blank");
            hostWebView.clearHistory();
            hostWebView.removeAllViews();
            hostWebView.destroy();

        } catch (Exception e) {

            debug(
                    "⚠️ destroyHost error = "
                            + e.getClass().getSimpleName()
                            + " | "
                            + e.getMessage()
            );
        }

        hostWebView = null;
    }

    // ============================================================
    // GODOT DESTROY
    // ============================================================

    @Override
    public void onMainDestroy() {

        handler.removeCallbacksAndMessages(
                null
        );

        if (activity != null) {

            activity.runOnUiThread(() -> {

                destroyPop();
                destroyHost();

            });
        }
    }
}
