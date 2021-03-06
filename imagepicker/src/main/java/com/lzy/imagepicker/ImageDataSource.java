package com.lzy.imagepicker;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.lzy.imagepicker.bean.ImageFolder;
import com.lzy.imagepicker.bean.ImageItem;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：加载手机图片实现类
 * 修订历史：
 * ================================================
 */
public class ImageDataSource implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ALL = 0;         //加载所有图片
    public static final int LOADER_CATEGORY = 1;    //分类加载图片

    public static final int LOADER_VIDEO_ALL = 2;
    public static final int LOADER_VIDEO_CATEGORY = 3;
    public static final int IMAGE = 1;
    public static final int VIDEO = 2;
    public static final int FILE = 3;
    private final String[] IMAGE_PROJECTION = {     //查询图片需要的数据列
            MediaStore.Images.Media.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
            MediaStore.Images.Media.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Images.Media.SIZE,           //图片的大小，long型  132492
            MediaStore.Images.Media.WIDTH,          //图片的宽度，int型  1920
            MediaStore.Images.Media.HEIGHT,         //图片的高度，int型  1080
            MediaStore.Images.Media.MIME_TYPE,      //图片的类型     image/jpeg
            MediaStore.Images.Media.DATE_ADDED};    //图片被添加的时间，long型  1450518608
    private final String[] VIDEO_PROJECTION = {//查询视频需要的数据列
            MediaStore.Video.Media.DISPLAY_NAME, //视频的显示名称  aaa.jpg
            MediaStore.Video.Media.DATA, //视频的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Video.Media.DURATION, //视频录制时长 ms, 11849
            MediaStore.Video.Media.MINI_THUMB_MAGIC, //视频缩略图的 id, 暂时不知道有啥用, 8111973006845647128
            MediaStore.Video.Media.RESOLUTION, //视频分辨率, 1920x1080
            MediaStore.Video.Media.SIZE, //视频的大小，long型  132492
            MediaStore.Video.Media.WIDTH, //视频的宽度，int型  0, 注意宽度和高度都是0, 需要在RESOLUTION字段中读取
            MediaStore.Video.Media.HEIGHT, //视频的高度，int型  0
            MediaStore.Video.Media.MIME_TYPE, //视频的类型     video/mp4
            MediaStore.Video.Media.DATE_ADDED, //视频被添加的时间，long型  1450518608
            MediaStore.Video.Media._ID   //视频id, 用来查询缩略图
    };
    int mLoaderType = IMAGE;
    private FragmentActivity activity;
    private OnImagesLoadedListener loadedListener;                     //图片加载完成的回调接口
    private ArrayList<ImageFolder> imageFolders = new ArrayList<>();   //所有的图片文件夹

    /**
     * @param activity       用于初始化LoaderManager，需要兼容到2.3
     * @param path           指定扫描的文件夹目录，可以为 null，表示扫描所有图片
     * @param loadedListener 图片加载完成的监听
     */
    public ImageDataSource(FragmentActivity activity, @LoaderType int loaderType, String path, OnImagesLoadedListener loadedListener) {
        this.activity = activity;
        this.loadedListener = loadedListener;
        this.mLoaderType = loaderType;

        LoaderManager loaderManager = activity.getSupportLoaderManager();
        if (loaderType == IMAGE) {
            if (path == null) {
                loaderManager.initLoader(LOADER_ALL, null, this);//加载所有的图片
            } else {
                //加载指定目录的图片
                Bundle bundle = new Bundle();
                bundle.putString("path", path);
                loaderManager.initLoader(LOADER_CATEGORY, bundle, this);
            }
        } else if (loaderType == VIDEO) {
            if (path == null) {
                loaderManager.initLoader(LOADER_VIDEO_ALL, null, this);//加载所有的图片
            } else {
                //加载指定目录的图片
                Bundle bundle = new Bundle();
                bundle.putString("path", path);
                loaderManager.initLoader(LOADER_VIDEO_CATEGORY, bundle, this);
            }
        }
    }

    public static String getLoaderTypeString(int loaderType) {
        String str;
        switch (loaderType) {
            case IMAGE:
                str = "Image";
                break;
            case VIDEO:
                str = "Video";
                break;
            case FILE:
                str = "File";
                break;
            default:
                str = "Unknown";
                break;
        }
        return str;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        //扫描所有图片
        if (id == LOADER_ALL)
            cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    null, null, IMAGE_PROJECTION[6] + " DESC");
        //扫描某个图片文件夹
        if (id == LOADER_CATEGORY)
            cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    IMAGE_PROJECTION[1] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[6] + " DESC");

        if (id == LOADER_VIDEO_ALL) {
            cursorLoader = new CursorLoader(activity, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION,
                    null, null, VIDEO_PROJECTION[9] + " DESC");//按照添加时间逆序
        }

        if (id == LOADER_VIDEO_CATEGORY) {
            cursorLoader = new CursorLoader(activity, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION,
                    VIDEO_PROJECTION[1] + " like '%" + args.getString("path") + "%'", null, VIDEO_PROJECTION[9] + " DESC");
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        imageFolders.clear();
        if (data != null) {
            ArrayList<ImageItem> allImages = new ArrayList<>();   //所有图片的集合,不分文件夹
            while (data.moveToNext()) {
                if (mLoaderType == IMAGE) {
                    loadImage(data, allImages);
                } else if (mLoaderType == VIDEO) {
                    loadVideo(data, allImages);
                }
            }
            //防止没有图片报异常
            if (data.getCount() > 0) {
                //构造所有图片的集合
                ImageFolder allImagesFolder = new ImageFolder();
                if (mLoaderType == IMAGE) {
                    allImagesFolder.name = activity.getResources().getString(R.string.all_images);
                } else if (mLoaderType == VIDEO) {
                    allImagesFolder.name = activity.getResources().getString(R.string.all_videos);
                } else {
                    allImagesFolder.name = "All";
                }
                allImagesFolder.path = "/";
                allImagesFolder.cover = allImages.get(0);
                allImagesFolder.images = allImages;
                imageFolders.add(0, allImagesFolder);  //确保第一条是所有图片
            }
        }

        //回调接口，通知图片数据准备完成
        ImagePicker.getInstance().setImageFolders(imageFolders);
        loadedListener.onImagesLoaded(imageFolders);
    }

    private void loadImage(Cursor data, ArrayList<ImageItem> allImages) {
        //查询数据
        String imageName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
        String imagePath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
        long imageSize = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
        int imageWidth = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
        int imageHeight = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
        String imageMimeType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
        long imageAddTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));

        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            return;
        }

        //封装实体
        ImageItem imageItem = new ImageItem(IMAGE);
        imageItem.name = imageName;
        imageItem.path = imagePath;
        imageItem.size = imageSize;
        imageItem.width = imageWidth;
        imageItem.height = imageHeight;
        imageItem.mimeType = imageMimeType;
        imageItem.addTime = imageAddTime;
        allImages.add(imageItem);
        //根据父路径分类存放图片
        File imageParentFile = imageFile.getParentFile();
        ImageFolder imageFolder = new ImageFolder();
        imageFolder.name = imageParentFile.getName();
        imageFolder.path = imageParentFile.getAbsolutePath();

        if (!imageFolders.contains(imageFolder)) {
            ArrayList<ImageItem> images = new ArrayList<>();
            images.add(imageItem);
            imageFolder.cover = imageItem;
            imageFolder.images = images;
            imageFolders.add(imageFolder);
        } else {
            imageFolders.get(imageFolders.indexOf(imageFolder)).images.add(imageItem);
        }
    }

    private void loadVideo(Cursor data, ArrayList<ImageItem> allImages) {
        //查询数据
        String imageName = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
        String imagePath = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[1]));

        long videoDuration = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[2]));
        //String thumbId = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[3])); //缩略图id
        String resolution = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[4])); //分辨率

        long imageSize = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[5]));
        int imageWidth = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[6]));
        int imageHeight = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[7]));
        String imageMimeType = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[8]));
        long imageAddTime = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[9]));

        String videoId = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[10]));
        String thumbPath = getVideoThumb(activity.getContentResolver(), videoId);

        if (videoDuration < 3 * 1000) {
            //小于3秒的视频过滤
            return;
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            return;
        }

        //封装实体
        ImageItem imageItem = new ImageItem(VIDEO);
        imageItem.name = imageName;
        imageItem.path = imagePath;
        imageItem.size = imageSize;
        imageItem.width = imageWidth;
        imageItem.height = imageHeight;
        imageItem.mimeType = imageMimeType;
        imageItem.addTime = imageAddTime;
        imageItem.videoDuration = videoDuration;
        imageItem.videoThumbPath = thumbPath;
        imageItem.resolution = resolution;

        try {
            String[] split = resolution.split("x");
            imageItem.width = Integer.parseInt(split[1]);
            imageItem.height = Integer.parseInt(split[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        allImages.add(imageItem);
        //根据父路径分类存放图片
        File imageParentFile = imageFile.getParentFile();
        ImageFolder imageFolder = new ImageFolder();
        imageFolder.name = imageParentFile.getName();
        imageFolder.path = imageParentFile.getAbsolutePath();

        if (!imageFolders.contains(imageFolder)) {
            ArrayList<ImageItem> images = new ArrayList<>();
            images.add(imageItem);
            imageFolder.cover = imageItem;
            imageFolder.images = images;
            imageFolders.add(imageFolder);
        } else {
            imageFolders.get(imageFolders.indexOf(imageFolder)).images.add(imageItem);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        System.out.println("--------");
    }

    /**
     * 获取视频缩略图
     */
    private String getVideoThumb(ContentResolver contentResolver, String videoId) {
        String path = "";
        Cursor dataCursor = contentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Thumbnails.DATA}, MediaStore.Video.Thumbnails.VIDEO_ID + "=?",
                new String[]{videoId}, null);
        if (dataCursor != null) {
            dataCursor.moveToFirst();
            try {
                path = dataCursor.getString(dataCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataCursor.close();
        }
        return path;
    }

    @IntDef({IMAGE, VIDEO, FILE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoaderType {
    }

    /**
     * 所有图片加载完成的回调接口
     */
    public interface OnImagesLoadedListener {
        void onImagesLoaded(List<ImageFolder> imageFolders);
    }
}
