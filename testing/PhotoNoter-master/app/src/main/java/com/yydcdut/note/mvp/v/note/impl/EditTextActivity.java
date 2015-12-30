package com.yydcdut.note.mvp.v.note.impl;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.yydcdut.note.NoteApplication;
import com.yydcdut.note.R;
import com.yydcdut.note.injector.component.DaggerActivityComponent;
import com.yydcdut.note.injector.module.ActivityModule;
import com.yydcdut.note.mvp.p.note.impl.EditTextPresenterImpl;
import com.yydcdut.note.mvp.v.BaseActivity;
import com.yydcdut.note.mvp.v.note.IEditTextView;
import com.yydcdut.note.utils.Const;
import com.yydcdut.note.utils.Utils;
import com.yydcdut.note.view.CircleProgressBarLayout;
import com.yydcdut.note.view.KeyBoardResizeFrameLayout;
import com.yydcdut.note.view.RevealView;
import com.yydcdut.note.view.VoiceRippleView;
import com.yydcdut.note.view.fab2.FloatingMenuLayout;
import com.yydcdut.note.view.fab2.snack.OnSnackBarActionListener;
import com.yydcdut.note.view.fab2.snack.SnackHelper;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import us.pinguo.edit.sdk.base.widget.AnimationAdapter;

/**
 * Created by yyd on 15-4-8.
 */
public class EditTextActivity extends BaseActivity implements IEditTextView, View.OnClickListener,
        KeyBoardResizeFrameLayout.OnKeyBoardShowListener, FloatingMenuLayout.OnFloatingActionsMenuUpdateListener {
    private static final String TAG = EditTextActivity.class.getSimpleName();
    /* title是否显示出来? */
    private boolean mIsEditTextShow = true;

    /* Views */
    @Bind(R.id.toolbar_edit)
    Toolbar mToolbar;
    @Bind(R.id.layout_edit_title)
    View mLayoutTitle;
    @Bind(R.id.et_edit_title)
    EditText mTitleEdit;
    @Bind(R.id.et_edit_content)
    EditText mContentEdit;
    @Bind(R.id.layout_fab_edittext)
    FloatingMenuLayout mFabMenuLayout;
    @Bind(R.id.img_ripple_fab)
    VoiceRippleView mVoiceRippleView;
    @Bind(R.id.layout_fab_voice_start)
    View mVoiceFabLayout;
    @Bind(R.id.txt_voice)
    View mVoiceTextView;
    @Bind(R.id.layout_voice)
    View mVoiceLayout;
    @Bind(R.id.layout_progress)
    CircleProgressBarLayout mProgressLayout;
    @Bind(R.id.reveal_fab)
    RevealView mFabRevealView;
    @Bind(R.id.reveal_voice)
    RevealView mVoiceRevealView;
    @Bind(R.id.view_fab_location)
    View mFabPositionView;
    ImageView mMenuArrowImage;

    @Inject
    EditTextPresenterImpl mEditTextPresenter;

    private static final String TAG_ARROW = "tag_arrow";

    @Override
    public boolean setStatusBar() {
        return true;
    }

    @Override
    public int setContentView() {
        return R.layout.activity_edit;
    }

    @Override
    public void initInjector() {
        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .applicationComponent(((NoteApplication) getApplication()).getApplicationComponent())
                .build();
        mActivityComponent.inject(this);
    }

    @Override
    public void initUiAndListener() {
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        mEditTextPresenter.bindData(bundle.getInt(Const.CATEGORY_ID_4_PHOTNOTES),
                bundle.getInt(Const.PHOTO_POSITION), bundle.getInt(Const.COMPARATOR_FACTORY));
        mEditTextPresenter.attachView(this);
        initToolBar();
        initFloating();
        initOtherUI();
    }

    void initOtherUI() {
        ((KeyBoardResizeFrameLayout) findViewById(R.id.layout_root)).setOnKeyboardShowListener(this);
        mFabRevealView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mFabMenuLayout.close();
                return true;
            }
        });
        mVoiceLayout.setVisibility(View.INVISIBLE);
    }

    private void initToolBar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_check_white_24dp);

        int actionbarHeight = getActionBarSize();
        float dimen24dip = getResources().getDimension(R.dimen.dimen_24dip);
        int margin = (int) ((actionbarHeight - dimen24dip) / 2);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_toolbar);

        ViewGroup.MarginLayoutParams mp = new ViewGroup.MarginLayoutParams((int) dimen24dip, (int) dimen24dip);  //item的宽高
        mp.setMargins(margin, margin, margin, margin);//分别是margin_top那四个属性
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mp);

        mMenuArrowImage = new ImageView(this);
        mMenuArrowImage.setImageResource(R.drawable.ic_keyboard_arrow_down_white_24dp);
        mMenuArrowImage.setLayoutParams(lp);
        mMenuArrowImage.setTag(TAG_ARROW);
        mMenuArrowImage.setOnClickListener(this);

        linearLayout.addView(mMenuArrowImage);
    }

    private void initFloating() {
        mFabMenuLayout.setOnFloatingActionsMenuUpdateListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mEditTextPresenter.saveText();
                mEditTextPresenter.finishActivity(true);
                break;
        }
        return true;
    }

    @Override
    public void startActivityAnimation() {
        int actionBarHeight = getActionBarSize();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        int contentEditHeight = screenHeight - actionBarHeight * 2;
        AnimatorSet animation = new AnimatorSet();
        animation.setDuration(Const.DURATION_ACTIVITY);
        animation.playTogether(
                ObjectAnimator.ofFloat(mToolbar, "translationY", -actionBarHeight, 0),
                ObjectAnimator.ofFloat(mLayoutTitle, "translationY", -actionBarHeight * 2, 0),
                ObjectAnimator.ofFloat(mContentEdit, "translationY", contentEditHeight, 0),
                ObjectAnimator.ofFloat(mFabMenuLayout, "translationY", contentEditHeight, 0)
        );
        animation.start();
    }

    private void openEditTextAnimation() {
        AnimatorSet animation = new AnimatorSet();
        animation.setDuration(Const.DURATION);
        animation.playTogether(
                ObjectAnimator.ofFloat(mMenuArrowImage, "rotationX", 180f, 0f),
                ObjectAnimator.ofFloat(mMenuArrowImage, "rotationY", 180f, 0f),
                ObjectAnimator.ofFloat(mTitleEdit, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(mLayoutTitle, "Y", 0f, getActionBarSize())
        );
        animation.start();
    }

    private void closeEditTextAnimation() {
        AnimatorSet animation = new AnimatorSet();
        animation.setDuration(Const.DURATION);
        animation.playTogether(
                ObjectAnimator.ofFloat(mMenuArrowImage, "rotationX", 0f, 180f),
                ObjectAnimator.ofFloat(mMenuArrowImage, "rotationY", 0f, 180f),
                ObjectAnimator.ofFloat(mTitleEdit, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(mLayoutTitle, "Y", getActionBarSize(), 0f)
        );
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLayoutTitle.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mLayoutTitle.setVisibility(View.GONE);
            }
        });
        animation.start();
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof String) {
            switch ((String) v.getTag()) {
                case TAG_ARROW:
                    if (mIsEditTextShow) {
                        mIsEditTextShow = false;
                        closeEditTextAnimation();
                    } else {
                        mIsEditTextShow = true;
                        openEditTextAnimation();
                        mLayoutTitle.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            return;
        }
    }

    @OnClick(R.id.fab_voice)
    public void clickFabVoice(View v) {
        mFabMenuLayout.close();
        revealVoiceAndStart();
    }

    @OnClick(R.id.fab_evernote_update)
    public void clickEvernoteUpdate(View v) {
        mFabMenuLayout.close();
        mEditTextPresenter.update2Evernote();
    }

    @OnClick(R.id.fab_voice_stop)
    public void clickVoiceStart(View v) {
        mEditTextPresenter.stopVoice();
    }

    @Override
    public void onBackPressed() {
        mEditTextPresenter.onBackPressEvent();
    }

    @Override
    public boolean isFabMenuLayoutOpen() {
        return mFabMenuLayout.isOpen();
    }

    @Override
    public void closeFabMenuLayout() {
        mFabMenuLayout.close();
    }

    @Override
    public void setFabMenuLayoutClickable(boolean clickable) {
        mFabMenuLayout.setMenuClickable(clickable);
    }

    @Override
    public void finishActivityWithAnimation(final boolean saved, final int categoryId, final int position, final int comparator) {
        int actionBarHeight = getActionBarSize();
        int screenHeight = Utils.sScreenHeight;
        int contentEditHeight = screenHeight - actionBarHeight * 2;
        AnimatorSet animation = new AnimatorSet();
        animation.setDuration(Const.DURATION_ACTIVITY);
        animation.playTogether(
                ObjectAnimator.ofFloat(mToolbar, "translationY", 0, -actionBarHeight),
                ObjectAnimator.ofFloat(mLayoutTitle, "translationY", 0, -actionBarHeight * 2),
                ObjectAnimator.ofFloat(mContentEdit, "translationY", 0, contentEditHeight),
                ObjectAnimator.ofFloat(mFabMenuLayout, "translationY", 0, contentEditHeight)
        );
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!saved) {
                    setResult(RESULT_NOTHING);
                    EditTextActivity.this.finish();
                    overridePendingTransition(R.anim.activity_no_animation, R.anim.activity_no_animation);
                } else {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(Const.PHOTO_POSITION, position);
                    bundle.putInt(Const.CATEGORY_ID_4_PHOTNOTES, categoryId);
                    bundle.putInt(Const.COMPARATOR_FACTORY, comparator);
                    intent.putExtras(bundle);
                    setResult(RESULT_DATA, intent);
                    EditTextActivity.this.finish();
                    overridePendingTransition(R.anim.activity_no_animation, R.anim.activity_no_animation);
                }
            }
        });
        animation.start();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeyboardShow() {
        mFabMenuLayout.close();
        mFabMenuLayout.setMenuClickable(false);
    }

    @Override
    public void onKeyboardHide() {
        mFabMenuLayout.setMenuClickable(true);
    }

    @Override
    public void onMenuExpanded() {
        Point p = getLocationInView(mFabRevealView, mFabPositionView);
        mFabRevealView.reveal(p.x, p.y, getResources().getColor(R.color.fab_reveal_black), Const.RADIUS, Const.DURATION, null);
    }

    @Override
    public void onMenuCollapsed() {
        Point p = getLocationInView(mFabRevealView, mFabPositionView);
        mFabRevealView.hide(p.x, p.y, Color.TRANSPARENT, 0, Const.DURATION, null);
    }

    private void revealVoiceAndStart() {
        mVoiceLayout.setVisibility(View.VISIBLE);
        mVoiceLayout.setOnClickListener(this);
        Point p = getLocationInView(mVoiceRevealView, mFabPositionView);
        mVoiceRevealView.reveal(p.x, p.y, getResources().getColor(R.color.bg_background),
                1, Const.DURATION, new RevealView.RevealAnimationListener() {
                    @Override
                    public void finish() {
                        mVoiceFabLayout.setVisibility(View.VISIBLE);
                        mVoiceTextView.setVisibility(View.VISIBLE);
                        Animation animation = AnimationUtils.loadAnimation(EditTextActivity.this, R.anim.anim_scale_small_2_big);
                        animation.setDuration(300l);
                        animation.setAnimationListener(new AnimationAdapter() {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                super.onAnimationEnd(animation);
                                mVoiceRippleView.startAnimation();
                                mEditTextPresenter.startVoice();
                            }
                        });
                        mVoiceFabLayout.startAnimation(animation);
                        mVoiceTextView.setAnimation(AnimationUtils.loadAnimation(EditTextActivity.this, R.anim.anim_alpha_in));
                    }
                });
    }

    @Override
    public void hideVoiceAnimation() {
        Animation alphaAnimation = AnimationUtils.loadAnimation(EditTextActivity.this, R.anim.anim_alpha_out);
        mVoiceTextView.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mVoiceRippleView.pauseAnimation();
                mVoiceFabLayout.startAnimation(AnimationUtils.loadAnimation(EditTextActivity.this, R.anim.anim_scale_big_2_small));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mVoiceFabLayout.setVisibility(View.GONE);
                mVoiceTextView.setVisibility(View.GONE);
                Point p = getLocationInView(mVoiceRevealView, mFabPositionView);
                mVoiceRevealView.hide(p.x, p.y, Color.TRANSPARENT, 0, Const.DURATION, new RevealView.RevealAnimationListener() {
                    @Override
                    public void finish() {
                        mVoiceLayout.setOnClickListener(null);
                        mVoiceLayout.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void setRippleVoice(float volume) {
        mVoiceRippleView.setVoice((volume / 50f) / 1.0f);
    }

    @Override
    public String getNoteTitle() {
        return mTitleEdit.getText().toString();
    }

    @Override
    public String getNoteContent() {
        return mContentEdit.getText().toString();
    }

    @Override
    public void setNoteTitle(String title) {
        mTitleEdit.setText(title);
    }

    @Override
    public void setNoteContent(String content) {
        mContentEdit.setText(content);
    }

    @Override
    public void showProgressBar() {
        mProgressLayout.show();
    }

    @Override
    public void hideProgressBar() {
        mProgressLayout.hide();
    }

    @Override
    public void showSnakeBar(String messge) {
        SnackHelper.make(mFabMenuLayout, messge, SnackHelper.LENGTH_SHORT).show(mFabMenuLayout);
    }

    @Override
    public void showSnakeBarWithAction(String message, String action, final OnSnackBarActionListener listener) {
        SnackHelper.make(mFabMenuLayout, message, SnackHelper.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onClick();
                        }
                    }
                }).show(mFabMenuLayout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditTextPresenter.detachView();
        mVoiceRippleView.stopAnimation();
    }
}
