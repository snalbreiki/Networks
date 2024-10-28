import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        DatagramSocket clientSocket = null;            // Initialize clientSocket

        try {
            clientSocket = new DatagramSocket();       // Create client socket
            Scanner cin = new Scanner(System.in);      // Scanner to take user input
            System.out.println("- Mail Client starting on host: " + InetAddress.getLocalHost().getHostName());

            InetAddress serverAddress = null;
            byte[] receiveData = new byte[ 1024 ];     // Buffer for server response
            DatagramPacket receivePacket = new DatagramPacket ( receiveData , receiveData.length );

            boolean isConnected = false;               // Status var for connection

            while (true) {
                // Validate server name given by user
                if(!isConnected) {
                    System.out.print ( "[-] Type server name: " );  // Ask user to enter server name
                    String serverName = cin.next();
                    serverAddress = InetAddress.getByName(serverName);  // Server address
                    sendMessage ( clientSocket , serverAddress , "CONNECTION-REQUEST" ); // Send connection request to server

                    clientSocket.receive ( receivePacket );                 // Receive response (should be actual server name)
                    String serverResponse = new String ( receivePacket.getData ( ) , 0 , receivePacket.getLength ( ) );  // parse to string
                    if (serverResponse.equals ( serverName )) {             // if actual server name matches user input
                        sendMessage ( clientSocket , serverAddress , "CONNECTION-REQUEST-CONFIRM" );  // send confirmation to server
                        System.out.println ( "- Connected to server" );     // notify client for connection success
                        isConnected = true;                                 // update connection status
                    } else {
                        System.out.println ( "- Server does not respond\n- Check for Possible name mismatch" ); // notify client for connection failure
                        continue;  // Loop for reattempt
                    }
                }

                // User input
                System.out.println("---------------------\nCreating New Email..");

                // take user input to fill email fields and check for "quit" in each field
                System.out.print("[-] To: ");
                String to = cin.next();
                if (to.equalsIgnoreCase("quit")) {      // if input is "quit"
                    sendMessage (clientSocket, serverAddress,"Client disconnecting");  // notify server for client disconnection
                    break; // exit the loop and disconnect client
                }
                System.out.print("[-] From: ");
                String from = cin.next();
                if (from.equalsIgnoreCase("quit")) {
                    sendMessage(clientSocket, serverAddress,"Client disconnecting");
                    break;
                }

                System.out.print("[-] Subject: ");
                String subject = cin.next();
                if (subject.equalsIgnoreCase("quit")) {
                    sendMessage(clientSocket, serverAddress,"Client disconnecting");
                    break;
                }

                System.out.print("[-] Body: ");
                String body = cin.next();
                if (body.equalsIgnoreCase("quit")) {
                    sendMessage(clientSocket, serverAddress,"Client disconnecting");
                    break;
                }

                // Send the email to server along with client timestamp
                String timestamp = LocalDateTime.now().format( DateTimeFormatter.ofPattern("EEE. MMM. d, yyyy HH:mm"));
                String email = String.format("From: %s\nTo: %s\nSubject: %s\nBody: %s\nTimestamp: %s\n", to, from, subject, body, timestamp);
                sendMessage(clientSocket, serverAddress,email);   // check the method below the main
                System.out.println ("- Mail Sent to Server, waiting... " );

                // Receive the server response
                receiveData = new byte[1024];          // Buffer for server response
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);  // Receive response
                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Print server's response
                System.out.println("- Server response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close client socket if open
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            System.out.println("Client disconnected.");
        }
    }

    // Method to send a message
    private static void sendMessage(DatagramSocket clientSocket, InetAddress serverAddress, String message) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 1234);
            clientSocket.send(sendPacket);  // Send disconnect message
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
