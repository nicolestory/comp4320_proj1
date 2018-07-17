import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

class UDPClient {

   private static String serverHostname = "tux059";
   private static int portNumber = 10052;
   private static double gremlinProbability = 0.0;
   private static String fileName = "TestFile.html";

   public static void main(String args[]) throws Exception
   {
      if (!parse_args(args))
         return;
         
      System.out.println("UDP Server Hostname: " + serverHostname);
      System.out.println("Port Number: " + portNumber);
      System.out.println("Gremlin Probability: " + gremlinProbability);
      System.out.println("File Name: " + fileName);
      
      String request = "GET " + fileName + " HTTP/1.0";
      System.out.println("Request: " + request);
      
      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress IPAddress = InetAddress.getByName(serverHostname);
      byte[] sendData = new byte[1024];
      sendData = request.getBytes();
     
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNumber);
      clientSocket.send(sendPacket);
      System.out.println("Sent a packet!\n");
      ArrayList<byte[]> packets = new ArrayList<byte[]>();
      boolean packetsLeft = false;
      
      // Read in packets, and sort them:
      do {
         byte[] receiveData = new byte[256];
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         clientSocket.receive(receivePacket);
         System.out.println("Recieved a packet!"); 
         receiveData = receivePacket.getData();  
         System.out.println(new String(receiveData, "UTF-8")); 
         byte sequenceNumber = receiveData[1];
         if (packets.size() == sequenceNumber) {
            packets.add(receiveData);
         }
         else if (packets.size() < sequenceNumber) {
         
            for (int i = sequenceNumber; i < packets.size(); i++) {
               packets.add(null);
            }
            packets.add(sequenceNumber, receiveData);
         }
         else {
         
            packets.add(sequenceNumber, receiveData);
         }
      
         packetsLeft = morePacketsLeft(receiveData);
      } while (packetsLeft);
      clientSocket.close(); 
      
      byte[][] packetsByteList = new byte[packets.size()][];
      packetsByteList = packets.toArray(packetsByteList);
      
      // Print all packets:
      System.out.println("All packets: \n");
      for (byte[] packet : packets) {
         System.out.print(new String(
            Arrays.copyOfRange(packet, 3, 256), "UTF-8"));
      }
      System.out.println("\n");
   
      
      // Gremlin attack:
      gremlin(packetsByteList, gremlinProbability);
      
      // Error detection and printing to file:
      FileOutputStream out = new FileOutputStream("new_" + fileName);
      for (byte[] packet : packetsByteList) {
         errorDetected(packet);
         out.write(Arrays.copyOfRange(packet, 3, 256));
      }
      out.close();
   }
   
   private static boolean parse_args(String args[])
   {
      if (args.length >= 2)
      {
         serverHostname = args[0];
         int tempPortNumber = Integer.parseInt(args[1]);
         if ((10052 > tempPortNumber) || (tempPortNumber > 10055))
         {
            System.out.println("Port " + tempPortNumber + " is out of our port range.");
            System.out.println("Try a port between 10052 and 10055");
            return false;
         }
         portNumber = tempPortNumber;
         
         if (args.length >= 3)
         {
            double tempGremlin = Double.parseDouble(args[2]);
            if ((0.0 > tempGremlin) || (tempGremlin > 1.0))
            {
               System.out.println("The probability " + tempGremlin + " is not valid.");
               System.out.println("Try a decimal value between 0.0 and 1.0");
               return false;
            }
            gremlinProbability = tempGremlin;
         }
         
         if (args.length >= 4)
         {
            fileName = args[3];
         }
      }
      return true;
   }
   public static boolean errorDetected(byte[] receiveData) throws Exception {
      if (receiveData == null) {
         System.out.println("Packet was lost!");
         return true;
      }
      
      byte checkSum = checkSum(receiveData);
      
      if (receiveData[0] != checkSum) {
         System.out.println("An error was detected in packet " + receiveData[1] + ": ");
         System.out.println(new String(receiveData, "UTF-8"));
      }
      return false;
   }
   
   public static byte checkSum(byte[] data) {
      int sum = 0;
   
      for (int i = 1; i < (int) (data[2] & 0xFF); i++) {
         sum += (int) (data[i] & 0xFF);
      }
      return (byte) (sum % 256);
   }
   
   public static byte[][] gremlin(byte[][] packets, double probability) {
      if (probability == 0.0)
      {
         System.out.println("No Gremlins are attacking today. Carry on.");
         return packets;
      }
      System.out.println("Gremlins are attacking!");
      
      for (byte[] packet : packets) {
         double changePacketProbability = Math.random();
         if (changePacketProbability <= gremlinProbability) {
            System.out.println("A gremlin got a packet!");
            packet = damagePacket(packet);
         }
      }
      System.out.println();
      
      return packets;
   }
   
   public static byte[] damagePacket(byte[] packet) {
      int numBytesDamaged = 3;
      double randNumBytesDamaged = Math.random();
      if (randNumBytesDamaged < 0.5) {
         numBytesDamaged = 1;
      } else if (randNumBytesDamaged < 0.8) {
         numBytesDamaged = 2;
      }
      
      for (int i = 0; i < numBytesDamaged; i++) {
         int byteToChange = (int) (Math.random() * packet.length);
         packet[byteToChange] = (byte) (packet[byteToChange] ^ 0xFF);
         System.out.println("Changed a byte!");
      }
      
      return packet;
   }
   
   private static boolean morePacketsLeft(byte[] packet) {
      int packetSize = packet[2] & 0xFF;
      if (packet[packetSize] == 0b0) {
         return false;
      }
      return true;
   }
}
