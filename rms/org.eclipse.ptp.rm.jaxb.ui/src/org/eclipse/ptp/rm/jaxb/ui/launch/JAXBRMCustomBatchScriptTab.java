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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.ConfigUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.rm.ui.utils.DataSource.ValidationException;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author arossi
 * 
 */
public class JAXBRMCustomBatchScriptTab extends BaseRMLaunchConfigurationDynamicTab implements IJAXBUINonNLSConstants {

	private class JAXBBatchScriptDataSource extends RMLaunchConfigurationDynamicTabDataSource {

		protected JAXBBatchScriptDataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			selected = choice.getText();
			contents = editor.getText();
		}

		@Override
		protected void copyToFields() {
			if (selected != null) {
				choice.setText(selected);
			}
			if (contents != null) {
				listener.toContents = true;
				editor.setText(contents);
				listener.toContents = false;
			}
		}

		@Override
		protected void copyToStorage() {
			ILaunchConfigurationWorkingCopy config = getConfigurationWorkingCopy();
			if (config == null) {
				JAXBUIPlugin.log(Messages.MissingLaunchConfigurationError);
				return;
			}

			if (ZEROSTR.equals(selected)) {
				config.removeAttribute(SCRIPT_PATH);
			} else {
				config.setAttribute(SCRIPT_PATH, selected);
			}

			if (ZEROSTR.equals(contents)) {
				config.removeAttribute(SCRIPT);
			} else {
				config.setAttribute(SCRIPT, contents);
			}
		}

		@Override
		protected void loadDefault() {
		}

		@Override
		protected void loadFromStorage() {
			ILaunchConfiguration config = getConfiguration();
			if (config == null) {
				JAXBUIPlugin.log(Messages.MissingLaunchConfigurationError);
				return;
			}
			try {
				selected = config.getAttribute(SCRIPT_PATH, ZEROSTR);
				contents = config.getAttribute(SCRIPT, ZEROSTR);
			} catch (CoreException t) {
				WidgetActionUtils.errorMessage(control.getShell(), t, Messages.ErrorOnLoadFromStore, Messages.ErrorOnLoadTitle,
						false);
			}
		}

		@Override
		protected void validateLocal() throws ValidationException {
		}
	}

	private class JAXBBatchScriptWidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener {
		private boolean toContents = false;

		public JAXBBatchScriptWidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (loading) {
				return;
			}
			Object source = e.getSource();
			if (source == editor && !toContents) {
				fileDirty = true;
				updateControls();
			}
			super.modifyText(e);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (loading) {
				return;
			}
			Object source = e.getSource();
			try {
				if (source == browseHomeButton) {
					selected = handlePathBrowseButtonSelected(ConfigUtils.getUserHome(), IRemoteUIConstants.OPEN);
					updateContents();
				} else if (source == browseProjectButton) {
					selected = handlePathBrowseButtonSelected(ConfigUtils.chooseLocalProject(control.getShell()),
							IRemoteUIConstants.OPEN);
					updateContents();
				} else if (source == saveToFile) {
					if (ZEROSTR.equals(selected)) {
						return;
					}
					String newPath = ConfigUtils.writeContentsToFile(control.getShell(), contents, new File(selected));
					if (newPath != null) {
						selected = newPath;
						updateContents();
					}
				} else if (source == clear) {
					selected = ZEROSTR;
					updateContents();
				}
				super.widgetSelected(e);
			} catch (Throwable t) {
				WidgetActionUtils.errorMessage(control.getShell(), t, Messages.WidgetSelectedError,
						Messages.WidgetSelectedErrorTitle, false);
			}
		}
	}

	private final JAXBRMLaunchConfigurationDynamicTab pTab;
	private final String title;

	private Composite control;
	private Text choice;
	private Text editor;
	private Button browseHomeButton;

	private Button browseProjectButton;
	private Button saveToFile;
	private Button clear;

	private JAXBBatchScriptDataSource dataSource;
	private JAXBBatchScriptWidgetListener listener;

	private String selected;
	private String contents;
	private boolean fileDirty;
	private boolean loading;

	/**
	 * @param dialog
	 */
	public JAXBRMCustomBatchScriptTab(IJAXBResourceManagerControl rm, ILaunchConfigurationDialog dialog, String title,
			JAXBRMLaunchConfigurationDynamicTab pTab) {
		super(dialog);
		this.pTab = pTab;
		if (title == null) {
			title = Messages.CustomBatchScriptTab_title;
		}
		this.title = title;
	}

	@Override
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		RMLaunchValidation rmv = super.canSave(control, rm, queue);
		if (!fileDirty) {
			return rmv;
		}
		return new RMLaunchValidation(false, Messages.FileContentsDirty);
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		loading = true;
		control = WidgetBuilderUtils.createComposite(parent, 1);
		GridLayout layout = WidgetBuilderUtils.createGridLayout(6, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(6);
		Composite comp = WidgetBuilderUtils.createComposite(control, SWT.NONE, layout, gd);
		WidgetBuilderUtils.createLabel(comp, Messages.BatchScriptPath, SWT.LEFT, 1);
		GridData gdsub = WidgetBuilderUtils.createGridDataFillH(3);
		choice = WidgetBuilderUtils.createText(comp, SWT.BORDER, gdsub, true, selected);
		browseHomeButton = WidgetBuilderUtils.createPushButton(comp, Messages.JAXBRMConfigurationSelectionWizardPage_1, listener);
		browseProjectButton = WidgetBuilderUtils
				.createPushButton(comp, Messages.JAXBRMConfigurationSelectionWizardPage_2, listener);

		layout = WidgetBuilderUtils.createGridLayout(1, true);
		gd = WidgetBuilderUtils.createGridData(GridData.FILL_BOTH, true, true, 130, 300, 1);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		int style = SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL;
		gdsub = WidgetBuilderUtils.createGridDataFill(DEFAULT, DEFAULT, 1);
		editor = WidgetBuilderUtils.createText(grp, style, gdsub, false, ZEROSTR, listener, null);
		WidgetBuilderUtils.applyMonospace(editor);

		layout = WidgetBuilderUtils.createGridLayout(6, true);
		gd = WidgetBuilderUtils.createGridDataFillH(6);
		comp = WidgetBuilderUtils.createComposite(control, SWT.NONE, layout, gd);
		saveToFile = WidgetBuilderUtils.createPushButton(comp, Messages.SaveToFileButton, listener);
		clear = WidgetBuilderUtils.createPushButton(comp, Messages.ClearScript, listener);
		selected = ZEROSTR;
		pTab.resize(control);
		fileDirty = false;
		updateControls();
		loading = false;
	}

	public Control getControl() {
		return control;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getText() {
		return title;
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	@Override
	public void updateControls() {
		if (fileDirty) {
			saveToFile.setEnabled(true);
		} else {
			saveToFile.setEnabled(false);
		}
	}

	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		if (dataSource == null) {
			dataSource = new JAXBBatchScriptDataSource(this);
		}
		return dataSource;
	}

	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		if (listener == null) {
			listener = new JAXBBatchScriptWidgetListener(this);
		}
		return listener;
	}

	private String handlePathBrowseButtonSelected(File initPath, int opt) {
		if (initPath == null) {
			return ZEROSTR;
		}
		IRemoteServices localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		IRemoteUIServices localUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(localServices);
		String path = null;
		if (localServices != null && localUIServices != null) {
			IRemoteConnectionManager lconnMgr = localServices.getConnectionManager();
			IRemoteConnection lconn = lconnMgr.getConnection(ZEROSTR);
			IRemoteUIFileManager localUIFileMgr = localUIServices.getUIFileManager();
			localUIFileMgr.setConnection(lconn);
			path = localUIFileMgr.browseFile(control.getShell(), Messages.JAXBRMConfigurationSelectionWizardPage_0,
					initPath.getAbsolutePath(), opt);
		}
		return path == null ? ZEROSTR : path;
	}

	private void saveSettings() {
		try {
			dataSource.copyFromFields();
			dataSource.copyToStorage();
		} catch (ValidationException t) {
			WidgetActionUtils.errorMessage(control.getShell(), t, Messages.ErrorOnSaveWidgets, Messages.ErrorOnSaveTitle, false);
		}
	}

	private void updateContents() throws Throwable {
		if (!ZEROSTR.equals(selected)) {
			contents = ConfigUtils.getFileContents(new File(selected));
		} else {
			contents = ZEROSTR;
		}
		dataSource.copyToFields();
		saveSettings();
		fileDirty = false;
		updateControls();
	}
}
