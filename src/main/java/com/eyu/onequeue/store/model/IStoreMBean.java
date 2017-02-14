package com.eyu.onequeue.store.model;

import javax.management.MXBean;
/**
 * @author solq
 **/
@MXBean
public interface IStoreMBean {
    public int getUseStoreCount();
}
