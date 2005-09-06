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
package org.eclipse.ptp.debug.internal.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
	
    public RangeDialog(Shell parentShell, int maxRow, int maxCol) {
        super(parentShell);
        this.maxRow = maxRow;
        this.maxCol = maxCol;
        this.toRow = maxRow;
        this.toCol = maxCol;
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
        shell.setText(DialogMessages.getString("RangeDialog.group"));
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
        colGroup.setText(DialogMessages.getString("RangeDialog.column"));
        colGroup.setLayout(new FillLayout());
        colGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
       	colFromField = new RangeIntegerFieldEditor("colFromField", DialogMessages.getString("RangeDialog.from"), colGroup);
       	colFromField.setPropertyChangeListener(listener);
       	colToField = new RangeIntegerFieldEditor("colToField", DialogMessages.getString("RangeDialog.to"), colGroup);
       	colToField.setPropertyChangeListener(listener);
       	
    	rowGroup = new Group(composite, SWT.NONE);
        rowGroup.setText(DialogMessages.getString("RangeDialog.row"));
        rowGroup.setLayout(new FillLayout());
        rowGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
       	rowFromField = new RangeIntegerFieldEditor("rowFromField", DialogMessages.getString("RangeDialog.from"), rowGroup);
       	rowFromField.setPropertyChangeListener(listener);
       	rowToField = new RangeIntegerFieldEditor("rowToField", DialogMessages.getString("RangeDialog.to"), rowGroup);
       	rowToField.setPropertyChangeListener(listener);
       	
        errorMessageText = new Text(composite, SWT.READ_ONLY);
        errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        return composite;
    }
    
    public int open(boolean showRow, boolean showCol, int fromRow, int toRow, int fromCol, int toCol) {
    	this.fromRow = fromRow;
    	this.toRow = toRow==0?maxRow:toRow;
    	this.fromCol = fromCol;
    	this.toCol = (toCol==0?maxCol:toCol);
    	return open(showRow, showCol);
    }

    private int open(boolean showRow, boolean showCol) {
    	create();
    	rowGroup.setEnabled(showRow);
    	rowFromField.setEnabled(showRow, rowGroup);
    	rowToField.setEnabled(showRow, rowGroup);
    	colGroup.setEnabled(showCol);
       	colFromField.setEnabled(showCol, colGroup);
       	colToField.setEnabled(showCol, colGroup);
       	rowFromField.setStringValue(""+fromRow);
       	rowToField.setStringValue(""+toRow);
       	colFromField.setStringValue(""+fromCol);
       	colToField.setStringValue(""+toCol);
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
    		errorMessageText.setText(hasError?"Invalid input":"");
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
