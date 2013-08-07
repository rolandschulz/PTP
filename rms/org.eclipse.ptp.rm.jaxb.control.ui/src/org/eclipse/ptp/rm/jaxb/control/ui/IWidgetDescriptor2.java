/**********************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rm.jaxb.control.ui;

import org.eclipse.debug.ui.ILaunchConfigurationDialog;

/**
 * Extended API for widget objects associated with SWT controls.
 * 
 * @author gwatson
 * @since 1.2
 * 
 */
public interface IWidgetDescriptor2 extends IWidgetDescriptor {
	/**
	 * Get the launch configuration dialog
	 * 
	 * @return launch configuration dialog
	 */
	public ILaunchConfigurationDialog getLaunchConfigurationDialog();
}
