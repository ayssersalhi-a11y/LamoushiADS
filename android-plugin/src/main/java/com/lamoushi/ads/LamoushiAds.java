package com.lamoushi.ads;

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
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class LamoushiAds extends GodotPlugin {
    private Activity activity;
    private WebView webView;
    private String currentZoneId;

    // فعّل/عطّل الفحص العميق لكل طلب (يبطئ التحميل قليلاً لكنه يعطيك التفاصيل الكاملة)
    private static final boolean DEEP_INSPECT = true;

    public LamoushiAds(Godot godot) {
        super(godot);
        this.activity = godot.getActivity();
    }

    @Override
    public String getPluginName() {
        return "LamoushiAds";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();
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

            // تفعيل remote debugging احتياطاً (لن يفيد بدون حاسوب لكن لا يضر تركه)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            currentZoneId = zoneId;
            webView = new WebView(activity);

            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);

            String userAgent = settings.getUserAgentString();
            settings.setUserAgentString(userAgent.replace("; wv", ""));

            webView.setBackgroundColor(Color.TRANSPARENT);

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(webView, true);
            }
            emitSignal("ad_debug", "تم تهيئة WebView و الكوكيز بنجاح ✅");

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage cm) {
                    emitSignal("ad_debug", "JS[" + cm.messageLevel() + "]: " + cm.message()
                            + " (line " + cm.lineNumber() + ")");
                    return true;
                }
            });

            webView.setWebViewClient(new WebViewClient() {

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    String method = request.getMethod();

                    if (!DEEP_INSPECT) {
                        emitSignal("ad_debug", "REQUEST: " + method + " " + url);
                        return super.shouldInterceptRequest(view, request);
                    }

                    // فحص عميق: فقط لروابط http/https
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        return super.shouldInterceptRequest(view, request);
                    }

                    try {
                        emitSignal("ad_debug", "→ REQUEST: " + method + " " + url);

                        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                        conn.setRequestMethod(method != null ? method : "GET");
                        conn.setConnectTimeout(8000);
                        conn.setReadTimeout(8000);
                        conn.setInstanceFollowRedirects(true);

                        // نسخ الرؤوس الأصلية من الطلب (مهم: Referer, Origin تؤثر على استجابة السيرفر)
                        Map<String, String> reqHeaders = request.getRequestHeaders();
                        for (Map.Entry<String, String> h : reqHeaders.entrySet()) {
                            try { conn.setRequestProperty(h.getKey(), h.getValue()); } catch (Exception ignore) {}
                        }

                        int status = conn.getResponseCode();
                        String contentType = conn.getContentType();
                        String server = conn.getHeaderField("Server");
                        String acao = conn.getHeaderField("Access-Control-Allow-Origin");
                        String corp = conn.getHeaderField("Cross-Origin-Resource-Policy");
                        String xcto = conn.getHeaderField("X-Content-Type-Options");

                        emitSignal("ad_debug", "← STATUS: " + status
                                + " | Content-Type: " + contentType
                                + " | Server: " + server);
                        emitSignal("ad_debug", "  CORS Headers → ACAO: " + acao
                                + " | CORP: " + corp
                                + " | X-Content-Type-Options: " + xcto);

                        InputStream is = (status >= 400)
                                ? conn.getErrorStream()
                                : conn.getInputStream();

                        if (is == null) {
                            emitSignal("ad_debug", "  ⚠ لا يوجد محتوى في الاستجابة (stream فارغ)");
                            return super.shouldInterceptRequest(view, request);
                        }

                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] data = new byte[4096];
                        int nRead;
                        int totalRead = 0;
                        while ((nRead = is.read(data, 0, data.length)) != -1 && totalRead < 500000) {
                            buffer.write(data, 0, nRead);
                            totalRead += nRead;
                        }
                        byte[] bodyBytes = buffer.toByteArray();
                        is.close();

                        emitSignal("ad_debug", "  حجم المحتوى الفعلي: " + bodyBytes.length + " bytes");

                        // إن كان المحتوى نصياً وصغيراً، اطبع أول جزء منه للتشخيص
                        if (contentType != null && (contentType.contains("text") || contentType.contains("json") || contentType.contains("javascript"))) {
                            String preview = new String(bodyBytes, 0, Math.min(150, bodyBytes.length));
                            emitSignal("ad_debug", "  معاينة المحتوى: " + preview.replace("\n", " "));
                        }

                        // تشخيص السبب المحتمل لحظر ORB
                        if (contentType == null || contentType.isEmpty()) {
                            emitSignal("ad_debug", "  🔴 سبب محتمل للحظر: Content-Type مفقود من السيرفر");
                        } else if (bodyBytes.length == 0) {
                            emitSignal("ad_debug", "  🔴 سبب محتمل للحظر: الاستجابة فارغة تماماً (0 bytes)");
                        } else if (status >= 300 && status < 400) {
                            emitSignal("ad_debug", "  🔴 سبب محتمل للحظر: إعادة توجيه (redirect) قد لا يُكمَّل بشكل صحيح");
                        }

                        // إعادة بناء الاستجابة لـ WebView (هذا قد يتجاوز ORB لأننا نمررها يدوياً)
                        Map<String, String> responseHeaders = new java.util.HashMap<>();
                        responseHeaders.put("Access-Control-Allow-Origin", "*");
                        String mimeType = contentType != null ? contentType.split(";")[0].trim() : "application/octet-stream";
                        String encoding = "UTF-8";

                        return new WebResourceResponse(
                                mimeType,
                                encoding,
                                status,
                                (status >= 200 && status < 300) ? "OK" : "Error",
                                responseHeaders,
                                new java.io.ByteArrayInputStream(bodyBytes)
                        );

                    } catch (Exception e) {
                        emitSignal("ad_debug", "  ❌ فشل الفحص اليدوي: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                        return super.shouldInterceptRequest(view, request);
                    }
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    emitSignal("ad_debug", "🔴 Error: " + error.getDescription()
                            + " (code " + error.getErrorCode() + ")"
                            + " | URL: " + request.getUrl());
                }

                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    emitSignal("ad_debug", "🔴 HTTP Error: " + errorResponse.getStatusCode()
                            + " | Content-Type: " + errorResponse.getMimeType()
                            + " | URL: " + request.getUrl());
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    emitSignal("ad_debug", "SSL Error: " + error.toString());
                    handler.proceed();
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    emitSignal("ad_debug", "✅ تم التحميل بنجاح: " + url);
                    view.evaluateJavascript("(function(){ return document.body.innerHTML.length; })();",
                        value -> {
                            if (Integer.parseInt(value) < 50) emitSignal("ad_debug", "⚠ تحذير: محتوى الصفحة فارغ تقريباً!");
                        });
                }
            });

            String html = "<!DOCTYPE html><html><head>"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "<style>body{margin:0;padding:0;background:transparent;overflow:hidden;}</style>"
                        + "</head><body>"
                        + "<script>atOptions = { 'key' : '" + zoneId + "', 'format' : 'iframe', 'height' : 50, 'width' : 320, 'params' : {} };</script>"
                        + "<script src='https://www.highperformanceformat.com/" + zoneId + "/invoke.js'></script>"
                        + "</body></html>";

            webView.loadDataWithBaseURL("https://www.highperformanceformat.com/", html, "text/html", "UTF-8", null);

            int heightPx = (int)(60 * activity.getResources().getDisplayMetrics().density);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
            params.gravity = Gravity.TOP;
            activity.addContentView(webView, params);
        });
    }

    @UsedByGodot
    public void showBanner() {
        activity.runOnUiThread(() -> { if (webView != null) webView.setVisibility(View.VISIBLE); });
    }

    @UsedByGodot
    public void hideBanner() {
        activity.runOnUiThread(() -> { if (webView != null) webView.setVisibility(View.GONE); });
    }

    @UsedByGodot
    public void removeBanner() {
        activity.runOnUiThread(() -> {
            if (webView != null) {
                if (webView.getParent() != null) {
                    ((ViewGroup) webView.getParent()).removeView(webView);
                }
                webView.destroy();
                webView = null;
                currentZoneId = null;
            }
        });
    }
}
