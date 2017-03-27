package cn.edu.hit.gpcs.area.model;

public class FeatureUnit {
    private int deviceId;
    private double gridArea;
    private double repeatedRate;
    private double unqualifiedRate;
    private double unitMileage;
    private int blockSize;
    private double centerSpacing;
    private double graphicDegree;
    private double leakageRate;
    public FeatureUnit() {}

    public FeatureUnit(int deviceId, double gridArea, double repeatedRate, double unqualifiedRate,
                       double unitMileage, int blockSize, double centerSpacing, double graphicDegree, double leakageRate) {
        this.deviceId = deviceId;
        this.gridArea = gridArea;
        this.repeatedRate = repeatedRate;
        this.unqualifiedRate = unqualifiedRate;
        this.unitMileage = unitMileage;
        this.blockSize = blockSize;
        this.centerSpacing = centerSpacing;
        this.graphicDegree = graphicDegree;
        this.leakageRate = leakageRate;
    }

    public double getGridArea() {
        return gridArea;
    }

    public void setGridArea(double gridArea) {
        this.gridArea = gridArea;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public double getRepeatedRate() {
        return repeatedRate;
    }

    public void setRepeatedRate(double repeatedRate) {
        this.repeatedRate = repeatedRate;
    }

    public double getUnqualifiedRate() {
        return unqualifiedRate;
    }

    public void setUnqualifiedRate(double unqualifiedRate) {
        this.unqualifiedRate = unqualifiedRate;
    }

    public double getUnitMileage() {
        return unitMileage;
    }

    public void setUnitMileage(double unitMileage) {
        this.unitMileage = unitMileage;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public double getCenterSpacing() {
        return centerSpacing;
    }

    public void setCenterSpacing(double centerSpacing) {
        this.centerSpacing = centerSpacing;
    }

    public double getGraphicDegree() {
        return graphicDegree;
    }

    public void setGraphicDegree(double graphicDegree) {
        this.graphicDegree = graphicDegree;
    }

    public double getLeakageRate() {
        return leakageRate;
    }

    public void setLeakageRate(double leakageRate) {
        this.leakageRate = leakageRate;
    }
}
