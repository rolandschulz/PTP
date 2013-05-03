package org.eclipse.ptp.services.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
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

public class ServiceConfigurationImportWizard extends Wizard implements IImportWizard {
	private class SelectFilePage extends WizardPage {

		private String file = ""; //$NON-NLS-1$
		private Combo fileCombo;
		private Button browseButton;
		private ServiceConfigurationSelectionWidget fServiceWidget;
		private int messageType = NONE;

		public SelectFilePage(String pageName, String title, ImageDescriptor titleImage) {
			super(pageName, title, titleImage);
			setDescription(Messages.ServiceConfigurationImportWizard_1); 
		}

		public void createControl(Composite parent) {
			Composite workArea = new Composite(parent, SWT.NONE);
			setControl(workArea);

	        workArea.setFont(parent.getFont());
	        workArea.setLayout(new GridLayout());
			workArea.setLayoutData(new GridData(GridData.FILL_BOTH
					| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

			createFileSelectionArea(workArea);
			createConfigurationsArea(workArea);
			
			updateEnablement();
			Dialog.applyDialogFont(parent);
			messageType = ERROR;
		}
		
		private void createFileSelectionArea(Composite workArea) {
			Composite composite = new Composite(workArea, SWT.NULL);
	        composite.setLayout(new GridLayout());
	        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			composite.setLayout(layout);
			
			Label label = new Label(composite, SWT.NONE);
			label.setText(Messages.ServiceConfigurationImportWizard_2);

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
			browseButton.setText(Messages.ServiceConfigurationImportWizard_3); 
			GridData data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
			data.widthHint = Math.max(widthHint, browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
			browseButton.setLayoutData(data);
			browseButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					FileDialog d = new FileDialog(getShell(), SWT.OPEN);
					d.setFilterExtensions(new String[] {"*.cfg", "*"}); //$NON-NLS-1$ //$NON-NLS-2$
					d.setFilterNames(new String[] {Messages.ServiceConfigurationImportWizard_4, Messages.ServiceConfigurationImportWizard_5});
					String fileName= getFileName();
					if (fileName != null && fileName.length() > 0) {
						int separator = fileName.lastIndexOf(System.getProperty("file.separator").charAt(0)); //$NON-NLS-1$
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
						updateConfigurations(file);
					}
				}
			});
		}
		
		private void updateConfigurations(String file) {
			try {
				final String filename = file;
				getContainer().run(true, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(Messages.ServiceConfigurationImportWizard_0, 100);
						fServiceConfigurations = fModelManager.importConfigurations(filename);
						monitor.worked(100);
						monitor.done();
					}
				});
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
				// Do nothing
			}
			
			fServiceWidget.setConfigurations(fServiceConfigurations);
			fServiceWidget.setAllChecked(true);
		}
		
		private void createConfigurationsArea(Composite workArea) {
			Composite composite = new Composite(workArea, SWT.NONE);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
			
			Label label = new Label(composite, SWT.NONE);
			label.setText(Messages.ServiceConfigurationImportWizard_12);
			
			fServiceWidget = new ServiceConfigurationSelectionWidget(composite, SWT.CHECK, null, null, true);
			fServiceWidget.setConfigurations(new IServiceConfiguration[0]);
		}
		
		public String getFileName() {
			return file;
		}
		
		public IServiceConfiguration[] getServiceConfigurations() {
			return fServiceWidget.getCheckedServiceConfigurations();
		}
		
		private void updateEnablement() {
			boolean complete = false;
			setMessage(null);
			
			if (file.length() == 0) {
				setMessage(Messages.ServiceConfigurationImportWizard_6, messageType);
				setPageComplete(false);
				return;
			} else {
				// See if the file exists
				File f = new File(file);
				if (!f.exists()) {
					setMessage(Messages.ServiceConfigurationImportWizard_7, messageType); 
					setPageComplete(false);
					return;
				} else if (f.isDirectory()) {
					setMessage(Messages.ServiceConfigurationImportWizard_8, messageType); 
					setPageComplete(false);
					return;
				} else if (!fModelManager.isValidConfigurationFile(file)) {
					setMessage(Messages.ServiceConfigurationImportWizard_13, messageType);
					setPageComplete(false);
					return;
				}
				complete = true;
			}
			
			if (complete) {
				setErrorMessage(null);
				setDescription(Messages.ServiceConfigurationImportWizard_9);
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
	
	private SelectFilePage mainPage;
	private IServiceConfiguration[] fServiceConfigurations;
	private IServiceModelManager fModelManager = ServiceModelManager.getInstance();
	
	public ServiceConfigurationImportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.ServiceConfigurationImportWizard_10); 
	}
	
	public void addPages() {
		mainPage = new SelectFilePage("importMainPage", Messages.ServiceConfigurationImportWizard_11, null); //$NON-NLS-1$ 
		addPage(mainPage);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		for (IServiceConfiguration config : mainPage.getServiceConfigurations()) {
			fModelManager.addConfiguration(config);
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
