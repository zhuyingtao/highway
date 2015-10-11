package com.siat.msg.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.siat.ds.UserData;
import com.siat.msg.Configuration;
import com.siat.msg.util.DataLogger;
import com.siat.msg.util.Utility;

/**
 * @ClassName DBServiceForData
 * @Description This class is used for operating the remote database;
 * @author Zhu Yingtao
 * @date 2015年4月9日 下午8:30:00
 */
public class DBServiceForData {
	String driver = "oracle.jdbc.driver.OracleDriver";
	String url = Configuration.USER_URL;
	String user = Configuration.USER_USER;
	String password = Configuration.USER_PASSWORD;
	String table = Configuration.USER_TABLE;

	static Connection conn = null;
	PreparedStatement pstm = null;
	ResultSet rs = null;
	Logger logger = null;

	public DBServiceForData() {
		if (logger == null)
			logger = DataLogger.getLogger();
		if (conn == null)
			conn = this.getConnection();
	}

	private Connection getConnection() {
		Date date1 = new Date();
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.info("====== get connection of User Database , using time "
				+ (new Date().getTime() - date1.getTime()) + " ms");
		return conn;
	}

	/**
	 * @Title: selectHistoryUserData
	 * @Description: select history user data from database,because history data
	 *               usually very large, so we can't do the duplicate checking
	 *               here;
	 * @param start
	 * @param end
	 * @return
	 */
	public List<UserData> selectHistoryUserData(String start, String end) {
		Date date1 = new Date();
		logger.info("==== begin to select history data : from " + start
				+ ", to " + end);
		List<UserData> userDatas = new ArrayList<>();
		// there are two kinds of method to send date type;
		// here use the index /*+ index(hw_data_user hw_data_user_index1) */
		String sql = "select tmsi,"
				+ "to_char(timestamp,'yyyy-mm-dd hh24:mi:ss'),lac, cellid, eventid,"
				+ " id from "
				+ this.table
				+ " where (timestamp between to_date(?,'yyyy-mm-dd hh24:mi:ss') and "
				+ "to_date(?,'yyyy-mm-dd hh24:mi:ss')) and "
				+ "(recordtime = ? or recordtime = ?) order by timestamp";
		int allNum = 0;
		int unusedNum = 0;
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, start);
			pstm.setString(2, end);
			pstm.setString(3, start.split(" ")[0].replace("-", ""));
			pstm.setString(4, end.split(" ")[0].replace("-", ""));
			rs = pstm.executeQuery();
			logger.info("==== select has over, using time = "
					+ Utility.intervalTime(date1, new Date()) + " s ");

			// Date dd = new Date();
			// FetchData fd = new FetchData(rs, userDatas);
			// Thread[] threads = new Thread[10];
			// for (int i = 0; i < 10; i++) {
			// threads[i] = new Thread(fd, i + "");
			// threads[i].start();
			// }
			// for (int i = 0; i < 10; i++) {
			// try {
			// threads[i].join();
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			// System.out.println("time = "
			// + (new Date().getTime() - dd.getTime()));

			while (rs.next()) {
				allNum++;
				String tmsi = rs.getString(1);
				Date timestamp = rs.getTimestamp(2);
				int lac = rs.getInt(3);
				int cellid = rs.getInt(4);
				int eventid = rs.getInt(5);
				int id = rs.getInt(6);
				// // not to check now , all id has been used;
				// // filter some unused data ：cellid = unusedId;
				// // boolean unused = false;
				// // for (int i = 0; i < Configuration.unused.length; i++) {
				// // if (cellid == Configuration.unused[i]) {
				// // unused = true;
				// // break;
				// // }
				// // }
				// // if (unused) {
				// // unusedNum++;
				// // continue;
				// // }
				//
				// because the data number is usually very large, so we can't do
				// the duplicate checking, just add directly;
				UserData ud = new UserData(tmsi, timestamp, lac, cellid,
						eventid, id);
				userDatas.add(ud);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		logger.info("==== traverse data structure has over : all-> "
				+ userDatas.size() + " , unused->" + unusedNum
				+ " , remaining -> " + (allNum - unusedNum) + ", time -> "
				+ Utility.intervalTime(date1, new Date()) + " s ");
		return userDatas;
	}

	public void close() {
		// close ResultSet;
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close PrepareStatement;
		try {
			if (pstm != null)
				pstm.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
