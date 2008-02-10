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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class FileSelectorRowWidget implements ValidationState
{
    private Label label;
    private Label nonDefaultMarker;
    private GridData gridData;
    private IAttributeDefinition<?, ?, ?> attr;
    private FileSelector widget;
    private String defaultValue;
    private EventMonitor eventMonitor;
    private int validationState;

    /**
     * Internal listener class for notifications when the value of the pathname field changes
     *
     */
    private class EventMonitor implements ModifyListener
    {
	public EventMonitor()
	{
	}

	/**
	 * Set the non-default indicator for this object when the pathname does not match the default pathname for this
	 * object
	 * @param e The notification event
	 */
	public void modifyText(ModifyEvent e)
	{
	    if (widget.getPath().equals(defaultValue)) {
		nonDefaultMarker.setText(" ");
	    } else {
		nonDefaultMarker.setText("*");
	    }
	}
    }

    /**
     * This class is a container for a text field and browse button combination used for file selection. When the browse
     * button is clicked, the registered SelectionListener will pop up a file or directory dialog prompting the user for
     * a path. When the user clicks ok in that dialog, the text field in this object is filled in with the selected
     * path. The browse button and text box are treated as a single object by the rest of the code in this file so that
     * they can be easily set to enabled or disabled state.
     * 
     */
    private class FileSelector
    {
	private Object attrValue;
	private Text path;
	private Button browseButton;

	/**
	 * Create the file selector object
	 * 
	 * @param parent Parent widget for the file selector
	 * @param selectorID Identifier for this file selector
	 * @param tooltipText Tooltip text for the selector
	 */
	public FileSelector(Composite parent, Integer selectorID, String tooltipText)
	{
	    GridData gd;

	    path = new Text(parent, SWT.SINGLE | SWT.BORDER);
	    path.setToolTipText(tooltipText);
	    gd = new GridData(GridData.FILL_HORIZONTAL);
	    gd.grabExcessHorizontalSpace = true;
	    path.setLayoutData(gd);
	    browseButton = new Button(parent, SWT.PUSH);
	    //TODO drw browseButton.setText(Messages.getString("FileSelector.BrowseButton"));
	    browseButton.setText("Browse");
	    browseButton.setData(WidgetAttributes.BUTTON_ID, new Integer(selectorID));
	    gd = new GridData(GridData.FILL_HORIZONTAL);
	    gd.grabExcessHorizontalSpace = false;
	    browseButton.setLayoutData(gd);

	}

	/**
	 * Set the file selector's enable state
	 * 
	 * @param enable Enable state
	 */
	public void setEnabled(boolean enable)
	{
	    path.setEnabled(enable);
	    browseButton.setEnabled(enable);
	}

	/**
	 * Determine the enable state for the file selector
	 * 
	 * @return enable state
	 */
	public boolean isEnabled()
	{
	    return path.isEnabled();
	}

	/**
	 * Set the path to be displayed in the text field of the file selector
	 * 
	 * @param pathname Pathname to be displayed
	 */
	public void setPath(String pathname)
	{
	    path.setText(pathname);
	}

	/**
	 * Get the specified path from the text field of the file selector
	 * 
	 * @return Pathname
	 */
	public String getPath()
	{
	    return path.getText();
	}

	/**
	 * Set focus on the text field within the selector widget
	 * 
	 * @return
	 */
	public boolean setFocus()
	{
	    return path.setFocus();
	}

	/**
	 * Set the 'data object' as if we were setting the ATTR_NAME object for a Widget object.
	 * 
	 * @param value - The value to be set
	 */
	public void setData(Object value)
	{
	    attrValue = value;
	}

	/**
	 * Return the 'data object' as if we were retrieving the ATTR_NAME object for a Widget object
	 * 
	 * @return - Value of the data object
	 */
	public Object getData()
	{
	    return attrValue;
	}

	public void addSelectionListener(SelectionListener listener)
	{
	    browseButton.addSelectionListener(listener);
	}

	public void addModifyListener(ModifyListener listener)
	{
	    path.addModifyListener(listener);
	}
	
	public boolean isMatchingWidget(Widget source)
	{
	    return path == source;
	}
    }

    /**
     * Create a file selector widget and related widgets for this class. This class assumes that the layout manager
     * for the parent Composite object is a GridLayout with 4 columns.
     * @param parent
     * @param id
     * @param selectorID
     * @param def
     */
    public FileSelectorRowWidget(Composite parent, String id, Integer selectorID, IAttributeDefinition<?, ?, ?> def)
    {
	attr = def;
	label = new Label(parent, SWT.NONE);
	//TODO drw label.setText(Messages.getString(attr.getName()));
	label.setText(attr.getName());
	nonDefaultMarker = new Label(parent, SWT.NONE);
	nonDefaultMarker.setText(" ");
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.grabExcessHorizontalSpace = false;
	label.setLayoutData(gridData);
	//TODO drw widget = new FileSelector(parent, selectorID, Messages.getString(attr.getDescription()));
	widget = new FileSelector(parent, selectorID,attr.getDescription());
	widget.setData(id);
	try {
	    defaultValue = attr.create().getValueAsString();
	}
	catch (IllegalValueException e) {
	    defaultValue = "";
	}
	eventMonitor = new EventMonitor();
	widget.addModifyListener(eventMonitor);
    }

    /**
     * Set the pathname for this object
     * @param path Pathname
     */
    public void setPath(String path)
    {
	widget.setPath(path);
    }

    /**
     * Set the enable state for this object
     * @param state The enable state
     */
    public void setEnabled(boolean state)
    {
	widget.setEnabled(state);
    }

    /**
     * Set the data value (attribute name) for this object
     * @param value The data value
     */
    public void setData(Object value)
    {
	widget.setData(value);
    }

    /**
     * Get the data value (attribute name) for this object
     * @return The data value
     */
    public Object getData()
    {
	return widget.getData();
    }

    /**
     * Get the pathname specified in this object
     * @return Pathname
     */
    public String getValue()
    {
	return widget.getPath().trim();
    }
    
    /**
     * Get the enable state for this object
     * @return The enable state
     */
    public boolean isEnabled()
    {
	return widget.isEnabled();
    }

    /**
     * Set focus to the text field within this object
     */
    public void setFocus()
    {
	widget.setFocus();
    }

    /**
     * Add a selection listener which will receive notifications when the browse button in this object has
     * been clicked.
     * @param listener The listener
     */
    public void addSelectionListener(SelectionListener listener)
    {
	widget.addSelectionListener(listener);
    }

    /**
     * Add a selection listener which will receive notifications when the path name field in this object has changed value
     * @param listener The listener
     */
    public void addModifyListener(ModifyListener listener)
    {
	widget.addModifyListener(listener);
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
