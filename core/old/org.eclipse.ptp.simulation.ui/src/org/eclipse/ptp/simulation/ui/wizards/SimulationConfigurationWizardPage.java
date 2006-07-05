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
package org.eclipse.ptp.simulation.ui.wizards;

import org.eclipse.ptp.rmsystem.SimulationRMConfiguration;
import org.eclipse.ptp.simulation.internal.ui.Messages;
import org.eclipse.ptp.ui.wizards.ConfigurationWizard;
import org.eclipse.ptp.ui.wizards.ConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public final class SimulationConfigurationWizardPage extends
		ConfigurationWizardPage {
	
	private static final int TEXT_WIDTH = 50;
	private SimulationRMConfiguration config;
	private int whichMachine = 0;
	private Combo whichMachineCombo;
	private Text numNodesText;

	public SimulationConfigurationWizardPage(ConfigurationWizard wizard) {
		super(wizard, Messages.getString("SimulationConfigurationWizardPage.name")); //$NON-NLS-1$
		setTitle(Messages.getString("SimulationConfigurationWizardPage.title")); //$NON-NLS-1$
		setDescription(Messages.getString("SimulationConfigurationWizardPage.description")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		final ConfigurationWizard confWizard = getConfigurationWizard();
		config = (SimulationRMConfiguration) confWizard.getConfiguration();
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.getString("SimulationConfigurationWizardPage.NumberOfMachines")); //$NON-NLS-1$

		final VerifyListener intTextVerifyListener = new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				String text = e.text;
				char[] chars = new char[text.length()];
				text.getChars(0, chars.length, chars, 0);
				for(int i=0; i<chars.length; i++) {
					if(!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		};
				
		final Text numMachinesText = new Text(container, SWT.BORDER | SWT.SINGLE);
		numMachinesText.setLayoutData(new GridData(TEXT_WIDTH, SWT.DEFAULT));
		/* only integers allowed */
		numMachinesText.addVerifyListener(intTextVerifyListener);
		numMachinesText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				setNumMachines(numMachinesText.getText());
				validate();
			}});

		final Label whichMachineLabel = new Label(container, SWT.NONE);
		whichMachineLabel.setText(Messages.getString("SimulationConfigurationWizardPage.SetNumberOfNodesWhichMachine")); //$NON-NLS-1$

		whichMachineCombo = new Combo(container, SWT.READ_ONLY);
		whichMachineCombo.setLayoutData(new GridData(TEXT_WIDTH, SWT.DEFAULT));
		whichMachineCombo.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
				// no-op
			}
			public void widgetSelected(SelectionEvent e) {
				setWhichMachine();
			}});
		
		numNodesText = new Text(container, SWT.BORDER | SWT.SINGLE);
		numNodesText.setLayoutData(new GridData(TEXT_WIDTH, SWT.DEFAULT));
		/* only integers allowed */
		numNodesText.addVerifyListener(intTextVerifyListener);
		numNodesText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				setNumNodes(numNodesText.getText());
				validate();
			}});

		setControl(container);
		// don't enabe these until we have some number of machines
		whichMachineCombo.setEnabled(false);
		numNodesText.setEnabled(false);
		setPageComplete(false);
	}

	private String[] getStringNumbers(int numMachines) {
		String[] strs = new String[numMachines];
		for (int i=0; i<numMachines; ++i) {
			strs[i] = Integer.toString(i);
		}
		return strs;
	}

	private void setNumMachines(String nmStr) {

		try {
			int value = Integer.parseInt(nmStr);
			if (value >= 0) {
				config.setNumMachines(value);
			}
			else {
				config.setNumMachines(-1);
			}
		}
		catch (NumberFormatException exc) {
			config.setNumMachines(-1);
		}

		final int numMachines = config.getNumMachines();
		final boolean enabled = numMachines > 0;
		whichMachineCombo.setItems(new String[0]);
		whichMachineCombo.setEnabled(enabled);
		numNodesText.setEnabled(enabled);
		
		if (enabled) {
			whichMachineCombo.setItems(getStringNumbers(numMachines));
			whichMachine = 0;
			whichMachineCombo.select(whichMachine);
			numNodesText.setText(Integer.toString(config.getNumNodesPerMachine(whichMachine)));
		}
	}

	private void setNumNodes(String nmStr) {
		try {
			int value = Integer.parseInt(nmStr);
			if (value >= 0) {
				config.setNumNodesPerMachine(whichMachine, value);
			}
			else {
				config.setNumNodesPerMachine(whichMachine, -1);
			}
		}
		catch (NumberFormatException exc) {
			config.setNumNodesPerMachine(whichMachine, -1);
		}
	}

	private void setWhichMachine() {
		whichMachine = whichMachineCombo.getSelectionIndex();
		numNodesText.setText(Integer.toString(config.getNumNodesPerMachine(whichMachine)));
	}

	private void validate() {
		boolean isValid = true;
		isValid = isValid && config.getNumMachines() >= 0;
		for (int i = 0, n = config.getNumMachines(); i < n; ++i) {
			int numNodes = config.getNumNodesPerMachine(i);
			isValid = isValid && numNodes >= 0;
		}
		setPageComplete(isValid);
	}

}
