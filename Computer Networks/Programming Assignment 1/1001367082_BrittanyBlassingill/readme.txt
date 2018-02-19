Brittany Blassingill - 1001367082

How to compile and run:

In project directory, run the following commands to compile the source files: 
javac WebServer.java
javac WebClient.java

After the files are compiled, run the web server with the following command:
java WebServer <port>, where port is the port number the server should listen to.

For example: java WebServer 8443

While the server is running, run the client with the following command in a different command prompt:
java WebClient <host> <port> <name_of_file>, where host is the host of the server, port
is the port number the server is listening to, and name_of_file is one of the three files included in the project directory
or a file name that does not exist. If the port number is not provided, the client will connect to port 80 by default.

For example: java WebClient localhost 8443 TestTextDocument.txt or java WebClient localhost TestTextDocument.txt
						
The IDE used to develop the source code was Intellij.