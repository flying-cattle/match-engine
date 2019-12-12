package com.flying.cattle.me.ws.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import com.flying.cattle.me.util.SnowflakeIdWorker;
import com.flying.cattle.me.ws.annotations.WebSocketMapping;
import com.flying.cattle.me.ws.entity.WebSocketSender;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@WebSocketMapping("/echoDept")
public class EchoDepthHandler implements WebSocketHandler {

	@Autowired
	private ConcurrentHashMap<String, WebSocketSender> senderMap;

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		// TODO Auto-generated method stub
		HandshakeInfo handshakeInfo = session.getHandshakeInfo();
		Map<String, String> queryMap = getQueryMap(handshakeInfo.getUri().getQuery());
		String queue = queryMap.getOrDefault("queue", "defaultId");
		String id = SnowflakeIdWorker.generateId().toString();
		
		Mono<Void> input = session.receive().map(WebSocketMessage::getPayloadAsText)
				.doOnNext(msg ->{
					//取消订阅，移除对应的推送
					if(msg.equals("unsub")){
						senderMap.remove(id+queue);
						session.send(Flux.just(session.textMessage("success"))).subscribe();
						session.close().block();
					}
				}).then();

		Mono<Void> output = session.send(Flux.create(sink -> senderMap.put(id+queue, new WebSocketSender(session, sink,queue))));
		/**
		 * Mono.zip() 会将多个 Mono 合并为一个新的 Mono，任何一个 Mono 产生 error 或 complete 都会导致合并后的 Mono
		 * 也随之产生 error 或 complete，此时其它的 Mono 则会被执行取消操作。
		 */
		return Mono.zip(input, output).then();
	}

	//用于获取url参数
	 private Map<String, String> getQueryMap(String queryStr) {
        Map<String, String> queryMap = new HashMap<>();
        if (!StringUtils.isEmpty(queryStr)) {
            String[] queryParam = queryStr.split("&");
            Arrays.stream(queryParam).forEach(s -> {
                String[] kv = s.split("=", 2);
                String value = kv.length == 2 ? kv[1] : "";
                queryMap.put(kv[0], value);
            });
        }
        return queryMap;
    }
}
