package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chao on 2017/12/19.
 */
public class LogTest {
    private final static Logger logger = LoggerFactory.getLogger(LogTest.class);
    public static void main(String[] args) {
        logger.error("error");
        logger.info("info");
        logger.debug("debug");
    }
}
