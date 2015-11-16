package com.siat.gps;

import java.util.Date;

/**
 * @author Zhu Yingtao
 * @ClassName GpsData
 * @Description TODO
 * @date 2015年1月29日 下午3:55:30
 */
public class GpsData {
    int altitude;
    double gisX;
    double gisY;
    int goAndOut;
    int mileage;
    int speed;
    String iMei;
    Date locateTime;
    String plateNumber;
    Date recordTime;
    String status;
    String tranStatus;

    Point point;

    public GpsData(int altitude, double gisX, double gisY, int goAndOut,
                   int mileage, int speed, String iMei, Date locateTime,
                   String plateNumber, Date recordTime, String status,
                   String tranStatus) {
        this.altitude = altitude;
        this.gisX = gisX;
        this.gisY = gisY;
        this.goAndOut = goAndOut;
        this.mileage = mileage;
        this.speed = speed;
        this.iMei = iMei;
        this.locateTime = locateTime;
        this.plateNumber = plateNumber;
        this.recordTime = recordTime;
        this.status = status;
        this.tranStatus = tranStatus;
        this.point = new Point(this.gisX, this.gisY);
    }

    @Override
    public boolean equals(Object obj) {
        GpsData data = (GpsData) obj;
        return this.plateNumber.equals(data.plateNumber);
    }

    @Override
    public String toString() {
        return this.plateNumber + "\t" + this.gisX + "\t" + this.gisY;
    }

}
