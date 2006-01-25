/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.ptp.rtsystem.simulation.SimulationControlSystem;
import org.eclipse.ptp.rtsystem.simulation.SimulationMonitoringSystem;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SimulationPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, PreferenceConstants 
{
	public static final String EMPTY_STRING = "";
	
	protected Spinner spin = null;
	protected Text nodeText = null;
	protected Combo mcombo = null;
	protected Label nodeLabel = null;
	protected int nodes[];

	public SimulationPreferencesPage() {
		setPreferenceStore(PTPCorePlugin.getDefault().getPreferenceStore());
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener
	{
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if(source == mcombo) {
				setupMachineRegion();
			}
			updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(source == spin) {
				setupMachineRegion();
			}
			else if(source == nodeText) {
				int mac = mcombo.getSelectionIndex();
				String sval = nodeText.getText();
				int val = 1;
				try {
					val = (new Integer(sval)).intValue();
				} catch (NumberFormatException e) {
				}
				nodes[mac] = val;
			}
			updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	protected Control createContents(Composite parent) 
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		createMyContents(composite);
		createSimulationProject(composite);

		loadSaved();
		defaultSetting();
		return composite;
	}
	
	private void createSimulationProject(Composite parent) {
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(3, false, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(CoreMessages.getResourceString("SimulationPreferencesPage.group_simulation"));
		
		new Label(aGroup, SWT.NONE).setText(CoreMessages.getResourceString("SimulationPreferencesPage.createSimulationProject_combo"));
		final Combo projectCombo = new Combo(aGroup, SWT.READ_ONLY);
		//FIXME hard code for c project 
		projectCombo.add("C");
		
		Button createSimulationButton = new Button(aGroup, SWT.PUSH);
		createSimulationButton.setText(CoreMessages.getResourceString("SimulationPreferencesPage.createSimulationProject_button"));
		createSimulationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (projectCombo.getSelectionIndex() > -1) {
					//FIXME Should be changed to call plugin configuration
					SimulationProjectCreation simulationProject = new ManagedCProjectCreation(SIMULATION_PROJECT_NAME, SIMULATION_FILE_NAME);
					try {
						simulationProject.createSimulatorProject();
					} catch (CoreException e) {
						ErrorDialog.openError(getShell(), "Error found", "Cannot create project", e.getStatus());					
					}
				}
			}
		});
	}

	private void createMyContents(Composite parent)
	{
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(CoreMessages.getResourceString("SimulationPreferencesPage.group_main"));
		
		Label ortedComment = new Label(aGroup, SWT.WRAP);
		ortedComment.setText("Number of simulated machines:");
		
		spin = new Spinner(aGroup, SWT.READ_ONLY);
		spin.setMinimum(1);
		spin.addModifyListener(listener);
		
		Group bGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		bGroup.setLayout(createGridLayout(1, true, 10, 10));
		bGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL, 2));
		bGroup.setText(CoreMessages.getResourceString("SimulationPreferencesPage.group_machines"));
		
		new Label(bGroup, SWT.NONE).setText("Select a machine:");
		
		mcombo = new Combo(bGroup, SWT.READ_ONLY);
		mcombo.addSelectionListener(listener);
		
		nodeLabel = new Label(bGroup, SWT.NONE);
		nodeLabel.setText("Number of nodes for machine #0:");
		nodeLabel.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		
		nodeText = new Text(bGroup, SWT.BORDER | SWT.SINGLE);
		nodeText.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		/* only integers allowed */
		nodeText.addVerifyListener(new VerifyListener() {
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
		});
		nodeText.addModifyListener(listener);
		
		setupMachineRegion();
		mcombo.select(0);
	}
	
	protected void setupMachineRegion()
	{		
		int choice = spin.getSelection();

		int sel = mcombo.getSelectionIndex();
		
		/* we're changing the size of the nodes now, so we need to save a backup and go
		 * back through the array and fill the entries back in as the array resizes
		 */		
		if(nodes != null) {
			int l = nodes.length;
			int[] nodesold = new int[l];
			for(int i=0; i<l; i++)
				nodesold[i] = nodes[i];
			nodes = new int[choice];
			for(int i=0; i<choice; i++) {
				if(i < l) nodes[i] = nodesold[i];
				else nodes[i] = 1;
			}
		}
		
		mcombo.removeAll();
		
		for(int i=0; i< choice; i++) {
			mcombo.add("Machine "+i);
		}
		
		/* put the user back on their selection */
		if(sel < choice && sel >= 0)
			mcombo.select(sel);
		/* unless they deleted it (were at last and removed it) in which case, go back to 0 */
		else if(sel == choice && sel != 0) {
			mcombo.select(sel-1);
		}
		else
			mcombo.select(0);
		mcombo.pack();
		
		sel = mcombo.getSelectionIndex();
		nodeLabel.setText("Number of nodes for machine #"+(sel)+":");
		//nodeLabel.pack();
		if(nodes != null)
			nodeText.setText(""+nodes[sel]+"");
	}

	protected void defaultSetting() 
	{
	}
	
	private void loadSaved()
	{
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		
		int numMachines = preferences.getInt(PreferenceConstants.SIMULATION_NUM_MACHINES);
		if(numMachines < 1) numMachines = 1;
		spin.setSelection(numMachines);
		
		nodes = new int[numMachines];
		
		for(int i=0; i<numMachines; i++) {
			/* look for a #nodes for each machine */
			int nnodes = preferences.getInt(PreferenceConstants.SIMULATION_MACHINE_NODE_PREFIX + ""+i+"");
			if(nnodes < 1) nnodes = 1;
			nodes[i] = nnodes;
		}
		nodeText.setText(""+nodes[0]+"");
	}

	public void init(IWorkbench workbench) 
	{
	}

	public void dispose() 
	{
		super.dispose();
	}

	public void performDefaults() 
	{
		defaultSetting();
		updateApplyButton();
	}

	public boolean performOk() 
	{
		int nmacs = spin.getSelection();
		
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();		
		preferences.setValue(PreferenceConstants.SIMULATION_NUM_MACHINES, nmacs);
		for(int i=0; i<nmacs; i++) {
			preferences.setValue(PreferenceConstants.SIMULATION_MACHINE_NODE_PREFIX + ""+i+"", nodes[i]);
		}
		PTPCorePlugin.getDefault().savePluginPreferences();

		IModelManager manager = PTPCorePlugin.getDefault().getModelManager();
		if (manager.getMonitoringSystem() instanceof SimulationMonitoringSystem && manager.getControlSystem() instanceof SimulationControlSystem) {
			PTPUIPlugin.getDefault().refreshRuntimeSystem(true, true);	
		}
		return true;
	}
	
	protected void updatePreferencePage() 
	{
		setErrorMessage(null);
		setMessage(null);

		setValid(true);
	}

	protected String getFieldContent(String text) 
	{
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;

		return text;
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) 
	{
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	protected GridData spanGridData(int style, int space) 
	{
		GridData gd = null;
		if (style == -1)
			gd = new GridData();
		else
			gd = new GridData(style);
		gd.horizontalSpan = space;
		return gd;
	}
}
