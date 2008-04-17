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
import org.eclipse.ptp.internal.rtsystem.events.RuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeConnectedStateEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeDisconnectedEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeErrorStateEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeJobChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeMachineChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeMessageEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewJobEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewMachineEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewNodeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewProcessEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNewQueueEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeNodeChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeProcessChangeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveAllEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveJobEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveMachineEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveNodeEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveProcessEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRemoveQueueEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeRunningStateEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeShutdownStateEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeStartupErrorEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.internal.rtsystem.events.RuntimeTerminateJobErrorEvent;

public class RuntimeEventFactory implements IRuntimeEventFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeAttributeDefinitionEvent(org.eclipse.ptp.core.attributes.IAttributeDefinition)
	 */
	public IRuntimeAttributeDefinitionEvent newRuntimeAttributeDefinitionEvent(IAttributeDefinition<?,?,?>[] defs) {
		return new RuntimeAttributeDefinitionEvent(defs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeConnectedStateEvent()
	 */
	public IRuntimeConnectedStateEvent newRuntimeConnectedStateEvent() {
		return new RuntimeConnectedStateEvent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeDisconnectedEvent(java.lang.String)
	 */
	public IRuntimeDisconnectedEvent newRuntimeDisconnectedEvent(String message) {
		return new RuntimeDisconnectedEvent(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeErrorStateEvent()
	 */
	public IRuntimeErrorStateEvent newRuntimeErrorStateEvent() {
		return new RuntimeErrorStateEvent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeJobChangeEvent(org.eclipse.ptp.core.elements.attributes.ElementAttributeManager)
	 */
	public IRuntimeJobChangeEvent newRuntimeJobChangeEvent(ElementAttributeManager attrs) {
		return new RuntimeJobChangeEvent(attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeMachineChangeEvent(org.eclipse.ptp.core.elements.attributes.ElementAttributeManager)
	 */
	public IRuntimeMachineChangeEvent newRuntimeMachineChangeEvent(ElementAttributeManager attrs) {
		return new RuntimeMachineChangeEvent(attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeMessageEvent(org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public IRuntimeMessageEvent newRuntimeMessageEvent(AttributeManager attrs) {
		return new RuntimeMessageEvent(attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeMessageEvent(org.eclipse.ptp.core.elements.attributes.MessageAttributes.Level, java.lang.String)
	 */
	public IRuntimeMessageEvent newRuntimeMessageEvent(Level level,
			String message) {
		return new RuntimeMessageEvent(level, message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeNewJobEvent(java.lang.String, org.eclipse.ptp.core.elements.attributes.ElementAttributeManager)
	 */
	public IRuntimeNewJobEvent newRuntimeNewJobEvent(String parent, ElementAttributeManager attrs) {
		return new RuntimeNewJobEvent(parent, attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeNewMachineEvent(java.lang.String, org.eclipse.ptp.core.elements.attributes.ElementAttributeManager)
	 */
	public IRuntimeNewMachineEvent newRuntimeNewMachineEvent(String parent, ElementAttributeManager attrs) {
		return new RuntimeNewMachineEvent(parent, attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeNewNodeEvent(java.lang.String, org.eclipse.ptp.core.elements.attributes.ElementAttributeManager)
	 */
	public IRuntimeNewNodeEvent newRuntimeNewNodeEvent(String parent, ElementAttributeManager attrs) {
		return new RuntimeNewNodeEvent(parent, attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeNewProcessEvent(java.lang.String, org.eclipse.ptp.core.elements.attributes.ElementAttributeManager)
	 */
	public IRuntimeNewProcessEvent newRuntimeNewProcessEvent(String parent, ElementAttributeManager attrs) {
		return new RuntimeNewProcessEvent(parent, attrs);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeNewQueueEvent(java.lang.String, org.eclipse.ptp.core.elements.attributes.ElementAttributeManager)
	 */
	public IRuntimeNewQueueEvent newRuntimeNewQueueEvent(String parent, ElementAttributeManager attrs) {
		return new RuntimeNewQueueEvent(parent, attrs);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeNodeChangeEvent(org.eclipse.ptp.core.elements.attributes.ElementAttributeManager)
	 */
	public IRuntimeNodeChangeEvent newRuntimeNodeChangeEvent(ElementAttributeManager attrs) {
		return new RuntimeNodeChangeEvent(attrs);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeProcessChangeEvent(org.eclipse.ptp.core.elements.attributes.ElementAttributeManager)
	 */
	public IRuntimeProcessChangeEvent newRuntimeProcessChangeEvent(ElementAttributeManager attrs) {
		return new RuntimeProcessChangeEvent(attrs);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeRemoveAllEventt()
	 */
	public IRuntimeRemoveAllEvent newRuntimeRemoveAllEventt() {
		return new RuntimeRemoveAllEvent();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeRemoveJobEvent(org.eclipse.ptp.core.util.RangeSet)
	 */
	public IRuntimeRemoveJobEvent newRuntimeRemoveJobEvent(RangeSet ids) {
		return new RuntimeRemoveJobEvent(ids);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeRemoveMachineEvent(org.eclipse.ptp.core.util.RangeSet)
	 */
	public IRuntimeRemoveMachineEvent newRuntimeRemoveMachineEvent(RangeSet ids) {
		return new RuntimeRemoveMachineEvent(ids);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeRemoveNodeEvent(org.eclipse.ptp.core.util.RangeSet)
	 */
	public IRuntimeRemoveNodeEvent newRuntimeRemoveNodeEvent(RangeSet ids) {
		return new RuntimeRemoveNodeEvent(ids);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeRemoveProcessEvent(org.eclipse.ptp.core.util.RangeSet)
	 */
	public IRuntimeRemoveProcessEvent newRuntimeRemoveProcessEvent(RangeSet ids) {
		return new RuntimeRemoveProcessEvent(ids);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeRemoveQueueEvent(org.eclipse.ptp.core.util.RangeSet)
	 */
	public IRuntimeRemoveQueueEvent newRuntimeRemoveQueueEvent(RangeSet ids) {
		return new RuntimeRemoveQueueEvent(ids);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeRunningStateEvent()
	 */
	public IRuntimeRunningStateEvent newRuntimeRunningStateEvent() {
		return new RuntimeRunningStateEvent();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeShutdownStateEvent()
	 */
	public IRuntimeShutdownStateEvent newRuntimeShutdownStateEvent() {
		return new RuntimeShutdownStateEvent();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeStartupErrorEvent(java.lang.String)
	 */
	public IRuntimeStartupErrorEvent newRuntimeStartupErrorEvent(String message) {
		return new RuntimeStartupErrorEvent(message);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeStartupErrorEvent(int, java.lang.String)
	 */
	public IRuntimeStartupErrorEvent newRuntimeStartupErrorEvent(int code, String message) {
		return new RuntimeStartupErrorEvent(code, message);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeSubmitJobErrorEvent(int, java.lang.String, java.lang.String)
	 */
	public IRuntimeSubmitJobErrorEvent newRuntimeSubmitJobErrorEvent(int code, String message, String jobSubID) {
		return new RuntimeSubmitJobErrorEvent(code, message, jobSubID);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory#newRuntimeTerminateJobErrorEvent(int, java.lang.String, java.lang.String)
	 */
	public IRuntimeTerminateJobErrorEvent newRuntimeTerminateJobErrorEvent(int code, String message, String jobID) {
		return new RuntimeTerminateJobErrorEvent(code, message, jobID);
	}
}
