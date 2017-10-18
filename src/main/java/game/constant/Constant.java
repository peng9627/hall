package game.constant;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by pengyi
 * Date : 17-9-6.
 * desc:
 */
public class Constant {

    public static int appVersion;
    public static String apiUrl;
    public static String userInfoUrl;
    public static String wechatLogin;
    public static String moneyDetailedCreate;
    public static String share;
    public static String gamerecordListUrl;
    public static String gamerecordInfoUrl;
    public static String gameServerIp;

    public static int messageTimeout;


    public static void init() {
        BufferedInputStream in = null;
        try {
            Properties prop = new Properties();
            prop.load(Constant.class.getResourceAsStream("/config.properties"));

            appVersion = Integer.parseInt(prop.getProperty("appVersion"));
            apiUrl = prop.getProperty("apiUrl");
            userInfoUrl = prop.getProperty("userInfoUrl");
            wechatLogin = prop.getProperty("wechatLogin");
            moneyDetailedCreate = prop.getProperty("moneyDetailedCreate");
            share = prop.getProperty("share");
            gamerecordListUrl = prop.getProperty("gamerecordListUrl");
            gamerecordInfoUrl = prop.getProperty("gamerecordInfoUrl");
            gameServerIp = prop.getProperty("gameServerIp");
            messageTimeout = Integer.parseInt(prop.getProperty("messageTimeout"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
