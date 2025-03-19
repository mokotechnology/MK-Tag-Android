package com.moko.bxp.tag.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.View;

import com.moko.bxp.tag.BuildConfig;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.databinding.ActivityAboutBinding;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.bxp.tag.utils.Utils;

import java.io.File;
import java.util.Calendar;


public class AboutActivity extends BaseActivity<ActivityAboutBinding> {

    @Override
    protected void onCreate() {
        if (!BuildConfig.IS_LIBRARY) {
            mBind.appVersion.setText(String.format("APP Version:V%s", Utils.getVersionInfo(this)));
            mBind.tvFeedbackLog.setVisibility(View.VISIBLE);
        }
        mBind.tvCompanyWebsite.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    @Override
    protected ActivityAboutBinding getViewBinding() {
        return ActivityAboutBinding.inflate(getLayoutInflater());
    }

    public void onBack(View view) {
        finish();
    }

    public void onCompanyWebsite(View view) {
        if (isWindowLocked())
            return;
        Uri uri = Uri.parse("https://" + getString(R.string.company_website));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void onFeedback(View view) {
        if (isWindowLocked())
            return;
        File trackerLog = new File(TagMainActivity.PATH_LOGCAT + File.separator + "MOKOTAG.txt");
        File trackerLogBak = new File(TagMainActivity.PATH_LOGCAT + File.separator + "MOKOTAG.txt.bak");
        File trackerCrashLog = new File(TagMainActivity.PATH_LOGCAT + File.separator + "crash_log.txt");
        if (!trackerLog.exists() || !trackerLog.canRead()) {
            ToastUtils.showToast(this, "File is not exists!");
            return;
        }
        String address = "Development@mokotechnology.com";
        StringBuilder mailContent = new StringBuilder("MOKOTAG_");
        Calendar calendar = Calendar.getInstance();
        String date = Utils.calendar2strDate(calendar, "yyyyMMdd");
        mailContent.append(date);
        String title = mailContent.toString();
        if ((!trackerLogBak.exists() || !trackerLogBak.canRead())
                && (!trackerCrashLog.exists() || !trackerCrashLog.canRead())) {
            Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog);
        } else if (!trackerCrashLog.exists() || !trackerCrashLog.canRead()) {
            Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog, trackerLogBak);
        } else if (!trackerLogBak.exists() || !trackerLogBak.canRead()) {
            Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog, trackerCrashLog);
        } else {
            Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog, trackerLogBak, trackerCrashLog);
        }
    }
}
