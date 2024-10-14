import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        DatagramSocket clientSocket = null;

        try {
            clientSocket = new DatagramSocket();  // Create client socket
            InetAddress serverAddress = InetAddress.getByName("localhost");  // Server address

            // Construct email message
            String email = "From: Client\n" +
                    "To: Server\n" +
                    "Subject: Test\n" +
                    "Body: Helllooo\n";

            byte[] sendData = email.getBytes();  // Convert email to bytes
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 1234);
            clientSocket.send(sendPacket);  // Send email packet

            byte[] receiveData = new byte[1024];  // Buffer for server response
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);  // Receive response

            // Print server's response
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Server response: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close client socket if open
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        }
    }
}
