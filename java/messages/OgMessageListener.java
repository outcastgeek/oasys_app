package messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: outcastgeek
 * Date: 7/24/12
 * Time: 12:07 AM
 * To change this template use File | Settings | File Templates.
 */

public class OgMessageListener {

    private Logger logger = LoggerFactory.getLogger(OgMessageListener.class);

    public void onMessage(String message) {
        //To change body of implemented methods use File | Settings | File Templates.
        logger.debug("RECEIVED MESSAGE: " + message);
    }
}
