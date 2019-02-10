//2016271
//Surabhi S Nath

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;


//RECEIVER
public class UDPReliability_Server 
{
    public byte[] generate(int val, int space)
    {
    	String string1 = val+"";
    	String string2 = space+"";
    	String string = string1+"_"+string2;
        byte[] buffer = string.getBytes();
        return buffer;
    }
    
    public int getseqnum(DatagramPacket packet)
    {
        String s = new String(packet.getData(), packet.getOffset(), packet.getLength());
        return Integer.parseInt(s);
    }
   
    
    public static void main(String[]args) throws IOException 
    {
    	System.out.println("SERVER STARTED");
    	
    	Scanner sc = new Scanner(System.in);
    	
    	UDPReliability_Server obj = new UDPReliability_Server();
        int receiving_port = 3000;
        int sending_port = 4000;
       // int sending_port = 3456;
        int Current_packet = -1;
        int Expecting_packet = 0;
        
        byte[] indata = new byte[1000];
        
        DatagramPacket packet;
        InetAddress ip = InetAddress.getLocalHost();
        //InetAddress ip = InetAddress.getByName("192.168.59.155");
        //ip = InetAddress.getByName("192.168.59.155");
        DatagramSocket recv = new DatagramSocket(receiving_port);
        DatagramSocket send = new DatagramSocket(5000);
        
        System.out.println("Waiting for Packets");
        
        ArrayList<Integer> buffer = new ArrayList<Integer>();
        System.out.println("Provide Buffer Size");
        int leng = sc.nextInt();
        double time1 = System.currentTimeMillis();
        
        double time2;
        
        while(true)
        {
        	time2 = System.currentTimeMillis();
        	
        	if((time2 - time1)>=200)
        	{
        		buffer = new ArrayList<Integer>();
        		time1 = System.currentTimeMillis();
        	}
        	
        	packet = new DatagramPacket(indata, indata.length);
        	
        	recv.receive(packet);
            int seqnum = obj.getseqnum(packet);
            System.out.println("Received packet " + seqnum);
            
            if(seqnum == Expecting_packet)
            {
                //Send ACK
            	buffer.add(seqnum);
                byte[] outdata = new byte[1000];
                outdata = obj.generate(seqnum,leng - buffer.size());
                DatagramPacket ack = new DatagramPacket(outdata, outdata.length, ip, sending_port); 
                send.send(ack);
                //System.out.println("Sent Ack " + Expecting_packet);
                System.out.println("Client"+seqnum);
                Expecting_packet++;
                Current_packet = seqnum;
            } 
            
            else// if(seqnum > Expecting_packet)
            {
                byte[] outdata = new byte[1000];
                outdata = obj.generate(Current_packet,leng - buffer.size());
                send.send(new DatagramPacket(outdata, outdata.length, ip, sending_port));
                System.out.println("OUT OF ORDER, DISCARDED");
               // System.out.println("Sent Ack " + Current_packet);
            }
            
//            else
//            {
//            	System.out.println("SHOULD NOT GET PRINTED: "+seqnum);
//            }

            indata = new byte[1000];
            
        }
        //recv_1.close();
    }
}