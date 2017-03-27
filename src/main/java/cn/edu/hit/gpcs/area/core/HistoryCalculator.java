package cn.edu.hit.gpcs.area.core;

import cn.edu.hit.gpcs.area.model.OperationRecord;
import cn.edu.hit.gpcs.area.model.OperationSettings;
import cn.edu.hit.gpcs.area.model.base.Coordinate;
import cn.edu.hit.gpcs.area.util.GeoUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 历史重耕判断，计算重耕面积，可视化分析
 */
public class HistoryCalculator extends Calculator {
    private double mWorkWidth;
    private double maxDistance = 999999999;
    private List<ArrayList<Coordinate>> centers = new ArrayList<>();
    private List<ArrayList<Double>> distances = new ArrayList<>();
    @Override
    public void calc(OperationRecord record, OperationSettings settings) {
        mWorkWidth = record.getWorkWidth() / 100.0;
        //引导程序
        List<ArrayList<Coordinate>> centerDevices = new ArrayList<>();//存储不同设备的中心点
        List<String> centerLists = importFile(new File("D:/MachineLearning/SVM/area/centers0210.txt"));
        for (int i = 0;i < centerLists.size();i++){
            String centerMsgs[] = centerLists.get(i).split(" ");
            String centers[] = centerMsgs[3].split("-");
            ArrayList<Coordinate> centerSingleDevice = new ArrayList<>();
            for (int j = 0;j < centers.length;j++){
                String centerXY[] = centers[j].split(",");
                double centerX = Double.parseDouble(centerXY[0]);
                double centerY = Double.parseDouble(centerXY[1]);
                Coordinate center = new Coordinate(centerX, centerY);
                centerSingleDevice.add(center);
            }
            centerDevices.add(centerSingleDevice);
        }
        double centersMinDistance = maxDistance;
        Coordinate index = new Coordinate();//存储索引设备
        for (int i = 0;i < centerDevices.size() - 1;i++){
            for (int j = i + 1;j < centerDevices.size();j++){
                for (int k = 0;k < centerDevices.get(i).size();k++){
                    for (int w = 0;w < centerDevices.get(j).size();w++){
                        double distance = GeoUtils.euclideanDistance(centerDevices.get(i).get(k), centerDevices.get(j).get(w));
                        if (distance < centersMinDistance){
                            centersMinDistance = distance;
                            index.setX(i);
                            index.setY(j);
                        }
                    }
                }
            }
        }
        String device1[] = centerLists.get((int)index.getX()).split(" ");
        String device2[] = centerLists.get((int)index.getY()).split(" ");
        System.out.println("可能存在历史重耕的设备为" + device1[0] + device1[1] + "和" + device2[0] + device2[1]);
        System.out.println(record.getDeviceId());
        /*
        //查重程序
        List<String> blockLists = importFile(new File("D:/JavaProjects/HistoryDoc/deviceNo1.txt"));
        List<String> blockTarget = importFile(new File("D:/JavaProjects/HistoryDoc/deviceNoTarget.txt"));
        for (int i = 0;i < blockLists.size();i++){
            ArrayList<Coordinate> centerSingleDay = getCenterMessage(blockLists.get(i));
            ArrayList<Double> distanceFarthest = new ArrayList<>();
            List<ArrayList<Coordinate>> blocks = getBlockMessage(blockLists.get(i));
            for (int j = 0;j <blocks.size();j++){
                double distance = getFarthest(blocks.get(i));
                distanceFarthest.add(distance);
            }
            for (int j = 0;j < centerSingleDay.size();j++){
                centerSingleDay.get(j).setDaySerial(j);
            }
            centers.add(centerSingleDay);
            distances.add(distanceFarthest);
        }
        ArrayList<Coordinate> centerTarget = getCenterMessage(blockTarget.get(0));
        List<ArrayList<Coordinate>> blockCurrent = getBlockMessage(blockTarget.get(0));
        ArrayList<Double> distancesTarget = new ArrayList<>();
        for (int i = 0;i < blockCurrent.size();i++){
            double distanceSingle = getFarthest(blockCurrent.get(i));
            distancesTarget.add(distanceSingle);
        }
        ArrayList<Coordinate> centersSuspicion = new ArrayList<>();
        for (int i = 0;i < centerTarget.size();i++){
            for (int j = 0;j < centers.size();j++){
                for (int k = 0;k < centers.get(j).size();j++){
                    double centersDistance = GeoUtils.euclideanDistance(centers.get(j).get(k),centerTarget.get(i));
                    double farthestDistance = distances.get(j).get(k) + distancesTarget.get(i);
                    if (centersDistance < farthestDistance){
                        centersSuspicion.add(centers.get(j).get(k));
                    }
                }
            }
        }
        List<ArrayList<Coordinate>> blockSuspicion = new ArrayList<>();
        boolean isRepeated = false;
        for (int i = 0;i <centersSuspicion.size();i++){
            int daySerial = centersSuspicion.get(i).getDaySerial();
            blockSuspicion = getBlockMessage(blockLists.get(daySerial));
            for (int j = 0;j < centerTarget.size();j++){
                for (int k = 0;k < blockSuspicion.size();k++) {
                    if (isInsidePolygon(centerTarget.get(j), blockSuspicion.get(k))){
                        isRepeated = true;
                        break;
                    }
                }
            }
        }
        if (isRepeated){
            System.out.println("发现与设备号为"+"存在历史重耕");
            List<String> coordinateLists = importFile(new File("D:/JavaProjects/HistoryDoc/deviceNoTarget_Coordinate.txt"));
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            for (int i = 0;i < coordinateLists.size();i++){
                String targetXY[] = coordinateLists.get(i).split(",");
                double targetX = Double.parseDouble(targetXY[0]);
                double targetY = Double.parseDouble(targetXY[1]);
                Coordinate target = new Coordinate(targetX, targetY);
                coordinates.add(target);
            }
            ArrayList<Coordinate> coordinateRepeated = new ArrayList<>();
            for (int i = 0;i < coordinates.size();i++){
                for (int j = 0;j < blockSuspicion.size();j++){
                    if (isInsidePolygon(coordinates.get(i), blockSuspicion.get(j))){
                        coordinateRepeated.add(coordinates.get(i));
                    }
                }
            }
            double areaTotal = calcBlockArea(coordinates);
            double areaRepeated = calcBlockArea(coordinateRepeated);
            System.out.println("历史重耕面积为：" + areaRepeated / 666.6666667 + "亩");
            System.out.println("有效耕地面积为：" + (areaTotal - areaRepeated) / 666.6666667 + "亩");
        }else {
            System.out.println("未发现历史重耕问题");
        }
        */
    }

    /**
     * 计算地块面积
     * @param coordinates XY坐标数组
     * @return  网格面积
     */
    public double calcBlockArea (ArrayList<Coordinate> coordinates) {
        double[] azimuth = findBorder(coordinates);
        List<Integer> cellDepth = countInsideArea(azimuth[0],azimuth[1],azimuth[2],azimuth[3],coordinates);//方格个数
        return mWorkWidth*mWorkWidth*cellDepth.size();
    }

    /**
     * 计算具有深度的网格个数
     * @param east 横向最大边界
     * @param west 横向最小边界
     * @param south 纵向最小边界
     * @param north 纵向最大边界
     * @param coordinates 一组XY坐标
     * @return 深度数组
     */
    public List<Integer> countInsideArea(double east, double west, double south, double north, List<Coordinate> coordinates) {
        int flag;
        Coordinate topLeft = new Coordinate(west, north); // 每个正方形的左上角的横纵坐标
        int totalDepth;
        int depthCount;
        List<Integer> cellDepth = new ArrayList<>();
        // 按行扫描矩形区域所有单元格
        while(topLeft.getX() <= east &&
                topLeft.getX() >= west &&
                topLeft.getY() <= north &&
                topLeft.getY() >= south) {
            flag = 0;
            totalDepth = 0;//记录单元格内总深度
            depthCount = 0;//记录单元格内点个数
            // 扫描所有点，判断是否在小格中
            for (int i = 0; i < coordinates.size(); i++) {
                if(isIn(topLeft, mWorkWidth, coordinates.get(i))) {
                    flag = 1;
                    totalDepth = totalDepth + coordinates.get(i).getDepth();
                    depthCount++;
                }
            }
            if(flag == 1) { // 单元格中有点
                totalDepth = totalDepth / depthCount;
                cellDepth.add(totalDepth);
            }
            if(topLeft.getX() + mWorkWidth < east) {
                topLeft.setX(topLeft.getX() + mWorkWidth); // 在同一行下次扫面的小格左上角坐标
            } else {
                topLeft.setX(west); // 换行
                topLeft.setY(topLeft.getY() - mWorkWidth);
            }
        }
        return cellDepth;
    }

    /**
     * 判断点是否在指定正方形内
     * @param edge 边界基准
     * @param arc 边长
     * @param point 待检测坐标
     */
    public boolean isIn(Coordinate edge, double arc, Coordinate point) {
        return point.getX() >= edge.getX() &&
                point.getX() <= edge.getX() + arc &&
                point.getY() <= edge.getY() &&
                point.getY() >= edge.getY() - arc;
    }

    /**
     * 判断点是否在多边形内部
     * @param coordinate 待判断的点
     * @param edges 边界点
     * @return 是否
     */
    public boolean isInsidePolygon(Coordinate coordinate, ArrayList<Coordinate> edges) {
        double tolerant = 5;//Y方向容差
        boolean judge = false;
        boolean lessJudge = false;
        boolean moreJudge = false;
        ArrayList<Coordinate> sameY = new ArrayList<Coordinate>();
        for (int i = 0;i < edges.size();i++){
            if (Math.abs(coordinate.getY() - edges.get(i).getY()) < tolerant){
                sameY.add(edges.get(i));
            }
        }
        for (int i = 0;i <sameY.size();i++){
            if (sameY.get(i).getX()<coordinate.getX()){
                lessJudge = true;
            }
            if (sameY.get(i).getX()>coordinate.getX()){
                moreJudge = true;
            }
        }
        if (lessJudge&&moreJudge){
            judge = true;
        }
        return judge;
    }

    /**
     * 找到边界值
     * @param border 一组地块坐标
     * @return 东西南北边界
     */
    public double[] findBorder(ArrayList<Coordinate> border){
        double[] azimuth = new double[4];
        double east = border.get(0).getX();
        double west = border.get(0).getX();
        double north = border.get(0).getY();
        double south = border.get(0).getY();
        for (int i = 1; i < border.size(); i++) {
            if (border.get(i).getX()>east){
                east = border.get(i).getX();
            }
            if (border.get(i).getX()<west){
                west = border.get(i).getX();
            }
            if (border.get(i).getY()<south){
                south = border.get(i).getY();
            }
            if (border.get(i).getY()>north){
                north = border.get(i).getY();
            }
        }
        azimuth[0] = east;
        azimuth[1] = west;
        azimuth[2] = south;
        azimuth[3] = north;
        return azimuth;
    }

    /**
     * 导入数据
     *
     * @param file 文件
     * @return 数据数组
     */
    public static List<String> importFile(File file) {
        List<String> dataList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
        } catch (Exception e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                    br = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataList;
    }

    /**
     * 通过字符获取某车某天的地块信息
     * @param data 地块字符center1_border1-border2&center2_border1-border2
     * @return 地块信息数组
     */
    public List<ArrayList<Coordinate>> getBlockMessage(String data){
        List<ArrayList<Coordinate>> blocks = new ArrayList<>();
        String blocksStr[] = data.split("&");
        for (int i = 0;i < blocksStr.length;i++){
            ArrayList<Coordinate> blockSingle = new ArrayList<>();
            String centerBorders[] = blocksStr[i].split("_");
            String centerXY[] = centerBorders[0].split(",");
            double centerX = Double.parseDouble(centerXY[0]);
            double centerY = Double.parseDouble(centerXY[1]);
            Coordinate center = new Coordinate(centerX, centerY);
            center.setSerial(i);
            blockSingle.add(center);
            String borders[] = centerBorders[1].split("-");
            for (int j = 0;j < borders.length;j++){
                String borderXY[] = borders[j].split(",");
                double borderX = Double.parseDouble(borderXY[0]);
                double borderY = Double.parseDouble(borderXY[1]);
                Coordinate border = new Coordinate(borderX, borderY);
                blockSingle.add(border);
            }
            blocks.add(blockSingle);
        }
        return blocks;
    }

    /**
     * 获取中心点信息
     * @param data 地块字符
     * @return 中心点信息数组
     */
    public ArrayList<Coordinate> getCenterMessage(String data){
        ArrayList<Coordinate> centers = new ArrayList<>();
        String blocksStr[] = data.split("&");
        for (int i = 0;i < blocksStr.length;i++){
            String centerBorders[] = blocksStr[i].split("_");
            String centerXY[] = centerBorders[0].split(",");
            double centerX = Double.parseDouble(centerXY[0]);
            double centerY = Double.parseDouble(centerXY[1]);
            Coordinate center = new Coordinate(centerX, centerY);
            centers.add(center);
        }
        return centers;
    }

    /**
     * 获取边界点最远距离
     * @param borders 边界点数组
     * @return 最远距离
     */
    public double getFarthest(ArrayList<Coordinate> borders){
        double distance = 0;
        for (int i = 0;i < borders.size()-1;i++){
            for (int j = i + 1;j < borders.size();j++){
                double distanceSingle = GeoUtils.euclideanDistance(borders.get(i), borders.get(j));
                if (distance < distanceSingle){
                    distance = distanceSingle;
                }
            }
        }
        return distance;
    }
}