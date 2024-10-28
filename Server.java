import java.io.*;
import java.net.*;
import java.text.*;
import java.time.*;
import java.time.format.*;

public class Server {
    static int port = 1234;    // Port number

    public static void main(String[] args) {
        int receivedEmailCount = 0;   // counter of received emails (for better readability)

        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(port); // Create server socket
            byte[] receiveData = new byte[1024];     // Buffer to store incoming data

            System.out.printf("- Mail Server Starting at host: %s \n", InetAddress.getLocalHost().getHostName());
            System.out.println("- Mail Server is listening on port " + port);
            System.out.println ("- Waiting for a client connection.. \n");

            //boolean isConnected = false;
            while(true) {
                // receive  packet
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);


                // get client address and port
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // convert packet data to string
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // check for connection request (better to comment)
                if(message.startsWith ("CONNECTION-REQUEST" )){
                    sendResponse ( serverSocket, clientAddress, clientPort, InetAddress.getLocalHost().getHostName() );
                    serverSocket.receive(receivePacket);
                    message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if(message.startsWith ("CONNECTION-REQUEST-CONFIRM" )){
                        //isConnected = true;
                        System.out.println ("- Client connected" );
                    }
                    continue;
                }
                
                // create Email object
                Email email = new Email();

                // Parse and validate  email
                if (email.parseEmailNValidate (message) && emailsExistsInDirectory(email)) {
                    // print email
                    receivedEmailCount++;
                    System.out.printf ("\n**** [NEW EMAIL | Email No: %d] ****\n",receivedEmailCount);
                    System.out.println("Mail Received from " + clientAddress.getHostName ());
                    System.out.println("From: " + email.getFrom());
                    System.out.println("To: " + email.getTo());
                    System.out.println("Subject: " + email.getSubject());
                    System.out.println("Time: " + email.getTimestamp());
                    System.out.println(email.getBody());
                    System.out.println("***********************************");

                    // send "250 OK" response
                    String serverTimeStamp = LocalDateTime.now().format( DateTimeFormatter.ofPattern("EEE. MMM. d, yyyy HH:mm"));

                    System.out.print( "- The Header fields are verified.\n- Sending \"250 OK\"\n" );
                    sendResponse(serverSocket, clientAddress, clientPort, ("250 OK: Email received successfully at " + serverTimeStamp));

                    System.out.println ("\n- Waiting to be contacted for transferring Mail... \n" );
                }
                else {
                    if(message.equals("Client disconnecting" )) {
                        System.out.println (message );
                        System.out.println ("- Waiting to be contacted for transferring Mail... \n\n");
                        continue;
                    }
                    // send "501 Error" response
                    String serverTimeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE. MMM. d, yyyy HH:mm"));
                    sendResponse( serverSocket , clientAddress , clientPort , "501 Error: at " + serverTimeStamp);
                    System.out.print( "- The Header fields are not valid.\n- Sending \"501 Error\"\n" );
                    sendResponse(serverSocket, clientAddress, clientPort, ("501 Error at " +serverTimeStamp));

                    System.out.println ("- Waiting to be contacted for transferring Mail... \n" );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    // Method to send response back
    private static void sendResponse(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort, String responseMessage) {
        try {
            byte[] sendData = responseMessage.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            serverSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to check if "from" and "to" emails exists in the server list of emails
    private static boolean emailsExistsInDirectory ( Email email ) {
        File dir = new File("Client_Emails");
        if (!dir.exists()) {    // if the dir does not exist, create one
            dir.mkdir();
        }

        File FromEmailFile = new File(dir, email.getFrom() +".txt");
        File ToEmailFile = new File(dir, email.getTo() +".txt");

        if(FromEmailFile.exists() && ToEmailFile.exists()) { // return true if emails in list
            saveEmailToClientDirectory(email);
            return true;
        }
        return false;
    }

    private static void saveEmailToClientDirectory(Email email) {
        File dir = new File("Client_Emails");
        // Create files for "From" and "To" email addresses
        File fromFile = new File(dir, email.getFrom() + ".txt");
        File toFile = new File(dir, email.getTo() + ".txt");

        try {
            // Write email content to the "From" file
            BufferedWriter writer = new BufferedWriter(new FileWriter(fromFile,true));
            writer.write("-------------Sent Email-----------------\n");
            writer.write("From: " + email.getFrom() + "\n");
            writer.write("To: " + email.getTo() + "\n");
            writer.write("Subject: " + email.getSubject() + "\n");
            writer.write("Timestamp: " + email.getTimestamp() + "\n");
            writer.write("Body: " + email.getBody() + "\n");
            writer.write("----------------------------------------\n");
            writer.close();

            // Write email content to the "To" file
            writer = new BufferedWriter(new FileWriter(toFile,true)); // true make it appends info
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

// Email class
class Email {
    private String from;
    private String to;
    private String subject;
    private String body;
    private String timestamp;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp ( String timestamp ) {
        this.timestamp = timestamp;
    }

    // Parse email and validate
    public boolean parseEmailNValidate ( String message) {
        String[] lines = message.split("\n");  // split headers indicated by ending new line

        for (String line : lines) {
            if (line.startsWith("From: ")) {
                this.from = line.substring(6).trim(); // extract email
            } else if (line.startsWith("To: ")) {
                this.to = line.substring(4).trim(); //extract..
            } else if (line.startsWith("Subject: ")) {
                this.subject = line.substring(9).trim();
            } else if (line.startsWith("Body: ")) {
                this.body = line.substring(6).trim();
            } else if (line.startsWith("Timestamp: ")) {
                this.timestamp = line.substring(11).trim();
            }
        }
        // Check if all headers are !null and "to" and "from" emails is valid by checking if (@ and .) exists in them
        return from != null && to != null && timestamp != null && this.to.contains("@") && this.to.contains(".") && this.from.contains("@") && this.from.contains(".");
    }
}
