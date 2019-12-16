/**
 * @filename: SendTata.java 2019年12月13日
 * @project match-engine  V1.0
 * Copyright(c) 2020 flying-cattle Co. Ltd. 
 * All right reserved. 
 */
package com.flying.cattle.me.other.ws.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: SendTata
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author flying-cattle
 * @date 2019年12月13日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendTata<T> implements Serializable  {
	
	private static final long serialVersionUID = 1811910601172941791L;
	
	String type;
	T data;
}
