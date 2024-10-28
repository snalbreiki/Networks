import java.io.*;
import java.net.*;
import java.text.*;
import java.time.*;
import java.time.format.*;

public class Server {
    static int port = 12345;    // Port number

    public static void main(String[] args) {
        int receivedEmailCount = 0;   // counter of received emails (for better readability)

        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(port); // Create server socket
            byte[] receiveData = new byte[1024];     // Buffer to store incoming data

            System.out.printf("- Mail Server Starting at host: %s \n", InetAddress.getLocalHost().getHostName());
            System.out.println("- Mail Server is listening on port " + port);
            System.out.println("- Waiting for a client connection.. \n");

            // Loop to continuously listen for client messages
            while (true) {
                // Receive packet from client
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                // Get client address and port to send responses back
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // Convert packet data to string for processing
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Check for connection request from client
                //serverSocket.receive(receivePacket);
                // message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if (message.startsWith("CONNECTION")) {
                    System.out.println("- Client connected");
                    continue;
                }


                // Create and validate the Email object from message
                Email email = new Email();

                // Parse and validate email fields; check if "To" and "From" files exist
                if (email.parseEmailNValidate(message) && emailsExistsInDirectory(email)) {
                    // Print received email details
                    receivedEmailCount++;
                    System.out.printf("\n** [NEW EMAIL | Email No: %d] **\n", receivedEmailCount);
                    System.out.println("Mail Received from " + clientAddress.getHostName());
                    System.out.println("From: " + email.getFrom());
                    System.out.println("To: " + email.getTo());
                    System.out.println("Subject: " + email.getSubject());
                    System.out.println("Time: " + email.getTimestamp());
                    System.out.println(email.getBody());
                    System.out.println("*************");

                    // Send "250 OK" response with current timestamp
                    String serverTimeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE. MMM. d, yyyy HH:mm"));
                    System.out.print("- The Header fields are verified.\n- Sending \"250 OK\"\n");
                    sendResponse(serverSocket, clientAddress, clientPort, ("250 OK: Email received successfully at " + serverTimeStamp));

                    System.out.println("\n- Waiting to be contacted for transferring Mail... \n");
                } else {
                    // Handle client disconnection message
                    if (message.equals("Client disconnecting")) {
                        System.out.println(message);
                        System.out.println("- Waiting to be contacted for transferring Mail... \n\n");
                        continue;
                    }

                    // Send "501 Error" response if email validation fails
                    String serverTimeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE. MMM. d, yyyy HH:mm"));
                    sendResponse(serverSocket, clientAddress, clientPort, "501 Error: at " + serverTimeStamp);
                    System.out.print("- The Header fields are not valid.\n- Sending \"501 Error\"\n");
                    sendResponse(serverSocket, clientAddress, clientPort, ("501 Error at " + serverTimeStamp));

                    System.out.println("- Waiting to be contacted for transferring Mail... \n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Ensure socket is closed when server shuts down
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    // Method to send response back to client
    private static void sendResponse(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort, String responseMessage) {
        try {
            byte[] sendData = responseMessage.getBytes(); // Convert response to bytes
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            serverSocket.send(sendPacket);  // Send response packet to client
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to check if "From" and "To" email files exist in directory; saves new email if valid
    private static boolean emailsExistsInDirectory(Email email) {
        File dir = new File("Client_Emails");
        if (!dir.exists()) {    // if directory does not exist, create it
            dir.mkdir();
        }

        // Define "From" and "To" email files in directory
        File FromEmailFile = new File(dir, email.getFrom() + ".txt");
        File ToEmailFile = new File(dir, email.getTo() + ".txt");

        // Save email if both files exist, else return false
        if (FromEmailFile.exists() && ToEmailFile.exists()) {
            saveEmailToClientDirectory(email);
            return true;
        }
        return false;
    }

    // Method to save email content to "From" and "To" files
    private static void saveEmailToClientDirectory(Email email) {
        File dir = new File("Client_Emails");
        // Create files for "From" and "To" email addresses
        File fromFile = new File(dir, email.getFrom() + ".txt");
        File toFile = new File(dir, email.getTo() + ".txt");

        try {
            // Append email content to "From" file
            BufferedWriter writer = new BufferedWriter(new FileWriter(fromFile, true));
            writer.write("-------------Sent Email-----------------\n");
            writer.write("From: " + email.getFrom() + "\n");
            writer.write("To: " + email.getTo() + "\n");
            writer.write("Subject: " + email.getSubject() + "\n");
            writer.write("Timestamp: " + email.getTimestamp() + "\n");
            writer.write("Body: " + email.getBody() + "\n");
            writer.write("----------------------------------------\n");
            writer.close();

            // Append email content to "To" file
            writer = new BufferedWriter(new FileWriter(toFile, true)); // 'true' to append
            writer.write("-------------Received Email------------\n");
            writer.write("From: " + email.getFrom() + "\n");
            writer.write("To: " + email.getTo() + "\n");
            writer.write("Subject: " + email.getSubject() + "\n");
            writer.write("Timestamp: " + email.getTimestamp() + "\n");
            writer.write("Body: " + email.getBody() + "\n");
            writer.write("----------------------------------------\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Email class for managing and validating email details
class Email {
    private String from;
    private String to;
    private String subject;
    private String body;
    private String timestamp;

    // Getter and setter methods for each email field
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    // Parse email content and validate basic fields
    public boolean parseEmailNValidate(String message) {
        String[] lines = message.split("\n");  // Split by line to identify headers

        // Extract fields based on header identifiers
        for (String line : lines) {
            if (line.startsWith("From: ")) {
                this.from = line.substring(6).trim(); // Extract 'From' email
            } else if (line.startsWith("To: ")) {
                this.to = line.substring(4).trim(); // Extract 'To' email
            } else if (line.startsWith("Subject: ")) {
                this.subject = line.substring(9).trim(); // Extract subject
            } else if (line.startsWith("Body: ")) {
                this.body = line.substring(6).trim(); // Extract body
            } else if (line.startsWith("Timestamp: ")) {
                this.timestamp = line.substring(11).trim(); // Extract timestamp
            }
        }
        // Validate required fields and basic email format (contains "@" and ".")
        return from != null && to != null && timestamp != null && this.to.contains("@") && this.to.contains(".") && this.from.contains("@") && this.from.contains(".");
    }
}
