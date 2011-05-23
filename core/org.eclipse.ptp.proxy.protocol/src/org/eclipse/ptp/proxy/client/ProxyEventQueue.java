/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.proxy.client;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.ptp.proxy.event.IProxyEvent;

/**
 * This class is used to enqueue events derived from messages sent to the PTP
 * client by a proxy prior to their being handled by the event handling methods
 * in the PTP client.
 * 
 * @author David Wootton
 * @since 4.0
 * 
 */
public class ProxyEventQueue {
	private ConcurrentLinkedQueue<IProxyEvent> eventQueue;
	private ConcurrentLinkedQueue<IProxyEvent> priorityEventQueue;

	/**
	 * Create the internal queue objects used to hold events prior to their
	 * being processed
	 */
	public ProxyEventQueue() {
		eventQueue = new ConcurrentLinkedQueue<IProxyEvent>();
		priorityEventQueue = new ConcurrentLinkedQueue<IProxyEvent>();
	}

	/**
	 * Add an event to the high priority event holding queue. \ Notify the
	 * thread listening for enqueued events
	 * 
	 * @param e
	 *            : The event to be enqueued
	 */
	public synchronized void addPriorityProxyEvent(IProxyEvent e) {
		priorityEventQueue.add(e);
		notify();
	}

	/**
	 * Add an event to the normal priority event holding queue. \ Notify the
	 * thread listening for enqueued events
	 * 
	 * @param e
	 *            : The event to be enqueued
	 */
	public synchronized void addProxyEvent(IProxyEvent e) {
		eventQueue.add(e);
		notify();
	}

	/**
	 * Retrieve the next event from the event holding queues. Preference is
	 * given to the priority queue such that all events in the priority queue
	 * will be processed before any normal priority events.
	 * 
	 * @return Next event from the queue
	 */
	public IProxyEvent getProxyEvent() {
		IProxyEvent event;
		synchronized(this) {
			if ((priorityEventQueue.peek() == null) && (eventQueue.peek() == null)) {
				waitForEvent();
			}
		}
		event = priorityEventQueue.poll();
		if (event != null) {
			return event;
		}
		event = eventQueue.poll();
		if (event != null) {
			return event;
		}
		return null;
	}

	/**
	 * Return the total count of enqueued events
	 * 
	 * @return Event count
	 */
	public int size() {
		return priorityEventQueue.size() + eventQueue.size();
	}

	/**
	 * Internal method used to block thread while waiting for next event
	 */
	private void waitForEvent() {
		try {
			wait();
		} catch (InterruptedException e) {
		}
	}
}
