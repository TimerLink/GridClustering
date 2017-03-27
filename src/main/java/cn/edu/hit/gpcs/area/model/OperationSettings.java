package cn.edu.hit.gpcs.area.model;

import cn.edu.hit.gpcs.utils.DotEnv;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 获得某辆车所在地区的作业参数
 */
public class OperationSettings extends Model {
    private int deviceId;
    private int lowestDepth      = -1;
    private int averageDepth     = -1;
    private int highestDepth     = -1;
    private int maxDistance      = -1;
    private int maxTimeout       = -1;
    private int areaMultiple     = -1;
    private float subsidyPerAcre = -1;

    /**
     * @param deviceId 车辆编号
     */
    public OperationSettings (int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return 农具编号
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * @return 最低深松深度(cm)
     */
    public int getLowestDepth () {
        return lowestDepth >= 0 ? lowestDepth : Integer.parseInt(DotEnv.get("DEFAULT_LOWEST_DEPTH"));
    }

    /**
     * @return 合格深松深度(cm)
     */
    public int getAverageDepth () {
        return averageDepth >= 0 ? averageDepth : Integer.parseInt(DotEnv.get("DEFAULT_AVERAGE_DEPTH"));
    }

    /**
     * @return 最高深松深度(cm)
     */
    public int getHighestDepth () {
        return highestDepth >= 0 ? highestDepth : Integer.parseInt(DotEnv.get("DEFAULT_HIGHEST_DEPTH"));
    }

    /**
     * @return 最长路线保持不断开距离(m)
     */
    public int getMaxDistance () {
        return maxDistance >= 0 ? maxDistance : Integer.parseInt(DotEnv.get("DEFAULT_MAX_DISTANCE"));
    }

    /**
     * @return 最长路线保持不断开时间间隔(s)
     */
    public int getMaxTimeout () {
        return maxTimeout >= 0 ? maxTimeout : Integer.parseInt(DotEnv.get("DEFAULT_MAX_TIMEOUT"));
    }

    /**
     * @return 面积补偿倍数(%)
     */
    public int getAreaMultiple () {
        return areaMultiple >= 0 ? areaMultiple : Integer.parseInt(DotEnv.get("DEFAULT_AREA_MULTIPLE"));
    }

    /**
     * @return 单位补贴(元/亩)
     */
    public float getSubsidyPerAcre () {
        return subsidyPerAcre >= 0 ? subsidyPerAcre : Float.parseFloat(DotEnv.get("DEFAULT_SUBSIDY_PER_ACRE"));
    }

    /**
     * 从服务器获取作业参数
     * @return this
     */
    public OperationSettings load () {
        String sql = String.format(
                "SELECT *\n" +
                "FROM car_infos\n" +
                "LEFT JOIN cooperatives ON car_infos.coopId = cooperatives.id\n" +
                "LEFT JOIN counties ON cooperatives.county = counties.id\n" +
                "LEFT JOIN cities ON counties.city = cities.id\n" +
                "LEFT JOIN operation_settings ON operation_settings.level = 'province'\n" +
                "AND operation_settings.level_num = cities.province\n" +
                "WHERE car_infos.id = %d", deviceId);
        executeQuery(sql, new OnQueryResultListener() {
            @Override
            public void onResult(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    lowestDepth     = rs.getInt("lowestDepth");
                    averageDepth    = rs.getInt("averageDepth");
                    highestDepth    = rs.getInt("highestDepth");
                    maxDistance     = rs.getInt("maxDistance");
                    maxTimeout      = rs.getInt("maxTimeout");
                    areaMultiple    = rs.getInt("areaMultiple");
                    subsidyPerAcre  = rs.getFloat("subsidy");
                    break;
                }
            }
        });
        return this;
    }

    @Override
    public Map<String, String> mapPropertyToColumn() {
        return null;
    }

    @Override
    public boolean commit() {
        return false;
    }

}
