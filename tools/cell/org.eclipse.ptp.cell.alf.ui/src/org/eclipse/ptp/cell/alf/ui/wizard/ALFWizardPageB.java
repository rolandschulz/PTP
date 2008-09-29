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

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.cell.alf.ui.Activator;
import org.eclipse.ptp.cell.alf.ui.Messages;
import org.eclipse.ptp.cell.alf.ui.core.ALFBuffer;
import org.eclipse.ptp.cell.alf.ui.core.ALFBufferValidator;
import org.eclipse.ptp.cell.alf.ui.core.ALFConstants;
import org.eclipse.ptp.cell.alf.ui.debug.Debug;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


/**
 * This class implements the second page in the ALF creation wizard
 * 
 * @author Sean Curry
 * @since 3.0.0
 */
public class ALFWizardPageB extends WizardPage {
	
	private static long TOTAL_AVAIL_SPU_MEMORY = 251904;
	
	private Table alfBuffersTable;
	
	private ArrayList alfBuffers;
	
	private ALFBufferValidator validator;
	
	private Button addBufferButton;
	private Button editBufferButton;
	private Button removeBufferButton;
	
	private Text numDTEntry;
	private Text numDTEntryHelp;
	
	private Text localMemoryRemaining;
	private Text localMemoryHelp;

	private int editIndex;

	protected ALFWizardPageB(String pageName, String title, String description) {
		super(pageName);
		setTitle(title);
		setDescription(description);
		alfBuffers = new ArrayList();
		validator = new ALFBufferValidator(this);
		editIndex = -1;
	}
	
	public boolean canFlipToNextPage(){
		updateAndValidateBuffers();
		return super.canFlipToNextPage();
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
		
		Label bufferListLabel = new Label(composite, SWT.NONE);
		bufferListLabel.setText(Messages.ALFWizardPageB_bufferListTitle + ":"); //$NON-NLS-1$
		bufferListLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		
		alfBuffersTable = new Table(composite, SWT.SINGLE);
		GridData tableData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		tableData.widthHint = 300;
		tableData.heightHint = 125;
		alfBuffersTable.setLayoutData(tableData);
		alfBuffersTable.setFont(font);
		alfBuffersTable.setLinesVisible(false);
		alfBuffersTable.setHeaderVisible(true);
		alfBuffersTable.setEnabled(true);
		
		TableColumn column1 = new TableColumn(alfBuffersTable, SWT.LEFT, 0);
		TableColumn column2 = new TableColumn(alfBuffersTable, SWT.RIGHT, 1);
		column1.setText(Messages.ALFWizardPageB_columnOneName);
		column1.setWidth(385);
		column2.setText(Messages.ALFWizardPageB_columnTwoName);
		column2.setWidth(85);
		
		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		GridLayout layout3 = new GridLayout();
		layout3.numColumns = 3;
		buttonsComposite.setLayout(layout3);
		
		addBufferButton = new Button(buttonsComposite, SWT.PUSH);
		addBufferButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));
		addBufferButton.setText(Messages.ALFWizardPageB_addBufferButtonText);
		addBufferButton.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				try {
					handleAddBufferPressed();
					updateAndValidateBuffers();
					getWizard().getContainer().updateButtons();
				} catch (Exception e) {
					Debug.POLICY.logError(e);
				}
			}
		});
		
		editBufferButton = new Button(buttonsComposite, SWT.PUSH);
		editBufferButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));
		editBufferButton.setText(Messages.ALFWizardPageB_editBufferButtonText);
		editBufferButton.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				try {
					handleEditBufferPressed();
					updateAndValidateBuffers();
					getWizard().getContainer().updateButtons();
				} catch (Exception e) {
					Debug.POLICY.logError(e);
				}
			}
		});

		removeBufferButton = new Button(buttonsComposite, SWT.PUSH);
		removeBufferButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));
		removeBufferButton.setText(Messages.ALFWizardPageB_removeBufferButtonText);
		removeBufferButton.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				try {
					handleRemoveBufferPressed();
					updateAndValidateBuffers();
					getWizard().getContainer().updateButtons();
				} catch (Exception e) {
					Debug.POLICY.logError(e);
				}
			}
			
		});
		
		
	   /* ****************************************************************************************************** *
		* Create the Group and other widgets for the Local Memory Size (total and remaining) of the accelerators *
		* ****************************************************************************************************** */
		Group localMemoryGroup = new Group (composite, SWT.NONE);
		GridLayout layout2 = new GridLayout();
		layout2.numColumns = 2;
		localMemoryGroup.setText(Messages.ALFWizardPageB_localMemoryGroupText);
		localMemoryGroup.setLayout(layout2);
		localMemoryGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		localMemoryGroup.setFont(font);
		
		Label localMemoryTotalLabel = new Label(localMemoryGroup, SWT.NONE);
		localMemoryTotalLabel.setText(Messages.ALFWizardPageB_localMemoryTotalMemoryLabelText);
		localMemoryTotalLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		
		Text localMemoryTotal = new Text(localMemoryGroup, SWT.READ_ONLY | SWT.SINGLE);
		localMemoryTotal.setText(TOTAL_AVAIL_SPU_MEMORY + " B"); //$NON-NLS-1$
		localMemoryTotal.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_BEGINNING));
		localMemoryTotal.setBackground(localMemoryTotal.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		Label localMemoryRemainingLabel = new Label(localMemoryGroup, SWT.NONE);
		localMemoryRemainingLabel.setText(Messages.ALFWizardPageB_localMemoryRemainingMemoryLabelText);
		localMemoryRemainingLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		
		localMemoryRemaining = new Text(localMemoryGroup, SWT.READ_ONLY | SWT.SINGLE);
		localMemoryRemaining.setText(Messages.ALFWizardPageB_localMemoryRemainingMemoryInitialText);
		localMemoryRemaining.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_BEGINNING));
		localMemoryRemaining.setBackground(localMemoryRemaining.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		localMemoryHelp = ALFWizard.createWhatsThisHelpText(localMemoryGroup, getShell(), getWizard(), "data/local_memory_help.html", 325, 275); //$NON-NLS-1$
		localMemoryHelp.getFont();

		
	   /* ********************************************************************************* *
		* Create the Group and other widgets for the number of DT entry related information *
		* ********************************************************************************* */
		Group numDTEntryGroup = new Group(composite, SWT.NONE);
		//GridLayout layout2 = new GridLayout();
		//layout2.numColumns = 2;
 		numDTEntryGroup.setText(Messages.ALFWizardPageB_numDTEntryGroupText);
		numDTEntryGroup.setLayout(layout2);
		numDTEntryGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		numDTEntryGroup.setFont(font);
		
		Label numDTEntryLabel = new Label(numDTEntryGroup, SWT.NONE);
		numDTEntryLabel.setText(Messages.ALFWizardPageB_numDTEntryLabelText);
		numDTEntryLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		
		numDTEntry = new Text(numDTEntryGroup, SWT.READ_ONLY | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd1.widthHint = 75;
		gd1.minimumWidth = 75;
		numDTEntry.setLayoutData(gd1);
		numDTEntry.setBackground(numDTEntry.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		numDTEntryHelp = ALFWizard.createWhatsThisHelpText(numDTEntryGroup, getShell(), getWizard(), "data/dt_entry_help.html", 550, 275); //$NON-NLS-1$
		numDTEntryHelp.getFont();
		
		updateAndValidateBuffers();
		getWizard().getContainer().updateButtons();

		initializeDialogUnits(composite);
		
		setControl(composite);
	}
	
	/*
	public void dispose(){
		
		alfBuffersTable.dispose();
		addBufferButton.dispose();
		editBufferButton.dispose();
		removeBufferButton.dispose();
		usedMemory.dispose();
		freeMemory.dispose();
		
		super.dispose();
	}
	*/
	
	/**
	 * Checks to see if there is an existing ALFBuffer object in the list of buffers with the same name as the argument.
	 * 
	 * @param name a buffers name to check against the name's of the existing buffers
	 * @return true if there exists an ALFBuffer in the ArrayList of buffers with the same name as the argument <code>name</code>
	 */
	public boolean doesSameBufferNameExist(String name){
		ALFBuffer tempBuffer;
		for(int i = 0; i < alfBuffers.size(); i++){
			tempBuffer = (ALFBuffer) alfBuffers.get(i);
			/* if a buffer is currently being edited, then editIndex will be the index of the buffer that is being edited.
			 * I use editIndex to prevent this method from finding the name of the same element that is being edited, and returning true, 
			 * saying that the name already exists. if an alf buffer element is being edited, then when the call to this method is made to 
			 * validate the alfbuffer object, this method should return false if the parameter 'name' is the same name as the already 
			 * existing alfbuffer object being edited. 
			 */
			if(name.equals(tempBuffer.getName()) && editIndex != i)
				return true;
		}
		
		return false;
	}
	
	protected ArrayList getBuffers(){
		return alfBuffers;
	}
	
	private void handleAddBufferPressed(){		
		ALFWizardPageA prevPage = (ALFWizardPageA)getPreviousPage();
		if(prevPage == null)
			throw new RuntimeException(Messages.ALFWizardPageB_previousPageIsNullErrorMsg);
		
		ALFBufferDialog dialog = new ALFBufferDialog(getShell(), Messages.ALFWizardPageB_createAlfBufferDialogTitle, null, validator, this);
		dialog.setBlockOnOpen(true);
		int returnCode = dialog.open();
		if(returnCode == IDialogConstants.OK_ID){
			ALFBuffer newBuffer = dialog.getBuffer();
			if(newBuffer != null){
				int bufferType = newBuffer.getBufferType();
				TableItem newItem = new TableItem(alfBuffersTable, SWT.LEFT);
				Image nullImage = null;
				newItem.setImage(nullImage);
				newItem.setText(0, newBuffer.getName());
			
				switch(bufferType){			
				case ALFConstants.ALF_BUFFER_INPUT: 
					newItem.setText(1, Messages.ALFWizardPageB_inputBufferType); 
					break;
				
				case ALFConstants.ALF_BUFFER_OUTPUT:
					newItem.setText(1, Messages.ALFWizardPageB_outputBufferType);
					break;
				
				default:
					newItem.setText(1, Messages.ALFWizardPageB_notAvailableTxt);
				}
				alfBuffersTable.select(alfBuffersTable.indexOf(newItem));
				
				alfBuffers.add(newBuffer);
			}
		}
	}
	
	private void handleEditBufferPressed(){
		int index = alfBuffersTable.getSelectionIndex();
		// if index == -1, then no item is currently selected. so we cannot open the bufferd dialog window
		if(index == -1)
			return;
		
		editIndex = index;		
		ALFBufferDialog dialog = new ALFBufferDialog(getShell(), Messages.ALFWizardPageB_editAlfBufferDialogTitle, (ALFBuffer) alfBuffers.get(index), validator, this);
		int returnCode = dialog.open();
		if(returnCode == IDialogConstants.OK_ID){
			
			ALFBuffer buffer = dialog.getBuffer();
			if(buffer != null){
				int bufferType = buffer.getBufferType();
				TableItem item = alfBuffersTable.getItem(index);
				item.setText(0, buffer.getName());
				
				switch(bufferType){
					
				case ALFConstants.ALF_BUFFER_INPUT: 
					item.setText(1, Messages.ALFWizardPageB_inputBufferType); break;
				
				case ALFConstants.ALF_BUFFER_OUTPUT:
					item.setText(1, Messages.ALFWizardPageB_outputBufferType); break;
				
				default:
					item.setText(1, Messages.ALFWizardPageB_notAvailableTxt); break;
				}
				
				alfBuffers.set(index, dialog.getBuffer());
			}
		}
		
		editIndex = -1;
	}

	private void handleRemoveBufferPressed(){
		int selected = alfBuffersTable.getSelectionIndex();
		if(selected != -1){
			ALFBuffer temp;
			String bufferName = alfBuffersTable.getItem(selected).getText(0);
			alfBuffersTable.remove(selected);

			// Find the associated buffer in the ArrayList of ALFBuffers and remove it as well
			for(int i = 0; i < alfBuffers.size(); i++){
				temp = (ALFBuffer) alfBuffers.get(i);
				if(temp.getName().equals(bufferName)){
					alfBuffers.remove(i);
					break;
				}
			}
		}
	}

	public boolean is64bit(){
		ALFWizardPageA pageA = (ALFWizardPageA)getPreviousPage();
		if(pageA == null)
			return false;
		else
			return pageA.is64bit();
	}
	
	/**
	 * Checks whether or not the argument <code>numDT</code> is equal to the number of data transfer entries for the existing alf buffers.
	 * 
	 * @param numDT the number of data transfer entries to be checked
	 * @return true if the argument <code>numDT</code> matches the number of data transfer entries of the existing alf buffer objects, else return false
	 */
	public boolean isNumDTEntriesValid(long numDT){

		// Verify that the argument 'numDT' is a valid number of data transfer entries (greater than 0)
		if(numDT < 1)
			return false;
		
		// If there currently are no alf buffers, then the 'numDT' argument is always valid
		if(alfBuffers == null || alfBuffers.size() == 0)
			return true;

		// Iterate through the array of alf buffer objects, and make sure the argument 'numDT' matches the number of data transfer entries of each existing buffer
		ALFBuffer temp;
		for(int i = 0; i < alfBuffers.size(); i++){
			temp = (ALFBuffer) alfBuffers.get(i);
			if(temp.getNumberDTEntries() != numDT)
				return false;
		}

		return true;
	}

	public boolean isPageComplete(){
		updateAndValidateBuffers();

		return super.isPageComplete();
	}

	protected void updateAndValidateBuffers(){
		ALFWizardPageA pageA = (ALFWizardPageA)getPreviousPage();
		if(pageA == null){
			if(getErrorMessage() != null){
				setErrorMessage(null);
				setPageComplete(false);
			}
			updateLocalMemorySize();
			updateNumDTEntries();
			return;
		}
		
		// If no buffers exist yet, there is nothing to update
		if(alfBuffers == null || alfBuffers.size() == 0){
			if(getErrorMessage() != null){
				setErrorMessage(null);
				setPageComplete(true);
			}
			updateLocalMemorySize();
			updateNumDTEntries();
			return;
		}

		int expNumAccel = pageA.getExpAccelNum();
		Image errorImage = Activator.getImageDescriptor("data/images/error.jpg").createImage(); //$NON-NLS-1$
		Image nullImage = null;
		ALFBuffer buffer;
		TableItem item;
		long newDistSize;
		boolean allBuffersValid = true;
		for(int i = 0; i < alfBuffers.size(); i++){
			buffer = (ALFBuffer)alfBuffers.get(i);
			item = alfBuffersTable.getItem(i);
			// If the expected number of accelerator nodes has been set to 0 (all available), then any buffers with a distribution model set to 
			// BLOCK need to be marked as invalid
			if(expNumAccel == 0){
				if((buffer.getDistributionModelX() == ALFConstants.DIST_MODEL_BLOCK) || 
						(buffer.getDistributionModelY() == ALFConstants.DIST_MODEL_BLOCK) || 
						(buffer.getDistributionModelZ() == ALFConstants.DIST_MODEL_BLOCK)){
					if(getErrorMessage() == null){
						setErrorMessage(Messages.ALFWizardPageB_errorMsgInvalidBuffer);
						setPageComplete(false);
					}
					buffer.setIsValid(false);
					item.setImage(errorImage);
					allBuffersValid = false;
				} else {
					buffer.setIsValid(true);
					item.setImage(nullImage);
				}
			}
			// Else, if the expected number of accelerator nodes is not 0, then update any distribution size fields that have the associated 
			// distribution model set to BLOCK
			else {
				if(buffer.getDistributionModelX() == ALFConstants.DIST_MODEL_BLOCK){
					newDistSize = buffer.getDimensionSizeX() / expNumAccel;
					if(newDistSize > 0)
						buffer.setDistributionSizeX(newDistSize);
					else
						buffer.setDistributionSizeX(0);
				}
				if(buffer.getDistributionSizeY() == ALFConstants.DIST_MODEL_BLOCK){
					newDistSize = buffer.getDimensionSizeY() / expNumAccel;
					if(newDistSize > 0)
						buffer.setDistributionSizeY(newDistSize);
					else
						buffer.setDistributionSizeY(0);
				}
				if(buffer.getDistributionSizeZ() == ALFConstants.DIST_MODEL_BLOCK){
					newDistSize = buffer.getDimensionSizeZ() / expNumAccel;
					if(newDistSize > 0)
						buffer.setDistributionSizeZ(newDistSize);
					else
						buffer.setDistributionSizeZ(0);
				}
				buffer.setIsValid(true);
				item.setImage(nullImage);
			}
		}

		if(allBuffersValid){
			if(getErrorMessage() != null){
				setErrorMessage(null);
				setPageComplete(true);
			}
		}
		
		updateLocalMemorySize();
		updateNumDTEntries();
	}
	
	public boolean updateLocalMemorySize(){
		IWizardPage prevPage = getPreviousPage();
		if(prevPage == null || !(prevPage instanceof ALFWizardPageA))
			return true;
		
		ALFWizardPageA pageA = (ALFWizardPageA)prevPage;
		
		long usedMem = pageA.getExpStackSize();
		long dataTransferSize = 0;
		ALFBuffer tempBuffer;
		
		if(usedMem == -1)
			return true;
		
		for(int i = 0; i < alfBuffers.size(); i++){
			tempBuffer = (ALFBuffer)alfBuffers.get(i);
			dataTransferSize = ALFBufferValidator.computeDataTransferSize(tempBuffer.getElementType(), tempBuffer.getNumDimensions(), 
					tempBuffer.getDistributionSizeX(), tempBuffer.getDistributionSizeY(), tempBuffer.getDistributionSizeZ(), is64bit());
			if(dataTransferSize != -1)
				usedMem += dataTransferSize;
		}
		
		// set the remaing memory Text widget's text
		long remainingMem = TOTAL_AVAIL_SPU_MEMORY - usedMem;
		localMemoryRemaining.setText(remainingMem + " B"); //$NON-NLS-1$
		localMemoryRemaining.pack(true);
		
		// if remaining memory is less than 0, then set the text to red
		if(remainingMem < 0){
			localMemoryRemaining.setForeground(new Color(numDTEntry.getDisplay(), 255, 0, 0));
			return false;
		} else {
			localMemoryRemaining.setForeground(numDTEntry.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
			return true;
		}
	}
		
	/**
	 * Updates the "Number Data Transfer Entries" UI field, in case any existing buffers have been edited or any new buffers 
	 * have been created.
	 * 
	 * @return true if all existing buffers have the same number of data transfer entries (or there are no existing buffers) else
	 * return false.
	 */
	public boolean updateNumDTEntries(){
		// Set NumDTEntries text to "N/A" if no buffers are created yet
		if(alfBuffers == null || alfBuffers.size() == 0){
			numDTEntry.setText(Messages.ALFWizardPageB_numDTEntryStatusNA);
			numDTEntry.pack(true);
			numDTEntry.setForeground(numDTEntry.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
			return true;
		}
		
		ALFBuffer currentBuffer = (ALFBuffer)alfBuffers.get(0);
		long currentNumDT = currentBuffer.getNumberDTEntries();
		boolean valid = true;
		long newNumDT; 
		for(int i = 1; i < alfBuffers.size(); i++){
			currentBuffer = (ALFBuffer)alfBuffers.get(i);
			newNumDT = currentBuffer.getNumberDTEntries();
			if(newNumDT != currentNumDT){
				valid = false;
				break;
			} else {
				currentNumDT = newNumDT;
			}
		}
		
		if(valid){
			numDTEntry.setText(Messages.ALFWizardPageB_numDTEntryStatusEqual + " (" + currentNumDT + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			numDTEntry.pack(true);
			numDTEntry.setForeground(numDTEntry.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
			return true;
		} else {
			numDTEntry.setText(Messages.ALFWizardPageB_numDTEntryStatusUnequal);
			numDTEntry.pack(true);
			numDTEntry.setForeground(new Color(numDTEntry.getDisplay(), 255, 0, 0));
			return false;
		}
	}
	
	
}
