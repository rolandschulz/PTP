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
package org.eclipse.ptp.cell.pdt.xml.wizard.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.eclipse.ptp.cell.pdt.xml.debug.Debug;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.table.TableContentProvider;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.table.TableLabelProvider;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.table.TableModifier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * 
 * 
 * @author Richard Maciel
 *
 */
public class PdtWizardGroupsPositionAndColorPage extends WizardPage {
	
	/**
	 * Enumerate the table columns
	 * 
	 * @author Richard Maciel
	 *
	 */
	public enum Column {
		NAME, YSTART, YEND, COLOR;
		
		public String getAssociatedName() {
			switch(this) {
			case NAME: return "name";//$NON-NLS-1$
			case YSTART: return "yStart";//$NON-NLS-1$
			case YEND: return "yEnd";//$NON-NLS-1$
			case COLOR: return "color";//$NON-NLS-1$
			}
			throw new AssertionError("Unknown op: " + this);//$NON-NLS-1$
		} 
		
		public static String [] getColumnsAssociatedNames() {
			return new String [] {NAME.getAssociatedName(), YSTART.getAssociatedName(), 
								  YEND.getAssociatedName(), COLOR.getAssociatedName()}; 
		}
	};
	
	
	protected EventGroupForest eventGroupForest;
	
	protected Table positionAndColorTable;
	protected TableViewer posAndColorTableViewer;
	protected Button selectColorButton;
	
	
	public PdtWizardGroupsPositionAndColorPage(EventGroupForest eventGroupForest) {
		super(PdtWizardGroupsPositionAndColorPage.class.getName());
		setTitle(Messages.PdtWizardGroupsPositionAndColorPage_Title);
		setDescription(Messages.PdtWizardGroupsPositionAndColorPage_Description);
		
		this.eventGroupForest = eventGroupForest;
	}
	
	public boolean isPageComplete() {
		boolean pageComplete = super.isPageComplete();
		
		// This page must be the actual to be complete.
		pageComplete &= isCurrentPage();
		
		return pageComplete;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
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

 		createTable(composite);
 		
 		selectColorButton = new Button(composite, SWT.PUSH);
 		GridData buttonGridData = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1);
 		selectColorButton.setLayoutData(buttonGridData);
 		selectColorButton.setText(Messages.PdtWizardGroupsPositionAndColorPage_Button_SelectColor);
 		selectColorButton.addSelectionListener(new ButtonSelectionHandler());
 		
 		
 		posAndColorTableViewer = createTableViewer();
 		
 		posAndColorTableViewer.setInput(eventGroupForest);
 		
 		setControl(composite);
	}

	/**
	 * Create the view responsible for displaying and manipulating (edit) table data.
	 * 
	 */
	private TableViewer createTableViewer() {
		TableViewer tableViewer = new TableViewer(positionAndColorTable);
		
		tableViewer.setUseHashlookup(true);
		
		tableViewer.setColumnProperties(Column.getColumnsAssociatedNames());
		
		// Create cell editors
		CellEditor [] editors = new CellEditor[Column.values().length];
		
		// Col 0: name (text)
		TextCellEditor textEditor = new TextCellEditor(positionAndColorTable);
		editors[Column.NAME.ordinal()] = textEditor;
		
		// Col 1: position start (float)
		textEditor = new TextCellEditor(positionAndColorTable);
		//((Text)textEditor.getControl()).addVerifyListener(posVerify);
		editors[Column.YSTART.ordinal()] = textEditor;
		
		// Col2: pos end (float)
		textEditor = new TextCellEditor(positionAndColorTable);
		//((Text)textEditor.getControl()).addModifyListener(modList);
		editors[Column.YEND.ordinal()] = textEditor;
		
		// col3: color (3 byte hexadecimal)
		textEditor = new TextCellEditor(positionAndColorTable);
		//((Text)textEditor.getControl()).addVerifyListener(colorVerify);
		editors[Column.COLOR.ordinal()] = textEditor;
		
		
		// Assign editors to table viewer
		tableViewer.setCellEditors(editors);
		
		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(new TableModifier(tableViewer, this));
		
		tableViewer.setContentProvider(new TableContentProvider());
		
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		return tableViewer;
	}

	/**
	 * Create table and table's columns 
	 * 
	 * @param composite
	 */
	private void createTable(Composite composite) {
		// Create table
		positionAndColorTable = new Table(composite, SWT.BORDER);
		
		// Set grid lines and header
		positionAndColorTable.setHeaderVisible(true);
		positionAndColorTable.setLinesVisible(true);
		
		// Set Layoutdata
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		positionAndColorTable.setLayoutData(tableData);
		
		// Create table columns
		TableColumn name, yStart, yEnd, color;
		name = new TableColumn(positionAndColorTable, SWT.LEFT);
		name.setWidth(200);
		name.setText(Messages.PdtWizardGroupsPositionAndColorPage_CreateTable_ColumnHeader_EventGroup_Name);
		
		yStart = new TableColumn(positionAndColorTable, SWT.LEFT);
		yStart.setWidth(100);
		yStart.setText(Messages.PdtWizardGroupsPositionAndColorPage_CreateTable_ColumnHeader_EventGroup_Start);
		
		yEnd = new TableColumn(positionAndColorTable, SWT.LEFT);
		yEnd.setWidth(100);
		yEnd.setText(Messages.PdtWizardGroupsPositionAndColorPage_CreateTable_ColumnHeader_EventGroup_End);
		
		color = new TableColumn(positionAndColorTable, SWT.LEFT);
		color.setWidth(200);
		color.setText(Messages.PdtWizardGroupsPositionAndColorPage_CreateTable_ColumnHeader_EventGroup_Color);
	}

	public EventGroupForest getEventGroupForest() {
		return eventGroupForest;
	} 
	
	public void displayErrorDialog(IStatus status) {
		Shell sh = this.getShell();
		
		ErrorDialog errdlg = new ErrorDialog(sh, Messages.PdtWizardGroupsPositionAndColorPage_DisplayErrorDialog_TableInputError, "", status, IStatus.ERROR); //$NON-NLS-1$
		errdlg.open();
	}
	
	protected class ButtonSelectionHandler extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Debug.read();
			try {
				selectColor();
			} catch (Exception exception) {
				Debug.POLICY.logError(exception);
			}
		}
	}

	/**
	 * Let the user select color of the selected event group using a color dialog. Does nothing if the
	 * there no event group is selected. 
	 */
	public void selectColor() {
		// Check if there is a row selected first.
		IStructuredSelection sel = (IStructuredSelection)posAndColorTableViewer.getSelection();
		if(!sel.isEmpty()) {
			EventGroup selEventGroup = (EventGroup)sel.getFirstElement();
			
			// Open dialog
			ColorDialog selColorDialog = new ColorDialog(this.getShell());
			selColorDialog.setText(NLS.bind(Messages.PdtWizardGroupsPositionAndColorPage_SelectColor_Dialog_Message, selEventGroup.getName()));
			selColorDialog.open();
			
			RGB selColor = selColorDialog.getRGB();
			
			// Set item color
			selEventGroup.setColor((selColor.red << 16) + (selColor.green << 8) + (selColor.blue));
			
			// Update view
			posAndColorTableViewer.update(selEventGroup, null);
		}
	
		
	}
	/**
	 * Refresh wizard page data 
	 */
	public void refresh() {
		posAndColorTableViewer.refresh();
	}

}
