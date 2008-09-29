/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.utils.ui.swt;

import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

/**
 * @author Richard Maciel
 *
 */
public class FileAndWorkspaceGroup extends GenericControlGroup {
	FileAndWorkspaceMold mold;
	
	Composite container;
	Button findInWorkspace;
	Text path;

	FilesystemTreeNode pai = new FilesystemTreeNode(new Integer(1));
	FilesystemTreeNode filho1 = new FilesystemTreeNode(new String("filho1")); //$NON-NLS-1$
	FilesystemTreeNode filho2 = new FilesystemTreeNode(new String("filho2")); //$NON-NLS-1$
	
	public class FilesystemTreeNode extends TreeNode {
		@Override
		public String toString() {
			if(value != null) {
				return value.toString();
			} else {
				return "null"; //$NON-NLS-1$
			}
		}
		
		public FilesystemTreeNode(Object value) {
			super(value);
		}
	}
	
	
	
	/**
	 * @param parent
	 * @param mold
	 */
	private FileAndWorkspaceGroup(Composite parent, FileAndWorkspaceMold mold) {
		super(parent, mold);
		this.mold = mold;
		
		findInWorkspace.setText(mold.workspaceLabel);
		getButton().setText(mold.buttonLabel);

		pai.setChildren(new TreeNode[]{filho1, filho2});
		
		generateButtonsHandler(mold);
	}

	private void generateButtonsHandler(FileAndWorkspaceMold mold2) {
		// If bit IS_DIRECTORY is on, register a directory handler
		// for both buttons.
		SelectionAdapter browseSelectionListener;
		if((mold.bitmask & FileAndWorkspaceMold.DIRECTORY_SELECTION) != 0) {
			browseSelectionListener = 
				new DirectoryButtonSelectionListener(path, mold.browseWindowLabel, mold.browseWindowMessage);
			
		} else {
			// Else register a file handler
			browseSelectionListener = 
				new FileButtonSelectionListener(path, mold.browseWindowLabel);
		}
		getButton().addSelectionListener(browseSelectionListener);
		
		///org.eclipse.ui.dialogs.
		findInWorkspace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementTreeSelectionDialog treeSelect = new ElementTreeSelectionDialog(getShell(), new LabelProvider(), new TreeNodeContentProvider());
				//treeSelect.setInput(new TreeNode []{pai});
				
				File workspace = new File("/home/richardm/tmp"); //$NON-NLS-1$
				FilesystemToTreeMapper treeMapper = new FilesystemToTreeMapper();
				treeSelect.setInput(new TreeNode []{treeMapper.filesystemTreeFactory(workspace)});
				//treeSelect.se
				
				treeSelect.open();
				
				TreeNode selNode;
				if((selNode = (TreeNode)treeSelect.getFirstResult()) != null) {
					path.setText(selNode.getValue().toString());
				}
			}
		});
		///findInWorkspace.addSelectionListener(new )
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.ui.swt.GenericControlGroup#createCustomControl(int, org.eclipse.swt.layout.GridData)
	 */
	@Override
	protected Control createCustomControl(int bitmask, GridData gridData) {
		container = new Composite(this, SWT.NONE);
		
		// Set layout
		GridLayout containerLayout = new GridLayout();
		containerLayout.marginHeight = 0;
		containerLayout.marginWidth = 0;
		containerLayout.horizontalSpacing = 5;
		containerLayout.verticalSpacing = 0;
		containerLayout.numColumns = 2;
		//containerLayout.marginLeft = 0;
		
		container.setLayout(containerLayout);
		
		// Set grid data
		gridData.horizontalAlignment = SWT.FILL;
		
		// Create griddata for the controls
		GridData pathGD = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		GridData workplaceGD = GridDataFactory.copyData(pathGD);
		workplaceGD.grabExcessHorizontalSpace = false;
		
		// Insert text and button inside the container
		path = new Text(container, SWT.SINGLE | SWT.BORDER);
		path.setLayoutData(pathGD);
		findInWorkspace = new Button(container, SWT.PUSH);
		findInWorkspace.setLayoutData(workplaceGD);
		
		
		
		
		return container;
	}
	
	public void addModifyListener(ModifyListener listener) {
		path.addModifyListener(listener);
	}
	
	public void removeModifyListener(ModifyListener listener) {
		path.removeModifyListener(listener);
	}
	
	public void setString(String string) {
		path.setText(string);
	}
	
	public String getString() {
		return path.getText();
	}
	
	public Button getWorkspaceButton() {
		return this.findInWorkspace;
	}
	
	public Button getBrowseButton() {
		return getButton();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		path.setEnabled(enabled);
		findInWorkspace.setEnabled(enabled);
	}
	
}
