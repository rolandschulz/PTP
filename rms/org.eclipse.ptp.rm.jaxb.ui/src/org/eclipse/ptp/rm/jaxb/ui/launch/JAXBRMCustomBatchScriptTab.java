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
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.ConfigUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.RemoteUIServicesUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
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
			/*
			 * this will call copyToFields if there is a change
			 */
			try {
				maybeWriteToFile();
			} catch (Throwable t1) {
				throw new ValidationException(t1.getMessage());
			}
			String uriStr = choice.getText();
			if (!ZEROSTR.equals(selected)) {
				try {
					selected = new URI(uriStr);
				} catch (URISyntaxException t) {
					throw new ValidationException(Messages.ErrorOnCopyFromFields);
				}
			}
			contents = editor.getText();
		}

		@Override
		protected void copyToFields() {
			if (selected != null) {
				choice.setText(selected.toString());
			} else {
				choice.setText(ZEROSTR);
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

			if (null == selected) {
				config.removeAttribute(SCRIPT_PATH);
			} else {
				config.setAttribute(SCRIPT_PATH, selected.toString());
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
				String uriStr = config.getAttribute(SCRIPT_PATH, ZEROSTR);
				if (!ZEROSTR.equals(selected)) {
					selected = new URI(uriStr);
				}
				contents = config.getAttribute(SCRIPT, ZEROSTR);
			} catch (Throwable t) {
				WidgetActionUtils.errorMessage(control.getShell(), t, Messages.ErrorOnLoadFromStore, Messages.ErrorOnLoadTitle,
						false);
			}
		}

		@Override
		protected void validateLocal() throws ValidationException {
		}
	}

	/*
	 * The list of listeners will always include the ContentsChangedListener of
	 * the Resources Tab, which bottoms out in an updateButtons call which
	 * enables the "Apply" button.
	 * 
	 * The performApply of the ResourcesTab calls the performApply of the
	 * BaseRMLaunchConfigurationDynamicTab which calls the storeAndValidate
	 * method of the DataSource.
	 * 
	 * The methods loadAndUpdate() and justUpdate() on the DataSource can be
	 * used to refresh. The former is called on RM initialization, which takes
	 * place when the RM becomes visible.
	 */
	private class JAXBBatchScriptWidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener {

		private boolean toContents = false;

		public JAXBBatchScriptWidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}

		@Override
		protected void doModifyText(ModifyEvent e) {
			if (loading) {
				return;
			}
			Object source = e.getSource();
			if (source == editor && !toContents) {
				fileDirty = true;
				updateControls();
			}
			super.doModifyText(e);
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			if (loading) {
				return;
			}
			Object source = e.getSource();
			try {
				if (source == browseLocal) {
					selected = RemoteUIServicesUtils.browse(control.getShell(), selected, delegate, false);
					updateContents();
				} else if (source == browseRemote) {
					selected = RemoteUIServicesUtils.browse(control.getShell(), selected, delegate, true);
					updateContents();
				} else if (source == clear) {
					selected = null;
					updateContents();
				}
				super.doWidgetSelected(e);
			} catch (Throwable t) {
				WidgetActionUtils.errorMessage(control.getShell(), t, Messages.WidgetSelectedError,
						Messages.WidgetSelectedErrorTitle, false);
			}
		}
	}

	private final JAXBRMLaunchConfigurationDynamicTab pTab;
	private final RemoteServicesDelegate delegate;
	private final String title;

	private Composite control;
	private Text choice;
	private Text editor;
	private Button browseLocal;
	private Button browseRemote;
	private Button clear;

	private JAXBBatchScriptDataSource dataSource;
	private JAXBBatchScriptWidgetListener listener;

	private URI selected;
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
		this.delegate = rm.getRemoteServicesDelegate();
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
		String s = selected == null ? ZEROSTR : selected.toString();
		choice = WidgetBuilderUtils.createText(comp, SWT.BORDER, gdsub, true, s);
		browseLocal = WidgetBuilderUtils.createPushButton(comp, Messages.JAXBRMConfigurationSelectionWizardPage_1, listener);
		browseRemote = WidgetBuilderUtils.createPushButton(comp, Messages.JAXBRMConfigurationSelectionWizardPage_2, listener);

		layout = WidgetBuilderUtils.createGridLayout(1, true);
		gd = WidgetBuilderUtils.createGridData(GridData.FILL_BOTH, true, true, 130, 300, 1, DEFAULT);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		int style = SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL;
		gdsub = WidgetBuilderUtils.createGridDataFill(DEFAULT, DEFAULT, 1);
		editor = WidgetBuilderUtils.createText(grp, style, gdsub, false, ZEROSTR, listener, null);
		WidgetBuilderUtils.applyMonospace(editor);

		layout = WidgetBuilderUtils.createGridLayout(6, true);
		gd = WidgetBuilderUtils.createGridDataFillH(6);
		comp = WidgetBuilderUtils.createComposite(control, SWT.NONE, layout, gd);
		clear = WidgetBuilderUtils.createPushButton(comp, Messages.ClearScript, listener);
		selected = null;
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

	public void maybeWriteToFile() throws Throwable {
		if (!fileDirty) {
			return;
		}

		URI newPath = RemoteUIServicesUtils.writeContentsToFile(control.getShell(), contents, selected);
		if (newPath != null) {
			selected = newPath;
			updateContents();
		}
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	@Override
	public void updateControls() {
		if (ZEROSTR.equals(contents)) {
			clear.setEnabled(false);
		} else {
			clear.setEnabled(true);
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

	private void updateContents() throws Throwable {
		if (null != selected) {
			contents = ConfigUtils.getFileContents(new File(selected));
		} else {
			contents = ZEROSTR;
		}
		dataSource.copyToFields();
		fileDirty = false;
		updateControls();
	}
}
