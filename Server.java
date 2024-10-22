import java.io.*;
import java.net.*;
import java.text.*;
// *TimeFormat needs modif

public class Server {
    static int port = 12345;    // Port number

    public static void main(String[] args) {
        int receivedEmailCount = 0;
        //SimpleDateFormat dateFormat = new SimpleDateFormat("EEE. MMM. d, yyyy HH:mm");

        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(port); // Create server socket
            byte[] receiveData = new byte[1024];     // Buffer to store incoming data

            System.out.printf("- Mail Server Starting at host: %s \n", InetAddress.getLocalHost().getHostName());
            System.out.println("- Mail Server is listening on port " + port);
            System.out.println ("- Waiting to be contacted for transferring Mail... \n\n");

            while (true) {
                // Kill program by ctrl c

                // receive the packet
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                // get client address and port
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // convert packet data to string
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // create Email object
                Email email = new Email();

                // Parse and validate  email
                if (email.parseEmailNValidate (message) && emailsExistsInDirectory(email)) {
                    // print email
                    receivedEmailCount++;
                    System.out.printf ("\n**** [NEW EMAIL | Email No: %d] ****\n",receivedEmailCount);
                    System.out.println("Mail Received from " + clientAddress.getHostName ());
                    System.out.println("FROM: " + email.getFrom());
                    System.out.println("TO: " + email.getTo());
                    System.out.println("SUBJECT: " + email.getSubject());
                    System.out.println("TIME: " + email.getTimestamp());
                    System.out.println(email.getBody());
                    System.out.println("***********************************");

                    // send "250 OK" response
                    String serverTimeStamp = java.time.LocalDateTime.now().toString();
                    System.out.print( "- The Header fields are verified.\n- Sending \"250 OK\"\n" );
                    sendResponse(serverSocket, clientAddress, clientPort, "250 OK: Email received successfully at " + serverTimeStamp);

                    System.out.println ("- Waiting to be contacted for transferring Mail... \n" );
                }
                else {
                    // send "501 Error" response
                    sendResponse( serverSocket , clientAddress , clientPort , "501 Error" );
                    System.out.print( "- The Header fields are not valid.\n- Sending \"501 Error\"\n" );
                    sendResponse(serverSocket, clientAddress, clientPort, "501 Error");

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

    // Methods to check if "from" and "to" emails exists in the server list of emails
    private static boolean emailsExistsInDirectory ( Email email ) {
        File dir = new File("Client_Emails");
        if (!dir.exists()) {    // if the dir does not exist, create one
            dir.mkdir();
        }

        File FromEmailFile = new File(dir, email.getFrom() +".txt");
        File ToEmailFile = new File(dir, email.getTo() +".txt");

        if(FromEmailFile.exists() && ToEmailFile.exists()) { // return true if emails in list
            return true;
        }
        return false;
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

