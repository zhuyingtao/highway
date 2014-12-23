package com.siat;

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

/**
 * 
 */

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

	/**
	 * 
	 */
	public DBService() {
		// TODO Auto-generated constructor stub
		this.getConnection();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @Title: executeSQL
	 * @Description: TODO
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
			while (rs.next()) {
				String tmsi = rs.getString(1);
				Date timestamp = rs.getTimestamp(2);
				int lac = rs.getInt(3);
				int cellid = rs.getInt(4);
				int eventid = rs.getInt(5);
				int id = rs.getInt(6);
				UserData ud = new UserData(tmsi, timestamp, lac, cellid,
						eventid, id);
				int index = userDatas.indexOf(ud);
				if (index >= 0) {
					System.out.println("contains same car in one batch --> "
							+ tmsi + " @ " + timestamp + " @ " + cellid);
					if (userDatas.get(index).getTimestamp().before(timestamp))
						userDatas.set(index, ud);
				} else
					userDatas.add(ud);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userDatas;
	}

	public static void main(String[] args) {
		// new DBService().executeSQL("db/create.sql");
		// new DBService().executeSQL("db/load.sql");
		// DBService db = new DBService();
	}
}
