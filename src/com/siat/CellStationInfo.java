/**
 * 
 */
package com.siat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @ClassName CellStationInfo
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2014年12月17日 上午10:46:20
 */
public class CellStationInfo {

	public ArrayList<RoadSegment> readFromFile(String filePath) {
		ArrayList<RoadSegment> rss = new ArrayList<>();
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
				ArrayList<CellStation> starts = new ArrayList<>();
				for (int i = 0; i < cellids.length; i++) {
					CellStation cs = new CellStation(
							Integer.parseInt(cellids[i]));
					starts.add(cs);
				}
				RoadSegment rs = new RoadSegment(id++, starts, length);
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
}
