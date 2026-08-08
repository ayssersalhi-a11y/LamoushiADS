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
import android.webkit.WebResourceResponse;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.FrameLayout;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

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

    // ============================================================
    // إعدادات التشخيص
    // ============================================================

    private static final boolean DEEP_INSPECT = true;

    // أقصى حجم نقرأه من أي استجابة للتشخيص
    private static final int MAX_INSPECT_BYTES = 500000;

    // أقصى عدد أحرف نطبعه من النص
    private static final int MAX_PREVIEW_CHARS = 1200;

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
    // أداة إخراج موحدة
    // ============================================================

    private void debug(String message) {

        emitSignal(
            "reward_debug",
            message
        );
    }

    // ============================================================
    // طباعة رؤوس HTTP المهمة
    // بدون طباعة Cookie
    // ============================================================

    private void logResponseHeaders(
        String prefix,
        HttpURLConnection conn
    ) {

        debug(
            prefix
            + " HEADER | Server="
            + conn.getHeaderField("Server")
        );

        debug(
            prefix
            + " HEADER | Content-Type="
            + conn.getHeaderField("Content-Type")
        );

        debug(
            prefix
            + " HEADER | Content-Length="
            + conn.getHeaderField("Content-Length")
        );

        debug(
            prefix
            + " HEADER | Content-Encoding="
            + conn.getHeaderField("Content-Encoding")
        );

        debug(
            prefix
            + " HEADER | Location="
            + conn.getHeaderField("Location")
        );

        debug(
            prefix
            + " HEADER | Access-Control-Allow-Origin="
            + conn.getHeaderField(
                "Access-Control-Allow-Origin"
            )
        );

        debug(
            prefix
            + " HEADER | Access-Control-Allow-Credentials="
            + conn.getHeaderField(
                "Access-Control-Allow-Credentials"
            )
        );

        debug(
            prefix
            + " HEADER | Cross-Origin-Resource-Policy="
            + conn.getHeaderField(
                "Cross-Origin-Resource-Policy"
            )
        );

        debug(
            prefix
            + " HEADER | X-Content-Type-Options="
            + conn.getHeaderField(
                "X-Content-Type-Options"
            )
        );

        debug(
            prefix
            + " HEADER | Cache-Control="
            + conn.getHeaderField(
                "Cache-Control"
            )
        );

        debug(
            prefix
            + " HEADER | Vary="
            + conn.getHeaderField(
                "Vary"
            )
        );
    }

    // ============================================================
    // قراءة محتوى الاستجابة
    // ============================================================

    private byte[] readResponse(
        InputStream is
    ) throws Exception {

        if (is == null) {
            return new byte[0];
        }

        ByteArrayOutputStream buffer =
            new ByteArrayOutputStream();

        byte[] data =
            new byte[8192];

        int nRead;
        int totalRead = 0;

        while (
            (nRead =
                is.read(
                    data,
                    0,
                    data.length
                )) != -1
            &&
            totalRead < MAX_INSPECT_BYTES
        ) {

            buffer.write(
                data,
                0,
                nRead
            );

            totalRead += nRead;
        }

        is.close();

        return buffer.toByteArray();
    }

    // ============================================================
    // تحديد هل المحتوى نصي
    // ============================================================

    private boolean isTextContent(
        String contentType
    ) {

        if (contentType == null) {
            return false;
        }

        String ct =
            contentType.toLowerCase();

        return
            ct.contains("text")
            ||
            ct.contains("javascript")
            ||
            ct.contains("json")
            ||
            ct.contains("xml")
            ||
            ct.contains("html")
            ||
            ct.contains("svg");
    }

    // ============================================================
    // معاينة المحتوى
    // ============================================================

    private void inspectBody(
        String prefix,
        byte[] bodyBytes,
        String contentType
    ) {

        debug(
            prefix
            + " BODY SIZE = "
            + bodyBytes.length
            + " bytes"
        );

        if (bodyBytes.length == 0) {

            debug(
                prefix
                + " BODY = EMPTY"
            );

            return;
        }

        if (!isTextContent(contentType)) {

            debug(
                prefix
                + " BODY = binary/non-text"
            );

            return;
        }

        try {

            String text =
                new String(
                    bodyBytes,
                    StandardCharsets.UTF_8
                );

            text =
                text.replace(
                    "\r",
                    " "
                ).replace(
                    "\n",
                    " "
                ).replace(
                    "\t",
                    " "
                );

            if (
                text.length()
                >
                MAX_PREVIEW_CHARS
            ) {

                text =
                    text.substring(
                        0,
                        MAX_PREVIEW_CHARS
                    )
                    + " ...[TRUNCATED]";
            }

            debug(
                prefix
                + " BODY PREVIEW = "
                + text
            );

        } catch (Exception e) {

            debug(
                prefix
                + " BODY DECODE ERROR = "
                + e.getClass().getSimpleName()
                + " : "
                + e.getMessage()
            );
        }
    }

    // ============================================================
    // فحص HTTP عميق
    // ============================================================

    private WebResourceResponse inspectRequest(
        WebView view,
        WebResourceRequest request,
        String prefix
    ) {

        String url =
            request.getUrl().toString();

        String method =
            request.getMethod();

        debug(
            prefix
            + " =================================================="
        );

        debug(
            prefix
            + " REQUEST → "
            + method
            + " "
            + url
        );

        debug(
            prefix
            + " isForMainFrame="
            + request.isForMainFrame()
        );

        debug(
            prefix
            + " hasGesture="
            + request.hasGesture()
        );

        Map<String, String> originalHeaders =
            request.getRequestHeaders();

        if (originalHeaders != null) {

            for (
                Map.Entry<String, String> entry :
                originalHeaders.entrySet()
            ) {

                String name =
                    entry.getKey();

                // لا نطبع قيم Cookies
                if (
                    name != null
                    &&
                    (
                        name.equalsIgnoreCase("Cookie")
                        ||
                        name.equalsIgnoreCase("Authorization")
                    )
                ) {

                    debug(
                        prefix
                        + " REQUEST HEADER "
                        + name
                        + " = [HIDDEN]"
                    );

                } else {

                    debug(
                        prefix
                        + " REQUEST HEADER "
                        + name
                        + " = "
                        + entry.getValue()
                    );
                }
            }
        }

        // ========================================================
        // روابط غير HTTP
        // ========================================================

        if (
            !url.startsWith("http://")
            &&
            !url.startsWith("https://")
        ) {

            debug(
                prefix
                + " NOT HTTP(S), passing to WebView"
            );

            return null;
        }

        // ========================================================
        // نحن لا نعيد تنفيذ POST/PUT/PATCH/DELETE
        // حتى لا نكرر عمليات قد تكون حساسة
        // ========================================================

        if (
            method == null
            ||
            !method.equalsIgnoreCase("GET")
        ) {

            debug(
                prefix
                + " METHOD "
                + method
                + " → not manually replayed"
            );

            return null;
        }

        try {

            URL target =
                new URL(url);

            HttpURLConnection conn =
                (HttpURLConnection)
                target.openConnection();

            conn.setRequestMethod("GET");

            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // مهم جداً للتشخيص
            // حتى لا تصلنا استجابة gzip يصعب معاينتها
            conn.setRequestProperty(
                "Accept-Encoding",
                "identity"
            );

            // ====================================================
            // نسخ رؤوس الطلب
            // ====================================================

            if (originalHeaders != null) {

                for (
                    Map.Entry<String, String> entry :
                    originalHeaders.entrySet()
                ) {

                    String name =
                        entry.getKey();

                    String value =
                        entry.getValue();

                    if (
                        name == null
                        ||
                        value == null
                    ) {
                        continue;
                    }

                    // هذه الرؤوس لا نريد إجبار HttpURLConnection عليها
                    if (
                        name.equalsIgnoreCase("Host")
                        ||
                        name.equalsIgnoreCase("Connection")
                        ||
                        name.equalsIgnoreCase("Content-Length")
                        ||
                        name.equalsIgnoreCase("Accept-Encoding")
                    ) {
                        continue;
                    }

                    try {

                        conn.setRequestProperty(
                            name,
                            value
                        );

                    } catch (Exception ignored) {
                    }
                }
            }

            // ====================================================
            // بدء الطلب
            // ====================================================

            long start =
                System.currentTimeMillis();

            int status =
                conn.getResponseCode();

            long elapsed =
                System.currentTimeMillis()
                - start;

            debug(
                prefix
                + " RESPONSE STATUS = "
                + status
                + " | time="
                + elapsed
                + "ms"
            );

            debug(
                prefix
                + " FINAL URL = "
                + conn.getURL()
            );

            debug(
                prefix
                + " RESPONSE MESSAGE = "
                + conn.getResponseMessage()
            );

            // ====================================================
            // الرؤوس
            // ====================================================

            logResponseHeaders(
                prefix,
                conn
            );

            // ====================================================
            // اختيار stream
            // ====================================================

            InputStream is;

            if (status >= 400) {

                is =
                    conn.getErrorStream();

            } else {

                is =
                    conn.getInputStream();
            }

            byte[] bodyBytes =
                readResponse(is);

            String contentType =
                conn.getContentType();

            // ====================================================
            // فحص النص
            // ====================================================

            inspectBody(
                prefix,
                bodyBytes,
                contentType
            );

            // ====================================================
            // أدلة تلقائية مفيدة
            // ====================================================

            if (status >= 200 && status < 300) {

                debug(
                    prefix
                    + " ✅ HTTP SUCCESS"
                );

            } else if (
                status >= 300
                &&
                status < 400
            ) {

                debug(
                    prefix
                    + " 🟡 REDIRECT"
                );

            } else if (
                status >= 400
                &&
                status < 500
            ) {

                debug(
                    prefix
                    + " 🔴 CLIENT ERROR"
                );

            } else if (status >= 500) {

                debug(
                    prefix
                    + " 🔴 SERVER ERROR"
                );
            }

            if (
                contentType == null
                ||
                contentType.trim().isEmpty()
            ) {

                debug(
                    prefix
                    + " ⚠️ NO CONTENT-TYPE"
                );
            }

            if (bodyBytes.length == 0) {

                debug(
                    prefix
                    + " ⚠️ EMPTY RESPONSE"
                );
            }

            String location =
                conn.getHeaderField(
                    "Location"
                );

            if (
                location != null
                &&
                !location.isEmpty()
            ) {

                debug(
                    prefix
                    + " 🔀 REDIRECT LOCATION = "
                    + location
                );
            }

            // ====================================================
            // إعادة بناء الاستجابة
            // ====================================================

            Map<String, String> responseHeaders =
                new HashMap<>();

            responseHeaders.put(
                "Access-Control-Allow-Origin",
                "*"
            );

            String mimeType =
                contentType != null
                ?
                contentType
                    .split(";")[0]
                    .trim()
                :
                "application/octet-stream";

            String encoding =
                "UTF-8";

            String reason =
                conn.getResponseMessage();

            if (
                reason == null
                ||
                reason.isEmpty()
            ) {

                reason =
                    (
                        status >= 200
                        &&
                        status < 300
                    )
                    ?
                    "OK"
                    :
                    "Error";
            }

            debug(
                prefix
                + " RETURNING RESPONSE TO WEBVIEW"
                + " | mime="
                + mimeType
                + " | encoding="
                + encoding
            );

            debug(
                prefix
                + " =================================================="
            );

            return new WebResourceResponse(
                mimeType,
                encoding,
                status,
                reason,
                responseHeaders,
                new java.io.ByteArrayInputStream(
                    bodyBytes
                )
            );

        } catch (Exception e) {

            debug(
                prefix
                + " ❌ MANUAL HTTP INSPECTION FAILED"
            );

            debug(
                prefix
                + " EXCEPTION = "
                + e.getClass().getName()
            );

            debug(
                prefix
                + " MESSAGE = "
                + e.getMessage()
            );

            return null;
        }
    }

    // ============================================================
    // تحميل الإعلان مسبقاً
    // ============================================================

    @UsedByGodot
    public void loadRewardedAd() {

        activity.runOnUiThread(() -> {

            rewardGranted = false;

            destroyHost();

            debug(
                "🚀 بدء تحميل Rewarded Ad"
            );

            debug(
                "🌐 AD_SCRIPT_URL = "
                + AD_SCRIPT_URL
            );

            debug(
                "🌐 AD_BASE_URL = "
                + AD_BASE_URL
            );

            debug(
                "🔬 DEEP_INSPECT = "
                + DEEP_INSPECT
            );

            CookieManager cm =
                CookieManager.getInstance();

            cm.setAcceptCookie(true);

            debug(
                "🍪 AcceptCookie = true"
            );

            hostWebView =
                new WebView(activity);

            WebSettings s =
                hostWebView.getSettings();

            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);

            s.setSupportMultipleWindows(true);

            s.setJavaScriptCanOpenWindowsAutomatically(
                true
            );

            s.setMixedContentMode(
                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );

            if (
                Build.VERSION.SDK_INT
                >=
                Build.VERSION_CODES.LOLLIPOP
            ) {

                cm.setAcceptThirdPartyCookies(
                    hostWebView,
                    true
                );

                debug(
                    "🍪 Third-party cookies = true"
                );
            }

            // ====================================================
            // HOST Chrome
            // ====================================================

            hostWebView.setWebChromeClient(
                new WebChromeClient() {

                    @Override
                    public boolean onConsoleMessage(
                        ConsoleMessage cm2
                    ) {

                        debug(
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

                    @Override
                    public boolean onCreateWindow(
                        WebView view,
                        boolean isDialog,
                        boolean isUserGesture,
                        android.os.Message resultMsg
                    ) {

                        debug(
                            "🪟 HOST onCreateWindow()"
                        );

                        debug(
                            "🖱 HOST isUserGesture = "
                            + isUserGesture
                        );

                        debug(
                            "📦 HOST isDialog = "
                            + isDialog
                        );

                        debug(
                            "📍 HOST current URL = "
                            + view.getUrl()
                        );

                        popWebView =
                            new WebView(activity);

                        WebSettings ps =
                            popWebView.getSettings();

                        ps.setJavaScriptEnabled(true);
                        ps.setDomStorageEnabled(true);

                        ps.setSupportMultipleWindows(
                            true
                        );

                        ps.setJavaScriptCanOpenWindowsAutomatically(
                            true
                        );

                        ps.setMixedContentMode(
                            WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        );

                        if (
                            Build.VERSION.SDK_INT
                            >=
                            Build.VERSION_CODES.LOLLIPOP
                        ) {

                            CookieManager.getInstance()
                                .setAcceptThirdPartyCookies(
                                    popWebView,
                                    true
                                );
                        }

                        // ====================================================
                        // POP Chrome
                        // ====================================================

                        popWebView.setWebChromeClient(
                            new WebChromeClient() {

                                @Override
                                public boolean onConsoleMessage(
                                    ConsoleMessage consoleMessage
                                ) {

                                    debug(
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

                                    debug(
                                        "🪟 POP onCreateWindow()"
                                    );

                                    debug(
                                        "🖱 POP isUserGesture="
                                        + isUserGesture
                                    );

                                    return false;
                                }
                            }
                        );

                        // ====================================================
                        // POP WebViewClient
                        // ====================================================

                        popWebView.setWebViewClient(
                            new WebViewClient() {

                                @Override
                                public void onPageStarted(
                                    WebView v,
                                    String url,
                                    android.graphics.Bitmap favicon
                                ) {

                                    debug(
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

                                    debug(
                                        "🟢 POP PAGE FINISHED"
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

                                    String url =
                                        request != null
                                        ?
                                        request
                                            .getUrl()
                                            .toString()
                                        :
                                        "unknown";

                                    debug(
                                        "❌ POP WEBVIEW ERROR"
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

                                    if (
                                        request != null
                                        &&
                                        errorResponse != null
                                    ) {

                                        debug(
                                            "⚠️ POP HTTP ERROR"
                                            + " | status="
                                            + errorResponse
                                                .getStatusCode()
                                            + " | mime="
                                            + errorResponse
                                                .getMimeType()
                                            + " | url="
                                            + request
                                                .getUrl()
                                        );
                                    }
                                }

                                @Override
                                public void onReceivedSslError(
                                    WebView view,
                                    SslErrorHandler sslHandler,
                                    SslError error
                                ) {

                                    debug(
                                        "🔐 POP SSL ERROR"
                                        + " | "
                                        + error
                                    );

                                    sslHandler.cancel();
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
                                            + request
                                                .getUrl()
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

                                    if (
                                        request != null
                                    ) {

                                        return inspectRequest(
                                            view,
                                            request,
                                            "POP"
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

                        // ====================================================
                        // WebView مخفي 1x1
                        // ====================================================

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

                        debug(
                            "✅ POP WebView تم ربطه بـ window.open()"
                        );

                        emitSignal(
                            "reward_ad_opened"
                        );

                        scheduleReward();

                        return true;
                    }
                }
            );

            // ====================================================
            // HOST WebViewClient
            // ====================================================

            hostWebView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageStarted(
                        WebView view,
                        String url,
                        android.graphics.Bitmap favicon
                    ) {

                        debug(
                            "🔵 HOST PAGE START"
                            + " | "
                            + url
                        );
                    }

                    @Override
                    public void onPageFinished(
                        WebView view,
                        String url
                    ) {

                        debug(
                            "✅ HOST PAGE FINISHED"
                            + " | "
                            + url
                        );

                        debug(
                            "📍 HOST URL = "
                            + url
                        );

                        view.evaluateJavascript(
                            "(function(){"
                            + "return {"
                            + "title:document.title,"
                            + "href:location.href,"
                            + "bodyLength:"
                            + "(document.body?"
                            + "document.body.innerHTML.length:0),"
                            + "readyState:"
                            + "document.readyState"
                            + "};"
                            + "})();",

                            value -> {

                                debug(
                                    "📄 HOST DOCUMENT INFO = "
                                    + value
                                );
                            }
                        );
                    }

                    @Override
                    public void onReceivedError(
                        WebView view,
                        WebResourceRequest request,
                        WebResourceError error
                    ) {

                        String url =
                            request != null
                            ?
                            request
                                .getUrl()
                                .toString()
                            :
                            "unknown";

                        debug(
                            "❌ HOST WEBVIEW ERROR"
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

                        if (
                            request != null
                            &&
                            errorResponse != null
                        ) {

                            debug(
                                "⚠️ HOST HTTP ERROR"
                                + " | status="
                                + errorResponse
                                    .getStatusCode()
                                + " | mime="
                                + errorResponse
                                    .getMimeType()
                                + " | url="
                                + request
                                    .getUrl()
                            );
                        }
                    }

                    @Override
                    public void onReceivedSslError(
                        WebView view,
                        SslErrorHandler sslHandler,
                        SslError error
                    ) {

                        debug(
                            "🔐 HOST SSL ERROR"
                            + " | primaryError="
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

                            debug(
                                "➡️ HOST NAVIGATION"
                                + " | "
                                + request
                                    .getUrl()
                            );
                        }

                        return false;
                    }

                    // ====================================================
                    // أهم جزء في التشخيص
                    // ====================================================

                    @Override
                    public WebResourceResponse
                    shouldInterceptRequest(
                        WebView view,
                        WebResourceRequest request
                    ) {

                        if (request != null) {

                            return inspectRequest(
                                view,
                                request,
                                "HOST"
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

            // ====================================================
            // HTML الإعلان
            // ====================================================

            String html =
                "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
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
                + "</head>"
                + "<body>"
                + "<div id='trigger'></div>"
                + "<script src='"
                + AD_SCRIPT_URL
                + "'></script>"
                + "</body>"
                + "</html>";

            debug(
                "📄 HTML الإعلان تم إنشاؤه"
            );

            debug(
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

            FrameLayout.LayoutParams hp =
                new FrameLayout.LayoutParams(
                    1,
                    1
                );

            activity.addContentView(
                hostWebView,
                hp
            );

            debug(
                "✅ HOST WebView تمت إضافته إلى Activity"
            );
        });
    }

    // ============================================================
    // تشغيل الإعلان
    // ============================================================

    @UsedByGodot
    public void showRewardedAd() {

        activity.runOnUiThread(() -> {

            if (hostWebView == null) {

                debug(
                    "⚠ لم يُحمَّل الإعلان بعد — "
                    + "نادِ load_rewarded_ad أولاً"
                );

                return;
            }

            debug(
                "🎯 showRewardedAd() بدأ"
            );

            hostWebView.evaluateJavascript(
                "(function(){"
              + "var el=document.getElementById('trigger');"

              + "if(!el){"
              + "console.error('trigger element غير موجود');"
              + "return false;"
              + "}"

              + "console.log("
              + "'trigger موجود — dispatching click'"
              + ");"

              + "var ev=new MouseEvent("
              + "'click',"
              + "{"
              + "bubbles:true,"
              + "cancelable:true,"
              + "view:window"
              + "}"
              + ");"

              + "var result=el.dispatchEvent(ev);"

              + "console.log("
              + "'dispatchEvent result='"
              + "+result"
              + ");"

              + "return result;"
              + "})();",

                value -> {

                    debug(
                        "👆 تم تمرير ضغطة المستخدم"
                        + " | JS result="
                        + value
                    );
                }
            );
        });
    }

    // ============================================================
    // المكافأة
    // ============================================================

    private void scheduleReward() {

        debug(
            "⏳ بدأ عداد المكافأة: "
            + REWARD_DELAY_MS
            + " ms"
        );

        handler.postDelayed(
            () -> {

                if (!rewardGranted) {

                    rewardGranted = true;

                    debug(
                        "🎁 انتهت مدة الانتظار — "
                        + "المكافأة جاهزة للمنح"
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
    // إغلاق الإعلان
    // ============================================================

    @UsedByGodot
    public void closeRewardedAd() {

        activity.runOnUiThread(() -> {

            debug(
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
    // تدمير POP
    // ============================================================

    private void destroyPop() {

        if (popWebView != null) {

            debug(
                "🗑 تدمير POP WebView"
            );

            if (
                popWebView.getParent()
                != null
            ) {

                (
                    (ViewGroup)
                    popWebView.getParent()
                ).removeView(
                    popWebView
                );
            }

            popWebView.destroy();

            popWebView = null;
        }
    }

    // ============================================================
    // تدمير HOST
    // ============================================================

    private void destroyHost() {

        if (hostWebView != null) {

            debug(
                "🗑 تدمير HOST WebView"
            );

            if (
                hostWebView.getParent()
                != null
            ) {

                (
                    (ViewGroup)
                    hostWebView.getParent()
                ).removeView(
                    hostWebView
                );
            }

            hostWebView.destroy();

            hostWebView = null;
        }
    }

    // ============================================================
    // نهاية Plugin
    // ============================================================

    @Override
    public void onMainDestroy() {

        debug(
            "💀 onMainDestroy()"
        );

        destroyPop();
        destroyHost();
    }
}
