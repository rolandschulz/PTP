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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.cell.alf.ui.Messages;
import org.eclipse.ptp.cell.alf.ui.core.ALFBuffer;
import org.eclipse.ptp.cell.alf.ui.core.ALFBufferValidator;
import org.eclipse.ptp.cell.alf.ui.core.ALFConstants;
import org.eclipse.ptp.cell.alf.ui.debug.Debug;
import org.eclipse.ptp.utils.ui.swt.ToolKit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


/**
 * This class implements the dialog window that will open when the user wishes to add a new buffer input
 * 
 * @author Sean Curry
 * @since 3.0.0
 */
public class ALFBufferDialog extends Dialog implements SelectionListener {
	
	/* the WizardPage (ALFWizardPageB) that created this ALFBufferDialog */
	private WizardPage parentPage;
	
	/* the ALFBuffer object that will be created after the user has filled in all necessary information and presses OK */
	private ALFBuffer alfBuffer;
	
	/* the ALFBufferValidator that will be used to validate the input fields */
	private ALFBufferValidator validator;
	
	/* the title of the dialog */
	private String title;

    /* widgets that make up the fields of this NewBufferDialog */
	private Text bufferName;
	private Text elementType;
	private Combo elementUnit;
	private Combo bufferType;
	private Combo numDimensions;
	private Text dimensionSizeX;
	private Text dimensionSizeY;
	private Text dimensionSizeZ;
	private Combo distributionModelX;
	private Combo distributionModelY;
	private Combo distributionModelZ;
	private Text distributionSizeX;
	private Text distributionSizeY;
	private Text distributionSizeZ;
	
	private Text distributionModelHelpText;
	
	/* error message text widget */
	private Text errorMsgText;
	
	/**
	 * Creates an ALFBuffer dialog with OK and Cancel buttons. Note that the dialog
     * will have no visual representation (no widgets) until it is told to open.
     * <p>
     * Note that the <code>open</code> method blocks for input dialogs.
     * </p>
	 * 
	 * @param parentShell the parent shell, or <code>null</code> to create a top-level shell
	 * @param dialogTitle the dialog title, or <code>null</code> if none
	 * @param buffer a non-null ALFBuffer if this dialog window is being used to edit an existing ALFBuffer's configuration,
	 * 			or <code>null</code> if this dialog window is being used to create a new ALFBuffer object
	 * @param bufferValidator an ALFBufferValidator used to validate the fields that the user is filling in
	 */
	public ALFBufferDialog(Shell parentShell, String dialogTitle, ALFBuffer buffer, ALFBufferValidator bufferValidator, WizardPage parentPage) {
		super(parentShell);
		
		this.title = dialogTitle;
		this.validator = bufferValidator;
		this.alfBuffer = buffer;
		this.parentPage = parentPage;
	}
	
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
    }
	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	public void create(){
		super.create();
		
		validateInput();
	}
	
	/* 
	 * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }
	
	/*
	 * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */	
	protected Control createDialogArea(Composite parent){
		Font font = parent.getFont();
		
		// create composite to hold all of this dialogs widgets
		Composite composite = (Composite) super.createDialogArea(parent);
		
		initializeDialogUnits(composite);
		
		/* create all of the widgets that will make up the dialog page */
		
		// create name of buffer label and text widgets		
		bufferName = ToolKit.createTextWithLabel(composite, Messages.ALFBufferDialog_variableNameLabelMessage, null, 0);
		
		// create the label and text widgets for the element type
		elementType = ToolKit.createTextWithLabel(composite, Messages.ALFBufferDialog_elementTypeLabelMessage, null, 0);
		
		// create the label and text widgets for the element unit
		Composite row = new Composite(composite, SWT.NONE);
		GridLayout layout2 = new GridLayout();
		layout2.numColumns=2;		
		row.setLayout(layout2);
		row.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		
		Label elementUnitLabel = new Label(row, SWT.WRAP | SWT.LEFT);
		elementUnitLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		elementUnitLabel.setFont(font);
		elementUnitLabel.setText(Messages.ALFBufferDialog_elementUnitLabelMessage);
		
		elementUnit = new Combo(row, SWT.READ_ONLY | SWT.DROP_DOWN);
		elementUnit.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER));
		elementUnit.setFont(font);
		elementUnit.add(Messages.ALFBufferDialog_elementUnitByte, ALFConstants.ALF_DATA_BYTE);
		elementUnit.add(Messages.ALFBufferDialog_elementUnitInt16, ALFConstants.ALF_DATA_INT16);
		elementUnit.add(Messages.ALFBufferDialog_elementUnitInt32, ALFConstants.ALF_DATA_INT32);
		elementUnit.add(Messages.ALFBufferDialog_elementUnitInt64, ALFConstants.ALF_DATA_INT64);
		elementUnit.add(Messages.ALFBufferDialog_elementUnitFloat, ALFConstants.ALF_DATA_FLOAT);
		elementUnit.add(Messages.ALFBufferDialog_elementUnitDouble, ALFConstants.ALF_DATA_DOUBLE);
		elementUnit.add(Messages.ALFBufferDialog_elementUnitAddr32, ALFConstants.ALF_DATA_ADDR32);
		elementUnit.add(Messages.ALFBufferDialog_elementUnitAddr64, ALFConstants.ALF_DATA_ADDR64);
		elementUnit.add(Messages.ALFBufferDialog_elementUnitType, ALFConstants.ALF_DATA_ELEMENT_TYPE);
		elementUnit.select(ALFConstants.ALF_DATA_ELEMENT_TYPE);		
		
		// create the label and combo widgets for the buffer type	
		Composite row2 = new Composite(composite, SWT.NONE);		
		row2.setLayout(layout2);
		row2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));

		Label bufferTypeLabel = new Label(row2, SWT.WRAP | SWT.LEFT);
		bufferTypeLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		bufferTypeLabel.setFont(font);
		bufferTypeLabel.setText(Messages.ALFBufferDialog_bufferTypeLabelMessage + ':');
		
		bufferType = new Combo(row2, SWT.READ_ONLY | SWT.DROP_DOWN);
		bufferType.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER));
		bufferType.setFont(font);
		bufferType.add(Messages.ALFBufferDialog_bufferTypeInput, ALFConstants.ALF_BUFFER_INPUT);
		bufferType.add(Messages.ALFBufferDialog_bufferTypeOutput, ALFConstants.ALF_BUFFER_OUTPUT);
		bufferType.select(ALFConstants.ALF_BUFFER_INPUT);

		
		// create the label and combo widgets for the dimension size
		Composite row3 = new Composite(composite, SWT.NONE);
		row3.setLayout(layout2);
		row3.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		
		Label dimensionSizeLabel = new Label(row3, SWT.WRAP | SWT.LEFT);
		dimensionSizeLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		dimensionSizeLabel.setFont(font);
		dimensionSizeLabel.setText(Messages.ALFBufferDialog_dimensionSizeLabelMessage + ':');
		
		numDimensions = new Combo(row3, SWT.READ_ONLY | SWT.DROP_DOWN);
		numDimensions.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER));
		numDimensions.setFont(font);
		numDimensions.add(Messages.ALFBufferDialog_oneDimensionMessage, ALFConstants.ONE_DIMENSIONAL);
		numDimensions.add(Messages.ALFBufferDialog_twoDimensionMessage, ALFConstants.TWO_DIMENSIONAL);
		numDimensions.add(Messages.ALFBufferDialog_threeDimensionMessage, ALFConstants.THREE_DIMENSIONAL);
		numDimensions.select(ALFConstants.ONE_DIMENSIONAL);
		
		
		// create the widgets for the dimension sizes of X, Y, and Z
		Group dimensionSizeGroup = new Group(composite, SWT.NONE);
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 1;
		dimensionSizeGroup.setLayout(layout1);
		dimensionSizeGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		dimensionSizeGroup.setText(Messages.ALFBufferDialog_dimensionSizeGroupMessage + ':');
		
		Composite dimensionSizeComposite = new Composite(dimensionSizeGroup, SWT.NONE);
		GridLayout layout6 = new GridLayout();
		layout6.numColumns = 6;
		dimensionSizeComposite.setLayout(layout6);
		dimensionSizeComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		dimensionSizeComposite.setFont(font);
		
		dimensionSizeX = ToolKit.createTextWithLabel(dimensionSizeComposite, Messages.ALFBufferDialog_XLabelMessage, "0", 0); //$NON-NLS-1$
		dimensionSizeX.setEnabled(true);
		dimensionSizeX.setEditable(true);
		dimensionSizeX.setText("1"); //$NON-NLS-1$
		
		dimensionSizeY = ToolKit.createTextWithLabel(dimensionSizeComposite, Messages.ALFBufferDialog_YLabelMessage, "0", 0); //$NON-NLS-1$
		dimensionSizeY.setEnabled(false);
		dimensionSizeY.setEditable(true);
		dimensionSizeY.setText("1"); //$NON-NLS-1$
		
		dimensionSizeZ = ToolKit.createTextWithLabel(dimensionSizeComposite, Messages.ALFBufferDialog_ZLabelMessage, "0", 0); //$NON-NLS-1$
		dimensionSizeZ.setEnabled(false);
		dimensionSizeZ.setEditable(true);
		dimensionSizeZ.setText("1"); //$NON-NLS-1$

		
		// create the widgets for the distribution model fields
		Group distributionGroup = new Group(composite, SWT.NONE);
		distributionGroup.setLayout(layout1);
		distributionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		distributionGroup.setText(Messages.ALFBufferDialog_distributionModelGroupMessage + ':');
		
		Composite distributionComposite = new Composite(distributionGroup, SWT.NONE);
		distributionComposite.setLayout(layout6);
		distributionComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		
		Label distributionXLabel = new Label(distributionComposite, SWT.WRAP | SWT.LEFT);
		distributionXLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		distributionXLabel.setFont(font);
		distributionXLabel.setText(Messages.ALFBufferDialog_XLabelMessage + ':'); 
		distributionModelX = new Combo(distributionComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		distributionModelX.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER));
		distributionModelX.setFont(font);
		distributionModelX.add(Messages.ALFBufferDialog_distributionModelStar, ALFConstants.DIST_MODEL_STAR);
		distributionModelX.add(Messages.ALFBufferDialog_distributionModelBlock, ALFConstants.DIST_MODEL_BLOCK);
		distributionModelX.add(Messages.ALFBufferDialog_distributionModelCyclic, ALFConstants.DIST_MODEL_CYCLIC);
		distributionModelX.select(ALFConstants.DIST_MODEL_STAR);
		distributionModelX.setEnabled(true);
		
		Label distributionYLabel = new Label(distributionComposite, SWT.WRAP | SWT.LEFT);
		distributionYLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		distributionYLabel.setFont(font);
		distributionYLabel.setText(Messages.ALFBufferDialog_YLabelMessage + ':');
		distributionModelY = new Combo(distributionComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		distributionModelY.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER));
		distributionModelY.setFont(font);
		distributionModelY.add(Messages.ALFBufferDialog_distributionModelStar, ALFConstants.DIST_MODEL_STAR);
		distributionModelY.add(Messages.ALFBufferDialog_distributionModelBlock, ALFConstants.DIST_MODEL_BLOCK);
		distributionModelY.add(Messages.ALFBufferDialog_distributionModelCyclic, ALFConstants.DIST_MODEL_CYCLIC);
		distributionModelY.select(ALFConstants.DIST_MODEL_STAR);
		distributionModelY.setEnabled(false);
		
		Label distributionZLabel = new Label(distributionComposite, SWT.WRAP | SWT.LEFT);
		distributionZLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
		distributionZLabel.setFont(font);
		distributionZLabel.setText(Messages.ALFBufferDialog_ZLabelMessage + ':');
		distributionModelZ = new Combo(distributionComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		distributionModelZ.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER));
		distributionModelZ.setFont(font);
		distributionModelZ.add(Messages.ALFBufferDialog_distributionModelStar, ALFConstants.DIST_MODEL_STAR);
		distributionModelZ.add(Messages.ALFBufferDialog_distributionModelBlock, ALFConstants.DIST_MODEL_BLOCK);
		distributionModelZ.select(ALFConstants.DIST_MODEL_STAR);
		distributionModelZ.setEnabled(false);
		
		distributionModelHelpText = ALFWizard.createWhatsThisHelpText(distributionGroup, getShell(), parentPage.getWizard(), "data/distribution_model_help.html", 570, 500); //$NON-NLS-1$
		distributionModelHelpText.getShell();
		
		// create the widgets for the distribution size fields 
		Group distributionSizeGroup = new Group(composite, SWT.NONE);
		distributionSizeGroup.setLayout(layout1);
		distributionSizeGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		distributionSizeGroup.setText(Messages.ALFBufferDialog_distributionSizeGroupMessage + ':');
		
		Composite distributionSizeComposite = new Composite(distributionSizeGroup, SWT.NONE);
		distributionSizeComposite.setLayout(layout6);
		distributionSizeComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		
		distributionSizeX = ToolKit.createTextWithLabel(distributionSizeComposite, Messages.ALFBufferDialog_XLabelMessage, "0", 0); //$NON-NLS-1$
		distributionSizeX.setEnabled(true);
		distributionSizeX.setEditable(false);
		distributionSizeX.setText("1"); //$NON-NLS-1$
		
		distributionSizeY = ToolKit.createTextWithLabel(distributionSizeComposite, Messages.ALFBufferDialog_YLabelMessage, "0", 0); //$NON-NLS-1$
		distributionSizeY.setEnabled(false);
		distributionSizeY.setEditable(false);
		distributionSizeY.setText("1"); //$NON-NLS-1$

		distributionSizeZ = ToolKit.createTextWithLabel(distributionSizeComposite, Messages.ALFBufferDialog_ZLabelMessage, "0", 0); //$NON-NLS-1$
		distributionSizeZ.setEnabled(false);
		distributionSizeZ.setEditable(false);
		distributionSizeZ.setText("1"); //$NON-NLS-1$

		// create text widget for the error message, and set font to red italic
		errorMsgText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
		GridData errorData = new GridData(GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
		errorData.minimumHeight = 85;
        errorMsgText.setLayoutData(errorData);
        errorMsgText.setBackground(errorMsgText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        Color errorMsgColor = new Color(font.getDevice(), new RGB(255, 0,0));
        errorMsgText.setFont(font);
        errorMsgText.setForeground(errorMsgColor);
		
        if(alfBuffer != null){
        	populateFields();
        }
        
        // Now add all of the listeners, since the fields might have been modified in populateFields()
        bufferName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					validateInput();
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
		});
		elementType.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					validateInput();
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
		});
        elementUnit.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) { /* do nothing */ }
			public void widgetSelected(SelectionEvent e) { 
				try {
					validateInput(); 
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
		});
        numDimensions.addSelectionListener(this);
        dimensionSizeX.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				try {
				/* if the selected distribution model is "BLOCK" or "*", then the distribution size field needs to be updated automatically, 
				so call "distModelChanged", which will cause the distribution size field to be updated according to the new dimension size value */				
				if((distributionModelX.getSelectionIndex() == ALFConstants.DIST_MODEL_STAR) || (distributionModelX.getSelectionIndex() == ALFConstants.DIST_MODEL_BLOCK))
					distModelChanged(distributionModelX);
				validateInput();
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
		});
        dimensionSizeY.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				try {
				/* if the selected distribution model is "BLOCK" or "*", then the distribution size field needs to be updated automatically, 
				so call "distModelChanged", which will cause the distribution size field to be updated according to the new dimension size value */				
				if((distributionModelY.getSelectionIndex() == ALFConstants.DIST_MODEL_STAR) || (distributionModelY.getSelectionIndex() == ALFConstants.DIST_MODEL_BLOCK))
					distModelChanged(distributionModelY);				
				validateInput();
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
		});
        dimensionSizeZ.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				try {
				/* if the selected distribution model is "BLOCK" or "*", then the distribution size field needs to be updated automatically, 
				so call "distModelChanged", which will cause the distribution size field to be updated according to the new dimension size value */				
				if((distributionModelZ.getSelectionIndex() == ALFConstants.DIST_MODEL_STAR) || (distributionModelZ.getSelectionIndex() == ALFConstants.DIST_MODEL_BLOCK))
					distModelChanged(distributionModelZ);
				validateInput();
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
		});
        distributionModelX.addSelectionListener(this);
        distributionModelY.addSelectionListener(this);
        distributionModelZ.addSelectionListener(this);
        distributionSizeX.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				try {
					if(distributionModelX.getSelectionIndex() == ALFConstants.DIST_MODEL_CYCLIC)
						validateInput();
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}			
		});
        distributionSizeY.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				try {
					if(distributionModelY.getSelectionIndex() == ALFConstants.DIST_MODEL_CYCLIC)
						validateInput();
				} catch (Exception ee) {
					Debug.POLICY.logError(ee);
				}
			}
		});

		return composite;

	}

	/*
	public void disposeWidgets(){
		bufferName.dispose();
		elementType.dispose();
		elementUnit.dispose();
		bufferType.dispose();
		numDimensions.dispose();
		dimensionSizeX.dispose();
		dimensionSizeY.dispose();
		dimensionSizeZ.dispose();
		distributionModelX.dispose();
		distributionModelY.dispose();
		distributionModelZ.dispose();
		distributionSizeX.dispose();
		distributionSizeY.dispose();
		distributionSizeZ.dispose();
		errorMsgText.dispose();
	}
	*/

	public ALFBuffer getBuffer(){
		return alfBuffer;
	}

	protected void cancelPressed(){
		//disposeWidgets();
		super.cancelPressed();
	}
	
	/*
	 * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */	
	protected void okPressed(){
		Control button = getButton(IDialogConstants.OK_ID);
		String name, type, aligned;
		
		validateInput();
		if(!button.isEnabled())
			return;
		
		// check if the buffer name field has been filled in yet. if not, show error message and do not create the buffer object
		name = bufferName.getText().trim();
		if(name == null || name.length() == 0){
			setErrorMessage(Messages.ALFBufferDialog_errorMsgBufferName);
			return;
		}
		
		// check if the element type field has been filling in yet. if not, show error message and do not create the buffer object
		type = elementType.getText().trim();
		if(type == null || type.length() == 0){
			setErrorMessage(Messages.ALFBufferDialog_errorMsgElementType);
			return;
		}
		
		ALFWizardPageB pageB = (ALFWizardPageB)parentPage;
		aligned = ALFBufferValidator.is16ByteAligned(type, numDimensions.getSelectionIndex(), 
				Long.parseLong(distributionSizeX.getText().trim()), Long.parseLong(distributionSizeY.getText().trim()), Long.parseLong(distributionSizeZ.getText().trim()), pageB.is64bit());
		
		if(aligned != null){
			final MessageDialog dialog = new MessageDialog(getShell(), title, null, aligned, MessageDialog.QUESTION, 
					new String[]{Messages.ALFWizard_yes, Messages.ALFWizard_no}, 0);

			// Run in syncExec because callback is from an operation,
	        // which is probably not running in the UI thread.
	        getShell().getDisplay().syncExec(new Runnable() {
	            public void run() {
	                dialog.open();
	            }
	        });

	        int returnCode = dialog.getReturnCode();
	        if(returnCode == 1)
	        	return;
		}
		
		if(alfBuffer == null){
			alfBuffer = new ALFBuffer(bufferName.getText().trim(), elementType.getText().trim(), elementUnit.getSelectionIndex(), bufferType.getSelectionIndex(), numDimensions.getSelectionIndex(),
					Long.parseLong(dimensionSizeX.getText().trim()), Long.parseLong(dimensionSizeY.getText().trim()), Long.parseLong(dimensionSizeZ.getText().trim()), 
					distributionModelX.getSelectionIndex(), distributionModelY.getSelectionIndex(),	distributionModelZ.getSelectionIndex(), 
					Long.parseLong(distributionSizeX.getText().trim()), Long.parseLong(distributionSizeY.getText().trim()), Long.parseLong(distributionSizeZ.getText().trim()), true);
		} else {
			alfBuffer.setName(bufferName.getText());
			alfBuffer.setElementType(elementType.getText());
			alfBuffer.setElementUnit(elementUnit.getSelectionIndex());
			alfBuffer.setBufferType(bufferType.getSelectionIndex());
			alfBuffer.setNumDimensions(numDimensions.getSelectionIndex());
			alfBuffer.setDimensionSizeX(Long.parseLong(dimensionSizeX.getText()));
			alfBuffer.setDimensionSizeY(Long.parseLong(dimensionSizeY.getText()));
			alfBuffer.setDimensionSizeZ(Long.parseLong(dimensionSizeZ.getText()));
			alfBuffer.setDistributionModelX(distributionModelX.getSelectionIndex());
			alfBuffer.setDistributionModelY(distributionModelY.getSelectionIndex());
			alfBuffer.setDistributionModelZ(distributionModelZ.getSelectionIndex());
			alfBuffer.setDistributionSizeX(Long.parseLong(distributionSizeX.getText()));
			alfBuffer.setDistributionSizeY(Long.parseLong(distributionSizeY.getText()));
			alfBuffer.setDistributionSizeZ(Long.parseLong(distributionSizeZ.getText()));
			alfBuffer.setIsValid(true);
		}
		
		setReturnCode(Dialog.OK);
		//disposeWidgets();
		close();
	}
	
	/**
	 * Fills in this ALFBuffers fields with the values from the existing ALFBuffer object that was passed into this dialog's constructor.
	 *
	 */
	private void populateFields(){
		// If the alfBuffer object is null, then the user is attempting to create a new buffer. so the fields will take on their default values
		if(alfBuffer == null){
			return;
		}
		
		// Since the alfBuffer object is not null, the user is trying to edit an existing buffer object, so fill in the fields with the existing objects values
		bufferName.setText(alfBuffer.getName());
		elementType.setText(alfBuffer.getElementType());
		elementUnit.select(alfBuffer.getElementUnit());
		bufferType.select(alfBuffer.getBufferType());
		numDimensions.select(alfBuffer.getNumDimensions());
		dimensionSizeX.setText(alfBuffer.getDimensionSizeX() + ""); //$NON-NLS-1$
		dimensionSizeY.setText(alfBuffer.getDimensionSizeY() + ""); //$NON-NLS-1$
		dimensionSizeZ.setText(alfBuffer.getDimensionSizeZ() + "");	//$NON-NLS-1$
		distributionModelX.select(alfBuffer.getDistributionModelX());
		distributionModelY.select(alfBuffer.getDistributionModelY());
		distributionModelZ.select(alfBuffer.getDistributionModelZ());
		distributionSizeX.setText(alfBuffer.getDistributionSizeX() + ""); //$NON-NLS-1$
		distributionSizeY.setText(alfBuffer.getDistributionSizeY() + ""); //$NON-NLS-1$
		distributionSizeZ.setText(alfBuffer.getDistributionSizeZ() + ""); //$NON-NLS-1$
		
		switch(alfBuffer.getNumDimensions()){
			case ALFConstants.THREE_DIMENSIONAL:
				dimensionSizeZ.setEnabled(true);
				distributionModelZ.setEnabled(true);
				distributionSizeZ.setEnabled(true);
				if(distributionModelZ.getSelectionIndex() == ALFConstants.DIST_MODEL_CYCLIC)
					distributionSizeZ.setEditable(true);
			case ALFConstants.TWO_DIMENSIONAL:
				dimensionSizeY.setEnabled(true);
				distributionModelY.setEnabled(true);
				distributionSizeY.setEnabled(true);
				if(distributionModelY.getSelectionIndex() == ALFConstants.DIST_MODEL_CYCLIC)
					distributionSizeY.setEditable(true);
			case ALFConstants.ONE_DIMENSIONAL:
				dimensionSizeX.setEnabled(true);
				distributionModelX.setEnabled(true);
				distributionSizeX.setEnabled(true);
				if(distributionModelX.getSelectionIndex() == ALFConstants.DIST_MODEL_CYCLIC)
					distributionSizeX.setEditable(true);
		}
	}

	/**
     * Sets or clears the error message.
     * If not <code>null</code>, the OK button is disabled.
     * 
     * @param errorMessage
     *            the error message, or <code>null</code> to clear
     * @since 3.0
     */
	public void setErrorMessage(String errorMessage){
		if (errorMsgText != null && !errorMsgText.isDisposed()) {
    		errorMsgText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
    		errorMsgText.getParent().update();
    		
    		// if the error message has been set to null, then enable the OK button
    		Control button = getButton(IDialogConstants.OK_ID);
    		if (button != null) {
    			// if there is an error message, and its length is at least 5 chars (to make sure an indexOutOfBoundsException doesn't occur),
    			// then check if the error message really is an error message, or a warning message. if the message does not have the string "Error"
    			// as the first 5 characters, then its a warning message, and the OK button needs to be enabled.
    			if(errorMessage != null && errorMessage.length() >= 9){
    				String messageType = errorMessage.substring(0, 9);
    				if(messageType.equalsIgnoreCase(Messages.ALFBufferValidator_warningMsg))
    					button.setEnabled(true);
    				else
    					button.setEnabled(false);
    			}
    			else{
    				button.setEnabled(errorMessage == null);
    			}
    		}
    	
    	}
	}

	/**
     * Validates the buffers fields.
     * <p>
     * The default implementation of this framework method delegates the request
     * to the supplied ALFBuffer validator object; if it finds the input invalid,
     * the error message is displayed in the dialog's message line. This hook
     * method is called whenever any field is modified.
     * </p>
     */
    protected void validateInput() {
        setErrorMessage(validator.isValid(bufferName.getText().trim(), elementType.getText().trim(), elementUnit.getSelectionIndex(), bufferType.getSelectionIndex(), numDimensions.getSelectionIndex(),
				dimensionSizeX.getText().trim(), dimensionSizeY.getText().trim(), dimensionSizeZ.getText().trim(),
				distributionModelX.getSelectionIndex(), distributionModelY.getSelectionIndex(),	distributionModelZ.getSelectionIndex(),
				distributionSizeX.getText().trim(), distributionSizeY.getText().trim(), distributionSizeZ.getText().trim()));
    }

    /*
     * (non-Javadoc)
	 * @see org.eclipse.swt.internal.SWTEventListener.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) { /* do nothing */ }
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.internal.SWTEventListener.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		try {
			Widget source = e.widget;
			
			// If the user has changed the number of dimensions, the widgets for each dimension must be enabled or disabled accordingly
			if(source == numDimensions){
				numDimensionsChanged();
			}
	
			// If the user has changed the distribution model for any of the 3 dimensions, the distribution size fields must be updated accordingly
			if(source == distributionModelX || source == distributionModelY || source == distributionModelZ){
				distModelChanged(source);
			}
			
			// If the user has changed the size of a dimension, then the distribution model size may need to be updated accordingly
			if(source == dimensionSizeX){
				distModelChanged(distributionModelX);
			}
			else if(source == dimensionSizeY){
				distModelChanged(distributionModelY);
			}
			else if(source == dimensionSizeZ){
				distModelChanged(distributionModelZ);
			}
			
			validateInput();
		} catch (Exception ee) {
			Debug.POLICY.logError(ee);
		}
	}
	
	private void numDimensionsChanged(){
		if(numDimensions.getSelectionIndex() == ALFConstants.ONE_DIMENSIONAL){
			// If 1-D array selected, then disable all Y and Z dimension fields
			dimensionSizeY.setEnabled(false);
			dimensionSizeZ.setEnabled(false);
			distributionModelY.setEnabled(false);
			distributionModelZ.setEnabled(false);
			distributionSizeY.setEnabled(false);
			distributionSizeZ.setEnabled(false);
			
		}
		else if(numDimensions.getSelectionIndex() == ALFConstants.TWO_DIMENSIONAL){
			// If 2-D array is selected, then enable all Y dimension fields, and disable all Z dimension fields	
			dimensionSizeY.setEnabled(true);
			dimensionSizeZ.setEnabled(false);
			distributionModelY.setEnabled(true);
			distributionModelZ.setEnabled(false);
			distributionSizeY.setEnabled(true);
			distributionSizeZ.setEnabled(false);
		}
		else if(numDimensions.getSelectionIndex() == ALFConstants.THREE_DIMENSIONAL){
			// If 3-D array is selected, then enable all Y and Z dimension fields
			dimensionSizeY.setEnabled(true);
			dimensionSizeZ.setEnabled(true);
			distributionModelY.setEnabled(true);
			distributionModelZ.setEnabled(true);
			distributionSizeY.setEnabled(true);
			distributionSizeZ.setEnabled(true);
		}
	}
	
	private void distModelChanged(Widget source){
		int expAccelNum = validator.getExpectedAccelNum();
		
		if(source == distributionModelX){
			// If distribution model is "*", then distribution size = dimension size
			if(distributionModelX.getSelectionIndex() == ALFConstants.DIST_MODEL_STAR){
				distributionSizeX.setText(dimensionSizeX.getText());
				distributionSizeX.setEditable(false);
			}
			// If distribution model is "BLOCK", then distribution size = (dimension size) / (num of expected accelerator nodes)
			else if(distributionModelX.getSelectionIndex() == ALFConstants.DIST_MODEL_BLOCK){
				long dimSizeX = Long.parseLong(dimensionSizeX.getText().trim());
				if(expAccelNum != 0 && dimSizeX > 0){
					long newDistributionSizeX = dimSizeX / expAccelNum;
					distributionSizeX.setText(newDistributionSizeX + ""); //$NON-NLS-1$
				} else {
					distributionSizeX.setText("0"); //$NON-NLS-1$
				}
				distributionSizeX.setEditable(false);
			}
			// If distribution model is "CYCLIC", then the user input a distribution size that is between 1 and dimension size
			else if(distributionModelX.getSelectionIndex() ==ALFConstants. DIST_MODEL_CYCLIC){
				distributionSizeX.setText("1"); //$NON-NLS-1$
				distributionSizeX.setEditable(true);
			}
		}
		else if(source == distributionModelY){
			if(distributionModelY.getSelectionIndex() == ALFConstants.DIST_MODEL_STAR){
				distributionSizeY.setText(dimensionSizeY.getText());
				distributionSizeY.setEditable(false);	
			}
			else if(distributionModelY.getSelectionIndex() == ALFConstants.DIST_MODEL_BLOCK){
				long dimSizeY = Long.parseLong(dimensionSizeY.getText().trim());
				if(expAccelNum != 0 && dimSizeY > 0){
					long newDistributionSizeY = dimSizeY / expAccelNum;
					distributionSizeY.setText(newDistributionSizeY + ""); //$NON-NLS-1$
				} else {
					distributionSizeY.setText("0"); //$NON-NLS-1$
				}
				distributionSizeY.setEditable(false);
			}
			else if(distributionModelY.getSelectionIndex() == ALFConstants.DIST_MODEL_CYCLIC){
				distributionSizeY.setText("1"); //$NON-NLS-1$
				distributionSizeY.setEditable(true);
			}
		}
		else if(source == distributionModelZ){
			if(distributionModelZ.getSelectionIndex() == ALFConstants.DIST_MODEL_STAR){
				distributionSizeZ.setText(dimensionSizeZ.getText());
				distributionSizeZ.setEditable(false);
			}
			else if(distributionModelZ.getSelectionIndex() == ALFConstants.DIST_MODEL_BLOCK){
				if(expAccelNum != 0){
					long newDistributionSizeZ = Long.parseLong(dimensionSizeZ.getText()) / expAccelNum;
					distributionSizeZ.setText(newDistributionSizeZ + ""); //$NON-NLS-1$
				} else {
					distributionSizeZ.setText("0"); //$NON-NLS-1$
				}
				distributionSizeZ.setEditable(false);
			}
		}
	}
}
