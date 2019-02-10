//2016271
//Surabhi S Nath

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.lang.Math;


//SENDER

class packet
{
	int seqnum;
	byte[] message;
	
	public packet(int num, byte[] mes)
	{
		seqnum = num;
		message = mes;
	}
}


//class Sender implements Runnable
//{
//	DatagramSocket sock_s;
//    int recv_port;
//    InetAddress address;
//    int send_port;
//    UDPReliability_Client ref;
//    
//    public Sender(UDPReliability_Client obj)
//    {
//    	ref = obj;
//        this.sock_s=obj.send;
//        this.recv_port=obj.receiving_port;
//    }
//    
//    public void run()
//    {
//        
//        try 
//        {
//			address = InetAddress.getLocalHost();
//		} 
//        
//        catch (UnknownHostException e3) 
//        {
//				e3.printStackTrace();
//		}
//        
//        while (ref.count < ref.windowsize) 
//        {
//        	byte[] send_data = new byte[1000];
//            try 
//            {
//            	ref.lock.acquire();
//			} 
//                
//            catch (InterruptedException e2) 
//            {
//				e2.printStackTrace();
//			}
//            
//            
//            send_data = ref.generate(ref.next_seq);
//            
//            try
//            {
//				sock_s.send(new DatagramPacket(send_data, send_data.length, address, send_port));
//            }
//                
//            catch (IOException e1) 
//            {
//				e1.printStackTrace();
//            }
//                
//            System.out.println("Sender: Sent seqNum " + ref.next_seq);
//            ref.setTimer(true);
//            ref.next_seq++;
//            ref.lock.release();
//            ref.count++;
//                
//            try 
//            {
//				Thread.sleep(2);
//            } 
//                
//            catch (InterruptedException e) 
//            {
//                	e.printStackTrace();
//            }
//        }
//    }
//}
//-------------------------------------------------------------------------------------------------------

class Receiver implements Runnable
{
	DatagramSocket sock;
    UDPReliability_Client ref;
    int Current_ack = -1;
    int Expecting_ack = 0;
    int max_pck = 3000;
    
    public Receiver(UDPReliability_Client obj)
    {
    	ref = obj;
    	sock = obj.recv;
    }
    
    //Get sequence number from ack
    
    public int getseqnum(DatagramPacket packet)
    {
    	//change line
        String s = new String(packet.getData(), packet.getOffset(), packet.getLength());
        System.out.println(s);
        String val[] = s.split("_");
        ref.allowed = Integer.parseInt(val[1]);
        return Integer.parseInt(val[0]);
    }
	
    public void run()
    {
    	//Constantly listening for ACK
    	
        byte[] indata = new byte[1000];
        
        DatagramPacket packet;
        
        System.out.println("Waiting for ACKS");
        
        while (true)
        {
        	packet = new DatagramPacket(indata, indata.length); 
        	
            try 
            {
				sock.receive(packet);
			} 
            
            catch (IOException e) 
            {
				e.printStackTrace();
			}
            
            int ack = getseqnum(packet);
            System.out.println("Received ACK "+ack);
            
            if(ack == Expecting_ack)
            {
            	if(ack == max_pck - 1)
            	{
            		ref.timer.cancel();
            		break;
            	}

            	Current_ack = ack;
            	Expecting_ack++;
            	
            	//Shift window
            	if(ref.windowstart+ref.windowsize != max_pck)
            	{
            		ref.windowstart++;
            		ref.windowend++;
            		if(ref.allowed>=1)
	            	{
	            		byte[] outdata = ref.makepacket(ref.windowend);
	            		packet = new DatagramPacket(outdata, outdata.length, ref.ip, ref.sending_port);
	            		
	            		//Each time while sending packet, start fresh new timer
	            		double p = Math.random();
	            		
	            		//if(p<=0.95)
	            		{
	            			try 
	    	        		{
	    						ref.send.send(packet);
	    					} 
	    	        		
	    	        		catch (IOException e) 
	    	        		{
	    						e.printStackTrace();
	    					}
	    	
	    	        		ref.startTimer(true);
	    	        		//System.out.println("Sent packet "+ref.windowend);
	            		}
	            	}
            	}

            	
//            	if(ref.windowend == 1001)
//            	{
//            		ref.timer.cancel();
//            		break;
//            	}
            	
            	//Send next packet
            	
            	
	        		
            }
            
            if(ack <= Current_ack)
            {
            	//DUPLICATE ACK, Ignore
            }
            
            else
            {
            	
            }               
        }
    }
}

//-----------------------------------------------------------------------------------
public class UDPReliability_Client 
{
	//Take window size as input
    int windowsize;
    int windowstart = 0;
    int windowend = windowsize - 1;
    
    int next_seq = 0;
    int prev_seq = 0;
    int count = 0;
    //Semaphore lock = new Semaphore(1);
    int timeout_dur = 1000;
    Timer timer;
    //int sending_port = 3000;
    int sending_port = 3000;
	int receiving_port = 4000;
	InetAddress ip;
	DatagramSocket send;
	DatagramSocket recv;
	int allowed;
    
    
    public void startTimer(boolean val)
    {
        if (timer != null)
            timer.cancel();	//Delete existing timeout, if earlier object times out, newer will also eventually timeout since cum
        
        if(val == true)
        {
            timer = new Timer();
            timer.schedule(new Timeout(), timeout_dur);
        }
    }
    
    public class Timeout extends TimerTask 
    {
        public void run()
        {
            
            System.out.println("Timeout!");
            System.out.println("Retransmitting Window: "+windowstart+" to "+(windowstart+allowed));
            
            //RETRANSMISSION WHEN TIMEOUT
            
            try 
            {
				sendwindow();
			} 
            
            catch (IOException e) 
            {
				e.printStackTrace();
			}
        }
    }
    
    
    //Make packet to send
    public byte[] makepacket(int val)
    {
        byte[] buffer = Integer.toString(val).getBytes();
        return buffer;
    }
    
    public void sendwindow() throws IOException
    {
    	System.out.println("HII");
    	DatagramPacket packet;
    	ip = InetAddress.getLocalHost();
    	//ip = InetAddress.getByName("192.168.59.23");
    	for(int i = windowstart; i < windowstart + allowed; i++)
    	{
    		byte[] data = makepacket(i);
    		packet = new DatagramPacket(data,data.length, ip, sending_port);
    		send.send(packet);
    		//System.out.println("Sent Packet "+i);
    	}
    	
    	startTimer(true);
    }


    public static void main(String[] args) throws InterruptedException, IOException
    {
    	UDPReliability_Client obj = new UDPReliability_Client();
    	
    	Scanner sc = new Scanner(System.in);
    	System.out.println("Provide Window Size");
    	obj.windowsize = sc.nextInt();
    	obj.allowed = obj.windowsize;
    	
    	obj.send = new DatagramSocket(8000);	//Create socket for sending
    	obj.recv = new DatagramSocket(obj.receiving_port);	//Create socket for receiving
    	
    	Receiver receiver = new Receiver(obj);
    	Thread receiver_thread = new Thread(receiver);    	

        obj.sendwindow();
        receiver_thread.start();
    	
    	//Create 2 threads
    	//Sender sender = new Sender(obj);
    	//Thread sender_thread = new Thread(sender);
    	//sender_thread.start();
        //sender_thread.join();
        //receiver_thread.join();
    }
}