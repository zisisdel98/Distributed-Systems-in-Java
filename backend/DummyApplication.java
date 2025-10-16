package com.activitytracker;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DummyApplication
{
    private String host;
    private int port;
    private String gpxFile;
    public DummyApplication(String host, int port, String gpxFile)
    {
        this.host = host;
        this.port = port;
        this.gpxFile = gpxFile;
    }

    public void sendReqAndReceiveResp()
    {
        try
        {
            Socket socket = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Send the request type
            out.writeObject("ACTIVITY_STATS_REQUEST");

            // Send the GPX file content
            List<String> gpxLines = Files.readAllLines(Paths.get(gpxFile));
            out.writeObject(gpxLines);
            out.flush();

            // Read and print the response
            Object response = in.readObject();
            if (response instanceof ActivityResult) {
                System.out.println("Server response: " + response.toString());
            } else {
                System.err.println("Unexpected response received: " + response.toString());
            }

            // Close
            in.close();
            out.close();
            socket.close();
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.err.println("Error sending request: " + e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        String host = "192.168.1.3";
        int port = 12346;
        String gpxfile=args[0];
        DummyApplication client = new DummyApplication(host, port, gpxfile);
        client.sendReqAndReceiveResp();
    }
}
