import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        DatagramSocket clientSocket = null;

        try {
            clientSocket = new DatagramSocket();  // Create client socket
            InetAddress serverAddress = InetAddress.getByName("localhost");  // Server address

            System.out.println ("- Mail Client starting on host: " + InetAddress.getLocalHost().getHostName());
            System.out.println ("- Type name of Mail server: " + serverAddress);

            while (true) {
                // write email message
                Scanner cin = new Scanner ( System.in );
                System.out.println ( "---------------------\n" +
                                     "Creating New Email.." );

                System.out.print ( "To: " );
                String to = cin.nextLine ( );
                System.out.print ( "From: " );
                String from = cin.nextLine ( );
                System.out.print ( "Subject: " );
                String subject = cin.nextLine ( );
                System.out.print ( "Body: " );
                String body = cin.nextLine ( );

                String timestamp = java.time.LocalDateTime.now ( ).toString ( );
                String email = String.format ( "From: %s\nTo: %s\nSubject: %s\nBody: %s\nTimestamp: %s\n" , to , from , subject , body , timestamp );

                byte[] sendData = email.getBytes ( );  // Convert email to bytes
                DatagramPacket sendPacket = new DatagramPacket ( sendData , sendData.length , serverAddress , 12345 );
                clientSocket.send ( sendPacket );  // Send email packet

                byte[] receiveData = new byte[ 1024 ];  // Buffer for server response
                DatagramPacket receivePacket = new DatagramPacket ( receiveData , receiveData.length );
                clientSocket.receive ( receivePacket );  // Receive response

                // Print server's response
                String response = new String ( receivePacket.getData ( ) , 0 , receivePacket.getLength ( ) );
                System.out.println ( "- Server response: " + response );
            }
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
