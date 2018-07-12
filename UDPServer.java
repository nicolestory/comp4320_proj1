import java.io.*;
import java.net.*;

class UDPServer {
   public static void main(String args[]) throws Exception     
   {
      DatagramSocket serverSocket = new DatagramSocket(9876);
      byte[] receiveData = new byte[1024];
      byte[] sendData  = new byte[1024];
      while(true)
      {
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         serverSocket.receive(receivePacket);
         String sentence = new String(receivePacket.getData());
         InetAddress IPAddress = receivePacket.getAddress();
         int port = receivePacket.getPort();
         String capitalizedSentence = sentence.toUpperCase();
         sendData = capitalizedSentence.getBytes();
         DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
         serverSocket.send(sendPacket);
      }
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
