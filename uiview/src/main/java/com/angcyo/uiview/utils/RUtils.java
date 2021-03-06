package com.angcyo.uiview.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.github.utilcode.utils.PhoneUtils;
import com.angcyo.uiview.widget.RExTextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import static com.angcyo.uiview.utils.RUtils.ImageType.BMP;
import static com.angcyo.uiview.utils.RUtils.ImageType.GIF;
import static com.angcyo.uiview.utils.RUtils.ImageType.JPEG;
import static com.angcyo.uiview.utils.RUtils.ImageType.PNG;
import static com.angcyo.uiview.utils.RUtils.ImageType.UNKNOWN;

/**
 * Created by angcyo on 15-12-16 016 15:41 下午.
 */
public class RUtils {


    private static final String[][] MIME_MapTable = {
            //{后缀名，MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

    /**
     * 跳至拨号界面
     *
     * @param phoneNumber 电话号码
     */
    public static void callTo(String phoneNumber) {
        PhoneUtils.dial(phoneNumber);
    }

    public static void emailTo(Activity activity, String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
//        intent.putExtra(Intent.EXTRA_CC, new String[]
//                {"1032694760@qq.com"});
        intent.putExtra(Intent.EXTRA_EMAIL, "");
        intent.putExtra(Intent.EXTRA_TEXT, "欢迎提供意您的见或建议");
        activity.startActivity(Intent.createChooser(intent, "选择邮件客户端"));
    }


    /**
     * 去除字符串左右的字符
     *
     * @param des the des
     * @return the string
     */
    public static String trimMarks(String des) {
        return trimMarks(des, 1);
    }

    /**
     * 去除字符串左右指定个数的字符
     *
     * @param des   the des
     * @param count the count
     * @return the string
     */
    public static String trimMarks(String des, int count) {
        if (des == null || count < 0 || des.length() < count + 1) {
            return des;
        }
        return des.substring(count, des.length() - count);
    }

    /**
     * 返回现在的时间,不包含日期
     *
     * @return the now time
     */
    public static String getNowTime() {
        return getNowTime("HH:mm:ss");
    }

    /**
     * Gets now time.
     *
     * @param pattern the patternUrl
     * @return the now time
     */
    public static String getNowTime(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(new Date());
    }

    /**
     * 判断字符串是否为空
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.trim().length() < 1);
    }

    /**
     * Gets time.
     *
     * @return 按照HH :mm:ss 返回时间
     */
    public static String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * Gets date.
     *
     * @return 按照yyyy -MM-dd 格式返回日期
     */
    public static String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date());
    }

    /**
     * Gets date and time.
     *
     * @return 按照yyyy -MM-dd HH:mm:ss 的格式返回日期和时间
     */
    public static String getDateAndTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * Gets date.
     *
     * @param pattern 格式
     * @return 返回日期 date
     */
    public static String getDate(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

    /**
     * Gets time.
     *
     * @param pattern 格式
     * @return 按照指定格式返回时间 time
     */
    public static String getTime(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

//

    /**
     * Gets date and time.
     *
     * @param pattern 指定的格式
     * @return 按照指定格式返回日期和时间 date and time
     */
    public static String getDateAndTime(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

    /**
     * @param context 上下文
     * @return 返回手机号码 tel number
     */
    public static String getTelNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }

    /**
     * Gets os version.
     *
     * @return 获取device的os version
     */
    public static String getOsVersion() {
        String string = Build.VERSION.RELEASE;
        return string;
    }

    /**
     * Gets os sdk.
     *
     * @return 返回设备sdk版本 os sdk
     */
    public static String getOsSdk() {
        int sdk = Build.VERSION.SDK_INT;
        return String.valueOf(sdk);
    }

    /**
     * Gets random.
     *
     * @return the random
     */
    public static int getRandom() {
        Random random = new Random();
        return random.nextInt();
    }

    /**
     * 获取随机数
     *
     * @param n 最大范围
     * @return random
     */
    public static int getRandom(int n) {
        Random random = new Random();
        return random.nextInt(n);
    }

    /**
     * 获取字符数组中随机的字符串
     *
     * @param context the context
     * @param resId   the res id
     * @return random string
     */
    public static String getRandomString(Context context, int resId) {
        String[] strings;
        strings = context.getResources().getStringArray(resId);

        return strings[getRandom(strings.length)];
    }

    /**
     * 从资源id获取字符串
     *
     * @param context 上下文
     * @param id      资源id
     * @return 字符串 string for res
     */
    public static String getStringForRes(Context context, int id) {
        if (context == null) {
            return "";
        }
        return context.getResources().getString(id);
    }

    /**
     * 返回app的版本名称.
     *
     * @param context the context
     * @return app version name
     */
    public static String getAppVersionName(Context context) {
        String version = "unknown";
// 获取package manager的实例
        PackageManager packageManager = context.getPackageManager();
// getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
// Log.i("版本名称:", version);
        return version;
    }

    /**
     * 返回app的版本代码.
     *
     * @param context the context
     * @return app version code
     */
    public static int getAppVersionCode(Context context) {
// 获取package manager的实例
        PackageManager packageManager = context.getPackageManager();
// getPackageName()是你当前类的包名，0代表是获取版本信息
        int code = 1;
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
            code = packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
// Log.i("版本代码:", version);
        return code;
    }

    /**
     * 获取屏幕的宽度高度
     *
     * @param context the context
     * @param size    the size
     * @return the display
     */
    public static DisplayMetrics getDisplay(Context context, Point size) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(size);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

    /**
     * 返回调用行的方法 所在行号
     *
     * @return the string
     */
    public static String callMethodAndLine() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getClassName() + ".");
        result.append(thisMethodStack.getMethodName());
        result.append("(" + thisMethodStack.getFileName());
        result.append(":" + thisMethodStack.getLineNumber() + ")");
        return result.toString();
    }

    /**
     * 返回调用行的类名
     *
     * @return the string
     */
    public static String callClassName() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getClassName());
        return result.toString();
    }

    /**
     * 返回调用行的方法名
     *
     * @return the string
     */
    public static String callMethodName() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getMethodName());
        return result.toString();
    }

    /**
     * 返回调用行的 类名 和 方法名
     *
     * @return the string
     */
    public static String callClassMethodName() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getClassName() + ".");
        result.append(thisMethodStack.getMethodName());
        return result.toString();
    }

    /**
     * 打开网页,调用系统应用
     *
     * @param context the context
     * @param url     the url
     */
    public static void openUrl(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (!url.toLowerCase().startsWith("http:") && !url.toLowerCase().startsWith("https:")) {
            url = "http:".concat(url);
        }

        Uri webPage = Uri.parse(url);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(webIntent);
    }

    /**
     * 打开文件
     *
     * @param file
     */
    public static void openFile(Context context, File file) {

        if (file == null || !file.exists()) {
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        String type = getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(/*uri*/getFileUri(context, file), type);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //跳转
        try {
            context.startActivity(intent);     //这里最好try一下，有可能会报错。 //比如说你的MIME类型是打开邮箱，但是你手机里面没装邮箱客户端，就会报错。
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     *
     * @param file
     */
    private static String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
    /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) { //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    /**
     * 跳转到应用市场
     */
    public static void jumpToMarket(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void sendSMS(Activity ctx, String message, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("sms_body", message);
        ctx.startActivity(intent);
    }


    /**
     * 使用,分割string, 并返回一个列表
     */
    public static List<String> split(String string) {
        return split(string, ",");
    }

    public static List<String> split(String string, String regex) {
        final ArrayList<String> list = new ArrayList<>();
        if (!"null".equalsIgnoreCase(string) && !TextUtils.isEmpty(string)) {
            final String[] split = string.split(regex);
            for (String s : split) {
                if (!TextUtils.isEmpty(s)) {
                    list.add(s);
                }
            }
        }
        return list;
    }

    /**
     * 安全的去掉字符串的最后一个字符
     */
    public static String safe(StringBuilder stringBuilder) {
        return stringBuilder.substring(0, Math.max(0, stringBuilder.length() - 1));
    }

    public static <T> String connect(List<T> list) {
        if (list == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (T bean : list) {
            builder.append(bean.toString());
            builder.append(",");
        }

        return safe(builder);
    }

    /**
     * 组装参数
     */
    public static Map<String, String> map(String... args) {
        final Map<String, String> map = new HashMap<>();
        foreach(new OnPutValue() {
            @Override
            public void onValue(String key, String value) {
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    map.put(key, value);
                }
            }
        }, args);
        return map;
    }

    private static void foreach(OnPutValue onPutValue, String... args) {
        if (onPutValue == null || args == null) {
            return;
        }
        for (String str : args) {
            String[] split = str.split(":");
            if (split.length >= 2) {
                String first = split[0];
                onPutValue.onValue(first, str.substring(first.length() + 1));
            }
        }
    }

    /**
     * 填充两个字段相同的数据对象
     */
    public static void fill(Object from, Object to) {
        Field[] fromFields = from.getClass().getDeclaredFields();
        Field[] toFields = to.getClass().getDeclaredFields();
        for (Field f : fromFields) {
            String name = f.getName();
            for (Field t : toFields) {
                String tName = t.getName();
                if (name.equalsIgnoreCase(tName)) {
                    try {
                        f.setAccessible(true);
                        t.setAccessible(true);
                        t.set(to, f.get(from));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }
    }

    /**
     * 判断是否是网址
     */
    public static boolean isHttpUrl(String url) {
//        return RegexUtils.isMatch(
//                "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?",
//                url);
        Matcher matcher = RExTextView.patternUrl.matcher(url);
        return matcher.find();
    }

    public static boolean isLast(List data, int position) {
        if (data == null || data.isEmpty()) {
            return true;
        }
        return data.size() - 1 == position;
    }

    public static String getLang() {
        Resources resources = RApplication.getApp().getResources();
        Configuration config = resources.getConfiguration();
        // 应用用户选择语言
        String language = config.locale.getLanguage();
        return language;
    }

    public static void changeLang(Locale locale) {
        Resources resources = RApplication.getApp().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, dm);
    }

    /**
     * 缩短很大的数字
     */
    public static String getShortString(long number) {
        return getShortString(String.valueOf(number), "");
    }

    public static String getShortString(String number) {
        return getShortString(number, "");
    }

    public static String getShortString(String number, String suffix) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }

        String unit;
        String num;
        if (number.length() > 7) {
            unit = "千万";
            num = number.substring(0, number.length() - 7);
        } else if (number.length() > 6) {
            unit = "百万";
            num = number.substring(0, number.length() - 6);
        } else if (number.length() > 5) {
            unit = "十万";
            num = number.substring(0, number.length() - 5);
        } else if (number.length() > 4) {
            unit = "万";
            num = number.substring(0, number.length() - 4);
        } else {
            unit = "";
            num = number;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(num);
        builder.append(unit);
        builder.append(suffix);
        return builder.toString();
    }

    /**
     * 该方法是调用了系统的下载管理器
     */
    public static long downLoadFile(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return -1;
        }
        /**
         * 在这里返回的 reference 变量是系统为当前的下载请求分配的一个唯一的ID，
         * 我们可以通过这个ID重新获得这个下载任务，进行一些自己想要进行的操作
         * 或者查询下载的状态以及取消下载等等
         */
        Uri uri = Uri.parse(url);        //下载连接
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);  //得到系统的下载管理
        DownloadManager.Request request = new DownloadManager.Request(uri);  //得到连接请求对象
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);   //指定在什么网络下进行下载，这里我指定了WIFI网络

        int indexOf = url.lastIndexOf('/');
        String fileName = "temp";
        if (indexOf != -1) {
            fileName = url.substring(indexOf + 1);
        }

        request.setDestinationInExternalPublicDir(context.getPackageName() + "/download", fileName);  //制定下载文件的保存路径，我这里保存到根目录
        request.setVisibleInDownloadsUi(true);  //设置是否显示在系统的下载界面
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);//下载的时候, 完成的时候都显示通知
        request.allowScanningByMediaScanner();  //表示允许MediaScanner扫描到这个文件，默认不允许。

        request.setTitle(fileName);      //设置下载中通知栏的提示消息, 并且也是系统下载界面的显示名
        request.setDescription(fileName + "文件正在下载...");//设置设置下载中通知栏提示的介绍, 部分机型无效

        long downLoadId = manager.enqueue(request);   //启动下载,该方法返回系统为当前下载请求分配的一个唯一的ID

        return downLoadId;
    }

    /**
     * 删除特殊字符
     */
    public static String fixName(String name) {
        String result = "-";
        if (TextUtils.isEmpty(name)) {
            return result;
        }
        String all = name.replaceAll("\\+", "").replaceAll("=", "").replaceAll("/", "").replaceAll("&", "");
        if (TextUtils.isEmpty(all)) {
            return result;
        }
        return all;
    }

    /**
     * 打印Map
     */
    public static void logMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            L.e("map is null or empty. ");
            return;
        }

        StringBuilder builder = new StringBuilder("\n");
        for (Map.Entry<String, String> p : map.entrySet()) {
            String key = p.getKey();
            String value = p.getValue();
            builder.append(key);
            builder.append(":");
            builder.append(value);
            builder.append("\n");
        }
        L.e(builder.toString());
    }

    public static Uri getFileUri(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName(), file);
        } else {
            uri = Uri.fromFile(file);
        }
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return uri;
    }

    /**
     * 判断是否是主线程
     */
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static ImageType getImageType(File file) {
        if (file == null) return UNKNOWN;
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return getImageType(is);
        } catch (IOException e) {
            e.printStackTrace();
            return UNKNOWN;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ImageType getImageType(InputStream is) {
        if (is == null) return UNKNOWN;
        try {
            byte[] bytes = new byte[8];
            return is.read(bytes, 0, 8) != -1 ? getImageType(bytes) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return UNKNOWN;
        }
    }

    private static ImageType getImageType(byte[] bytes) {
        if (isJPEG(bytes)) return JPEG;
        if (isGIF(bytes)) return GIF;
        if (isPNG(bytes)) return PNG;
        if (isBMP(bytes)) return BMP;
        return ImageType.UNKNOWN;
    }

    private static boolean isJPEG(byte[] b) {
        return b.length >= 2
                && (b[0] == (byte) 0xFF) && (b[1] == (byte) 0xD8);
    }

    private static boolean isGIF(byte[] b) {
        return b.length >= 6
                && b[0] == 'G' && b[1] == 'I'
                && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private static boolean isPNG(byte[] b) {
        return b.length >= 8
                && (b[0] == (byte) 137 && b[1] == (byte) 80
                && b[2] == (byte) 78 && b[3] == (byte) 71
                && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    private static boolean isBMP(byte[] b) {
        return b.length >= 2
                && (b[0] == 0x42) && (b[1] == 0x4d);
    }

    public enum ImageType {
        JPEG, GIF, PNG, BMP, UNKNOWN
    }

    interface OnPutValue {
        void onValue(String key, String value);
    }
}
