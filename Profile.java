package com.activitytracker;

public class Profile {
	private String name;
	private int sessions;
	private double personalTotalDistance;
    private double personalTotalClimb;
    private double personalTotalTime;
	
	public Profile(String name)
	{
		this.name = name;	
		sessions = 0;
		personalTotalDistance = 0;
	    personalTotalClimb = 0;
	    personalTotalTime = 0;
	}
	
	public String getUsername()
	{
		return name;
	}
	
	public void incrSessions()
	{
		sessions++;
	}
	
	public void incrTotalDistance(double dist)
	{
		personalTotalDistance += dist;
	}
	
	public void incrTotalClimb(double climb)
	{
		personalTotalClimb += climb;
	}
	
	public void incrTotalTime(double time)
	{
		personalTotalTime += time;
	}
	
	public double getAvgDistance()
	{
		return (personalTotalDistance / sessions);
	}
	
	public double getAvgClimb()
	{
		return (personalTotalClimb / sessions);
	}
	
	public double getAvgTime()
	{
		return (personalTotalTime / sessions);
	}
	
	public double getAvgSpd()
	{
		return (personalTotalDistance / personalTotalTime);
	}


	
	public String toString()
	{
		return "Username: " + name + "\n" +
				"Sessions: " + sessions + "\n" +
				"Average Distance: " + String.format("%.3f", getAvgDistance() / 1000.0) + "km\n" +
				"Average Climb: " + String.format("%.3f", getAvgClimb()) + "\n" +
				"Average Time: " + (int) (getAvgTime() / 60) + "min and " + (int) (getAvgTime() % 60) + "sec\n"+
				"\nTotal Distance: " + String.format("%.3f", getTotalDistance() / 1000.0) + "km\n" +
				"Total Climb: " + String.format("%.3f", getTotalClimb()) + "\n" +
				"Total Time: " + (int) (getTotalTime() / 60) + "min and " + (int) (getTotalTime() % 60) + "sec\n";
	}

	public double getTotalDistance() {
		return personalTotalDistance;
	}

	public double getTotalClimb() {
		return personalTotalClimb;
	}

	public double getTotalTime() {
		return personalTotalTime;
	}

	public Object getSessions() {
		return sessions;
	}
}