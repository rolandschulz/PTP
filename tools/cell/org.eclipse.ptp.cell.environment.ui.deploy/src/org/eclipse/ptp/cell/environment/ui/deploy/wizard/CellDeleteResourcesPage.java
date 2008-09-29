/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.ui.deploy.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ptp.cell.environment.ui.deploy.events.DeleteJobWrapper;
import org.eclipse.ptp.cell.environment.ui.deploy.events.Messages;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


public class CellDeleteResourcesPage extends AbstractCellWizardPage {
	
	private Text resourcePath;
	private Button deleteButton;	
	
	public CellDeleteResourcesPage(String pageName, ITargetControl control){
		super(pageName, control);
		setTitle(Messages.CellDeleteResourcesPage_0);
		setDescription(Messages.CellDeleteResourcesPage_1);
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
        deleteButton.setFocus();
	}
	
	protected void createDestinationGroup(Composite parent) {
		Font font = parent.getFont();
		
		Group deleteGroup = new Group(parent, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 1;
		deleteGroup.setLayout(groupLayout);
		deleteGroup.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_BEGINNING));
		deleteGroup.setText(Messages.CellDeleteResourcesPage_2);
		deleteGroup.setFont(font);
		
		resourcePath = new Text(deleteGroup, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
		resourcePath.addListener(SWT.Modify, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH + 200;
		resourcePath.setLayoutData(data);
		resourcePath.setFont(font);
		
		Composite comp = new Composite(deleteGroup, SWT.NONE);
		comp.setLayout(groupLayout);
		comp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		deleteButton = new Button(comp, SWT.PUSH);
		//deleteButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_BEGINNING));
		deleteButton.setText(Messages.CellDeleteResourcesPage_3);
		deleteButton.addListener(SWT.Selection, this);
		deleteButton.setFont(font);
        setButtonLayoutData(deleteButton);
        deleteButton.setFocus();
	}

	protected void createOptionsGroup(Composite parent){}
	
	protected void createResourcesGroup(Composite parent) {}

	public boolean finish() {
		return true;
	}

	private void handleDeletePressed(){
		try {
		String path = resourcePath.getText();
		
		if(path == null || path.length() == 0){
			setErrorMessage(Messages.CellDeleteResourcesPage_4);
			return;
		}
		
		try{        	
        	//create copy job wrapper class, and initialize its data. 
			DeleteJobWrapper deleteWrapper = new DeleteJobWrapper();
			ProgressMonitorDialog jobMonitor = new ProgressMonitorDialog(getContainer().getShell());
			deleteWrapper.init(cellControl, path);
				
			jobMonitor.setOpenOnRun(true);
			jobMonitor.run(true, true, deleteWrapper);
        	
        } catch (InvocationTargetException e) {
			if (e.getTargetException() == null) {
				MessageDialog.openError(getContainer().getShell(), Messages.CellDeleteResourcesPage_5, e.getMessage());
				return;
			}
			Debug.POLICY.logError(e);
			MessageDialog.openError(getContainer().getShell(), e.getMessage(), e.getTargetException().toString());
			return;
		} catch (InterruptedException e) {
			MessageDialog.openInformation(getContainer().getShell(), Messages.CellDeleteResourcesPage_6, Messages.CellDeleteResourcesPage_7);
			return;
		}
		
		MessageDialog.openInformation(getContainer().getShell(), Messages.CellDeleteResourcesPage_8, 
		Messages.CellDeleteResourcesPage_9);
		} catch (Exception ee) {
    		Debug.POLICY.logError(ee);
    	}
	}
	
	public void handleEvent(Event event) {
		try {
			setErrorMessage(null);
			Widget source = event.widget;
			if(source == deleteButton) {
				handleDeletePressed();
			}
		} catch (Exception ee) {
    		Debug.POLICY.logError(ee);
		}
	}
}
