package com.activitytracker;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GPXReader {

    public static String readGPXUname(List<String> gpxLines){
	String ret = "notfound";	
	try{
		for (String line : gpxLines){
			if (!line.contains("creator"))
	   			continue;
	   		line = line.trim();
	   		String[] vals = line.split("\"");
	   		ret = vals[3];
	   		break;
			}
    	}
    	catch (Exception e)
    	{
    		System.out.println("Exception: " + e);
    	}
    	return ret;
    }

    public static List<Waypoint> readGPX(List<String> gpxLines) {
        List<Waypoint> waypoints = new ArrayList<>();
        boolean in = false;
        Waypoint waypoint = null;
        String content = "";

        for (String line : gpxLines) {
            line = line.trim();
            if (line.startsWith("<wpt")) {
                in = true;
                double lat = Double.parseDouble(line.split(" ")[1].split("=")[1].replaceAll("\"", ""));
                double lon = Double.parseDouble(line.split(" ")[2].split("=")[1].replaceAll("\"|>", ""));
                waypoint = new Waypoint(lat, lon);
            } else if (line.startsWith("</wpt>")) {
                in = false;
                waypoints.add(waypoint);
            } else if (in) {
                content += line;
                if (line.startsWith("<ele>")) {
                    double ele = Double.parseDouble(line.replaceAll("<ele>|</ele>", ""));
                    waypoint.setEle(ele);
                } else if (line.startsWith("<time>")) {
                    String timeStr = line.replaceAll("<time>|</time>", "");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    LocalDateTime date = null;
                    date = LocalDateTime.parse(timeStr, formatter);
                    waypoint.setTime(date);
                }
            }
        }
        return waypoints;
    }
    public static class Waypoint implements Serializable {
        private double lat;
        private double lon;
        private double ele;
        private LocalDateTime time;

        public Waypoint(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public double getEle() {
            return ele;
        }

        public void setEle(double ele) {
            this.ele = ele;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }

        public String toString() {
            return "Waypoint{" +
                    "lat=" + lat +
                    ", lon=" + lon +
                    ", ele=" + ele +
                    ", time='" + time + '\'' +
                    '}';
        }

        public double distanceTo(Waypoint waypoint) {
            double lat1 = Math.toRadians(lat);
            double lon1 = Math.toRadians(lon);
            double lat2 = Math.toRadians(waypoint.lat);
            double lon2 = Math.toRadians(waypoint.lon);
            double dlon = lon2 - lon1;
            double dlat = lat2 - lat1;
            double a = Math.pow(Math.sin(dlat / 2), 2)
                    + Math.cos(lat1) * Math.cos(lat2)
                    * Math.pow(Math.sin(dlon / 2), 2);
            double c = 2 * Math.asin(Math.sqrt(a));
            double r = 6371;
            return (c * r) * 1000;
        }
    }

}
