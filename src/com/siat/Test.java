/**
 * 
 */
package com.siat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

	/**
	 * @Title: main
	 * @Description: TODO
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath = "I:\\zyt\\Desktop\\highway_data\\tmp_no_res_user_hmh_6.txt";
		Date d = getEarliestDate(new File(filePath));
		System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
				.format(d));
	}
}
