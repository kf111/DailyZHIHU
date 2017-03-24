package com.neil.dailyzhihu.ui.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.View;

import com.neil.dailyzhihu.R;
import com.neil.dailyzhihu.api.API;
import com.neil.dailyzhihu.api.AtyExtraKeyConstant;
import com.neil.dailyzhihu.ui.aty.WebViewActivity;
import com.neil.dailyzhihu.utils.AppUtil;
import com.neil.dailyzhihu.utils.SnackbarUtil;

/**
 * 作者：Neil on 2017/2/26 01:34.
 * 邮箱：cn.neillee@gmail.com
 */

public class AboutFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private Context mContext;

    private Preference mAppIntro;
    private Preference mCheckUpdate;
    private Preference mVersionIntro;
    private Preference mShareApp;
    private Preference mBlog;
    private Preference mGithub;
    private Preference mEmail;
    private Preference mOpensourceLicense;

    private static final String APP_INTRO = "key_app_intro";
    private static final String CHECK_UPDATE = "key_checkupdate";
    private static final String VERSION_INFO = "key_versioninfo";
    private static final String SHARE_APP = "key_shareapp";
    private static final String BLOG = "key_blog";
    private static final String GITHUB = "key_github";
    private static final String EMAIL = "key_email";
    private static final String OPENSOURCE_LICENSE = "key_opensource_license";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);

        mContext = getActivity();
        initAllPreferences();
    }

    private void initAllPreferences() {
        mAppIntro = findPreference(APP_INTRO);
        mCheckUpdate = findPreference(CHECK_UPDATE);
        mVersionIntro = findPreference(VERSION_INFO);
        mShareApp = findPreference(SHARE_APP);
        mBlog = findPreference(BLOG);
        mGithub = findPreference(GITHUB);
        mEmail = findPreference(EMAIL);
        mOpensourceLicense = findPreference(OPENSOURCE_LICENSE);

        mCheckUpdate.setSummary(AppUtil.getVersionInfo(mContext));

        mAppIntro.setOnPreferenceClickListener(this);
        mCheckUpdate.setOnPreferenceClickListener(this);
        mVersionIntro.setOnPreferenceClickListener(this);
        mShareApp.setOnPreferenceClickListener(this);
        mBlog.setOnPreferenceClickListener(this);
        mGithub.setOnPreferenceClickListener(this);
        mEmail.setOnPreferenceClickListener(this);
        mOpensourceLicense.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        View view = getView();
        if (mAppIntro == preference) {
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra(AtyExtraKeyConstant.WEB_URL, API.APP_INTRODUCTION);
            startActivity(intent);
        } else if (mCheckUpdate == preference) {
            SnackbarUtil.ShortSnackbar(view, getResources().getString(R.string.to_do), SnackbarUtil.Confirm).show();
        } else if (mVersionIntro == preference) {
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra(AtyExtraKeyConstant.WEB_URL, API.VERSION_INFO);
            startActivity(intent);
        } else if (mShareApp == preference) {
            AppUtil.copyText2Clipboard(mContext, mContext.getResources().getString(R.string.shareapp_content));
            SnackbarUtil.ShortSnackbar(view, getResources().getString(R.string.notify_info_copied), SnackbarUtil.Info).show();
        } else if (mBlog == preference) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri content_url = Uri.parse(getString(R.string.blog_summary));
            intent.setData(content_url);
            startActivity(intent);
        } else if (mGithub == preference) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri content_url = Uri.parse(getString(R.string.github_address));
            intent.setData(content_url);
            startActivity(intent);
        } else if (mEmail == preference) {
            AppUtil.copyText2Clipboard(mContext, mContext.getResources().getString(R.string.email_address));
            SnackbarUtil.ShortSnackbar(view, getResources().getString(R.string.notify_info_copied), SnackbarUtil.Info).show();
        } else if (mOpensourceLicense == preference) {
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra(AtyExtraKeyConstant.WEB_URL, API.OPENSOURCE_LICENSE);
            startActivity(intent);
        }
        return true;
    }
}
