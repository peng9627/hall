package game.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pengyi
 * Date : 17-8-31.
 * desc:
 */
public class LoggerUtil {

    private static Logger logger = LoggerFactory.getLogger(LoggerUtil.class);

    public static void logger(String content) {
        logger.info(content);
    }
}
