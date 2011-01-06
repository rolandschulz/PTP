/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.generic.ui.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab;

public class GenericRMLaunchConfigurationDynamicTab extends ExtendableRMLaunchConfigurationDynamicTab {

	public GenericRMLaunchConfigurationDynamicTab(IPResourceManager rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab
	 * #getAttributes(org.eclipse.ptp.core.elements.IPResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	@Override
	public IAttribute<?, ?, ?>[] getAttributes(IPResourceManager rm, IPQueue queue, ILaunchConfiguration configuration, String mode)
			throws CoreException {
		List<IAttribute<?, ?, ?>> attrs = new ArrayList<IAttribute<?, ?, ?>>();

		/*
		 * Always set the number of processes to 1
		 */
		try {
			attrs.add(JobAttributes.getNumberOfProcessesAttributeDefinition().create(Integer.valueOf(1)));
		} catch (IllegalValueException e) {
			// Should never happen
		}

		return attrs.toArray(new IAttribute<?, ?, ?>[attrs.size()]);
	}
}
