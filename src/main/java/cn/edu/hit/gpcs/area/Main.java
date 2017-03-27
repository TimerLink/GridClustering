package cn.edu.hit.gpcs.area;

import cn.edu.hit.gpcs.area.model.Entry;
import cn.edu.hit.gpcs.area.model.OperationRecord;
import cn.edu.hit.gpcs.area.thread.CalcThreadPool;
import cn.edu.hit.gpcs.area.util.DateUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Main {
    @Option(name = "--date", usage = "Specify a date to calculate records", metaVar = "2000-01-01")
    private String date = null;
    @Option(name = "--device", usage = "Specify a device ID to calculate records", metaVar = "1001")
    private Integer deviceId = null;
    @Deprecated
    @Option(name = "--workwidth", usage = "(Deprecated) Calc a specified record with certain work width", metaVar = "250")
    private Integer workWidth = null;
    @Option(name = "--all", usage = "If calculate all records")
    private boolean calcAll = false;
    @Option(name = "--daily", usage = "Run the daily calculation tasks")
    private boolean daily = false;
    @Option(name = "--service", usage = "Start the application as a Java Bridge service")
    private boolean service = false;

    public static void main (String[] args) {
        new Main().handleOptions(args);
    }

    /**
     * 解析程序启动参数
     */
    private Main handleOptions (String[] args) {
        List<Entry> recordEntries;
        CmdLineParser parser = new CmdLineParser(this, ParserProperties.defaults().withUsageWidth(80));
        try {
            parser.parseArgument(args);
            if (daily || service) {
                if (daily) ScheduleManager.run();
                if (service) Bridge.run();
                return this;
            }
            if (calcAll) {
                recordEntries = OperationRecord.getAll();
            } else if (date != null && deviceId == null) {
                recordEntries = OperationRecord.getAllByDate(DateUtils.parseDate(date));
            } else if (date != null) {
                recordEntries = new ArrayList<>();
                Date dateObject = DateUtils.parseDate(date);
                if (dateObject != null) {
                    if (workWidth != null && workWidth > 0) {
                        System.out.println((new Bridge()).calc(deviceId, date, workWidth));
                    } else {
                        recordEntries.add(new Entry<>(deviceId, dateObject));
                    }
                }
            } else {
                throw new CmdLineException("No valid option.");
            }
            if (recordEntries != null) {
                (new CalcThreadPool()).executeThreads(recordEntries);
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("Available options:");
            parser.printUsage(System.err);
            System.err.println();
        }
        return this;
    }
}