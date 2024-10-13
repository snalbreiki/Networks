import java.io.*;
import java.net.*;

public class Server{
    static int port = 12345;    // port no
    public static void main(String[] args) {
        DatagramSocket serverSocket = null;

        try {
            serverSocket = new DatagramSocket(port);
            byte[] receiveData = new byte[1024];

            System.out.println("Server is listening on port " + port + "...");

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Parse and extract headers
                String from=null,to=null,subject=null,body = null;
                boolean fromExist = false,toExist=false,subjectExist=false  ,bodyExist=false;

                String[] lines = message.split("\n");

                for (String line : lines) {
                    if (line.startsWith("From: ")) {                // ensure to have more chars than trimmed to avoid error
                        fromExist = true;
                        from = line.substring(6).trim(); // extract "From" value
                    } else if (line.startsWith("To: ")) {
                        toExist = true;
                        to = line.substring(4).trim();   // exttart "To" value
                    } else if (line.startsWith("Subject: ")) {
                        subjectExist = true;
                        subject = line.substring(9).trim(); // extarct "Subject" value
                    } else if (line.startsWith("Body: ")) {
                        bodyExist = true;
                        body = line.substring(6).trim(); // extract "Body" value
                    }
                }
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                if (fromExist && toExist && subjectExist && bodyExist) {
                    System.out.println("Email from " + receivePacket.getAddress() + " received successfully.");

                    String response = "Server received your email successfully.";
                    byte[] sendData = response.getBytes();

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);

                    System.out.println("Successful email response sent to client at " + clientAddress + ":" + clientPort);
                    System.out.println ( message ); //Test
                }
                else {
                    System.out.println ("Invalid email: One or more headers are missing." );

                    String response = "invalid email: One or more headers are missing.";
                    byte[] sendData = response.getBytes();

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);

                    System.out.println("Invalid email response sent to client at " + clientAddress + ":" + clientPort);
                }

                /*
                String[] parts = message.split("_");

                if (parts.length == 2 && parts[0].equals("GET") && parts[1].equals("TIMESTAMP")) {

                    String timestamp = java.time.LocalDateTime.now().toString();
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    byte[] sendData = timestamp.getBytes();

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);

                    System.out.println("Timestamp sent to client at " + clientAddress + ":" + clientPort);
                } else {
                    System.out.println("Invalid request from client");
                }
                */

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

}
