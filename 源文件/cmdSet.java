/**
 * Created by liaoweiduo on 22/12/2016.
 */
public class cmdSet {

    static String password="111111";
    static String passwordError="password error";
    static String registered = "registered";    //another master connect
    static String success="success";

    /**
     * cmd structure
     * cmd      ip/mac      msg
     * get      <ip>        mac
     * send     <ip>        <cmd>
     *
     * cmd  msg
     */

    static String isCmd = "cmdmsg:";
    static String get   = "get";
    static String send  = "send";
    static String mac   = "mac";
    static String clientList = "clientlist";
    static String masterList = "masterlist";
    static String macList = "maclist";
    static String start = "start";
    static String stop = "stop";
    static String clientListenedList = "clientlistenedlist";


}
