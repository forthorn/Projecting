package com.forthorn.projecting.downloader;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.forthorn.projecting.app.AppConstant;
import com.forthorn.projecting.db.DBUtils;
import com.forthorn.projecting.entity.Download;
import com.forthorn.projecting.entity.Task;
import com.forthorn.projecting.util.LogUtils;
import com.forthorn.projecting.util.MD5Util;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;

/**
 * Created by: Forthorn
 * Date: 11/5/2017.
 * Description:
 */

public class Downloader {

    private static Downloader sDownloader;
    private String mCurrentFilePath;

    private static class InstanceHolder {
        private static final Downloader DOWNLOADER = new Downloader();
    }

    public static Downloader getInstance() {
        return InstanceHolder.DOWNLOADER;
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }

    public void setCurrentFilePath(String currentFilePath) {
        mCurrentFilePath = currentFilePath;
    }

    public void download(Task task) {
        long time = task.getStart_time() * 1000L - System.currentTimeMillis();
        if (time < 5 * 60 * 1000L) {
            LogUtils.e("CancelD", "播放时间距离现在小于5分钟，不进行下载");
            return;
        }
        String dir = checkDiskSpaceAndDir();
        Download download = new Download();
        String fileName = MD5Util.getMD5Str(task.getContent());
        download.setId(task.getId());
        download.setStatus(AppConstant.DOWNLOAD_STATUS_READY);
        download.setTime((int) (System.currentTimeMillis() / 1000L));
        download.setUrl(task.getContent());
        download.setTaskId(task.getId());
        if (DBUtils.getInstance().findDownload(download.getId()) == null) {
            DBUtils.getInstance().insertDownload(download);
        }
        if (FileDownloader.getImpl().isServiceConnected()) {
            FileDownloader.getImpl().setMaxNetworkThreadCount(1);
        }
        FileDownloader.getImpl().create(task.getContent())
                .setPath(dir + File.separator + fileName, false)
                .setTag(new Integer(task.getId()))
                .setListener(new FileDownloadSampleListener() {
                    @Override
                    protected void completed(BaseDownloadTask task) {
                        super.completed(task);
                        Integer id = (Integer) task.getTag();
                        Download download = DBUtils.getInstance().findDownload(id);
                        if (download == null) {
                            new File(task.getPath()).delete();
                            return;
                        }
                        download.setPath(task.getPath());
                        download.setFileSize(task.getSmallFileTotalBytes());
                        download.setStatus(AppConstant.DOWNLOAD_STATUS_COMPLETE);
                        DBUtils.getInstance().updateDownload(download);
                        LogUtils.e("下载完成：", download.toString());
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                        Integer id = (Integer) task.getTag();
                        Download download = DBUtils.getInstance().findDownload(id);
                        if (download == null) {
                            return;
                        }
                        download.setStatus(AppConstant.DOWNLOAD_STATUS_DOWNLOADING);
                        DBUtils.getInstance().updateDownload(download);
                        LogUtils.e("下载开始：", download.toString());
                    }
                }).start();
        LogUtils.e("Downloader", "start");
    }


    /**
     * 检查磁盘空间大小，大于2048M则先删除文件再下载
     */
    private String checkDiskSpaceAndDir() {
        File sdcardDir = null;
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        } else {
            sdcardDir = Environment.getDownloadCacheDirectory();
        }
        File dir = new File(sdcardDir, "Projecting");
        if (!dir.exists()) {
            dir.mkdir();
        }
        while (getFolderSize(dir) > 2048) {
            Download download = DBUtils.getInstance().findEarliestDownload();
            if (download != null) {
                if (!TextUtils.isEmpty(download.getPath())) {
                    File earliestFile = new File(download.getPath());
                    if (earliestFile.exists()) {
                        earliestFile.delete();
                    }
                }
                DBUtils.getInstance().deleteDownload(download);
            } else {
                File[] files = dir.listFiles();
                if (files.length == 0) {
                    break;
                }
                int index = 0;
                long earliestLastModify = files[0].lastModified();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].lastModified() < earliestLastModify) {
                        index = i;
                        earliestLastModify = files[i].lastModified();
                    }
                }
                try {
                    if (files[index].exists() && files[index].isFile()) {
                        if (getCurrentFilePath() != null || !files[index].getPath().equals(getCurrentFilePath())) {
                            files[index].delete();
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return dir.toString();
    }


    public void pause() {

    }


    public long getFolderSize(File file) {
        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtils.e("当前缓存大小：", size / 1024 / 1024 + "M");
        return size / 1024L / 1024L;
    }
}
