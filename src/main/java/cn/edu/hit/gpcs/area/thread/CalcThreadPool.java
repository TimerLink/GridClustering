package cn.edu.hit.gpcs.area.thread;

import cn.edu.hit.gpcs.area.model.Entry;
import cn.edu.hit.gpcs.area.model.OperationRecord;
import cn.edu.hit.gpcs.area.model.OperationSettings;
import cn.edu.hit.gpcs.utils.DotEnv;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 计算线程包装，用于存贮一系列计算的计数器
 */
public class CalcThreadPool {
    /**
     * TTC: TOTAL TASK COUNT
     * FTC: FINISHED TASK COUNT
     */
    private int TTC;
    private int FTC;

    private ExecutorService pool;
    private ExecutorCompletionService<Boolean> completionService;

    static {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    /**
     * 作业参数缓存池
     */
    private class SettingsPool {
        private Map<Integer, OperationSettings> cache = new HashMap<>();

        public OperationSettings get (int deviceId) {
            OperationSettings settings = cache.get(deviceId);
            if (settings == null) {
                settings = new OperationSettings(deviceId).load();
                cache.put(deviceId, settings);
            }
            return settings;
        }
    }

    public CalcThreadPool () {
        pool = Executors.newFixedThreadPool(Integer.parseInt(DotEnv.get("MAX_CONCURRENCY")));
        completionService = new ExecutorCompletionService<>(pool);
    }

    /**
     * @return 自增后的完成任务数
     */
    public synchronized int increaseAndGetFTC() {
        return ++FTC;
    }

    /**
     * @return 总任务数
     */
    public int getTTC() {
        return TTC;
    }

    /**
     * 建立并运行计算线程
     * @param recordEntries 作业记录
     * @param canOverride 是否在计算结果比现有值小的情况下覆盖现有值
     */
    public void executeThreads (List<Entry> recordEntries, String pointsSource, boolean canOverride) {
        if (recordEntries == null) return;
        TTC = recordEntries.size();
        SettingsPool settingsPool = new SettingsPool();
        for (Entry record : recordEntries) {
            OperationSettings settings = settingsPool.get((int) record.getKey());
            completionService.submit(
                    new CalcThread((int) record.getKey(), (Date) record.getValue())
                            .withPool(this)
                            .withSettings(settings)
                            .withPointsSource(pointsSource)
                            .canOverride(canOverride)
                    , null);
        }
        pool.shutdown();
        while (FTC < TTC) {
            try { completionService.take(); } catch (InterruptedException ignored) {}
        }
        System.gc();
    }

    public void executeThreads (List<Entry> recordEntries) {
        executeThreads(recordEntries, OperationRecord.DEFAULT_POINT_SOURCE, true);
    }
}
