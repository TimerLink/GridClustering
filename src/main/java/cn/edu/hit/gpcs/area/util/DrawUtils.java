package cn.edu.hit.gpcs.area.util;

import cn.edu.hit.gpcs.area.model.base.Coordinate;
import java.awt.*;
import java.util.ArrayList;

public class DrawUtils extends Frame{

    private ArrayList<Coordinate> coordinates = new ArrayList<>();//修改过的坐标点
    private ArrayList<ArrayList<Coordinate>> eageNew = new ArrayList<>();//各地块边缘点

    public DrawUtils(ArrayList<Coordinate> coordinates,ArrayList<ArrayList<Coordinate>> eageNew,String name){
        super(name);
        setVisible(true);
        setSize(800,800);
        for (Coordinate coordinate:coordinates){
            this.coordinates.add(coordinate);
        }
        for (int i = 0;i<eageNew.size();i++){
            this.eageNew.add(eageNew.get(i));
        }
    }

    /**
     * 获取每个地块几何中心
     * @return
     */
    public ArrayList<Coordinate> getDiffCenter(){
        ArrayList<Coordinate> centers = new ArrayList<>();
        for (int i = 0;i<eageNew.size();i++){
            eageNew.get(i).get(eageNew.get(i).size()-1).setLandBlock(i+1);
            centers.add(eageNew.get(i).get(eageNew.get(i).size()-1));
        }
        return centers;
    }
    public void paint(Graphics graphics){
        //边界映射
        double maxX = 0;
        double maxY = 0;
        double minX = 99999999;
        double minY = 99999999;
        for (int i = 0;i<coordinates.size();i++){
            if (coordinates.get(i).getX()>maxX){
                maxX = coordinates.get(i).getX();
            }
            if (coordinates.get(i).getY()>maxY){
                maxY = coordinates.get(i).getY();
            }
            if (coordinates.get(i).getX()<minX){
                minX = coordinates.get(i).getX();
            }
            if (coordinates.get(i).getY()<minY){
                minY = coordinates.get(i).getY();
            }
        }
        double diffY = maxY - minY;
        double diffX = maxX - minX;
        //ForDebug 打印端点个数
//        System.out.println("端点坐标:"+maxX);
//        System.out.println("端点坐标:"+maxY);
//        System.out.println("端点坐标:"+minX);
//        System.out.println("端点坐标:"+minY);
//        System.out.println("Y差值:"+diffY+" X差值:"+diffX);
        double ratioX = diffX/600.0;
        double ratioY = diffY/600.0;
        for (int i = 0;i<coordinates.size();i++){
            double currentX = (coordinates.get(i).getX() - minX)/ratioX;
            double currentY = (coordinates.get(i).getY() - minY)/ratioY;
            coordinates.get(i).setGraphicX(currentX+50);
            coordinates.get(i).setGraphicY(currentY+50);
        }
        //不同的地块用不同的颜色标记，边界点用黑色，几何中心用数字
        for (Coordinate coordinate:coordinates) {
            graphics.drawOval((int) coordinate.getGraphicX(), (int) coordinate.getGraphicY(), 5, 5);
            switch (coordinate.getLandBlock()) {
                case 0:
                    graphics.setColor(Color.blue);
                    break;
                case 1:
                    graphics.setColor(Color.magenta);
                    break;
                case 2:
                    graphics.setColor(Color.red);
                    break;
                case 3:
                    graphics.setColor(Color.green);
                    break;
                case 4:
                    graphics.setColor(Color.gray);
                    break;
                case 5:
                    graphics.setColor(Color.yellow);
                    break;
                default:
                    graphics.setColor(Color.pink);
            }
            graphics.fillOval((int) coordinate.getGraphicX(), (int) coordinate.getGraphicY(), 5, 5);
            if (coordinate.getEageFlag()==1){
                graphics.setColor(Color.black);
                graphics.fillOval((int) coordinate.getGraphicX(), (int) coordinate.getGraphicY(), 10, 10);
            }
        }
        ArrayList<Coordinate> blockNum = getDiffCenter();//取几何中心
//        ArrayList<Coordinate> blockNum = GeoUtils.getDiffBlockCoor(coordinates);//取任意坐标
//        System.out.println("不重复的地块数:"+blockNum.size());
        //设置字体和颜色属性
        Font font = new Font("TimesRoman", Font.BOLD+Font.ITALIC,14);
        graphics.setColor(Color.black);
        graphics.setFont(font);
        ArrayList<Integer> blockInt = new ArrayList<>();
        for (int i = 0;i<blockNum.size();i++){
            Integer integer = blockNum.get(i).getLandBlock();
            blockInt.add(integer);
            double currentX = (blockNum.get(i).getX() - minX)/ratioX;
            double currentY = (blockNum.get(i).getY() - minY)/ratioY;
            blockNum.get(i).setGraphicX(currentX+50);
            blockNum.get(i).setGraphicY(currentY+50);
        }
        //标记每个地块
        for (int i = 0;i<blockNum.size();i++) {
            graphics.drawString(Integer.toString(blockInt.get(i)),(int)blockNum.get(i).getGraphicX(), (int)blockNum.get(i).getGraphicY());
        }
    }
}