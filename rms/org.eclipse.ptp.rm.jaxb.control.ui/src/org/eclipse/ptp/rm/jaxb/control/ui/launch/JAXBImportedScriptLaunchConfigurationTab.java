/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.launch;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIPlugin;
import org.eclipse.ptp.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.ui.utils.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
public class JAXBImportedScriptLaunchConfigurationTab extends AbstractJAXBLaunchConfigurationTab implements SelectionListener,
		ModifyListener {

	private Text choice;
	private Text editor;
	private Text stdoutText;
	private Text stderrText;
	private Button browseWorkspace;
	private Button clear;
	private Button enableFetchStdout;
	private Button enableFetchStderr;

	private String selected;
	private String stdoutPath;
	private String stderrPath;
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
		stdoutPath = JAXBControlUIConstants.ZEROSTR;
		stderrPath = JAXBControlUIConstants.ZEROSTR;
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
	public void createControl(final Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetBuilderUtils.createComposite(parent, 1);

		GridLayout layout = WidgetBuilderUtils.createGridLayout(6, false);
		GridData gd = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, false, false, 600,
				JAXBControlUIConstants.DEFAULT, 6, JAXBControlUIConstants.DEFAULT);
		Group comp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);

		/*
		 * path buttons/text
		 */
		maybeAddPathControls(control, ((IJAXBResourceManager) rm).getControl().getEnvironment());

		/*
		 * script upload controls
		 */
		WidgetBuilderUtils.createLabel(comp, Messages.BatchScriptPath, SWT.LEFT, 1);
		WidgetBuilderUtils.createLabel(comp, JAXBControlUIConstants.ZEROSTR, SWT.LEFT, 1);
		GridData gdsub = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, true, false, 310,
				JAXBControlUIConstants.DEFAULT, 2, JAXBControlUIConstants.DEFAULT);
		String s = selected == null ? JAXBControlUIConstants.ZEROSTR : selected.toString();
		choice = WidgetBuilderUtils.createText(comp, SWT.BORDER, gdsub, true, s);
		browseWorkspace = WidgetBuilderUtils.createPushButton(comp, Messages.JAXBRMConfigurationSelectionWizardPage_1, this);
		clear = WidgetBuilderUtils.createPushButton(comp, Messages.ClearScript, this);

		/*
		 * text editor
		 */
		layout = WidgetBuilderUtils.createGridLayout(1, true);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, null);
		int style = SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
		gdsub = WidgetBuilderUtils.createGridDataFill(600, 400, 1);
		editor = WidgetBuilderUtils.createText(grp, style, gdsub, true, JAXBControlUIConstants.ZEROSTR, null, null);
		WidgetBuilderUtils.applyMonospace(editor);
		editor.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
				MessageDialog.openWarning(parent.getShell(), Messages.ReadOnlyWarning_title, Messages.ReadOnlyWarning);
			}
		});

		size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		selected = null;
		updateControls();
	}

	/*
	 * If there is a path selected, store it in the local map as the
	 * SCRIPT_PATH. Also store or remove remote paths. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.launch.AbstractJAXBLaunchConfigurationTab#
	 * doRefreshLocal()
	 */
	@Override
	protected void doRefreshLocal() {
		if (selected != null) {
			localMap.put(JAXBControlUIConstants.SCRIPT_PATH, selected);
		}
		maybeRefreshPaths();
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
			String uriStr = configuration.getAttribute(JAXBControlUIConstants.SCRIPT_PATH, JAXBControlUIConstants.ZEROSTR);
			if (!JAXBControlUIConstants.ZEROSTR.equals(uriStr)) {
				selected = uriStr;
			} else {
				selected = null;
			}
			uploadScript();
			maybeInitializePaths(configuration);
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

	/**
	 * If the variables for remote path are defined in the configuration, add
	 * the buttons and text for retrieving them.
	 * 
	 * @param parent
	 * @throws CoreException
	 */
	private void maybeAddPathControls(final Composite parent, IVariableMap env) throws CoreException {
		if (env == null) {
			/*
			 * This means the tab has been opened without having started the RM.
			 * When the RM is started, this tab will be rebuilt.
			 */
			return;
		}

		Object stdout = env.get(JAXBControlUIConstants.STDOUT_REMOTE_FILE);
		Object stderr = env.get(JAXBControlUIConstants.STDERR_REMOTE_FILE);
		if (stdout == null && stderr == null) {
			return;
		}

		GridLayout layout = WidgetBuilderUtils.createGridLayout(4, false);
		GridData data = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, false, false, 400,
				JAXBControlUIConstants.DEFAULT, 4, JAXBControlUIConstants.DEFAULT);
		Group group = WidgetBuilderUtils.createGroup(parent, SWT.NONE, layout, data);
		if (stdout != null) {
			Label l = WidgetBuilderUtils.createLabel(group, Messages.RemoteScriptPath, SWT.LEFT, 1);
			l.setToolTipText(Messages.RemotePathTooltip);
			data = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, true, false, 175, JAXBControlUIConstants.DEFAULT, 2,
					JAXBControlUIConstants.DEFAULT);
			stdoutText = WidgetBuilderUtils.createText(group, SWT.BORDER, data, false, JAXBControlUIConstants.ZEROSTR);
			stdoutText.addModifyListener(this);
			enableFetchStdout = WidgetBuilderUtils.createCheckButton(group, Messages.EnableStdoutFetch, this);
		}
		if (stderr != null) {
			WidgetBuilderUtils.createLabel(group, Messages.RemoteScriptPath, SWT.LEFT, 1);
			data = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, true, false, 175, JAXBControlUIConstants.DEFAULT, 2,
					JAXBControlUIConstants.DEFAULT);
			stderrText = WidgetBuilderUtils.createText(group, SWT.BORDER, data, false, JAXBControlUIConstants.ZEROSTR);
			stderrText.addModifyListener(this);
			enableFetchStderr = WidgetBuilderUtils.createCheckButton(group, Messages.EnableStderrFetch, this);
		}
	}

	/**
	 * Upload the saved values, or disable if none.
	 * 
	 * @param configuration
	 */
	private void maybeInitializePaths(ILaunchConfiguration configuration) {
		try {
			if (stdoutText != null) {
				stdoutPath = configuration.getAttribute(JAXBControlUIConstants.STDOUT_REMOTE_FILE, JAXBControlUIConstants.ZEROSTR);
				if (JAXBControlUIConstants.ZEROSTR.equals(stdoutPath)) {
					stdoutText.setText(JAXBControlUIConstants.ZEROSTR);
					stdoutText.setEnabled(false);
					enableFetchStdout.setSelection(false);
				} else {
					stdoutText.setText(stdoutPath);
					stdoutText.setEnabled(true);
					enableFetchStdout.setSelection(true);
				}
			}
			if (stderrText != null) {
				stderrPath = configuration.getAttribute(JAXBControlUIConstants.STDERR_REMOTE_FILE, JAXBControlUIConstants.ZEROSTR);
				if (JAXBControlUIConstants.ZEROSTR.equals(stderrPath)) {
					stderrText.setText(JAXBControlUIConstants.ZEROSTR);
					stderrText.setEnabled(false);
					enableFetchStderr.setSelection(false);
				} else {
					stderrText.setText(stderrPath);
					stderrText.setEnabled(true);
					enableFetchStderr.setSelection(true);
				}
			}
		} catch (CoreException t) {
			JAXBControlUIPlugin.log(t);
		}
	}

	/**
	 * If the values are non-empty, add to map; else remove.
	 */
	private void maybeRefreshPaths() {
		if (JAXBControlUIConstants.ZEROSTR.equals(stdoutPath)) {
			localMap.remove(JAXBControlUIConstants.STDOUT_REMOTE_FILE);
		} else {
			localMap.put(JAXBControlUIConstants.STDOUT_REMOTE_FILE, stdoutPath);
		}
		if (JAXBControlUIConstants.ZEROSTR.equals(stderrPath)) {
			localMap.remove(JAXBControlUIConstants.STDERR_REMOTE_FILE);
		} else {
			localMap.put(JAXBControlUIConstants.STDERR_REMOTE_FILE, stderrPath);
		}
	}

	/*
	 * Tab acts as listener for path text boxes. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events
	 * .ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		try {
			if (stdoutText == e.getSource()) {
				stdoutPath = stdoutText.getText().trim();
			} else if (stderrText == e.getSource()) {
				stderrPath = stderrText.getText().trim();
			}
			fireContentsChanged();
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(control.getShell(), t, Messages.ModifyError, Messages.ModifyErrorTitle, false);
		}
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
					contents.append(line).append(JAXBControlUIConstants.LINE_SEP);
				} catch (EOFException eof) {
					break;
				}
			}
		}
		updateControls();
	}

	/*
	 * Tab acts as listener for browse, clear and path buttons (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Tab acts as listener for browse, clear and path buttons (non-Javadoc)
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
			} else if (source == enableFetchStdout) {
				boolean enabled = enableFetchStdout.getSelection();
				if (enabled) {
					stdoutText.setText(stdoutPath);
					stdoutText.setEnabled(true);
				} else {
					stdoutText.setText(JAXBControlUIConstants.ZEROSTR);
					stdoutText.setEnabled(false);
				}
				fireContentsChanged();
			} else if (source == enableFetchStderr) {
				boolean enabled = enableFetchStderr.getSelection();
				if (enabled) {
					stderrText.setText(stderrPath);
					stderrText.setEnabled(true);
				} else {
					stderrText.setText(JAXBControlUIConstants.ZEROSTR);
					stderrText.setEnabled(false);
				}
				fireContentsChanged();
			}
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(control.getShell(), t, Messages.WidgetSelectedError, Messages.WidgetSelectedErrorTitle,
					false);
		}
	}
}
