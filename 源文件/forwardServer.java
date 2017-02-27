import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.*;
import jpcap.*;
import jpcap.packet.*;

/**
 * Created by liaoweiduo on 20/12/2016.
 */
public class forwardServer {
    static ArrayList<Thread> clientList=new ArrayList<Thread>();
    static ArrayList<String> macList = new ArrayList<String>();
    static ArrayList<Thread> masterList=new ArrayList<Thread>();
    public static void main(String[] args){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    newServerSocket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            ServerSocket cmdSender =new ServerSocket(1434);
            ServerSocket packetReceiver = new ServerSocket(1438);
            while(true){
                Socket cmdMaster=cmdSender.accept();
                Socket packetMaster=packetReceiver.accept();
                Thread t1=new ServerMasterThread(cmdMaster,packetMaster);
                t1.start();
                System.out.println(Calendar.getInstance().getTime()+" "+"connect success...(master:"+cmdMaster.getInetAddress()+":"+cmdMaster.getPort()+")");
                System.out.println("At:"+t1);
                masterList.add(t1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void newServerSocket() throws IOException{
        ServerSocket cmdReceiver =  new ServerSocket(1432);
        ServerSocket packetSender = new ServerSocket(1436);

        System.out.println(Calendar.getInstance().getTime()+" "+"Server started...(Server:"+cmdReceiver.getLocalSocketAddress()+")");
        while(true) {
            Socket cmdClient = cmdReceiver.accept();
            Socket packetClient = packetSender.accept();
            ServerThread t1=new ServerThread(cmdClient,packetClient);
            t1.start();
            System.out.println(Calendar.getInstance().getTime()+" "+"connect success...(client:"+cmdClient.getInetAddress()+":"+cmdClient.getPort()+")");
            System.out.println("At:"+t1);
            clientList.add(t1);
        }
    }
    static String[] getViaSyntax(String sentense){
        String[] sens =sentense.trim().split(" ");
        String[] temp={"","",""};
        if (sens.length!=3 && sens.length!=2)
            return temp;
        else
            return sens;
    }
    static boolean isMac(String macStr){
        if(macStr.charAt(2)=='-' && macStr.charAt(5)=='-' && macStr.charAt(8)=='-' && macStr.charAt(11)=='-' && macStr.charAt(14)=='-'){
            return true;
        }
        return false;
    }
}


class ServerThread extends Thread implements Runnable {

    private Socket cmdClient;
    private Socket packetClient;
    private ArrayList<String> macList = new ArrayList<String>();
    private boolean flag = true;
    private BufferedInputStream is;
    private BufferedOutputStream os;
    private BufferedInputStream ois;
    private BufferedOutputStream oos;

    ServerThread(Socket cmdClient, Socket packetClient) throws IOException {
        this.cmdClient = cmdClient;
        this.packetClient = packetClient;
    }

    public Socket getCmdClient(){
        return this.cmdClient;
    }
    public Socket getPacketClient(){
        return this.packetClient;
    }

    public void setFlag(boolean flag) {
        System.out.println(Calendar.getInstance().getTime()+" "+"client:"+getCmdClient()+"::set flag to:"+flag);
        this.flag=flag;
    }

    public void sendToMaster(String mac, byte[] buffer, int offset, int len){
        if (forwardServer.masterList.size()==0)
            return;
        for (Thread thread : forwardServer.masterList) {
            try {
                ServerMasterThread master = (ServerMasterThread) thread;
                for (String clientMac :master.getClientListenedList()) {
                    if(mac.equals(clientMac)) {
                        System.out.println(master.getCmdMaster());
                        BufferedOutputStream bs = master.getPacketOutputStream();
                        bs.write(buffer,offset,len);
                        bs.flush();
                    }
                }
            }catch (IOException e){
                System.err.println(Calendar.getInstance().getTime()+" "+"send to master error:"+e.getMessage());
            }
        }
    }

    public ArrayList<String> getMacList(){
        return this.macList;
    }
    public BufferedInputStream getBufferedInputStream(){
        return this.is;
    }
    public BufferedOutputStream getBufferedOutputStream(){
        return this.os;
    }
    public BufferedInputStream getPacketInputStream(){
        return this.ois;
    }
    public BufferedOutputStream getPacketOutputStream(){
        return this.oos;
    }
    @Override
    public void run() {
        try {
            //System.out.println("start run...");
            is = new BufferedInputStream(cmdClient.getInputStream());
            os = new BufferedOutputStream(cmdClient.getOutputStream());
            ois = new BufferedInputStream(packetClient.getInputStream());
            oos = new BufferedOutputStream(packetClient.getOutputStream());
            int len;byte[] b=new byte[cmdSet.password.length()];
            int len2;byte[] b2=new byte[cmdSet.password.length()];
            len = is.read(b);
            len2 = ois.read(b2);
            if (new String(b).equals(cmdSet.password) && new String(b2).equals(cmdSet.password)) {
                System.out.println(Calendar.getInstance().getTime()+" "+"client:"+getCmdClient()+"::password correct.");
            }else{
                System.err.println(Calendar.getInstance().getTime()+" "+"client:"+getCmdClient()+"::password error.");
                is.close();
                os.close();
                ois.close();
                oos.close();
                cmdClient.close();
                packetClient.close();
                throw new Exception();
            }
            new Thread(new Runnable() {     //receive packet
                @Override
                public void run() {
                    try {
                        int len;byte[] buffer = new byte[8192];
                        len = ois.read(buffer);
                        if (len < 17) {
                            System.out.println(Calendar.getInstance().getTime() + " " + "client:" + getCmdClient() + "::receive not a packet msg: " + new String(buffer, 0, len));
                            throw new IOException();
                        }
                        String macStr = new String(buffer, 0, 17);
                        if (!forwardServer.isMac(macStr)) {
                            System.out.println(Calendar.getInstance().getTime() + " " + "client:" + getCmdClient() + "::receive not a packet msg: ");
                            throw new IOException();
                        }
                        boolean flag = false;
                        for (String mac : getMacList()) {
                            if (macStr.equals(mac)) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            getMacList().add(macStr);
                            forwardServer.macList.add(macStr);
                        }

                        while ((len = ois.read(buffer)) != -1) {

                            System.out.println(Calendar.getInstance().getTime() + " " + "client:" + getCmdClient() + "::receive message from: " + macStr);
                            File file = CreateFileUtil.createFileByMac(macStr);
                            System.out.println(Calendar.getInstance().getTime() + " " + "client:" + getCmdClient() + "::from mac: " + macStr + " ,send message to masterThread:");

                            sendToMaster(macStr, buffer, 0, len);
                            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file,true));
                            fos.write(buffer,0,len);
                            fos.flush();
                            fos.close();
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
            byte[] buffer=new byte[1024];
            while ((len=is.read(buffer))!=-1) {
                String isCmd=new String(buffer,0,cmdSet.isCmd.length());
                if(isCmd.equals(cmdSet.isCmd)) {
                    String cmdMsg=new String(buffer,cmdSet.isCmd.length(),len-cmdSet.isCmd.length());

                    String cmdMsgs[]=forwardServer.getViaSyntax(cmdMsg);
                    String cmd=cmdMsgs[0];String ip=cmdMsgs[1];String msg=cmdMsgs[2];
                    if (cmd.equals(cmdSet.send)){
                        for (Thread thread:forwardServer.masterList) {
                            ServerMasterThread masterThread=(ServerMasterThread) thread;
                            if(masterThread.getCmdMaster().getInetAddress().getHostAddress().equals(ip)) {
                                System.out.println(Calendar.getInstance().getTime()+" "+"client:"+getCmdClient()+"::send cmd to "+ip+":"+msg);
                                BufferedOutputStream osTo=masterThread.getBufferedOutputStream();
                                osTo.write(msg.getBytes());
                                osTo.flush();
                                break;
                            }
                        }
                    }

                }
            }
            is.close();
            os.close();
            ois.close();
            oos.close();
            cmdClient.close();
            packetClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        forwardServer.clientList.remove(this);
        for (int i=0;i<getMacList().size();i++){
            String mac=getMacList().get(i);
            forwardServer.macList.remove(mac);
        }
        System.out.println(Calendar.getInstance().getTime()+" "+"client:"+getCmdClient()+"::Thread end..."+this);
    }
}

class ServerMasterThread extends Thread implements Runnable{
    private Socket cmdMaster;
    private Socket packetMaster;
    private ArrayList<String> clientListenedList=new ArrayList<String>();
    private BufferedInputStream is;
    private BufferedOutputStream os;
    private BufferedInputStream ois;
    private BufferedOutputStream oos;

    ServerMasterThread(Socket cmdMaster, Socket packetMaster) throws IOException {
        this.cmdMaster = cmdMaster;
        this.packetMaster = packetMaster;
    }
    public BufferedInputStream getBufferedInputStream(){
        return this.is;
    }
    public BufferedOutputStream getBufferedOutputStream(){
        return this.os;
    }
    public BufferedInputStream getPacketInputSteam(){
        return ois;
    }
    public BufferedOutputStream getPacketOutputStream(){
        return oos;
    }
    public Socket getCmdMaster(){
        return this.cmdMaster;
    }
    public Socket getPacketMaster(){
        return this.packetMaster;
    }
    public ArrayList<String> getClientListenedList(){
        return this.clientListenedList;
    }
    @Override
    public void run() {
        try {
            is = new BufferedInputStream(cmdMaster.getInputStream());
            os = new BufferedOutputStream(cmdMaster.getOutputStream());
            ois = new BufferedInputStream(packetMaster.getInputStream());
            oos = new BufferedOutputStream(packetMaster.getOutputStream());
            String str="";
            int len;byte[] b=new byte[cmdSet.password.length()];
            int len2;byte[] b2=new byte[cmdSet.password.length()];
            len = is.read(b);
            len2 = ois.read(b2);
            if (new String(b).equals(cmdSet.password) && new String(b2).equals(cmdSet.password)) {
                System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::password correct.");
            }else{
                System.out.println(Calendar.getInstance().getTime()+" "+"master:" + getCmdMaster() + "::password error.");
                os.write(cmdSet.passwordError.getBytes());
                os.flush();
                is.close();
                os.close();
                ois.close();
                oos.close();
                cmdMaster.close();
                packetMaster.close();
                throw new Exception();
            }
/*
            for (Thread thread:forwardServer.masterList){
                ServerMasterThread masterThread=(ServerMasterThread)thread;
                if (cmdMaster.getInetAddress().getHostAddress().equals(masterThread.getCmdMaster().getInetAddress().getHostAddress())){
                    BufferedOutputStream bos=masterThread.getBufferedOutputStream();
                    bos.write(cmdSet.registered.getBytes());
                    bos.flush();
                    bos.close();
                    masterThread.getBufferedInputStream().close();
                }
            }
*/
            byte[] buffer=new byte[1024];
            while ((len=is.read(buffer))!=-1) {
                str=new String(buffer,0,len);
                System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::received cmd:"+str);

                String strs[]=forwardServer.getViaSyntax(str);
                String cmd=new String();
                String ip =new String();
                String msg=new String();
                if(strs.length==3) {
                    cmd = strs[0];
                    ip = strs[1];
                    msg = strs[2];
                }else if (strs.length==2) {
                    cmd = strs[0];
                    msg = strs[1];
                }
                if(cmd.equals(cmdSet.get)){
                    if(msg.equals(cmdSet.mac)){
                        for (Thread thread:forwardServer.clientList) {
                            ServerThread clientThread=(ServerThread)thread;
                            if(clientThread.getCmdClient().getInetAddress().getHostAddress().equals(ip)) {
                                ArrayList<String> maclist=clientThread.getMacList();
                                System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::send mac of "+ip+":");
                                for (String mac : maclist) {
                                    System.out.println(mac);
                                    os.write((mac+'\n').getBytes());
                                }
                            }
                        }
                    }else if (msg.equals(cmdSet.clientList)){
                        System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::send client list:");
                        for (Thread thread:forwardServer.clientList){
                            ServerThread clientThread=(ServerThread)thread;
                            System.out.println(clientThread.toString()+clientThread.getCmdClient());
                            os.write((clientThread.getCmdClient().getInetAddress().getHostAddress()+'\n').getBytes());
                        }
                    }else if (msg.equals(cmdSet.macList)){
                        System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::send mac list:");
                        for (String macStr:forwardServer.macList){
                            System.out.println(macStr);
                            os.write((macStr+'\n').getBytes());
                        }
                    }else if (msg.equals(cmdSet.masterList)){
                        System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::send master list:");
                        for (Thread thread:forwardServer.masterList) {
                            ServerMasterThread masterThread=(ServerMasterThread) thread;
                            System.out.println(masterThread.toString()+masterThread.getCmdMaster());
                            os.write((masterThread.getCmdMaster().getInetAddress().getHostAddress()+'\n').getBytes());
                        }
                    }else if (msg.equals(cmdSet.start)){
                        System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::add "+ip+" to client listened list.");
                        clientListenedList.add(ip);
                        os.write(cmdSet.success.getBytes());
                    }else if (msg.equals(cmdSet.stop)){
                        System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::delete "+ip+" out of client listened list.");
                        clientListenedList.remove(ip);
                        os.write(cmdSet.success.getBytes());
                    }else if (msg.equals(cmdSet.clientListenedList)){
                        System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::get client listened list of "+ip+":");
                        for (Thread thread:forwardServer.masterList) {
                            ServerMasterThread masterThread =(ServerMasterThread)thread;
                            if(masterThread.getCmdMaster().getInetAddress().getHostAddress().equals(ip)) {
                                for (String mac : masterThread.getClientListenedList()) {
                                    System.out.println(mac);
                                    os.write((mac+'\n').getBytes());
                                }
                            }
                        }
                    }
                }else if (cmd.equals(cmdSet.send)){
                    for (Thread thread:forwardServer.clientList) {
                        ServerThread clientThread=(ServerThread)thread;
                        if(clientThread.getCmdClient().getInetAddress().getHostAddress().equals(ip)) {
                            System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::send cmd to "+ip+":"+msg);
                            BufferedOutputStream osTo =clientThread.getBufferedOutputStream();
                            osTo.write(msg.getBytes());
                            osTo.flush();
                        }
                    }
                    os.write(cmdSet.success.getBytes());
                }
                os.flush();
            }
            is.close();
            os.close();
            ois.close();
            oos.close();
            cmdMaster.close();
            packetMaster.close();
        }catch (IOException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        forwardServer.masterList.remove(this);
        System.out.println(Calendar.getInstance().getTime()+" "+"master:"+getCmdMaster()+"::Thread end..."+this);
    }
}