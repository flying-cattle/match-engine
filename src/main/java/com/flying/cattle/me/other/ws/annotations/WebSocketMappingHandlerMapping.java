package com.flying.cattle.me.other.ws.annotations;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
/**
 * @filename: WebSocketMappingHandlerMapping.java 2019年12月4日
 * @project match-engine  V1.0
 * Copyright(c) 2020 FlyCattle Co. Ltd. 
 * All right reserved. 
 */
import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
/**
 * @ClassName: WebSocketMappingHandlerMapping
 * @Description: TODO(把所有加了WebSocketMapping注解的请求加到ws请求中，这个是ws请求配置类)
 * @author flying-cattle
 * @date 2019年12月4日
 */
@Component
public class WebSocketMappingHandlerMapping extends SimpleUrlHandlerMapping{
	
	private Map<String, WebSocketHandler> handlerMap = new LinkedHashMap<>();
	/**
     * Register WebSocket handlers annotated by @WebSocketMapping
     * @throws BeansException
     */
    @Override
    public void initApplicationContext() throws BeansException {
        Map<String, Object> beanMap = obtainApplicationContext().getBeansWithAnnotation(WebSocketMapping.class);
        beanMap.values().forEach(bean -> {
            if (!(bean instanceof WebSocketHandler)) {
                throw new RuntimeException(
                        String.format("Controller [%s] doesn't implement WebSocketHandler interface.",
                                bean.getClass().getName()));
            }
            WebSocketMapping annotation = AnnotationUtils.getAnnotation(bean.getClass(), WebSocketMapping.class);
            //webSocketMapping 映射到管理中
            handlerMap.put(Objects.requireNonNull(annotation).value(),(WebSocketHandler) bean);
        });
        super.setOrder(Ordered.HIGHEST_PRECEDENCE);
        super.setUrlMap(handlerMap);
        super.initApplicationContext();
    }
}
