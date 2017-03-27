package cn.edu.hit.gpcs.area.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.sql.Timestamp;
import java.util.Map;

/**
 * 作业点模型
 */
public class Point extends Model {
    private int depth;
    private int workWidth;
    private double latitude;
    private double longitude;
    @JSONField(format = "y-M-d H:m:s")
    private Timestamp gpsTime;

    public Point () {}

    public Point (Timestamp gpsTime, double latitude, double longitude, int depth, int workWidth) {
        this.gpsTime = gpsTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.depth = depth;
        this.workWidth = workWidth;
    }

    public double getLatitude (){
        return latitude;
    }

    public double getLongitude (){
        return longitude;
    }

    public Timestamp getGpsTime () {
        return gpsTime;
    }

    public int getDepth () {
        return depth;
    }

    public int getWorkWidth () {
        return workWidth;
    }

    @Override
    public Map<String, String> mapPropertyToColumn() {
        return null;
    }

    @Override
    public boolean commit() {
        return false;
    }

    public static void clearYesterdayPoints () {
        String query = "DELETE\n" +
                "FROM today_points_details\n" +
                "WHERE DATEDIFF(DAY, updateTime, GETDATE()) >= 1";
        execute(query);
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
