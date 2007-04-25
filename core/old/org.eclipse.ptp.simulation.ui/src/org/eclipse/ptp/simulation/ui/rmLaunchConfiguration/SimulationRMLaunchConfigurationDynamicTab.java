/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.simulation.ui.rmLaunchConfiguration;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class SimulationRMLaunchConfigurationDynamicTab
extends AbstractRMLaunchConfigurationDynamicTab {
	
	private static RMLaunchValidation defaultValidation = 
		new RMLaunchValidation(false, "This is a stub for Simulation");
	private Label label;

	public SimulationRMLaunchConfigurationDynamicTab(IResourceManager rm) {
		// TODO Auto-generated constructor stub
	}

	public RMLaunchValidation canSave(Control control,
			IResourceManager rm, IPQueue queue) {
		// TODO Auto-generated method stub
		return defaultValidation;
	}

	public void createControl(Composite parent,
			IResourceManager rm, IPQueue queue) {
		label = new Label(parent, SWT.NONE);
		label.setText("Simulation Specific info goes here");
	}

	public IAttribute[] getAttributes(IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration) {
		// TODO stub
		return new IAttribute[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getControl()
	 */
	public Control getControl() {
		return label;
	}

	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm,
			IPQueue queue, ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub
		return defaultValidation;
	}

	public RMLaunchValidation isValid(ILaunchConfiguration configuration, IResourceManager rm,
			IPQueue queue) {
		// TODO Auto-generated method stub
		return defaultValidation;
	}

	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		// TODO Auto-generated method stub
		return defaultValidation;
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		// TODO Auto-generated method stub
		return defaultValidation;
	}

}
