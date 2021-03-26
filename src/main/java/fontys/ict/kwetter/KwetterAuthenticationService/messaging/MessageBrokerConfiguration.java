package fontys.ict.kwetter.KwetterAuthenticationService.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageBrokerConfiguration {

    @Value("${rabbitmq.exchange}")
    private String exchange;

/*
    @Bean("create-account-queue")
    public Queue createAccountQueue() {
        return new Queue("create-account-queue");
    }
*/

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange);
    }

/*    @Bean
    Binding createAccountBinding(@Qualifier("create-account-queue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("create-account");
    }*/

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
