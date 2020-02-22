package com.fhr.timewheel.standalone.singleround;

import com.fhr.timewheel.standalone.Timeout;

import java.util.Set;

/**
 * Created by Huaran Fan on 2018/8/1
 *
 * @description 一个时间节点（一个桶）
 */
public class HashWheelBucket {
	
	// 双向链表
	private HashWheelTimeout head; // 链表头部
	private HashWheelTimeout tail; // 链表尾部
	
	/**
	 * 链表新增节点
	 * @param timeout
	 */
	public void addTimeout(HashWheelTimeout timeout) {
		// 新加入的节点，桶为null
		assert timeout.bucket == null;
		// 该节点所属桶为当前桶
		timeout.bucket = this;
		// 头部为null,说明是空链表
		if (head == null) {
			// 链表头部,尾部都为当前节点
			head = tail = timeout;
		} 
		else {// 非空链表,将新节点添加到链表的尾部
			// 尾结点的下一个节点为 新节点
			tail.next = timeout;
			// 新节点的上一个节点为 之前的尾结点（双向链表）
			timeout.prev = tail;
			// 尾结点指向新节点
			tail = timeout;
		}
	}

	/**
	 * 处理该桶里的所有任务
	 * @param deadline
	 */
	public void expireTimeouts(long deadline) {
		// 从链表头部开始遍历
		HashWheelTimeout timeout = head;
		// process all timeouts
		while (timeout != null) {
			boolean remove = false;
			// 轮数小于等于0
			if (timeout.remainingRounds <= 0) {
				// 满足最后期限时间
				if (timeout.deadline <= deadline) {
					// 触发到期操作（到期尝试执行任务）
					timeout.expire();
				} 
				else {
					// The timeout was placed into a wrong slot. This should never happen.
					throw new IllegalStateException(String.format(
							"timeout.deadline (%d) > deadline (%d)", timeout.deadline, deadline));
				}
				// 标识该任务可移除
				remove = true;
			} 
			else if (timeout.isCancelled()) { // 任务被撤销了
				// 标记可移除
				remove = true;
			} 
			else {
				// 未到期，空走一轮，轮数减一
				timeout.remainingRounds --;
			}
			
			// store reference to next as we may null out timeout.next in the remove block.
			// 删除之前,暂存下一个节点
			HashWheelTimeout next = timeout.next;
			if (remove) { // 可移除
				// 移除当前节点
				remove(timeout);
			}
			// 赋值下一个节点，循环将继续处理下一个节点
			timeout = next;
		}
	}

	public void remove(HashWheelTimeout timeout) {
		// 获取当前节点的下一个节点
		HashWheelTimeout next = timeout.next;
		// remove timeout that was either processed or cancelled by updating the linked-list
		if (timeout.prev != null) {// 当前节点为非头部节点
			timeout.prev.next = next; // 当前节点的位置，让给当前节点的下一个节点
		}
		if (timeout.next != null) { // 当前节点有后续节点
			timeout.next.prev = timeout.prev; // 后续节点的前节点 为 当前节点的前节点
		}
		
		// 当前节点 为头部节点
		if (timeout == head) {
			// if timeout is also the tail we need to adjust the entry too
			// 且当前节点为尾部节点,则为最后一个节点,链表设置为空链表
			if (timeout == tail) {
				tail = null; 
				head = null;
			} 
			else { // 为头部节点且非最后一个节点
				// 头部往后移动一位
				head = next;
			}
		} 
		else if (timeout == tail) { // 当前节点为尾部节点
			// if the timeout is the tail modify the tail to be the prev node.
			tail = timeout.prev; // 尾部前移一位
		}
		// null out prev, next and bucket to allow for GC.
		// 当前节点清空，待垃圾回收
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
	
	/**
	 * 取出链表头部节点
	 * @return
	 */
	private HashWheelTimeout pollTimeout() {
		HashWheelTimeout head = this.head;
		if (head == null) {// 空链表
			return null;
		}
		HashWheelTimeout next = head.next;
		if (next == null) {// 头节点为最后一个节点
			tail = this.head =  null;
		} 
		else {
			// 头节点后移
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
