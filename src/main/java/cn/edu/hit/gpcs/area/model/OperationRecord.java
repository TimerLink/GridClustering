package cn.edu.hit.gpcs.area.model;

import cn.edu.hit.gpcs.area.util.GeoUtils;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * 某个车辆某天使用某个农具的作业数据
 * 注意：如果sensorId为空，将获取当日所有作业数据
 */
public class OperationRecord extends Model {
    private final static String DB_NAME = "operation_statistics";
    public final static String DEFAULT_POINT_SOURCE = "points_details";
    public final static String TODAY_POINT_SOURCE = "today_points_details";

    @JSONField(format = "y-M-d")
    private Date date;
    private int deviceId;
    private int sensorId                = -1;
    private int workWidth               = -1;
    private int averageDepth            = -1;
    private int workWidthQualifiedArea  = -1;
    private int workWidthTotalArea      = -1;
    private int integralQualifiedArea   = -1;
    private int integralTotalArea       = -1;
    private int integral2QualifiedArea  = -1;
    private int integral2TotalArea      = -1;
    private int finalQualifiedArea      = -1;
    private int finalTotalArea          = -1;
    @JSONField(format = "y-M-d H:m:s")
    private Timestamp lastUpdate        = null;

    @JSONField(serialize = false) private List<Point> points;
    @JSONField(serialize = false) private String pointsSource; // 指定作业点数据库表
    @JSONField(serialize = false) private Map<Integer, Integer> sensorCnt;    // 用于统计农具点数
    @JSONField(serialize = false) private Map<Integer, Integer> workWidthMap; // 当前保存路径中所有农具的作业幅宽

    /**
     * @param deviceId 车辆编号
     * @param date 作业日期
     */
    public OperationRecord (int deviceId, Date date) {
        this.deviceId = deviceId;
        this.date = date;
        sensorCnt = new HashMap<>();
        workWidthMap = new HashMap<>();
        pointsSource = DEFAULT_POINT_SOURCE;
    }

    /**
     * @param sensorId 农具编号
     */
    public OperationRecord (int deviceId, int sensorId, Date date) {
        this(deviceId, date);
        this.sensorId = sensorId;
    }

    /**
     * @return 车辆编号
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * @return 作业日期
     */
    public Date getDate() {
        return date;
    }

    /**
     * @return 作业幅宽
     */
    public int getWorkWidth () {
        if (workWidth == -1)
            getPoints();
        return workWidth;
    }

    /**
     * @return 农具编号
     */
    public int getSensorId() {
        return sensorId;
    }

    /**
     * @return 平均作业深度
     */
    public int getAverageDepth() {
        return averageDepth;
    }

    /**
     * @return 最终合格作业面积
     */
    public int getFinalQualifiedArea() {
        return finalQualifiedArea;
    }

    /**
     * @return 最终总作业面积
     */
    public int getFinalTotalArea() {
        return finalTotalArea;
    }

    /**
     * @return 合格积分算法作业面积
     */
    public int getIntegralQualifiedArea() {
        return integralQualifiedArea;
    }

    /**
     * @return 合格轨迹等效作业面积
     */
    public int getWorkWidthQualifiedArea() {
        return workWidthQualifiedArea;
    }

    /**
     * @return 总积分算法作业面积
     */
    public int getIntegralTotalArea() {
        return integralTotalArea;
    }

    /**
     * @return 新总积分算法作业面积
     */
    public int getIntegral2TotalArea() {
        return integral2TotalArea;
    }

    /**
     * @return 新合格积分算法作业面积
     */
    public int getIntegral2QualifiedArea() {
        return integral2QualifiedArea;
    }

    /**
     * @return 总轨迹等效作业面积
     */
    public int getWorkWidthTotalArea() {
        return workWidthTotalArea;
    }

    /**
     * @return 最近更新时的GPS Time
     */
    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param depth 平均深度（cm）
     */
    public void setAverageDepth (int depth) {
        averageDepth = depth;
    }

    /**
     * @param area 合格轨迹等效作业面积
     */
    public void setWorkWidthQualifiedArea(int area) {
        workWidthQualifiedArea = area;
    }

    /**
     * @param area 总轨迹等效作业面积
     */
    public void setWorkWidthTotalArea(int area) {
        workWidthTotalArea = area;
    }

    /**
     * @param area 合格积分作业面积
     */
    public void setIntegralQualifiedArea(int area) {
        integralQualifiedArea = area;
    }

    /**
     * @param area 总积分作业面积
     */
    public void setIntegralTotalArea(int area) {
        integralTotalArea = area;
    }

    /**
     * @param area 新总积分作业面积
     */
    public void setIntegral2TotalArea(int area) {
        integral2TotalArea = area;
    }

    /**
     * @param area 新合格积分作业面积
     */
    public void setIntegral2QualifiedArea(int area) {
        integral2QualifiedArea = area;
    }

    /**
     * @param area 最终合格作业面积
     */
    public void setFinalQualifiedArea (int area) {
        finalQualifiedArea = area;
    }

    /**
     * @param area 最终总作业面积
     */
    public void setFinalTotalArea(int area) {
        finalTotalArea = area;
    }

    public void setPointsSource(String source) {
        if (!source.equals(TODAY_POINT_SOURCE))
            this.pointsSource = DEFAULT_POINT_SOURCE;
        else
            this.pointsSource = source;
    }

    /**
     * @return this
     */
    public OperationRecord load () {
        String sql = String.format(
                "SELECT * FROM %s\n" +
                        "WHERE deviceNo = %d\n" +
                        "  AND DATEDIFF(DAY, date, '%s') = 0", DB_NAME, deviceId, date);
        executeQuery(sql, new OnQueryResultListener() {
            @Override
            public void onResult(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    averageDepth            = rs.getInt("averageDepth");
                    workWidthQualifiedArea  = rs.getInt("workWidthQualifiedArea");
                    workWidthTotalArea      = rs.getInt("workWidthTotalArea");
                    integralQualifiedArea   = rs.getInt("integralQualifiedArea");
                    integralTotalArea       = rs.getInt("integralTotalArea");
                    finalQualifiedArea      = rs.getInt("finalQualifiedArea");
                    finalTotalArea          = rs.getInt("finalTotalArea");
                    lastUpdate              = rs.getTimestamp("gpsTime");
                    break;
                }
            }
        });
        return this;
    }

    /**
     * 处理点集查询结果
     * @param rs 查询结果
     * @throws SQLException
     */
    private void addPoint (ResultSet rs) throws SQLException {
        int mSensorId = rs.getInt("deep");
        int mWorkWidth = rs.getInt("workWidth");
        if (mSensorId > 0) {
            int cnt = sensorCnt.get(mSensorId) != null ? sensorCnt.get(mSensorId) : 0;
            sensorCnt.put(mSensorId, cnt + 1);
            workWidthMap.put(mSensorId, mWorkWidth);
        }
        lastUpdate = rs.getTimestamp("gpsTime");
        points.add(
                new Point(
                        lastUpdate,
                        (float) rs.getLong("latitude") / GeoUtils.COORDINATE_MULTIPLE,
                        (float) rs.getLong("longitude") / GeoUtils.COORDINATE_MULTIPLE,
                        rs.getInt("deep2"),
                        mWorkWidth
                )
        );
    }

    /**
     * 根据农具计数器选取点数最多的农具作为主农具
     */
    private void setSensorId () {
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(sensorCnt.entrySet());
        if (list.size() == 0)
            return;
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> a, Map.Entry<Integer, Integer> b) {
                return b.getValue() - a.getValue();
            }
        });
        sensorId = list.get(0).getKey();
        workWidth = workWidthMap.get(sensorId);
    }

    /**
     * 获取当日作业点集
     * 注意：如果当前对象有点集的缓存，则不从服务器获取最新数据
     */
    public List<Point> getPoints () {
        if (points != null)
            return points;
        points = new ArrayList<>();
        String sql = String.format(
                "SELECT gpsTime,\n" +
                        "       latitude,\n" +
                        "       longitude,\n" +
                        "       deep,\n" +
                        "       deep2,\n" +
                        "       workWidth\n" +
                        "FROM " + pointsSource + "\n" +
                        "LEFT JOIN deep_tools ON deep_tools.id = " + pointsSource + ".deep\n" +
                        "WHERE (flag > 127\n" +
                        "       OR flag = 0)\n" +
                        "  AND deviceNo = %s\n" + (sensorId >= 0 ?
                        "  AND deep = " + sensorId + "\n" : "") +
                        "  AND DATEDIFF(DAY, '%s', gpsTime) = 0\n" +
                        "ORDER BY gpsTime", deviceId, date);
        executeQuery(sql, new OnQueryResultListener() {
            @Override
            public void onResult(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    addPoint(rs);
                }
                if (sensorId == -1) setSensorId();
            }
        });
        return points;
    }

    /**
     * 获取一定深度范围内的点集
     * @param minDepth 最低深度
     * @param maxDepth 最高深度
     * @return 筛选后点集
     */
    public List<Point> getPoints (final int minDepth, final int maxDepth) {
        List<Point> mPoints = getPoints();
        if (mPoints == null || mPoints.size() == 0)
            return mPoints;
        return Lists.newArrayList(Collections2.filter(mPoints, new Predicate<Point>() {
            @Override
            public boolean apply(Point point) {
                return true;
            }
        }));
    }

    @Override
    public Map<String, String> mapPropertyToColumn() {
        Map<String, String> columns = new HashMap<>();
        if (sensorId > -1) columns.put("deeptoolId", String.valueOf(sensorId));
        if (averageDepth > -1) columns.put("averageDepth", String.valueOf(averageDepth));
        if (workWidthQualifiedArea > -1) columns.put("workWidthQualifiedArea", String.valueOf(workWidthQualifiedArea));
        if (workWidthTotalArea > -1) columns.put("workWidthTotalArea", String.valueOf(workWidthTotalArea));
        if (integralQualifiedArea > -1) columns.put("integralQualifiedArea", String.valueOf(integralQualifiedArea));
        if (integralTotalArea > -1) columns.put("integralTotalArea", String.valueOf(integralTotalArea));
        if (integral2QualifiedArea > -1) columns.put("integral2QualifiedArea", String.valueOf(integral2QualifiedArea));
        if (integral2TotalArea > -1) columns.put("integral2TotalArea", String.valueOf(integral2TotalArea));
        if (finalQualifiedArea > -1) columns.put("finalQualifiedArea", String.valueOf(finalQualifiedArea));
        if (finalTotalArea > -1) columns.put("finalTotalArea", String.valueOf(finalTotalArea));
        if (lastUpdate != null) columns.put("gpsTime", "'" + lastUpdate.toString() + "'");
        return columns;
    }

    @Override
    public boolean commit () {
        // 如果当天没有作业点，则不插入新记录
        if (getPoints().size() == 0) return false;
        Map<String, String> columns = mapPropertyToColumn();
        if (columns.size() == 0) return false;
        String insertSQL = String.format(
                "INSERT INTO %s (deviceNo, date, %s, created_at, updated_at)\n" +
                "VALUES (%s, '%s', %s, GETDATE(), GETDATE())",
                DB_NAME,
                Joiner.on(", ").join(columns.keySet()),
                deviceId, date,
                Joiner.on(", ").join(columns.values()));
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            entries.add(entry.getKey() + " = " + entry.getValue());
        }
        String updateSQL = String.format(
                "UPDATE %s SET %s, updated_at = GETDATE()\n" +
                "WHERE deviceNo = %s AND DATEDIFF(DAY, '%s', date) = 0",
                DB_NAME, Joiner.on(", ").join(entries), deviceId, date);
        String query = String.format(
                "IF NOT EXISTS (SELECT * FROM %s WHERE deviceNo = %s AND DATEDIFF(DAY, '%s', date) = 0)\n" +
                "%s \n" + "ELSE \n" + "%s", DB_NAME, deviceId, date, insertSQL, updateSQL);
        return execute(query);
    }

    /**
     * 尝试修复内存占用问题
     */
    public void release() {
        points = null;
    }

    /**
     * 将ResultSet转换为OperationRecord列表
     * @param result 结果列表
     */
    private static OnQueryResultListener mapResultSetToEntities(final List<Entry> result) {
        return new OnQueryResultListener() {
            @Override
            public void onResult(ResultSet rs) {
                try {
                    while (rs.next()) {
                        result.add(new Entry<>(rs.getInt("deviceNo"), rs.getDate("date")));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 所有历史作业记录
     */
    public static List<Entry> getAll () {
        List<Entry> result = new ArrayList<>();
        String sql = "SELECT deviceNo, date\n" +
                "FROM operation_statistics\n" +
                "WHERE deeptoolId > 0\n" +
                "  AND averageDepth > 0\n" +
                "  AND DATEDIFF(YEAR, date, '2015-01-01') <= 0";
        executeQuery(sql, mapResultSetToEntities(result));
        return result;
    }

    /**
     * 返回指定日期的所有作业记录
     * @param date 作业日期
     */
    public static List<Entry> getAllByDate (Date date) {
        List<Entry> result = new ArrayList<>();
        String sql = String.format(
                "SELECT DISTINCT deviceNo, '%s' AS date\n" +
                "FROM points_details\n" +
                "WHERE DATEDIFF(DAY, gpsTime, '%s') = 0", date, date);
        executeQuery(sql, mapResultSetToEntities(result));
        return result;
    }

    /**
     * 返回昨日上点的所有作业记录，用于每日计算
     */
    public static List<Entry> getYesterdayRecords() {
        List<Entry> records = new ArrayList<>();
        String sql = "SELECT DISTINCT deviceNo,\n" +
                "                CONVERT(varchar(10), gpsTime, 23) AS date\n" +
                "FROM today_points_details\n" +
                "WHERE DATEDIFF(YEAR, gpsTime, '2015-01-01') <= 0\n" +
                "  AND DATEDIFF(DAY, updateTime, GETDATE()) >= 1";
        executeQuery(sql, mapResultSetToEntities(records));
        return records;
    }
}
