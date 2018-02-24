import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

/**
 * Author: Brittany Blassingill
 * Student ID: 1001367082
 */
public class WebClient {
    private final static String CRLF = "\r\n";

    private static long startTime = 0;
    private static long endTime = 0;

    public static void main(String[] args) throws Exception {
        String host = args[0];

        //Get port number
        int portNumber;
        String fileName;

        if (!args[1].matches("[0-9]+")) {
           portNumber = 80;
           fileName = args[1];
        }

        else {
            portNumber = Integer.parseInt(args[1]);
            fileName = args[2];
        }


        try {
            //Get the starting timestamp to calculate the RTT
            startTime = System.currentTimeMillis();
            makeConnection(host, portNumber, fileName);
            calculateRTT();
        }

        catch (UnknownHostException e) {
            System.out.println("The following host is unknown: " + host);
        }

        catch (IOException e) {
            System.out.println("Unable to connect to " + host);
        }
    }

    /**
     * Helper method to establish a connection with the host
     * @param host
     * @param portNumber
     * @param fileName
     * @throws IOException
     */
    private static void makeConnection(String host, int portNumber, String fileName) throws IOException {
        Socket clientSocket = new Socket(host, portNumber);
        clientSocket.setSoTimeout(5000);

        //Get the client socket's streams to interact with the server socket
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        //Make request and send socket details
        out.println("GET /" + fileName + " HTTP/1.1");
        out.println("Connection: Close");
        out.println("Client Socket type: Connection-oriented (TCP)" + CRLF + "Client Timeout: " + clientSocket.getSoTimeout() + " milliseconds" + CRLF + "Client Protocol: TCP/IP" + CRLF + "Client Host Name: " + clientSocket.getInetAddress().getCanonicalHostName() + CRLF);
        out.println();

        //Get the full response from the server
        String response;
        while ((response = in.readLine()) != null) {
            System.out.println(response);
        }

        //Get the ending timestamp to calculate the RTT
        endTime = System.currentTimeMillis();

        in.close();
        out.close();
        clientSocket.close();
    }


    /**
     * This is a helper function to calculate the RTT time
     */
    private static void calculateRTT() {
        long duration = endTime - startTime;

        System.out.println("The total RTT was: " + duration + " milliseconds");
    }
}
