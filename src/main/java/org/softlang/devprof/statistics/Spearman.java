package org.softlang.devprof.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Spearman {

	private Map<String,Integer> map1;
	private Map<String,Integer> map2;
	
	public Spearman(Map<String,Integer> map1,Map<String,Integer> map2){
		this.map1 = map1;
	}
	
	public double getCorrelationCoefficient(){
		Map<String,Integer> rankedMap1 = getRankedMap(map1);
		Map<String,Integer> rankedMap2 = getRankedMap(map2);
		return 0.0;
	}

	private Map<String, Integer> getRankedMap(Map<String, Integer> map) {
		Map<String, Integer> rankedMap = new HashMap<>();
		Set<String> keys = map.keySet();
		for (String string : keys) {
			
		}
		return rankedMap;
	}
	
}
