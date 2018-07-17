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
         System.out.println(result[0]);
         System.out.println(result[1]);
         String response = result[0];
         for (byte[] packet : packets) {
            DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, IPAddress, port);
            serverSocket.send(sendPacket);
            System.out.println("Sent the packet back.");
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
   public static boolean errorDetected(byte[] receiveData) {
      int checkSum;
      boolean errorExists = false;
      String originalMessage = new String(receiveData);
      
      if (errorExists) {
         String packetInfo = new String(receiveData);
         System.out.println("\nAn error was detected in the following packet: ");
         System.out.println(originalMessage);
         System.out.println("\nTime Out!");
      }
      return errorExists;
   }
   public static byte checkSum(byte[] data) {
      byte sum = 0;
   
      for (int i = 1; i < data.length; i++) {
         sum += data[i];
      }
      return sum;
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
   
      byte[][] segmentationMatrix = new byte[(int)(fileSize / 256.0) + 2][256];
      int numBytes = segmentationMatrix.length - 2;
   
      byte[] fileInBytes = new byte[fileSize];//[(int)(fileSize / 256.0) + 1];
      FileInputStream fis = new FileInputStream(f);
      
      System.out.println("\nfileInBytes.len " + fileInBytes.length);
      System.out.println("fileSize " + fileSize);
   
      fis.read(fileInBytes);
      fis.close();
      
      int lastPacketSize = 0;      
   
      for (int i = 0; i < numBytes; i++) {
         for (int j = 3; j < 256; j++) {
            //System.out.println("i: " + i + ", j: " + j);
            segmentationMatrix[i][j] = fileInBytes[(i * 253) + j - 3];
            if (segmentationMatrix[i][j] == 0b0) {
               lastPacketSize = j;
            }
         }
      }
   
      // Get last packet size:
   
   
      // Header info:
      for (int k = 0; k < numBytes; k++) {
         segmentationMatrix[k][0] = (byte) checkSum(segmentationMatrix[k]); // Checksum
         segmentationMatrix[k][1] = (byte) k; // Sequence number
         // Packet Size
         if (k == fileInBytes.length - 1) {
            segmentationMatrix[k][2] = (byte) (lastPacketSize - 1);
         }
         else {
            segmentationMatrix[k][2] = (byte) 255;
         }
      }
   
      return segmentationMatrix;
   
   }

   /*public static void send(String filename, int portNumber, InetAddress IPAddress) {
	File f = new File(filename);
	int fileSize = (int) f.length();
	byte[][] segmentationMatrix = segmentation(filename);
	byte[] packet = new byte[256];
	for (int i = 0; i < fileSize; i++) {
		for (int j = 0; j < 256; j++) {
			packet[j] = segmentationMatrix[i][j];
		}
		
	}

   }*/
}	
