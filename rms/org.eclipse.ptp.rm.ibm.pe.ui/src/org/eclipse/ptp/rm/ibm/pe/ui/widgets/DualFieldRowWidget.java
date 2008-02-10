
/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.ui.widgets;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
//TODO drw import org.eclipse.ptp.ibmll.ui.internal.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class DualFieldRowWidget implements ValidationState
{
    private Label label;
    private Label nonDefaultMarker;
    private GridData gridData;
    private IAttributeDefinition<?, ?, ?> attr1;
    private IAttributeDefinition<?, ?, ?> attr2;
    private DualField widget;
    private String defaultValue[];
    private EventMonitor eventMonitor;
    private int validationState;

    /**
     * Internal listener class used to monitor changes in value for this object
     *
     */
    private class EventMonitor implements ModifyListener
    {
	public EventMonitor()
	{
	}

	/**
	 * Set the non-default indicator to '*' if the data value for this object does not match
	 * the default value from the attribute definition
	 * @param e The event indicating value has changed
	 */
	public void modifyText(ModifyEvent e)
	{
	    String value[];

	    value = widget.getText();
	    if ((value[0].equals(defaultValue[0])) && (value[1].equals(defaultValue[1]))) {
		nonDefaultMarker.setText(" ");
	    } else {
		nonDefaultMarker.setText("*");
	    }
	}

    }

    /**
     * This class implements a widget containing two text fields laid out horizontally. The intended use is for
     * attributes which may have two values, such as MP_BUFFER_SIZE, so that each field can be handled and validated
     * individually.
     */
    private class DualField
    {
	private Object attrValue1;
	private Object attrValue2;
	private Composite group;
	private Text value[];

	/**
	 * Create the DualField object
	 * 
	 * @param parent parent for this object
	 */
	public DualField(Composite parent)
	{
	    GridLayout layout;
	    GridData gd;

	    group = new Composite(parent, SWT.NONE);
	    layout = new GridLayout(2, true);
	    layout.marginWidth = 0;
	    layout.marginHeight = 0;
	    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
	    group.setLayout(layout);
	    value = new Text[2];
	    value[0] = new Text(group, SWT.SINGLE | SWT.BORDER);
	    value[0].setLayoutData(gd);
	    value[1] = new Text(group, SWT.SINGLE | SWT.BORDER);
	    value[1].setLayoutData(gd);

	}

	/**
	 * Set the layout data for this widget
	 * 
	 * @param gd Layout data
	 */
	public void setLayoutData(GridData gd)
	{
	    group.setLayoutData(gd);
	}

	/**
	 * Add a ModifyListener that will monitor the Text field for modifications
	 * 
	 * @param listener The ModifyListener
	 */
	public void addModifyListener(ModifyListener listener)
	{
	    value[0].addModifyListener(listener);
	    value[1].addModifyListener(listener);
	}

	/**
	 * Set tooltip text for each field of the widget
	 * 
	 * @param tip1 Tooltip text for first field
	 * @param tip2 Tooltip text for second field
	 */
	public void setToolTipText(String tip1, String tip2)
	{
	    value[0].setToolTipText(tip1);
	    value[1].setToolTipText(tip2);
	}

	/**
	 * Set initial values for each field in the widget
	 * 
	 * @param value1 Value for first field
	 * @param value2 Value for second field
	 */
	public void setText(String value1, String value2)
	{
	    value[0].setText(value1);
	    value[1].setText(value2);
	}

	/**
	 * Get the value pair for this widget
	 * 
	 * @return text values contained in text widgets
	 */
	public String[] getText()
	{
	    String textValue[];

	    textValue = new String[2];
	    textValue[0] = value[0].getText().trim();
	    textValue[1] = value[1].getText().trim();
	    return textValue;
	}

	/**
	 * Check if this widget is enabled
	 * 
	 * @return Enable state
	 */
	public boolean isEnabled()
	{
	    return value[0].isEnabled();
	}

	/**
	 * Set the enable state for this widget
	 * 
	 * @param state Enable state
	 */
	public void setEnabled(boolean state)
	{
	    value[0].setEnabled(state);
	    value[1].setEnabled(state);
	}

	/**
	 * Set the 'data object' as if we were setting the ATTR_NAME object for a Widget object.
	 * 
	 * @param value - The value to be set
	 */
	public void setData1(Object value)
	{
	    attrValue1 = value;
	}

	/**
	 * Return the 'data object' as if we were retrieving the ATTR_NAME object for a Widget object
	 * 
	 * @return - Value of the data object
	 */
	public Object getData1()
	{
	    return attrValue1;
	}

	/**
	 * Set the 'data object' as if we were setting the ATTR_NAME object for a Widget object.
	 * 
	 * @param value - The value to be set
	 */
	public void setData2(Object value)
	{
	    attrValue2 = value;
	}

	/**
	 * Return the 'data object' as if we were retrieving the ATTR_NAME object for a Widget object
	 * 
	 * @return - Value of the data object
	 */
	public Object getData2()
	{
	    return attrValue2;
	}
	
	public boolean isMatchingWidget(Widget source)
	{
	    return ((value[0] == source) || (value[1] == source));
	}
    }

    /**
     * Create a widget with two text entry fields representing the two sub-fields required by this widget.
     * This class assumes that the layout set for the parent Composite object is a GridLayout with four columns.
     * @param parent The parent widget
     * @param id1 The attribute name for the first field in this widget
     * @param id2 The attribute name for the second field in this widget
     * @param def1 The attribute definition for the first field in this widget
     * @param def2 The attribute definition for the second field in this widget
     */
    public DualFieldRowWidget(Composite parent, String id1, String id2, IAttributeDefinition<?, ?, ?> def1,
	    IAttributeDefinition<?, ?, ?> def2)
    {
	attr1 = def1;
	attr2 = def2;
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.grabExcessHorizontalSpace = false;
	label = new Label(parent, SWT.NONE);
	//TODO drw label.setText(Messages.getString(attr1.getName()));
	label.setText(attr1.getName());
	label.setLayoutData(gridData);
	nonDefaultMarker = new Label(parent, SWT.NONE);
	nonDefaultMarker.setText(" ");
	widget = new DualField(parent);
	//TODO drw widget.setToolTipText(Messages.getString(attr1.getDescription()), Messages.getString(attr2.getDescription()));
	widget.setToolTipText(attr1.getDescription(), attr2.getDescription());
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.horizontalSpan = 2;
	gridData.grabExcessHorizontalSpace = true;
	widget.setLayoutData(gridData);
	widget.setData1(id1);
	widget.setData2(id2);
	try {
	    defaultValue = new String[2];
	    defaultValue[0] = attr1.create().getValueAsString();
	    defaultValue[1] = attr2.create().getValueAsString();
	}
	catch (IllegalValueException e) {
	    defaultValue[0] = "";
	    defaultValue[1] = "";
	    ;
	}
	eventMonitor = new EventMonitor();
	widget.addModifyListener(eventMonitor);
    }

    /**
     * Add a modify listener to this object 
     * @param listener The listener object
     */
    public void addModifyListener(ModifyListener listener)
    {
	widget.addModifyListener(listener);
    }

    /**
     * Set the enable state for this widget
     * @param state The enable state
     */
    public void setEnabled(boolean state)
    {
	widget.setEnabled(state);
    }

    /**
     * Set the values for the two Text widgets contained in this object
     * @param value1 First text object's value
     * @param value2 Second text object's value
     */
    public void setValue(String value1, String value2)
    {
	widget.setText(value1, value2);
    }

    /** 
     * Get the data object (the attribute name) associated with the first Text widget in this object
     * @return The attribute name
     */
    public Object getData1()
    {
	return widget.getData1();
    }

    /** 
     * Get the data object (the attribute name) associated with the second Text widget in this object
     * @return The attribute name
     */
    public Object getData2()
    {
	return widget.getData2();
    }

    /**
     * Return a String array containing the values from the two Text objects contained in this object
     * @return Two element array containing the value from the two Text widgets contained in this object
     */
    public String[] getValue()
    {
	return widget.getText();
    }

    /**
     * Return the enable state for this object
     * @return The enable state
     */
    public boolean isEnabled()
    {
	return widget.isEnabled();
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
	return widget.isMatchingWidget(source);
    }
}
