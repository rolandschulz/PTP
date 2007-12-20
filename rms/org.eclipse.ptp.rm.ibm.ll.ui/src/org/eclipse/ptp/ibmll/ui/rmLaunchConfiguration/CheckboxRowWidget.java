/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.ibmll.ui.rmLaunchConfiguration;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.ibmll.ui.internal.ui.Messages;
import org.eclipse.ptp.ibmll.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

public class CheckboxRowWidget implements ValidationState
{

    private Label label;
    private Label fill;
    private Label nonDefaultMarker;
    private GridData gridData;
    private IAttributeDefinition<?, ?, ?> attrDef;
    private Button widget;
    private String defaultValue;
    private int validationState;

    /**
     * Create a checkbox widget and associated widgets. This class assumes that the layout manager for the 
     * parent Composite object is a GridLayout with 4 columns. 
     * @param parent The parent Composite widget
     * @param id The name of the attribute associated with this object
     * @param def The attribute definition for the attribute associated with this object 
     */
    public CheckboxRowWidget(Composite parent, String id, IAttributeDefinition<?, ?, ?> def)
    {
	attrDef = def;
	label = new Label(parent, SWT.NONE);
	label.setText(Messages.getString(def.getName()));
	nonDefaultMarker = new Label(parent, SWT.NONE);
	nonDefaultMarker.setText(" ");
	widget = new Button(parent, SWT.CHECK);
	widget.setToolTipText(Messages.getString(def.getDescription()));
	widget.setText("");
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.grabExcessHorizontalSpace = true;
	widget.setLayoutData(gridData);
	widget.setData(WidgetAttributes.ATTR_NAME, id);
	fill = new Label(parent, SWT.NONE);
	fill.setText("");
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.grabExcessHorizontalSpace = false;
	fill.setLayoutData(gridData);
    }

    /**
     * Add a selection listener for this object
     * @param listener The listener
     */
    public void addSelectionListener(SelectionListener listener)
    {
	widget.addSelectionListener(listener);
    }

    /**
     * Set the value of a data object in this class using the associated key
     * @param key The data object key
     * @param value The value to set
     */
    public void setData(String key, Object value)
    {
	widget.setData(key, value);
    }

    /**
     * Return status indicating if this object is in the selected state
     * @return selection state
     */
    public boolean getSelection()
    {
	return widget.getSelection();
    }

    /**
     * Set the selection state (check mark) for this widget
     * @param state The state to be set
     */
    public void setSelection(boolean state)
    {
	widget.setSelection(state);
    }

    /**
     * Get the value associated with the specified key
     * @param key Key for data to be retrieved
     * @return The data value
     */
    public String getData(String key)
    {
	return (String) widget.getData(key);
    }
    /** 
     * Return status indicating that the actual widget contained in this object is the widget that is being searched
     * for
     * @param source - The widget that is being searched for
     * @return - status indicating this object contains the matching widget.
     */
    public boolean isMatchingWidget(Widget source)
    {
	return widget == source;
    }

    /**
     * Set this object's validation state to indicate no validation is required
     */
    public void resetValidationState()
    {
	validationState = ValidationState.UNCHANGED;
    }

    /**
     * Set this object's validation state to indicate that the value is in error and must be validated again
     */
    public void setFieldInError()
    {
	validationState = ValidationState.IN_ERROR;
    }

    /**
     * Set this object's validation state to indicate the value has changed and validation is required
     */
    public void setValidationRequired()
    {
	validationState = ValidationState.CHANGED;
    }

    /**
     * Return status indicating if this object needs to be validated
     * @return - validation status
     */
    public boolean isValidationRequired()
    {
	return ((validationState == ValidationState.CHANGED) || (validationState == ValidationState.IN_ERROR));
    }

}
