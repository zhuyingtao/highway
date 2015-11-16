package com.siat.msg.alg;

import com.siat.ds.UserData;
import com.siat.msg.Configuration;
import com.siat.msg.db.DBServiceForData;
import com.siat.msg.db.DBServiceForOracle;
import com.siat.msg.util.DataLogger;
import com.siat.msg.util.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Zhu Yingtao
 * @ClassName SpeedAlgorithm
 * @Description TODO
 * @date 2015年3月23日 下午10:58:11
 */
public class SpeedAlgorithm {

    private DBServiceForOracle db = null;
    private DBServiceForData userDb = null;
    public StationSegmentSpeed sss = null;
    public NodeSegmentSpeed nss = null;

    private Logger logger = null;
    String timeStamp = null;

    private SimpleDateFormat sdf = null;

    // used for history speed, marked the index of one batch data;
    private int endIndex = 0;

    public SpeedAlgorithm() {
        this.sss = new StationSegmentSpeed();
        this.nss = new NodeSegmentSpeed();
        this.logger = DataLogger.getLogger();
        this.db = new DBServiceForOracle();
        this.userDb = new DBServiceForData();
        this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void initSegments() {
        if (this.sss != null)
            sss.initialStationSegments();
        if (this.nss != null)
            nss.initialNodeSegments();
    }

    /**
     * @param startTime
     * @param endTime
     * @param rate
     * @Title: computeHistorySpeed
     * @Description: compute history speeds by the given start time, end time,
     * and computing rate(the default rate is 1).It will divide
     * the user data selected from database into batches with the
     * specific interval time and computing the station segments
     * speed and node segments speed for every batch. The result
     * has two kind of shows: write to database or write to file.
     */
    public void computeHistorySpeed(String startTime, String endTime, int rate) {
        Date startDate = new Date(); // used for recording computing time;

        this.timeStamp = startTime;
        // set the interval computing time of every batch, if it is not enough,
        // then just wait; if it is exceed, then give a warning;
        int batchInterval = Configuration.INTERVAL_TIME / rate;
        // not to clear here;
        // this.userDataPool = null;

        // 1. get all the history data from databases into memory according to
        // the startTime and endTime;
        List<UserData> allData = this.getHistoryUserData(startTime, endTime);
        // 2. compute the average speed of every batch;
        logger.info("**** All batches begin to compute ... ");

        int startIndex = 0;
        int batchNum = 0;

        while (startIndex < allData.size()) {
            Date date1 = new Date();
            // before every computing, check whether the request has been
            // updated;
            boolean updated = this.checkRequest();
            if (updated) {
                logger.info("New request has coming ... now return ");
                return;
            }
            // if not updated, then begin to compute;
            List<UserData> batchData = this
                    .getOneBatchData(allData, startIndex);
            // update the start time, the time must update before write to
            // database or write to file;
            this.timeStamp = this.updateTime(timeStamp);
            boolean firstCompute = sss.computeAvgSpeed(batchData, timeStamp);
            if (!firstCompute)
                nss.computeAvgSpeed(sss.getStationSegments(), timeStamp);
            startIndex = endIndex;
            batchNum++;

            // clear all the speeds of the station segments for next batch;
            sss.clearSpeeds();

            // check the computing time;
            double time = Utility.intervalTime(date1, new Date()); // s
            if (time > batchInterval) {
                // computing time is exceed;
                logger.severe("'''''''' over time , stand = " + batchInterval
                        + " s , real = " + time + " s");
            } else {
                // computing time is not enough;
                double waitTime = batchInterval - time;
                logger.severe("''''''' wait time , sleep = " + waitTime + " s ");
                Utility.sleep(waitTime);
            }
        }
        // sss.writeToFile();

        logger.info("**** All batches have finished ... count : " + batchNum
                + " , time = " + Utility.intervalTime(startDate, new Date()));
    }

    public void computeRealSpeed() {
        Date now = new Date();
        String startTime = null;
        String endTime = null;
        if (Configuration.START_TIME.equals("null")) {
            endTime = sdf.format(now);
            startTime = sdf.format(new Date(now.getTime()
                    - Configuration.INTERVAL_TIME * 1000));
        } else {
            endTime = Configuration.START_TIME;
            try {
                startTime = sdf.format(new Date(sdf.parse(endTime).getTime()
                        - Configuration.INTERVAL_TIME * 1000));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // set the interval computing time of every batch, if it is not enough,
        // then just wait; if it is exceed, then give a warning;
        int batchInterval = Configuration.INTERVAL_TIME / Configuration.rate;

        // begin to enter the real time cycle;
        while (true) {
            Date startDate = new Date(); // used for recording computing time;
            this.timeStamp = endTime;
            // 1. get all the history data from databases into memory according
            // to the startTime and endTime;
            List<UserData> allData = this
                    .getHistoryUserData(startTime, endTime);
            // 2. compute the average speed of every batch;
            logger.info("**** One batch begin to compute ... ");

            boolean firstCompute = sss.computeAvgSpeed(allData, timeStamp);
            if (!firstCompute)
                nss.computeAvgSpeed(sss.getStationSegments(), timeStamp);

            // clear all the speeds of the station segments for next batch;
            sss.clearSpeeds();

            // check the computing time;
            double time = Utility.intervalTime(startDate, new Date()); // s
            logger.info("**** One batch has finished ... time = " + time
                    + " s ");
            if (time > batchInterval) {
                // computing time is exceed;
                logger.severe("'''''''' over time , stand = " + batchInterval
                        + " s , real = " + time + " s");
            } else {
                // computing time is not enough;
                double waitTime = batchInterval - time;
                logger.severe("''''''' wait time , sleep = " + waitTime + " s ");
                Utility.sleep(waitTime);
            }
            startTime = endTime;
            endTime = this.updateTime(startTime);
        }
    }

    /**
     * @param startTime
     * @param endTime
     * @return the list of history data;
     * @Title: getUserData
     * @Description: get HISTORY data from databases;
     */
    public List<UserData> getHistoryUserData(String startTime, String endTime) {
        List<UserData> userDatas = userDb.selectHistoryUserData(startTime,
                endTime);
        return userDatas;
    }

    /**
     * @param allData
     * @param startIndex
     * @return the list of one batch data,with no same data;
     * @Title: getOneBatchData
     * @Description: split one batch data from the all data.
     */
    public List<UserData> getOneBatchData(List<UserData> allData, int startIndex) {
        UserData start = allData.get(startIndex);
        List<UserData> batchData = new ArrayList<UserData>();
        // find the end index of one batch based on interval time;
        endIndex = startIndex + 1;
        int newAdd = 0; // count the distinct data number;
        int same = 0; // count the same data number;
        while (endIndex < allData.size()) {
            UserData end = allData.get(endIndex);
            long interval = Utility.intervalTime(start.getTimestamp(),
                    end.getTimestamp());
            if (interval > Configuration.INTERVAL_TIME) {
                logger.info("batch time : " + end.getTimestamp() + "----"
                        + start.getTimestamp());
                break;
            }
            // here we use the OLDEST of the same user data; so the
            // speed may be SMALLER than before;
            // <<<<------------------------------->>>>
            // now we use the LATEST again...
            int index = batchData.indexOf(end);
            if (index < 0) {
                batchData.add(end);
                newAdd++;
            } else {
                // if the exist data's time is earlier than now data's, then
                // update it;
                if (batchData.get(index).getTimestamp()
                        .before(end.getTimestamp())) {
                    batchData.set(index, end);
                }
                same++;
            }
            endIndex++;
        }
        logger.info("all data size : " + allData.size() + ", one batch : "
                + startIndex + ", " + endIndex + ", distinct = " + newAdd
                + " , same = " + same);
        return batchData;
    }

    /**
     * @return true if there is a new request, otherwise false;
     * @Title: checkRequest
     * @Description: check whether there is a new request coming before every
     * batch computing;
     */
    public boolean checkRequest() {
        boolean updated = false;
        int flag = db.checkRequest();
        if (flag != 0)
            updated = true;
        return updated;
    }

    /**
     * @param timeStamp
     * @return the new timeStamp
     * @Title: updateTime
     * @Description: update the timeStamp for next batch;
     */
    public String updateTime(String timeStamp) {
        Date start = null;
        try {
            start = sdf.parse(timeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date end = new Date(start.getTime() + 1000
                * Configuration.INTERVAL_TIME);
        String endTime = sdf.format(end);
        return endTime;
    }
}
