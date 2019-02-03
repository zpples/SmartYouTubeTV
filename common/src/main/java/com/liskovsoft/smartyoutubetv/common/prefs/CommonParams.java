package com.liskovsoft.smartyoutubetv.common.prefs;

import android.content.Context;
import com.liskovsoft.common.configparser.AssetPropertyParser2;
import com.liskovsoft.common.configparser.ConfigParser;

public final class CommonParams {
    private final Context mContext;
    private final ConfigParser mParser;
    private static CommonParams sInstance;

    public static CommonParams instance(Context context) {
        if (sInstance == null) {
            sInstance = new CommonParams(context);
        }

        return sInstance;
    }

    private CommonParams(Context context) {
        mContext = context;
        mParser = new AssetPropertyParser2(context, "ads.properties", "service.properties", "common.properties");
    }

    public String[] getAdsUrls() {
        return mParser.getArray("ads_urls");
    }

    public String getMainPageUrl() {
        return mParser.get("main_page_url");
    }

    public String getMusicPageUrl() {
        return mParser.get("music_page_url");
    }

    public String getSubscriptionsPageUrl() {
        return mParser.get("subscriptions_page_url");
    }

    public String[] getStableUpdateUrls() {
        return mParser.getArray("stable_urls");
    }

    public String[] getBetaUpdateUrls() {
        return mParser.getArray("beta_urls");
    }

    public String[] getOldPackageNames() {
        return mParser.getArray("old_packages");
    }
}
