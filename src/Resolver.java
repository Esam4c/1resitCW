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
                dataStream.writeByte(0); //end of domain name
                dataStream.writeShort(1); //qtype = A (ip address)
                dataStream.writeShort(1); //qclass = internet

                byte[] queryBytes = byteStream.toByteArray();
                DatagramSocket socket = new DatagramSocket();
                DatagramPacket queryPacket = new DatagramPacket(queryBytes, queryBytes.length, nextServerToQuery, rootServerPort);
                socket.send(queryPacket);

                byte[] responseBuffer = new byte[1024]; // buffer for iterative responses
                DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                socket.receive(responsePacket);
                socket.close();

                //parse response
                ByteArrayInputStream responseByteStream = new ByteArrayInputStream(responseBuffer);
                DataInputStream responseDataStream = new DataInputStream(responseByteStream);

                //read header
                short responseID = responseDataStream.readShort();
                if (responseID =! transactionID) {
                    throw new Exception("ERROR! TransactionID mismatch");
                }
                responseDataStream.readShort();//flags
                short questions = responseDataStream.readShort();
                short answerCount = responseDataStream.readShort();
                short authorityCount = responseDataStream.readShort();
                short additionalCount = responseDataStream.readShort();

                //move stream position past question section
                int questionsToSkip = questions;
                while (questionsToSkip > 0) {
                    int labelSize;
                    do {
                        labelSize = responseDataStream.readUnsignedByte();
                        if (labelSize > 0) {
                            responseDataStream.skip(labelSize);
                        }
                    } while (labelSize != 0);

                    //skip qtype and qclass fields
                    responseDataStream.skipBytes(4);
                    questionsToSkip--;
                }

                // checcks in answer section has our result
                if (answerCount > 0) {
                    int answersToProcess = answerCount;
                    while (answersToProcess > 0) {
                        //read all fields of the record
                        short namePointer = responseDataStream.readShort();
                        short resourceRecordType = responseDataStream.readShort();
                        short resourceRecordClass = responseDataStream.readShort();
                        int timeToLive = responseDataStream.readInt();
                        short resourceDataLength = responseDataStream.readShort();

                        //check for 'A' that we are looking for
                        if (resourceRecordType == 1) {
                            byte[] addressBytes = new byte[resourceDataLength];
                            responseDataStream.readFully(addressBytes);
                            //successful clause
                            return InetAddress.getByAddress(addressBytes);
                        } else {//fail, skip over data to next answer
                            responseDataStream.skipBytes(resourceDataLength);
                        }
                        answersToProcess--;


                    }
                }

                queriesSent++;
                break;

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
