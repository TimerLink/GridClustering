package cn.edu.hit.gpcs.area.thread;

import cn.edu.hit.gpcs.area.core.*;
import cn.edu.hit.gpcs.area.model.OperationRecord;
import cn.edu.hit.gpcs.area.model.OperationSettings;
import cn.edu.hit.gpcs.area.util.FileUtils;

import java.sql.Date;

/**
 * 计算线程
 */
public class CalcThread implements Runnable {
    private long startTime;
    private boolean canOverride;
    private CalcThreadPool pool;
    private OperationRecord record;
    private OperationSettings settings;

    public CalcThread (int deviceId, Date date) {
        this.record = new OperationRecord(deviceId, date);
    }

    /**
     * @return 设置计算使用的作业参数
     */
    public CalcThread withSettings (OperationSettings settings) {
        this.settings = settings;
        return this;
    }

    /**
     * @return 设置计算使用的点的源数据库表
     */
    public CalcThread withPointsSource (String source) {
        record.setPointsSource(source);
        return this;
    }

    /**
     * @return 设置计算是否可以覆盖已经存在于库中的更大的面积
     */
    public CalcThread canOverride (boolean value) {
        this.canOverride = value;
        return this;
    }

    /**
     * @return 设置用于计数的CalcThreadPool
     */
    public CalcThread withPool (CalcThreadPool pool) {
        this.pool = pool;
        return this;
    }

    /**
     * 打印总进度并使pool的计数器自增
     */
    private void logCounter () {
        if (pool != null) {
            System.out.print(String.format("[%d/%d]", pool.increaseAndGetFTC(), pool.getTTC()));
        }
    }

    private void loginfo () {
        logCounter();
        System.out.println(String.format("[%ss] Record %s@%s done.",
                (System.currentTimeMillis() - startTime) / 1000,
                record.getDeviceId(), record.getDate()));
    }

    private void logerr (Exception e) {
        logCounter();
        System.err.println(String.format("[%ss] Record %s@%s failed.",
                (System.currentTimeMillis() - startTime) / 1000,
                record.getDeviceId(), record.getDate()));
        FileUtils.logError(e, String.format("Record %s@%s failed.\n", record.getDeviceId(), record.getDate()));
    }

    /**
     * 计算并保存作业记录
     */
    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        try {
            CalculatorGroup calculatorGroup = new CalculatorGroup(
//                    new WorkWidthAreaCalculator(),
//                    new IntegralAreaCalculator(),
                    new Integral2AreaCalculator(),
                    new BorderCalculator()
//                    new HistoryCalculator()
//                    new BorderCalculator(),
//                    new FinalAreaCalculator()
            );
            calculatorGroup.calc(record, settings);
            OperationRecord oldRecord = null;
            if (!canOverride) {
                oldRecord = new OperationRecord(record.getDeviceId(), record.getDate()).load();
            }
            if (canOverride || (oldRecord != null && oldRecord.getFinalTotalArea() <= record.getFinalTotalArea())) {
                record.commit();
            }
            record.release();
            loginfo();
        } catch (Exception e) {
            logerr(e);
        }
    }
}