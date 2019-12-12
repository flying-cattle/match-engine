package com.flying.cattle.matchengine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.flying.cattle.me.entity.LevelMatch;

public class TestList {
	public static void main(String[] args) {
		List<LevelMatch> list = new ArrayList<LevelMatch>();
		for (int i = 1; i < 4; i++) {
			LevelMatch lm = new LevelMatch();
			lm.setNumber(new BigDecimal(i));
			lm.setPrice(new BigDecimal(i));
			list.add(lm);
		}

		Iterator<LevelMatch> iterator = list.iterator();
		while (iterator.hasNext()) {
			LevelMatch out = iterator.next();
			if (out.getPrice().compareTo(BigDecimal.ONE) == 0) {
				iterator.remove();
			}else {
				out.setNumber(out.getNumber().add(BigDecimal.TEN));
			}
		}
		list.forEach(a -> System.out.println(a.toJsonString()));
	}
}
