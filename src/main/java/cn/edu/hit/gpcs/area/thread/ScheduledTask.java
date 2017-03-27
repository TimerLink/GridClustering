package cn.edu.hit.gpcs.area.thread;

import cn.edu.hit.gpcs.area.ScheduleManager;
import cn.edu.hit.gpcs.area.model.Entry;
import cn.edu.hit.gpcs.area.model.OperationRecord;
import cn.edu.hit.gpcs.area.model.Point;
import cn.edu.hit.gpcs.area.util.ConsoleUtils;
import cn.edu.hit.gpcs.area.util.DateUtils;

import java.util.List;
import java.util.TimerTask;

/**
 * 每日定时计算任务
 */
public class ScheduledTask extends TimerTask {
    private long time;

    public void beforeRun () {
        time = System.currentTimeMillis();
        ConsoleUtils.print(ConsoleUtils.Color.ANSI_YELLOW, "Daily calculation started.\n");
    }

    @Override
    public void run() {
        beforeRun();

        List<Entry> recordEntries = OperationRecord.getYesterdayRecords();
        (new CalcThreadPool()).executeThreads(recordEntries, OperationRecord.TODAY_POINT_SOURCE, false);

        // 计算完成后清空作业点
        ConsoleUtils.print(ConsoleUtils.Color.ANSI_RED, "Cleaning points...\n");
        Point.clearYesterdayPoints();

        afterRun();
    }

    public void afterRun () {
        ConsoleUtils.print(ConsoleUtils.Color.ANSI_YELLOW,
                String.format("Daily calculation finished, cost %s.\n", DateUtils.periodToString(System.currentTimeMillis() - time)));
        ConsoleUtils.print(ConsoleUtils.Color.ANSI_CYAN,
                String.format("Next job starts at %s.\n", ScheduleManager.getNextDate()));
        System.gc();
    }
}
