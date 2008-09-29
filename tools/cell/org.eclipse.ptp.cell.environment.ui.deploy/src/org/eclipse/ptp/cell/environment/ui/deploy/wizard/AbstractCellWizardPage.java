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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ptp.cell.environment.ui.deploy.events.Messages;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.WizardDataTransferPage;


public abstract class AbstractCellWizardPage extends WizardDataTransferPage implements Listener {

	protected ITargetControl cellControl;
	
	protected AbstractCellWizardPage(String pageName) {
		super(pageName);
	}
	
	public AbstractCellWizardPage(String pageName, ITargetControl control){
		super(pageName);
		this.cellControl = control;
	}

	protected boolean allowNewContainerName() {	return false; }	
	
	public boolean canFinish(){	return determinePageCompletion(); }
	
	public boolean canFlipToNextPage(){ return false; }

	public void createControl(Composite parent){
		initializeDialogUnits(parent);
		
		final ScrolledComposite topScrolledControl = new ScrolledComposite(parent, SWT.V_SCROLL);
		setControl(topScrolledControl);		
		
		final Composite topControl = new Composite(topScrolledControl, SWT.NONE);
		
		topControl.setLayout(new GridLayout());
		topControl.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		topControl.setFont(parent.getFont());

        createResourcesGroup(topControl);
        
        createDestinationGroup(topControl);
        createOptionsGroup(topControl);

        restoreWidgetValues();

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());        
        
        topScrolledControl.setContent(topControl);
		topScrolledControl.setExpandVertical(true);
		topScrolledControl.setExpandHorizontal(true);
		topScrolledControl.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				try {
					Rectangle r = topScrolledControl.getClientArea();
					topScrolledControl.setMinSize(topControl.computeSize(r.width, SWT.DEFAULT));
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
		});
	}
	
	protected abstract void createDestinationGroup(Composite parent);
	
	protected abstract void createResourcesGroup(Composite parent);
	
	public abstract boolean finish();
	
	public abstract void handleEvent(Event event);
	
	public boolean queryQuestion(String title, String message){
		final MessageDialog dialog = new MessageDialog(getContainer().getShell(), title, null, 
				message, MessageDialog.QUESTION, 
				new String[]{Messages.AbstractCellWizardPage_0, Messages.AbstractCellWizardPage_1}, 0);
		
		//run in syncExec because callback is from an operation,
        //which is probably not running in the UI thread.
        getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					dialog.open();
				} catch (Exception e) {
					Debug.POLICY.logError(e);
				}
			}
		});		
        
        if(dialog.getReturnCode() == 0)
        	return true;
        else
        	return false;
	}
	
}
