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
import android.widget.FrameLayout;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.HashSet;
import java.util.Set;

public class LamoushiRewarded extends GodotPlugin {
    private Activity activity;
    private WebView hostWebView;   // الصفحة التي تحمل سكربت الـ Popunder
    private WebView popWebView;    // النافذة المنبثقة الفعلية (window.open)
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean rewardGranted = false;
    private static final int REWARD_DELAY_MS = 10000; // مدة الانتظار قبل منح المكافأة

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
        signals.add(new SignalInfo("reward_debug", String.class));
        signals.add(new SignalInfo("reward_ad_opened", new Class<?>[]{}));
        signals.add(new SignalInfo("reward_ready", new Class<?>[]{}));
        signals.add(new SignalInfo("reward_ad_closed", new Class<?>[]{}));
        return signals;
    }

    // استدعِها مسبقاً (عند فتح المشهد مثلاً) لتحميل الإعلان في الخلفية
    @UsedByGodot
    public void loadRewardedAd() {
        activity.runOnUiThread(() -> {
            rewardGranted = false;
            destroyHost();

            CookieManager cm = CookieManager.getInstance();
            cm.setAcceptCookie(true);

            hostWebView = new WebView(activity);
            WebSettings s = hostWebView.getSettings();
            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);
            s.setSupportMultipleWindows(true);
            s.setJavaScriptCanOpenWindowsAutomatically(true);
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cm.setAcceptThirdPartyCookies(hostWebView, true);
            }

            hostWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage cm2) {
                    emitSignal("reward_debug", "JS: " + cm2.message());
                    return true;
                }

                @Override
                public boolean onCreateWindow(WebView view, boolean isDialog,
                                               boolean isUserGesture, android.os.Message resultMsg) {
                    emitSignal("reward_debug", "✅ window.open() انطلقت — النافذة المنبثقة فُتحت فعلياً");

                    popWebView = new WebView(activity);
                    WebSettings ps = popWebView.getSettings();
                    ps.setJavaScriptEnabled(true);
                    ps.setDomStorageEnabled(true);

                    popWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView v, String url) {
                            emitSignal("reward_debug", "🔗 محتوى النافذة المنبثقة: " + url);
                        }
                    });

                    // WebView مخفي (1x1) فقط لاستضافة النافذة الناتجة عن window.open
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(1, 1);
                    activity.addContentView(popWebView, lp);

                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(popWebView);
                    resultMsg.sendToTarget();

                    emitSignal("reward_ad_opened");
                    scheduleReward();
                    return true;
                }
            });

            hostWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    emitSignal("reward_debug", "✅ صفحة الإعلان جاهزة، بانتظار ضغطة المستخدم");
                }
            });

            String html = "<!DOCTYPE html><html><head>"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "<style>html,body{margin:0;padding:0;background:transparent;} "
                        + "#trigger{position:fixed;top:0;left:0;width:100%;height:100%;}</style>"
                        + "</head><body>"
                        + "<div id='trigger'></div>"
                        + "<script src='" + AD_SCRIPT_URL + "'></script>"
                        + "</body></html>";

            hostWebView.loadDataWithBaseURL(AD_BASE_URL, html, "text/html", "UTF-8", null);

            FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(1, 1);
            activity.addContentView(hostWebView, hp);
        });
    }

    // استدعِها من GDScript عند ضغط المستخدم الفعلي على زر "شاهد إعلاناً للمكافأة"
    @UsedByGodot
    public void showRewardedAd() {
        activity.runOnUiThread(() -> {
            if (hostWebView == null) {
                emitSignal("reward_debug", "⚠ لم يُحمَّل الإعلان بعد — نادِ load_rewarded_ad أولاً");
                return;
            }
            hostWebView.evaluateJavascript(
                "(function(){ var el = document.getElementById('trigger'); "
              + "var ev = new MouseEvent('click', {bubbles:true, cancelable:true, view:window}); "
              + "el.dispatchEvent(ev); return true; })();",
                value -> emitSignal("reward_debug", "👆 تم تمرير ضغطة المستخدم لتفعيل الإعلان")
            );
        });
    }

    private void scheduleReward() {
        handler.postDelayed(() -> {
            if (!rewardGranted) {
                rewardGranted = true;
                emitSignal("reward_debug", "🎁 انتهت مدة الانتظار — المكافأة جاهزة للمنح");
                emitSignal("reward_ready");
            }
        }, REWARD_DELAY_MS);
    }

    @UsedByGodot
    public void closeRewardedAd() {
        activity.runOnUiThread(() -> {
            destroyPop();
            destroyHost();
            emitSignal("reward_ad_closed");
        });
    }

    private void destroyPop() {
        if (popWebView != null) {
            if (popWebView.getParent() != null) {
                ((ViewGroup) popWebView.getParent()).removeView(popWebView);
            }
            popWebView.destroy();
            popWebView = null;
        }
    }

    private void destroyHost() {
        if (hostWebView != null) {
            if (hostWebView.getParent() != null) {
                ((ViewGroup) hostWebView.getParent()).removeView(hostWebView);
            }
            hostWebView.destroy();
            hostWebView = null;
        }
    }

    @Override
    public void onMainDestroy() {
        destroyPop();
        destroyHost();
    }
                                             }
