package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebServer {
    public static void main(String args[]) throws Exception {
        //Set the port number
        //TODO: Grab port number from arguments
        //int port = Integer.parseInt(args[0]);
        int port = 8443;

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
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        //Set up unput stream filters
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //Get the request line of the HTTP request message
        String requestLine = br.readLine();

        //Display the request line
        System.out.println("\n" + requestLine);

        //Extract the filename from the request line
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); //skip over the method, which should be "GET"
        String fileName = tokens.nextToken();

        //Prepend a "." so that file request is within the current directory
        fileName = "." + fileName;

        //Open the requested file
        FileInputStream fis = null;
        boolean fileExists = true;

        try {
            fis = new FileInputStream(fileName);
        }

        catch(FileNotFoundException e) {
            fileExists = false;
        }

        //Construct the response message
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;

        if (fileExists) {
            statusLine = "HTTP/1.1 200 OK" + CRLF;
            contentTypeLine = "Content-Type: " + contentType(fileName) + CRLF;
            entityBody = "<HTML><HEAD><TITLE>File found.</TITLE></HEAD><BODY>You are free to download the file.</BODY></HTML>";

            //TODO: Need to figure out how to return file to client
            //TODO: Test by doing localhost:8080/fileName
        }

        else {
            //statusLine = "404 Not Found" + CRLF;
            statusLine = "HTTP/1.1 404 Not Found" + CRLF;

            contentTypeLine = "Content-Type: text/html" + CRLF;
            entityBody = "<HTML><HEAD><TITLE>File Not Found</TITLE></HEAD><BODY>Not Found</BODY></HTML>";

        }

        //Send the status line
        os.writeBytes(statusLine);
        System.out.println(statusLine);

        //Send the content type line
        os.writeBytes(contentTypeLine);
        System.out.println(contentTypeLine);

        //Send a blank line to indicate the end of the header lines
        os.writeBytes(CRLF);

        //Send the entity body
        if (fileExists) {
            sendBytes(fis, os);
            fis.close();
        }

        else {
            os.writeBytes(entityBody);
        }

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

    private static String contentType(String fileName) {
        String patternString = "(\\.[a-zA-Z]+)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(fileName);

        matcher.find();
        String endOfFileName = matcher.group(1);
        System.out.println(endOfFileName);
        String contentType = "";

        switch(endOfFileName) {
            case ".htm":
                contentType = "text/html";
                break;
            case ".html":
                contentType = "text/html";
                break;
            case ".jpeg":
                contentType = "image/jpeg";
                break;
            case ".jpg":
                contentType = "image/jpeg";
                break;
            case ".gif":
                contentType = "image/gif";
                break;
            case ".doc":
                contentType = "application/msword";
                break;
            case ".docx":
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                break;
            case ".csv":
                contentType = "text/csv";
                break;
            case ".pdf":
                contentType = "application/pdf";
                break;
            case ".ppt":
                contentType = "application/vnd.ms-powerpoint\n";
                break;
            case ".pptx":
                contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                break;
            default:
                contentType = "application/octet‐stream";
                break;

        }

        return contentType;
    }

    private static void sendBytes(FileInputStream fis, DataOutputStream os) throws Exception {
        //Construct a 1K buffer to hold bytes on their way to the socket
        byte[] buffer = new byte[1024];
        int bytes = 0;

        //Copy requested file into the socket's output stream
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }
}
