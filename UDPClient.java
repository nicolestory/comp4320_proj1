import java.io.*;
import java.net.*;

class UDPClient {
   public static void main(String args[]) throws Exception
   {
      BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress IPAddress = InetAddress.getByName("hostname");
      byte[] sendData = new byte[1024];
      byte[] receiveData = new byte[1024];
      String sentence = inFromUser.readLine();
      sendData = sentence.getBytes();
     
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
      clientSocket.send(sendPacket);
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      clientSocket.receive(receivePacket);
      String modifiedSentence = new String(receivePacket.getData());
      System.out.println("FROM SERVER:" + modifiedSentence);
      clientSocket.close();
   }
  public static boolean errorDetected(byte[] receiveData) {
      int checkSum;
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
         System.out.println("\nTime Out!");
      }
      return errorExists;
   }
   
   public static int checkSum(byte[] data) {
      int sum = 0;
   
      for (int i = 0; i < data.length; i++) {
         sum += (int) data[i];
      }
      return sum;
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
}
