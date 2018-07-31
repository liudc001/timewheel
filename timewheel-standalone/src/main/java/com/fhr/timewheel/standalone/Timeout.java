package com.fhr.timewheel.standalone;

/**
 * Created by Huaran Fan on 2018/7/31
 *
 * @description 具体的时间轮任务，相当于job+cron
 */
public interface Timeout {
	TimeWheel timer();

	TimeTask task();

	boolean isExpired();

	boolean isCancelled();

	boolean cancel();
}
