package com.siat.msg.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.siat.ds.Node;
import com.siat.ds.NodeSegment;
import com.siat.ds.Station;
import com.siat.ds.StationSegment;
import com.siat.ds.UserData;
import com.siat.msg.Configuration;
import com.siat.msg.util.DataLogger;
import com.siat.msg.util.Utility;

/**
 * @ClassName DBServiceForOracle
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2015年1月12日 下午9:05:27
 */
public class DBServiceForOracle extends Object {

	String driver = "oracle.jdbc.driver.OracleDriver";
	String url = null;
	String user = null;
	String password = null;

	static Connection conn = null;
	PreparedStatement pstm = null;
	ResultSet rs = null;
	Logger logger = null;

	/**
	 * 
	 */
	public DBServiceForOracle() {
		if (logger == null)
			logger = DataLogger.getLogger();
		this.initDatabase();
		if (conn == null)
			conn = this.getConnection();
	}

	/**
	 * @Title: getConnect
	 * @Description: get the connection of database;
	 * @return: the reference of the connection;
	 */
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
		logger.info("====== get connection , using time "
				+ (new Date().getTime() - date1.getTime()) + " ms");
		return conn;
	}

	/**
	 * @Title: executeSQL
	 * @Description: execute some SQL code;
	 * @param sqlPath
	 */
	public void executeSQL() {
		try {
			Statement stm = conn.createStatement();

			ResultSet rs = stm.executeQuery("select * from hw_road_node");
			while (rs.next()) {
				System.out.println(rs.getString(1) + " , " + rs.getString(2)
						+ " , " + rs.getString(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
	}

	public List<NodeSegment> selectNodeSegment() {
		Date date1 = new Date();
		logger.info("==== begin to select node segment ... ");
		String sql = "select id, direction, start_road_node, end_road_node, length from "
				+ "hw_road_node_segment order by id";
		List<NodeSegment> list = new ArrayList<NodeSegment>();

		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				int direction = rs.getInt(2);
				int startNode = rs.getInt(3);
				int endNode = rs.getInt(4);
				double length = rs.getDouble(5);
				NodeSegment ns = new NodeSegment(id, startNode, endNode,
						direction, length);
				list.add(ns);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		Date date2 = new Date();
		logger.info("==== end of select, count = " + list.size() + " , time = "
				+ Utility.intervalTime(date1, date2));
		return list;
	}

	public List<Node> selectNode() {
		Date date1 = new Date();
		logger.info("==== begin to select node ... ");
		String sql = "select id, direction, cell_id, length from "
				+ "hw_road_node";
		List<Node> list = new ArrayList<Node>();

		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				int direction = rs.getInt(2);
				int cellId = rs.getInt(3);
				double length = rs.getDouble(4);
				Node node = new Node(id, cellId, length, direction);
				list.add(node);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		Date date2 = new Date();
		logger.info("==== end of select, count = " + list.size() + " , time = "
				+ Utility.intervalTime(date1, date2));
		return list;
	}

	public List<Station> selectStation() {
		Date date1 = new Date();
		logger.info("==== begin to select station ... ");
		String sql = "select cell_id, lac_id, longitude, latitude from "
				+ "hw_station where serial_number = ? ";
		List<Station> list = new ArrayList<Station>();

		try {
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, 6); // select the stations in line 6 (福银高速);
			rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				int lacId = rs.getInt(2);
				double longitude = rs.getDouble(3);
				double latitude = rs.getDouble(3);
				Station s = new Station(id, lacId, longitude, latitude);
				list.add(s);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		Date date2 = new Date();
		logger.info("==== end of select, count = " + list.size() + " , time = "
				+ Utility.intervalTime(date1, date2));
		return list;
	}

	public List<StationSegment> selectStationSegment() {
		Date date1 = new Date();
		logger.info("==== begin to select station segment... ");
		String sql = "select id, roadLength, direction, cell_id_start, cell_id_end from "
				+ "hw_station_segment";
		List<StationSegment> list = new ArrayList<StationSegment>();

		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				double length = rs.getDouble(2);
				int direction = rs.getInt(3);
				// parse start string to integer array;
				String[] starts = rs.getString(4).split(";");
				List<Integer> startIds = new ArrayList<Integer>();
				for (int i = 0; i < starts.length; i++) {
					startIds.add(Integer.parseInt(starts[i]));
				}
				// parse end string to integer array;
				String[] ends = rs.getString(5).split(";");
				List<Integer> endIds = new ArrayList<Integer>();
				for (int i = 0; i < ends.length; i++) {
					endIds.add(Integer.parseInt(ends[i]));
				}
				StationSegment ss = new StationSegment(id, length, startIds,
						endIds, direction);
				list.add(ss);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		Date date2 = new Date();
		logger.info("==== end of select, count = " + list.size() + " , time = "
				+ Utility.intervalTime(date1, date2));
		return list;
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
				+ Configuration.USER_TABLE
				+ " where timestamp between to_date(?,'yyyy-mm-dd hh24:mi:ss') and "
				+ "to_date(?,'yyyy-mm-dd hh24:mi:ss') order by timestamp";
		int allNum = 0;
		int unusedNum = 0;
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, start);
			pstm.setString(2, end);
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

	/**
	 * @Title: insertStationSpeeds
	 * @Description: insert station speeds into database;
	 * @param ss
	 * @param timeStamp
	 * @param direction
	 */
	public void insertStationSpeeds(List<StationSegment> ss, String timeStamp) {
		// before this insertion, we should check whether the data of same time
		// has ever been inserted;
		Date date1 = new Date();
		String sql = "insert into hw_station_segment_speed (id,time,filter_speed)"
				+ " values (?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			for (int i = 0; i < ss.size(); i++) {
				StationSegment rs = ss.get(i);
				pstm.setInt(1, rs.id);
				pstm.setTimestamp(2,
						Timestamp.valueOf(timeStamp.replace('/', '-')));
				// pstm.setInt(3, rs.getMaxSpeed());
				// pstm.setInt(4, rs.getMinSpeed());
				// pstm.setInt(5, rs.getAvgSpeed());
				pstm.setInt(3, rs.getFilterAvgSpeed());
				// pstm.setInt(7, rs.getRealNum());
				// pstm.setInt(8, rs.getExpectedNum());
				// here use batch to improve insertion efficiency, its effect is
				// obvious;
				pstm.addBatch();
			}
			// execute the sqls all by one;
			pstm.executeBatch();
			logger.info(" ==== insert StationSpeed into databases, count = "
					+ ss.size() + ", time = "
					+ (new Date().getTime() - date1.getTime()) + " ms");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.info(" ==== duplicated ! the data of time = " + timeStamp
					+ " has been insert into the hw_station_segment_speed !");
		} finally {
			// here, I must say that we should keep a good programming
			// habit,such as using the keyword 'finally' and close the
			// connection after using. I have encountered an exception like:
			// ORA-01000: maximum open cursors exceeded. After several times
			// bugging, I found that the reason was that the code caught an
			// exception(I have shielded the exception!) and entered the
			// exception section, then the prepareStatment and resultSet had not
			// been closed! Time after time, the program crashed... So, a good
			// programming habit is very important!
			this.close();
		}
	}

	/**
	 * @Title: insertNodeSpeeds
	 * @Description: insert node speeds into database;
	 * @param ns
	 * @param timeStamp
	 */
	public void insertNodeSpeeds(List<NodeSegment> nss, String timeStamp) {
		// before this insertion, we should check whether the data of same time
		// has ever been inserted;
		Date date1 = new Date();
		String sql = "insert into hw_road_node_segment_speed (id,time,avg_speed)"
				+ " values (?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			for (int i = 0; i < nss.size(); i++) {
				NodeSegment ns = nss.get(i);
				pstm.setInt(1, ns.getId());
				pstm.setTimestamp(2,
						Timestamp.valueOf(timeStamp.replace('/', '-')));
				// pstm.setInt(3, ns.getMaxSpeed());
				// pstm.setInt(4, ns.getMinSpeed());
				pstm.setInt(3, ns.getAvgSpeed());
				// pstm.setInt(6, ns.getRealNum());
				// pstm.setInt(7, ns.getExpectedNum());
				// here use batch to improve insertion efficiency, its effect is
				// obvious;
				pstm.addBatch();
			}
			// execute the sqls all by one;
			pstm.executeBatch();
			logger.info(" ==== insert NodeSegmentSpeed into databases, count = "
					+ nss.size()
					+ " , time = "
					+ (new Date().getTime() - date1.getTime()) + " ms");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.info(" ==== duplicated , the data of time = " + timeStamp
					+ " has been " + "insert into the hw_node_segment_speed !");
		} finally {
			this.close();
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

			rs = pstm.executeQuery();
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
		} finally {
			this.close();
		}
		return userDatas;
	}

	public int checkRequest() {
		int flag = 0;
		String sql = "select to_char(startTime,'yyyy-mm-dd hh24:mi:ss'),"
				+ "to_char(endTime,'yyyy-mm-dd hh24:mi:ss'),"
				+ "to_char(setTime,'yyyy-mm-dd hh24:mi:ss'), interval "
				+ " from hw_para_time_set";
		try {
			// count the record number,if it > 1, then may be something error;
			int count = 0;
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				count++;
				String setTime = rs.getString(3);
				// if setTime is not changed, that means there is no new
				// request;
				if (setTime == null)
					continue;
				if (setTime.equals(Configuration.setTime)) {
					logger.info("==== set time is " + setTime
							+ " , equals configuration , count = " + count);
					break;
				}
				// if setTime is changed, then update the related variables;
				logger.info("==== set time has been updated, begin to compute, count = "
						+ count);
				Configuration.setTime = setTime;
				Configuration.startTime = rs.getString(1);
				Configuration.endTime = rs.getString(2);
				Configuration.rate = rs.getInt(4);
				flag = 1;
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			this.close();
		}
		return flag;
	}

	public void updateTime(String time) {
		String sql = "update hw_para_time_set set updatetime = ? ";
		// + "where settime = ?";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setTimestamp(1, Timestamp.valueOf(time.replace('/', '-')));
			// pstm.setTimestamp(2,
			// Timestamp.valueOf(Configuration.setTime.replace('/', '-')));
			pstm.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.close();
		}
	}

	public void insertTime(String startTime, String endTime, int rate,
			String setTime, String updateTime) {
		String sql = "insert into hw_para_time_set values(?,?,?,?,?)";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setTimestamp(1, Timestamp.valueOf(startTime.replace('/', '-')));
			pstm.setTimestamp(2, Timestamp.valueOf(endTime.replace('/', '-')));
			pstm.setTimestamp(4, Timestamp.valueOf(setTime.replace('/', '-')));
			pstm.setTimestamp(5,
					Timestamp.valueOf(updateTime.replace('/', '-')));
			pstm.setInt(3, 1);
			pstm.execute();
			System.out.println("Time has been initialized ! ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.close();
		}
	}

	/**
	 * @Title: close
	 * @Description: close the connection of ResultSet, PrepareStatement, and so
	 *               on;
	 */
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

	public static void main(String[] args) {
		new DBServiceForOracle().selectHistoryUserData("2015-02-18 14:00:00",
				"2015-02-18 15:00:00");
	}

	public void writeSpecificData(List<UserData> uds) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"used_data.txt", true));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < uds.size(); i++) {
				UserData ud = uds.get(i);
				sb.append(ud.getTmsi() + "\t" + ud.getTimestamp() + "\t"
						+ ud.getLac() + "\t" + ud.getCellid() + "\t"
						+ ud.getEventid() + "\t" + ud.getId() + "\n");
			}
			bw.write(sb.toString() + "\n\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void selectSpecificData() {
		String start = "2015-02-18 00:00:00";
		String end = "2015-02-18 00:30:00";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			for (int i = 0; i < 48; i++) {
				start = sdf
						.format((sdf.parse(start).getTime() + i * 30 * 60 * 1000));
				end = sdf
						.format((sdf.parse(end).getTime() + i * 30 * 60 * 1000));
				String sql = "select count(*) from hw_data_user_lost where timestamp between to_date()";
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	class FetchData implements Runnable {

		ResultSet rs = null;
		List<UserData> list = null;
		int count = 0;

		public FetchData(ResultSet rs, List<UserData> list) {
			// TODO Auto-generated constructor stub
			this.rs = rs;
			this.list = list;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				System.out.println("thread start "
						+ Thread.currentThread().getName());
				while (rs.next()) {
					count++;
					if (count % 1000 == 0)
						System.out.println(Thread.currentThread().getName()
								+ " --> " + count);
					String tmsi = rs.getString(1);
					Date timestamp = rs.getTimestamp(2);
					int lac = rs.getInt(3);
					int cellid = rs.getInt(4);
					int eventid = rs.getInt(5);
					int id = rs.getInt(6);

					UserData ud = new UserData(tmsi, timestamp, lac, cellid,
							eventid, id);
					list.add(ud);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void initDatabase() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					Configuration.Configuration_Path));
			this.url = br.readLine().split("=")[1].trim();
			this.user = br.readLine().split("=")[1].trim();
			this.password = br.readLine().split("=")[1].trim();

			Configuration.USER_URL = br.readLine().split("=")[1].trim();
			Configuration.USER_USER = br.readLine().split("=")[1].trim();
			Configuration.USER_PASSWORD = br.readLine().split("=")[1].trim();
			Configuration.USER_TABLE = br.readLine().split("=")[1].trim();
			Configuration.START_TIME = br.readLine().split("=")[1].trim();
			Configuration.INTERVAL_TIME = Integer.parseInt(br.readLine().split(
					"=")[1].trim());
			Configuration.rate = Integer.parseInt(br.readLine().split("=")[1]
					.trim());
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
