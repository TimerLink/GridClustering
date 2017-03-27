package cn.edu.hit.gpcs.area;

import cn.edu.hit.gpcs.area.thread.ScheduledTask;
import cn.edu.hit.gpcs.area.util.ConsoleUtils;
import cn.edu.hit.gpcs.area.util.ConsoleUtils.Color;
import cn.edu.hit.gpcs.area.util.DateUtils;
import cn.edu.hit.gpcs.utils.DotEnv;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

/**
 * 任务计划管理
 */
public class ScheduleManager {
    /**
     * 时间间隔（一天）
     */
    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;

    private static Date nextDate;

    /**
     * 运行每日任务计划
     */
    public static void run () {
        Calendar time = Calendar.getInstance();
        time.setTime(DateUtils.parseTime(DotEnv.get("DAILY_JOB_START")));
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        start.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
        start.set(Calendar.SECOND, time.get(Calendar.SECOND));
        nextDate = start.getTime();
        nextDate = getNextDate();
        Timer timer = new Timer();
        ScheduledTask task = new ScheduledTask();
        timer.schedule(task, nextDate, PERIOD_DAY);
        ConsoleUtils.print(Color.ANSI_CYAN,
                String.format("Daily calculation job scheduled.\nNext job starts at %s.\n", nextDate));
    }

    /**
     * 如果第一次执行定时任务的时间小于当前的时间，
     * 此时要在第一次执行定时任务的时间加一天，以便此任务在下个时间点执行。
     * 如果不加一天，任务会立即执行。
     */
    public static Date getNextDate () {
        if (nextDate.before(new Date())) {
            nextDate = addDay(nextDate, 1);
        }
        return nextDate;
    }

    /**
     * 增加或减少天数
     */
    private static Date addDay (Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }
}
