package com.fhr.timewheel.standalone;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Huaran Fan on 2018/7/31
 *
 * @description 时间轮接口
 */
public interface TimeWheel {
	/**
	 * 开启时间轮
	 */
	void start();

	/**
	 * 关闭时间轮
	 *
	 * @param mayInterrupt
	 * @return
	 * @throws InterruptedException
	 */
	Set<Timeout> stop(boolean mayInterrupt) throws InterruptedException;

	/**
	 * 新建任务
	 *
	 * @param timeTask
	 * @param delay
	 * @param unit
	 * @return
	 */
	Timeout newTimeout(TimeTask timeTask, long delay, TimeUnit unit);
}
