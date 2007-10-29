/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.proxy.runtime.event;

import org.eclipse.ptp.proxy.event.IProxyEventFactory;

public interface IProxyRuntimeEventFactory extends IProxyEventFactory {
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeAttributeDefEvent newProxyRuntimeAttributeDefEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeNewJobEvent newProxyRuntimeNewJobEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeNewMachineEvent newProxyRuntimeNewMachineEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeNewNodeEvent newProxyRuntimeNewNodeEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeNewProcessEvent newProxyRuntimeNewProcessEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeNewQueueEvent newProxyRuntimeNewQueueEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeJobChangeEvent newProxyRuntimeJobChangeEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeMachineChangeEvent newProxyRuntimeMachineChangeEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeNodeChangeEvent newProxyRuntimeNodeChangeEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeProcessChangeEvent newProxyRuntimeProcessChangeEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeRemoveAllEvent newProxyRuntimeRemoveAllEventt(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeRemoveJobEvent newProxyRuntimeRemoveJobEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeRemoveMachineEvent newProxyRuntimeRemoveMachineEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeRemoveNodeEvent newProxyRuntimeRemoveNodeEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeRemoveProcessEvent newProxyRuntimeRemoveProcessEvent(int transID, String[] args);
	
	/**
	 * @param transID
	 * @param args
	 * @return
	 */
	public IProxyRuntimeRemoveQueueEvent newProxyRuntimeRemoveQueueEvent(int transID, String[] args);
}
