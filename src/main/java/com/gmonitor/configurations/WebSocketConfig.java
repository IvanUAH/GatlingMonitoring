package com.gmonitor.configurations;

import com.gmonitor.auth.WebSocketAuthInterceptor;
import com.gmonitor.exception.handler.StompCustomErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.concurrent.Executor;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final Executor executor = threadPoolTaskExecutor();

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .setErrorHandler(new StompCustomErrorHandler())
                .addEndpoint("/ws")
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config
                .setApplicationDestinationPrefixes("/app")
                .enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000}) //send heartbeats from server every 10 secs and client 10 secs
                .setTaskScheduler(taskScheduler());
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor((ThreadPoolTaskExecutor) executor);
        registration.interceptors(webSocketAuthInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor((ThreadPoolTaskExecutor) executor);
    }

    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(getAvailableProcessors());
        taskScheduler.setThreadNamePrefix("subscription--heartbeat-");
        taskScheduler.initialize();
        return taskScheduler;
    }

    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(getAvailableProcessors());
        executor.setThreadNamePrefix("subscription-executor-");
        executor.initialize();
        log.debug("created subscription-executor with number of threads:{}", executor.getCorePoolSize());
        return executor;
    }

    private int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors() * 100;
    }

}
