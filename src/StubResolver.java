// IN2011 Computer Networks
// Coursework 2024/2025 Resit
//
// Submission by
// YOUR_NAME_GOES_HERE Mohammed Choudhury
// YOUR_STUDENT_ID_NUMBER_GOES_HERE 220077503
// YOUR_EMAIL_GOES_HERE Mohammed.Choudhury4@city.ac.uk

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.util.Random;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


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



	throw new Exception("Not implemented");
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
	throw new Exception("Not implemented");
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
	throw new Exception("Not implemented");
    }
}
