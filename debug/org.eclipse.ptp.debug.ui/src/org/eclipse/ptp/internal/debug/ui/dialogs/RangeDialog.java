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
package org.eclipse.ptp.internal.debug.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Clement chu
 *
 */
public class RangeDialog extends Dialog {
	private Group rowGroup = null;
	private Group colGroup = null;
	private IntegerFieldEditor rowFromField = null;
	private IntegerFieldEditor rowToField = null;
	private IntegerFieldEditor colFromField = null;
	private IntegerFieldEditor colToField = null;
	private Text errorMessageText = null;
	private int maxRow = 0;
	private int maxCol = 0;
	private int fromRow = 0;
	private int toRow = 0;
	private int fromCol = 0;
	private int toCol = 0;
	
    public RangeDialog(Shell parentShell, int maxCol, int maxRow) {
        super(parentShell);
        this.maxCol = maxCol;
        this.toCol = maxCol;
        this.maxRow = maxRow;
        this.toRow = maxRow;
    }
    public int getFromRow() {
    	return fromRow;
    }
    public int getToRow() {
    	return toRow;
    }
    public int getFromCol() {
    	return fromCol;
    }
    public int getToCol() {
    	return toCol;
    }
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	fromRow = rowFromField.getIntValue();
            toRow = rowToField.getIntValue();
        	fromCol = colFromField.getIntValue();
            toCol = colToField.getIntValue();
        }        
        super.buttonPressed(buttonId);
    }
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Range setting"); //$NON-NLS-1$
    }
    protected void createButtonsForButtonBar(Composite parent) {
    	super.createButtonsForButtonBar(parent);
    	getOkButton().setEnabled(false);
    }
    public Button getOkButton() {
        return getButton(IDialogConstants.OK_ID);
    }
    protected Control createDialogArea(Composite parent) {
    	Composite composite = (Composite)super.createDialogArea(parent);

    	colGroup = new Group(composite, SWT.NONE);
        colGroup.setText(Messages.RangeDialog_0);
        colGroup.setLayout(new FillLayout());
        colGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
       	colFromField = new RangeIntegerFieldEditor("colFromField", Messages.RangeDialog_1, colGroup); //$NON-NLS-1$
       	colFromField.setPropertyChangeListener(listener);
       	colToField = new RangeIntegerFieldEditor("colToField", Messages.RangeDialog_2, colGroup); //$NON-NLS-1$
       	colToField.setPropertyChangeListener(listener);
       	
    	rowGroup = new Group(composite, SWT.NONE);
        rowGroup.setText(Messages.RangeDialog_3);
        rowGroup.setLayout(new FillLayout());
        rowGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
       	rowFromField = new RangeIntegerFieldEditor("rowFromField", Messages.RangeDialog_1, rowGroup); //$NON-NLS-1$
       	rowFromField.setPropertyChangeListener(listener);
       	rowToField = new RangeIntegerFieldEditor("rowToField", Messages.RangeDialog_2, rowGroup); //$NON-NLS-1$
       	rowToField.setPropertyChangeListener(listener);
       	
        errorMessageText = new Text(composite, SWT.READ_ONLY);
        errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        return composite;
    }
    
    public int open(boolean showCol, boolean showRow, int fromCol, int toCol, int fromRow, int toRow) {
    	this.fromCol = fromCol;
    	this.toCol = (toCol==0?maxCol:toCol);
    	this.fromRow = fromRow;
    	this.toRow = toRow==0?maxRow:toRow;
    	return open(showCol, showRow);
    }

    private int open(boolean showCol, boolean showRow) {
    	create();
    	colGroup.setEnabled(showCol);
       	colFromField.setEnabled(showCol, colGroup);
       	colToField.setEnabled(showCol, colGroup);
    	rowGroup.setEnabled(showRow);
    	rowFromField.setEnabled(showRow, rowGroup);
    	rowToField.setEnabled(showRow, rowGroup);
       	colFromField.setStringValue(""+fromCol); //$NON-NLS-1$
       	colToField.setStringValue(Messages.RangeDialog_12+toCol);
       	rowFromField.setStringValue(Messages.RangeDialog_13+fromRow);
       	rowToField.setStringValue(Messages.RangeDialog_14+toRow);
    	return open();
    }
    
    private IPropertyChangeListener listener = new IPropertyChangeListener() {
    	public void propertyChange(PropertyChangeEvent event) {
    		boolean hasError = false;
    		if (colGroup.isEnabled()) {
	    		hasError = (!colFromField.isValid() || !colToField.isValid());
	    		if (!hasError) {
	    			int fromValue = colFromField.getIntValue();
	    			int toValue = colToField.getIntValue();
	    			hasError = (fromValue < 0 || fromValue >= toValue || toValue > maxCol);
	    		}
    		}
    		if (rowGroup.isEnabled() && !hasError) {
	    		hasError = (!rowFromField.isValid() || !rowToField.isValid());
	    		if (!hasError) {
	    			int fromValue = rowFromField.getIntValue();
	    			int toValue = rowToField.getIntValue();
	    			hasError = (fromValue < 0 || fromValue >= toValue || toValue > maxRow);
	    		}
    		}
    		getOkButton().setEnabled(!hasError);
    		errorMessageText.setText(hasError?Messages.RangeDialog_15:Messages.RangeDialog_16);
    	}
    };
    
    private class RangeIntegerFieldEditor extends IntegerFieldEditor {
    	private RangeIntegerFieldEditor(String name, String label, Composite parent) {
    		super(name, label, parent);
    	}
        protected void createControl(Composite parent) {
            GridLayout layout = new GridLayout();
            layout.numColumns = getNumberOfControls();
            layout.marginWidth = 5;
            layout.marginHeight = 5;
            layout.horizontalSpacing = HORIZONTAL_GAP;
            parent.setLayout(layout);
            doFillIntoGrid(parent, layout.numColumns);
        }    	
    }
}
