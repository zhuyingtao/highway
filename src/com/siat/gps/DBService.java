package com.siat.gps;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author Zhu Yingtao
 * @ClassName DBServiceForMySQL
 * @Description TODO
 * @date 2015年1月29日 下午3:17:49
 */
public class DBService {

    String driver = "com.mysql.jdbc.Driver";
    String url = "jdbc:mysql://localhost:3306/highway";
    String user = "root";
    String password = "123456";

    Connection conn = null;
    PreparedStatement pstm = null;
    Logger logger = null;

    public DBService() {
        this.getConnection();
        this.logger = GpsLogger.getLogger();
    }

    /**
     * @Title: getConnect
     * @Description: 连接数据库
     */
    private void getConnection() {
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param sqlPath
     * @Title: executeSQL
     * @Description: 执行SQL脚本文件
     */
    public void executeSQL(String sqlPath) {
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

    int count = 0;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void insertGpsData(ArrayList<GpsData> data) {
        String sql = "insert into gps_data values (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            pstm = conn.prepareStatement(sql);
            for (int i = 0; i < data.size(); i++) {
                GpsData gps = data.get(i);
                pstm.setInt(1, gps.altitude);
                pstm.setDouble(2, gps.gisX);
                pstm.setDouble(3, gps.gisY);
                pstm.setInt(4, gps.goAndOut);
                pstm.setInt(5, gps.speed);
                pstm.setInt(6, gps.mileage);
                pstm.setString(7, gps.iMei);
                pstm.setString(8, sdf.format(gps.locateTime));
                pstm.setString(9, gps.plateNumber);
                pstm.setString(10, sdf.format(gps.recordTime));
                pstm.setString(11, gps.status);
                pstm.setString(12, gps.tranStatus);
                pstm.execute();
                count++;
            }
            logger.info("insert into databases. ==> " + count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param start
     * @param end
     * @return
     * @Title: selectUserData
     * @Description: 从数据库中获取数据
     */
    public ArrayList<GpsData> selectGpsData(String start, String end) {
        ArrayList<GpsData> gpsDatas = new ArrayList<>();
        String sql = "select * from gps_data where locateTime >= ? and locateTime < ?";
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, start);
            pstm.setString(2, end);

            ResultSet rs = pstm.executeQuery();
            int allNum = 0;
            int filterNum = 0;
            while (rs.next()) {
                allNum++;
                int altitude = rs.getInt(1);
                double gisX = rs.getDouble(2);
                double gisY = rs.getDouble(3);
                int goAndOut = rs.getInt(4);
                int mileage = rs.getInt(5);
                int speed = rs.getInt(6);
                String iMei = rs.getString(7);
                Date locateTime = rs.getTimestamp(8);
                String plateNumber = rs.getString(9);
                Date recordTime = rs.getTimestamp(10);
                String status = rs.getString(11);
                String tranStatus = rs.getString(12);

                GpsData gps = new GpsData(altitude, gisX, gisY, goAndOut,
                        mileage, speed, iMei, locateTime, plateNumber,
                        recordTime, status, tranStatus);

                // 过滤掉重复的数据
                int index = gpsDatas.indexOf(gps);
                if (index >= 0) {
                    filterNum++;
                    if (gpsDatas.get(index).locateTime.before(locateTime))
                        gpsDatas.set(index, gps);
                } else {
                    gpsDatas.add(gps);
                }
                gpsDatas.add(gps);
            }
            logger.info("select from database : all-> " + allNum + ", same-> "
                    + filterNum + " , remaining -> " + (allNum - filterNum));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gpsDatas;
    }

    Statement stm1 = null;
    int count2 = 0;

    public void loadData(String filePath) throws SQLException {
        String sql = "load data local infile '" + filePath
                + "' into table gps_data2 fields terminated by ','";
        if (stm1 == null)
            stm1 = conn.createStatement();
        stm1.execute(sql);
        count++;
        logger.info("load data into database ++ " + filePath + "\t" + count2);
    }

    public static void main(String[] args) {
        // new DBServiceForMySQL().executeSQL("db/create.sql");
        // new DBServiceForMySQL().executeSQL("db/load.sql");
        DBService db = new DBService();
        System.out.println(db.selectGpsData("2015-01-22 23:55:23",
                "2015-01-23 00:00:00"));
    }
}
