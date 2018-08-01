package com.fhr.timewheel.standalone.singleround;

import com.fhr.timewheel.standalone.TimeTask;
import com.fhr.timewheel.standalone.TimeWheel;
import com.fhr.timewheel.standalone.Timeout;
import io.netty.util.internal.StringUtil;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Created by Huaran Fan on 2018/8/1
 *
 * @description
 */
public class HashWheelTimeout implements Timeout {
	private static final int ST_INIT = 0;
	private static final int ST_CANCELLED = 1;
	private static final int ST_EXPIRED = 2;
	private static final AtomicIntegerFieldUpdater<HashWheelTimeout> STATE_UPDATER;

	static {
		STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashWheelTimeout.class, "state");
	}

	protected final HashTimeWheel timer;
	protected final TimeTask task;
	protected final long deadline;

	protected volatile int state = ST_INIT;

	// remainingRounds will be calculated and set by Worker.transferTimeoutsToBuckets() before the
	// HashedWheelTimeout will be added to the correct HashedWheelBucket.
	protected long remainingRounds;

	// This will be used to chain timeouts in HashedWheelTimerBucket via a double-linked-list.
	// As only the workerThread will act on it there is no need for synchronization / volatile.
	protected HashWheelTimeout next;
	protected HashWheelTimeout prev;

	// The bucket to which the timeout was added
	HashWheelBucket bucket;

	protected HashWheelTimeout(HashTimeWheel timer, TimeTask task, long deadline) {
		this.timer = timer;
		this.task = task;
		this.deadline = deadline;
	}

	@Override
	public TimeWheel timer() {
		return timer;
	}

	@Override
	public TimeTask task() {
		return task;
	}

	@Override
	public boolean cancel() {
		// only update the state it will be removed from HashedWheelBucket on next tick.
		if (!compareAndSetState(ST_INIT, ST_CANCELLED)) {
			return false;
		}
		// If a task should be canceled we put this to another queue which will be processed on each tick.
		// So this means that we will have a GC latency of max. 1 tick duration which is good enough. This way
		// we can make again use of our MpscLinkedQueue and so minimize the locking / overhead as much as possible.
		timer.cancelledTimeouts.add(this);
		return true;
	}

	void remove() {
		HashWheelBucket bucket = this.bucket;
		if (bucket != null) {
			bucket.remove(this);
		}
	}

	public boolean compareAndSetState(int expected, int state) {
		return STATE_UPDATER.compareAndSet(this, expected, state);
	}

	public int state() {
		return state;
	}

	@Override
	public boolean isCancelled() {
		return state() == ST_CANCELLED;
	}

	@Override
	public boolean isExpired() {
		return state() == ST_EXPIRED;
	}

	public void expire() {
		if (!compareAndSetState(ST_INIT, ST_EXPIRED)) {
			return;
		}

		try {
			task.run(this);
		} catch (Throwable t) {
//			if (logger.isWarnEnabled()) {
//				logger.warn("An exception was thrown by " + TimerTask.class.getSimpleName() + '.', t);
//			}
		}
	}

	@Override
	public String toString() {
		final long currentTime = System.nanoTime();
		long remaining = deadline - currentTime + timer.startTime;

		StringBuilder buf = new StringBuilder(192)
				.append(StringUtil.simpleClassName(this))
				.append('(')
				.append("deadline: ");
		if (remaining > 0) {
			buf.append(remaining)
					.append(" ns later");
		} else if (remaining < 0) {
			buf.append(-remaining)
					.append(" ns ago");
		} else {
			buf.append("now");
		}

		if (isCancelled()) {
			buf.append(", cancelled");
		}

		return buf.append(", task: ")
				.append(task())
				.append(')')
				.toString();
	}
}
