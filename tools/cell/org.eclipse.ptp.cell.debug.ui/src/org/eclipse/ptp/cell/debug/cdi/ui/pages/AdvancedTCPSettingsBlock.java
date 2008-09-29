/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.cdi.ui.pages;

import java.util.Observable;

import org.eclipse.cdt.debug.mi.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.ComboDialogField;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.StringDialogField;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.cell.debug.CellDebugPlugin;
import org.eclipse.ptp.cell.debug.launch.CellDebugRemoteDebugger;
import org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class AdvancedTCPSettingsBlock extends Observable {

	private final static String DEFAULT_PORT_NUMBER = "10000"; //$NON-NLS-1$
	
	private final static String DEFAULT_REMOTEDBGCONFIG_KEY = "Cell PPU gdbserver"; //$NON-NLS-1$
	
	private String[] fRemoteDBGconfigs;

	private Shell fShell;

	//private GDBServerLaunchSettingsBlock fGDBServerSettings;

	private StringDialogField fPortNumberField;
	
	private Button fFlag;
	
	private ComboDialogField fConfigCombo;
	
	private StringDialogField fGDBServerBinary;

	private Control fControl;

	private String fErrorMessage = null;
	
	private boolean init = true;

	public AdvancedTCPSettingsBlock() {
		super();
		CellDebugPlugin plugin = CellDebugPlugin.getDefault();
		fRemoteDBGconfigs = plugin.getRemoteDbgNames();
		fConfigCombo = createConfigCombo();
		fGDBServerBinary = createGDBServerBinary();
		fPortNumberField = createPortNumberField();
		
	}

	public void createBlock( Composite parent ) {
		fShell = parent.getShell();
		Composite comp = ControlFactory.createCompositeEx( parent, 2, GridData.FILL_BOTH );
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout)comp.getLayout()).marginHeight = 0;
		((GridLayout)comp.getLayout()).marginWidth = 0;
		comp.setFont( parent.getFont() );
		PixelConverter converter = new PixelConverter( comp );
		fPortNumberField.doFillIntoGrid( comp, 2 );
		((GridData)fPortNumberField.getTextControl( null ).getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		LayoutUtil.setWidthHint( fPortNumberField.getTextControl( null ), converter.convertWidthInCharsToPixels( 10 ) );
		fFlag = createFlag(comp);
		fConfigCombo.doFillIntoGrid( comp, 2 );
		((GridData)fConfigCombo.getComboControl(null).getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		LayoutUtil.setWidthHint( fConfigCombo.getComboControl( null ), converter.convertWidthInCharsToPixels( 30 ) );
		fGDBServerBinary.doFillIntoGrid( comp, 2 );
		((GridData)fGDBServerBinary.getTextControl( null ).getLayoutData()).horizontalAlignment = GridData.BEGINNING;
		LayoutUtil.setWidthHint( fGDBServerBinary.getTextControl( null ), converter.convertWidthInCharsToPixels( 20 ) );
		
		setControl( comp );
	}

	protected Shell getShell() {
		return fShell;
	}

	public void dispose() {
		deleteObservers();
	}

	public void initializeFrom( ILaunchConfiguration configuration ) {
		init = true;
		initializeFlag( configuration );
		initializeConfigCombo( configuration );
		flagChanged();
		initializePortNumber( configuration );
		init = false;
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
		//configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_HOST, DEFAULT_HOST_NAME );
		//configuration.setAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_PORT, DEFAULT_PORT_NUMBER );

		String remoteCfg;
		try {
			remoteCfg = configuration.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_CFG,DEFAULT_REMOTEDBGCONFIG_KEY);
		} catch (CoreException e) {
			remoteCfg = DEFAULT_REMOTEDBGCONFIG_KEY;
		}
		CellDebugRemoteDebugger dbgCfg = CellDebugPlugin.getDefault().getRemoteDbgConfigByName(remoteCfg);
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH, 1);
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_CFG,dbgCfg.getName());
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_BINARY, dbgCfg.getDebugConfig().getDbgBinaryName());
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_PORT, dbgCfg.getDebugConfig().getDbgPort());
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_DBGID, dbgCfg.getDebuggerId());
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		if ( fPortNumberField != null )
			configuration.setAttribute( ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_PORT, fPortNumberField.getText().trim() );
		CellDebugRemoteDebugger dbgCfg = CellDebugPlugin.getDefault().getRemoteDbgConfigByName(fRemoteDBGconfigs[fConfigCombo.getSelectionIndex()]);
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH, fFlag.getSelection() ? 1 : 0);
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_CFG,dbgCfg.getName());
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_BINARY, fGDBServerBinary.getText().trim());
		configuration.setAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_DBGID, dbgCfg.getDebuggerId());
	}
	
	private Button createFlag(Composite parent) {
		Button field = ControlFactory.createCheckBox( parent, PagesMessages.getString("AdvancedTCPSettingsBlock.0") ); //$NON-NLS-1$
		field.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent e ) {
				if (!init)
					flagChanged();
			}
		} );
		ControlFactory.createEmptySpace(parent);
		return field;
	}
	
	private ComboDialogField createConfigCombo() {
		ComboDialogField field = new ComboDialogField( SWT.DROP_DOWN | SWT.READ_ONLY );
		field.setLabelText( PagesMessages.getString("AdvancedTCPSettingsBlock.1") ); //$NON-NLS-1$
		field.setItems(fRemoteDBGconfigs);
		field.setDialogFieldListener( new IDialogFieldListener() {

			public void dialogFieldChanged( DialogField f ) {
				if (!init)
					configComboChanged();
			}
		} );
		return field;
	}
	
	private StringDialogField createGDBServerBinary() {
		StringDialogField field = new StringDialogField();
		field.setLabelText( PagesMessages.getString("AdvancedTCPSettingsBlock.2") ); //$NON-NLS-1$
		field.setDialogFieldListener( new IDialogFieldListener() {

			public void dialogFieldChanged( DialogField f ) {
				if (!init)
					gdbServerBinaryChanged();
			}
		} );
		return field;
	}

	private StringDialogField createPortNumberField() {
		StringDialogField field = new StringDialogField();
		field.setLabelText( PagesMessages.getString("AdvancedTCPSettingsBlock.4") ); //$NON-NLS-1$
		field.setDialogFieldListener( new IDialogFieldListener() {

			public void dialogFieldChanged( DialogField f ) {
				portNumberFieldChanged();
			}
		} );
		return field;
	}

	protected void portNumberFieldChanged() {
		updateErrorMessage();
		setChanged();
		notifyObservers();
	}
	
	protected void flagChanged() {
		if (!fFlag.getSelection()) {
			fConfigCombo.setEnabled(false);
			fGDBServerBinary.setEnabled(false);
		} else {
			fConfigCombo.setEnabled(true);
			fGDBServerBinary.setEnabled(true);
		}
		updateErrorMessage();
		setChanged();
		notifyObservers();
	}
	
	protected void configComboChanged() {
		int index = fConfigCombo.getSelectionIndex();
		if (index >= 0) {
			CellDebugRemoteDebugger dbgCfg = CellDebugPlugin.getDefault().getRemoteDbgConfigByName(fRemoteDBGconfigs[index]);
			fGDBServerBinary.setText(dbgCfg.getDebugConfig().getDbgBinaryName());
			updateErrorMessage();
			setChanged();
			notifyObservers();
		}
	}
	
	protected void gdbServerBinaryChanged() {
		updateErrorMessage();
		setChanged();
		notifyObservers();
	}

	private void initializePortNumber( ILaunchConfiguration configuration ) {
		if ( fPortNumberField != null ) {
			try {
				fPortNumberField.setText( configuration.getAttribute( ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_PORT, DEFAULT_PORT_NUMBER ) );
			}
			catch( CoreException e ) {
			}
		}
	}
	
	private void initializeFlag( ILaunchConfiguration configuration ) {
		
		try {
			fFlag.setSelection(configuration.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH, 1) == 1 ? true : false);
		} catch (CoreException e) {
			fFlag.setSelection(true);
		}
		
	}
	
	private void initializeConfigCombo( ILaunchConfiguration configuration ) {
		int i = 0;
		String current;
		try {
			current = configuration.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_CFG, DEFAULT_REMOTEDBGCONFIG_KEY);
		} catch (CoreException e1) {
			current = DEFAULT_REMOTEDBGCONFIG_KEY;
		}
		CellDebugRemoteDebugger dbgCfg = CellDebugPlugin.getDefault().getRemoteDbgConfigByName(current);
		String[] items = fConfigCombo.getItems();
		for (i=0; i < items.length; i++) {
			if (items[i].equals(current))
				break;
		}
		fConfigCombo.selectItem(i);
		try {
			fGDBServerBinary.setText(configuration.getAttribute(ICellDebugLaunchConstants.TARGET_REMOTELAUNCH_SELECTED_BINARY, dbgCfg.getDebugConfig().getDbgBinaryName()));
		} catch (CoreException e) {
			fGDBServerBinary.setText(dbgCfg.getDebugConfig().getDbgBinaryName());
		}
	}

	public Control getControl() {
		return fControl;
	}

	protected void setControl( Control control ) {
		fControl = control;
	}

	public boolean isValid( ILaunchConfiguration configuration ) {
		updateErrorMessage();
		return (getErrorMessage() == null);
		
	}

	private void updateErrorMessage() {
		setErrorMessage( null );
		if ( fPortNumberField != null ) {
			if ( fPortNumberField.getText().trim().length() == 0 )
				setErrorMessage( PagesMessages.getString("AdvancedTCPSettingsBlock.5") ); //$NON-NLS-1$
			else if ( !portNumberIsValid( fPortNumberField.getText().trim() ) )
				setErrorMessage( PagesMessages.getString("AdvancedTCPSettingsBlock.6") ); //$NON-NLS-1$
		}
		if (getErrorMessage() == null) {
			if (fFlag.getSelection()) {
				if (fGDBServerBinary.getText() != null) {
					if (!(fGDBServerBinary.getText().trim().length() == 0) ) {
						if (!gdbServerBinaryIsValid()) {
							setErrorMessage(PagesMessages.getString("AdvancedTCPSettingsBlock.3")); //$NON-NLS-1$
						}
					} else {
						setErrorMessage(PagesMessages.getString("AdvancedTCPSettingsBlock.3")); //$NON-NLS-1$
					}
				} else {
					setErrorMessage(PagesMessages.getString("AdvancedTCPSettingsBlock.3")); //$NON-NLS-1$
				}
			}
		}
	}

	public String getErrorMessage() {
		return fErrorMessage;
	}

	private void setErrorMessage( String string ) {
		fErrorMessage = string;
	}

	private boolean portNumberIsValid( String portNumber ) {
		try {
			int port = Integer.parseInt( portNumber );
			return ( port > 0 && port <= 0xFFFF );
		}
		catch( NumberFormatException e ) {
			return false;
		}
	}
	
	private boolean gdbServerBinaryIsValid() {
		
		return true;
	}

}
