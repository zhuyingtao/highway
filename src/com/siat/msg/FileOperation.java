package com.siat.msg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.siat.ds.Node;
import com.siat.ds.NodeSegment;
import com.siat.ds.Station;
import com.siat.ds.StationSegment;
import com.siat.ds.UserData;
import com.siat.msg.db.DBServiceForMySQL;

/**
 * @ClassName FileOperation
 * @Description This is a previous version of using localhost database mysql and
 *              some data files, it contains a series method of operating the
 *              files. Now this class is DEPRECATED.
 * @author Zhu Yingtao
 * @date 2015��1��29�� ����6:03:50
 */
public class FileOperation {

	DBServiceForMySQL db = new DBServiceForMySQL();

	/**
	 * @Title: readFromFile
	 * @Description: read user data from file directly;
	 * @param filePath
	 * @return
	 */
	public ArrayList<UserData> readUserData(String filePath) {
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

	/**
	 * @Title: scanAllFiles
	 * @Description: scan all the file in a certain directory to do some
	 *               operating;
	 * @param dirPath
	 */
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
					db.loadData(dirPath + "/" + fileNames[i], false);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// deprecated
	public static ArrayList<NodeSegment> initialNodeSegments() {
		ArrayList<Node> nodes = new ArrayList<>();
		ArrayList<NodeSegment> sections = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"data/������2.txt"));
			br.readLine();
			String line = br.readLine();
			while (line != null) {
				String[] parts = line.split("\t");
				// int id = Integer.parseInt(parts[0]);
				int nodeId = Integer.parseInt(parts[1]);
				String nodeName = parts[2];
				int direction = Integer.parseInt(parts[4]);
				int cellId = Integer.parseInt(parts[8]);
				double length = Double.parseDouble(parts[9]);

				// ����ֻ��һ���ڵ����
				// Node ns = new Node(nodeId, nodeName, cellId, length,
				// direction);
				Node ns = null;

				nodes.add(ns);
				br.readLine();
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

		// for (int j = 0; j < 2; j++) {
		// for (int i = 0; i < 24; i++) {
		// Node start = nodes.get(i);
		// Node end = nodes.get(i + 1);
		// int direction = (j == 0 ? 1 : 2);
		// NodeSegment rs = new NodeSegment(i, start, end, direction);
		// sections.add(rs);
		// }
		// }
		return sections;
	}

	/**
	 * @Title: readFromFile
	 * @Description: DEPRECATED
	 * @param filePath
	 * @return
	 */
	@SuppressWarnings("unused")
	public static ArrayList<StationSegment> readStationSegment(String filePath) {
		ArrayList<StationSegment> rss = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			br.readLine();
			String line = br.readLine();
			int id = 0;
			while (line != null) {
				String[] parts = line.split("\t");
				double xs = Double.parseDouble(parts[0]);
				double ys = Double.parseDouble(parts[1]);
				double xe = Double.parseDouble(parts[2]);
				double ye = Double.parseDouble(parts[3]);
				double length = Double.parseDouble(parts[4]);
				String cellidStr = parts[5];
				String lacidStr = parts[6];
				String[] cellids = cellidStr.split(",");
				ArrayList<Station> starts = new ArrayList<>();
				for (int i = 0; i < cellids.length; i++) {
					Station cs = new Station(Integer.parseInt(cellids[i]));
					starts.add(cs);
				}
				StationSegment rs = new StationSegment(id++, starts, length);
				rss.add(rs);
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
		return rss;
	}

	public static void main(String[] args) {
		FileOperation fos = new FileOperation();
		fos.scanAllFiles("I:/zyt/Desktop/��������");
	}
}
