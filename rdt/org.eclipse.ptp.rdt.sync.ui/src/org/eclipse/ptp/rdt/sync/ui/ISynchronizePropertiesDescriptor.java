/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

/**
 * Must be implemented by extensions to the syncProperties extension point.
 * 
 */
public interface ISynchronizePropertiesDescriptor {

	/**
	 * @return the nature
	 */
	public String getNature();

	/**
	 * @return
	 */
	public ISynchronizeProperties getProperties();
}
