package com.siat.ds;

import com.siat.msg.Configuration;
import com.siat.msg.util.DataLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Zhu Yingtao
 * @ClassName RoadSegment
 * @Description the segment between Station and Station;
 * @date 2014年12月16日 下午2:54:15
 */
public class StationSegment {

    // an array stored the confidence level of specific sample number; the index
    // represents the sample number;
    private static double[] confidenceLevel = null;

    public int id; // custom id for station segments;
    private int direction;

    private double length; // the length of the segment;
    // Here use a list to store the start stations as there may be different
    // stations in same position, so do the ends;
    ArrayList<Station> starts;
    ArrayList<Station> ends;
    // the IDs of the start stations or the end stations;
    List<Integer> startIds;

    List<Integer> endIds;
    // a series info by computing;
    private int avgSpeed = Configuration.DEFAULT_SPEED; // set default is 80;
    private int filterAvgSpeed = Configuration.DEFAULT_SPEED;
    private int maxSpeed = -1;
    private int minSpeed = 100;
    private int realNum = 0; // the real number in this station segment;
    private int expectedNum = 0; // the expected number in this station segment;

    // a list stored all speeds related to this segment in this batch;
    private List<Integer> speeds = new ArrayList<Integer>();

    private int lastAvgSpeed = Configuration.DEFAULT_SPEED;

    // a list stored all speeds of all batches, just for testing; (time + speed)
    private List<String> avgSpeedStrs = new ArrayList<String>();
    // a list contains the history speed for last 30 minutes
    private double historyAvgSpeed;
    private Queue<Integer> historySpeeds;

    public StationSegment(int id, ArrayList<Station> starts,
                          ArrayList<Station> ends) {
        this.id = id;
        this.starts = starts;
        this.ends = ends;
    }

    public StationSegment(int id, ArrayList<Station> starts, double length) {
        this.id = id;
        this.starts = starts;
        this.setLength(length);
    }

    public StationSegment(int id, double length, List<Integer> startIds,
                          List<Integer> endIds, int direction) {
        this.id = id;
        this.setLength(length);
        this.startIds = startIds;
        this.endIds = endIds;
        this.setDirection(direction);
        this.initHistorySpeed();
        if (confidenceLevel == null) {
            this.initConfidenceLevel();
        }
    }

    public void addExpected() {
        this.expectedNum++;
    }

    public double addIntoHistory(int speed) {
        int size = historySpeeds.size();
        int head = historySpeeds.poll();
        historySpeeds.add(speed);
        // return the new historyAvgSpeed;
        return (historyAvgSpeed * size - head + speed) / size;
    }

    public void addReal() {
        this.realNum++;
    }

    public void addSpeed(int speed) {
        this.speeds.add(speed);
        if (maxSpeed < speed)
            maxSpeed = speed;
        if (minSpeed > speed)
            minSpeed = speed;
    }

    public void clearSpeeds() {
        this.speeds.clear();
        this.realNum = 0;
        this.expectedNum = 0;
        this.lastAvgSpeed = filterAvgSpeed > 100 ? 100 : filterAvgSpeed;
    }

    public void computeAvgSpeed() {
        // if this batch is empty, then just return and maintain the last
        // average speed of this segment;
        if (this.speeds.size() == 0)
            return;

        double sum = 0;
        for (int i = 0; i < this.speeds.size(); i++) {
            sum += this.speeds.get(i);
        }
        this.avgSpeed = (int) (sum / speeds.size());
    }

    public void computeFilterAvgSpeed() {
        if (this.speeds.size() == 0)
            return;

        List<Integer> qualifiedSpeeds = this.getQualifiedData();
        double sum = 0;
        for (int i = 0; i < qualifiedSpeeds.size(); i++) {
            sum += qualifiedSpeeds.get(i);
        }
        if (qualifiedSpeeds.size() > 0)
            this.filterAvgSpeed = (int) (sum / qualifiedSpeeds.size());
    }

    /**
     * @param cellid
     * @return
     * @Title: contains
     * @Description: determine whether the car belongs to this segment. Now we
     * just assert that if the cellId of the data is contained in
     * the startIds, then it belongs to this segment. This may be
     * INACCURATE.
     */
    public boolean contains(int cellid) {
        boolean contains = false;
        for (int i = 0; i < startIds.size(); i++) {
            if (startIds.get(i) == cellid) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public String dumpAvgSpeedStr() {
        StringBuffer sb = new StringBuffer();
        for (String str : this.avgSpeedStrs) {
            sb.append(str + "\n");
        }
        return sb.toString();
    }

    public void genAvgSpeedStr(String timeStr) {
        String str = timeStr + " --> speed = " + Math.round(this.avgSpeed)
                + " & " + Math.round(this.filterAvgSpeed) + " , real = "
                + this.realNum + " , expect = " + this.getSpeeds().size();
        this.avgSpeedStrs.add(str);
    }

    public int getAvgSpeed() {
        // if (this.expectedNum < 3)
        // return this.lastAvgSpeed;
        // if (this.avgSpeed > 100)
        // return 100;
        this.avgSpeed = this.optimizeSpeed(avgSpeed);
        return this.avgSpeed;
    }

    public List<String> getAvgSpeedStrs() {
        return avgSpeedStrs;
    }

    public int getDirection() {
        return direction;
    }

    public int getExpectedNum() {
        return this.expectedNum;
    }

    public int getFilterAvgSpeed() {
        // if (this.expectedNum < 3)
        // return this.lastAvgSpeed;
        // if (this.filterAvgSpeed > 100)
        // return 100;
        this.filterAvgSpeed = this.optimizeSpeed(this.filterAvgSpeed);
        return this.filterAvgSpeed;
    }

    public double getLength() {
        return length;
    }

    public int getMaxSpeed() {
        return this.maxSpeed;
    }

    public int getMinSpeed() {
        return this.minSpeed;
    }

    /**
     * @param avgSpeed
     * @param aveSpeed
     * @return a list of the qualified data
     * @Title: getQualifiedData
     * @Description: get the qualified data based on the Gaussian distribution;
     */
    public List<Integer> getQualifiedData() {
        List<Integer> qualifiedSpeeds = new ArrayList<Integer>();
        double variance = variance(speeds, avgSpeed);
        double svar = Math.sqrt(variance);
        for (int i = 0; i < speeds.size(); i++) {
            if (Math.abs(speeds.get(i) - avgSpeed) <= 3 * svar) {
                qualifiedSpeeds.add(speeds.get(i));
            } else {
                DataLogger.getLogger().fine(
                        "===> Filter one speed : avg = " + this.avgSpeed
                                + " , speed = " + speeds.get(i)
                                + " , variance = " + variance);
            }
        }
        return qualifiedSpeeds;
    }

    public int getRealNum() {
        return this.realNum;
    }

    public List<Integer> getSpeeds() {
        return speeds;
    }

    public String getStarts() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < starts.size(); i++) {
            sb.append(starts.get(i).getCellId() + ",");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public void initConfidenceLevel() {
        confidenceLevel = new double[50];
        for (int i = 0; i < 50; i++) {
            // NOTE : integer to double !!
            confidenceLevel[i] = (i / 5 * 5 + 5) * 1.0 / 50;
        }
    }

    /**
     * @param stations
     * @Title: initEnds
     * @Description: just same as the method 'initStarts()' above;
     */
    public void initEnds(List<Station> stations) {
        this.ends = new ArrayList<>();
        for (int i = 0; i < this.endIds.size(); i++) {
            int id = endIds.get(i);
            for (int j = 0; j < stations.size(); j++) {
                if (stations.get(j).getCellId() == id) {
                    this.ends.add(stations.get(j));
                    break;
                }
            }
        }
    }

    public void initHistorySpeed() {
        historySpeeds = new LinkedList<Integer>();
        // contain some last speeds, initializing with the default speed;
        for (int i = 0; i < 7; i++) {
            historySpeeds.add(Configuration.DEFAULT_SPEED);
        }
        historyAvgSpeed = Configuration.DEFAULT_SPEED;
    }

    /**
     * @param stations
     * @Title: initStarts
     * @Description: After select data from database, the StationSegment and
     * Station are still unlinked (because the StationSegment only
     * selected the Station id, not object), this method is used
     * to linked them;
     */
    public void initStarts(List<Station> stations) {
        this.starts = new ArrayList<>();
        for (int i = 0; i < this.startIds.size(); i++) {
            int id = startIds.get(i);
            for (int j = 0; j < stations.size(); j++) {
                if (stations.get(j).getCellId() == id) {
                    this.starts.add(stations.get(j));
                    break;
                }
            }
        }
    }

    /**
     * @param speed
     * @return
     * @Title: optimizeSpeed
     * @Description: this method does some optimization for the average speed.
     * We do confidence examination and segment limit examination
     * here, if the speed beyond the limit, then it should be
     * changed.
     */
    public int optimizeSpeed(int speed) {
        int num = this.expectedNum;
        double avg = this.historyAvgSpeed;
        if (num == 0) {
            // if the sample number is 0, then just use the last average speed;
            speed = this.lastAvgSpeed;
        } else if (num >= confidenceLevel.length) {
            // if the sample number is large enough, then we consider the
            // average speed believable;
        } else {
            // if the sample number is not large enough, then we should do some
            // optimization;
            double diff = Math.abs((speed - avg) * 1.0 / avg);
            if (diff > confidenceLevel[num]) {
                // if the difference beyond the confidence level's limit, then
                // change(increase or decrease) the speed to adapt to the
                // limitation;
                int lower = (int) (avg * (1 - confidenceLevel[num]));
                int upper = (int) (avg * (1 + confidenceLevel[num]));
                DataLogger.getLogger().fine(
                        "----- optimize speed ---> diff : " + diff
                                + ", stand = " + confidenceLevel[num]
                                + ", speed = " + speed + ", avg = " + avg);
                if (speed < lower)
                    speed = lower;
                else if (speed > upper)
                    speed = upper;

            }
        }
        // if the speed beyond the limit of the segment, then change it to the
        // limit speed;
        if (speed > Configuration.LIMIT_SEGMENT_SPEED)
            speed = Configuration.LIMIT_SEGMENT_SPEED;
        this.historyAvgSpeed = this.addIntoHistory(speed);
        return speed;
    }

    public void setAvgSpeedStrs(List<String> avgSpeedStrs) {
        this.avgSpeedStrs = avgSpeedStrs;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setLength(double length) {
        this.length = length;
    }

    /**
     * @param speed
     * @param aveSpeed
     * @return the variance
     * @Title: variance
     * @Description: compute the variance of all the speeds in the speed list;
     */
    public double variance(List<Integer> speed, double aveSpeed) {
        double sum = 0;
        for (int i = 0; i < speed.size(); i++)
            sum += Math.pow((speed.get(i) - aveSpeed), 2);
        sum = sum / speed.size();
        return sum;
    }
}
