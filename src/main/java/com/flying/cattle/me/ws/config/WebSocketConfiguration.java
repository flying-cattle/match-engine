/**
 * @filename: WebSocketConfiguration.java 2019年12月4日
 * @project match-engine  V1.0
 * Copyright(c) 2020 FlyCattle Co. Ltd. 
 * All right reserved. 
 */
package com.flying.cattle.me.ws.config;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import com.flying.cattle.me.ws.annotations.WebSocketMappingHandlerMapping;
import com.flying.cattle.me.ws.entity.WebSocketSender;

/**
 * @ClassName: WebSocketConfiguration
 * @Description: TODO(ws配置类)
 * @author flying-cattle
 * @date 2019年12月12日
 */
@Configuration
public class WebSocketConfiguration {

	@Bean
	public HandlerMapping webSocketMapping() {
		return new WebSocketMappingHandlerMapping();
	}

	@Bean
	public WebSocketHandlerAdapter handlerAdapter() {
		return new WebSocketHandlerAdapter();
	}
	
	//记录所有接受者
	@Bean
	public ConcurrentHashMap<String, WebSocketSender> senderMap() {
		return new ConcurrentHashMap<String, WebSocketSender>();
	}
}
