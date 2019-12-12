package com.flying.cattle.me.match;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import com.flying.cattle.me.entity.LevelMatch;
import com.flying.cattle.me.entity.MatchOrder;
import com.flying.cattle.me.entity.Trade;
import com.flying.cattle.me.enums.DealWay;
import com.flying.cattle.me.enums.OrderState;
import com.flying.cattle.me.util.HazelcastUtil;
import com.hazelcast.jet.IMapJet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

@Component
@EnableAsync
public class MatchDetailHandler {

	@Autowired
	JetInstance jet;

	@Autowired
	ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

	/**
	 * -发送记录，并记录到数据库
	 * 
	 * @param order   交易订单
	 * @param price   成交价格
	 * @param number  成交数量
	 * @param dealWay 交易方式
	 */
	@Async
	public void sendTradeRecord(MatchOrder order, BigDecimal price, BigDecimal number, DealWay dealWay) {
		Trade tarde = new Trade(null, order.getUid(), order.getId(), order.getIsBuy(), number, price,
				number.multiply(price), order.getCoinTeam(), null, null, dealWay.value, null);
		// 把数据传入redis或者mq
//		ReactiveListOperations<String, String> operations = reactiveRedisTemplate.opsForList();
//		operations.leftPush("match_trade_record", tarde.toJsonString());
		if (order.getId()>=10000) {
			System.out.println("~~~交易记录："+System.currentTimeMillis()+"=="+tarde.toJsonString());
		}
		
	}

	/**
	 * -推送订单变化
	 * 
	 * @param order 变化后的订单对象
	 */
	public void sendOrderChange(MatchOrder order) {
		if (order.getId()>=10000) {
			System.err.println("===订单变化："+System.currentTimeMillis()+"=="+order.toJsonString());
		}
		
//		ReactiveListOperations<String, String> operations = reactiveRedisTemplate.opsForList();
//		Order or = new Order();
//		BeanUtils.copyProperties(order, or);
//		operations.leftPush("match_order_change", or.toJsonString());
	}

	/**
	 * @Title: inputMatchDepth
	 * @Description: TODO(买入队列)
	 * @param  input 入单
	 * @return void 返回类型
	 * @throws
	 */
	public void inputMatchDepth(MatchOrder input) {
		IMapJet<BigDecimal, BigDecimal> map = jet.getMap(HazelcastUtil.getMatchKey(input.getCoinTeam(), input.getIsBuy()));
		IMapJet<Long, MatchOrder> order_map = jet.getMap(HazelcastUtil.getOrderBookKey(input.getCoinTeam(), input.getIsBuy()));
		map.compute(input.getPrice(),(k, v) -> HazelcastUtil.numberAdd(k, input.getUnFinishNumber()));
		input.setList(null);
		order_map.put(input.getId(), input);
	}

	/**
	 * @Title: outMatchDepth
	 * @Description: TODO(out订单处理)
	 * @param @param order 入单
	 * @return void 返回类型
	 * @throws
	 */
	@Async
	public void outMatchDepth(MatchOrder order) {
		List<LevelMatch> list = order.getList();
		if (null!=list&&list.size()>0) {
			Iterator<LevelMatch> itr = list.iterator();
			while (itr.hasNext()){
				LevelMatch lm = itr.next();
				itr.remove();
				BigDecimal dealNumber = lm.getNumber();
				while (dealNumber.compareTo(BigDecimal.ZERO)>0) {
					//对手盘
					IMapJet<Long, MatchOrder> order_map = jet.getMap(HazelcastUtil.getOrderBookKey(order.getCoinTeam(), !order.getIsBuy()));
					@SuppressWarnings("rawtypes")
					Predicate pricePredicate = Predicates.equal("price", lm.getPrice());
					Collection<MatchOrder> orders = order_map.values(pricePredicate);
					for (MatchOrder mor : orders) {
						MatchOrder out = order_map.remove(mor.getId());
						if (null!=out) {
							int cpr = dealNumber.compareTo(out.getUnFinishNumber());
							if (cpr>0) {
								dealNumber=dealNumber.subtract(out.getUnFinishNumber());
								updateOutOder(out, OrderState.ALL, out.getUnFinishNumber());
							}else if (cpr==0) {
								updateOutOder(out, OrderState.ALL, dealNumber);
								dealNumber = BigDecimal.ZERO;
								break;
							}else {
								out = updateOutOder(out, OrderState.PART, dealNumber);
								order_map.put(out.getId(), out);
								dealNumber = BigDecimal.ZERO;
								break;
							}
						}
					}
				}
			}
		}
	}
	/**
	 * @Title: updateOutOder
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param  out 出订单
	 * @param  dealNumber 交易数量
	 * @return void 返回类型
	 * @throws
	 */
	private MatchOrder updateOutOder(MatchOrder out,OrderState orderState ,BigDecimal dealNumber) {
		out.setState(orderState.value);
		out.setFinishNumber(out.getFinishNumber().add(dealNumber));
		out.setUnFinishNumber(out.getUnFinishNumber().subtract(dealNumber));
		if (out.getIsBuy()) {
			out.setSurplusFrozen(out.getSurplusFrozen().subtract(dealNumber.multiply(out.getPrice())));
		}else {
			out.setSurplusFrozen(out.getSurplusFrozen().subtract(dealNumber));
		}
		this.sendOrderChange(out);
		this.sendTradeRecord(out, out.getPrice(), dealNumber, DealWay.MAKER);
		return out;
	}
}
