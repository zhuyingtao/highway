package com.siat.gps;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Zhu Yingtao
 * @ClassName FileOperation
 * @Description TODO
 * @date 2015年1月29日 下午2:21:45
 */
public class FileOPs {

    DBService db = new DBService();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public ArrayList<GpsData> readFromFile(String filePath) {
        ArrayList<GpsData> gpsData = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (line != null) {
                String[] tokens = line.split(",");
                int altitude = Integer.parseInt(tokens[0]);
                double gisX = Double.parseDouble(tokens[1]);
                double gisY = Double.parseDouble(tokens[2]);
                int goAndOut = Integer.parseInt(tokens[3]);
                int mileage = Integer.parseInt(tokens[4]);
                int speed = Integer.parseInt(tokens[5]);
                String iMei = tokens[6];
                Date locateTime = sdf.parse(tokens[7]);
                String plateNumber = tokens[8];
                Date recordTime = sdf.parse(tokens[tokens.length - 3]);
                String status = tokens[tokens.length - 2];
                String tranStatus = tokens[tokens.length - 1];

                gpsData.add(new GpsData(altitude, gisX, gisY, goAndOut,
                        mileage, speed, iMei, locateTime, plateNumber,
                        recordTime, status, tranStatus));
                // System.out.println(altitude + "\t" + "\t" + locateTime + "\t"
                // + recordTime + "\t" + status + "\t" + tranStatus);
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return gpsData;
    }

    public void scanAllFiles(String dirPath) {
        File f = new File(dirPath);
        if (!f.isDirectory()) {
            System.out.println("must enter a directory name!");
            return;
        }
        String[] fileNames = f.list();
        // System.out.println(Arrays.toString(fileNames));
        for (int i = 0; i < fileNames.length; i++) {
            ArrayList<GpsData> datas = this.readFromFile(dirPath + "/"
                    + fileNames[i]);
            db.insertGpsData(datas);
            // try {
            // db.loadData(dirPath+"/"+fileNames[i]);
            // } catch (SQLException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
        }
    }

    public static void main(String[] args) {
        FileOPs fos = new FileOPs();
        fos.scanAllFiles("I:/zyt/Desktop/0122-0128");
    }
}
