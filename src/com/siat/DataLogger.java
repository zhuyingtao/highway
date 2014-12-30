package com.siat;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @ClassName Logger
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月29日 下午2:39:10
 */
public class DataLogger {

	public static Logger logger = null;

	public static Logger getLogger() {
		if (logger == null) {
			logger = Logger.getLogger(DataLogger.class.getName());
			FileHandler fh = null;
			try {
				fh = new FileHandler("highway.log", true);
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

	/**
	 * @Title: main
	 * @Description: TODO
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
