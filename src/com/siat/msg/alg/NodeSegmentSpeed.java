package com.siat.msg.alg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.siat.ds.Node;
import com.siat.ds.NodeSegment;
import com.siat.ds.StationSegment;
import com.siat.msg.Configuration;
import com.siat.msg.db.DBServiceForOracle;
import com.siat.msg.util.DataLogger;

/**
 * @ClassName NodeSegmentSpeed
 * @Description TODO
 * @author Zhu Yingtao
 * @date 2015-03-20 16:49:02
 */
public class NodeSegmentSpeed {

	private DBServiceForOracle db = null;
	private Logger logger = null;
	public List<NodeSegment> nodeSegments = null;

	/**
	 * 
	 */
	public NodeSegmentSpeed() {
		// TODO Auto-generated constructor stub
		this.db = new DBServiceForOracle();
		this.logger = DataLogger.getLogger();
	}

	/**
	 * @Title: computeAvgSpeed
	 * @Description: TODO
	 * @param stations
	 */
	public void computeAvgSpeed(List<StationSegment> stations, String timeStamp) {
		// compute every node segment's speed in the list;
		for (int i = 0; i < nodeSegments.size(); i++) {
			// 1. get the start and end cellId;
			NodeSegment ns = nodeSegments.get(i);
			// sometimes the node segment's start or end node may not in the
			// node list, now we just skip this node segment.
			if (ns.startNode == null) {
				logger.fine(">>>>> Can't find the start Node -> "
						+ ns.startNodeId);
				continue;
			}
			int start = ns.startNode.cellId;
			if (ns.endNode == null) {
				logger.fine(">>>>> Can't find the end Node -> " + ns.endNodeId);
				continue;
			}
			int end = ns.endNode.cellId;
			int direction = ns.direction;
			if (direction != ns.startNode.direction
					|| direction != ns.endNode.direction) {
				logger.severe("----- node segment's direction does not"
						+ " match start or end direction ! seg_dir = "
						+ direction + " , s_dir = " + ns.startNode.direction
						+ " , e_dir = " + ns.endNode.direction);
			}
			// 2. select these cellIds from the station segments; NOTE: the
			// index of start and end cellId in station segments must be in
			// order, that is, the start must be found first;
			int startIndex = -1;
			int endIndex = -1;
			for (int j = 0; j < stations.size(); j++) {
				StationSegment ss = stations.get(j);
				if (ss.contains(start) && direction == ss.getDirection())
					startIndex = j;
				if (ss.contains(end) && direction == ss.getDirection())
					endIndex = j;
				if (startIndex != -1 && endIndex != -1)
					break;
			}

			// may be some exception will occur as below; now just skip them...
			if (startIndex == -1 || endIndex == -1) {
				logger.severe("----- station_segment not found ! start = "
						+ start + ", start_index = " + startIndex + " , end = "
						+ end + ", endIndex = " + endIndex);
				continue;
			}
			if (startIndex > endIndex) {
				logger.severe("---- station_segment confilcts ... start = "
						+ startIndex + " , end =" + endIndex + " , dir = "
						+ direction);
				continue;
			}

			// 3. compute the average speed according to the station segments
			// average speed; In particular, sum them all and then divide, with
			// length rate;
			double sum = 0; // sum of speed * length;
			double length = 0; // sum of length;
			int maxSpeed = -1;
			int minSpeed = 200;
			int num = 0; // the car number in this node segment;
			// int expectedNum = 0;
			for (int j = startIndex; j <= endIndex; j++) {
				StationSegment ss = stations.get(i);
				// use filter speed;
				sum += ss.getFilterAvgSpeed() * ss.getLength();
				length += ss.getLength();
				if (maxSpeed < ss.getMaxSpeed())
					maxSpeed = ss.getMaxSpeed();
				if (minSpeed > ss.getMinSpeed())
					minSpeed = ss.getMinSpeed();
				num += ss.getRealNum();
				// expectedNum += ss.getExpectedNum(); // here , INACCURATE
			}
			int avgSpeed = (int) (sum / length);
			// if (avgSpeed == 0) // set default is 80;
			// avgSpeed = 80;

			// 4. set these speeds to this Node Segment;
			ns.setAvgSpeed(avgSpeed);
			ns.setMaxSpeed(maxSpeed);
			ns.setMinSpeed(minSpeed);
			ns.setRealNum(num);
			// ns.setExpectedNum(expectedNum);
		}

		// 5. after all the segments have been computing, store them into
		// database;
		if (Configuration.WRITE_TO_DATABASE)
			this.storeData(timeStamp);
		if (Configuration.WRITE_TO_FILE)
			this.dumpData(timeStamp);
	}

	/**
	 * @Title: storeData
	 * @Description: store the speed data into the database;
	 * @param timeStamp
	 */
	public void storeData(String timeStamp) {
		db.insertNodeSpeeds(nodeSegments, timeStamp);
	}

	/**
	 * @Title: dumpData
	 * @Description: write the speed data to file directly, so it is easier to
	 *               examine the result;
	 * @param timeStamp
	 */
	public void dumpData(String timeStamp) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("node_speeds.txt", true));
			StringBuffer sb = new StringBuffer();
			sb.append("=======Time : " + timeStamp + " =====\n");
			sb.append("id\t\tdir\t\tmax\t\tmin\t\tavg\t\tnum\n");
			// write out all the node segments information;
			for (int i = 0; i < nodeSegments.size(); i++) {
				NodeSegment ns = nodeSegments.get(i);
				sb.append(ns.getId() + " -- " + ns.getDirection() + " : "
						+ ns.getMaxSpeed() + " , " + ns.getMinSpeed() + " , "
						+ ns.getAvgSpeed() + " ( " + ns.getRealNum() + " )\n");
			}
			bw.write(sb.toString() + "\n\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<NodeSegment> initialNodeSegments() {
		logger.info("initial node segments ... ");
		List<Node> nodes = db.selectNode();
		List<NodeSegment> segments = db.selectNodeSegment();
		for (int i = 0; i < segments.size(); i++) {
			NodeSegment ns = segments.get(i);
			ns.initStart(nodes);
			ns.initEnd(nodes);
		}
		this.nodeSegments = segments;
		return segments;
	}
}
