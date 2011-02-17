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
package org.eclipse.ptp.launch.ui.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IRMLaunchConfigurationDynamicTab {

	/**
	 * The events for this listener should be fired when GUI elements are
	 * modified by the user, e.g. a Text widget should have its ModifyListener's
	 * modifyText method set up to notify all of the contents changed listeners.
	 * 
	 * @param launchContentsChangedListener
	 */
	public abstract void addContentsChangedListener(IRMLaunchConfigurationContentsChangedListener launchContentsChangedListener);

	/**
	 * Returns whether this tab is in a state that allows the launch
	 * configuration whose values this tab is showing to be saved. This differs
	 * from <code>isValid()</code> in that <code>canSave()</code> determines if
	 * this tab prevents the current launch configuration from being saved,
	 * whereas <code>isValid()</code> determines if this tab prevents the
	 * current launch configuration from being launched.
	 * 
	 * <p>
	 * This information is typically used by the launch configuration dialog to
	 * decide when it is okay to save a launch configuration.
	 * </p>
	 * 
	 * @param control
	 * @param rm
	 * @param queue
	 * @return whether this tab is in a state that allows the current launch
	 *         configuration to be saved
	 * @since 5.0
	 */
	public abstract RMLaunchValidation canSave(Control control, IResourceManagerControl rm, IPQueue queue);

	/**
	 * Creates the top level control for the dynamic part of the parallel launch
	 * configuration tab under the given parent composite. This method is called
	 * every time a new resource manager or queue is selected by the user.
	 * <p>
	 * Implementors are responsible for ensuring that the created control can be
	 * accessed via <code>getControl</code>
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite
	 * @param rm
	 * @param queue
	 * @throws CoreException
	 * @since 5.0
	 */
	public abstract void createControl(Composite parent, IResourceManagerControl rm, IPQueue queue) throws CoreException;

	/**
	 * Returns the top level control for the dynamic portion of the parallel
	 * tab.
	 * <p>
	 * May return <code>null</code> if the control has not been created yet.
	 * </p>
	 * 
	 * @return the top level control or <code>null</code>
	 */
	public Control getControl();

	/**
	 * Initializes this dynamic tab's controls with values from the given launch
	 * configuration. This method is called when a configuration is selected to
	 * view or edit, after this tab's control has been created. It is also
	 * called every time the user selects a new resource manager or queue. In
	 * this case the configuration that is passed in has been cached by the
	 * parallel tab.
	 * 
	 * @param queue
	 * @param rm
	 * @param control
	 * @param configuration
	 *            launch configuration
	 * @return
	 * @since 5.0
	 */
	public abstract RMLaunchValidation initializeFrom(Control control, IResourceManagerControl rm, IPQueue queue,
			ILaunchConfiguration configuration);

	/**
	 * Returns whether this tab is in a valid state in the context of the
	 * specified launch configuration.
	 * <p>
	 * This information is typically used by the launch configuration dialog to
	 * decide when it is okay to launch.
	 * </p>
	 * 
	 * 
	 * @param launchConfig
	 *            launch configuration which provides context for validating
	 *            this tab. This value must not be <code>null</code>.
	 * @param rm
	 * @param queue
	 * @return whether this tab is in a valid state
	 * @since 5.0
	 */
	public abstract RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManagerControl rm, IPQueue queue);

	/**
	 * Copies values from this tab into the given launch configuration.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param rm
	 * @param queue
	 * @return
	 * @since 5.0
	 */
	public abstract RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManagerControl rm,
			IPQueue queue);

	/**
	 * @param launchContentsChangedListener
	 */
	public abstract void removeContentsChangedListener(IRMLaunchConfigurationContentsChangedListener launchContentsChangedListener);

	/**
	 * Initializes the given launch configuration with default values for this
	 * tab. This method is called when a new launch configuration is created
	 * such that the configuration can be initialized with meaningful values.
	 * This method may be called before this tab's control is created.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param rm
	 * @param queue
	 * @return
	 * @since 5.0
	 */
	public abstract RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManagerControl rm,
			IPQueue queue);

}