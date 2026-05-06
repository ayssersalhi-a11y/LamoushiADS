package com.lamoushi.ads;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import com.adsterra.sdk.BannerAd; // استيراد مكتبة ادستيرا

public class LamoushiAds extends GodotPlugin {
    private Activity activity;
    private BannerAd bannerAd;

    public LamoushiAds(Godot godot) {
        super(godot);
        this.activity = godot.getActivity();
    }

    @Override
    public String getPluginName() {
        return "LamoushiAds";
    }

    @UsedByGodot
    public void loadBanner(final String zoneId) {
        activity.runOnUiThread(() -> {
            if (bannerAd != null) return;

            bannerAd = new BannerAd(activity);
            bannerAd.setPlacementId(zoneId);
            
            // ضبط مكان الإعلان في الأعلى (Top)
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.TOP; 

            activity.addContentView(bannerAd, params);
            bannerAd.loadAd();
        });
    }
}
