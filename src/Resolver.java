// IN2011 Computer Networks
// Coursework 2024/2025 Resit
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

// DO NOT EDIT starts
interface ResolverInterface {
    public void setNameServer(InetAddress ipAddress, int port) throws Exception;

    public InetAddress iterativeResolveAddress(String domainName) throws Exception;
    public String iterativeResolveText(String domainName) throws Exception;
    public String iterativeResolveName(String domainName, int type) throws Exception;
}
// DO NOT EDIT ends


public class Resolver implements ResolverInterface {

    private InetAddress rootServerAddress;
    private int rootServerPort;

    public void setNameServer(InetAddress ipAddress, int port) throws Exception {
        this.rootServerAddress = ipAddress;
        this.rootServerPort = port;
    }

    public InetAddress iterativeResolveAddress(String domainName) throws Exception {
            // You can assume that domainName is a valid domain name.
            //
            // Performs a iterative resolution for domainName's A resource
            // record using the name server given by setNameServer.
            //
            // If the domainName has A records, it returns the IP
            // address from one of them.  If there is no record then it
            // returns null.  In any other case it throws an informative
            // exception.

            // start the seaarch from root servver
            InetAddress nextServerToQuery = rootServerAddress;

            // keep looping until answer is found or fail
            int queriesSent = 0;
            while (queriesSent < 20) { // Safety limit to prevent infinite loops.

                System.out.println("Queery started on server: " + nextServerToQuery.getHostAddress() + "for the domain " + domainName);

                //build packet
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                DataOutputStream dataStream = new DataOutputStream(byteStream);

                //header
                dataStream.writeShort((short) new Random().nextInt()); // transaction ID
                dataStream.writeShort (0x0000);
                dataStream.writeShort(1); // one question
                dataStream.writeShort(0); // answer count
                dataStream.writeShort(0); // authority count
                dataStream.writeShort(0); // additional count

                //question
                String[] domainParts = domainName.split("\\.");
                for (String part : domainParts) {
                    byte[] partBytes = part.getBytes("UTF-8");
                    dataStream.writeByte(partBytes.length);
                    dataStream.write(partBytes);
                }
                dataStream.writeByte(0);
                dataStream.writeShort(1);
                dataStream.writeShort(1);

            }

            throw new Exception("Not implemented yet");



    }

    
    public String iterativeResolveText(String domainName) throws Exception {
	// You can assume that domainName is a valid domain name.
	//
	// Performs a iterative resolution for domainName's TXT resource
	// record using the name server given by setNameServer.
	//
	// If the domainName has TXT records, it returns the string
	// contained one of the records. If there is no record then it
	// returns null.  In any other case it throws an informative
	// exception.
	throw new Exception("Not implemented");
    }
    
    public String iterativeResolveName(String domainName, int type) throws Exception {
	// You can assume that domainName is a valid domain name.
	//
	// You can assume that type is one of NS, MX or CNAME.
	//
	// Performs a iterative resolution for domainName's resource
	// record using the name server given by setNameServer.
	//
	// If the domainName has appropriate records, it returns the
	// domain name contained in one of the records. If there is no
	// record then it returns null.  In any other case it throws
	// an informative exception.
	throw new Exception("Not implemented");
    }
}
