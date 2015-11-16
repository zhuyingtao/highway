package com.siat.gps;

import java.util.Date;

/**
 * @author Z.Y.T
 *         <p/>
 *         2014年10月27日上午10:35:15
 */
public class Car implements Comparable<Car> {
    int id;
    String number;
    Date date; // car's saving date
    Point point; // car's location
    double speed;
    int direction; // 0 is forwards, and 1 is backwards

    int roadNodeId; // the roadNode that the car belongs to

    public Car(int id, String number, Date date, Point point, double speed,
               int direction) {
        this.id = id;
        this.number = number;
        this.date = date;
        this.point = point;
        this.speed = speed;
        this.direction = direction;

    }

    public Car(String number, Date date) {
        this.number = number;
        this.date = date;
    }

    // Note: it is seemed that in TreeSet,if you want to determine the equal of
    // two objects, you should write the equal discipline in the override method
    // compare().If you override the method equals() as below,it won't work.
    @Override
    public boolean equals(Object obj) {
        if (this.number.equals(((Car) obj).number))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        return this.number.hashCode();
    }

    @Override
    public int compareTo(Car o) {
        return this.date.before(o.date) ? -1 : 1;
    }

    /**
     * @return
     * @Title: getRad
     * @Description: transform value from 180 to PI;
     */
    public double getRad() {
        if (this.speed > 0)
            return this.direction / 180.0 * Math.PI;
        else
            return 0;
    }

    @Override
    public String toString() {
        // return this.number + " " + this.date;
        return this.number + " " + this.roadNodeId;
    }

    public int getRoadNodeId() {
        return roadNodeId;
    }

    public void setRoadNodeId(int roadNodeId) {
        this.roadNodeId = roadNodeId;
    }
}