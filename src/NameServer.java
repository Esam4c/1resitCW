// IN2011 Computer Networks
// Coursework 2024/2025 Resit
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DO NOT EDIT starts
interface NameServerInterface {
    public void setNameServer(InetAddress ipAddress, int port) throws Exception;
    public void handleIncomingQueries(int port) throws Exception;
}
// DO NOT EDIT ends


public class NameServer implements NameServerInterface {

    private Resolver internalResolver;
    private Map<String, byte[]> cache = new HashMap<>(); //cache to store answers we've found

    public void setNameServer(InetAddress ipAddress, int port) throws Exception {
	// This method must be called first.
	// You can assume that the IP address and port number lead to
	// a working domain name server which supports iterative
	// queries.

        //create instance of our own resolver and establish root server
        internalResolver = new Resolver();
        internalResolver.setNameServer(ipAddress, port);
    }

    public void handleIncomingQueries(int port) throws Exception {
	// You can assume that port is a valid UDP port number.
	//
	// Listens for incoming DNS queries on the given port number
	// and responds to them by using cached values and performing
	// iterative resolution.
	DatagramSocket serverSocket = new DatagramSocket(port);
    System.out.println("DNS Server is active and listening on port " + port);

    //server running endless - infinite loop start
        while (true) {
            byte[] incomingBuffer = new byte[1024];
            DatagramPacket incomingPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);

            //pause program until query arrives
            serverSocket.receive(incomingPacket);

            //when query shows up, get client's address and port for reply;
            InetAddress clientAddress = incomingPacket.getAddress();
            int clientPort = incomingPacket.getPort();

            System.out.println("Received query from client: " + clientAddress.getHostAddress());

            //create streams to read incoming data
            ByteArrayInputStream byteStream = new ByteArrayInputStream(incomingPacket.getData());
            DataInputStream dataStream = new DataInputStream(byteStream);

            // 1. Read the header to get the Transaction ID
            short transactionID = dataStream.readShort();
            // We can skip the rest of the header for now
            dataStream.skipBytes(10);

            // 2. Parse the question to get the domain name and type
            String domainName = helperReadName(dataStream, incomingPacket.getData());
            short queryType = dataStream.readShort();
            short queryClass = dataStream.readShort();

            System.out.println("Client is asking for type " + queryType + " record for " + domainName);

            String cacheKey = domainName + ":" + queryType;
            byte[] responsePacketBytes;

            if(cache.containsKey(cacheKey)) {
                //case 1, answer is in cache
                System.out.println("Found answer in cache for " + domainName);
                byte[] cachedAnswerRecord = cache.get(cacheKey);
                responsePacketBytes = buildResponsePacket(incomingPacket.getData(), transactionID, cachedAnswerRecord);
            } else {//case 2 answer isnt in cache so needs to be resolved
                System.out.println("Answer is not in cache. Starting to resolve " + domainName + "...");
                byte[] newAnswerRecord = null;
                //NEED TO CONTINUE FROM HERE
            }
        }
    }

    private String helperReadName(DataInputStream dataStream, byte[] fullPacket) throws Exception {
        List<String> labels = new ArrayList<>();
        int length = dataStream.readUnsignedByte();
        while (length != 0) {
            if ((length & 0xC0) == 0xC0) { // Check if it's a pointer
                int offset = ((length & 0x3F) << 8) + dataStream.readUnsignedByte();
                DataInputStream newStream = new DataInputStream(new ByteArrayInputStream(fullPacket, offset, fullPacket.length - offset));
                labels.add(helperReadName(newStream, fullPacket));
                return String.join(".", labels);
            } else { // It's a normal label
                byte[] labelBytes = new byte[length];
                dataStream.readFully(labelBytes);
                labels.add(new String(labelBytes, "UTF-8"));
                length = dataStream.readUnsignedByte();
            }
        }
        return String.join(".", labels);
    }
}
