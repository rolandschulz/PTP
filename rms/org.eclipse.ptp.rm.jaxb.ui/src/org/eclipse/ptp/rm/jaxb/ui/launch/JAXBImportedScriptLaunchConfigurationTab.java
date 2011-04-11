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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * Specialized Launch Tab for displaying (read only) custom batch scripts. To
 * edit scripts, they should be imported into the workspace. The selection of a
 * pre-existent file sets the SCRIPT_PATH variable in the environment which
 * overrides any SCRIPT content that may have been previously set.
 * 
 * @author arossi
 * 
 */
public class JAXBImportedScriptLaunchConfigurationTab extends AbstractJAXBLaunchConfigurationTab implements SelectionListener {

	private Text choice;
	private Text editor;
	private Button browseWorkspace;
	private Button clear;

	private String selected;
	private final StringBuffer contents;

	/**
	 * @param rm
	 *            the resource manager
	 * @param dialog
	 *            the ancestor main launch dialog
	 * @param title
	 *            to display in the parent TabFolder tab
	 * @param parentTab
	 *            the parent controller tab
	 */
	public JAXBImportedScriptLaunchConfigurationTab(IJAXBResourceManager rm, ILaunchConfigurationDialog dialog, String title,
			JAXBControllerLaunchConfigurationTab parentTab) {
		super(parentTab, dialog);
		if (title != null) {
			this.title = title;
		}
		contents = new StringBuffer();
	}

	/*
	 * Nothing to validate here. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #canSave(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	/*
	 * Fixed construction of read-only text field and browse button for the
	 * selection, a clear button to clear the choice, and a large text area for
	 * displaying the script. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetBuilderUtils.createComposite(parent, 1);
		GridLayout layout = WidgetBuilderUtils.createGridLayout(6, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(6);
		Composite comp = WidgetBuilderUtils.createComposite(control, SWT.NONE, layout, gd);
		WidgetBuilderUtils.createLabel(comp, Messages.BatchScriptPath, SWT.LEFT, 1);
		GridData gdsub = WidgetBuilderUtils.createGridDataFillH(3);
		String s = selected == null ? ZEROSTR : selected.toString();
		choice = WidgetBuilderUtils.createText(comp, SWT.BORDER, gdsub, true, s);
		browseWorkspace = WidgetBuilderUtils.createPushButton(comp, Messages.JAXBRMConfigurationSelectionWizardPage_1, this);
		clear = WidgetBuilderUtils.createPushButton(comp, Messages.ClearScript, this);

		layout = WidgetBuilderUtils.createGridLayout(1, true);
		gd = WidgetBuilderUtils.createGridData(GridData.FILL_BOTH, true, true, 130, 300, 1, DEFAULT);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		int style = SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL;
		gdsub = WidgetBuilderUtils.createGridDataFill(DEFAULT, DEFAULT, 1);
		editor = WidgetBuilderUtils.createText(grp, style, gdsub, true, ZEROSTR, null, null);
		WidgetBuilderUtils.applyMonospace(editor);
		editor.setToolTipText(Messages.ReadOnlyWarning);

		selected = null;
		parentTab.resize(control);
		updateControls();
	}

	/**
	 * The top-level control.
	 */
	public Control getControl() {
		return control;
	}

	@Override
	public Image getImage() {
		return null;
	}

	/**
	 * @return title of tab.
	 */
	@Override
	public String getText() {
		return title;
	}

	/*
	 * If there is a script path in the configuration, display that script.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		try {
			String uriStr = configuration.getAttribute(SCRIPT_PATH, ZEROSTR);
			if (!ZEROSTR.equals(uriStr)) {
				selected = uriStr;
			} else {
				selected = null;
			}
			uploadScript();
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(control.getShell(), t, Messages.ErrorOnLoadFromStore, Messages.ErrorOnLoadTitle, false);
		}
		return new RMLaunchValidation(true, null);
	}

	/*
	 * Nothing to do here. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	/*
	 * Nothing to do here. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	/*
	 * Tab acts as listener for browse and clear buttons (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Tab acts as listener for browse and clear buttons (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		Object source = e.getSource();
		try {
			if (source == browseWorkspace) {
				selected = WidgetActionUtils.browseWorkspace(control.getShell());
				updateContents();
			} else if (source == clear) {
				selected = null;
				updateContents();
			}
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(control.getShell(), t, Messages.WidgetSelectedError, Messages.WidgetSelectedErrorTitle,
					false);
		}
	}

	/*
	 * If there is a path selected, store it in the local map as the
	 * SCRIPT_PATH.(non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.launch.AbstractJAXBLaunchConfigurationTab#
	 * doRefreshLocal()
	 */
	@Override
	protected void doRefreshLocal() {
		if (selected != null) {
			localMap.put(SCRIPT_PATH, selected);
		}
	}

	/*
	 * Reads in the script if the selected path is set, then notifies the
	 * ResourcesTab of the change.
	 */
	private void updateContents() throws Throwable {
		uploadScript();
		fireContentsChanged();
	}

	/*
	 * Display the selected path and the script if they are set; enable the
	 * clear button if script is non-empty.
	 */
	private void updateControls() {
		if (selected != null) {
			choice.setText(selected);
		} else {
			choice.setText(ZEROSTR);
		}
		editor.setText(contents.toString());
		if (ZEROSTR.equals(contents)) {
			clear.setEnabled(false);
		} else {
			clear.setEnabled(true);
		}
	}

	/*
	 * If selected is set, read in the contents of the file with that path.
	 * Calls #updateControls().
	 */
	private void uploadScript() throws Throwable {
		contents.setLength(0);
		if (null != selected) {
			BufferedReader br = new BufferedReader(new FileReader(new File(selected)));
			while (true) {
				try {
					String line = br.readLine();
					if (line == null) {
						break;
					}
					contents.append(line).append(LINE_SEP);
				} catch (EOFException eof) {
					break;
				}
			}
		}
		updateControls();
	}
}
