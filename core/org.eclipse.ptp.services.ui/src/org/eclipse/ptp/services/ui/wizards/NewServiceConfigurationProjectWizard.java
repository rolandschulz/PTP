/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.services.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * A wizard for creating a new service configuration by stepping through service provider
 * configuration wizard pages.
 * 
 * NOT CURRENTLY USED AND MAY BE DEPRECATED
 */
public class NewServiceConfigurationProjectWizard extends Wizard implements INewWizard {

	private class IntroPage extends WizardPage {

		public IntroPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(Messages.ServiceConfigurationWizard_2);
		}

		public void createControl(Composite parent) {
			Composite canvas = new Composite(parent, SWT.NONE);
			GridLayout canvasLayout = new GridLayout(1, false);
			canvas.setLayout(canvasLayout);
			
			Label label = new Label(canvas, SWT.NONE);
			label.setText(Messages.ServiceConfigurationWizard_3);
			
			Label label2 = new Label(canvas, SWT.NONE);
			label2.setText(Messages.ServiceConfigurationWizard_4);
			
			Button button = new Button(canvas, SWT.CHECK);
			button.setText(Messages.ServiceConfigurationWizard_5);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			button.setLayoutData(data);
			
			setControl(canvas);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
		 */
		@Override
		public boolean isPageComplete() {
			return true;
		}
		
	}
	
	private IWorkbench fWorkbench = null;
	private IStructuredSelection fSelection = null;
	
	public NewServiceConfigurationProjectWizard() {
		setForcePreviousAndNextButtons(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(new IntroPage(Messages.ServiceConfigurationWizard_6));
		addPage(new ServiceConfigurationSelectionWizardPage(Messages.NewServiceConfigurationProjectWizard_0));
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fWorkbench = workbench;
		fSelection = selection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		return true;
	}
	
	/**
	 * @return the Selection
	 */
	public IStructuredSelection getSelection() {
		return fSelection;
	}

	/**
	 * @return the Workbench
	 */
	public IWorkbench getWorkbench() {
		return fWorkbench;
	}
}
