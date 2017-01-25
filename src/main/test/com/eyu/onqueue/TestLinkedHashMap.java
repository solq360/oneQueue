package com.eyu.onqueue;

import java.util.LinkedHashMap;

import com.eyu.common.utils.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

public class TestLinkedHashMap {

    public static void main(String[] args) {
	LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();

	map.put(1, 1);
	map.put(2, 2);
	map.put(3, 3);
	map.put(11, 3);

	map.put(2, 22);
	map.put(1, 111);
	map.put(11, 4);

	map.put(1, 1);

	for (int k : map.keySet()) {
	    System.out.println(k);
	}

	final String json = JsonUtils.object2String(map);
	System.out.println(json);
	map = JsonUtils.string2GenericObject(json, new TypeReference<LinkedHashMap<Integer, Integer>>() {
	});

	for (int k : map.keySet()) {
	    System.out.println(k);
	}
    }
}
