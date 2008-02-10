/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.ui.widgets;

import java.util.List;

import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringSetAttributeDefinition;
//TODO drw import org.eclipse.ptp.ibmll.ui.internal.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

public class BooleanRowWidget implements ValidationState
{
    private Label label;
    private Label nonDefaultMarker;
    private GridData gridData;
    private StringSetAttributeDefinition attrDef;
    private BooleanOption widget;
    private String defaultValue;
    private EventMonitor eventMonitor;
    private int validationState;

    /**
     * Internal listener class for events needed for notification of changes in data value for this widget
     *
     */
    private class EventMonitor implements SelectionListener
    {
	public EventMonitor()
	{
	}

	public void widgetDefaultSelected(SelectionEvent e)
	{
	}

	/**
	 * Set the non-default value indicator if the widget's value does not match the attribute's default
	 * value
	 * @param e The selection event indicating this object was clicked
	 */
	public void widgetSelected(SelectionEvent e)
	{
	    Button selectedButton;

	    selectedButton = (Button) e.getSource();
	    if (selectedButton.getText().equals(defaultValue)) {
		nonDefaultMarker.setText(" ");
	    } else {
		nonDefaultMarker.setText("*");
	    }
	}
    }

    private class BooleanOption
    {
	private String attrValue;
	private Composite group;
	private Button buttons[];

	/**
	 * Create a radio button pair for use in selecting a boolean option. This object is a container for a pair of
	 * radio buttons in a group box.
	 * 
	 * @param parent The parent widget for this object
	 * @param labels A list of two labels used to label the radio buttons
	 */
	public BooleanOption(Composite parent, List<String> labels, int button_id)
	{
	    GridLayout layout;

	    if (labels.size() != 2) {
		throw new IllegalArgumentException("BooleanOption requires two labels");
	    }
	    group = new Composite(parent, SWT.NONE);
	    layout = new GridLayout(2, true);
	    layout.marginHeight = 0;
	    layout.marginWidth = 0;
	    group.setLayout(layout);
	    buttons = new Button[2];
	    buttons[0] = new Button(group, SWT.RADIO);
	    buttons[0].setText(labels.get(0));
	    buttons[0].setData(WidgetAttributes.BUTTON_ID, button_id);
	    buttons[1] = new Button(group, SWT.RADIO);
	    buttons[1].setText(labels.get(1));
	    buttons[1].setData(WidgetAttributes.BUTTON_ID, button_id);
	}

	/**
	 * Set the tooltip text for the object. The tooltip text is set on the group object
	 * 
	 * @param text The tooltip text
	 */
	public void setToolTipText(String text)
	{
	    group.setToolTipText(text);
	}

	/**
	 * Set the selection state. The button with text matching the value string is set selected
	 * 
	 * @param value The value to be selected.
	 */
	public void setSelectedButton(String value)
	{
	    if (buttons[0].getText().equals(value)) {
		buttons[0].setSelection(true);
	    } else {
		buttons[1].setSelection(true);
	    }
	}

	/**
	 * Get the value for the selected button. The value returned is the label for the selected button.
	 * 
	 * @return Value for this object
	 */
	public String getValue()
	{
	    if (buttons[0].getSelection()) {
		return buttons[0].getText();
	    }
	    return buttons[1].getText();
	}

	/**
	 * Set the layout data for this object
	 * 
	 * @param gd Layout data
	 */
	public void setLayoutData(GridData gd)
	{
	    group.setLayoutData(gd);
	}

	/**
	 * Set the enable state for the Button objects in this object to the specified state 
	 * @param state Enable state for this widget
	 */
	public void setEnabled(boolean state)
	{
	    buttons[0].setEnabled(state);
	    buttons[1].setEnabled(state);
	}

	/**
	 * Determine if this widget is in the enabled state
	 * @return Enable state
	 */
	public boolean isEnabled()
	{
	    return buttons[0].isEnabled();
	}

	/**
	 * Set the 'data object' as if we were setting the ATTR_NAME object for a Widget object.
	 * 
	 * @param value - The value to be set
	 */
	public void setData(String value)
	{
	    attrValue = value;
	}

	/**
	 * Return the 'data object' as if we were retrieving the ATTR_NAME object for a Widget object
	 * 
	 * @return - Value of the data object
	 */
	public String getData()
	{
	    return attrValue;
	}

	/**
	 * Add a selection listener to the Button objects contained in this object
	 * @param listener The selection listener
	 */
	public void addSelectionListener(SelectionListener listener)
	{
	    buttons[0].addSelectionListener(listener);
	    buttons[1].addSelectionListener(listener);
	}
	
	public boolean isMatchingWidget(Widget source)
	{
	    return ((source == buttons[0]) || (source == buttons[1]));
	}
    }

    /**
     * Create the widgets contained within this row. This class assumes that the layout manager for the
     * Composite parent is a GridLayout with four columns.
     * @param parent The Composite object that contains this widget
     * @param id The attribute name for this widget
     * @param def The attribute definition for this widget
     */
    public BooleanRowWidget(Composite parent, String id, StringSetAttributeDefinition def, int button_id)
    {
	attrDef = def;
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.grabExcessHorizontalSpace = false;
	label = new Label(parent, SWT.NONE);
	//TODO drw label.setText(Messages.getString(attrDef.getName()));
	label.setText(attrDef.getName());
	label.setLayoutData(gridData);
	nonDefaultMarker = new Label(parent, SWT.NONE);
	nonDefaultMarker.setText(" ");
	widget = new BooleanOption(parent, attrDef.getValues(), button_id);
	//TODO drw widget.setToolTipText(Messages.getString(attrDef.getDescription()));
	widget.setToolTipText(attrDef.getDescription());
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.grabExcessHorizontalSpace = true;
	gridData.horizontalSpan = 2;
	widget.setLayoutData(gridData);
	widget.setData(id);
	try {
	    defaultValue = attrDef.create().getValueAsString();
	}
	catch (IllegalValueException e) {
	    defaultValue = "";
	}
	eventMonitor = new EventMonitor();
	widget.addSelectionListener(eventMonitor);
    }

    /**
     * Add a selection listener to this object. This method is a wrapper that just calls 
     * addSelectionListener for the BooleanOption widget contained in this class
     * @param listener The selection listener
     */
    public void addSelectionListener(SelectionListener listener)
    {
	widget.addSelectionListener(listener);
    }

    /**
     * Set the enable state for the BooleanOption object contained in this object
     * @param state The enable state
     */
    public void setEnabled(boolean state)
    {
	widget.setEnabled(state);
    }

    /**
     * Set the value for the BooleanOption object contained in this object. The result is that the radio button with
     * label text matching the value parameter is set selected.
     * @param value The value to set
     */
    public void setValue(String value)
    {
	widget.setSelectedButton(value);
    }

    /**
     * Get the attribute name stored in this object.
     * @return The attribute name
     */
    public String getData()
    {
	return widget.getData();
    }

    /**
     * Get the value of the BooleanOption object contained in this object. THe returned value is the label text of the
     * radio button in selected state.
     * @return The object's value
     */
    public String getValue()
    {
	return widget.getValue();
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
