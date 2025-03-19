package com.moko.bxp.tag.dialog;

import android.content.Context;

import com.moko.bxp.tag.databinding.DialogUrlSchemeBinding;
import com.moko.support.tag.entity.UrlSchemeEnum;

public class UrlSchemeDialog extends BaseDialog<DialogUrlSchemeBinding> {

    private String urlScheme;

    public UrlSchemeDialog(Context context) {
        super(context);
    }

    @Override
    protected DialogUrlSchemeBinding getViewBind() {
        return DialogUrlSchemeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate() {
        UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlDesc(urlScheme);
        switch (urlSchemeEnum.getUrlType()) {
            case 0:
                mBind.rbHttpWww.setChecked(true);
                break;
            case 1:
                mBind.rbHttpsWww.setChecked(true);
                break;
            case 2:
                mBind.rbHttp.setChecked(true);
                break;
            case 3:
                mBind.rbHttps.setChecked(true);
                break;
        }
        mBind.tvCancel.setOnClickListener(v -> dismiss());
        mBind.tvEnsure.setOnClickListener(v -> {
            dismiss();
            urlSchemeClickListener.onEnsureClicked((String) findViewById(mBind.rgUrlScheme.getCheckedRadioButtonId()).getTag());
        });
    }

    public void setUrlScheme(String urlScheme) {
        this.urlScheme = urlScheme;
    }

    private UrlSchemeClickListener urlSchemeClickListener;

    public void setUrlSchemeClickListener(UrlSchemeClickListener urlSchemeClickListener) {
        this.urlSchemeClickListener = urlSchemeClickListener;
    }

    public interface UrlSchemeClickListener {

        void onEnsureClicked(String urlType);
    }
}
