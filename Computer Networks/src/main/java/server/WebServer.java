package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public final class WebServer {
    public static void main(String args[]) throws Exception {
        //Set the port number
        int port = 1090;

        //Establish the listen socket
        ServerSocket listeningSocket = new ServerSocket(port);
        //Socket listeningSocket = new Socket("localhost", port);

        //Process HTTP service requests in an infinite loop
        while (true) {
            //Listen for a TCP connection request
            Socket clientSocket = listeningSocket.accept();
            //BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //Construct an object to process the HTTP request message
            HttpRequest request = new HttpRequest(clientSocket);

            //Create a new thread to process the request
            Thread thread = new Thread(request);

            //Start the thread
            thread.start();
        }
    }
}

final class HttpRequest implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    //Constructor
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    //Implement the run() method of the Runnable interface
    public void run() {
        try {
            processRequest();
        }

        catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        //Get a reference to the socket's input and output streams
        DataOutputStream os = (DataOutputStream) socket.getOutputStream();

        //Set up unput stream filters
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //Get the request line of the HTTP request message
        String requestLine = br.readLine();

        //Display the request line
        System.out.println("\n" + requestLine);

        //Get and display the header lines
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }

        //Close streams and socket
        os.close();
        br.close();
        socket.close();
    }
}
