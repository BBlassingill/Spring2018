import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class WebClient {
    private final String USER_AGENT = "Mozilla/5.0";
    private final static String CRLF = "\r\n";

    private static long startTime = 0;
    private static long endTime = 0;

    public static void main(String[] args) throws Exception {


//        String hostName = "localhost";
//        int portNumber = 8443;

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String fileName = args[2];

        try {
            startTime = System.currentTimeMillis();

            Socket clientSocket = new Socket(hostName, portNumber);
            clientSocket.setSoTimeout(5000);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("GET /" + fileName + " HTTP/1.1");
            //
            out.println("Connection: Close");

            out.println("Client Socket type: Connection-oriented (TCP)" + CRLF + "Client Timeout: " + clientSocket.getSoTimeout() + " milliseconds" + CRLF + "Client Protocol: TCP/IP" + CRLF + "Client Host Name: " + clientSocket.getInetAddress().getCanonicalHostName() + CRLF);
            out.println();

            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
            }

            endTime = System.currentTimeMillis();

            in.close();
            out.close();

            calculateRTT();

            //SocketAddress remoteSocketAddress = clientSocket.getRemoteSocketAddress();
            //System.out.println(clientSocket.getKeepAlive());
            //System.out.println(clientSocket.getRemoteSocketAddress());

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

    private static void calculateRTT() {
        long duration = endTime - startTime;

        System.out.println("The total RTT was: " + duration + " milliseconds");
    }


//    private void sendGet() throws Exception {
//        String url = "http://localhost:8443/TestWordDocument.txt";
//        URL obj = new URL(url);
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//        con.setRequestProperty("User-Agent", USER_AGENT);
//
//        int responseCode = con.getResponseCode();
//        System.out.println("\nSending 'GET' request to URL : " + url);
//        System.out.println("Response Code : " + responseCode);
//
//        try {
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            //print result
//            System.out.println(response.toString());
//        } catch (IOException e) {
//            System.out.println("File Not Found");
//        }
//    }
}
