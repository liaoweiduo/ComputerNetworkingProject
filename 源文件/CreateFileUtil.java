import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Calendar;

/**
 * Created by liaoweiduo on 21/12/2016.
 */
public class CreateFileUtil {

    public static File createFileByMac(byte[] mac){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        String macStr=sb.toString();
        return createFileByMac(macStr);
    }
    public static File createFileByMac(String macStr){
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);//获取年份
        int month=ca.get(Calendar.MONTH)+1;//获取月份
        int day=ca.get(Calendar.DATE);//获取日
        int hour=ca.get(Calendar.HOUR_OF_DAY);//小时
        String fileName=hour+".txt";
        File file=new File("data"+File.separator+macStr+File.separator+year+File.separator+month+File.separator+day+File.separator+fileName);
        CreateFileUtil.createFile(file);
        return file;
    }

    public static boolean createFile(File file) {
        String destFileName=file.getName();
        if(file.exists()) {
            System.out.println("create single file " + destFileName + " fail, exist");
            return false;
        }
        if (destFileName.endsWith(File.separator)) {
            System.out.println("create single file " + destFileName + " fail, not dir");
            return false;
        }
        //判断目标文件所在的目录是否存在
        if(!file.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            System.out.println("target file "+file.getName()+" no parent dir, create");
            if(!file.getParentFile().mkdirs()) {
                System.out.println("create target file "+file.getName()+" parent dir fail");
                return false;
            }
        }
        //创建目标文件
        try {
            if (file.createNewFile()) {
                System.out.println("create single file " + destFileName + " success");
                return true;
            } else {
                System.out.println("create single file " + destFileName + " fail");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("create single file " + destFileName + " fail, " + e.getMessage());
            return false;
        }
    }


    public static boolean createDir(File dir) {
        String destDirName=dir.getName();
        if (dir.exists()) {
            System.out.println("create dir " + destDirName + " fail, exist");
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            System.out.println("create dir " + destDirName + " success");
            return true;
        } else {
            System.out.println("create dir" + destDirName + " fail");
            return false;
        }
    }


    public static String createTempFile(String prefix, String suffix, String dirName) {
        File tempFile = null;
        if (dirName == null) {
            try{
                //在默认文件夹下创建临时文件
                tempFile = File.createTempFile(prefix, suffix);
                //返回临时文件的路径
                return tempFile.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("创建临时文件失败！" + e.getMessage());
                return null;
            }
        } else {
            File dir = new File(dirName);
            //如果临时文件所在目录不存在，首先创建
            if (!dir.exists()) {
                if (!CreateFileUtil.createDir(new File(dirName))) {
                    System.out.println("创建临时文件失败，不能创建临时文件所在的目录！");
                    return null;
                }
            }
            try {
                //在指定目录下创建临时文件
                tempFile = File.createTempFile(prefix, suffix, dir);
                return tempFile.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("创建临时文件失败！" + e.getMessage());
                return null;
            }
        }
    }



}
