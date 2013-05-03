package org.eclipse.ptp.services.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.widgets.ServiceConfigurationSelectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ServiceConfigurationExportWizard extends Wizard implements IImportWizard {
	private class MainExportWizardPage extends WizardPage {

		private String file = ""; //$NON-NLS-1$
		private Combo fileCombo;
		private Button browseButton;
		private int messageType = NONE;
		private ServiceConfigurationSelectionWidget serviceConfigWidget;

		public MainExportWizardPage(String pageName, String title, ImageDescriptor titleImage) {
			super(pageName, title, titleImage);
			setDescription(Messages.ServiceConfigurationExportWizard_0);
		}

		public void createControl(Composite parent) {
			Composite workArea = new Composite(parent, SWT.NONE);
			setControl(workArea);

			workArea.setFont(parent.getFont());
			workArea.setLayout(new GridLayout());
			workArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

			createFileSelectionArea(workArea);
			createConfigurationsSelectionArea(workArea);

			setControl(workArea);
			Dialog.applyDialogFont(parent);
			updateEnablement();
			messageType = ERROR;
		}

		public void createFileSelectionArea(Composite workArea) {
			Composite composite = new Composite(workArea, SWT.NULL);
			composite.setFont(workArea.getFont());
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			composite.setLayout(layout);

			Label label = new Label(composite, SWT.NONE);
			label.setText(Messages.ServiceConfigurationExportWizard_4);

			fileCombo = new Combo(composite, SWT.DROP_DOWN);
			GridData comboData = new GridData(GridData.FILL_HORIZONTAL);
			comboData.verticalAlignment = GridData.CENTER;
			comboData.grabExcessVerticalSpace = false;
			comboData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			fileCombo.setLayoutData(comboData);
			file = FilenameStore.getSuggestedDefault();
			fileCombo.setItems(FilenameStore.getHistory());
			fileCombo.setText(file);
			fileCombo.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					file = fileCombo.getText();
					updateEnablement();
				}
			});

			browseButton = new Button(composite, SWT.PUSH);
			browseButton.setText(Messages.ServiceConfigurationExportWizard_5);
			GridData data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
			data.widthHint = Math.max(widthHint, browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
			browseButton.setLayoutData(data);
			browseButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					FileDialog d = new FileDialog(getShell(), SWT.SAVE);
					d.setFilterExtensions(new String[] { "*.cfg" }); //$NON-NLS-1$
					d.setFilterNames(new String[] { Messages.ServiceConfigurationExportWizard_6 });
					d.setFileName(Messages.ServiceConfigurationExportWizard_7);
					String fileName = getFileName();
					if (fileName != null) {
						int separator = fileName.lastIndexOf(System.getProperty("file.separator").charAt(0)); //$NON-NLS-1$
						if (separator != -1) {
							fileName = fileName.substring(0, separator);
						}
					}
					d.setFilterPath(fileName);
					String f = d.open();
					if (f != null) {
						fileCombo.setText(f);
						file = f;
					}
				}
			});
		}

		public String getFileName() {
			return file;
		}

		public IServiceConfiguration[] getServiceConfigurations() {
			return serviceConfigWidget.getCheckedServiceConfigurations();
		}

		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				fileCombo.setFocus();
			}
		}

		private void createConfigurationsSelectionArea(Composite workArea) {
			Composite composite = new Composite(workArea, SWT.NULL);
			composite.setFont(workArea.getFont());
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			composite.setLayout(layout);

			Label label = new Label(composite, SWT.NONE);
			label.setText(Messages.ServiceConfigurationExportWizard_1);

			serviceConfigWidget = new ServiceConfigurationSelectionWidget(composite, SWT.CHECK, null, null, true);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = 100;
			serviceConfigWidget.setLayoutData(data);
			serviceConfigWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					updateEnablement();
				}
			});
		}

		private void updateEnablement() {
			boolean complete = false;
			setMessage(null);
			setPageComplete(false);

			if (getServiceConfigurations().length == 0) {
				setPageComplete(false);
				return;
			}

			if (file.length() == 0) {
				setMessage(Messages.ServiceConfigurationExportWizard_8, messageType);
				setPageComplete(false);
				return;
			} else {
				// See if the file exists
				File f = new File(file);
				if (f.isDirectory()) {
					setMessage(Messages.ServiceConfigurationExportWizard_9, messageType);
					setPageComplete(false);
					return;
				}
				complete = true;
			}

			if (complete) {
				setErrorMessage(null);
				setDescription(Messages.ServiceConfigurationExportWizard_10);
			}

			setPageComplete(complete);
		}
	}

	private final MainExportWizardPage mainPage = new MainExportWizardPage("exportWizardPage", //$NON-NLS-1$
			Messages.ServiceConfigurationExportWizard_11, null);

	public ServiceConfigurationExportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.ServiceConfigurationExportWizard_13);
	}

	@Override
	public void addPages() {
		addPage(mainPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		FilenameStore.setDefaultFromSelection(workbench);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		File f = new File(mainPage.getFileName());
		if (f.exists()) {
			boolean result = MessageDialog.openQuestion(getShell(), Messages.ServiceConfigurationExportWizard_14,
					Messages.ServiceConfigurationExportWizard_15);
			if (!result) {
				return false;
			}
		}
		try {
			ServiceModelManager.getInstance().exportConfigurations(mainPage.getFileName(), mainPage.getServiceConfigurations());
		} catch (InvocationTargetException e) {
			// TODO: error dialog?
			return false;
		}
		return true;
	}

}
