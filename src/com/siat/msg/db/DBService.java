package com.siat.msg.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.siat.ds.NodeSegment;
import com.siat.ds.StationSegment;
import com.siat.msg.Configuration;
import com.siat.msg.UserData;
import com.siat.msg.util.DataLogger;

/**
 * @ClassName DBService
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月16日 下午2:52:23
 */
public class DBService {

	String driver = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://localhost:3306/highway";
	String user = "root";
	String password = "123456";

	Connection conn = null;
	PreparedStatement pstm = null;
	Logger logger = null;

	/**
	 * 
	 */
	public DBService() {
		// TODO Auto-generated constructor stub
		this.getConnection();
		this.logger = DataLogger.getLogger();
	}

	/**
	 * @Title: getConnect
	 * @Description: 连接数据库
	 */
	private void getConnection() {
		// TODO Auto-generated method stub
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @Title: executeSQL
	 * @Description: 执行SQL脚本文件
	 * @param sqlPath
	 */
	public void executeSQL(String sqlPath) {
		// TODO Auto-generated method stub
		try {
			Statement stm = conn.createStatement();
			// "source" doesn't work here?
			// stm.execute("source " + sqlPath + ";");

			BufferedReader br = new BufferedReader(new FileReader(new File(
					sqlPath)));
			String sql = "";
			String temp = br.readLine();
			while (temp != null) {
				sql += temp;
				if (sql.endsWith(";")) {
					System.out.println(sql);
					if (!sql.startsWith("#"))
						stm.execute(sql);
					sql = "";
				}
				temp = br.readLine();
			}
			br.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void insertNodeSpeeds(ArrayList<NodeSegment> rss, String timeStamp) {
		String sql = "insert into section_speeds values (?,?,?,?,?,?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			for (int i = 0; i < rss.size(); i++) {
				NodeSegment rs = rss.get(i);
				pstm.setInt(1, rs.getId());
				pstm.setString(2, rs.getSectionName());
				pstm.setString(3, timeStamp);
				pstm.setInt(4, rs.getDirection());
				pstm.setInt(5, rs.getMaxSpeed());
				pstm.setInt(6, rs.getMinSpeed());
				pstm.setInt(7, rs.getAvgSpeed());
				pstm.setInt(8, rs.getSpeedNum());
				pstm.execute();
			}
			logger.info("insert into databases.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertStationSpeeds(ArrayList<StationSegment> rss,
			String timeStamp, int direction) {
		String sql = "insert into station_speeds values (?,?,?,?,?,?,?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			for (int i = 0; i < rss.size(); i++) {
				StationSegment rs = rss.get(i);
				pstm.setInt(1, rs.id);
				pstm.setString(2, rs.getStarts());
				pstm.setString(3, timeStamp);
				pstm.setInt(4, direction);
				pstm.setInt(5, rs.getMaxSpeed());
				pstm.setInt(6, rs.getMinSpeed());
				pstm.setInt(7, rs.getAvgSpeed());
				pstm.setInt(8, rs.getFilterAvgSpeed());
				pstm.setInt(9, rs.getRealNum());
				pstm.execute();
			}
			logger.info("insert into databases.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		// TODO Auto-generated method stub
		Date date1 = new Date();
		logger.info("==== begin to select history data : from " + start
				+ ", to " + end);
		List<UserData> userDatas = new ArrayList<>();
		String sql = "select * from user_data where timestamp >= ? and timestamp < ?";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, start);
			pstm.setString(2, end);
			ResultSet rs = pstm.executeQuery();
			int allNum = 0;
			int unusedNum = 0;
			Date date3 = new Date();
			logger.info("==== select has over, using time = "
					+ (date3.getTime() - date1.getTime()) / 1000);
			while (rs.next()) {
				allNum++;
				String tmsi = rs.getString(1);
				Date timestamp = rs.getTimestamp(2);
				int lac = rs.getInt(3);
				int cellid = rs.getInt(4);
				int eventid = rs.getInt(5);
				int id = rs.getInt(6);
				// filter some unused data ：cellid = unusedId;
				boolean unused = false;
				for (int i = 0; i < Configuration.unusedId.length; i++) {
					if (cellid == Configuration.unusedId[i]) {
						unused = true;
						break;
					}
				}
				if (unused) {
					unusedNum++;
					continue;
				}
				// can't do the duplicate checking, just add directly;
				UserData ud = new UserData(tmsi, timestamp, lac, cellid,
						eventid, id);
				userDatas.add(ud);

			}
			Date date2 = new Date();

			logger.info("==== bulid data structure has over : all-> " + allNum
					+ " , unused->" + unusedNum + " , remaining -> "
					+ (allNum - unusedNum) + ", time -> "
					+ ((date2.getTime() - date1.getTime())) / 1000);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userDatas;
	}

	/**
	 * @Title: selectUserData
	 * @Description: 从数据库中获取数据
	 * @param start
	 * @param end
	 * @return
	 */
	public List<UserData> selectUserData(String start, String end) {
		// TODO Auto-generated method stub
		Date date1 = new Date();
		logger.info("begin to select data : from " + start + ", to " + end);
		List<UserData> userDatas = new ArrayList<>();
		String sql = "select * from user_data where timestamp >= ? and timestamp < ?";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, start);
			pstm.setString(2, end);
			ResultSet rs = pstm.executeQuery();
			int allNum = 0;
			int filterNum = 0;
			int unusedNum = 0;
			Date date3 = new Date();
			logger.info("select has over, using time = "
					+ (date3.getTime() - date1.getTime()) / 1000);
			while (rs.next()) {
				if (allNum % 100000 == 0)
					logger.info("..." + allNum);
				allNum++;
				String tmsi = rs.getString(1);
				Date timestamp = rs.getTimestamp(2);
				int lac = rs.getInt(3);
				int cellid = rs.getInt(4);
				int eventid = rs.getInt(5);
				int id = rs.getInt(6);
				// 过滤掉某些有问题数据：cellid= unusedId;
				boolean unused = false;
				for (int i = 0; i < Configuration.unusedId.length; i++) {
					if (cellid == Configuration.unusedId[i]) {
						unused = true;
						break;
					}
				}
				if (unused) {
					unusedNum++;
					continue;
				}
				// 过滤掉重复的数据
				UserData ud = new UserData(tmsi, timestamp, lac, cellid,
						eventid, id);
				int index = userDatas.indexOf(ud);
				if (index >= 0) {
					filterNum++;
					if (userDatas.get(index).getTimestamp().before(timestamp))
						userDatas.set(index, ud);
				} else {
					userDatas.add(ud);
				}
			}
			Date date2 = new Date();

			logger.info("select from database : all-> " + allNum + ", same-> "
					+ filterNum + " , unused->" + unusedNum
					+ " , remaining -> " + (allNum - filterNum - unusedNum)
					+ ", time -> " + ((date2.getTime() - date1.getTime()))
					/ 1000);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userDatas;
	}

	Statement stm1 = null;
	int count = 0;

	public void loadData(String filePath, boolean end) throws SQLException {
		String sql = "load data local infile '" + filePath
				+ "' into table user_data fields terminated by '\t'";
		if (stm1 == null)
			stm1 = conn.createStatement();
		stm1.execute(sql);
		count++;
		logger.info("load data into database ++ " + filePath + "\t" + count);
	}

	public static void main(String[] args) {
		new DBService().executeSQL("db/create.sql");
		// new DBService().executeSQL("db/load.sql");
		// DBService db = new DBService();
	}
}
