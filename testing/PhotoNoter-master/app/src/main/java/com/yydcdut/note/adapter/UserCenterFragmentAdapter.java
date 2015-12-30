package com.yydcdut.note.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yydcdut.note.mvp.v.login.impl.UserDetailFragment;
import com.yydcdut.note.utils.Const;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuyidong on 15/10/22.
 */
public class UserCenterFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragmentList;

    public UserCenterFragmentAdapter(FragmentManager fm) {
        super(fm);
        mFragmentList = new ArrayList<>(2);
        UserDetailFragment firstFragment = UserDetailFragment.newInstance();
        Bundle firstBundle = new Bundle();
        firstBundle.putInt(Const.USER_DETAIL_TYPE, 0);
        firstFragment.setArguments(firstBundle);

        UserDetailFragment secondFragment = UserDetailFragment.newInstance();
        Bundle thirdBundle = new Bundle();
        thirdBundle.putInt(Const.USER_DETAIL_TYPE, 1);
        secondFragment.setArguments(thirdBundle);

        mFragmentList.add(firstFragment);
        mFragmentList.add(secondFragment);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
