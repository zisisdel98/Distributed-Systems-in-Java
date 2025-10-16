package com.activitytracker;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.*;

public class Worker implements Runnable{
    private Socket workerSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Worker(Socket workerSocket) {
        this.workerSocket = workerSocket;

        try {
            this.out = new ObjectOutputStream(workerSocket.getOutputStream());
            out.flush();
            this.in = new ObjectInputStream(workerSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error initializing worker communication: " + e.getMessage());
        }
    }

    private IntermediateResult processWaypoints(List<GPXReader.Waypoint> waypoints) {
        double partialDistance = 0;
        double partialClimb = 0;
        double partialTime = 0;

        for (int i = 0; i < waypoints.size() - 1; i++) {
            GPXReader.Waypoint wp1 = waypoints.get(i);
            GPXReader.Waypoint wp2 = waypoints.get(i + 1);

            double distance = wp1.distanceTo(wp2);
            double climb =  wp2.getEle() - wp1.getEle();
            Duration time = Duration.between(wp1.getTime() , wp2.getTime());
            int seconds = (int) time.getSeconds();

            partialDistance += distance;
            partialClimb += climb;
            partialTime += seconds;
        }

        IntermediateResult result = new IntermediateResult();
        result.setPartialDistance(partialDistance);
        result.setPartialClimb(partialClimb);
        result.setPartialTime(partialTime);

        //print the intermidiate result
        System.out.println(result);
        return result;
    }

    public void run() {
        try {
            while (true) {
                Object message = in.readObject();
                if ("WAYPOINTS".equals(message)) {
                    List<GPXReader.Waypoint> waypoints = (List<GPXReader.Waypoint>) in.readObject();
                    IntermediateResult intermediateResult = processWaypoints(waypoints);
                    out.writeObject(intermediateResult);
                    out.flush();

                } else {
                    System.err.println("Unexpected message received: " + message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in worker communication: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                workerSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing worker communication: " + e.getMessage());
            }
        }
    }
    public ObjectOutputStream getOut() {
        return out;
    }

    public ObjectInputStream getIn() {
        return in;
    }
    public Socket getWorkerSocket() {
        return workerSocket;
    }

    public static void main(String[] args) {
        String masterServerAddress = "192.168.1.3";
        int masterServerWorkerPort = 12345;

        try {
            Socket workerSocket = new Socket(masterServerAddress, masterServerWorkerPort);

            Worker worker = new Worker(workerSocket);
            Thread workerThread = new Thread(worker::run);
            workerThread.start();

        } catch (IOException e) {
            System.err.println("Error connecting to MasterServer: " + e.getMessage());
        }
    }
}
