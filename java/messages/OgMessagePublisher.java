package messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: outcastgeek
 * Date: 7/24/12
 * Time: 1:00 AM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class OgMessagePublisher {

    private Logger logger = LoggerFactory.getLogger(OgMessagePublisher.class);

    @Autowired
    @Qualifier("jRedisTemplate")
    private RedisTemplate redisTemplate;

    public void dispatchMessage(String channel, String message) {

        logger.debug("CHANNEL: " + channel);
        logger.debug("MESSAGE: " + message);

        redisTemplate.convertAndSend(channel, message);
    }
}
