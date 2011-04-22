/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.launch;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Base class for the JAXB LaunchConfiguration tabs which provide views of
 * editable widgets. Up to three such tabs can be configured as children of the
 * controller tab.<br>
 * <br>
 * 
 * Each tab maintains its own local map of values which are set from its
 * widgets, and this swapped into the environment active map when configuration
 * changes are needed.
 * 
 * @see org.eclipse.ptp.rm.jaxb.ui.launch.JAXBDynamicLaunchConfigurationTab
 * @see org.eclipse.ptp.rm.jaxb.ui.launch.JAXBImportedScriptLaunchConfigurationTab
 * 
 * @author arossi
 * 
 */
public abstract class AbstractJAXBLaunchConfigurationTab extends AbstractRMLaunchConfigurationDynamicTab implements
		IJAXBUINonNLSConstants {

	protected final JAXBControllerLaunchConfigurationTab parentTab;
	protected final Map<String, Object> localMap;
	protected String title;
	protected Composite control;
	protected Button captureJobOut;

	/**
	 * @param parentTab
	 *            the controller
	 * @param dialog
	 *            the dialog to which this tab belongs
	 * @param tabIndex
	 *            child index for the parent
	 */
	protected AbstractJAXBLaunchConfigurationTab(JAXBControllerLaunchConfigurationTab parentTab, ILaunchConfigurationDialog dialog) {
		super(dialog);
		this.parentTab = parentTab;
		this.title = Messages.DefaultDynamicTab_title;
		localMap = new TreeMap<String, Object>();
	}

	/**
	 * @return image to display in the folder tab for this LaunchTab
	 */
	public abstract Image getImage();

	/**
	 * @return text to display in the folder tab for this LaunchTab
	 */
	public abstract String getText();

	/**
	 * This performApply is triggered whenever there is an update on the
	 * controller. We do not want the values of the tab to be flushed to the
	 * configuration unless this tab is the origin of the change; hence we check
	 * to see if the tab is visible.<br>
	 * <br>
	 * If write to configuration is indicated, the the local map is refreshed,
	 * swapped in to the active map, and then flushed to the configuration.
	 * 
	 * @param configuration
	 *            working copy of current launch configuration
	 * @param current
	 *            resource manager
	 * @param queue
	 *            (unused)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		if (control.isVisible()) {
			Map<String, Object> current = null;
			LCVariableMap lcMap = parentTab.getLCMap();
			try {
				refreshLocal(configuration);
				current = lcMap.swapVariables(localMap);
				lcMap.writeToConfiguration(configuration);
			} catch (CoreException t) {
				JAXBUIPlugin.log(t);
				return new RMLaunchValidation(false, t.getMessage());
			} finally {
				try {
					lcMap.swapVariables(current);
				} catch (CoreException t) {
					JAXBUIPlugin.log(t);
					return new RMLaunchValidation(false, t.getMessage());
				}
			}
		}
		return new RMLaunchValidation(true, null);
	}

	/**
	 * Add the capture option checkbox. The listener sets the variables
	 * controlling whether stdout and stderr files for the job should be
	 * captured via tail -F and displayed in the console.<br>
	 * <br>
	 * Checks to see if this option makes sense by seeing if the configuration
	 * supports batch submissions.
	 * 
	 * @param parent
	 *            composite to add to
	 * @param tooltip
	 *            add warning about using this option with custom scripts
	 */
	protected void createOutputCaptureOption(final Composite parent, boolean tooltip) {
		try {
			if (parentTab.getRmConfig().getResourceManagerData().getControlData().getSubmitBatch() == null) {
				return;
			}
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
		}

		GridLayout layout = WidgetBuilderUtils.createGridLayout(2, false, 5, 5, 2, 2);
		GridData gd = WidgetBuilderUtils.createGridData(SWT.NONE, 2);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		gd = WidgetBuilderUtils.createGridData(SWT.NONE, 2);
		captureJobOut = WidgetBuilderUtils.createButton(grp, gd, Messages.CaptureJobOutput, SWT.CHECK | SWT.LEFT,
				new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}

					public void widgetSelected(SelectionEvent e) {
						Button b = (Button) e.getSource();
						String defaultValueOut = null;
						String defaultValueErr = null;
						RMVariableMap map = null;

						try {
							map = parentTab.getRmConfig().getRMVariableMap();
							Object o = map.get(STDOUT_TAIL_F);
							if (o instanceof PropertyType) {
								PropertyType pout = (PropertyType) o;
								defaultValueOut = pout.getDefault();
							} else if (o instanceof AttributeType) {
								AttributeType aout = (AttributeType) o;
								defaultValueOut = aout.getDefault();
							}
							o = map.get(STDERR_TAIL_F);
							if (o instanceof PropertyType) {
								PropertyType perr = (PropertyType) o;
								defaultValueErr = perr.getDefault();
							} else if (o instanceof AttributeType) {
								AttributeType aerr = (AttributeType) o;
								defaultValueErr = aerr.getDefault();
							}
						} catch (Throwable t) {
							WidgetActionUtils.errorMessage(parent.getShell(), t, Messages.CaptureJobOutputError,
									Messages.CaptureJobOutputError_title, false);
						}
						if (map != null) {
							if (b.getSelection()) {
								if (defaultValueOut != null) {
									localMap.put(STDOUT_TAIL_F, defaultValueOut);
								}
								if (defaultValueErr != null) {
									localMap.put(STDERR_TAIL_F, defaultValueOut);
								}
							} else {
								localMap.put(STDOUT_TAIL_F, ZEROSTR);
								localMap.put(STDERR_TAIL_F, ZEROSTR);
							}
						}
						fireContentsChanged();
					}
				});
		if (tooltip) {
			captureJobOut.setToolTipText(Messages.CaptureJobOutputTooltip);
		}
	}

	/**
	 * Tab-specific handling of local variable map.
	 */
	protected abstract void doRefreshLocal();

	/**
	 * Subclasses should call this method, but implement doRefreshLocal().
	 * 
	 * @param current
	 *            configuration
	 */
	protected void refreshLocal(ILaunchConfiguration config) throws CoreException {
		boolean b = captureJobOut.getSelection();
		Object tailFout = localMap.get(STDOUT_TAIL_F);
		Object tailFerr = localMap.get(STDERR_TAIL_F);
		localMap.clear();
		localMap.putAll(LCVariableMap.getStandardConfigurationProperties(config));
		doRefreshLocal();
		localMap.put(STDOUT_TAIL_F, tailFout);
		localMap.put(STDERR_TAIL_F, tailFerr);
		captureJobOut.setSelection(b);
	}
}
