package com.khayal.ads;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
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

/**
 * KhayalWebView
 * =============
 * يعرض صفحة ويب حقيقية (WebView أصلي بأندرويد) داخل التطبيق، بجلسة
 * المستخدم الحقيقية على جهازه الفعلي - بلا أي أتمتة خفية أو انتحال بصمة.
 * المستخدم نفسه يتفاعل مع الصفحة بإصبعه مباشرة.
 *
 * الميزات المدعومة (حسب النقاش):
 *   1. تصغير/تكبير/تحريك مساحة العرض (resizeWebView / setWebViewVisible)
 *   2. جسر بيانات حي بالاتجاهين أثناء التشغيل:
 *      - Godot -> الصفحة: runJavaScript(js) لتنفيذ أي كود JS وقراءة نتيجته
 *      - الصفحة -> Godot: نداء window.Khayal.postMessage(text) من داخل
 *        الصفحة نفسها يصل فوراً كإشارة "web_message" في Godot
 *   3. تصميم يسمح بوضع أزرار Godot خاصة بجانب/فوق/تحت منطقة الـWebView
 *      (وليس متراكبة مباشرة معها - قيد معروف بكل الـNative Views بـGodot)
 */
public class KhayalWebView extends GodotPlugin {

    private Activity activity;
    private WebView webView;
    private FrameLayout container;

    public KhayalWebView(Godot godot) {
        super(godot);
        this.activity = godot.getActivity();
    }

    @Override
    public String getPluginName() {
        return "KhayalWebView";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();
        signals.add(new SignalInfo("web_message", String.class));
        signals.add(new SignalInfo("page_loaded", String.class));
        signals.add(new SignalInfo("page_error", String.class));
        return signals;
    }

    // ============================================================
    // فتح الصفحة
    // ============================================================

    @UsedByGodot
    public void loadUrl(final String url) {
        activity.runOnUiThread(() -> {
            _ensureWebView();
            webView.loadUrl(url);
        });
    }

    private void _ensureWebView() {
        if (webView != null) {
            return;
        }

        container = new FrameLayout(activity);
        container.setBackgroundColor(Color.WHITE);

        webView = new WebView(activity);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        // نفس جلسة تصفح المستخدم الحقيقية على جهازه - بلا أي جلسة مزيَّفة
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // الجسر: أي صفحة مفتوحة تقدر تستدعي window.Khayal.postMessage("...")
        // من كود JS بها، فيصل النص فوراً كإشارة web_message بـGodot
        webView.addJavascriptInterface(new JsBridge(), "Khayal");

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                emitSignal("page_loaded", url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                emitSignal("page_error", description + " (" + failingUrl + ")");
            }
        });

        container.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        activity.addContentView(container, new FrameLayout.LayoutParams(0, 0));
    }

    // ============================================================
    // 1) التحكم بالحجم والموضع (بالبكسل الحقيقي على الجهاز)
    // ============================================================

    /**
     * يضبط موضع وحجم منطقة عرض الصفحة (تصغير/تكبير/تحريك).
     * الوحدات بالبكسل الفعلي على الشاشة (وليس وحدات Godot الافتراضية) -
     * اضرب قيم Godot بمعامل الكثافة إن احتجت تطابقاً دقيقاً مع واجهتك.
     */
    @UsedByGodot
    public void resizeWebView(final int x, final int y, final int width, final int height) {
        activity.runOnUiThread(() -> {
            if (container == null) {
                return;
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            params.leftMargin = x;
            params.topMargin = y;
            params.gravity = Gravity.TOP | Gravity.START;
            container.setLayoutParams(params);
        });
    }

    /** اختصار: يملأ الشاشة كاملة (نفس مساحة نافذة Godot). */
    @UsedByGodot
    public void setFullscreen() {
        activity.runOnUiThread(() -> {
            if (container == null) {
                return;
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            container.setLayoutParams(params);
        });
    }

    @UsedByGodot
    public void setWebViewVisible(final boolean visible) {
        activity.runOnUiThread(() -> {
            if (container != null) {
                container.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });
    }

    // ============================================================
    // 2) جسر البيانات الحي (Godot <-> الصفحة أثناء التشغيل)
    // ============================================================

    /**
     * يُنفِّذ أي كود JavaScript داخل الصفحة المفتوحة حالياً. النتيجة
     * (إن وُجدت) تصل عبر نفس إشارة "web_message" بعد قليل من التنفيذ.
     * مثال استخدام من GDScript:
     *   KhayalWebView.runJavaScript("document.querySelector('.gpu-status').innerText")
     */
    @UsedByGodot
    public void runJavaScript(final String jsCode) {
        activity.runOnUiThread(() -> {
            if (webView == null) {
                return;
            }
            webView.evaluateJavascript(jsCode, value -> emitSignal("web_message", value));
        });
    }

    /**
     * الجسر الفعلي: أي صفحة مفتوحة بالـWebView تقدر تستدعي هذه الدالة
     * مباشرة من كود JS بها (window.Khayal.postMessage("نص")) لترسل أي
     * نص/بيانات حية لـGodot أثناء التشغيل، دون انتظار طلب من Godot أولاً.
     */
    private class JsBridge {
        @JavascriptInterface
        public void postMessage(final String text) {
            // يصل من ثريد JS الخاص بالـWebView - يُمرَّر مباشرة، Godot
            // نفسه يستقبل الإشارات بأمان من أي ثريد عبر emitSignal
            emitSignal("web_message", text);
        }
    }

    // ============================================================
    // إغلاق/تنظيف
    // ============================================================

    @UsedByGodot
    public void closeWebView() {
        activity.runOnUiThread(this::_destroy);
    }

    private void _destroy() {
        if (webView != null) {
            if (webView.getParent() != null) {
                ((ViewGroup) webView.getParent()).removeView(webView);
            }
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        if (container != null) {
            if (container.getParent() != null) {
                ((ViewGroup) container.getParent()).removeView(container);
            }
            container = null;
        }
    }

    @Override
    public void onMainDestroy() {
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(this::_destroy);
    }
}
