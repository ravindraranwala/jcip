package com.concurrency.jcip.ch15;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class LinkedQueueWithAtomicFieldUpdaters<E> {
	private static class Node<E> {
		final E item;
		private volatile LinkedQueueWithAtomicFieldUpdaters.Node<E> next;

		public Node(E item, LinkedQueueWithAtomicFieldUpdaters.Node<E> next) {
			this.item = item;
			this.next = next;
		}
	}

	private final LinkedQueueWithAtomicFieldUpdaters.Node<E> dummy = new LinkedQueueWithAtomicFieldUpdaters.Node<>(null,
			null);
	private final AtomicReference<LinkedQueueWithAtomicFieldUpdaters.Node<E>> head = new AtomicReference<>(dummy);
	private final AtomicReference<LinkedQueueWithAtomicFieldUpdaters.Node<E>> tail = new AtomicReference<>(dummy);

	private static AtomicReferenceFieldUpdater<Node, Node> nextUpdater = AtomicReferenceFieldUpdater
			.newUpdater(Node.class, Node.class, "next");

	public boolean put(E item) {
		LinkedQueueWithAtomicFieldUpdaters.Node<E> newNode = new LinkedQueueWithAtomicFieldUpdaters.Node<E>(item, null);
		while (true) {
			LinkedQueueWithAtomicFieldUpdaters.Node<E> curTail = tail.get();
			LinkedQueueWithAtomicFieldUpdaters.Node<E> tailNext = curTail.next;
			if (curTail == tail.get()) {
				if (tailNext != null) {
					// Queue in intermediate state, advance tail
					tail.compareAndSet(curTail, tailNext);
				} else {
					// In quiescent state, try inserting new node
					if (nextUpdater.compareAndSet(curTail, null, newNode)) {
						// Insertion succeeded, try advancing tail
						tail.compareAndSet(curTail, newNode);
						return true;
					}
				}
			}
		}
	}
}
