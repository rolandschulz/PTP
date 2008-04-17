/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rtsystem.events;

import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.MessageAttributes.Level;
import org.eclipse.ptp.core.util.RangeSet;

public interface IRuntimeEventFactory {

	/**
	 * @param defs
	 * @return
	 */
	public IRuntimeAttributeDefinitionEvent newRuntimeAttributeDefinitionEvent(IAttributeDefinition<?, ?, ?>[] defs);

	/**
	 * @return
	 */
	public IRuntimeConnectedStateEvent newRuntimeConnectedStateEvent();

	/**
	 * @param message
	 * @return
	 */
	public IRuntimeDisconnectedEvent newRuntimeDisconnectedEvent(String message);

	/**
	 * @return
	 */
	public IRuntimeErrorStateEvent newRuntimeErrorStateEvent();

	/**
	 * @param attrs
	 * @return
	 */
	public IRuntimeJobChangeEvent newRuntimeJobChangeEvent(ElementAttributeManager attrs);

	/**
	 * @param attrs
	 * @return
	 */
	public IRuntimeMachineChangeEvent newRuntimeMachineChangeEvent(ElementAttributeManager attrs);

	/**
	 * @param attrs
	 * @return
	 */
	public IRuntimeMessageEvent newRuntimeMessageEvent(AttributeManager attrs);

	/**
	 * @param level
	 * @param message
	 * @return
	 */
	public IRuntimeMessageEvent newRuntimeMessageEvent(Level level, String message);

	/**
	 * @param parent
	 * @param attrs
	 * @return
	 */
	public IRuntimeNewJobEvent newRuntimeNewJobEvent(String parent, ElementAttributeManager attrs);

	/**
	 * @param parent
	 * @param attrs
	 * @return
	 */
	public IRuntimeNewMachineEvent newRuntimeNewMachineEvent(String parent, ElementAttributeManager attrs);

	/**
	 * @param parent
	 * @param attrs
	 * @return
	 */
	public IRuntimeNewNodeEvent newRuntimeNewNodeEvent(String parent, ElementAttributeManager attrs);

	/**
	 * @param parent
	 * @param attrs
	 * @return
	 */
	public IRuntimeNewProcessEvent newRuntimeNewProcessEvent(String parent, ElementAttributeManager attrs);

	/**
	 * @param parent
	 * @param attrs
	 * @return
	 */
	public IRuntimeNewQueueEvent newRuntimeNewQueueEvent(String parent, ElementAttributeManager attrs);

	/**
	 * @param attrs
	 * @return
	 */
	public IRuntimeNodeChangeEvent newRuntimeNodeChangeEvent(ElementAttributeManager attrs);

	/**
	 * @param attrs
	 * @return
	 */
	public IRuntimeProcessChangeEvent newRuntimeProcessChangeEvent(ElementAttributeManager attrs);

	/**
	 * @return
	 */
	public IRuntimeRemoveAllEvent newRuntimeRemoveAllEventt();

	/**
	 * @param ids
	 * @return
	 */
	public IRuntimeRemoveJobEvent newRuntimeRemoveJobEvent(RangeSet ids);

	/**
	 * @param ids
	 * @return
	 */
	public IRuntimeRemoveMachineEvent newRuntimeRemoveMachineEvent(RangeSet ids);

	/**
	 * @param ids
	 * @return
	 */
	public IRuntimeRemoveNodeEvent newRuntimeRemoveNodeEvent(RangeSet ids);

	/**
	 * @param ids
	 * @return
	 */
	public IRuntimeRemoveProcessEvent newRuntimeRemoveProcessEvent(RangeSet ids);

	/**
	 * @param ids
	 * @return
	 */
	public IRuntimeRemoveQueueEvent newRuntimeRemoveQueueEvent(RangeSet ids);

	/**
	 * @return
	 */
	public IRuntimeRunningStateEvent newRuntimeRunningStateEvent();

	/**
	 * @return
	 */
	public IRuntimeShutdownStateEvent newRuntimeShutdownStateEvent();

	/**
	 * @param message
	 * @return
	 */
	public IRuntimeStartupErrorEvent newRuntimeStartupErrorEvent(String message);

	/**
	 * @param code
	 * @param message
	 * @return
	 */
	public IRuntimeStartupErrorEvent newRuntimeStartupErrorEvent(int code, String message);

	/**
	 * @param code
	 * @param message
	 * @param jobSubID
	 * @return
	 */
	public IRuntimeSubmitJobErrorEvent newRuntimeSubmitJobErrorEvent(int code, String message, String jobSubID);

	/**
	 * @param code
	 * @param message
	 * @param jobID
	 * @return
	 */
	public IRuntimeTerminateJobErrorEvent newRuntimeTerminateJobErrorEvent(int code, String message, String jobID);

}