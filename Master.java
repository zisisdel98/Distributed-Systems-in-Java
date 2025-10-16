package com.activitytracker;

import java.io.*;
import java.net.*;
import java.util.*;

public class Master  {
    private int numWorkers;
    private List<Worker> worker;
    private List<IntermediateResult> intermediateResults;
    private int clientPort;
    private int workerPort;
    private ServerSocket workerServerSocket;
    private ServerSocket clientServerSocket;
    private Map<String, Profile> _profilesMap;
    private int _globalSessions;
    private double _totalDistance;
    private double _totalClimb;
    private double _totalTime;

    public Master(int workerPort, int clientPort, int numWorkers)
    {
        this.workerPort = workerPort;
        this.clientPort = clientPort;
        this.numWorkers = numWorkers;
        this.worker= new ArrayList<Worker>();
        this.intermediateResults = new ArrayList<>();

        try {
            workerServerSocket = new ServerSocket(workerPort);
            clientServerSocket = new ServerSocket(clientPort);
        } catch (IOException e) {
            System.err.println("Error creating server sockets: " + e.getMessage());
            e.printStackTrace();
        }
        _profilesMap = new HashMap<String, Profile>();
        _globalSessions = 0;
        _totalDistance = 0.0;
        _totalClimb = 0.0;
        _totalTime = 0.0;
    }

    public void run() {
        acceptConnections();
    }

    public void acceptConnections() {
        // separate threads for accepting worker and client connections
        Thread workerConnectionsThread = new Thread(() -> {
            System.out.println("MasterServer listening for workers on port " + workerPort);
            for (int i = 0; i < numWorkers; i++) {
                try {
                    Socket workerSocket = workerServerSocket.accept();
                    Worker workers = new Worker(workerSocket);
                    worker.add(workers);
                    System.out.println("Worker " + i + " connected");

                    // Start the worker thread
                    Thread workerThread = new Thread(() -> handleWorkerConnection(workerSocket));
                    workerThread.start();
                } catch (IOException e) {
                    System.err.println("Error accepting worker connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        Thread clientConnectionsThread = new Thread(() -> {
            System.out.println("MasterServer listening for clients on port " + clientPort);
            while (true) {
                try {
                    Socket clientSocket = clientServerSocket.accept();
                    handleClientConnection(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        workerConnectionsThread.start();
        clientConnectionsThread.start();
    }

private void handleClientConnection(Socket clientSocket) {
    try {
        //input and output streams for communication with the client
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

        // Read the request type from the client
        Object requestType = (String) in.readObject();

        // Handle the request
        if ("ACTIVITY_STATS_REQUEST".equals(requestType)) {

            // Read the GPX file content sent by the client
            List<String> gpxLines = (List<String>) in.readObject();
            
            // Read the name and create profile
            String uName = GPXReader.readGPXUname(gpxLines);
            
            // if it already exists then get it and update else create a new one and add it to the map
            if (!(_profilesMap.containsKey(uName)))
            {
            	Profile newUser = new Profile(uName);
            	_profilesMap.put(uName, newUser);
            }
            
            // Parse GPX file content into a list of waypoints using GPXReader
            List<GPXReader.Waypoint> waypoints = GPXReader.readGPX(gpxLines);

            synchronized (this) {
                intermediateResults = new ArrayList<>();
            }

            // Distribute waypoints to worker nodes for processing
            map(waypoints);

            // Wait for all intermediate results to be collected and reduced
            synchronized (this) {
                while (intermediateResults.size() < numWorkers) {
                    wait();
                }
            }

            // Reduce intermediate results into a final result
            ActivityResult finalResult = reduce(intermediateResults, uName);
            finalResult.setUsername(uName);
            // Send final result back to client
            out.writeObject(finalResult.toString() + "%%%" + finalResult.getUsername());

            // Update the statistics of the profile
            Profile user = _profilesMap.get(uName);
            user.incrSessions();
            user.incrTotalDistance(finalResult.getTotalDistance());
            user.incrTotalClimb(finalResult.getTotalClimb());
            user.incrTotalTime(finalResult.getTotalTime());

            System.out.println(user.toString());

            // Update global statistics
            _globalSessions += 1;
            _totalDistance += finalResult.getTotalDistance();
            _totalClimb += finalResult.getTotalClimb();
            _totalTime += finalResult.getTotalTime();

        }else if ("USER_STATS_REQUEST".equals(requestType)) {
            System.out.println("User stats request received");
            String username = (String) in.readObject(); // read the username sent by client

            Profile userProfile = _profilesMap.get(username); // get the user profile
            if (userProfile != null) {
                out.writeObject(userProfile.toString()); // send the user profile to client
            } else {
                out.writeObject("No profile found for this username.");
            }
        } else if ("AVERAGE_STATS_REQUEST".equals(requestType)) {
            // Create a new Statistics object
            Statistics stats = new Statistics();

            // Set the stats based on the global variables
            stats.setTotalDistance(_totalDistance);
            stats.setTotalClimb(_totalClimb);
            stats.setTotalTime(_totalTime);
            stats.setGlobalSessions(_globalSessions);


            String username = (String) in.readObject(); // read the username sent by client

            // Get the user profile
            Profile userProfile = _profilesMap.get(username);

            if (userProfile != null) {
                stats.setUserSessions(userProfile.getSessions());
                stats.setUserTotalDistance(userProfile.getTotalDistance());
                stats.setUserTotalClimb(userProfile.getTotalClimb());
                stats.setUserTotalTime(userProfile.getTotalTime());
            }

            // Send the stats to the client
            out.writeObject(stats.toString());
        }else {
            System.err.println("Unexpected request type received: " + requestType);
        }
        // Close
        in.close();
        out.close();
        clientSocket.close();
    } catch (IOException | ClassNotFoundException | InterruptedException e) {
        e.printStackTrace();
    }
}

    private void handleWorkerConnection(Socket workerSocket) {
        Worker workerInstance = null;
        for (Worker worker : worker) {
            if (worker.getWorkerSocket().equals(workerSocket)) {
                workerInstance = worker;
                break;
            }
        }
        if (workerInstance == null) {
            System.err.println("Worker instance not found for the given socket.");
            return;
        }
        try {
            // Use the input stream from the Worker instance
            ObjectInputStream in = workerInstance.getIn();
            while(true) {
                // Read the IntermediateResult from the worker
                IntermediateResult intermediateResult = (IntermediateResult) in.readObject();
                // Store the IntermediateResult in the intermediateResults list
                synchronized (this) {
                    intermediateResults.add(intermediateResult);
                    // Notify if all IntermediateResults are collected
                    if (intermediateResults.size() == numWorkers) {
                        notifyAll();
                    }
                }
            }

        } catch (EOFException e) {
            System.out.println("Worker connection closed.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendWaypoints(Worker workerInstance, List<GPXReader.Waypoint> waypoints) {
        try {
            // Send the waypoints to the worker
            ObjectOutputStream out = workerInstance.getOut();
            out.writeObject("WAYPOINTS"); // Send the "WAYPOINTS" message before sending the actual waypoints
            out.writeObject(new ArrayList<>(waypoints)); // Create a new ArrayList from the sublist
            out.flush();

        } catch (IOException e) {
            System.err.println("Error sending waypoints to the worker: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void map(List<GPXReader.Waypoint> waypoints) {
        int numWaypoints = waypoints.size();
        int numChunks = Math.min(numWorkers, numWaypoints - 1);

        // Calculate the size of the base chunk (without overlap)
        int baseChunkSize = (int) Math.floor((double) numWaypoints / (numChunks + 1));

        // Distribute chunks of waypoints to workers
        for (int i = 0; i < numChunks; i++) {
            int startIndex = i * baseChunkSize;
            int endIndex = startIndex + baseChunkSize + 1;

            // Add extra waypoints to the last chunk if there are remaining waypoints
            if (i == numChunks - 1) {
                endIndex = numWaypoints;
            }
            List<GPXReader.Waypoint> waypointChunk = waypoints.subList(startIndex, endIndex);

            // Send waypointChunk to worker nodes for processing
            Worker workerInstance = worker.get(i);
            sendWaypoints(workerInstance, waypointChunk);
        }
    }

    private ActivityResult reduce(List<IntermediateResult> intermediateResults, String user) {
        double totalDistance = 0;
        double totalClimb = 0;
        long totalTime = 0;

        for (IntermediateResult result : intermediateResults) {
            totalDistance += result.getPartialDistance();
            totalClimb += result.getPartialClimb();
            totalTime += result.getPartialTime();
        }
        double averageSpeed = totalDistance / totalTime * 3.6; // convert m/s to km/h

        // Create the final result
        ActivityResult finalResult = new ActivityResult(user);
        finalResult.setTotalDistance(totalDistance);
        finalResult.setTotalClimb(totalClimb);
        finalResult.setTotalTime(totalTime);
        finalResult.setAverageSpeed(averageSpeed);
        return finalResult;
    }

    public static void main(String[] args) {
        int workerport = 12345;
        int clientport = 12346;
        int numWorkers =1;

        Master masterServer = new Master(workerport, clientport, numWorkers);
        masterServer.run();
    }

}
