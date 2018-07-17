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
      byte[] receiveData = new byte[256];
      ArrayList<byte[]> packets = new ArrayList<byte[]>();
      
      // Read in packets, and sort them:
      do {
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         clientSocket.receive(receivePacket);
         System.out.println("Recieved a packet!"); 
         receiveData = receivePacket.getData();  
         //System.out.println(new String(receiveData, "UTF-8")); 
         byte sequenceNumber = receiveData[1];
         System.out.println(sequenceNumber);
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
      } while (morePacketsLeft(receiveData));
      clientSocket.close(); 
      byte[][] packetsByteList = new byte[packets.size()][];
      packetsByteList = packets.toArray(packetsByteList);
      
      // Gremlin attack:
      gremlin(packetsByteList, gremlinProbability);
      
      System.out.println(new String(packetsByteList[0], "UTF-8"));
      
      // Error detection and printing to file:
      FileOutputStream out = new FileOutputStream("new_" + fileName);
      for (byte[] packet : packetsByteList) {
         //System.out.println("Packet: \n" + new String(packet, "UTF-8"));
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
      /*
      boolean errorExists = false;
      String originalMessage = new String(receiveData);
      String sumIn = getCheckSumSent(receiveData);
      byte[] falseHeader = zeroCheckSum(receiveData);
      checkSum = checkSum(falseHeader);
   
      if (sumIn.equals(Integer.toString(checkSum))) {
         System.out.println("\n" + originalMessage);
      } else {
         errorExists = true;
         String packetInfo = new String(receiveData);
         System.out.println("\nAn error was detected in the following packet: ");
         System.out.println(originalMessage);
      }
      return errorExists; */
      System.out.println("Checksum: " + checkSum + ", receiveData[0]: " + receiveData[0]);
      if (receiveData[0] != checkSum) {
         System.out.println("An error was detected in the following packet: ");
         System.out.println(new String(receiveData, "UTF-8"));
         System.out.println("Checksum: " + checkSum + ", receiveData[0]: " + receiveData[0]);
         System.out.println(receiveData[1]);
      }
      return false;
   }
   
   public static byte checkSum(byte[] data) {
      int sum = 0;
   
      for (int i = 1; i < data[2]; i++) {
         sum += (int) data[i];
      }
      System.out.println("Checksum: " + sum);
      return (byte) sum;
   } 
   
   public static String getCheckSumSent(byte[] input) {
      String checkSum = "";
   
      byte[] byteCheckSum = new byte[5];
      String info = new String(input);
      int index = info.indexOf(":") + 1;
      int j = 0;
      for (int i = index + 1; i < index + 6; i++) {
         byteCheckSum[j] = input[i];
         j++;
      }
      checkSum = new String(byteCheckSum);
   
      boolean leadingZeros = true;
      while (leadingZeros) {
         leadingZeros = checkSum.startsWith("0");
         if (leadingZeros) {
            checkSum = checkSum.substring(1);
         }
      }
      return checkSum;
   }
   
   public static byte[] zeroCheckSum(byte[] message) {
      String info = new String(message);
      int index = info.indexOf(":") + 1;
   
      for (int i = index + 1; i < index + 6; i++) {
         message[i] = 48;
      }
      return message;
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
