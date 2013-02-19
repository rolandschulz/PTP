/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.launch.ui.extensions;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.utils.LaunchTabBuilder;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.utils.WidgetActionUtils;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewerType;
import org.eclipse.ptp.rm.jaxb.core.data.LaunchTabType;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * Specialized Launch Tab for displaying (read only) custom batch scripts. To edit scripts, they should be imported into the
 * workspace. The selection of a pre-existent file sets the SCRIPT_PATH variable in the environment which overrides any SCRIPT
 * content that may have been previously set.<br>
 * <br>
 * The configuration provides for the exporting of the environment for the purpose of overriding settings.
 * 
 * @author arossi
 * @since 7.0
 * 
 */
public class JAXBImportedScriptLaunchConfigurationTab extends JAXBDynamicLaunchConfigurationTab {

	private final String rmPrefix;
	private final StringBuffer contents;
	private final AttributeViewerType viewerType;

	private Text choice;
	private Text editor;
	private Button browseWorkspace;
	private Button clear;
	private String selected;

	/**
	 * @param control
	 *            the job controller
	 * @param dialog
	 *            the ancestor main launch dialog
	 * @param importTab
	 *            describing configurable parts
	 * @param parentTab
	 *            the parent controller tab
	 */
	public JAXBImportedScriptLaunchConfigurationTab(ILaunchController control, LaunchTabType.Import importTab,
			JAXBControllerLaunchConfigurationTab parentTab, IProgressMonitor monitor) {
		super(control, parentTab);
		setProgressMonitor(monitor);
		this.title = importTab.getTitle();
		this.viewerType = importTab.getExportForOverride();
		shared = new String[0];
		rmPrefix = control.getControlId() + JAXBUIConstants.DOT;
		contents = new StringBuffer();
	}

	/*
	 * Nothing to validate here. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #canSave(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public RMLaunchValidation canSave(Control control) {
		return super.canSave(control);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBDynamicLaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite
	 * ,
	 * java.lang.String)
	 */
	@Override
	public void createControl(final Composite parent, String id) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		control.setLayout(WidgetBuilderUtils.createGridLayout(1, false));

		/*
		 * script upload controls
		 */
		GridLayout layout = WidgetBuilderUtils.createGridLayout(6, false);
		GridData gd = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, false, false, 600,
				JAXBControlUIConstants.DEFAULT, 6, JAXBControlUIConstants.DEFAULT);
		gd.verticalAlignment = SWT.CENTER;
		Group group = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);

		WidgetBuilderUtils.createLabel(group, Messages.BatchScriptPath, SWT.LEFT, 1);
		WidgetBuilderUtils.createLabel(group, JAXBControlUIConstants.ZEROSTR, SWT.LEFT, 1);
		gd = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, true, false, JAXBControlUIConstants.DEFAULT,
				JAXBControlUIConstants.DEFAULT, 2, JAXBControlUIConstants.DEFAULT);
		gd.verticalAlignment = SWT.CENTER;
		String s = selected == null ? JAXBControlUIConstants.ZEROSTR : selected.toString();
		choice = WidgetBuilderUtils.createText(group, SWT.BORDER, gd, true, s);
		browseWorkspace = WidgetBuilderUtils.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_1, this);
		SWTUtil.setButtonDimensionHint(browseWorkspace);
		clear = WidgetBuilderUtils.createPushButton(group, Messages.ClearScript, this);
		SWTUtil.setButtonDimensionHint(clear);

		/*
		 * attribute viewer
		 */
		if (viewerType != null) {
			try {
				LaunchTabBuilder builder = new LaunchTabBuilder(this);
				if (listeners != null) {
					listeners.clear();
				}
				localWidgets.clear();
				layout = WidgetBuilderUtils.createGridLayout(1, true);
				gd = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, true, false, JAXBControlUIConstants.DEFAULT,
						JAXBControlUIConstants.DEFAULT, 2, JAXBControlUIConstants.DEFAULT);
				gd.verticalAlignment = SWT.CENTER;
				group = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
				group.setText(Messages.OverrideEnvironment);
				group.setToolTipText(Messages.OverrideEnvironmentTooltip);
				builder.addAttributeViewer(viewerType, group);
			} catch (Throwable t) {
				t.printStackTrace();
				throw CoreExceptionUtils.newException(Messages.CreateControlConfigurableError + JAXBUIConstants.SP + title, t);
			}
		}

		/*
		 * text editor
		 */
		layout = WidgetBuilderUtils.createGridLayout(1, true);
		gd = WidgetBuilderUtils.createGridData(GridData.FILL_BOTH, true, true, JAXBControlUIConstants.DEFAULT,
				JAXBControlUIConstants.DEFAULT, 1, JAXBControlUIConstants.DEFAULT);
		group = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		int style = SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
		editor = WidgetBuilderUtils.createText(group, style, gd, true, JAXBControlUIConstants.ZEROSTR, null, null);
		WidgetBuilderUtils.applyMonospace(editor);
		editor.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				// Ignore
			}

			public void mouseDown(MouseEvent e) {
				// Ignore
			}

			public void mouseUp(MouseEvent e) {
				MessageDialog.openWarning(parent.getShell(), Messages.ReadOnlyWarning_title, Messages.ReadOnlyWarning);
			}
		});

		/*
		 * view buttons
		 */
		createViewScriptGroup(control);

		control.layout(true, true);
		selected = null;
		updateControls();
	}

	/*
	 * If there is a script path in the configuration, display that script. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public RMLaunchValidation initializeFrom(ILaunchConfiguration configuration) {
		try {
			RMLaunchValidation validation = super.initializeFrom(configuration);
			if (!validation.isSuccess()) {
				return validation;
			}
			String value = getAttribute(JAXBControlUIConstants.SCRIPT_PATH, configuration);
			if (!JAXBControlUIConstants.ZEROSTR.equals(value)) {
				selected = value;
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
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig) {
		return super.isValid(launchConfig);
	}

	/*
	 * Nothing to do here. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		return super.setDefaults(configuration);
	}

	/*
	 * Tab acts as listener for browse, clear and path buttons (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt .events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object source = e.getSource();
		try {
			if (source == browseWorkspace) {
				selected = WidgetActionUtils.browseWorkspace(control.getShell());
				updateContents();
			} else if (source == clear) {
				selected = null;
				updateContents();
			} else {
				super.widgetSelected(e);
			}
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(control.getShell(), t, Messages.WidgetSelectedError, Messages.WidgetSelectedErrorTitle,
					false);
		}
	}

	/*
	 * Store SCRIPT_PATH. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.launch.AbstractJAXBLaunchConfigurationTab# doRefreshLocal()
	 */
	@Override
	protected void doRefreshLocal() {
		super.doRefreshLocal();
		parentTab.getVariableMap().putValue(JAXBControlUIConstants.SCRIPT_PATH, selected);
	}

	/*
	 * Let the writeLocalProperties determine validity of script, excluded by all other controllers. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBDynamicLaunchConfigurationTab #getLocalInvalid()
	 */
	@Override
	protected Set<String> getLocalInvalid() {
		Set<String> localInvalid = super.getLocalInvalid();
		localInvalid.remove(JAXBControlUIConstants.SCRIPT_PATH);
		return localInvalid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.AbstractJAXBLaunchConfigurationTab #writeLocalProperties()
	 */
	@Override
	protected void writeLocalProperties() {
		if (selected != null && !JAXBUIConstants.ZEROSTR.equals(selected)) {
			validSet.add(JAXBControlUIConstants.SCRIPT_PATH);
		}
		super.writeLocalProperties();
	}

	/**
	 * Adds the View Script and Restore Defaults buttons to the bottom of the control pane.
	 * 
	 * @param control
	 */
	private void createViewScriptGroup(final Composite control) {
		GridLayout layout = WidgetBuilderUtils.createGridLayout(4, true, 5, 5, 2, 2);
		GridData gd = WidgetBuilderUtils.createGridData(SWT.NONE, 4);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);

		Button b = WidgetBuilderUtils.createPushButton(grp, Messages.ViewConfig, this);
		SWTUtil.setButtonDimensionHint(b);
		b.setToolTipText(Messages.ViewConfigTooltip);

		b = WidgetBuilderUtils.createPushButton(grp, Messages.DefaultValues, new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				resetDefaults();
			}
		});
		SWTUtil.setButtonDimensionHint(b);
	}

	/**
	 * Provides the LCVariableMap prefixing function
	 * 
	 * @param name
	 *            of attribute
	 * @param config
	 *            Launch Configuration
	 * @return variable value
	 */
	private String getAttribute(String name, ILaunchConfiguration config) throws CoreException {
		return config.getAttribute(rmPrefix + name, JAXBUIConstants.ZEROSTR);
	}

	/*
	 * Reads in the script if the selected path is set, then notifies the ResourcesTab of the change.
	 */
	private void updateContents() throws Throwable {
		uploadScript();
		fireContentsChanged();
	}

	/*
	 * Display the selected path and the script if they are set; enable the clear button if script is non-empty.
	 */
	private void updateControls() {
		if (selected != null) {
			choice.setText(selected);
		} else {
			choice.setText(JAXBControlUIConstants.ZEROSTR);
		}
		editor.setText(contents.toString());
		if (JAXBControlUIConstants.ZEROSTR.equals(contents)) {
			clear.setEnabled(false);
		} else {
			clear.setEnabled(true);
		}
	}

	/*
	 * If selected is set, read in the contents of the file with that path. Calls #updateControls().
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
					contents.append(line).append(JAXBControlUIConstants.LINE_SEP);
				} catch (EOFException eof) {
					break;
				}
			}
			br.close();
		}
		updateControls();
	}
}
