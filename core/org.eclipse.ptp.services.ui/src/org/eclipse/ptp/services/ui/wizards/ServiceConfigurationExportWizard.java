package org.eclipse.ptp.services.ui.wizards;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.ui.messages.Messages;
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
	private class SelectServiceConfigurationsPage extends WizardPage {

		private ServiceConfigurationSelectionWidget serviceConfigWidget;

		public SelectServiceConfigurationsPage(String pageName, String title, ImageDescriptor titleImage) {
			super(pageName, title, titleImage);
			setDescription(Messages.ServiceConfigurationExportWizard_0); 
		}

		public void createControl(Composite parent) {
	        Composite composite = new Composite(parent, SWT.NULL);
	        composite.setFont(parent.getFont());
	        composite.setLayout(new GridLayout());
	        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
			
			setControl(composite);
			Dialog.applyDialogFont(parent);
			updateEnablement();
		}
		
		public IServiceConfiguration[] getServiceConfigurations() {
			return Arrays.asList(serviceConfigWidget.getCheckedElements()).toArray(new IServiceConfiguration[0]);
		}
		
		private void updateEnablement() {
			setMessage(null);
			setPageComplete(false);
			
			if (serviceConfigWidget.getCheckedElements().length > 0) {			
				setPageComplete(true);
			}
		}
	}
	
	private class SelectFilePage extends WizardPage {

		private String file = ""; //$NON-NLS-1$
		private Combo fileCombo;
		private Button browseButton;
		private int messageType = NONE;

		public SelectFilePage(String pageName, String title, ImageDescriptor titleImage) {
			super(pageName, title, titleImage);
			setDescription(Messages.ServiceConfigurationExportWizard_0); 
		}

		public void createControl(Composite parent) {
	        Composite composite = new Composite(parent, SWT.NULL);
	        composite.setFont(parent.getFont());
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
					FileDialog d = new FileDialog(getShell());
					d.setFilterExtensions(new String[] {"*.xml", "*"}); //$NON-NLS-1$ //$NON-NLS-2$
					d.setFilterNames(new String[] {Messages.ServiceConfigurationExportWizard_6, Messages.ServiceConfigurationExportWizard_7});
					String fileName= getFileName();
					if (fileName != null && fileName.length() > 0) {
						int separator= fileName.lastIndexOf(System.getProperty ("file.separator").charAt (0)); //$NON-NLS-1$
						if (separator != -1) {
							fileName= fileName.substring(0, separator);
						}
					} else {
						fileName= ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
					}
					d.setFilterPath(fileName);
					String f = d.open();
					if (f != null) {
						fileCombo.setText(f);
						file = f;
					}
				}
			});

			setControl(composite);
			updateEnablement();
			Dialog.applyDialogFont(parent);
			messageType = ERROR;
		}
		
		public String getFileName() {
			return file;
		}
		
		private void updateEnablement() {
			boolean complete = false;
			setMessage(null);
			
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
		
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				fileCombo.setFocus();
			}
		}
	}
	
	private SelectFilePage selectFilePage = new SelectFilePage("exportFilePage", Messages.ServiceConfigurationExportWizard_11, null); //$NON-NLS-1$
	private SelectServiceConfigurationsPage serviceConfigurationsPage = new SelectServiceConfigurationsPage("exportConfigurationPage", Messages.ServiceConfigurationExportWizard_11, null); //$NON-NLS-1$;
	
	public ServiceConfigurationExportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.ServiceConfigurationExportWizard_13); 
	}
	
	public void addPages() {
		addPage(serviceConfigurationsPage);
		addPage(selectFilePage);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		File f = new File(selectFilePage.getFileName());
		if (f.exists()) {
			boolean result = MessageDialog.openQuestion(getShell(), Messages.ServiceConfigurationExportWizard_14, Messages.ServiceConfigurationExportWizard_15);		
			if (!result) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		FilenameStore.setDefaultFromSelection(workbench);
	}

}
