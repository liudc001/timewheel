package com.fhr.timewheel.standalone.singleround;

import com.fhr.timewheel.standalone.Timeout;

import java.util.Set;

/**
 * Created by Huaran Fan on 2018/8/1
 *
 * @description 一个时间节点（一个桶）
 */
public class HashWheelBucket {
	private HashWheelTimeout head;
	private HashWheelTimeout tail;


	public void addTimeout(HashWheelTimeout timeout) {
		assert timeout.bucket == null;
		timeout.bucket = this;
		if (head == null) {
			head = tail = timeout;
		} else {
			tail.next = timeout;
			timeout.prev = tail;
			tail = timeout;
		}
	}

	public void expireTimeouts(long deadline) {
		HashWheelTimeout timeout = head;

		// process all timeouts
		while (timeout != null) {
			boolean remove = false;
			if (timeout.remainingRounds <= 0) {
				if (timeout.deadline <= deadline) {
					timeout.expire();
				} else {
					// The timeout was placed into a wrong slot. This should never happen.
					throw new IllegalStateException(String.format(
							"timeout.deadline (%d) > deadline (%d)", timeout.deadline, deadline));
				}
				remove = true;
			} else if (timeout.isCancelled()) {
				remove = true;
			} else {
				timeout.remainingRounds --;
			}
			// store reference to next as we may null out timeout.next in the remove block.
			HashWheelTimeout next = timeout.next;
			if (remove) {
				remove(timeout);
			}
			timeout = next;
		}
	}

	public void remove(HashWheelTimeout timeout) {
		HashWheelTimeout next = timeout.next;
		// remove timeout that was either processed or cancelled by updating the linked-list
		if (timeout.prev != null) {
			timeout.prev.next = next;
		}
		if (timeout.next != null) {
			timeout.next.prev = timeout.prev;
		}

		if (timeout == head) {
			// if timeout is also the tail we need to adjust the entry too
			if (timeout == tail) {
				tail = null;
				head = null;
			} else {
				head = next;
			}
		} else if (timeout == tail) {
			// if the timeout is the tail modify the tail to be the prev node.
			tail = timeout.prev;
		}
		// null out prev, next and bucket to allow for GC.
		timeout.prev = null;
		timeout.next = null;
		timeout.bucket = null;
	}

	/**
	 * Clear this bucket and return all not expired / cancelled {@link Timeout}s.
	 */
	public void clearTimeouts(Set<Timeout> set) {
		for (;;) {
			HashWheelTimeout timeout = pollTimeout();
			if (timeout == null) {
				return;
			}
			if (timeout.isExpired() || timeout.isCancelled()) {
				continue;
			}
			set.add(timeout);
		}
	}

	private HashWheelTimeout pollTimeout() {
		HashWheelTimeout head = this.head;
		if (head == null) {
			return null;
		}
		HashWheelTimeout next = head.next;
		if (next == null) {
			tail = this.head =  null;
		} else {
			this.head = next;
			next.prev = null;
		}

		// null out prev and next to allow for GC.
		head.next = null;
		head.prev = null;
		head.bucket = null;
		return head;
	}

}
