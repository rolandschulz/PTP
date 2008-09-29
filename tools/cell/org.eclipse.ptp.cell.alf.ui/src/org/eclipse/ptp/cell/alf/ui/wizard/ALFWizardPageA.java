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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.cell.alf.ui.Messages;
import org.eclipse.ptp.cell.alf.ui.core.ALFConstants;
import org.eclipse.ptp.cell.alf.ui.debug.Debug;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * This class implements the first page in the ALF creation wizard
 * 
 * @author Sean Curry
 * @since 3.0.0
 */
public class ALFWizardPageA extends WizardPage {

	private static int MIN_STACK_SIZE = 10;
	private static int MAX_STACK_SIZE = 251904;

	private Text expectedStackSize;
	private Combo expectedNumAccelerators;
	private Combo partitionMethod;
	private Text partitionMethodHelpText;
	private Combo configuration;

	protected ALFWizardPageA(String pageName, String title, String description) {
		super(pageName);
		setTitle(title);
		setDescription(description);
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Font font = parent.getFont();
		
		// create the composite to hold this wizard page's widgets
		Composite composite = new Composite(parent, SWT.NONE);
		
		//create desired layout for this wizard page
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
 		composite.setLayout(layout);
 		composite.setFont(font);
 		
 		/* create each widget and its layout data */
 		
 		// create the expected stack size widgets
 		Composite stackSizeComposite = new Composite(composite, SWT.NONE);
 		GridLayout layout2 = new GridLayout();
 		layout2.numColumns = 2;
 		stackSizeComposite.setLayout(layout2);
 		stackSizeComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
 		stackSizeComposite.setFont(font);
 		
 		Label stackSizeLabel = new Label(stackSizeComposite, SWT.WRAP | SWT.LEFT);
 		stackSizeLabel.setText(Messages.ALFWizardPageA_stackSizeLabelMessage);  	
 		stackSizeLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
 		stackSizeLabel.setFont(font);
 		
 		expectedStackSize = new Text(stackSizeComposite, SWT.SINGLE | SWT.BORDER);
 		expectedStackSize.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
 		expectedStackSize.setText("10");  //$NON-NLS-1$
 		expectedStackSize.setFont(font);
 		expectedStackSize.addModifyListener(new ModifyListener(){
 			public void modifyText(ModifyEvent e) {
 				try { 
				validateInput();
				getWizard().getContainer().updateButtons();
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
 		});

 		// create the expected number of accelerators widgets 
 		Composite numAcceleratorsComposite = new Composite(composite, SWT.NONE);
 		numAcceleratorsComposite.setLayout(layout2);
 		numAcceleratorsComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
 		numAcceleratorsComposite.setFont(font);
 		
 		Label numAcceleratorsLabel = new Label(numAcceleratorsComposite, SWT.WRAP | SWT.LEFT);
 		numAcceleratorsLabel.setText(Messages.ALFWizardPageA_numAcceleratorsLabelMessage);
 		numAcceleratorsLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
 		numAcceleratorsLabel.setFont(font);
 		
 		expectedNumAccelerators = new Combo(numAcceleratorsComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
 		expectedNumAccelerators.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL)); 	
 		expectedNumAccelerators.setFont(font);
 		expectedNumAccelerators.add(0 + " " + Messages.ALFWizardPageA_allAvailableAccelerators, 0); //$NON-NLS-1$
 		for(int i = 1; i <= 16; ++i){
 			expectedNumAccelerators.add(i + "", i); //$NON-NLS-1$
 		}
 		expectedNumAccelerators.select(0);
 		expectedNumAccelerators.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) { /* do nothing */ }

			public void widgetSelected(SelectionEvent e) {
				try {
					ALFWizardPageB pageB = (ALFWizardPageB) getNextPage();
					if(pageB != null){
						pageB.updateAndValidateBuffers();
					}
					getWizard().getContainer().updateButtons();
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
 			
 		});
 		
        // create the partition method widgets
 		Composite partitionMethodLabelComposite = new Composite(composite, SWT.NONE);
 		GridLayout layout3 = new GridLayout();
 		layout3.numColumns = 3;
		partitionMethodLabelComposite.setLayout(layout3);
		partitionMethodLabelComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		partitionMethodLabelComposite.setFont(font);
 		
 		Label partitionMethodLabel = new Label(partitionMethodLabelComposite, SWT.WRAP | SWT.LEFT);
 		partitionMethodLabel.setText(Messages.ALFWizardPageA_partitionMethodLabelMessage);
 		partitionMethodLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
 		partitionMethodLabel.setFont(font);
 		
 		partitionMethodHelpText = ALFWizard.createWhatsThisHelpText(partitionMethodLabelComposite, getShell(), getWizard(), "data/partition_method_help.html", 325, 275); //$NON-NLS-1$
 		partitionMethodHelpText.getFont();
 		
 		partitionMethod = new Combo(partitionMethodLabelComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
 		partitionMethod.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL)); 		
 		partitionMethod.setFont(font);
 		partitionMethod.add(Messages.ALFWizardPageA_partitionMethodHost, ALFConstants.ALF_PARTITION_HOST);
 		partitionMethod.add(Messages.ALFWizardPageA_partitionMethodAccelerator, ALFConstants.ALF_PARTITION_ACCELERATOR);
 		partitionMethod.select(ALFConstants.ALF_PARTITION_HOST);
 		
 		// create the configuration selection widgets
 		Composite configurationComposite = new Composite(composite, SWT.NONE);
 		configurationComposite.setLayout(layout2);
 		configurationComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
 		configurationComposite.setFont(font);
 		
 		Label configurationLabel = new Label(configurationComposite, SWT.WRAP | SWT.LEFT);
 		configurationLabel.setText(Messages.ALFWizardPageA_configurationLabelMessage);
 		configurationLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
 		configurationLabel.setFont(font);
 		
 		configuration = new Combo(configurationComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
 		configuration.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL));
 		configuration.setFont(font);
 		configuration.add(Messages.ALFWizardPageA_configuration32bit, ALFConstants.CONFIG_GNU_32);
 		configuration.add(Messages.ALFWizardPageA_configuration64bit, ALFConstants.CONFIG_GNU_64);
 		configuration.select(ALFConstants.CONFIG_GNU_32); 		
 		
		setControl(composite);
	}
	
	/*
	public void dispose(){		
		expectedStackSize.dispose();
		expectedNumAccelerators.dispose();
		partitionMethod.dispose();
		super.dispose();
	}
	*/

	/**
	 * Returns the expected number of accelerator nodes that the user has chosen. 
	 * 
	 * @return the expected number of accelerator nodes
	 */
	public int getExpAccelNum(){
		if(expectedNumAccelerators != null && !expectedNumAccelerators.isDisposed())
			return expectedNumAccelerators.getSelectionIndex();
		else
			return -1;
	}
	
	public int getExpStackSize(){
		if(expectedStackSize != null && !expectedStackSize.isDisposed())
			return Integer.parseInt(expectedStackSize.getText().trim());
		else
			return -1;
	}
	
	public int getPartitionMethod(){
		if(partitionMethod != null && !partitionMethod.isDisposed())
			return partitionMethod.getSelectionIndex();
		else
			return -1;
	}
	
	public boolean is64bit(){
		if(configuration == null)
			return false;
		else {
			if(configuration.getSelectionIndex() == ALFConstants.CONFIG_GNU_64)
				return true;
			else
				return false;
		}
	}
	
	private void validateInput(){
		try{
			if(expectedStackSize != null && !expectedStackSize.isDisposed()){
				if((Long.parseLong(expectedStackSize.getText()) < MIN_STACK_SIZE) || (Long.parseLong(expectedStackSize.getText()) > MAX_STACK_SIZE)){
					setErrorMessage(Messages.ALFWizardPageA_errorMsgExpectedStackSize);
					setPageComplete(false);
					return;
				}
			}
		} catch(NumberFormatException e){
			setErrorMessage(Messages.ALFWizardPageA_errorMsgExpectedStackSize);
			setPageComplete(false);
			return;
		}
		setPageComplete(true);
		setErrorMessage(null);
	}
}
