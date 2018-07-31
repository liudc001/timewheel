package com.fhr.timewheel.standalone;

/**
 * Created by Huaran Fan on 2018/7/31
 *
 * @description 时间轮任务 相当于job
 */
public interface TimeTask {

	void run(Timeout timeout) throws Exception;
}
