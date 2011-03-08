/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ptp.internal.ui.handlers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;

public class JobPropertyTester extends PropertyTester {
	private final static String STARTING = "starting"; //$NON-NLS-1$
	private final static String RUNNING = "running"; //$NON-NLS-1$
	private final static String SUSPENDED = "suspended"; //$NON-NLS-1$
	private final static String COMPLETED = "completed"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object,
	 * java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IPJob)) {
			return false;
		}
		IPJob job = (IPJob) receiver;
		if (property.equals(STARTING) && expectedValue instanceof Boolean) {
			boolean value = ((Boolean) expectedValue).booleanValue();
			return (job.getState() == JobAttributes.State.STARTING) == value;
		}
		if (property.equals(RUNNING) && expectedValue instanceof Boolean) {
			boolean value = ((Boolean) expectedValue).booleanValue();
			return (job.getState() == JobAttributes.State.RUNNING) == value;
		}
		if (property.equals(SUSPENDED) && expectedValue instanceof Boolean) {
			boolean value = ((Boolean) expectedValue).booleanValue();
			return (job.getState() == JobAttributes.State.SUSPENDED) == value;
		}
		if (property.equals(COMPLETED) && expectedValue instanceof Boolean) {
			boolean value = ((Boolean) expectedValue).booleanValue();
			return (job.getState() == JobAttributes.State.COMPLETED) == value;
		}
		return false;
	}
}
