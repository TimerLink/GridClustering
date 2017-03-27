package cn.edu.hit.gpcs.area.model;

import javax.xml.crypto.Data;

public class TimeUnit {
    private long gridPartitionTime;
    private long gridAreaTime;
    private Data data;

    public TimeUnit(long gridPartitionTime, long gridAreaTime) {
        this.gridPartitionTime = gridPartitionTime;
        this.gridAreaTime = gridAreaTime;
    }

    public long getGridPartitionTime() {
        return gridPartitionTime;
    }

    public void setGridPartitionTime(long gridPartitionTime) {
        this.gridPartitionTime = gridPartitionTime;
    }

    public long getGridAreaTime() {
        return gridAreaTime;
    }

    public void setGridAreaTime(long gridAreaTime) {
        this.gridAreaTime = gridAreaTime;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
