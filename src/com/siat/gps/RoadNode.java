package com.siat.gps;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

class TimeSpeed {
	int speed;
	Date time;
}

/**
 * @ClassName RoadNode
 * @Description TODO
 * @author Zhu Yingtao, Tan Vicky
 * @date 2014年11月7日 下午3:14:35
 */
@SuppressWarnings("serial")
public class RoadNode implements Serializable {

	int id;
	int orientation; // distinguish from the Car's direction;
	Point start;
	Point end;
	double carSpeed;
	// double length; // the length of this roadNode
	int roadID;

	ArrayList<TimeSpeed> array = new ArrayList<>();

	public int getRoadSpeed() {
		int speed = 0;
		// remove the dead TimeSpeed in the array.
		// Date nowTime = new Date();
		for (int i = 0; i < array.size(); i++) {
			// TimeSpeed ts = array.get(i);
		}
		// calculate the speed in weight(custom);
		return speed;
	}

	public RoadNode(int id, int roadID, Point start, Point end, int orientation) {
		this.id = id;
		this.roadID = roadID;
		this.start = start;
		this.end = end;
		this.orientation = orientation;
	}

	public String getRoadResult() {
		// TODO Auto-generated method stub
		return this.id + "\t" + this.getRoadSpeed() + "\t" + new Date();
	}

	public double length() {
		return Utility.getDistance(start, end);
	}
}
