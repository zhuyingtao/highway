package com.siat.gps;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Zhu Yingtao
 * @ClassName Logger
 * @Description TODO
 * @date 2015年1月29日 下午5:19:25
 */
public class GpsLogger {

    public static Logger logger = null;

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(GpsLogger.class.getName());
            FileHandler fh = null;
            try {
                fh = new FileHandler("log/gps.log", true);
                fh.setFormatter(new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        // TODO Auto-generated method stub
                        return record.getLevel() + "\t" + record.getMessage()
                                + "\n";
                    }
                });
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.addHandler(fh);
        }
        return logger;
    }
}
