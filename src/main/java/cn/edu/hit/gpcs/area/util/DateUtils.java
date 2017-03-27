package cn.edu.hit.gpcs.area.util;

import com.google.common.base.Joiner;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 日期工具类
 */
public class DateUtils {

    /**
     * 生成SQL日期对象
     * @param year 年
     * @param month 月
     * @param day 日
     */
    public static Date getDate (int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month - 1, day);
        return new Date(date.getTimeInMillis());
    }

    /**
     * 将字符串转换成Date对象
     * @param string 日期字符串
     */
    public static Date parseDate (String string) {
        Date result = null;
        DateFormat dateFormat = new SimpleDateFormat("y-M-d");
        try {
            java.util.Date dateObject = dateFormat.parse(string);
            result = new Date(dateObject.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将字符串转换成Time对象
     * @param string 时间字符串
     */
    public static Date parseTime (String string) {
        Date result = null;
        DateFormat dateFormat = new SimpleDateFormat("H:m:s");
        try {
            java.util.Date dateObject = dateFormat.parse(string);
            result = new Date(dateObject.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 时间段转化为字符串
     * @param time 微秒数
     */
    public static String periodToString (long time) {
        long seconds = time / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes = (seconds - hours * 60 * 60) / 60;
        seconds = seconds - hours * 60 * 60 - minutes * 60;
        List<String> slices = new ArrayList<>();
        if (hours > 0) slices.add(hours + (hours > 1 ? "hours" : "hour"));
        if (minutes > 0) slices.add(minutes + (minutes > 1 ? "minutes" : "minute"));
        if (seconds > 0) slices.add(seconds + (seconds > 1 ? "seconds" : "second"));
        return Joiner.on(" ").join(slices);
    }

    /**
     * @return 今日日期对象
     */
    public static Date getToday () {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        return new Date(todayCalendar.getTime().getTime());
    }
}
