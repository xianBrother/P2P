package com.rdc.p2p.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.content.MimeTypeFilter;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.rdc.p2p.activity.ChatDetailActivity;
import com.rdc.p2p.app.App;
public class SDUtil {

    private static final String TAG = "SDUtil";
    public static String saveBitmap(Bitmap bm, String name,String type) {
        try {
            File file = getMyAppFile(name,type);
            assert file != null;
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();    }
        return null;
    }


    /**
     * 获取本App缓存目录下的文件对象
     * @param fileName 文件名 e.g: sun
     * @param fileType 文件类型 e.g: .jpg
     * @return
     */
    public static File getMyAppFile(String fileName, String fileType){
        try {
            File dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/P2P");
            File file;
            if (dirFile.exists()){
                if (!dirFile.isDirectory()){
                    dirFile.delete();
                    dirFile.mkdirs();
                }
            }else {
                dirFile.mkdirs();
            }
            while (true){
                file = new File(dirFile,fileName+fileType);
                if (file.exists()){
                    fileName = fileName+"&";
                }else {
                    file.createNewFile();
                    break;
                }
            }
            return file;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }






    public static String getFilePathByUri(final Context context, final Uri uri) {
        if (Build.VERSION.SDK_INT >= 19){
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                // DownloadsProvider
                else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
            return null;
        }else {
            return getDataColumn(context,uri,null,null);
        }
    }

    /**
     * 通过ContentProvider文件的绝对路径
     * @param context
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * 根据文件本地路径获取文件类型
     * @param filePath
     * @return
     */
    public static String getMimeTypeFromFilePath(String filePath) {
        String type = null;
        String fName = new File(filePath).getName();
        String fileExtension = getFileExtension(fName);
        if (fileExtension != null){
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        }
        return type;
    }

    /**
     * 获取文件后缀名(不包含 . )
     * @param fileName
     * @return String fileExtension or null
     */
    public static String getFileExtension(String fileName){
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            //获取文件的后缀名
            return fileName.substring(dotIndex+1, fileName.length()).toLowerCase(Locale.getDefault());
        }
        return null;
    }




}

