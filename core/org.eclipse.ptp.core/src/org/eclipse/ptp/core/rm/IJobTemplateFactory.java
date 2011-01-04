/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.core.rm;

import org.eclipse.ptp.core.rm.exceptions.ResourceManagerException;

public interface IJobTemplateFactory {
	/**
	 * Create a new empty job template.
	 * 
	 * @return new empty job template
	 */
	public IJobTemplate createJobTemplate() throws ResourceManagerException;

	/**
	 * Deallocate resources used by a job template.
	 * 
	 * @param jobTemplate
	 */
	public void deleteJobTemplate(IJobTemplate jobTemplate) throws ResourceManagerException;
}
