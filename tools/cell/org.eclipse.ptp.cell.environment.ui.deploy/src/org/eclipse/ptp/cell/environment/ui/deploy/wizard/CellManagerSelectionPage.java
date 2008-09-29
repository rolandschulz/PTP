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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.ptp.cell.environment.ui.deploy.DeployPlugin;
import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ptp.cell.environment.ui.deploy.events.Messages;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class CellManagerSelectionPage extends WizardSelectionPage implements SelectionListener, MouseListener {

	class CopyToEnvWizardNode implements IWizardNode {

		Wizard copyToEnvWizard;
		
		public void dispose() {
			if(copyToEnvWizard != null)
				copyToEnvWizard.dispose();			
		}

		public Point getExtent() {
			return new Point(0,0);
		}

		public IWizard getWizard() {
			if(copyToEnvWizard == null){
				copyToEnvWizard = new Wizard(){
					public void addPages(){
						addPage(new CellExportResourcesPage("CellExportResourcesPage", cellControl)); //$NON-NLS-1$
					}
					
					public boolean performFinish() {
						CellExportResourcesPage page = (CellExportResourcesPage)getStartingPage();
						return page.finish();
					}			
				};
				copyToEnvWizard.setWindowTitle(Messages.CellManagerSelectionPage_0);
			}
			return copyToEnvWizard;
		}

		public boolean isContentCreated() {
			if(copyToEnvWizard == null)
				return false;
			else
				return true;
		}
		
		public String toString(){
			return Messages.CellManagerSelectionPage_1;
		}
	}
	
	class CopyFromEnvWizardNode implements IWizardNode {

		Wizard copyFromEnvWizard;
		
		public void dispose() {
			if(copyFromEnvWizard != null)
				copyFromEnvWizard.dispose();			
		}

		public Point getExtent() {
			return new Point(0,0);
		}

		public IWizard getWizard() {
			if(copyFromEnvWizard == null){
				copyFromEnvWizard = new Wizard(){
					public void addPages(){
						addPage(new CellImportResourcesPage("CellImportResourcesPage", cellControl)); //$NON-NLS-1$
					}
					
					public boolean performFinish() {
						CellImportResourcesPage page = (CellImportResourcesPage)getStartingPage();
						return page.finish();
					}			
				};
				copyFromEnvWizard.setWindowTitle(Messages.CellManagerSelectionPage_2);
			}
			return copyFromEnvWizard;
		}

		public boolean isContentCreated() {
			if(copyFromEnvWizard == null)
				return false;
			else
				return true;
		}
		
		public String toString(){
			return Messages.CellManagerSelectionPage_3;
		}
	}
	
	class DeleteFromEnvWizardNode implements IWizardNode {

		Wizard deleteFromEnvWizard;
		
		public void dispose() {
			if(deleteFromEnvWizard != null)
				deleteFromEnvWizard.dispose();			
		}

		public Point getExtent() {
			return new Point(0,0);
		}

		public IWizard getWizard() {
			if(deleteFromEnvWizard == null){
				deleteFromEnvWizard = new Wizard(){
					public void addPages(){
						addPage(new CellDeleteResourcesPage("CellDeleteResourcesPage", cellControl)); //$NON-NLS-1$
					}
					
					public boolean performFinish() {
						CellDeleteResourcesPage page = (CellDeleteResourcesPage)getStartingPage();
						return page.finish();
					}			
				};
				deleteFromEnvWizard.setWindowTitle(Messages.CellManagerSelectionPage_4);
			}
			return deleteFromEnvWizard;
		}

		public boolean isContentCreated() {
			if(deleteFromEnvWizard == null)
				return false;
			else
				return true;
		}		
		
		public String toString(){
			return Messages.CellManagerSelectionPage_5;
		}
	}	
	
	private ITargetControl cellControl;
	private Table table;
	private IWizardNode[] nodes;
	private boolean initialSelection;
	
	protected CellManagerSelectionPage(String pageName) {
		super(pageName);
	}
	
	public CellManagerSelectionPage(String pageName, ITargetControl control){
		super(pageName);
		this.cellControl = control;
		nodes = new IWizardNode[3];
		nodes[0] = new CopyToEnvWizardNode();
		nodes[1] = new CopyFromEnvWizardNode();
		nodes[2] = new DeleteFromEnvWizardNode();
		initialSelection = true;
		
		setTitle(Messages.CellManagerSelectionPage_6);
		setMessage(Messages.CellManagerSelectionPage_7);
	}

	public void widgetDefaultSelected(SelectionEvent e) {}

	public void widgetSelected(SelectionEvent e) {
		try {
			if (initialSelection) {
				table.deselectAll();
				initialSelection = false;
				return;
			}
			int index = table.getSelectionIndex();
			if (index != -1) {
				setSelectedNode(nodes[index]);
				setMessage(nodes[index].toString());
			}
		} catch (Exception ee) {
			Debug.POLICY.logError(ee);
		}
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite = new Composite(parent, SWT.NULL);
	    composite.setLayout(new GridLayout());
	    composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
	    composite.setFont(parent.getFont());
	        
	    table = new Table(composite, SWT.SINGLE | SWT.FULL_SELECTION);
	    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	    data.widthHint = 300;
	    data.heightHint = 250;
		table.setLayoutData(data);
		table.setFont(composite.getFont());
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		table.setEnabled(true);
		table.setVisible(true);
		table.addSelectionListener(this);
		table.addMouseListener(this);
	    
		TableItem item1 = new TableItem(table, SWT.NONE, 0);
		TableItem item2 = new TableItem(table, SWT.NONE, 1);
		TableItem item3 = new TableItem(table, SWT.NONE, 2);
		item1.setText(Messages.CellManagerSelectionPage_8);
		item1.setImage(DeployPlugin.getImageDescriptor("icons/copy_to.png").createImage()); //$NON-NLS-1$
		item2.setText(Messages.CellManagerSelectionPage_9);
		item2.setImage(DeployPlugin.getImageDescriptor("icons/copy_from.png").createImage()); //$NON-NLS-1$
		item3.setText(Messages.CellManagerSelectionPage_10);
		item3.setImage(DeployPlugin.getImageDescriptor("icons/delete.png").createImage()); //$NON-NLS-1$
	        
	    setPageComplete(false);
	    setControl(composite);		
	}
	
	public void dispose(){
		super.dispose();
		TableItem[] items = table.getItems();
		for(int i = 0; i < items.length; ++i){
			Image temp = items[i].getImage();
			if(temp != null)
				temp.dispose();
		}			
	}

	public void mouseDoubleClick(MouseEvent e) {
		try {
			Object source = e.getSource();
			if (source instanceof Table) {
				if (source == table) {
					int index = table.getSelectionIndex();
					if (index != -1) {
						IWizardPage page = getNextPage();
						if (page != null)
							getContainer().showPage(page);
					}
				}
			}
		} catch (Exception ee) {
			Debug.POLICY.logError(ee);
		}
	}

	public void mouseDown(MouseEvent e) {/*do nothing*/}

	public void mouseUp(MouseEvent e) {/*do nothing*/}
}