package com.yydcdut.note.mvp.p.service.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Message;

import com.yydcdut.note.bean.PhotoNote;
import com.yydcdut.note.bean.SandExif;
import com.yydcdut.note.bean.SandPhoto;
import com.yydcdut.note.model.rx.RxPhotoNote;
import com.yydcdut.note.model.rx.RxSandBox;
import com.yydcdut.note.mvp.IView;
import com.yydcdut.note.mvp.p.service.ISandBoxServicePresenter;
import com.yydcdut.note.mvp.v.service.ISandBoxServiceView;
import com.yydcdut.note.utils.Const;
import com.yydcdut.note.utils.FilePathUtils;
import com.yydcdut.note.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

/**
 * Created by yuyidong on 15/11/22.
 */
public class SandBoxServicePresenterImpl implements ISandBoxServicePresenter, Handler.Callback {
    private ISandBoxServiceView mSandBoxServiceView;

    private RxPhotoNote mRxPhotoNote;
    private RxSandBox mRxSandBox;

    @Inject
    public SandBoxServicePresenterImpl(RxSandBox rxSandBox, RxPhotoNote rxPhotoNote) {
        mRxPhotoNote = rxPhotoNote;
        mRxSandBox = rxSandBox;
    }

    @Override
    public void attachView(IView iView) {
        mSandBoxServiceView = (ISandBoxServiceView) iView;
        mSandBoxServiceView.notification();
        final Handler handler = new Handler(this);
        mRxSandBox.getNumber()
                .subscribe(integer -> {
                    if (integer > 0) {
                        int total = integer;
                        for (int i = 0; i < total; i++) {
                            mRxSandBox.findFirstOne()
                                    .subscribe(sandPhoto1 -> {
                                        if (sandPhoto1.getSize() == -1 || sandPhoto1.getFileName().equals("X")) {
                                            deleteFromDBAndSDCard(sandPhoto1);
                                        } else {
                                            makePhoto(sandPhoto1);
                                        }
                                    });
                        }
                        handler.sendEmptyMessage(0);
                    } else {
                        handler.sendEmptyMessage(0);
                    }
                });
    }

    /**
     * 做图
     *
     * @param sandPhoto
     */
    private void makePhoto(SandPhoto sandPhoto) {
        byte[] data = getDataFromFile(sandPhoto.getFileName(), sandPhoto.getSize());
        if (data == null) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        String fileName = getTime4Photo(sandPhoto.getTime()) + ".jpg";
        if (FilePathUtils.savePhoto(fileName, bitmap)) {
            FilePathUtils.saveSmallPhoto(fileName, bitmap);
        }
        PhotoNote photoNote = new PhotoNote(fileName, sandPhoto.getTime(), sandPhoto.getTime(), "", "",
                sandPhoto.getTime(), sandPhoto.getTime(), sandPhoto.getCategoryId());
        photoNote.setPaletteColor(Utils.getPaletteColor(bitmap));
        mRxPhotoNote.savePhotoNote(photoNote)
                .subscribe(photoNote1 -> {
                    try {
                        setExif(photoNote, sandPhoto.getSandExif(), sandPhoto.getCameraId(), sandPhoto.isMirror());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    deleteFromDBAndSDCard(sandPhoto);
                });
        bitmap.recycle();
        System.gc();
    }

    //todo isMirror不好做啊！
    private void setExif(PhotoNote photoNote, SandExif sandExif, String cameraId, boolean isMirror) throws IOException {
        ExifInterface exif = new ExifInterface(photoNote.getBigPhotoPathWithoutFile());
        if (cameraId.equals(Const.CAMERA_BACK)) {
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
        } else {
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
        }
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, sandExif.getLatitude());
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, sandExif.getLontitude());
        exif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, String.valueOf(sandExif.getWhiteBalance()));
        exif.setAttribute(ExifInterface.TAG_FLASH, String.valueOf(sandExif.getFlash()));
        exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(sandExif.getImageLength()));
        exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(sandExif.getImageWidth()));
        exif.setAttribute(ExifInterface.TAG_MAKE, sandExif.getMake());
        exif.setAttribute(ExifInterface.TAG_MODEL, sandExif.getModel());
        exif.saveAttributes();

        ExifInterface exif2 = new ExifInterface(photoNote.getSmallPhotoPathWithoutFile());
        if (cameraId.equals(Const.CAMERA_BACK)) {
            exif2.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
        } else {
            exif2.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
        }
        exif2.saveAttributes();
    }

    private byte[] getDataFromFile(String fileName, int size) {
        boolean bool = true;
        File file = new File(FilePathUtils.getSandBoxDir() + fileName);
        byte[] data;
        if (!file.exists()) {
            return null;
        }
        data = new byte[size];
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            inputStream.read(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            bool = false;
        } catch (IOException e) {
            e.printStackTrace();
            bool = false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    bool = false;
                }
            }
        }
        if (!bool) {
            return null;
        } else {
            return data;
        }
    }

    /**
     * 从数据库中删数据
     *
     * @param sandPhoto
     * @return
     */
    private void deleteFromDBAndSDCard(SandPhoto sandPhoto) {
        String path = FilePathUtils.getSandBoxDir() + sandPhoto.getFileName();
        mRxSandBox.deleteOne(sandPhoto)
                .subscribe(integer -> {
                    new File(path).delete();
                });
    }


    @Override
    public void detachView() {

    }

    @Override
    public boolean handleMessage(Message msg) {
        mSandBoxServiceView.sendBroadCast();
        mSandBoxServiceView.cancelNotification();
        mSandBoxServiceView.stopService();
        mSandBoxServiceView.killProgress();
        return false;
    }

    /**
     * 通过时间戳给出固定格式的照片名字
     *
     * @param time
     * @return
     */
    private static String getTime4Photo(long time) {
        String s;
        java.text.DateFormat format1 = new java.text.SimpleDateFormat("yyyyMMddhhmmss");
        s = format1.format(time);
        return s;
    }
}
