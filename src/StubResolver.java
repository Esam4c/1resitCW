// IN2011 Computer Networks
// Coursework 2024/2025 Resit
//
// Submission by
// YOUR_NAME_GOES_HERE Mohammed Choudhury
// YOUR_STUDENT_ID_NUMBER_GOES_HERE 220077503
// YOUR_EMAIL_GOES_HERE Mohammed.Choudhury4@city.ac.uk

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.util.Random;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;


// DO NOT EDIT starts
interface StubResolverInterface {
    public void setNameServer(InetAddress ipAddress, int port) throws Exception;

    public InetAddress recursiveResolveAddress(String domainName) throws Exception;
    public String recursiveResolveText(String domainName) throws Exception;
    public String recursiveResolveName(String domainName, int type) throws Exception;
}
// DO NOT EDIT ends


public class StubResolver implements StubResolverInterface {

    private InetAddress dnsServerAddress;
    private int dnsServerPort;

    public void setNameServer(InetAddress ipAddress, int port) throws Exception {
        // This method must be called first.
        // You can assume that the IP address and port number lead to
        // a working domain name server which supports recursive
        // queries.
        this.dnsServerAddress = ipAddress; //assign address
        this.dnsServerPort = port; //asign port
    }

    public InetAddress recursiveResolveAddress(String domainName) throws Exception {
        // You can assume that domainName is a valid domain name.
        //
        // Performs a recursive resolution for domainName's A resource
        // record using the name server given by setNameServer.
        //
        // If the domainName has A records, it returns the IP
        // address from one of them.  If there is no record then it
        // returns null.  In any other case it throws an informative
        // exception.
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream); //build packet in memory

        //jumpto query
        short transactionNum = (short) new Random().nextInt();
        dataStream.writeShort(transactionNum); //random number for query

        dataStream.writeShort(0x0100); //start of recursive query
        dataStream.writeShort(1); //ask once
        dataStream.writeShort(0); //answer 0 for queryt
        dataStream.writeShort(0); //authority count
        dataStream.writeShort(0);

        //domain name/qname
        String[] domainParts = domainName.split("\\."); //split domain name
        for (int i = 0; i < domainParts.length; i++) {
            byte[] labelBytes = domainParts[i].getBytes("UTF-8");
            dataStream.writeByte(labelBytes.length);
            dataStream.write(labelBytes);
        }
        dataStream.writeByte(0);

        short recordType = 1;
        dataStream.writeShort(recordType);  // A record type 1

        short recordClass = 1;
        dataStream.writeShort(recordClass);  //internet class type 1

        byte[] dnsQueryBytes = byteStream.toByteArray(); //getting raw bytes of query being built

        //UDP packet and socket send/recieve setup
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packetSending = new DatagramPacket(dnsQueryBytes, dnsQueryBytes.length, dnsServerAddress, dnsServerPort);
        socket.send(packetSending);

        // buffer to hold response of server
        byte[] serverResponseBuffer = new byte[512];
        DatagramPacket responsePacket = new DatagramPacket(serverResponseBuffer, serverResponseBuffer.length);
        socket.receive(responsePacket);

        socket.close();

        //jumpto response

        //inputstreams for reading response bytes

        ByteArrayInputStream responseStreamBytes = new ByteArrayInputStream(serverResponseBuffer);
        DataInputStream responseStreamData = new DataInputStream(responseStreamBytes);

        short receivedTransactionID = responseStreamData.readShort();
        short flags = responseStreamData.readShort();
        short questions = responseStreamData.readShort();
        short answers = responseStreamData.readShort();
        short authority = responseStreamData.readShort();
        short additional = responseStreamData.readShort();
        //error-check for query=response
        if (receivedTransactionID != transactionNum) {
            throw new Exception("QUERY FAILED! Transaction IDS don't match!");
        }

        for (int i = 0; i < questions; i++) {
            int labelLength;
            while ((labelLength = responseStreamData.readByte()) != 0) {
                responseStreamData.skipBytes(labelLength);
            }
            responseStreamData.skipBytes(4);
        }

        //loop for 'A' record
        for (int i = 0; i < answers; i++) {
            //read name of field
            responseStreamData.readShort();//skip namepointer
            short answerType = responseStreamData.readShort();
            responseStreamData.readShort(); // class
            responseStreamData.readInt();   // TTL
            short dataLen = responseStreamData.readShort();

            // checks if 'A' record
            if (answerType == 1) {
                byte[] ipBytes = new byte[dataLen];
                responseStreamData.readFully(ipBytes);
                return InetAddress.getByAddress(ipBytes);
            } else {
                // if not, skip over
                responseStreamData.skipBytes(dataLen);
            }
        }
        return null;
    }

    public String recursiveResolveText(String domainName) throws Exception {
        // You can assume that domainName is a valid domain name.
        //
        // Performs a recursive resolution for domainName's TXT resource
        // record using the name server given by setNameServer.
        //
        // If the domainName has TXT records, it returns the string
        // contained one of the records. If there is no record then it
        // returns null.  In any other case it throws an informative
        // exception.

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        //build query
        short transactionNum = (short) new Random().nextInt();
        dataStream.writeShort(transactionNum);
        dataStream.writeShort(0x0100);
        dataStream.writeShort(1);
        dataStream.writeShort(0);
        dataStream.writeShort(0);
        dataStream.writeShort(0);

        String[] domainParts = domainName.split("\\.");
        for (int i = 0; i < domainParts.length; i++) {
            byte[] labelBytes = domainParts[i].getBytes("UTF-8");
            dataStream.writeByte(labelBytes.length);
            dataStream.write(labelBytes);
        }
        dataStream.writeByte(0);

        // reequesting TXT record
        short recordType = 16;
        dataStream.writeShort(recordType);

        short recordClass = 1;
        dataStream.writeShort(recordClass);

        byte[] dnsQueryBytes = byteStream.toByteArray();
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packetSending = new DatagramPacket(dnsQueryBytes, dnsQueryBytes.length, dnsServerAddress, dnsServerPort);
        socket.send(packetSending);
        byte[] serverResponseBuffer = new byte[512];
        DatagramPacket responsePacket = new DatagramPacket(serverResponseBuffer, serverResponseBuffer.length);
        socket.receive(responsePacket);
        socket.close();

        ByteArrayInputStream responseStreamBytes = new ByteArrayInputStream(serverResponseBuffer);
        DataInputStream responseStreamData = new DataInputStream(responseStreamBytes);

        short receivedTransactionID = responseStreamData.readShort();
        if (receivedTransactionID != transactionNum) {
            throw new Exception("QUERY FAILED! Transaction IDS don't match!");
        }
        responseStreamData.readShort(); //flags
        short questions = responseStreamData.readShort();
        short answers = responseStreamData.readShort();
        responseStreamData.readShort(); //authority
        responseStreamData.readShort(); //additionkl

        for (int i = 0; i < questions; i++) {
            int labelLength;
            while ((labelLength = responseStreamData.readByte()) != 0) {
                responseStreamData.skipBytes(labelLength);
            }
            responseStreamData.skipBytes(4);
        }

        //loop through answers to find txt record
        for (int i = 0; i < answers; i++) {
            responseStreamData.readShort(); // Skip name pointer
            short answerType = responseStreamData.readShort();
            responseStreamData.readShort(); // class
            responseStreamData.readInt();   // TTL
            short dataLen = responseStreamData.readShort();

            //check if its txt record

            if (answerType == 16) {
                int txtLength = responseStreamData.readByte();
                byte[] txtBytes = new byte[txtLength];
                responseStreamData.readFully(txtBytes);
                return new String(txtBytes, "UTF-8");
            } else {
                responseStreamData.skipBytes(dataLen);
            }

        }
        return null;
    }

    public String recursiveResolveName(String domainName, int type) throws Exception {
        // You can assume that domainName is a valid domain name.
        //
        // You can assume that type is one of NS, MX or CNAME.
        //
        // Performs a recursive resolution for domainName's resource
        // record using the name server given by setNameServer.
        //
        // If the domainName has appropriate records, it returns the
        // domain name contained in one of the records. If there is no
        // record then it returns null.  In any other case it throws
        // an informative exception.

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        short transactionNum = (short) new Random().nextInt();
        dataStream.writeShort(transactionNum);
        dataStream.writeShort(0x0100);
        dataStream.writeShort(1);
        dataStream.writeShort(0);
        dataStream.writeShort(0);
        dataStream.writeShort(0);

        String[] domainParts = domainName.split("\\.");
        for (int i = 0; i < domainParts.length; i++) {
            byte[] labelBytes = domainParts[i].getBytes("UTF-8");
            dataStream.writeByte(labelBytes.length);
            dataStream.write(labelBytes);
        }
        dataStream.writeByte(0);

        dataStream.writeShort(type);
        dataStream.writeShort(1);

        byte[] dnsQueryBytes = byteStream.toByteArray();
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packetSending = new DatagramPacket(dnsQueryBytes, dnsQueryBytes.length, dnsServerAddress, dnsServerPort);
        socket.send(packetSending);
        byte[] serverResponseBuffer = new byte[512];
        DatagramPacket responsePacket = new DatagramPacket(serverResponseBuffer, serverResponseBuffer.length);
        socket.receive(responsePacket);
        socket.close();

        ByteArrayInputStream responseStreamBytes = new ByteArrayInputStream(serverResponseBuffer);
        DataInputStream responseStreamData = new DataInputStream(responseStreamBytes);

        short receivedTransactionID = responseStreamData.readShort();
        if (receivedTransactionID != transactionNum) {
            throw new Exception("QUERY FAILED! Transaction IDS don't match!");
        }
        responseStreamData.readShort();
        short questions = responseStreamData.readShort();
        short answers = responseStreamData.readShort();
        responseStreamData.readShort();
        responseStreamData.readShort();

        for (int i = 0; i < questions; i++) {
            int labelLength;
            while ((labelLength = responseStreamData.readByte()) != 0) {
                responseStreamData.skipBytes(labelLength);
            }
            responseStreamData.skipBytes(4);
        }

        for (int i = 0; i < answers; i++) {
            responseStreamData.readShort();
            short answerType = responseStreamData.readShort();
            responseStreamData.readShort();
            responseStreamData.readInt();
            short dataLen = responseStreamData.readShort();

            if (answerType == type) {
                if (answerType == 15) { // MX record
                    responseStreamData.skipBytes(2); // Skip preference
                }
                return helperParseName(responseStreamData, serverResponseBuffer);
            } else {
                responseStreamData.skipBytes(dataLen);
            }
        }
        return null;
    }

    //my own helper method to parse a domain name from a DNS response / handling pointers
    private String helperParseName(DataInputStream dataStream, byte[] fullPacket) throws Exception {
        List<String> labels = new ArrayList<>();
        int length = dataStream.readUnsignedByte();

        while (length != 0) {
            //check for pointer
            if ((length & 0xC0) == 0xC0) {
                int offset = ((length & 0x3F) << 8) + dataStream.readUnsignedByte();

                //create new stream from pointer's offset
                ByteArrayInputStream newStream = new ByteArrayInputStream(fullPacket, offset, fullPacket.length - offset);
                DataInputStream newStreamCast = new DataInputStream(newStream);

                //recursively parse name
                labels.add(helperParseName(newStreamCast, fullPacket));
                return String.join(".", labels);
            } else {//normal label
                byte[] labelBytes = new byte[length];
                dataStream.readFully(labelBytes);
                labels.add(new String(labelBytes, "UTF-8"));
                length = dataStream.readUnsignedByte();
            }
        }
        return String.join(".", labels);


    }
}
