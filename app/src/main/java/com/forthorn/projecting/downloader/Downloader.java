package com.forthorn.projecting.downloader;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.forthorn.projecting.BuildConfig;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by: Forthorn
 * Date: 11/5/2017.
 * Description:
 */

public class Downloader {

    private String mCurrentFilePath;
    private static final int AVAILABLE_SIZE = 1024;

    private static class InstanceHolder {
        private static final Downloader DOWNLOADER = new Downloader();
    }

    public static Downloader getInstance() {
        return InstanceHolder.DOWNLOADER;
    }

    private String getCurrentFilePath() {
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
        final String dir = checkDiskSpaceAndDir();
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
        LogUtils.e("下载开始：", download.toString());
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
                        if (BuildConfig.DEBUG) {
//                            Toast.makeText(AppApplication.getApplication(),
//                                    "下载完成\n下载任务Id:" + download.getId() + "\n下载链接:" + download.getUrl(), Toast.LENGTH_SHORT).show();
                        }
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
                        if (BuildConfig.DEBUG) {
//                            Toast.makeText(AppApplication.getApplication(),
//                                    "尝试下载开始\n下载任务Id:" + download.getId() + "\n下载链接:" + download.getUrl(), Toast.LENGTH_SHORT).show();
                        }
                        LogUtils.e("下载开始：", download.toString());
                    }
                }).start();
        LogUtils.e("Downloader", "start");
    }


    /**
     * 检查磁盘空间大小，可用空间不足1024M则先删除文件再下载
     * TODO   清除一个月前的日志文件
     */
    private String checkDiskSpaceAndDir() {
        File sdcardDir;
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
        //如果可用空间大于AVAILABLE_SIZE，直接返回目录
        long availableSize = getAvailableSize(sdcardDir);
        if (availableSize > AVAILABLE_SIZE) {
            LogUtils.e("Downloader", "剩余空间:" + availableSize + "MB , 空间充足");
            return dir.toString();
        }
        LogUtils.e("Downloader", "剩余空间:" + availableSize + "MB , 空间不足, 需要删除文件");
        //否则需要开始删除文件
        final File[] files = dir.listFiles();
        if (files.length == 0) {
            return dir.toString();
        }
        //对已有文件进行排序，根据最新修改时间进行排序
        List<File> list = new ArrayList<>(Arrays.asList(files));
//        StringBuilder stringBuilder = new StringBuilder();
//        for (File file : list) {
//            stringBuilder.append("[ 文件名：" + file.getName() + ",修改时间：" + file.lastModified() + ", 绝对路径:" + file.getAbsolutePath() + " ]");
//        }
//        Log.e("Downloader", "排序前列表：" + stringBuilder.toString());
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return (o1.lastModified() < o2.lastModified()) ? -1 : ((o1.lastModified() == o2.lastModified()) ? 0 : 1);
            }
        });
//        stringBuilder = new StringBuilder();
//        for (File file : list) {
//            stringBuilder.append("[ 文件名：" + file.getName() + ",修改时间：" + file.lastModified() + ", 绝对路径:" + file.getAbsolutePath() + " ]");
//        }
//        Log.e("Downloader", "排序后列表：" + stringBuilder.toString());
        //根据排序好的顺序进行处理
        //如果文件没在数据库中找到，先删除
        for (File file : list) {
            if (getAvailableSize(sdcardDir) < AVAILABLE_SIZE) {
                if (!DBUtils.getInstance().findDownload(file.getAbsolutePath())) {
                    //当前播放的地址不等于文件地址就可以进行删除
                    if (!file.getAbsolutePath().equals(getCurrentFilePath()) && !file.getName().startsWith("log_")) {
                        LogUtils.e("Downloader", "空间不足，删除无效文件：" + file.getAbsolutePath());
                        file.delete();
                    }
                }
            } else {
                break;
            }
        }
        //如果大小还不足，开始删除数据库中存在的文件，找到最早下载的文件进行删除
        //不使用while循环，删除一个就好
        while (getAvailableSize(sdcardDir) < AVAILABLE_SIZE) {
            Download download = DBUtils.getInstance().findEarliestDownload();
            if (download != null) {
                if (!TextUtils.isEmpty(download.getPath())) {
                    File earliestFile = new File(download.getPath());
                    if (earliestFile.exists()) {
                        LogUtils.e("Downloader", "空间不足，删除最早的缓存文件：" + earliestFile.getAbsolutePath());
                        earliestFile.delete();
                    }
                }
                DBUtils.getInstance().deleteDownload(download);
            }
        }
        return dir.toString();
    }


    public void pause() {

    }


    /**
     * 获取可用空间大小
     *
     * @param sdcardDir
     * @return
     */
    private long getAvailableSize(File sdcardDir) {
        StatFs sf = new StatFs(sdcardDir.getPath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();
        long totalSize = blockSize * blockCount / 1024;
        long availableSize = availCount * blockSize / 1024;
//        LogUtils.e("Downloader", "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:" + blockSize * blockCount / 1024 + "KB");
//        LogUtils.e("Downloader", "可用的block数目：:" + availCount + ",剩余空间:" + availCount * blockSize / 1024 + "KB");
        return availableSize / 1024;
    }

    public long getFolderSize(File file) {
        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (File file1 : fileList) {
                if (file1.isDirectory()) {
                    size = size + getFolderSize(file1);
                } else {
                    size = size + file1.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtils.e("当前缓存大小：", size / 1024 / 1024 + "M");
        return size / 1024L / 1024L;
    }
}
