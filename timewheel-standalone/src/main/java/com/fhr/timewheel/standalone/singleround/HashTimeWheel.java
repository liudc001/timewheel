package com.fhr.timewheel.standalone.singleround;

import com.fhr.timewheel.standalone.TimeTask;
import com.fhr.timewheel.standalone.TimeWheel;
import com.fhr.timewheel.standalone.Timeout;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by Huaran Fan on 2018/7/31
 *
 * @description 时间轮实现
 */
public class HashTimeWheel implements TimeWheel {
	/**
	 * 指针多久转动一格
	 */
	private final long tickDuration;

	/**
	 * 指针转动一格的单位
	 */
	private final TimeUnit unit;

	/**
	 * 一圈的格子数
	 */
	private final int ticksPerWheel;

	/**
	 * 工作线程数
	 */
	private final int workerThreadNum;

	/**
	 * 工作者线程工厂
	 */
	@SuppressWarnings("unused")
	private final ThreadFactory threadFactory = new ThreadFactory() {
		public Thread newThread(Runnable r) {
			return null;
		}
	};

	/**
	 * 待执行timeout
	 */
	protected final Queue<HashWheelTimeout> timeouts = new LinkedList<>();

	/**
	 * 取消的timeout
	 */
	protected final Queue<HashWheelTimeout> cancelledTimeouts = new LinkedList<>();

	/**
	 * 时间轮开始运行时间
	 */
	protected volatile long startTime;

	public HashTimeWheel(long tickDuration, TimeUnit unit, int ticksPerWheel, int workerThreadNum) {
		this.tickDuration = tickDuration;
		this.unit = unit;
		this.ticksPerWheel = ticksPerWheel;
		this.workerThreadNum = workerThreadNum;
	}

	@Override
	public void start() {
		// TODO 开启时间轮
	}

	@Override
	public Set<Timeout> stop(boolean mayInterrupt) throws InterruptedException {
		// TODO 停止时间轮
		return null;
	}

	@Override
	public Timeout newTimeout(TimeTask timeTask, long delay, TimeUnit unit) {
		// TODO 新建任务
		return null;
	}


	public long getTickDuration() {
		return tickDuration;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	public int getTicksPerWheel() {
		return ticksPerWheel;
	}

	public int getWorkerThreadNum() {
		return workerThreadNum;
	}
}
