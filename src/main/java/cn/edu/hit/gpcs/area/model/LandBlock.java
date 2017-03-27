package cn.edu.hit.gpcs.area.model;

import cn.edu.hit.gpcs.area.util.GeoUtils;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Joiner;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 地块信息模型
 */
public class LandBlock extends Model {
    private final static String DB_NAME = "land_blocks";

    @JSONField(format = "y-M-d")
    private Date date;
    private int deviceId;
    private int countyId    = -1;
    private double eastLng  = -1;
    private double westLng  = -1;
    private double northLat = -1;
    private double southLat = -1;

    /**
     * @param deviceId 作业车辆
     * @param date 作业日期
     */
    public LandBlock (int deviceId, Date date) {
        this.deviceId = deviceId;
        this.date = date;
    }

    /**
     * @param countyId 所属县编号
     */
    public LandBlock (int deviceId, Date date, int countyId) {
        this(deviceId, date);
        this.countyId = countyId;
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
     * @return 所属县编号
     */
    public int getCountyId() {
        return countyId;
    }

    /**
     * @return 东经度
     */
    public double getEastLng() {
        return eastLng;
    }

    /**
     * @return 北纬度
     */
    public double getNorthLat() {
        return northLat;
    }

    /**
     * @return 南纬度
     */
    public double getSouthLat() {
        return southLat;
    }

    /**
     * @return 西经度
     */
    public double getWestLng() {
        return westLng;
    }

    /**
     * @param id 所属县编号
     */
    public void setCountyId(int id) {
        countyId = id;
    }

    /**
     * 设置地块边界
     * @param eastLng   东经度
     * @param southLat  南纬度
     * @param westLng   西经度
     * @param northLat  北纬度
     */
    public void setBoundary (double eastLng, double southLat, double westLng, double northLat) {
        this.eastLng = eastLng;
        this.southLat = southLat;
        this.westLng = westLng;
        this.northLat = northLat;
    }

    /**
     * @param northWest 西北顶点
     * @param southEast 东南顶点
     */
    public void setBoundary (Point northWest, Point southEast) {
        setBoundary(southEast.getLongitude(), southEast.getLatitude(), northWest.getLongitude(), northWest.getLatitude());
    }

    @Override
    public Map<String, String> mapPropertyToColumn() {
        Map<String, String> columns = new HashMap<>();
        if (countyId > -1)  columns.put("county", String.valueOf(countyId));
        if (eastLng > -1)   columns.put("eastLongitude", String.valueOf(eastLng * GeoUtils.COORDINATE_MULTIPLE));
        if (westLng > -1)   columns.put("westLongitude", String.valueOf(westLng * GeoUtils.COORDINATE_MULTIPLE));
        if (northLat > -1)  columns.put("northLatitude", String.valueOf(northLat * GeoUtils.COORDINATE_MULTIPLE));
        if (southLat > -1)  columns.put("southLatitude", String.valueOf(southLat * GeoUtils.COORDINATE_MULTIPLE));
        return columns;
    }

    @Override
    public boolean commit() {
        Map<String, String> columns = mapPropertyToColumn();
        if (columns.size() == 0) return false;
        String insertSQL = String.format(
                "INSERT INTO %s (deviceNo, date, %s, created_at, updated_at)\n" +
                        "VALUES (%s, '%s', %s, GETDATE(), GETDATE())",
                DB_NAME,
                Joiner.on(", ").join(columns.keySet()),
                deviceId, date,
                Joiner.on(", ").join(columns.values()));
        return execute(insertSQL);
    }
}
