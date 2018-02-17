package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

public class WebClient {
    private final String USER_AGENT = "Mozilla/5.0";
    public static void main(String[] args) throws Exception {
//        WebClient test = new WebClient();
//        System.out.println("Testing GET request");
//        test.sendGet();
        String hostName = "localhost";
        int portNumber = 8443;

        try {
            Socket clientSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("GET /TestPDFDocument.pdf HTTP/1.1");
            //out.println("Host: localhost:8443");
            out.println("Connection: Close");
            out.println();


            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
            }

            in.close();
            out.close();

//            String userInput;
//            while ((userInput = stdIn.readLine()) != null) {
//                out.println(userInput);
//                System.out.println("echo: " + in.readLine());
//            }
        }

        catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }


    private void sendGet() throws Exception {
        String url = "http://localhost:8443/TestWordDocument.txt";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
        } catch (IOException e) {
            System.out.println("File Not Found");
        }
    }
}
