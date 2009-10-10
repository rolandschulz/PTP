package org.eclipse.ptp.services.ui.wizards;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.services.ui.messages.Messages;
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
		private int messageType = NONE;

		public SelectFilePage(String pageName, String title, ImageDescriptor titleImage) {
			super(pageName, title, titleImage);
			setDescription(Messages.ServiceConfigurationImportWizard_1); 
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
					FileDialog d = new FileDialog(getShell());
					d.setFilterExtensions(new String[] {"*.xml", "*"}); //$NON-NLS-1$ //$NON-NLS-2$
					d.setFilterNames(new String[] {Messages.ServiceConfigurationImportWizard_4, Messages.ServiceConfigurationImportWizard_5});
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
//				} else if (!ProjectSetImporter.isValidProjectSetFile(file)) {
//					setMessage("The specified file is not a valid service configuration file.", messageType);
//					setPageComplete(false);
//					return;
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
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		FilenameStore.setDefaultFromSelection(workbench);
	}

}
