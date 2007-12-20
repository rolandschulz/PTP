/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.ibmll.ui.rmLaunchConfiguration;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.ibmll.ui.internal.ui.Messages;
import org.eclipse.ptp.ibmll.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class TextRowWidget implements ValidationState
{
    private Label label;
    private Label nonDefaultMarker;
    private GridData gridData;
    private IAttributeDefinition<?, ?, ?> attrDef;
    private Text widget;
    private String defaultValue;
    private EventMonitor eventMonitor;
    private int validationState;

    /**
     * Internal class which listens for events indicating the data value for this object has changed
     *
     */
    private class EventMonitor implements ModifyListener
    {
	public EventMonitor()
	{
	}

	/**
	 * Change the non-default value indicator for this object to indicate whether the data value matches
	 * the default value from the attribute definition
	 * @param e The event indicating data value has changed 
	 */
	public void modifyText(ModifyEvent e)
	{
	    String value;
	    
	    value = widget.getText().trim();
	    if ((value.length () == 0) || (value.equals(defaultValue))) {
		nonDefaultMarker.setText(" ");
	    } else {
		nonDefaultMarker.setText("*");
	    }
	}
    }

    /**
     * Create a widget which is used as a text entry field in the launch configuration. This class assumes that
     * the layout manager set for the parent Composite object is a GridLayout widget with 4 columns
     * @param parent The parent Composite object
     * @param id The name of the attribute for this widget
     * @param def The attribute definition for this widget
     */
    public TextRowWidget(Composite parent, String id, IAttributeDefinition<?, ?, ?> def)
    {
	attrDef = def;
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.grabExcessHorizontalSpace = false;
	label = new Label(parent, SWT.NONE);
	label.setText(Messages.getString(attrDef.getName()));
	label.setLayoutData(gridData);
	nonDefaultMarker = new Label(parent, SWT.NONE);
	nonDefaultMarker.setText("*");
	widget = new Text(parent, SWT.SINGLE | SWT.BORDER);
	widget.setToolTipText(Messages.getString(attrDef.getDescription()));
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.horizontalSpan = 2;
	gridData.grabExcessHorizontalSpace = true;
	widget.setLayoutData(gridData);
	widget.setData(WidgetAttributes.ATTR_NAME, id);
	try {
	    defaultValue = attrDef.create().getValueAsString();
	}
	catch (IllegalValueException e) {
	    defaultValue = "";
	}
	eventMonitor = new EventMonitor();
	widget.addModifyListener(eventMonitor);
    }

    /**
     * Add a ModifyListener to this object
     * @param monitor the listening object
     */
    public void addModifyListener(ModifyListener monitor)
    {
	widget.addModifyListener(monitor);
    }

    /**
     * Get the data value for this object
     * @return The data value
     */
    public String getValue()
    {
	return widget.getText().trim();
    }

    /**
     * Set the data value for this object
     * @param value The data value
     */
    public void setValue(String value)
    {
	widget.setText(value);
    }

    /**
     * Get the data object (attribute name) stored in this widget
     * @param key The key to use in retrieving the data object
     * @return the data object
     */
    public String getData(String key)
    {
	return (String) widget.getData(key);
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
    
    public void setEnabled(boolean state)
    {
	widget.setEnabled(state);
    }
    
    public boolean isEnabled()
    {
	return widget.isEnabled();
    }

	public String getDefaultValue() {
		return defaultValue;
	}
}
