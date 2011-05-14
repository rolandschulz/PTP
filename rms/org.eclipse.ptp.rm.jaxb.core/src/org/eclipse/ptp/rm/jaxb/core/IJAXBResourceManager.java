/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * JAXB specific resource manager.
 * 
 * @author arossi
 * 
 */
public interface IJAXBResourceManager extends IResourceManager {

	/**
	 * Exported abstract call.
	 * 
	 * @param jobId
	 *            of job whose state has changed
	 */
	public void fireJobChanged(String jobId);

	/**
	 * @return the control sub-manager.
	 */
	public IJAXBResourceManagerControl getControl();

	/**
	 * @return the associated configuration
	 */
	public IJAXBResourceManagerConfiguration getJAXBConfiguration();
}
