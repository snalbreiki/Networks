import java.io.*;
import java.net.*;
import java.sql.Timestamp;

public class Server {
    static int port = 1234;    // Port number

    public static void main(String[] args) {
        DatagramSocket serverSocket = null;

        try {
            serverSocket = new DatagramSocket(port); // Create server socket
            byte[] receiveData = new byte[1024];     // Buffer for incoming data
            System.out.println("Server is listening on port " + port + "...");


            while (true) {
                // Receive the packet
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                // Convert the packet data to string
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Initialize email components
                String from = null, to = null, subject = null, body = null;
                boolean fromExist = false, toExist = false, subjectExist = false, bodyExist = false;

                // Parse headers
                String[] lines = message.split("\n");  // Split message into lines

                for (String line : lines) {
                    if (line.startsWith("From: ")) {
                        fromExist = true;
                        from = line.substring(6).trim(); // Extract "From" value
                    } else if (line.startsWith("To: ")) {
                        toExist = true;
                        to = line.substring(4).trim();   // Extract "To" value
                    } else if (line.startsWith("Subject: ")) {
                        subjectExist = true;
                        subject = line.substring(9).trim(); // Extract "Subject" value
                    } else if (line.startsWith("Body: ")) {
                        bodyExist = true;
                        body = line.substring(6).trim(); // Extract "Body" value
                    }
                }
                // Verification

                InetAddress clientAddress = receivePacket.getAddress();  // Get client address
                int clientPort = receivePacket.getPort();                // Get client port

                // If all headers exist, send success response
                if (fromExist && toExist && subjectExist && bodyExist) {
                    System.out.println("Email from " + receivePacket.getAddress() + " received successfully.");

                    String response = "Server received your email successfully \nReceived timestamp: " + java.time.LocalDateTime.now().toString();
                    byte[] sendData = response.getBytes();

                    // Send response
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);

                    System.out.println("Successful email response sent to client at " + clientAddress + ":" + clientPort);
                    System.out.println(message);  // Test
                } else {
                    // If headers are missing, send error response
                    System.out.println("Invalid email from " + receivePacket.getAddress() + ": One or more headers are missing.");

                    String response = "Invalid email: One or more headers are missing.";
                    byte[] sendData = response.getBytes();

                    // Send response
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);

                    System.out.println("Invalid email response sent to client at " + clientAddress + ":" + clientPort);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }
}
