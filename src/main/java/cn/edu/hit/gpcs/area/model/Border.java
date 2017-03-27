package cn.edu.hit.gpcs.area.model;

public class Border {
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;

    public Border(double minX, double minY, double maxX, double maxY){
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }
}
