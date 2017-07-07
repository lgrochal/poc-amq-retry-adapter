package com.open.camel.amq.retry;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageRetryTagger
{

    private static final Logger logger = LoggerFactory.getLogger(CamelAmqRouteBuilder.class);


    public static final String ATTEMPT_HEADER = "retry.attempts";

    @Handler
    public void handleMsg(@Body BaseMessage msg,
                          @Headers Map<String, Object> headers)
    {
        logger.info("TAGGER Received: "+msg);

        Integer attempts = (Integer)headers.get(ATTEMPT_HEADER);
        if (attempts == null)
            attempts = 1;
        else
            attempts++;

        logger.warn("This is delivery attempt "+attempts);
        headers.put(ATTEMPT_HEADER, attempts);
    }

}
