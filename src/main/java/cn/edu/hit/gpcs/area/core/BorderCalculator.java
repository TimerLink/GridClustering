package cn.edu.hit.gpcs.area.core;

import cn.edu.hit.gpcs.area.model.*;
import cn.edu.hit.gpcs.area.model.base.Coordinate;
import cn.edu.hit.gpcs.area.util.DrawUtils;
import cn.edu.hit.gpcs.area.util.GeoUtils;
import cn.edu.hit.gpcs.area.util.RouteUtils;
import cn.edu.hit.gpcs.utils.DotEnv;
import com.sun.prism.impl.Disposer;
import javafx.scene.layout.BorderRepeat;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.JarEntry;


/**
 * 积分面积计算核心
 */
public class BorderCalculator extends Calculator {

    private static double mWorkWidth;
    private int standardDepth;

    @Override
    public void calc(OperationRecord record, OperationSettings settings) {
        final double AREA_RATE = 666.6666667;
        mWorkWidth = getWorkWidth(record);
        standardDepth = settings.getAverageDepth();//标准深度，判断合格
        List<List<Point>> routes = sliceByPointsWithoutDepth(record, settings);
        if (!routes.isEmpty()) {
            Point pointStandard = getStandardPoint(record.getPoints(
                    settings.getLowestDepth(), settings.getHighestDepth()));
            double standLat = pointStandard.getLatitude();
            double standLng = pointStandard.getLongitude();
            int count = 0;//记录时间序列
            ArrayList<Coordinate> border = new ArrayList<>();//放置坐标
            ArrayList<Border> borders = new ArrayList<>();//放置每段边界
            ArrayList<ArrayList<Coordinate>> singleLines = new ArrayList<>();//放置单行分段坐标
            //插点转换
            for (int k1 = 0;k1<routes.size();k1++){
                ArrayList<Coordinate> coordinates = new ArrayList<>();
                List<Point> newRoute = RouteUtils.interpolate(routes.get(k1)); //段内插点
                for (Point point : newRoute) {
                    if (Math.abs(point.getLatitude() - standLat) + Math.abs(point.getLongitude() - standLng) < 5.0) {
                        Coordinate coordinate = GeoUtils.transformToXY(point);
                        coordinate.setLandBlock(k1);
                        coordinate.setDepth(point.getDepth());
                        coordinate.setTimeSeries(count);count++;
                        coordinates.add(coordinate);
                    }
                }
                if (coordinates.size() > 3) {
                    singleLines.add(coordinates);
                    //三行插值
                    for (int i = 0;i<coordinates.size()-1;i++){
                        ArrayList<Coordinate> bothSides = getBothSides(coordinates.get(i), coordinates.get(i+1));
                        border.addAll(bothSides);
                    }
                    double[] azimuth = GeoUtils.findBorder(coordinates);
                    borders.add(new Border(azimuth[0], azimuth[2], azimuth[1], azimuth[3]));
                }
            }
//            double areaSubsectionRectangle = getSubsectionRectangle(singleLines) / AREA_RATE;
//            double areaUnrepeatedRectangleSingleLine = getTotalUnrepeatedRectangle(singleLines,borders) / AREA_RATE;
//            System.out.println("分段矩形解析面积:" + areaSubsectionRectangle/666.6666667);
//            System.out.println("不重复单行插值分段矩形解析面积:" + areaUnrepeatedRectangleSingleLine/666.6666667);
            //划分地块
            System.out.println("==============");
            System.out.print(record.getDeviceId() + "@" + record.getDate() + "整合地块信息:");
            long startTime = System.currentTimeMillis();   //获取开始时间
            ArrayList<Coordinate> boor = gridPartition(border);//根据相邻关系划分地块
            long endTime = System.currentTimeMillis(); //获取结束时间
            long differTime = (endTime - startTime)/1000;

            ArrayList<ArrayList<Coordinate>> blockArray = getBlockArray(boor);//地块集合
            ArrayList<ArrayList<Coordinate>> blockArrayNew = getQualifiedBlockArray(blockArray);//大地块集合，去除小于10个点的地块

            double areaTotalRectangle = getSubsectionRectangle(blockArrayNew) / AREA_RATE;//聚类后直接计算
            double areaTotalSubsectionRectangle = getTotalSubsectionRectangle(blockArrayNew) / AREA_RATE;//聚类后分段计算,sortByTimeSeries

            System.out.println(record.getDeviceId() + "@" + record.getDate() + "聚类后面积:" + areaTotalSubsectionRectangle);
            System.out.println(record.getDeviceId() + "@" + record.getDate() + "聚类后直接面积:" + areaTotalRectangle);

//            exportCoordinatesWithDepth(border, record);//导出带有深度坐标
//            exportCoordinatesWithBlockNum(blockArrayNew);//导出带有地块的坐标

//            /* //1
            //计算地块面积，周长，合格面积，边缘点
            double areaOther = 0;//记录其他地块面积
            int countOtherPoint = 0;//记录其他地块点个数
            int countOther = 0;
            double totalCompare = 0;//记录总面积
            double qualifiedCompare = 0;//记录合格面积
            double graphicDegree = 0;//衡量图形的规则程度，越小越规则
            ArrayList<Coordinate> centers = new ArrayList<>();
            ArrayList<ArrayList<Coordinate>> eageNew = new ArrayList<>();//大地块集合
            long startTimeBlock = System.currentTimeMillis();   //获取开始时间
            for (int i = 0;i<blockArrayNew.size();i++){
                double[] areaSummarizing = calcBlockArea(blockArrayNew.get(i));
                double area = areaSummarizing[0];
                double qualifiedArea = areaSummarizing[1];
                ArrayList<Coordinate> eageSingle = getEage(blockArrayNew.get(i));
                eageNew.add(eageSingle);
                centers.add(eageSingle.get(eageSingle.size()-1));
                double perimeter = getPerimeter(eageSingle);
                graphicDegree += perimeter / area;
                totalCompare += area;
                qualifiedCompare += qualifiedArea;
                //重新排序编号
                if (area>0.5*AREA_RATE) {
                    for (int j = 0; j < blockArrayNew.get(i).size(); j++) {
                        blockArrayNew.get(i).get(j).setLandBlock(i + 1 - countOther);
                    }
//                    System.out.println("地块标号:"+blockArrayNew.get(i).get(0).getLandBlock()+
//                            " 点个数:"+blockArrayNew.get(i).size()+" 面积:"+area/AREA_RATE);
                }else {
                    for (int j = 0; j < blockArrayNew.get(i).size(); j++) {
                        blockArrayNew.get(i).get(j).setLandBlock(0);
                        countOtherPoint++;
                    }
                    countOther++;
                    areaOther += area;
                }
            }
            long endTimeBlock = System.currentTimeMillis(); //获取结束时间
            long differTimeBlock = (endTimeBlock - startTimeBlock) / 1000;
            TimeUnit timeUnit = new TimeUnit(differTime, differTimeBlock);

            //打印地块面积信息
            System.out.println(record.getDeviceId() + "@" + record.getDate() + "地块总面积:"+totalCompare/AREA_RATE);
//            System.out.println("其它地块点个数:"+countOtherPoint+" 面积:"+areaOther/AREA_RATE);
//            System.out.println("地块合格面积:"+qualifiedCompare/AREA_RATE);
//            System.out.println(record.getDeviceId() + "@" + record.getDate() + "地块总数:"+blockArrayNew.size());
            System.out.println("==============");
            //导出网格面积时间
//            exportTime(boor, timeUnit);
            //导出中心点:设备号 日期 center1-center2，供引导程序使用
//            exportCenters(centers, record);
            //导出设备当天中心点和边缘点:center1_border1-border2&center2_border1-border2，供历史查重使用
//            exportCentersAndEdges(eageNew);
            //导出地块和边缘点
//            exportCoordinatesWithBlockNumAndEdge(blockArrayNew, eageNew);
            //导出特征信息
//            double totalDistance = getTotalDistance(boor);//行驶路径长度
//            FeatureUnit featureUnit = new FeatureUnit(record.getDeviceId(),
//                    totalCompare / AREA_RATE,//网格计算面积
//                    totalDistance * mWorkWidth / totalCompare - 1,//重耕率
//                    1 - qualifiedCompare / totalCompare,//不合格率
//                    totalDistance / totalCompare,//单位面积行驶里程数
//                    blockArrayNew.size(),//地块数
//                    getCenterSpacing(centers) / 1000,//地块紧凑度
//                    graphicDegree / 1000,//图形规则度
//                    (totalCompare / AREA_RATE - areaTotalRectangle) / areaTotalRectangle);//漏耕率
//            exportFeature(featureUnit);
            //绘制地块
//            draw(boor,eageNew,record.getDeviceId()+" "+record.getDate());
        }
    }

/**
 * 导出函数群
 */

    public void exportTime(ArrayList<Coordinate> boor, TimeUnit timeUnit) {
        String blockTime = boor.size() + "," + timeUnit.getGridAreaTime() + "\r\n";
        writeOS("D:/JavaProjects/BootStrapHistoryCalculator/blockTime0.5.txt", blockTime);
    }

    public void exportFeature(FeatureUnit unit) {
        if (unit.getBlockSize()>0) {
            String message = unit.getDeviceId() + ","
                    + unit.getGridArea() + ","
                    + unit.getRepeatedRate() + ","
                    + unit.getUnqualifiedRate() + ","
                    + unit.getUnitMileage() + ","
                    + unit.getBlockSize() + ","
                    + unit.getCenterSpacing() + ","
                    + unit.getGraphicDegree() + ","
                    + unit.getLeakageRate() + "\r\n";
            System.out.println(message);
            String dateString = getDateString();
            writeOS("D:/MachineLearning/SVM/general_messageTri"+ dateString +".txt", message);
        }else {
            System.out.println("无合法地块导出");
        }
    }

    /**
     * 导出聚类时间
     * @param border 轨迹点
     * @param blockArrayNew 地块数组
     * @param differTime 聚类算法运行时间
     */
    public void exportClusteringTime(ArrayList<Coordinate> border, ArrayList<ArrayList<Coordinate>> blockArrayNew, long differTime){
        String clusteringTime = border.size() + "," + blockArrayNew.size() + "," + differTime + "\r\n";
        System.out.println("导出聚类时间:" + clusteringTime);
        writeOS("D:/JavaProjects/BootStrapHistoryCalculator/clusteringTime.csv", clusteringTime);
    }

    /**
     * 导出中心点和边缘点
     * @param eageNew 边缘点数组，最后一个是中心点
     */
    public void exportCentersAndEdges(ArrayList<ArrayList<Coordinate>> eageNew){
        String blockStr = "";
        for (int i = eageNew.size() - 1;i >= 1;i--){
            blockStr += eageNew.get(i).get(eageNew.get(i).size() - 1).getX() + ","
                    + eageNew.get(i).get(eageNew.get(i).size() - 1).getY() + "_";
            for (int j = eageNew.get(i).size() - 2;j >= 1;j--){
                blockStr += eageNew.get(i).get(j).getX() + "," + eageNew.get(i).get(j).getY() + "-";
            }
            blockStr += eageNew.get(i).get(0).getX() + "," + eageNew.get(i).get(0).getY() + "&";
        }
        blockStr += eageNew.get(0).get(eageNew.get(0).size() - 1).getX() + ","
                + eageNew.get(0).get(eageNew.get(0).size() - 1).getY() + "_";
        for (int i = eageNew.get(0).size() - 2;i >= 1;i--){
            blockStr += eageNew.get(0).get(i).getX() + "," + eageNew.get(0).get(i).getY() + "-";
        }
        blockStr += eageNew.get(0).get(0).getX() + "," + eageNew.get(0).get(0).getY() + "\r\n";
        writeOS("D:/JavaProjects/BootStrapHistoryCalculator/device10223.txt", blockStr);
    }

    /**
     * 导出中心点
     * @param centers 中心点
     * @param record 记录
     */
    public void exportCenters(ArrayList<Coordinate> centers, OperationRecord record){
        String centerStr = record.getDeviceId() + " " + record.getDate() + " ";
        for (int i = 0;i < centers.size() - 1;i++){
            centerStr += centers.get(i).getX() + "," + centers.get(i).getY() + "-";
        }
        centerStr += centers.get(centers.size() - 1).getX() + "," + centers.get(centers.size() - 1).getY() + "\r\n";
        writeOS("D:/JavaProjects/BootStrapHistoryCalculator/centers0211.txt", centerStr);
    }

    /**
     * 导出带有地块编号的坐标
     * @param blockArrayNew 地块数组
     */
    public void exportCoordinatesWithBlockNum(ArrayList<ArrayList<Coordinate>> blockArrayNew){
        String coordinateList = "";
        for (int i = 0;i < blockArrayNew.size();i++){
            for (int j = 0;j < blockArrayNew.get(i).size();j++){
                coordinateList += blockArrayNew.get(i).get(j).getX() + ","
                        + blockArrayNew.get(i).get(j).getY() + "," + i +"\r\n";//带有地块编号
            }
        }
        writeOS("D:/JavaProjects/BootStrapHistoryCalculator/deviceNoTarget_Coordinate2059.txt", coordinateList);
    }

    public void exportCoordinatesWithBlockNumAndEdge(ArrayList<ArrayList<Coordinate>> blockArrayNew, ArrayList<ArrayList<Coordinate>> eageNew){
        exportCoordinatesWithBlockNum(blockArrayNew);
        String coordinateList = "";
        for (int i = 0;i < eageNew.size();i++){
            for (int j = 0;j < eageNew.get(i).size();j++){
                coordinateList += eageNew.get(i).get(j).getX() + ","
                        + eageNew.get(i).get(j).getY() + "," + 100 +"\r\n";//100表示边缘
            }
        }
        writeOS("D:/JavaProjects/BootStrapHistoryCalculator/deviceNoTarget_Coordinate2059.txt", coordinateList);
    }

    /**
     * 导出带有深度的坐标
     * @param border 轨迹数组
     * @param record 记录
     */
    public void exportCoordinatesWithDepth(ArrayList<Coordinate> border, OperationRecord record){
        String coordinateList = "";
        for (int i = 0;i < border.size();i++){
            coordinateList += border.get(i).getX() + ","
                    + border.get(i).getY() + ","
                    + border.get(i).getDepth() +"\r\n";

        }
        writeOS("D:/JavaProjects/BootStrapHistoryCalculator/tracing_points/"+record.getDeviceId()+".txt", coordinateList);
    }

/**
 * 面积函数群
 */

    /**
     * 网格面积算法
     * @param coordinates 一组XY坐标
     * @return 总面积，合格面积
     */
    public double[] calcBlockArea (List<Coordinate> coordinates) {
        double[] area = new double[2];
        double gridLength = mWorkWidth;
        double gridArea = gridLength * gridLength;
        ArrayList<Coordinate> border = new ArrayList<>();
        border.addAll(coordinates);
        double[] azimuth = GeoUtils.findBorder(border);
        List<Integer> cellDepth = GeoUtils.countInsideArea(azimuth[0],azimuth[1],azimuth[2],azimuth[3],coordinates,gridLength);//方格个数
        int qualifiedNum = 0;
        for (int i = 0;i<cellDepth.size();i++){
            if (cellDepth.get(i)>=standardDepth){
                qualifiedNum++;
            }
        }
        area[0] = gridArea * cellDepth.size();
        area[1] = gridArea * qualifiedNum;
        return area;
    }

    /**
     * 获取分段总面积
     * @param sections 分段数组
     * @return 矩形解析总面积
     */
    public double getSubsectionRectangle(ArrayList<ArrayList<Coordinate>> sections){
        double area = 0;
        for (int i = 0;i < sections.size();i++){
            double areaSingle = calcRectangle(sections.get(i));
            if (areaSingle > 0) {
                area += areaSingle;
            }
        }
        return area;
    }

    /**
     * 获取不重耕分段总面积
     * @param sections 分段坐标点
     * @param borders 边界数组
     * @return 总面积
     */
    public double getTotalUnrepeatedRectangle(ArrayList<ArrayList<Coordinate>> sections, ArrayList<Border> borders){
        double area = 0;
        for (int i = 0;i < sections.size();i++){
            double areaSingle = getSubsectionUnrepeatedRectangle(sections.get(i), i, borders, sections);
//            System.out.println(i + "段矩形面积:" + areaSingle / 666.6666667);
//            double areaIntegral[] = calcBlockArea(sections.get(i));
//            System.out.println(i + "段网格面积:" + areaIntegral[0] / 666.6666667);
            area += areaSingle;
        }
        return area;
    }

    /**
     * 按照时间序列排序
     * @param sections 分段数组
     * @return 排序后的数组
     */
    public ArrayList<Coordinate> sortByTimeSeries(ArrayList<Coordinate> sections){
        Coordinate sectionArray[] = new Coordinate[sections.size()];
        ArrayList<Coordinate> sectionsNew = new ArrayList<>();
        for (int i = 0;i < sections.size();i++){
            sectionArray[i] = sections.get(i);
        }
        for (int i = 0;i < sections.size() - 1;i++){
            for (int j = i + 1;j < sections.size();j++){
                if (sectionArray[j].getTimeSeries()<sectionArray[i].getTimeSeries()){
                    Coordinate temp = sectionArray[i];
                    sectionArray[i] = sectionArray[j];
                    sectionArray[j] = temp;
                }
            }
        }
        for (int i = 0;i < sectionArray.length;i++){
            sectionsNew.add(sectionArray[i]);
        }
        return sectionsNew;
    }

    /**
     * 获取当前段不重耕面积
     * @param section 当前段坐标点
     * @param index 当前段序号
     * @param borders 边界数组
     * @param sections 分段坐标
     * @return 当前段面积
     */
    public double getSubsectionUnrepeatedRectangle(ArrayList<Coordinate> section, int index, ArrayList<Border> borders, ArrayList<ArrayList<Coordinate>> sections){
        for (int i = 0;i < section.size();i++){
            for (int j = 0;j < index;j++){
                if (isInsideBorder(borders.get(j), section.get(i))){
                    for (int k = 0;k < sections.size();k++){
                        if (isInsideRectangle(sections.get(k), section.get(i))){
                            section.remove(section.get(i));
                            break;
                        }
                    }
                }
            }
        }
        return getSingleSubsectionRectangle(section);
    }

    /**
     * 先分段在计算矩形解析总面积
     * @param blockNew 地块数组
     * @return 矩形解析总面积
     */
    public double getTotalSubsectionRectangle(ArrayList<ArrayList<Coordinate>> blockNew){
        double area = 0;
        if (blockNew.size() > 0) {
            for (int i = 0; i < blockNew.size(); i++) {
                double areaSingle = getSingleSubsectionRectangle(blockNew.get(i));
                area += areaSingle;
            }
        }
        return area;
    }

    /**
     * 将当前坐标数组重新分段，获取面积
     * @param section 坐标数组
     * @return 面积
     */
    public double getSingleSubsectionRectangle(ArrayList<Coordinate> section){
        double area = 0;
        if (section.size() > 3) {
            ArrayList<Integer> breakJudges = new ArrayList<>();
            breakJudges.add(-1);
            for (int i = 0; i < section.size() - 1; i++) {
                if (GeoUtils.euclideanDistance(section.get(i), section.get(i + 1)) > 20) {
                    breakJudges.add(i);
                }
            }
            breakJudges.add(section.size() - 1);
            ArrayList<ArrayList<Coordinate>> sectionsNew = new ArrayList<>();
            for (int i = 0; i < breakJudges.size() - 1; i++) {
                ArrayList<Coordinate> sectionNew = new ArrayList<>();
                for (int j = breakJudges.get(i) + 1; j < breakJudges.get(i + 1); j++) {
                    sectionNew.add(section.get(j));
                }
                sectionsNew.add(sectionNew);
            }
//        System.out.println("sectionsNew:" + sectionsNew.size());
            for (int i = 0; i < sectionsNew.size(); i++) {
                double areaSingle = calcRectangle(sectionsNew.get(i));
                if (areaSingle > 0) {
                    area += areaSingle;
                }
            }
        }
        return area;
    }

    /**
     * 判断是否在之前段内的矩形中
     * @param prevCoordinates 待判断的段坐标数组
     * @param coordinate 当前坐标
     * @return 判断结果
     */
    public boolean isInsideRectangle(ArrayList<Coordinate> prevCoordinates, Coordinate coordinate){
        boolean judge = false;
        for (int i = 0;i < prevCoordinates.size() - 1;i++){
            double areaSingle = getAreaFromRectangle(prevCoordinates.get(i), prevCoordinates.get(i+1),coordinate);
            if (areaSingle > 0){
                judge = true;
                break;
            }
        }
        return judge;
    }

    /**
     * 判断是否在段的边界值内部
     * @param border 边界
     * @param coordinate 当前坐标
     * @return 判断结果
     */
    public boolean isInsideBorder(Border border, Coordinate coordinate){
        if (coordinate.getX()>border.getMinX()&&coordinate.getX()<border.getMaxX()&&coordinate.getY()>border.getMinY()&&coordinate.getY()<border.getMaxY()){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 计算矩形解析面积
     * @param coordinates 地块坐标
     * @return 精确面积
     */
    public double calcRectangle(ArrayList<Coordinate> coordinates){
        double areaTotal = 0;
        if (coordinates.size() > 3) {
            boolean judge;
            boolean judgeLast = false;
            for (int i = 2; i < coordinates.size(); i++) {
                judge = false;
                double areaSingle = getAreaFromRectangle(coordinates.get(i - 2), coordinates.get(i - 1), coordinates.get(i));
                for (int j = i - 1; j > 0; j--) {
                    areaSingle = getAreaFromRectangle(coordinates.get(j - 1), coordinates.get(j), coordinates.get(i));
                    if (areaSingle == 0) {
                        judge = true;
                        judgeLast = true;
                        break;
                    }
                }
                if (!judge) {
                    if (!judgeLast) {
                        areaTotal += areaSingle;
                    } else {
                        areaTotal += getAreaFromClosest(coordinates, i);
                    }
                    judgeLast = false;
                }
            }
        }
        return areaTotal;
    }

    /**
     * 使用矩形解析计算相邻点面积
     * @param coordinate1 前一点
     * @param coordinate2 后一点
     * @param coordinate 添加点
     * @return 单个矩形解析面积
     */
    public double getAreaFromRectangle(Coordinate coordinate1, Coordinate coordinate2, Coordinate coordinate){
        double area;
        double dx = Math.sqrt(Math.pow((coordinate1.getX() - coordinate2.getX()), 2) + Math.pow((coordinate1.getY() - coordinate2.getY()), 2));
        double dy = mWorkWidth / 2;
        double dxNew = Math.sqrt(Math.pow((coordinate.getX() - coordinate2.getX()), 2) + Math.pow((coordinate.getY() - coordinate2.getY()), 2));
        double x0 = coordinate1.getX();
        double y0 = coordinate1.getY();
        double sint = (coordinate2.getY()-coordinate1.getY())/dx;
        double cost = (coordinate2.getX()-coordinate1.getX())/dx;
        double xNew;
        double yNew;
        xNew = (coordinate.getY() - y0) * sint + (coordinate.getX() - x0) * cost;
        yNew = (coordinate.getY() - y0) * cost - sint * (coordinate.getX() - x0);
        if (xNew <= dx && xNew >= 0 && yNew >= -0.9*dy && yNew <= 0.9*dy) {
            area = 0;
        } else {
            area = dxNew * mWorkWidth;//面积
        }
        return area;
    }

    /**
     * 从最近点获取矩形解析面积
     * @param coordinates 地块坐标
     * @param num 当前标号
     * @return 单个矩形解析面积
     */
    public double getAreaFromClosest(ArrayList<Coordinate> coordinates, int num){
        double distanceMin = Math.sqrt(Math.pow((coordinates.get(num).getX() - coordinates.get(num - 1).getX()), 2) + Math.pow((coordinates.get(num).getY() - coordinates.get(num - 1).getY()), 2));
        int numberMin = num-1;
        for (int i=num-2;i>=0;i--){
            double distance = Math.sqrt(Math.pow((coordinates.get(num).getX() - coordinates.get(i).getX()), 2) + Math.pow((coordinates.get(num).getY() - coordinates.get(i).getY()), 2));
            if (distanceMin>distance) {
                distanceMin = distance;
                numberMin = i;
            }
        }
        return mWorkWidth*(Math.sqrt(Math.pow((coordinates.get(num).getX() - coordinates.get(numberMin).getX()), 2) + Math.pow((coordinates.get(num).getY() - coordinates.get(numberMin).getY()), 2)));
    }

/**
 * 处理函数群
 */

    /**
     * 获取标准坐标
     * @param points 经纬度数组
     * @return 标准经纬度点
     */
    public Point getStandardPoint(List<Point> points){
        Point pointStandard = new Point();
        for (Point point : points) {
            if (GeoUtils.isInsideChina(point)) {
                pointStandard.setLatitude(point.getLatitude());
                pointStandard.setLongitude(point.getLongitude());
                break;
            }
        }
        return pointStandard;
    }

    /**
     * 利用深度过滤经纬度
     * @param routes 分段后的路段
     * @param settings 存有深浅深度
     * @return 过滤后的路段
     */
    public List<List<Point>> routesFilter(List<List<Point>> routes, OperationSettings settings){
        for (int i = 0;i<routes.size();i++){
            for (int j = 0;j<routes.get(i).size();j++){
                if (routes.get(i).get(j).getDepth()<settings.getLowestDepth()||routes.get(i).get(j).getDepth()>settings.getHighestDepth()){
                    routes.get(i).remove(routes.get(i).get(j));
                }
            }
        }
        for (int i = 0;i < routes.size();i++) {
            if (routes.get(i).size() < 4) {
                routes.remove(i);
            }
        }
        return routes;
    }

    /**
     * 获取两侧插值
     * @param coordinatePrev 前一坐标
     * @param coordinateNext 后一坐标
     * @return 三行插值
     */
    public ArrayList<Coordinate> getBothSides(Coordinate coordinatePrev, Coordinate coordinateNext){
        ArrayList<Coordinate> border = new ArrayList<>();
        double x0 = coordinatePrev.getX();
        double x1 = coordinateNext.getX();
        double y0 = coordinatePrev.getY();
        double y1 = coordinateNext.getY();
        double k = -(x1-x0)/(y1-y0);
        double w = mWorkWidth*mWorkWidth/4;
        double x2 = x0 + Math.pow(w/(k*k+1),0.5);
        double x3 = x0 - Math.pow(w/(k*k+1),0.5);
        double y2 = y0 + k*Math.pow(w/(k*k+1),0.5);
        double y3 = y0 - k*Math.pow(w/(k*k+1),0.5);
        Coordinate coordinate1 = new Coordinate();
        Coordinate coordinate2 = new Coordinate();
        coordinate1.setX(x2);
        coordinate1.setY(y2);
        coordinate1.setLandBlock(coordinatePrev.getLandBlock());
        coordinate1.setDepth(coordinatePrev.getDepth());
        coordinate2.setX(x3);
        coordinate2.setY(y3);
        coordinate2.setLandBlock(coordinatePrev.getLandBlock());
        coordinate2.setDepth(coordinatePrev.getDepth());
        coordinate1.setTimeSeries(coordinatePrev.getTimeSeries());
        coordinate2.setTimeSeries(coordinatePrev.getTimeSeries());
        border.add(coordinate1);
        border.add(coordinatePrev);
        border.add(coordinate2);
        return border;
    }

    /**
     * 获取当前日期字符
     * @return yyyy-MM-dd
     */
    public String getDateString(){
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(currentTime);
    }

    /**
     * 计算边界周长
     * @param borders 边界点数组
     * @return 周长
     */
    public double getPerimeter(ArrayList<Coordinate> borders) {
        double perimeter = 0;
        for (int i = 1;i < borders.size();i++){
            double distance = GeoUtils.euclideanDistance(borders.get(i-1),borders.get(i));
            perimeter += distance;
        }
        return perimeter;
    }

    /**
     * 使用文件流的形式导出综合信息
     * @param fileName 文件名
     * @param message 写入信息
     */
     public void writeOS(String fileName,String message){
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName, true)));
            out.write(message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public double getCenterSpacing(ArrayList<Coordinate> centers) {
        double centerSpacing = 0;//衡量地块间距
        for (int i = 0;i < centers.size()-1;i++){
            for (int j = i+1;j < centers.size();j++){
                centerSpacing += GeoUtils.euclideanDistance(centers.get(i),centers.get(j));
            }
        }
        return centerSpacing;
    }

    public double getTotalDistance(ArrayList<Coordinate> boor) {
        double totalDistance = 0;//行驶路径长度
        for (int i = 0;i<boor.size()-1;i++){
            totalDistance += GeoUtils.euclideanDistance(boor.get(i),boor.get(i+1));
        }
        return totalDistance;
    }

    public double getWorkWidth(OperationRecord record) {
        double mWorkWidth = 320 / 100.0;
//        mWorkWidth = record.getWorkWidth() / 100.0;
        if (mWorkWidth <= 0) {
            mWorkWidth = Double.parseDouble(DotEnv.get("SCAN_SQUARE_LENGTH"));
        }
        return mWorkWidth;
    }

    public List<List<Point>> sliceByPointsWithoutDepth(OperationRecord record, OperationSettings settings) {
        RouteUtils.RouteBreakJudge judge = new RouteUtils.RouteBreakJudge(settings.getMaxDistance());
        List<Point> points = record.getPoints(settings.getLowestDepth(), settings.getHighestDepth());
        judge.Depth(settings.getLowestDepth(), settings.getHighestDepth());
        List<List<Point>> routes = RouteUtils.sliceRoute(points, judge);
        return routesFilter(routes, settings);
    }

    public List<List<Point>> sliceByDistance(OperationRecord record, OperationSettings settings) {
        RouteUtils.RouteBreakJudge judge = new RouteUtils.RouteBreakJudge(settings.getMaxDistance());
        List<Point> points = record.getPoints(settings.getLowestDepth(), settings.getHighestDepth());
        return RouteUtils.sliceRoute(points, judge);
    }

/**
 * 聚类函数群
 */

    /**
     * 地块详细信息
     * @param coordinates 标记坐标
     * @return 按照地块划分的坐标数组
     */
    public ArrayList<ArrayList<Coordinate>> getBlockArray(ArrayList<Coordinate> coordinates){
        ArrayList<ArrayList<Coordinate>> blockArray = new ArrayList<>();
        ArrayList<Coordinate> blockNum = GeoUtils.getDiffBlockCoor(coordinates);
        for (int i = 0;i<blockNum.size();i++){
            ArrayList<Coordinate> single = new ArrayList<>();
            for (int j = 0;j<coordinates.size();j++){
                if (coordinates.get(j).getLandBlock()==blockNum.get(i).getLandBlock()){
                    single.add(coordinates.get(j));
                }
            }
            blockArray.add(single);
        }
        return blockArray;
    }

    /**
     * 获取点个数大于10的地块数组
     * @param blockArray 原聚类地块数组
     * @return 新地块数组
     */
    public ArrayList<ArrayList<Coordinate>> getQualifiedBlockArray(ArrayList<ArrayList<Coordinate>> blockArray){
        ArrayList<ArrayList<Coordinate>> blockArrayNew = new ArrayList<>();
        for (int i = 0;i<blockArray.size();i++){
            if (blockArray.get(i).size()>10){
                blockArrayNew.add(blockArray.get(i));
            }
        }
        return blockArrayNew;
    }

    /**
     * 网格映射后划分地块
     * @param border 一组原始XY坐标
     * @return 划分后的XY坐标
     */
    public ArrayList<Coordinate> gridPartition (ArrayList<Coordinate> border) {
        double[] azimuth = GeoUtils.findBorder(border);
        double west = azimuth[1];
        double south = azimuth[2];
        //单倍划分较慢且过于精细
        double work = 3 * mWorkWidth ;//使用三倍幅宽网格
        //设置相对坐标
        for (int i = 0;i<border.size();i++){
            int relativeColumn = (int)((border.get(i).getX()-west)/work);
            int relativeRow = (int)((border.get(i).getY()-south)/work);
            border.get(i).setColumn(relativeColumn);
            border.get(i).setRow(relativeRow);
        }
        Coordinate[] coor = new Coordinate[border.size()];
        for (int i = 0;i<border.size();i++){
            if (border.get(i).getColumn()==0){
                coor[i] = null;
            }else {
                coor[i] = border.get(i);
            }
        }
        ArrayList<Coordinate> boor = new ArrayList<>();
        for (int i = 0;i<coor.length;i++){
            if (coor[i]!=null){
                boor.add(coor[i]);
            }
        }
        for (int i = 0;i<boor.size()-1;i++){
            int block1 = boor.get(i).getLandBlock();
            for (int j = i+1;j<boor.size();j++){
                int block2 = boor.get(j).getLandBlock();
                if (block1!=block2){
                    double a1 = Math.abs(boor.get(i).getRow()-boor.get(j).getRow());
                    double a2 = Math.abs(boor.get(i).getColumn()-boor.get(j).getColumn());
                    double length = a1 + a2;
                    if (length<=2){
                        changeNum(boor.get(i),boor.get(j),boor);
                    }
                }
            }
        }
        return boor;//返回的函数值包含地块完整信息
    }

    /**
     * 改变所有相关坐标的地块编号
     * @param coordinate1 目标编号
     * @param coordinate2 待改变坐标
     * @param border 一组地块坐标范围
     */
    public void changeNum(Coordinate coordinate1,Coordinate coordinate2,ArrayList<Coordinate> border){
        int block1 = coordinate1.getLandBlock();
        int block2 = coordinate2.getLandBlock();
        for (int i = 0;i<border.size();i++){
            if (border.get(i).getLandBlock()==block2){
                border.get(i).setLandBlock(block1);
            }
        }
    }

    /**
     * 双向扫描确定边缘点坐标和中心坐标点
     * @param block 一组地块坐标
     * @return 边缘点坐标
     */
    public ArrayList<Coordinate> getEage(ArrayList<Coordinate> block){
        ArrayList<Coordinate> eage = new ArrayList<>();
        //行扫描边界
        double maxRow = block.get(0).getRow();
        double minRow = block.get(0).getRow();
        //计算几何中心
        double sumX = 0;
        double sumY = 0;
        for (int i = 0;i<block.size();i++){
            if (block.get(i).getRow()>maxRow){
                maxRow = block.get(i).getRow();
            }
            if (block.get(i).getRow()<minRow){
                minRow = block.get(i).getRow();
            }
            sumX += block.get(i).getX();
            sumY += block.get(i).getY();
        }
        sumX = sumX/block.size();
        sumY = sumY/block.size();
        Coordinate center = new Coordinate(sumX,sumY);
        int maxR = (int) maxRow;
        int minR = (int) minRow;
        ArrayList<Coordinate> east = new ArrayList<>();
        ArrayList<Coordinate> south = new ArrayList<>();
        ArrayList<Coordinate> west = new ArrayList<>();
        ArrayList<Coordinate> north = new ArrayList<>();
        for (int i = minR;i<maxR;i++){
            ArrayList<Coordinate> layer = new ArrayList<>();
            for (int j = 0;j<block.size();j++){
                if (block.get(j).getRow()==i){
                    layer.add(block.get(j));
                }
            }
            Coordinate[] extreme = getExtreme(layer);
            west.add(extreme[0]);
            east.add(extreme[1]);
        }
        //列扫描边界
        double maxColumn = block.get(0).getColumn();
        double minColumn = block.get(0).getColumn();
        for (int i = 0;i<block.size();i++){
            if (block.get(i).getColumn()>maxColumn){
                maxColumn = block.get(i).getColumn();
            }
            if (block.get(i).getColumn()<minColumn){
                minColumn = block.get(i).getColumn();
            }
        }
        int maxC = (int) maxColumn;
        int minC = (int) minColumn;
        for (int i = minC;i<maxC;i++){
            ArrayList<Coordinate> layer = new ArrayList<>();
            for (int j = 0;j<block.size();j++){
                if (block.get(j).getColumn()==i){
                    layer.add(block.get(j));
                }
            }
            Coordinate[] extreme = getExtremeRow(layer);
            north.add(extreme[0]);
            south.add(extreme[1]);
        }
        ArrayList<Coordinate> westNew = new ArrayList<>();
        ArrayList<Coordinate> southNew = new ArrayList<>();
        for (int i = west.size() - 1;i >= 0;i--){
            westNew.add(west.get(i));
        }
        for (int i = south.size() - 1;i >= 0;i--){
            southNew.add(south.get(i));
        }
        //边缘点顺时针排列
        eage.addAll(east);
        eage.addAll(southNew);
        eage.addAll(westNew);
        eage.addAll(north);
        eage.add(center);
        return eage;
    }

    /**
     * 获取每行的边界坐标
     * @param layer 当行坐标
     * @return 行最小最大坐标
     */
    public Coordinate[] getExtreme(ArrayList<Coordinate> layer){
        Coordinate[] extreme = new Coordinate[2];
        if (layer.size()==0){
            extreme[0] = extreme[1] = null;
        }else {
            Coordinate min = layer.get(0);
            Coordinate max = layer.get(0);
            for (int i = 0; i < layer.size(); i++) {
                if (layer.get(i).getColumn() > max.getColumn()) {
                    max = layer.get(i);
                }
                if (layer.get(i).getColumn() < min.getColumn()) {
                    min = layer.get(i);
                }
            }
            min.setEageFlag(1);
            max.setEageFlag(1);
            extreme[0] = min;
            extreme[1] = max;
        }
        return extreme;
    }

    /**
     * 获取每列的边界坐标
     * @param layer 当列坐标
     * @return 列最小最大坐标
     */
    public Coordinate[] getExtremeRow(ArrayList<Coordinate> layer){
        Coordinate[] extreme = new Coordinate[2];
        if (layer.size()==0){
            extreme[0] = extreme[1] = null;
        }else {
            Coordinate min = layer.get(0);
            Coordinate max = layer.get(0);
            for (int i = 0; i < layer.size(); i++) {
                if (layer.get(i).getRow() > max.getRow()) {
                    max = layer.get(i);
                }
                if (layer.get(i).getRow() < min.getRow()) {
                    min = layer.get(i);
                }
            }
            min.setEageFlag(1);
            max.setEageFlag(1);
            extreme[0] = min;
            extreme[1] = max;
        }
        return extreme;
    }

    /**
     * 绘制不同地块、边界点、中心点
     * @param border 地块信息坐标
     * @param eageNew 边界信息坐标
     * @param name 标题，包括设备号和日期
     */
    public void draw(ArrayList<Coordinate> border,ArrayList<ArrayList<Coordinate>> eageNew,String name){
        DrawUtils drawUtils = new DrawUtils(border,eageNew,name);
        drawUtils.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing( WindowEvent event )
                    {System.exit( 0 );}
                }
        );
    }
}