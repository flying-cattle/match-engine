package com.flying.cattle.me.ws.entity;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.FluxSink;

public class WebSocketSender {
	private WebSocketSession session;
    private FluxSink<WebSocketMessage> sink;
    public String queue;
    public WebSocketSender(WebSocketSession session, FluxSink<WebSocketMessage> sink,String queue) {
        this.session = session;
        this.sink = sink;
        this.queue = queue;
    }
    
    public void sendData(String data,String queue) {
    	if (this.queue.equals(queue)) {
    		sink.next(session.textMessage(data));
		}
    }
}
