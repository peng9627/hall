package game.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Key;

/**
 * get data from api
 * Created by pengyi on 2015/7/27.
 */
public class HttpUtil {

    private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    public static String urlConnectionByRsa(String url, String pa) {

        try {
            Key publicKey = RSAUtils.getPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDmlCWDcOa9hOWq+ZTmuaKAr7yQqRRGBNb1LtYAlXMtuuXCWMSGdRiIrRrEsTDDBNRcjjm+slFt0BOCZoR4xtcO9d4+SLkg8mIJnDaLPnNsSM1GVuxMGTjdqT9jl/N7LBkHuW3JeIlZ5qk/7iX3JCUzXxGbs6aHnP2KW9RvXrdvPQIDAQAB");
            if (CoreStringUtils.isEmpty(pa)) {
                return new String(RSAUtils.decrypt(publicKey, urlConnection(url, null, "utf-8").getBytes("utf-8")), "utf-8");
            }
            byte[] bytes = RSAUtils.encrypt(publicKey, pa.getBytes("utf-8"));
            return new String(RSAUtils.decrypt(publicKey, urlConnection(url, URLEncoder.encode(new String(bytes, "utf-8"), "utf-8"), "utf-8").getBytes("utf-8")), "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.toString(), e);
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return null;
    }

    public static String urlConnection(String url, String pa) {
        return urlConnection(url, pa, "utf-8");
    }

    public static String urlConnection(String url, String pa, String charset) {

        String response = null;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("POST");

            // Send data
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), charset));
            // pa为请求的参数
            pw.print(pa);
            pw.flush();
            pw.close();

            // Get the response!
            int httpResponseCode = conn.getResponseCode();
            if (httpResponseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP response code: " + httpResponseCode +
                        "\nurl:" + url);
            }

            InputStream inputStream = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset));
            StringBuilder builder = new StringBuilder();
            String readLine;
            while (null != (readLine = br.readLine())) {
                builder.append(readLine);
            }
            inputStream.close();
            response = builder.toString();

            conn.disconnect();
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return response;
    }
}
