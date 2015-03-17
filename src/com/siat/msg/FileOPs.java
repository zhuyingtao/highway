package com.siat.msg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.siat.msg.db.DBServiceForMySQL;

/**
 * @ClassName FileOPs
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2015年1月29日 下午6:03:50
 */
public class FileOPs {

	DBServiceForMySQL db = new DBServiceForMySQL();

	public ArrayList<UserData> readFromFile(String filePath) {
		ArrayList<UserData> arrays = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = br.readLine();
			while (line != null) {
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arrays;
	}

	public void scanAllFiles(String dirPath) {
		File f = new File(dirPath);
		if (!f.isDirectory()) {
			System.out.println("must enter a directory name!");
			return;
		}
		String[] fileNames = f.list();
		for (int i = 0; i < fileNames.length; i++) {
			if (fileNames[i].endsWith("txt")) {
				try {
					db.loadData(dirPath+"/" + fileNames[i], false);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		FileOPs fos = new FileOPs();
		fos.scanAllFiles("I:/zyt/Desktop/福银高速");
	}
}
