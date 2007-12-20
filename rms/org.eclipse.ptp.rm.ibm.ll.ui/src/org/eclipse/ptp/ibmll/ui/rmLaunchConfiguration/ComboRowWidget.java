/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.ibmll.ui.rmLaunchConfiguration;

import java.util.Iterator;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringSetAttributeDefinition;
import org.eclipse.ptp.ibmll.ui.internal.ui.Messages;
import org.eclipse.ptp.ibmll.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

public class ComboRowWidget implements ValidationState
{

    private Label label;
    private Label nonDefaultMarker;
    private GridData gridData;
    private IAttributeDefinition<?, ?, ?> attrDef;
    private Combo widget;
    private String defaultValue;
    private EventMonitor eventMonitor;
    private int validationState;

    /**
     * Internal listener class used to monitor for changes in the data value of this object
     */
    private class EventMonitor implements ModifyListener, SelectionListener
    {
	public EventMonitor()
	{
	}

	/**
	 * Handle event notification indicating that the value entered in the text field of the widget has changed
	 * @param e The notification event
	 */
	public void modifyText(ModifyEvent e)
	{
	    setDefaultStatus();
	}

	/**
	 * Handle event notification indicating that a value has been selected from the Combo dropdown
	 * @param e The notification event
	 */
	public void widgetDefaultSelected(SelectionEvent e)
	{
	}

	public void widgetSelected(SelectionEvent e)
	{
	    setDefaultStatus();
	}
    }

    /**
     * Create an object containing a Combo widget and related widgets for use in a tab pabe. This class assumes that the
     * layout manager for the parent Composite widget is a GridLayout with 4 columns.
     * @param parent The parent Composite widget
     * @param id The name of the attribute associated with this object
     * @param def The attribute definition for the attribute associated with this object
     * @param readOnly Indicates whether the Combo widget is editable
     */
    public ComboRowWidget(Composite parent, String id, IAttributeDefinition<?, ?, ?> def, boolean readOnly, int selector_id)
    {
	Iterator<String> iter;

	attrDef = def;
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.grabExcessHorizontalSpace = false;
	label = new Label(parent, SWT.NONE);
	label.setText(Messages.getString(attrDef.getName()));
	label.setLayoutData(gridData);
	nonDefaultMarker = new Label(parent, SWT.NONE);
	nonDefaultMarker.setText(" ");
	if (readOnly) {
	    widget = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
	} else {
	    widget = new Combo(parent, SWT.DROP_DOWN);
	}
	iter = ((StringSetAttributeDefinition) def).getValues().iterator();
	while (iter.hasNext()) {
	    widget.add(iter.next());
	}
	// Even though setToolTipText is called, the tooltip text does not
	// appear when this is run on Linux. The problem seems to be
	// setting tooltip text on a read-only combobox. Editable comboboxes
	// work correctly. Maybe a Linux implementation bug since this works
	// on Windows XP.
	widget.setToolTipText(Messages.getString(attrDef.getDescription()));
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.horizontalSpan = 2;
	gridData.grabExcessHorizontalSpace = true;
	widget.setLayoutData(gridData);
	widget.setData(WidgetAttributes.ATTR_NAME, id);
    widget.setData(WidgetAttributes.BUTTON_ID, selector_id);
	try {
	    defaultValue = attrDef.create().getValueAsString();
	}
	catch (IllegalValueException e) {
	    defaultValue = "";
	}
	eventMonitor = new EventMonitor();
	widget.addModifyListener(eventMonitor);
	widget.addSelectionListener(eventMonitor);
    }

    /** 
     * Add a selection listener to this object for notification that a value was selected from the Combo dropdown
     * @param listener The listener object
     */
    public void addSelectionListener(SelectionListener listener)
    {
	widget.addSelectionListener(listener);
    }

    /**
     * Add a modify listener to this object for notification that the value in the Combo's text entry field has changed
     * @param listener The listener object
     */
    public void addModifyListener(ModifyListener listener)
    {
	widget.addModifyListener(listener);
    }

    /**
     * Get the data value from the Combo widget
     * @return The data value
     */
    public String getValue()
    {
	return widget.getText().trim();
    }

    /**
     * Set the data value in the Combo widget.
     * @param value Data value
     */
    public void setValue(String value)
    {
	widget.setText(value);
    }

    /**
     * Get the data value associated with the specified key
     * @param key The key to be used
     * @return The data value associated with the key
     */
    public String getData(String key)
    {
	return (String) widget.getData(key);
    }

    /**
     * Get the enable state for the object 
     * @return the enable state
     */
    public boolean isEnabled()
    {
	return widget.isEnabled();
    }

    /**
     * Set the non-default status flag to show whether the data value for this object is equal to the default value
     * for the attribute.
     */
    protected void setDefaultStatus()
    {
	String value;
	
	value = widget.getText().trim();
	if ((value.length() == 0) || (value.equals(defaultValue))) {
	    nonDefaultMarker.setText(" ");
	} else {
	    nonDefaultMarker.setText("*");
	}
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

	public String getDefaultValue() {
		return defaultValue;
	}
}
