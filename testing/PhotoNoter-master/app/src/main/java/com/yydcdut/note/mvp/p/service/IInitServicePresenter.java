package com.yydcdut.note.mvp.p.service;

import com.yydcdut.note.mvp.IPresenter;

/**
 * Created by yuyidong on 15/11/22.
 */
public interface IInitServicePresenter extends IPresenter {
    void initContent();

    void initCamera();

    boolean isFinish();
}
