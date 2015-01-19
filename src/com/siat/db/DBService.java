package com.siat.db;

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
import java.util.logging.Logger;

import com.siat.Configuration;
import com.siat.DataLogger;
import com.siat.RoadSection;
import com.siat.UserData;

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

	public void insertSectionSpeeds(ArrayList<RoadSection> rss, String timeStamp) {
		String sql = "insert into section_speeds values (?,?,?,?,?,?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			for (int i = 0; i < rss.size(); i++) {
				RoadSection rs = rss.get(i);
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

	/**
	 * @Title: selectUserData
	 * @Description: 从数据库中获取数据
	 * @param start
	 * @param end
	 * @return
	 */
	public ArrayList<UserData> selectUserData(String start, String end) {
		// TODO Auto-generated method stub
		ArrayList<UserData> userDatas = new ArrayList<>();
		String sql = "select * from user_data where timestamp >= ? and timestamp < ?";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, start);
			pstm.setString(2, end);

			ResultSet rs = pstm.executeQuery();
			int allNum = 0;
			int filterNum = 0;
			int unusedNum = 0;

			while (rs.next()) {
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
			logger.info("select from database : all-> " + allNum + ", same-> "
					+ filterNum + " , unused->" + unusedNum
					+ " , remaining -> " + (allNum - filterNum - unusedNum));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userDatas;
	}

	public static void main(String[] args) {
		new DBService().executeSQL("db/create.sql");
		// new DBService().executeSQL("db/load.sql");
		// DBService db = new DBService();
	}
}
