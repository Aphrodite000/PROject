package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public  static final String DATE_PATERN="yyyy-MM-dd HH:mm:ss";
    //文件大小转成带单位的字符串

    /**
     *
     * @param size  解析文件大小为中文描述
     * @return
     */
    public static String parseSize(long size) {
        String[] danweis={"B","KB","MB","GB","PB","TB"};
        int index=0;
        while (size>1024&&index<danweis.length-1){
            size/=1024;
            index++;
        }

        return size+danweis[index];
    }

    /**
     * 解析日期为中文日期描述
     * @param lastMooified
     * @return
     */
    public static String parseDate(Date lastMooified) {
        return new SimpleDateFormat(DATE_PATERN).format(lastMooified);
    }

    public static void main(String[] args) {
        System.out.println(Util.parseSize(100000));
        System.out.println(parseDate(new Date()));
    }
}
