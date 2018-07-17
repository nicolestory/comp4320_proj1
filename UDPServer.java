import java.io.*;
import java.net.*;

class UDPServer {

   private static int portNumber = 10052;

   public static void main(String args[]) throws Exception     
   {
      if (!parse_args(args))
         return;
         
      System.out.println("Starting UDP Server on port " + portNumber);
               
      DatagramSocket serverSocket = new DatagramSocket(portNumber);
      byte[] receiveData = new byte[1024];
      byte[] sendData  = new byte[1024];
      while(true)
      {
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         System.out.println("Ready to recieve packets");
         serverSocket.receive(receivePacket);
         System.out.println("Recieved a packet!");
         String sentence = new String(receivePacket.getData());
         InetAddress IPAddress = receivePacket.getAddress();
         int port = receivePacket.getPort();
         String[] result = parseRequest(sentence);
         byte[][] packets = segmentation(result[1]);
         String response = result[0];
         for (byte[] packet : packets) {
            DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, IPAddress, port);
            serverSocket.send(sendPacket);
            System.out.println("Sent a packet back.");
         }
      }
   }
   
   private static boolean parse_args(String args[])
   {
      if (args.length >= 1)
      {
         int tempPortNumber = Integer.parseInt(args[0]);
         if ((10052 > tempPortNumber) || (tempPortNumber > 10055))
         {
            System.out.println("Port " + tempPortNumber + " is out of our port range.");
            System.out.println("Try a port between 10052 and 10055.");
            return false;
         }
         portNumber = tempPortNumber;
      }
      return true;
   }
   
   public static byte checkSum(byte[] data) {
      int sum = 0;
   
      for (int i = 1; i < (int) (data[2] & 0xFF); i++) {
         sum += (int) (data[i] & 0xFF);
      }
      return (byte) (sum % 256);
   }

   public static String[] parseRequest(String request) {
      String parsedSoFar = "";
      String file = "";
      int fileSize = 0;
      String[] s = new String[2];
      s[0] = "Invalid request";
      s[1] = file;
   
      if (request.length() < 14) {
         return s;
      }
   	
      if (!request.substring(0, 4).equals("GET ")) {
         return s;
      }
   
      parsedSoFar = request.substring(4, request.length());
   	
      int counter = 0;
   
      while (parsedSoFar.charAt(counter) != ' ') {
         counter++;
      }
   
      file = parsedSoFar.substring(0, counter);
   
      parsedSoFar = parsedSoFar.substring(file.length() + 1, parsedSoFar.length()).trim();
   
      if (!parsedSoFar.equals("HTTP/1.0")) {
         return s;
      }
   
      File f = new File(file);
      
      String fileText = "";
      try
      {
         BufferedReader br = new BufferedReader(new FileReader(file));
         for (String line; (line = br.readLine()) != null;) {
            System.out.print(line);
            fileText += line + "\n";
         }
         br.close();
      }
      catch (Exception e)
      {
         System.out.println("Oh no.");
         return s;
      }
   
      if (f.exists()) {
         s[0] = "HTTP/1.0 200 Document Follows \r\nContent-Type: text/plain\r\nContent-Length: " + f.length() + "\r\n\r\n" + fileText;
         s[1] = file;
      }
      else {
         s[0] = "Error: File not found";
      }
      return s;
   }

   public static byte[][] segmentation(String filename) throws Exception {
      int fileSize = 0;
      File f = new File(filename);
   
      fileSize = (int) f.length();
   
      byte[][] segmentationMatrix = new byte[(int)(fileSize / 253.0) + 1][256];
      int numBytes = segmentationMatrix.length - 1;
   
      byte[] fileInBytes = new byte[fileSize];
      FileInputStream fis = new FileInputStream(f);
   
      fis.read(fileInBytes);
      fis.close();
   
      for (int i = 0; i < numBytes; i++) {
         for (int j = 3; j < 256; j++) {
            segmentationMatrix[i][j] = fileInBytes[(i * 253) + j - 3];
         }
      }
   
      // Get last packet size:
      int lastPacketSize = fileSize % 253;
   
      // Header info:
      for (int k = 0; k < numBytes; k++) {
         segmentationMatrix[k][1] = (byte) k; // Sequence number
         // Packet Size
         if (k == fileInBytes.length - 1) {
            segmentationMatrix[k][2] = (byte) (lastPacketSize - 1);
         }
         else {
            segmentationMatrix[k][2] = (byte) 255;
         }
         segmentationMatrix[k][0] = checkSum(segmentationMatrix[k]); // Checksum
      
      }
      byte[] lastPacket = {0, (byte) numBytes, 4, 0b0};
      lastPacket[0] = checkSum(lastPacket);
      segmentationMatrix[numBytes] = lastPacket;
   
      return segmentationMatrix;
   
   }
}	
