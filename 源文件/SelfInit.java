package dataProject;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

public class SelfInit extends JDialog
{
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextPane textPane;
  private JTextPane editorPane;

  public SelfInit()
  {
	  setContentPane(contentPane);
//	  setContentPane(this.contentPane);
    setModal(true);
    getRootPane().setDefaultButton(this.buttonOK);
    this.buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SelfInit.this.onOK();
      }
    });
    this.buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SelfInit.this.onCancel();
      }
    });
    setDefaultCloseOperation(2);
    setSize(200, 400);

    setDefaultCloseOperation(0);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        SelfInit.this.onCancel();
      }
    });
    this.contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SelfInit.this.onCancel();
      }
    }
    , KeyStroke.getKeyStroke(27, 0), 
      1);
  }

  private void onOK() {
    addToSelfStart();
  }

  private void onCancel() {
    dispose();
  }

  private void removeSelfStart() {
    File startUp = new File(this.editorPane.getText() + "\\startup.vbs");
    startUp.deleteOnExit();
  }

  private void addToSelfStart()
  {
    String selfPath = this.editorPane.getText();
    File dir = new File(selfPath);
    if (dir.exists()) {
      File bat = new File("startup.bat");
      try
      {
        if (!bat.exists()) {
          bat.createNewFile();
        }
        FileWriter writer = new FileWriter(bat);
        writer.write("java -jar SelfStartUp.jar");
        writer.flush();
        writer.close();
        writer = new FileWriter(selfPath + "\\startup.vbs");
        writer.write("createobject(\"wscript.shell\").run \"" + bat.getAbsolutePath() + "\",0 ");
        writer.flush();
        writer.close();
        this.textPane.setText("自启动程序已经装上");
        return;
      } catch (IOException e) {
        e.printStackTrace();

        this.textPane.setText("注册失败！请使用管理员模式在此运行程序！");
      }
    } else { this.textPane.setText("注册失败！请修改start的文件夹目录后重试！"); }
  }

  public static void main(String[] args)
  {
    SelfInit dialog = new SelfInit();
    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }

  private void createUIComponents()
  {
  }
}