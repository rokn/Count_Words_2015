package com.yydcdut.note.mvp.v.setting;

import com.yydcdut.note.bean.Category;
import com.yydcdut.note.mvp.IView;

import java.util.List;

/**
 * Created by yuyidong on 15/11/15.
 */
public interface IEditCategoryView extends IView {

    void showProgressBar();

    void hideProgressBar();

    void finishActivity();

    void updateListView();

    void showSnackbar(String messgae);

    void showCategoryList(List<Category> categoryList);
}
