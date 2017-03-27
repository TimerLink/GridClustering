package cn.edu.hit.gpcs.area.model.base;

/**
 * 聚类中心类
 */
public class Center extends Coordinate {
    private double integralArea;

    public double getIntegralArea() {
        return integralArea;
    }

    public void setIntegralArea(double integralArea) {
        this.integralArea = integralArea;
    }
}
