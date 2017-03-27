package cn.edu.hit.gpcs.area.util;

import cn.edu.hit.gpcs.area.model.Point;
import cn.edu.hit.gpcs.utils.DotEnv;

import java.util.ArrayList;
import java.util.List;

/**
 * 路径工具
 */
public class RouteUtils {
    /**
     * 路径断开规则
     */
    public static class RouteBreakJudge {
        private int maxTimeout = -1;
        private int maxDistance = -1;
        private int minDepth;
        private int maxDepth;

        public void Depth(int minDepth, int maxDepth) {
            this.minDepth = minDepth;
            this.maxDepth = maxDepth;
        }

        /**
         * @param maxDistance 最大路径不断开距离(m)
         */
        public RouteBreakJudge (int maxDistance) {
            this.maxDistance = maxDistance;
        }

        /**
         * @param maxTimeout 最大路径不断开时间间隔(m)
         */
        public RouteBreakJudge (int maxDistance, int maxTimeout) {
            this(maxDistance);
            this.maxTimeout = maxTimeout;
        }

        /**
         * 判断前后两点之间是否应该断开:大于最大距离或中间有无深度的点
         * @return boolean
         */
        public boolean shouldBreak (Point a, Point b) {
            if (maxDistance > -1 && GeoUtils.distance(a, b) > maxDistance)
                return true;
//            if (a.getDepth()<minDepth||a.getDepth()>maxDepth){
//                return true;
//            }
//            if (b.getDepth()<minDepth||b.getDepth()>maxDepth){
//                return true;
//            }
//            if (maxTimeout > -1 && Math.abs(a.getGpsTime().getTime() - b.getGpsTime().getTime()) > maxTimeout * 1000)
//                return true;
            return false;
        }
    }

    /**
     * 按照一定规则切分路径
     * @param points 原始点集
     * @param judge 路径断开规则
     * @return 断开后的路径集
     */
    public static List<List<Point>> sliceRoute (List<Point> points, RouteBreakJudge judge) {
        Point lastPoint = null;
        List<Point> currentRoute = new ArrayList<Point>();
        List<List<Point>> routes = new ArrayList<List<Point>>();
        for (Point point : points) {
            if (lastPoint != null && judge.shouldBreak(lastPoint, point)) {
                if (currentRoute.size() > 1)
                    routes.add(currentRoute);
                currentRoute = new ArrayList<>();
            }
            lastPoint = point;
            currentRoute.add(point);
        }
        if (currentRoute.size() > 1) routes.add(currentRoute);
        return routes;
    }

    /**
     * 进行插点
     * @param points 原始点集
     * @return 插点后点集
     */
    public static List<Point> interpolate (List<Point> points) {
        return interpolate(points, Integer.parseInt(DotEnv.get("SCAN_SQUARE_LENGTH")));
    }

    /**
     * @param spacing 插点间距
     */
    public static List<Point> interpolate (List<Point> points, int spacing) {
        List<Point> result = new ArrayList<Point>();
        Point lastPoint = null;
        for (Point point : points) {
            if (lastPoint != null) {
                double distance = GeoUtils.distance(lastPoint, point);
                if (distance > spacing) {
                    int interCount = (int) Math.ceil(distance / spacing);
                    double deltaLat = (point.getLatitude() - lastPoint.getLatitude()) / interCount;
                    double deltaLng = (point.getLongitude() - lastPoint.getLongitude()) / interCount;
                    for (int i = 1; i < interCount; i++) {
                        result.add(
                                new Point(
                                        lastPoint.getGpsTime(),
                                        lastPoint.getLatitude() + i * deltaLat,
                                        lastPoint.getLongitude() + i * deltaLng,
                                        lastPoint.getDepth(),
                                        lastPoint.getWorkWidth()
                                )
                        );
                    }
                }
            }
            lastPoint = point;
            result.add(point);
        }
        return result;
    }
}
