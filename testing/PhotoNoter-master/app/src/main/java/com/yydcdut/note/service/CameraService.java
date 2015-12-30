package com.yydcdut.note.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.yydcdut.note.ICameraData;
import com.yydcdut.note.NoteApplication;
import com.yydcdut.note.injector.component.DaggerServiceComponent;
import com.yydcdut.note.injector.module.ServiceModule;
import com.yydcdut.note.mvp.p.service.impl.CameraServicePresenterImpl;
import com.yydcdut.note.mvp.v.service.ICameraServiceView;
import com.yydcdut.note.utils.Const;

import javax.inject.Inject;

/**
 * Created by yuyidong on 15/7/17.
 */
public class CameraService extends Service implements ICameraServiceView {
    private static final String TAG = CameraService.class.getSimpleName();

    @Inject
    CameraServicePresenterImpl mCameraServicePresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerServiceComponent.builder()
                .serviceModule(new ServiceModule(this))
                .applicationComponent(((NoteApplication) getApplication()).getApplicationComponent())
                .build()
                .inject(this);
        mCameraServicePresenter.attachView(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mCameraServicePresenter.stopThread();
        return super.onUnbind(intent);
    }

    /**
     * 加添到Service之后，添加到数据库中，作图，作图完成的话就从数据库中删除
     */
    ICameraData.Stub mStub = new ICameraData.Stub() {
        @Override
        public void add(String fileName, int size, String cameraId, long time, int categoryId,
                        boolean isMirror, int ratio, int orientation,
                        String latitude, String lontitude, int whiteBalance, int flash,
                        int imageLength, int imageWidth, String make, String model) throws RemoteException {
            mCameraServicePresenter.add2DB(fileName, size, cameraId, time, categoryId,
                    isMirror, ratio, orientation, latitude, lontitude, whiteBalance, flash,
                    imageLength, imageWidth, make, model);
        }
    };

    @Override
    public void sendBroadCast() {
        /*
         * 发送广播到外面Album去更新界面
         */
        Intent intent = new Intent();
        //因为是另外个进程，所以....
        intent.putExtra(Const.TARGET_BROADCAST_PROCESS, true);
        intent.setAction(Const.BROADCAST_PHOTONOTE_UPDATE);
        sendBroadcast(intent);//这里会进行更新处理
    }
}
