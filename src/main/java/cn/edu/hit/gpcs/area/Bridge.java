package cn.edu.hit.gpcs.area;

import cn.edu.hit.gpcs.area.core.*;
import cn.edu.hit.gpcs.area.model.OperationRecord;
import cn.edu.hit.gpcs.area.model.OperationSettings;
import cn.edu.hit.gpcs.area.util.ConsoleUtils;
import cn.edu.hit.gpcs.area.util.DateUtils;
import cn.edu.hit.gpcs.utils.DotEnv;
import php.java.bridge.JavaBridgeRunner;

import java.sql.Date;

/**
 * Java Bridge接口类
 */
@SuppressWarnings("unused")
public class Bridge {
    /**
     * Java Bridge 设置
     */
    private static final String JAVA_BRIDGE_PORT = DotEnv.get("JAVA_BRIDGE_PORT");

    /**
     * 运行Java Bridge服务
     */
    public static void run () {
        JavaBridgeRunner runner = JavaBridgeRunner.getInstance("INET:" + JAVA_BRIDGE_PORT);
        ConsoleUtils.print(ConsoleUtils.Color.ANSI_CYAN,
                String.format("Java Bridge service started at port %s.\n", JAVA_BRIDGE_PORT));
        try {
            runner.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runner.destroy();
        System.exit(0);
    }

    /**
     * 计算某辆车某天的作业
     * 注意：没有将结果保存到数据库
     * @param deviceId 车辆编号
     * @param dateString 作业日期
     */
    public OperationRecord calc (int deviceId, String dateString) {
        System.out.printf("Calc %d @ %s\n", deviceId, dateString);
        Date date = DateUtils.parseDate(dateString);
        Date today = DateUtils.getToday();
        OperationRecord record = new OperationRecord(deviceId, date);
        if (!date.before(today) && !date.after(today)) {
            record.setPointsSource(OperationRecord.TODAY_POINT_SOURCE);
        }
        OperationSettings settings = new OperationSettings(record.getDeviceId()).load();
        CalculatorGroup calculatorGroup = new CalculatorGroup(
//                new WorkWidthAreaCalculator(),
//                new IntegralAreaCalculator(),
                new Integral2AreaCalculator(),
                new BorderCalculator()
//                new HistoryCalculator()
//                new BorderCalculator(),
//                new FinalAreaCalculator()
        );
        calculatorGroup.calc(record, settings);
        record.release();
        return record;
    }

    /**
     * 计算单辆车某天的作业，并使用指定的幅宽
     * @param deviceId 车辆编号
     * @param dateString 作业日期
     * @param workWidth 工作幅宽
     */
    public OperationRecord calc (int deviceId, String dateString, int workWidth) {
        System.out.printf("Calc %d @ %s * %dcm\n", deviceId, dateString, workWidth);
        Date date = DateUtils.parseDate(dateString);
        Date today = DateUtils.getToday();
        OperationRecord record = new OperationRecord(deviceId, date);
        if (!date.before(today) && !date.after(today)) {
            record.setPointsSource(OperationRecord.TODAY_POINT_SOURCE);
        }
        OperationSettings settings = new OperationSettings(record.getDeviceId()).load();
        RescueCalculator calculator = new RescueCalculator();
        calculator.setWorkWidth(workWidth);
        calculator.calc(record, settings);
        record.release();
        return record;
    }

}
