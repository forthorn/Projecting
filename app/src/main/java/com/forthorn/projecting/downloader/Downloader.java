package com.forthorn.projecting.downloader;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.forthorn.projecting.app.AppApplication;
import com.forthorn.projecting.app.AppConstant;
import com.forthorn.projecting.db.DBUtils;
import com.forthorn.projecting.entity.Download;
import com.forthorn.projecting.entity.Task;
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


    private static class InstanceHolder {
        private static final Downloader DOWNLOADER = new Downloader();
    }

    public static Downloader getInstance() {
        return InstanceHolder.DOWNLOADER;
    }

    public void download(Task task) {
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
                        Log.e("下载完成：", download.toString());
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
                        Log.e("下载开始：", download.toString());
                    }
                }).start();
        Log.e("Downloader", "start");
    }


    /**
     * 检查磁盘空间大小，大于700M则先删除文件再下载
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
        while (getFolderSize(dir) > 700 * 1024 * 1024) {
            Download download = DBUtils.getInstance().findEarliestDownload();
            File earliestFile = new File(download.getPath());
            if (earliestFile.exists()) {
                earliestFile.delete();
            }
            DBUtils.getInstance().deleteDownload(download);
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
        Log.e("当前缓存大小：", size / 1024 / 1024 + "M");
        return size;
    }
}
