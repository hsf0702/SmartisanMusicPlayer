package com.yibao.music.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;


/**
 * 描述：${文件操作工具类}
 * 邮箱：strangermy@outlook.com
 *
 * @author Luoshipeng
 */

public class FileUtil {


    public static boolean getFavoriteFile() {
        File file = new File(Constants.FAVORITE_FILE);
        return file.exists();
    }

    public static File getLyricsFile(String songName, String songArtisa) {
        File file = new File(Constants.MUSIC_LYRICS_ROOT);
        if (!file.exists()) {
            file.mkdirs();
        }
        File lyricFile = new File(file + "/", songName + "$$" + songArtisa + ".lrc");
        if (!lyricFile.exists()) {
            try {
                lyricFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lyricFile;
    }


    public static long getId(String str) {
        //        String str="2017-06-12T10:22:59.890Z";
        return Long.parseLong(str.substring(11, 19)
                .replaceAll(":", ""));
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        // 有存储的SDCard
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public static File getHeaderFile() {
        File file = new File(Constants.HEADER_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(file, Constants.CROP_IMAGE_FILE_NAME);
    }

    public static Uri getPicUri(Context context, String savePath) {
        File file = new File(savePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File pictureFile = new File(savePath, Constants.IMAGE_FILE_NAME);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? FileProvider.getUriForFile(context, context.getPackageName(), pictureFile) : Uri.fromFile(pictureFile);
    }

    public static void deleteFile(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFile(file);
                } else {
                    file.delete();
                }
            }
            dir.delete();
        }
    }
}