package dataProject;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

/**
 * Created by liaoweiduo on 2016/11/13.
 */
public class masterTest extends JFrame {
    public static void main(String[] args) {
        try {
			new masterClass();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}

class masterClass extends JFrame {
	private JTextArea text= new JTextArea();;
	private JTextField sendText;
	private JComboBox packageCached =new JComboBox();
	private JButton AnalyzeButton;
	FileDialog filedialog_save,filedialog_load;
	FileInputStream file_reader;
	FileOutputStream file_writer;
    BufferedReader sin=null;
    BufferedOutputStream os =null;
    BufferedInputStream is =null;
    BufferedOutputStream Oos=null;
    ObjectOutputStream Ooos=null;
    BufferedInputStream Ois=null;
    ObjectInputStream Oiis=null;
	static Object[] packageC=null;
    Socket socket=null;
    Socket socket1=null;
    String msg="";
    String msgg="";
    boolean setFlag=true;
	private JScrollBar vsBar;
	MenuBar menubar;
	Menu menu;
	MenuItem itemOpen,itemSave;
	Object pointer;
	public void store(){
		    packageC =new Object[packageCached.getItemCount()];
		for (int i=0;i<packageCached.getItemCount();i++){
			packageC[i]=packageCached.getItemAt(i);
		}
	}
	public masterClass() throws Exception{
        socket = new Socket("120.77.168.152",1434);
        socket1 =new Socket("120.77.168.152",1438);
		  os = new BufferedOutputStream(socket.getOutputStream());
		  Oos = new BufferedOutputStream(socket1.getOutputStream());
          os.write(cmdSet.password.getBytes());
          Oos.write(cmdSet.password.getBytes());
          os.flush();
          Oos.flush();
		setTitle("Master");
		setBounds(100,100,400,300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		text.setEditable(false);
		JScrollPane textPanel=new JScrollPane(text);
		vsBar=textPanel.getVerticalScrollBar();
		add(textPanel,BorderLayout.CENTER);
		JPanel panel=new JPanel();
		BorderLayout panelLayout=new BorderLayout();
		panelLayout.setHgap(5);
		panel.setLayout(panelLayout);
		AnalyzeButton=new JButton("Analyze");
		panel.add(AnalyzeButton,BorderLayout.WEST);
		sendText=new JTextField();
		panel.add(sendText,BorderLayout.NORTH);
		sendText.setText("Press Enter to Send Text");
		panel.add(packageCached,BorderLayout.CENTER);
		packageCached.addItem(packageC);
		packageCached.removeItemAt(0);
		packageCached.setEditable(false);
		add(panel,BorderLayout.SOUTH);
		menubar = new MenuBar();
		menu = new Menu("File");
		itemOpen = new MenuItem("Open file");
		itemSave = new MenuItem("Save file");
		menu.add(itemOpen);
		menu.add(itemSave);
		menubar.add(menu);
		setMenuBar(menubar);
		setVisible(true);
		try {
		 newSocket();
		 newPackageSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		filedialog_save = new FileDialog(this,"Save file dailog",FileDialog.SAVE);
		filedialog_load = new FileDialog(this,"Open file dialog",FileDialog.LOAD);
		filedialog_save.addWindowListener(new WindowAdapter()
        {
        	public void windowClosing(WindowEvent e)
        	{
        		filedialog_save.setVisible(false);
        	}
        });
filedialog_load.addWindowListener(new WindowAdapter()
        {
        	public void windowClosing(WindowEvent e)
        	{
        		filedialog_load.setVisible(false);
        	}
        });
		



		AnalyzeButton.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
					text.setText("");
					String[] a=pointer.toString().split("\\|||");
					for (int i=0;i<a.length;i++){
                    text.append(a[i]);
                    }
					}
					
				}
				);
		itemOpen.addActionListener(
new ActionListener(){
	@Override
	public void actionPerformed(ActionEvent arg0) {
		filedialog_load.setVisible(true);
		if(filedialog_load.getFile()!=null)
		{
			try{
				File file = new File (filedialog_load.getDirectory(),filedialog_load.getFile());
				file_reader = new FileInputStream(file);
				Oiis = new ObjectInputStream(file_reader);
                packageCached.removeAllItems();
                for(int i=0;i<file.length();i++){
                packageCached.addItem(Oiis.readObject());
                }
				file_reader.close();
				Oiis.close();
			}
			catch(Exception e2)
			{
			}
		}			
	}
	
}
				);
		itemSave.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						store();
						filedialog_save.setVisible(true);
						if(filedialog_save.getFile()!=null)
						{
							try{
								File file = new File(filedialog_save.getDirectory(),filedialog_save.getFile());
								file_writer = new FileOutputStream(file);
								Ooos = new ObjectOutputStream(file_writer);
								for(int i=0;i<packageC.length;i++){
			                    Ooos.writeObject(packageC[i]);
			                    }
								file_writer.close();
								Ooos.close();
							}
							catch(IOException e2)
					        {
					        }
						}				
					}				
				}
				);
		
	    sendText.addKeyListener(
	    		new KeyListener(){

					@Override
					public void keyPressed(KeyEvent arg0) {
						try{
							if(arg0.getKeyCode()==KeyEvent.VK_ENTER){
							String str=sendText.getText();
							if(str.equals("Filter TCP")){
								for(int i=0;i<packageCached.getItemCount();i++){
									if(packageCached.getItemAt(i).toString().replaceAll("TCP", "qwertyuiop").length()!=packageCached.getItemAt(i).toString().length())
										packageCached.removeItemAt(i);
								}
								
							}
							if(str.equals("Filter UDP")){
								for(int i=0;i<packageCached.getItemCount();i++){
									if(packageCached.getItemAt(i).toString().replaceAll("UDP", "qwertyuiop").length()==packageCached.getItemAt(i).toString().length())
										packageCached.removeItemAt(i);
								}
								
							}
							if(str.equals("Filter ARP")){
								for(int i=0;i<packageCached.getItemCount();i++){
									if(packageCached.getItemAt(i).toString().replaceAll("ARP", "qwertyuiop").length()==packageCached.getItemAt(i).toString().length())
										packageCached.removeItemAt(i);
								}
								
							}
							if(str.equals("Filter IP")){
								for(int i=0;i<packageCached.getItemCount();i++){
									if(packageCached.getItemAt(i).toString().replaceAll("IP", "qwertyuiop").length()==packageCached.getItemAt(i).toString().length())
										packageCached.removeItemAt(i);
								}
								
							}
							else{
					        os.write(str.getBytes());
					        os.flush();
							}
							sendText.setText(null);
							msg="";
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}						 
					}

					@Override
					public void keyReleased(KeyEvent arg0) {
					}

					@Override
					public void keyTyped(KeyEvent arg0) {
					}
	    			
	    		});
	    packageCached.addItemListener(
	    		new ItemListener(){
					@Override
					public void itemStateChanged(ItemEvent arg0) {
                    pointer=packageCached.getSelectedItem();
					}
	    			
	    		}
	    		);
	    addWindowListener(
	    		new WindowListener(){

					@Override
					public void windowActivated(WindowEvent arg0) {					
					}

					@Override
					public void windowClosed(WindowEvent arg0) {	
					}

					@Override
					public void windowClosing(WindowEvent arg0) {
						try{
						setFlag=false;	
						sin.close();
						os.close();
						Oos.close();
						Ooos.close();
						is.close();
						Ois.close();
						Oiis.close();
						socket.close();
						socket1.close();
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}

					@Override
					public void windowDeactivated(WindowEvent arg0) {					
					}

					@Override
					public void windowDeiconified(WindowEvent arg0) {					
					}

					@Override
					public void windowIconified(WindowEvent arg0) {	
					}

					@Override
					public void windowOpened(WindowEvent arg0) {
					}
	    			
	    		});
	}

    void newSocket() throws IOException {
      Runnable runnable= new Runnable() {
            @Override
            public void run() {
            	while(true){
            		if(setFlag==true){
            		try {
		                sin = new BufferedReader(new InputStreamReader(System.in));
		                os = new BufferedOutputStream(socket.getOutputStream());
		                is = new BufferedInputStream(socket.getInputStream());	
	                    byte[] buffer = new byte[2014];
	                    int len;
							while ((len = is.read(buffer)) != -1) {
								msg=new String(buffer,0,len);
								text.append(msg+"\n"); 
				            } 
							os.flush();
							} catch (Exception e) {
						e.printStackTrace();
					}
            }
            		else
            	    break;
            	}
            	}
        };
    new Thread(runnable).start();
    }
    
    void newPackageSocket() throws IOException{
      Runnable runnable1= new Runnable() {
      	public void run(){
      		while(true){
      			if(setFlag==true){
      		try{
            Ois = new BufferedInputStream(socket1.getInputStream());
            byte[] buffer = new byte[2014];
            int len;
				while ((len = Ois.read(buffer)) != -1) {
					msgg=new String(buffer,0,len);
		            packageCached.addItem(msgg);
	            }
      		}
      		catch (Exception e) {
					e.printStackTrace();
				}
      	}
      			else
      			break;
      		}
      		}
      };
      new Thread(runnable1).start();
    }
}


