package com.siat.db;

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
 * @ClassName DBServiceForOracle
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2015年1月12日 下午9:05:27
 */
public class DBServiceForOracle {

	String driver = "oracle.jdbc.driver.OracleDriver";
	String url = "jdbc:oracle:thin:@172.21.5.232:1521:orcl";
	String user = "hw";
	String password = "hw";

	Connection conn = null;
	PreparedStatement pstm = null;

	/**
	 * 
	 */
	public DBServiceForOracle() {
		// TODO Auto-generated constructor stub
		conn = this.getConnection();
	}

	/**
	 * @Title: getConnect
	 * @Description: 连接数据库
	 */
	private Connection getConnection() {
		// TODO Auto-generated method stub
		Connection conn = null;
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
		return conn;
	}

	/**
	 * @Title: executeSQL
	 * @Description: 执行SQL脚本文件
	 * @param sqlPath
	 */
	public void executeSQL() {
		// TODO Auto-generated method stub
		try {
			Statement stm = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS section_speeds"
					+ "(id NUMBER,name VARCHAR2(100),time DATE,"
					+ "direction NUMBER,max_speed NUMBER,min_speed NUMBER,"
					+ "avg_speed NUMBER,num NUMBER)";

			stm.execute(sql);
			ResultSet rs = stm.executeQuery("select * from section_speeds");
			while (rs.next()) {
				System.out.println(rs.getString(0));
			}
		} catch (SQLException e) {
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
			Logger logger = DataLogger.getLogger();
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
		// new DBService().executeSQL("db/create.sql");
		// new DBService().executeSQL("db/load.sql");
		new DBServiceForOracle().executeSQL();
	}
}
