package cn.edu.hit.gpcs.area.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 车辆信息模型
 */
public class DeviceInfo extends Model {
    private int deviceId;
    private int cityId;
    private int countyId;
    private int provinceId;
    private int cooperativeId;

    public DeviceInfo (int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return 车辆编号
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * @return 所属省编号
     */
    public int getProvinceId() {
        return provinceId;
    }

    /**
     * @return 所属城市编号
     */
    public int getCityId() {
        return cityId;
    }

    /**
     * @return 所属县/区编号
     */
    public int getCountyId() {
        return countyId;
    }

    /**
     * @return 所属合作社编号
     */
    public int getCooperativeId() {
        return cooperativeId;
    }

    /**
     * 从服务器获取车辆信息
     * @return this
     */
    public DeviceInfo load () {
        String sql = String.format(
                "SELECT provinces.id AS provinceId,\n" +
                "       cities.id AS cityId,\n" +
                "       counties.id AS countyId,\n" +
                "       cooperatives.id AS cooperativeId\n" +
                "FROM car_infos\n" +
                "LEFT JOIN cooperatives ON car_infos.coopId = cooperatives.id\n" +
                "LEFT JOIN counties ON cooperatives.county = counties.id\n" +
                "LEFT JOIN cities ON counties.city = cities.id\n" +
                "LEFT JOIN provinces ON cities.province = provinces.id\n" +
                "WHERE car_infos.id = %d", deviceId);
        executeQuery(sql, new OnQueryResultListener() {
            @Override
            public void onResult(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    provinceId      = rs.getInt("provinceId");
                    cityId          = rs.getInt("cityId");
                    countyId        = rs.getInt("countyId");
                    cooperativeId   = rs.getInt("cooperativeId");
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
