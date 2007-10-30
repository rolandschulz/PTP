/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.core;

/**
 * Interface definition for targets' changes listerners.
 * Two kinds of events can occur:
 * <ul>
 * <il> Add: Element was added to the pool of targets
 * <il> Remove: Element was removed from the pool of targets
 * </ul>
 * 
 * @author Ricardo M. Matinata, Richard Maciel
 * @since 1.2
 *
 */
public interface ITargetEnvironmentEventListener {
	
	public static int ADDED = 0;
	
	public static int REMOVED = 1;
	
	/**
	 * Notifies that an element has just been added to the model
	 * 
	 * @param element the added element
	 */
	public void elementAdded(TargetElement element);
	
	/**
	 * Notifies that an element has just been removed from the model
	 * 
	 * @param element the lement being removed
	 */
	public void elementRemoved(ITargetElement element);

}
