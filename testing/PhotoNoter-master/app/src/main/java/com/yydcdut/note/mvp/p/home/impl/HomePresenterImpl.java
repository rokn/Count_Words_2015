package com.yydcdut.note.mvp.p.home.impl;

import android.app.Activity;

import com.baidu.mapapi.SDKInitializer;
import com.yydcdut.note.bean.Category;
import com.yydcdut.note.bus.CategoryCreateEvent;
import com.yydcdut.note.bus.CategoryDeleteEvent;
import com.yydcdut.note.bus.CategoryEditEvent;
import com.yydcdut.note.bus.CategoryMoveEvent;
import com.yydcdut.note.bus.CategoryUpdateEvent;
import com.yydcdut.note.bus.PhotoNoteCreateEvent;
import com.yydcdut.note.bus.PhotoNoteDeleteEvent;
import com.yydcdut.note.model.compare.ComparatorFactory;
import com.yydcdut.note.model.rx.RxCategory;
import com.yydcdut.note.model.rx.RxPhotoNote;
import com.yydcdut.note.model.rx.RxUser;
import com.yydcdut.note.mvp.IView;
import com.yydcdut.note.mvp.p.home.IHomePresenter;
import com.yydcdut.note.mvp.v.home.IHomeView;
import com.yydcdut.note.utils.LocalStorageUtils;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by yuyidong on 15/11/19.
 */
public class HomePresenterImpl implements IHomePresenter {
    private IHomeView mHomeView;
    /**
     * 当前的category的Id
     */
    private int mCategoryId = -1;

    private RxCategory mRxCategory;
    private RxPhotoNote mRxPhotoNote;
    private RxUser mRxUser;
    private LocalStorageUtils mLocalStorageUtils;
    private Activity mActivity;

    @Inject
    public HomePresenterImpl(Activity activity, RxCategory rxCategory, RxPhotoNote rxPhotoNote, RxUser rxUser,
                             LocalStorageUtils localStorageUtils) {
        mRxCategory = rxCategory;
        mRxPhotoNote = rxPhotoNote;
        mRxUser = rxUser;
        mLocalStorageUtils = localStorageUtils;
        mActivity = activity;
    }

    @Override
    public void attachView(IView iView) {
        mHomeView = (IHomeView) iView;
        EventBus.getDefault().register(this);
        initBaiduSdk();
    }

    private void initBaiduSdk() {
        SDKInitializer.initialize(mActivity.getApplication());
    }

    @Override
    public void detachView() {
        EventBus.getDefault().unregister(this);
    }

    public void setCategoryId(int categoryId) {
        mCategoryId = categoryId;
    }

    @Override
    public int getCategoryId() {
        return mCategoryId;
    }

    @Override
    public void setCheckCategoryPosition() {
        //todo
        mRxCategory.getAllCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categories -> {
                    boolean checkSuccessful = false;
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).isCheck()) {
                            mHomeView.setCheckPosition(i);
                            checkSuccessful = true;
                            break;
                        }
                    }
                    if (!checkSuccessful) {
                        mHomeView.setCheckPosition(0);
                    }
                });
    }

    @Override
    public void setCheckedCategoryPosition(int position) {
        mRxCategory.getAllCategories()
                .subscribe(categories -> {
                    mRxCategory.setCategoryMenuPosition(categories.get(position).getId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(categories1 -> {
                                mHomeView.notifyCategoryDataChanged();
                                mCategoryId = categories1.get(position).getId();
                                mHomeView.changeFragment(mCategoryId);
                            });
                });
    }

    @Override
    public void changeCategoryAfterSaving(Category category) {
        mRxCategory.setCategoryMenuPosition(category.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categories -> {
                    mHomeView.notifyCategoryDataChanged();
                    mCategoryId = category.getId();
                    mHomeView.changePhotos4Category(mCategoryId);
                });
    }

    @Override
    public void setAdapter() {
        mRxCategory.getAllCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categories -> mHomeView.setCategoryList(categories));
    }

    @Override
    public void drawerUserClick(int which) {
        switch (which) {
            case USER_ONE:
                mRxUser.isLoginQQ()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aBoolean -> {
                            if (aBoolean) {
                                mHomeView.jump2UserCenterActivity();
                            } else {
                                mHomeView.jump2LoginActivity();
                            }
                        });
                break;
            case USER_TWO:
                mRxUser.isLoginEvernote()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aBoolean -> {
                            if (aBoolean) {
                                mHomeView.jump2UserCenterActivity();
                            } else {
                                mHomeView.jump2LoginActivity();
                            }
                        });
                break;
        }
    }

    @Override
    public void drawerCloudClick() {
        mHomeView.cloudSyncAnimation();
    }

    @Override
    public void updateQQInfo() {
        mRxUser.isLoginQQ()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        mRxUser.getQQ()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(iUser -> mHomeView.updateQQInfo(true, iUser.getName(), iUser.getImagePath()));
                    } else {
                        mHomeView.updateQQInfo(false, null, null);
                    }
                });
    }

    @Override
    public void updateEvernoteInfo() {
        mRxUser.isLoginEvernote()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        mHomeView.updateEvernoteInfo(true);
                    } else {
                        mHomeView.updateEvernoteInfo(false);
                    }
                });

    }

    @Override
    public void updateFromBroadcast(boolean broadcast_process, boolean broadcast_service) {
        //有时候categoryLabel为null，感觉原因是activity被回收了，但是一直解决不掉，所以迫不得已的解决办法
        if (mCategoryId == -1) {
            mRxCategory.getAllCategories()
                    .subscribe(categories -> {
                        for (Category category : categories) {
                            if (category.isCheck()) {
                                mCategoryId = category.getId();
                            }
                        }
                    });
        }

        //从另外个进程过来的数据
        if (broadcast_process) {
            mRxPhotoNote.refreshByCategoryId(mCategoryId, ComparatorFactory.FACTORY_NOT_SORT)
                    .subscribe(photoNoteList -> {
                        mRxCategory.findByCategoryId(mCategoryId)
                                .subscribe(category -> {
                                    category.setPhotosNumber(photoNoteList.size());
                                    mRxCategory.updateCategory(category)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(categories -> {
                                                mHomeView.notifyCategoryDataChanged();
                                            });
                                });
                    });
        }

        //从Service中来
        if (broadcast_service) {
            mRxCategory.getAllCategories()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(categories -> mHomeView.updateCategoryList(categories));
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onCategoryCreateEvent(CategoryCreateEvent categoryCreateEvent) {
        mRxCategory.getAllCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categories -> mHomeView.updateCategoryList(categories));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onCategoryUpdateEvent(CategoryUpdateEvent categoryUpdateEvent) {
        mRxCategory.getAllCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categories -> mHomeView.updateCategoryList(categories));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onCategoryMoveEvent(CategoryMoveEvent categoryMoveEvent) {
        mRxCategory.getAllCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categories -> mHomeView.updateCategoryList(categories));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onCategoryRenameEvent(CategoryEditEvent categoryEditEvent) {
        mRxCategory.getAllCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categories -> mHomeView.updateCategoryList(categories));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onCategoryDeleteEvent(CategoryDeleteEvent categoryDeleteEvent) {
        mRxCategory.getAllCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categories -> {
                    int beforeCategoryId = mCategoryId;
                    for (Category category : categories) {
                        if (category.isCheck()) {
                            mCategoryId = category.getId();
                            break;
                        }
                    }
                    mHomeView.updateCategoryList(categories);
                    if (mCategoryId != beforeCategoryId) {
                        mHomeView.changePhotos4Category(mCategoryId);
                    }
                });

    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onPhotoNoteCreateEvent(PhotoNoteCreateEvent photoNoteCreateEvent) {
        mRxPhotoNote.findByCategoryId(mCategoryId, ComparatorFactory.FACTORY_NOT_SORT)
                .subscribe(photoNoteList -> {
                    mRxCategory.findByCategoryId(mCategoryId)
                            .subscribe(category -> {
                                category.setPhotosNumber(photoNoteList.size());
                                mRxCategory.updateCategory(category)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(categories -> {
                                            mHomeView.updateCategoryList(categories);
                                        });
                            });
                });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onPhotoNoteDeleteEvent(PhotoNoteDeleteEvent photoNoteDeleteEvent) {
        mRxPhotoNote.findByCategoryId(mCategoryId, ComparatorFactory.FACTORY_NOT_SORT)
                .subscribe(photoNoteList -> {
                    mRxCategory.findByCategoryId(mCategoryId)
                            .subscribe(category -> {
                                category.setPhotosNumber(photoNoteList.size());
                                mRxCategory.updateCategory(category)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(categories -> {
                                            mHomeView.updateCategoryList(categories);
                                        });
                            });
                });
    }
}
