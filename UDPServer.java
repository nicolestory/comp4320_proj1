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
         String capitalizedSentence = sentence.toUpperCase();
         sendData = capitalizedSentence.getBytes();
         DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
         serverSocket.send(sendPacket);
         System.out.println("Sent the packet back.");
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
      return errorExists;
   }
      public static int checkSum(byte[] sendData) {
      int sum = 0;
   
      for (int i = 0; i < sendData.length; i++) {
         sum += (int) sendData[i];
      }
      return sum;
   }
}
