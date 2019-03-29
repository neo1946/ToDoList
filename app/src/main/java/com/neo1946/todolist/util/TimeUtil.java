package com.neo1946.todolist.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ouyangzhaoxian on 2019/03/11
 * 时间格式化
 */
public class TimeUtil {

    public static String FORMAT_YMD_CN = "yyyy年MM月dd日";
    public static String FORMAT_HMS_CN = "HH时mm分ss秒";

    private static SimpleDateFormat FORMAT_WITH_TODAY = new SimpleDateFormat("今天 HH:mm");
    private static SimpleDateFormat FORMAT_WITH_YESTERDAY = new SimpleDateFormat("昨天 HH:mm");
    private static SimpleDateFormat FORMAT_WITH_DAY_BEFORE_YESTERDAY = new SimpleDateFormat("前天 HH:mm");
    private static SimpleDateFormat FORMAT_WITH_THIS_YEAR = new SimpleDateFormat("MM/dd HH:mm");
    private static SimpleDateFormat FORMAT_WITH_NORMAL = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public static String getTimeFormat(long time){
        Date date = new Date(time);
        return getTimeFormat(date);
    }

    public static String getTimeFormat(Date date) {
        SimpleDateFormat format = FORMAT_WITH_NORMAL;
        Date now = new Date();
        if(date.getYear() == now.getYear()){
            //今年的
            if(date.getMonth() == now.getMonth()){
                //这个月的
                switch (now.getDay() - date.getDay()) {
                    case 0://今天
                        format = FORMAT_WITH_TODAY;
                        break;
                    case 1://昨天
                        format = FORMAT_WITH_YESTERDAY;
                        break;
                    case 2://前天
                        format = FORMAT_WITH_DAY_BEFORE_YESTERDAY;
                        break;
                    default:
                        format = FORMAT_WITH_THIS_YEAR;
                        break;
                }
            }else{
                format = FORMAT_WITH_THIS_YEAR;
            }
        }
        String strDate = "";
        strDate = format.format(date);
        return strDate;
    }

    public static String long2Str(long mseconds, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date(mseconds);
        return sdf.format(date);
    }
}
