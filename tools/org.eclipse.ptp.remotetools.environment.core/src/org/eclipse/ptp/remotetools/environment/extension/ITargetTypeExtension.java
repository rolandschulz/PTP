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
package org.eclipse.ptp.remotetools.environment.extension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remotetools.environment.control.ITargetConfig;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.wizard.AbstractEnvironmentDialogPage;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;

/**
 * Provides an interface that is an extension of a target type. This extension
 * must be implemented by each plugin that extends the Environment Control
 * Delegate extension point, in order to define a new target type.
 * 
 * @author Ricardo M. Matinata, Richard Maciel
 * @since 1.1
 */
public interface ITargetTypeExtension {

	/**
	 * Given the target configuration, produces a fresh new control instance.
	 * 
	 * @param element
	 *            TODO
	 * 
	 * @return the control instance
	 * @since 2.0
	 */
	ITargetControl createControl(ITargetConfig config) throws CoreException;

	/**
	 * @return
	 * @since 2.0
	 */
	ITargetConfig createConfig(ControlAttributes attrs) throws CoreException;

	/**
	 * Provides an array with all the attributes' name needed by the associated
	 * controlFactory. Only non-ciphered attributes are listed here.
	 * 
	 * @return an array of attributes' name
	 */
	String[] getControlAttributeNames();

	/**
	 * Provides a Dialog Page to edit the associated control's attributes This
	 * should be used to edit an existent target environment
	 * 
	 * @param attributes
	 *            the attributes' map. May be null in the case of a new
	 *            configuration.
	 * @param name
	 *            the configuration's key name. May be null in the case of a new
	 *            configuration.
	 * @return an wizard page
	 */
	AbstractEnvironmentDialogPage dialogPageFactory(ITargetElement targetElement);

	/**
	 * Provides a Dialog Page to edit the associated control's attributes This
	 * should be used when a new Target Environment is created
	 * 
	 * @return an wizard page
	 */
	AbstractEnvironmentDialogPage dialogPageFactory();

	/**
	 * Provides an array with all the attributes' name needed by the associated
	 * controlFactory. Only ciphered attributes are listed here.
	 * 
	 * @return an array of attributes' name
	 */
	String[] getControlAttributeNamesForCipheredKeys();
}
