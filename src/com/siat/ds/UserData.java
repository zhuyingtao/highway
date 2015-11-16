package com.siat.ds;

import java.util.Date;
import java.util.HashMap;

/**
 * @author Zhu Yingtao
 * @ClassName UserData
 * @Description the information of user data;
 * @date 2014年12月16日 下午2:55:04
 */
public class UserData {

    private String tmsi; // 唯一标识手机
    private Date timestamp; // 时间戳
    private int lac; // 位置区编号
    private int cellid; // 基站编码
    private int eventid; // 事件类型
    private int id; // 线路ID

    public UserData(String tmsi) {
        // TODO Auto-generated constructor stub
        this.tmsi = tmsi;
    }

    public UserData(String tmsi, Date timestamp, int lac, int cellid,
                    int eventid, int id) {
        this.tmsi = tmsi;
        this.timestamp = timestamp;
        this.lac = lac;
        this.cellid = cellid;
        this.eventid = eventid;
        this.id = id;
    }

    public String getTmsi() {
        return tmsi;
    }

    public void setTmsi(String tmsi) {
        this.tmsi = tmsi;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCellid() {
        return cellid;
    }

    public void setCellid(int cellid) {
        this.cellid = cellid;
    }

    public int getEventid() {
        return eventid;
    }

    public void setEventid(int eventid) {
        this.eventid = eventid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        return this.tmsi.equals(((UserData) obj).tmsi);
    }

    @Override
    public int hashCode() {
        return this.tmsi.hashCode();
    }

    @Override
    public String toString() {
        return this.tmsi + " , " + this.timestamp + " , " + this.lac + " , "
                + this.cellid + " , " + this.eventid + " , " + this.id;
    }

    public boolean isLater(UserData ud) {
        boolean isLater = false;
        if (this.timestamp.after(ud.timestamp))
            isLater = true;
        return isLater;
    }

    public static void main(String[] args) {
        // just test the hash map;
        String a = "4987986B6F3A9C3A1D7354B175B052F7";
        String b = "4987D6ECDBEFC89426D9C847921E2462";
        String c = "4987986B6F3A9C3A1D7354B175B052F7";
        System.out.println(a.hashCode() + "  ; " + b.hashCode() + " ; "
                + c.hashCode());
        HashMap<String, UserData> map = new HashMap<>();
        map.put("123", new UserData("123"));
        map.put("123", new UserData("123"));
        map.put("322", new UserData("322"));
        System.out.println(map.containsKey("123"));
        System.out.println(map.size());
        map.put("322", new UserData("222"));
        System.out.println(map);
    }
}
