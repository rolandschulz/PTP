/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

/**
 * Must be implemented by extensions to the syncProperties extension point.
 * 
 */
public interface ISynchronizeWizardExtensionDescriptor {

	/**
	 * @return the nature
	 */
	public String getNature();

	/**
	 * @return the page
	 */
	public ISynchronizeWizardExtension getWizardExtension();
}
