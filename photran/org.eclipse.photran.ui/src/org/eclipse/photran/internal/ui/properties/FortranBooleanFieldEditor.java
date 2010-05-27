/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.properties;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class FortranBooleanFieldEditor extends BooleanFieldEditor
{
    //DO NOT SET THIS VALUE TO ANYTHING!! This field is initialized via getChangeControl(Composite parent)
    // which is called from super() constructor. Since fields are initialized AFTER the super() constructor
    // is called, if you set this to, let's say NULL, then AFTER it was already initialized to a proper value
    // by the super() constructor, it will get re-set to NULL, and you don't want that. So, don't set this
    // field to anything!
    private Button myCheckBox;
    
    public FortranBooleanFieldEditor(String enableVpgPropertyName, 
        String string,
        Composite composite)
    {
        super(enableVpgPropertyName, string, composite);
    }

    ////// !! Copied from BooleanFieldEditor !! /////
    /**
     * Returns the change button for this field editor.
     * @param parent The Composite to create the receiver in.
     *
     * @return the change button
     */
    @Override protected Button getChangeControl(Composite parent) 
    {
        if (myCheckBox == null) {
            myCheckBox = new Button(parent, SWT.CHECK | SWT.LEFT);
            myCheckBox.setFont(parent.getFont());
            myCheckBox.addSelectionListener(new SelectionAdapter() {
                @Override public void widgetSelected(SelectionEvent e) {
                    boolean isSelected = myCheckBox.getSelection();
                    valueChanged(!isSelected, isSelected);
                }
            });
            myCheckBox.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    myCheckBox = null;
                }
            });
        } else {
            checkParent(myCheckBox, parent);
        }
        return myCheckBox;
    }  
    
    public void setValue(boolean value)
    {
        if(myCheckBox != null)
            myCheckBox.setSelection(value);
    }
}