package com.yydcdut.note.mvp.p.setting.impl;

import android.content.Context;

import com.yydcdut.note.injector.ContextLife;
import com.yydcdut.note.mvp.IView;
import com.yydcdut.note.mvp.p.setting.IAboutAppPresenter;
import com.yydcdut.note.mvp.v.setting.IAboutAppView;
import com.yydcdut.note.utils.PhoneUtils;

import javax.inject.Inject;

/**
 * Created by yuyidong on 15/11/13.
 */
public class AboutAppPresenterImpl implements IAboutAppPresenter {
    private IAboutAppView mAboutAppView;

    private Context mContext;

    @Inject
    public AboutAppPresenterImpl(@ContextLife("Activity") Context context) {
        mContext = context;
    }

    @Override
    public void attachView(IView iView) {
        mAboutAppView = (IAboutAppView) iView;
        mAboutAppView.showVersion(PhoneUtils.getVersion(mContext));
    }

    @Override
    public void detachView() {

    }

    @Override
    public void doUpdateVersion() {
        mAboutAppView.updateApk();
    }

    @Override
    public void doFeedback() {
        mAboutAppView.jump2FeedbackActivity();
    }

    @Override
    public void doShare() {
        mAboutAppView.share();
    }

    @Override
    public void gotoGithub() {
        mAboutAppView.viewGitHub();
    }

}
