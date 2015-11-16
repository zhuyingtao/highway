package com.siat.msg;

import com.siat.msg.alg.SpeedAlgorithm;
import com.siat.msg.db.DBServiceForOracle;
import com.siat.msg.util.DataLogger;
import com.siat.msg.util.Utility;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Zhu Yingtao
 * @ClassName Server
 * @Description TODO
 * @date 2014年12月23日 上午9:57:03
 */
public class Server {

    DBServiceForOracle db = new DBServiceForOracle();

    public void work() {
        this.clearLog(); // delete the previous log;
        Logger logger = DataLogger.getLogger();
        SpeedAlgorithm sa = new SpeedAlgorithm();
        // initial all the station segments and node segments;
        sa.initSegments();
        while (true) {
            int request = this.checkRequest();
            if (request == 0) {
                // wait 1 second;
                Utility.sleep(1);
            } else if (request == 1) {
                logger.info("=============== History speed ================");
                // String[] strs = { "08", "09", "10", "11", "12", "13", "14",
                // "15", "16", "17", "18" };
                // for (int j = 0; j < strs.length - 1; j++) {
                // String startTime = "2015-02-18 " + strs[j] + ":00:00";
                // String endTime = "2015-02-18 " + strs[j + 1] + ":00:00";
                // sa.computeHistorySpeed(startTime, endTime,
                // Configuration.rate);
                // }
                // break;
                sa.computeHistorySpeed(Configuration.startTime,
                        Configuration.endTime, Configuration.rate);
            } else if (request == 2) {
                sa.computeRealSpeed();
            }
        }
    }

    // check whether there is a new request based on the flag selected from
    // the database;
    // the flag has 3 values:
    // 0, means no new request;
    // 1, means history_speed request;
    // 2, means real_time_speed request;
    public int checkRequest() {
        int flag = 0;
        // flag = db.checkRequest();
        flag = 2;
        return flag;
    }

    public void clearLog() {
        File f = new File(".");
        String[] files = f.list();
        int count = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].matches("(.*)\\.log(.*)")
                    || files[i].matches(".*speeds\\.txt")) {
                File file = new File(files[i]);
                file.delete();
                System.out.println(file.getName() + " deleted ! ");
                count++;
            }
        }
        System.out.println("delete log : " + count);
    }

    /**
     * @param args
     * @Title: main
     * @Description: TODO
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new Server().work();
    }
}
