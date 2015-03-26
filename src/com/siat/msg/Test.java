/**
 * 
 */
package com.siat.msg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName Test
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月17日 上午10:17:54
 */
public class Test {

	public static Date getEarliestDate(File file) {
		Date earliest = new Date();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String line = br.readLine();
			while (line != null) {
				String[] parts = line.split(",");
				String dateStr = parts[1];
				Date tempDate = df.parse(dateStr);
				if (tempDate.before(earliest))
					earliest = tempDate;
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return earliest;
	}

	public static void getDayData(String startTime, String endTime) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"I:/tb_seu_freeway_route_list_201502.txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"I:/day_0218.csv"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date start = sdf.parse(startTime);
			Date end = sdf.parse(endTime);
			StringBuffer sb = new StringBuffer();

			String line = br.readLine();
			int count = 0;
			int lineCount = 0;
			while (line != null) {
				lineCount++;
				if (lineCount % 100000 == 0)
					System.out.println(lineCount + " has finished!");
				String[] tokens = line.split("\t");
				String time = tokens[1];
				Date t = sdf.parse(time);
				if (t.after(start) && t.before(end)) {
					line = line.replace('\t', ',');
					sb.append(line + "\n");
					count++;
					System.out.println(count + " : " + line);
				}
				if (count % 10000 == 0) {
					bw.write(sb.toString());
					bw.flush();
					sb = new StringBuffer();
				}
				line = br.readLine();
			}
			bw.write(sb.toString());
			bw.flush();
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void transform() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"I:/day_new.csv"));
			BufferedReader br = new BufferedReader(new FileReader(
					"I:/ntydx.txt"));
			String line = br.readLine();
			int lineCount = 0;
			StringBuffer sb = new StringBuffer();
			while (line != null) {
				lineCount++;
				if (lineCount % 100000 == 0)
					System.out.println(lineCount + " has finished!");
				sb.append(line + "\n");
				if (lineCount % 10000 == 0) {
					bw.write(sb.toString());
					bw.flush();
					sb = new StringBuffer();
				}
				line = br.readLine();
			}
			br.close();
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @Title: main
	 * @Description: TODO
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// String filePath =
		// "I:\\zyt\\Desktop\\highway_data\\tmp_no_res_user_hmh_6.txt";
		// Date d = getEarliestDate(new File(filePath));
		// System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		// .format(d));
		// System.out.println(Timestamp.valueOf("2014/02/01 11:11:11".replace('/',
		// '-')));
//		getDayData("2015-02-18 00:00:00", "2015-02-19 00:00:01");
		transform();
	}
}
