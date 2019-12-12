/**
 * @filename: WebSocketMapping.java 2019年12月4日
 * @project match-engine  V1.0
 * Copyright(c) 2020 FlyCattle Co. Ltd. 
 * All right reserved. 
 */
package com.flying.cattle.me.ws.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * @ClassName: MatchStrategyFactory
 * @Description: TODO(ws注解，加了这个注解的都走ws请求)
 * @author flying-cattle
 * @date 2019年12月4日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebSocketMapping {
    String value() default "";
}
