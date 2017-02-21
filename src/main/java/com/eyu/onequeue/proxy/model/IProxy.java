package com.eyu.onequeue.proxy.model;

/**
 * 代理对象接口
 * @author solq
 * @version 2014-3-6 上午11:20:38
 */
public interface IProxy {
	/**
	 * 转换增强对象
	 * @param target 代理目标
	 */
	public <T> T transform(T target);

	/**
	 * 转换增强对象
	 * @param target 代理目标
	 */
	public <T> T transform(Class<T> target);

	/**
	 * 注册增强服务
	 */
	public void register(Class<?> clz, IEnhanceService  service);

}
