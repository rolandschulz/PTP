/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.alf.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ptp.cell.alf.ui.Activator;
import org.eclipse.ptp.cell.alf.ui.Messages;
import org.eclipse.ptp.cell.alf.ui.core.ALFWizardCreationAction;
import org.eclipse.ptp.cell.alf.ui.core.HelpDocumentRuntimeException;
import org.eclipse.ptp.cell.alf.ui.debug.Debug;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;


/**
 * This class implements the ALF project creation wizard
 * 
 * @author Sean Curry
 * @since 3.0.0
 */
public class ALFWizard extends Wizard implements INewWizard, IExecutableExtension {
	
	protected IConfigurationElement wizardConfigElement;
	
	/**
	 * This simple class extends DirectoryDialog, so I can call open(), and 
	 * get the resulting selection after the dialog has closed, since the dialog will be opened
	 * in a Runnable.
	 * 
	 * @author spcurry
	 * 
	 */
	class MyDirectoryDialog extends DirectoryDialog {
		String selectedDir;
		
		public MyDirectoryDialog(Shell parent) { 
			super(parent);
			selectedDir = null;
		}
		
		public void openDialog(){ selectedDir = this.open(); }
		public String getResult(){ return selectedDir; }
	}
	
	public void addPages(){		
		ALFWizardNewProjectCreationPage projectNamePage = new ALFWizardNewProjectCreationPage(Messages.ALFWizard_projectNamePageName);
		projectNamePage.setTitle(Messages.ALFWizard_projectNamePageTitle);
		projectNamePage.setDescription(Messages.ALFWizard_projectNamePageDescription);
		addPage(projectNamePage);
		
		ALFWizardPageA pageA = new ALFWizardPageA(Messages.ALFWizardPageA_pageName, Messages.ALFWizardPageA_pageTitle, Messages.ALFWizardPageA_pageDescription);
		addPage(pageA);
		
		ALFWizardPageB pageB = new ALFWizardPageB(Messages.ALFWizardPageB_pageName, Messages.ALFWizardPageB_pageTitle, Messages.ALFWizardPageB_pageDescription);
		addPage(pageB);
	}
	
	public boolean canFinish(){
		IWizardPage namePage = getPage(Messages.ALFWizard_projectNamePageName);
		IWizardPage pageA = getPage(Messages.ALFWizardPageA_pageName);

		if((namePage == null) || (pageA == null))
			return false;
		else
			return (namePage.isPageComplete() && pageA.isPageComplete());
	}
	
	/**
	 * This method creates a Text widget with the text "What's this?". This Text has a font which is smaller than the parent font, is the color blue,
	 * and is in italics. The "What's this?" Text is to be used to provide a UI element for the user to find out information about some parameter of the ALF wizard.
	 * 
	 * @param parent the parent composite
	 * @return a Text widget with special font and MouseListener which is to be used in providing UI help to the end user
	 */
	public static Text createWhatsThisHelpText(Composite parent, final Shell shell, final IWizard wizard, final String urlPath, final int width, final int height){
		Text helpText = new Text(parent, SWT.READ_ONLY | SWT.WRAP | SWT.SINGLE);
		helpText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		
		Font parentFont = parent.getFont();
		FontData[] parentData = parentFont.getFontData();
		int h= 7;
		if((parentData.length == 1) && (parentData[0].height > 0))
			h = parentData[0].getHeight() - 3;		
		Font font = new Font(parentFont.getDevice(), "MyFont", h, SWT.ITALIC); //$NON-NLS-1$		
		helpText.setFont(font);
		helpText.setForeground(new Color(parentFont.getDevice(), 0, 0, 255));
		helpText.setBackground(helpText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		helpText.setText(Messages.ALFWizard_whatsThisText);
		
		helpText.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent e) { /* do nothing */ }
			public void mouseUp(MouseEvent e) { /* do nothing */ }			
			public void mouseDown(MouseEvent e) {
				try{
					ApplicationWindow helpAppWindow = new ApplicationWindow(shell){
						protected Control createContents(Composite parent){
							Browser helpAppBrowser = new Browser(parent, SWT.NONE);

							URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(urlPath), null);
							String temp = null;
							try {
								if(url == null)
									throw new IOException();
								temp = FileLocator.toFileURL(url).toString();
								helpAppBrowser.setUrl(temp);

							} catch (IOException e) {
								throw new HelpDocumentRuntimeException();
							}
							return helpAppBrowser;
						}
					};
					helpAppWindow.create();
					helpAppWindow.getShell().setMinimumSize(width, height);
					helpAppWindow.setBlockOnOpen(true);
					helpAppWindow.open();
				} catch (HelpDocumentRuntimeException exception){
					ALFWizard alfWizard = (ALFWizard) wizard;
					alfWizard.logErrorMessage(Messages.ALFWizard_errorHelpDocTitle, Messages.ALFWizard_errorHelpDocMessage);
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
 		});
 		//helpText.addTraverseListener(new TraverseListener(){
			//public void keyTraversed(TraverseEvent e) {
				//if(e.detail == SWT.TRAVERSE_TAB_NEXT){
					//System.out.println("HELLO TEST");
				//}
			//}
 		//});
		
		return helpText;
	}
	
	public IWizard getWizard(){ return this; }
	
	public boolean performFinish() {
		if(!canFinish()){
			logErrorMessage(Messages.ALFWizard_projectNamePageTitle, Messages.ALFWizard_errorCannotFinishMessage);
			return false;
		}
		
		WizardNewProjectCreationPage namePage = (WizardNewProjectCreationPage) getPage(Messages.ALFWizard_projectNamePageName);
		ALFWizardPageA pageA = (ALFWizardPageA) getPage(Messages.ALFWizardPageA_pageName);
		ALFWizardPageB pageB = (ALFWizardPageB) getPage(Messages.ALFWizardPageB_pageName);
		if((namePage == null) || (pageA == null) || (pageB == null))
			return false;
		
		// Update the validity of the buffers, just in case something has changed
		pageB.updateAndValidateBuffers();
		
		// Check if any buffers have been created. If none have been created, present the user with a message stating that no buffers
		// currently exist, and ask if they would like to continue anyway.
		ArrayList buffers = pageB.getBuffers();
		if(buffers.size() == 0){
			boolean question = queryYesNoQuestion(Messages.ALFWizard_projectNamePageTitle, Messages.ALFWizard_warningNoBuffersExist);
			if(!question)
				return false;
		}
		
		// Check if any of the buffers are invalid. If invalid buffers exist, present the user with a message telling them of the invalid 
		// buffers, and ask if they would like to continue anyway (with unexpected results).
		if(!pageB.isPageComplete()){
			boolean question = queryYesNoQuestion(Messages.ALFWizard_projectNamePageTitle, Messages.ALFWizard_warningBufferErrorsExist);
			if(!question)
				return false;
		}
		
		// Check if the number of DT entries for all of the buffers are equal. If they are not equal, tell the user of this, and ask 
		// if they would like to continue anyway. 
		if(!pageB.updateNumDTEntries()){
			boolean question = queryYesNoQuestion(Messages.ALFWizard_projectNamePageTitle, Messages.ALFWizard_warningUnequalNumDT);
			if(!question)
				return false;
		}
		
		if(!pageB.updateLocalMemorySize()){
			boolean question = queryYesNoQuestion(Messages.ALFWizard_projectNamePageTitle, Messages.ALFWizard_warningNoRemainingMemory);
			if(!question)
				return false;
		}
		
		ProgressMonitorDialog monitor = new ProgressMonitorDialog(getContainer().getShell());
		
		ALFWizardCreationAction action = new ALFWizardCreationAction(this, namePage.getLocationPath(), namePage.getProjectName(), pageA.getExpStackSize(), pageA.getExpAccelNum(),
																	 pageA.getPartitionMethod(), pageB.getBuffers(), pageA.is64bit());

		try{
			monitor.run(true, true, action);
			BasicNewProjectResourceWizard.updatePerspective(wizardConfigElement);
		} catch (InvocationTargetException e){
			if(e.getMessage() == null){
				MessageDialog.openError(getContainer().getShell(), Messages.ALFWizard_errorWizardFinish, Messages.ALFWizard_errorUnknown);
				getContainer().updateButtons();
				return false;
			}
			MessageDialog.openError(getContainer().getShell(), Messages.ALFWizard_errorWizardFinish, e.getMessage());
			getContainer().updateButtons();
			return false;
		} catch (InterruptedException e){
			MessageDialog.openInformation(getContainer().getShell(), Messages.ALFWizard_projectNamePageTitle, Messages.ALFWizard_errorWizardCanceled);
			getContainer().updateButtons();
			return false;
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {		
		setWindowTitle(Messages.ALFWizard_title);
		setNeedsProgressMonitor(true);
	}
	
	public String queryPathLocation(String title, String message, String defaultValue) {
		final InputDialog dialog = new InputDialog(getShell(), title, message, defaultValue, new IInputValidator(){
			public String isValid(String newText) {
				File file = new File(newText);
				if(!file.exists())
					return Messages.ALFWizard_errorDirDoesNotExist;
				else
					return null;
			}			
		});
		
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.setBlockOnOpen(true);
				dialog.open();
			}
		});
		
		if(dialog.getReturnCode() == Window.CANCEL)
			return null;
		else
			return dialog.getValue();
	}
	
	/**
	 * Opens a MessageDialog window which presents the user with a message, and asks for them to choose "Yes" or "No".
	 * 
	 * @param title the title of the MessageDialog question window
	 * @param Message the message of the MessageDialog question window
	 * @return <code>true</code> if the user selects "Yes", else <code>false</code>
	 */
	public boolean queryYesNoQuestion(String title, String message){
		final MessageDialog dialog = new MessageDialog(getShell(), title, null, message, MessageDialog.QUESTION, 
				new String[]{Messages.ALFWizard_yes, Messages.ALFWizard_no}, 0);

		// Run in syncExec because callback is from an operation,
        // which is probably not running in the UI thread.
        getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                dialog.open();
            }
        });

        int returnCode = dialog.getReturnCode();
        if(returnCode == 0)
        	return true;
        else
        	return false;
	}
	
	public void logWarningMessage(String title, String message){
		Debug.POLICY.logStatus(new Status(IStatus.WARNING, Activator.getDefault().getBundle().getSymbolicName(), 0, message, null));
	}

	public void openWarningMessage(String title, String message){
		final MessageDialog dialog = new MessageDialog(getShell(), title, null, message, MessageDialog.WARNING, 
				new String[]{Messages.ALFWizard_ok}, 0);		
		// Run in syncExec because callback is from an operation,
        // which is probably not running in the UI thread.
        getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                dialog.open();
            }
        });
	}
	
	public void logErrorMessage(String title, String message){
		Debug.POLICY.logStatus(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), 0, message, null));
	}

	public void openErrorMessage(String title, String message){
		final MessageDialog dialog = new MessageDialog(getShell(), title, null, message, MessageDialog.ERROR, 
				new String[]{Messages.ALFWizard_ok}, 0);	
		// Run in syncExec because callback is from an operation,
        // which is probably not running in the UI thread.
        getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                dialog.open();
            }
        });
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.wizardConfigElement = config;
	}
}
