
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class PacketCatandFor {
	int value = 0;

	public void packetCatandFor(Socket socket) {
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		System.out.println(devices.length);
		//judge whether the network is connected
		netJudge(socket);
		//major codes
		try {
			OutputStream outputStream = socket.getOutputStream();
			BufferedOutputStream objectOutputStream = new BufferedOutputStream(outputStream);
			byte b[] = cmdSet.password.getBytes();
			objectOutputStream.write(b);
			objectOutputStream.flush();
			// find out the exact networkinterface we wanna check
			for (int i = 0; i < devices.length; i++) {
				for (NetworkInterfaceAddress address : devices[i].addresses) {
					InetAddress iaddr = address.address;
					//used to add "c" to the ip so bytes number can be fitted
					StringBuffer newaddress = new StringBuffer("");
					//to add "c" to the targetd ip to be in line with above one
					StringBuffer newaddress1 = new StringBuffer("");
					//used to collect selected characters of the selected ip
					StringBuffer adrselctool = new StringBuffer("");
					newaddress.append(iaddr.toString());
					newaddress1.append(socket.getLocalAddress().toString());
					//if (iaddr instanceof Inet4Address){
					//iaddr.getHostAddress().equals(ip);
					// }
					if (address.address.toString().length() < 16) {
						for (int n = 0; n <= (16 - address.address.toString().length()); n++) {
							newaddress.append("c");
						}
//            			   System.out.println(newaddress);
					}
					if (socket.getLocalAddress().toString().length() < 16) {
						for (int n = 0; n <= (15 - socket.getLocalAddress().toString().length()); n++) {
							newaddress1.append("c");
						}
//            			   System.out.println(newaddress1);
					}
					for (int j = 0; j < 16; j++) {
//            		      System.out.print(newaddress.charAt(j));
						adrselctool.append(newaddress.charAt(j));
					}
					if (newaddress1.toString().equals(adrselctool.toString())) {
						value = i;
					}
				}
			}

			byte[] mac = devices[value].mac_address;
			StringBuffer sb = new StringBuffer("");
			for (int j = 0; j < mac.length; j++) {
				//add "-"
				if (j != 0) {
					sb.append("-");
				}
				//transformation from hex to integer
				int temp = mac[j] & 0xff;
				String str = Integer.toHexString(temp);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}
			String macStr = sb.toString();
			objectOutputStream.write(macStr.getBytes());
			objectOutputStream.flush();

			StringBuilder sb1 = new StringBuilder();
			for (int i = 0; i < macStr.length(); i++) {
				if (macStr.charAt(i) != '-') {
					sb1.append(macStr.charAt(i));
				} else {
					sb1.append(':');
				}
			}
			macStr=sb1.toString();

			JpcapCaptor captor = JpcapCaptor.openDevice(devices[value], 65535, false, 50);
			String finalMacStr = macStr;
			captor.loopPacket(-1, new PacketReceiver() {
				@Override
				public void receivePacket(Packet packet) {
					try {
//                		System.out.println("value:"+value);

						EthernetPacket ethernetPacket = (EthernetPacket) packet.datalink;
						netJudge(socket);
//                		  System.out.println(ethernetPacket.getSourceAddress().length());
						if( ethernetPacket.getSourceAddress().toString().equals(finalMacStr) || ethernetPacket.getDestinationAddress().toString().equals(finalMacStr)) {
							objectOutputStream.write(depacketize(packet).getBytes());
							objectOutputStream.flush();
							Thread.sleep(1000);
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println(e);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void netJudge(Socket socket) {
		int judge = 0;
		//judge whether the network is connected
		while (judge == 0) {
			try {
				judge = 1;
				socket.sendUrgentData(0xFF);
			} catch (Exception ex) {
				judge = 0;
			}
		}
	}

	public String depacketize(Packet pointer) {
		StringBuilder sb = new StringBuilder();
		if (pointer instanceof ARPPacket) {
			ARPPacket arppacket = (ARPPacket) pointer;
			sb.append("\nARP: \n" + arppacket);
		} else if (pointer instanceof TCPPacket) {
			TCPPacket tcpPacket = (TCPPacket) pointer;
			EthernetPacket ethernetPacket = (EthernetPacket) pointer.datalink;
			sb.append("\nTCP: \nSource IP: " + tcpPacket.src_ip + "|||Destination IP: " + tcpPacket.dst_ip + "|||Source port: " + tcpPacket.src_port + "|||Destination port: " + tcpPacket.dst_port);
			sb.append("|||Source MAC: " + ethernetPacket.getSourceAddress() + "|||Destination MAC: " + ethernetPacket.getDestinationAddress());
			sb.append("|||Protocol: " + tcpPacket.protocol + "\n");
			sb.append("|||Data: \n");
			sb.append(new String(tcpPacket.data));
		} else if (pointer instanceof UDPPacket) {
			UDPPacket udpPacket = (UDPPacket) pointer;
			EthernetPacket ethernetPacket = (EthernetPacket) pointer.datalink;
			sb.append("\nUDP: \nSource IP: " + udpPacket.src_ip + "|||Destination IP: " + udpPacket.dst_ip + "|||Source port: " + udpPacket.src_port + "|||Destination port: " + udpPacket.dst_port);
			sb.append("|||Source MAC: " + ethernetPacket.getSourceAddress() + "|||Destination MAC: " + ethernetPacket.getDestinationAddress());
			sb.append("|||Protocol Type" + udpPacket.protocol);
			sb.append("|||Data: \n");
			sb.append(new String(udpPacket.data));
		} else if (pointer instanceof IPPacket) {
			IPPacket ippacket = (IPPacket) pointer;
			sb.append("\nIP: \nSource IP: " + ippacket.src_ip + "|||Destination IP: " + ippacket.dst_ip);
			sb.append("|||TTL: " + ippacket.hop_limit + "|||Upper Protocol: " + ippacket.protocol);
		} else if (pointer instanceof ICMPPacket) {
			ICMPPacket icmpPacket = (ICMPPacket) pointer;
			sb.append("\nICMP: \n"+icmpPacket.toString());
		} else
			sb.append("This packet are not available");
		return sb.toString();
	}
}
