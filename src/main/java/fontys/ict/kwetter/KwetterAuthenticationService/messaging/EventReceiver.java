package fontys.ict.kwetter.KwetterAuthenticationService.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class EventReceiver {

    private Logger log = LoggerFactory.getLogger(EventReceiver.class);

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

/*    @RabbitListener(queues = "${rabbitmq.queue}")
    public void receive(Message message) {
        System.out.println("received the event!");
        log.info("Received event in service authentication: {}", new String(message.getBody()));
        // Convert to object.
        // Do with it whatever you please.
    }*/
}
