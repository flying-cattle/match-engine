/**
 * @filename: PushDetphJob.java 2019-12-13
 * @project power-web  V1.0
 * Copyright(c) 2018 BianPeng Co. Ltd. 
 * All right reserved. 
 */
package com.flying.cattle.me.other.job;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.flying.cattle.me.entity.Depth;
import com.flying.cattle.me.other.ws.entity.SendTata;
import com.flying.cattle.me.other.ws.entity.WebSocketSender;
import com.flying.cattle.me.util.HazelcastUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.IMapJet;
import com.hazelcast.jet.JetInstance;
import lombok.extern.slf4j.Slf4j;

/**   
 * Copyright: Copyright (c) 2019 
 * 
 * <p>说明：深度推送 </P>
 * @version: V1.0
 * @author: BianPeng
 * 
 */
@Component
@EnableScheduling
@Slf4j
public class PushDetphJob {

	@Autowired
	public HazelcastInstance hzInstance;
	
	@Autowired
	private ConcurrentHashMap<String, WebSocketSender> senderMap;
	
	/**
	 * -推送深度
	 */
	@Scheduled(fixedDelay = 1000)
	public void pushDetph() {
		try {
			//XBIT-USDT 买盘 
			IMap<BigDecimal, BigDecimal> buyMap = hzInstance.getMap(HazelcastUtil.getMatchKey("XBIT-USDT", Boolean.TRUE));
			List<Depth> buyList = new ArrayList<Depth>();
			List<Depth> sellList = new ArrayList<Depth>();
			if (buyMap.size()>0) {
				buyList = buyMap.entrySet().stream().sorted(Entry.<BigDecimal, BigDecimal>comparingByKey().reversed())
						.map(obj -> new Depth(obj.getKey().toString(), obj.getValue().toString(), obj.getValue().toString(), 1,"XBIT-USDT", Boolean.TRUE))
						.collect(Collectors.toList());
			}else {
				Depth depth = new Depth("0.00", "0.0000", "0.0000", 1, "XBIT-USDT", Boolean.TRUE);
				buyList.add(depth);
			}
			
			//XBIT-USDT 卖盘 
			IMap<BigDecimal, BigDecimal> sellMap = hzInstance.getMap(HazelcastUtil.getMatchKey("XBIT-USDT", Boolean.FALSE));
			if (sellMap.size()>0) {
				sellList = sellMap.entrySet().stream().sorted(Entry.<BigDecimal, BigDecimal>comparingByKey())
						.map(obj -> new Depth(obj.getKey().toString(), obj.getValue().toString(), obj.getValue().toString(), 1,"XBIT-USDT", Boolean.TRUE))
						.collect(Collectors.toList());
			}else {
				Depth depth = new Depth("0.00", "0.0000", "0.0000", 1, "XBIT-USDT", Boolean.FALSE);
				sellList.add(depth);
			}
			//盘口过大处理
			if (buyList.size() > 100) {
				buyList.subList(0, 100);
			}
			if (sellList.size() > 100) {
				sellList.subList(0, 100);
			}
			//发送数据处理
			Map<String, List<Depth>> map = new HashMap<String, List<Depth>>();
			map.put("buy", buyList);
			map.put("sell", sellList);
			SendTata<Map<String, List<Depth>>> data = new SendTata<Map<String,List<Depth>>>();
			data.setType("depth");
			data.setData(map);
			//发送数据
			senderMap.values().parallelStream().forEach(sender ->{
				sender.sendData(JSON.toJSONString(data), "XBIT-USDT");
			});
		} catch (Exception e) {
			log.error("深度数据处理错误："+e);
		}
	}
}
