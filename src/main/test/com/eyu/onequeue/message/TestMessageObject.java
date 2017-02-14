package com.eyu.onequeue.message;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class TestMessageObject {

    private Long start;
    private Map<String, String> value;

    public static TestMessageObject of() {
	TestMessageObject ret = new TestMessageObject();
	ret.start = System.currentTimeMillis();
	ret.value = new HashMap<>();
	return ret;
    }
    
    public static TestMessageObject ofBig(int count) {
	TestMessageObject ret = of();
	for(int i=0;i<count;i++){
	    ret.value.put(""+i, ""+i);
	}
	return ret;
    }

    public Long getStart() {
	return start;
    }

    public Map<String, String> getValue() {
	return value;
    }

}
