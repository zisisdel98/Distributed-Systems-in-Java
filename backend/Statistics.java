package com.activitytracker;

public class Statistics {
	private double totalDistance;
	private double totalClimb;
	private double totalTime;
	private int globalSessions;

	private double userTotalDistance;
	private double userTotalClimb;
	private double userTotalTime;

	private int userSessions;

	public Statistics() {
		this.totalDistance = 0;
		this.totalClimb = 0;
		this.totalTime = 0;
		this.userTotalDistance = 0;
		this.userTotalClimb = 0;
		this.userTotalTime = 0;
	}

	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}

	public void setTotalClimb(double totalClimb) {
		this.totalClimb = totalClimb;
	}

	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}

	public void setGlobalSessions(int globalSessions) {
		this.globalSessions = globalSessions;
	}

	public void setUserTotalDistance(double userTotalDistance) {
		this.userTotalDistance = userTotalDistance;
	}

	public void setUserTotalClimb(double userTotalClimb) {
		this.userTotalClimb = userTotalClimb;
	}

	public void setUserTotalTime(double userTotalTime) {
		this.userTotalTime = userTotalTime;
	}

	private double getAverageTotalDistance() {
		return (globalSessions != 0) ? totalDistance / globalSessions : 0;
	}

	private double getAverageTotalClimb() {
		return (globalSessions != 0) ? totalClimb / globalSessions : 0;
	}

	private double getAverageTotalTime() {
		return (globalSessions != 0) ? totalTime / globalSessions : 0;
	}

	private double getPercentageDifference(double average, double userTotal) {
		return ((userTotal - average) / average) * 100.0;
	}

	private double getAverageUserTotalDistance() {
		return (userSessions != 0) ? userTotalDistance / userSessions : 0;
	}

	private double getAverageUserTotalClimb() {
		return (userSessions != 0) ? userTotalClimb / userSessions : 0;
	}

	private double getAverageUserTotalTime() {
		return (userSessions != 0) ? userTotalTime / userSessions : 0;
	}


	@Override
	public String toString() {
		return "\nGlobal sessions: " + globalSessions + "\n" +
				"\nGlobal Average Distance: " + String.format("%.3f", getAverageTotalDistance() / 1000.0) + "km" +
				"\nUser Average Distance: " + String.format("%.3f", getAverageUserTotalDistance() / 1000.0) + "km" +
				"\nDifference in Distance: " + String.format("%.2f", getPercentageDifference(getAverageTotalDistance(), getAverageUserTotalDistance())) + "% \n" +
				"\nGlobal Average Climb: " + String.format("%.3f", getAverageTotalClimb()) + "m" +
				"\nUser Average Climb: " + String.format("%.3f", getAverageUserTotalClimb()) + "m" +
				"\nDifference in Climb: " + String.format("%.2f", getPercentageDifference(getAverageTotalClimb(), getAverageUserTotalClimb())) + "% \n" +
				"\nGlobal Average Time: " + (int) (getAverageTotalTime() / 60) + "min " + (int) (getAverageTotalTime() % 60) + "sec" +
				"\nUser Average Time: " + (int) (getAverageUserTotalTime() / 60) + "min " + (int) (getAverageUserTotalTime() % 60) + "sec" +
				"\nDifference in Time: " + String.format("%.2f", getPercentageDifference(getAverageTotalTime(), getAverageUserTotalTime())) + "% \n";
	}
	public void setUserSessions(Object sessions) {
		this.userSessions = (int) sessions;
	}

	public int getUserSessions() {
		return userSessions;
	}
}
