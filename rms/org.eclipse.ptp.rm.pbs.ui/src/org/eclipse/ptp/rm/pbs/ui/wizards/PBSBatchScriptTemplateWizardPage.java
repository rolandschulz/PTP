/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors: 
 * 	IBM Corporation - initial API and implementation Contributors:
 * 	Albert L. Rossi (NCSA) - full implementation
 *                         - modifications 05/11/2010
 *                  - modifications to use new converter class; non-nls
 *                    constants interface (09/14/2010)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.wizards;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;
import org.eclipse.ptp.rm.pbs.core.templates.PBSBatchScriptTemplate;
import org.eclipse.ptp.rm.pbs.core.templates.PBSBatchScriptTemplateManager;
import org.eclipse.ptp.rm.pbs.ui.PBSUIPlugin;
import org.eclipse.ptp.rm.pbs.ui.dialogs.ScrollingEditableMessageDialog;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.providers.AttributeContentProvider;
import org.eclipse.ptp.rm.pbs.ui.providers.AttributeLabelProvider;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.ui.utils.WidgetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.progress.UIJob;

/**
 * Allows the user to create or modify new templates for use in the Launch Tab.
 * 
 * @author arossi
 */
public class PBSBatchScriptTemplateWizardPage extends WizardPage implements IPBSNonNLSConstants {

	/*
	 * Associated with the selection of a choice of configuration file.
	 */
	private class ConfigurationChangeListener extends WidgetListener implements ISelectionChangedListener {

		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSource() == readOnlyView)
				updateSettings();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.rm.ui.utils.WidgetListener#doModifyText(org.eclipse
		 * .swt.events.ModifyEvent)
		 */
		@Override
		protected void doModifyText(ModifyEvent e) {
			if (e.getSource() == templates)
				updateSettings();
		}
	}

	/*
	 * Associated with the delete button.
	 */
	private class DeleteConfigurationSelectionAdapter implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		/**
		 * Deletes the selected configuration file, and updates the combo box
		 * selections, as well as clearing the table viewer if the deleted file
		 * was the one selected.
		 */
		public void widgetSelected(SelectionEvent e) {
			try {
				String name = WidgetUtils.getSelected(templates);
				resourceManager.getTemplateManager().removeTemplate(name);
				updateTemplates(name);
				updateSettings();
			} catch (Throwable t) {
				t.printStackTrace();
				WidgetUtils.errorMessage(getShell(), t, Messages.PBSRMLaunchConfigDeleteError_message,
						Messages.PBSRMLaunchConfigDeleteError_title, false);
			}
		}
	}

	/*
	 * Associated with the edit button.
	 */
	private class EditConfigurationSelectionAdapter implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		/**
		 * Opens the selected file for editing. If a new name is provided, the
		 * changes are written to a new file by that name. Else changes are
		 * overwritten to the existing file.
		 */
		public void widgetSelected(SelectionEvent e) {
			FileWriter fw = null;
			try {
				Shell shell = getShell();
				String newName = null;
				String oldName = WidgetUtils.getSelected(templates);
				InputDialog nameDialog = new InputDialog(shell, Messages.PBSRMLaunchConfigEditChoose_new + QM,
						Messages.PBSRMLaunchConfigEditChoose_new_name, oldName, null);
				if (nameDialog.open() == Window.CANCEL)
					return;
				newName = resourceManager.getTemplateManager().validateTemplateNameForEdit(nameDialog.getValue());

				PBSBatchScriptTemplate template = resourceManager.getTemplateManager().loadTemplate(oldName, null);
				ScrollingEditableMessageDialog dialog = new ScrollingEditableMessageDialog(getShell(),
						Messages.PBSRMLaunchConfigEditChoose_message, template.getText());
				if (dialog.open() == Window.CANCEL)
					return;
				String edited = dialog.getValue();
				resourceManager.getTemplateManager().storeTemplate(edited, newName);
				updateTemplates(newName);
				updateSettings();
			} catch (Throwable t) {
				t.printStackTrace();
				WidgetUtils.errorMessage(getShell(), t, Messages.PBSRMLaunchConfigEditError_message,
						Messages.PBSRMLaunchConfigEditError_title, false);
			} finally {
				if (fw != null)
					try {
						fw.close();
					} catch (IOException t) {
						t.printStackTrace();
					}
			}
		}
	}

	/*
	 * Associated with the export button.
	 */
	private class ExportConfigurationSelectionAdapter implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		/**
		 * Stores the selected template to a file on the local file system.
		 * 
		 */
		public void widgetSelected(SelectionEvent e) {
			final String original = WidgetUtils.getSelected(templates);
			String input = original;
			if (ZEROSTR.equals(original))
				return;
			final String dir = handleExportBrowseButtonSelected();
			if (ZEROSTR.equals(dir))
				return;
			InputDialog nameDialog = new InputDialog(PBSUIPlugin.getActiveWorkbenchShell(), Messages.PBSRMLaunchConfigExportRename,
					Messages.PBSRMLaunchConfigExportRename_new, original, null);
			if (nameDialog.open() != Window.CANCEL)
				input = nameDialog.getValue();
			final String renamed = input;
			new UIJob(Messages.PBSRMLaunchConfigExportJobMessage0 + renamed + Messages.PBSRMLaunchConfigExportJobMessage1) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						monitor.beginTask(Messages.PBSRMLaunchConfigImportJobMessage, 2);
						resourceManager.getTemplateManager().exportTemplate(dir, original, renamed);
						monitor.worked(2);
					} catch (Throwable t) {
						t.printStackTrace();
						WidgetUtils.errorMessage(getShell(), t, Messages.PBSRMLaunchConfigExportError_message,
								Messages.PBSRMLaunchConfigExportError_title, false);
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	/*
	 * Associated with the import button.
	 */
	private class ImportConfigurationSelectionAdapter implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		/**
		 * Imports a template from a file selected from the local file system.
		 * 
		 */
		public void widgetSelected(SelectionEvent e) {
			String file = handleImportBrowseButtonSelected();
			final File imported = new File(file);
			new UIJob(Messages.PBSRMLaunchConfigImportJobMessage + imported.getName()) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						monitor.beginTask(Messages.PBSRMLaunchConfigImportJobMessage, 2);
						resourceManager.getTemplateManager().addImportedTemplate(imported);
						monitor.worked(1);
						updateTemplates(imported.getName());
						updateSettings();
					} catch (Throwable t) {
						t.printStackTrace();
						WidgetUtils.errorMessage(getShell(), t, Messages.PBSRMLaunchConfigImportError_message,
								Messages.PBSRMLaunchConfigImportError_title, false);
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	private boolean isValid;

	private ConfigurationChangeListener listener;
	private TableViewer readOnlyView;
	private final PBSResourceManager resourceManager;
	private Combo templates;
	private String selected;

	public PBSBatchScriptTemplateWizardPage(PBSResourceManager rm) throws Throwable {
		super(Messages.PBSConfigurationWizardPage_name);
		setTitle(Messages.PBSConfigurationWizardPage_title);
		setDescription(Messages.PBSConfigurationWizardPage_description);
		this.resourceManager = rm;
		selected = ZEROSTR;
		setValid(true);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		composite.setLayout(topLayout);
		createContents(composite);
		PBSBatchScriptTemplate current = resourceManager.getTemplateManager().getCurrent();
		String last = current == null ? PBSBatchScriptTemplateManager.FULL_TEMPLATE : current.getName();
		WidgetUtils.select(templates, last);
		setControl(composite);
	}

	public String getSelectedTemplate() {
		return selected;
	}

	private void createContents(Composite parent) {
		Group templateContainer = WidgetUtils.createFillingGroup(parent, ZEROSTR, 4, 1, false);
		listener = new ConfigurationChangeListener();
		String[] available = resourceManager.getTemplateManager().findAvailableTemplates();
		String initial = available.length == 0 ? null : available[0];
		templates = WidgetUtils.createItemCombo(templateContainer, Messages.PBSRMLaunchConfigTemplate_title, available, initial,
				Messages.PBSRMLaunchConfigTemplate_message, true, listener, 2);
		WidgetUtils.createLabel(templateContainer, ZEROSTR, SWT.LEFT, 1);
		WidgetUtils.createButton(templateContainer, Messages.PBSRMLaunchConfigEditButton_title, null, SWT.PUSH, 1, true,
				new EditConfigurationSelectionAdapter());
		WidgetUtils.createButton(templateContainer, Messages.PBSRMLaunchConfigDeleteButton_title, null, SWT.PUSH, 1, true,
				new DeleteConfigurationSelectionAdapter());
		WidgetUtils.createButton(templateContainer, Messages.PBSRMLaunchConfigImportButton_title, null, SWT.PUSH, 1, true,
				new ImportConfigurationSelectionAdapter());
		WidgetUtils.createButton(templateContainer, Messages.PBSRMLaunchConfigExportButton_title, null, SWT.PUSH, 1, true,
				new ExportConfigurationSelectionAdapter());

		Group preferencesContainer = WidgetUtils.createFillingGroup(parent, Messages.PBSRMLaunchConfigPreferences_message, 1, 1,
				false);
		Table t = WidgetUtils.createFillingTable(preferencesContainer, 3, 500, 1, SWT.FULL_SELECTION | SWT.MULTI);
		readOnlyView = new TableViewer(t);
		readOnlyView.setContentProvider(new AttributeContentProvider());
		readOnlyView.setLabelProvider(new AttributeLabelProvider());
		WidgetUtils.addTableColumn(readOnlyView, Messages.PBSRMLaunchConfigPreferences_column_0, SWT.LEFT, null);
		WidgetUtils.addTableColumn(readOnlyView, Messages.PBSRMLaunchConfigPreferences_column_1, SWT.LEFT, null);
		WidgetUtils.addTableColumn(readOnlyView, Messages.PBSRMLaunchConfigPreferences_column_2, SWT.LEFT, null);
		readOnlyView.getTable().setHeaderVisible(true);
	}

	private String handleExportBrowseButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(PBSUIPlugin.getActiveWorkbenchShell());
		dialog.setText(Messages.PBSRMLaunchConfigExportButton_message);
		return dialog.open();
	}

	private String handleImportBrowseButtonSelected() {
		FileDialog dialog = new FileDialog(PBSUIPlugin.getActiveWorkbenchShell(), SWT.SINGLE);
		dialog.setText(Messages.PBSRMLaunchConfigImportButton_message);
		return dialog.open();
	}

	/**
	 * @param b
	 */
	private void setValid(boolean b) {
		isValid = b;
		setPageComplete(isValid);
	}

	/*
	 * Populates the table viewer with the attributes from the selected template
	 * file.
	 * 
	 * @param choice the configuration file selected
	 */
	private void updateSettings() {
		PBSBatchScriptTemplate template = null;
		selected = WidgetUtils.getSelected(templates);
		if (!ZEROSTR.equals(selected))
			template = resourceManager.getTemplateManager().loadTemplate(selected, null);
		if (template == null)
			readOnlyView.setInput(this);
		else
			readOnlyView.setInput(template);
	}

	/*
	 * Called after the Edit and Delete actions; refreshes the choices in the
	 * combo box.
	 * 
	 * @param current the edited or deleted template
	 */
	private void updateTemplates(String current) {
		listener.disable();
		templates.setItems(resourceManager.getTemplateManager().findAvailableTemplates());
		String next = WidgetUtils.select(templates, current);
		listener.enable();
		if (!next.equals(current))
			WidgetUtils.select(templates, next);
	}
}
