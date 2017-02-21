package com.eyu.onequeue.bytebuffer;

import java.util.ArrayList;
import java.util.List;

public class TestPoolSubpage {
    private static int pageSize = 1024*8;
    private static int maxNumElems = pageSize / 16;
    private static int bitmapLength = maxNumElems >> 6;
    static{
	 if ((maxNumElems & 63) != 0) {
             bitmapLength ++;
         } 
    }
    private static List<Boolean>[] bitmap = new ArrayList[bitmapLength];

    public static void main(String[] args) {
	System.out.println(normalizeCapacity(589));
  	for (int i = 0; i < bitmapLength; i++) {
	    bitmap[i] = new ArrayList<>(64);
	}
//	for (int i = 0; i < 65; i++) {
//	    findNextAvail();
//	}
    }

    static int normalizeCapacity(int reqCapacity) {
	// 大等于512时 双倍增加
	if ((reqCapacity & -512) != 0) {
	    // 这里为什么不用 normalizedCapacity<<1 直接加倍，有可能normalizedCapacity刚好是512倍数

	    int normalizedCapacity = reqCapacity;
	    // 减一是避免双倍自增
	    normalizedCapacity--;
	    // 将低四位的二进制都设置为1，一个int是32位
	    // 注释掉代码是优化过的，逻辑用for简明示例
	    // normalizedCapacity |= normalizedCapacity >>> 1;
	    // normalizedCapacity |= normalizedCapacity >>> 2;
	    // normalizedCapacity |= normalizedCapacity >>> 4;
	    // normalizedCapacity |= normalizedCapacity >>> 8;
	    // normalizedCapacity |= normalizedCapacity >>> 16;
	    for (int i = 1; i <= 16; i++) {
		normalizedCapacity |= reqCapacity >>> i;
	    }
	    // 最后要加回1
	    normalizedCapacity++;

	    // 少于0去掉最高位1即可变正数
	    if (normalizedCapacity < 0) {
		normalizedCapacity >>>= 1;
	    }

	    return normalizedCapacity;
	}
	// 少于512情况
	// 刚好16倍数直接返回
	if ((reqCapacity & 15) == 0) {
	    return reqCapacity;
	}
	// &~15 相当于 &-16
	// 如果少于16结果为0，否则大于16取16的倍数
	return (reqCapacity & ~15) + 16;
    }

    private static void findNextAvail() {

	for (int i = 0; i < bitmapLength; i++) {
	    if (bitmap[i].size() == 64) {
		continue;
	    }
	    for (int j = 0; i < 64; j++) {
		if (bitmap[i].size() <= j || !bitmap[i].get(j)) {
		    if (bitmap[i].size() == j) {
			bitmap[i].add(true);
		    } else {
			bitmap[i].set(i, true);
		    }
		    System.out.println("id :" + (i*64 + j) + " section :" + i);
		    break;
		}
	    }
	    break;
	}
	System.out.println();
    }

}
