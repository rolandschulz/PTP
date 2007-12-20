/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.ibmll.ui.rmLaunchConfiguration;


import org.eclipse.swt.widgets.Widget;

public interface ValidationState
{
    public static final int UNCHANGED = 0;
    public static final int CHANGED = 1;
    public static final int IN_ERROR=2;
    

    /**
     * Set this object's validation state to indicate no validation is required
     */
    public void resetValidationState();
    
    /**
     * Set this object's validation state to indicate the value has changed and validation is required
     */
    public void setValidationRequired();
    
    /**
     * Set this object's validation state to indicate that the value is in error and must be validated again
     */
    public void setFieldInError();

    /**
     * Return status indicating if this object needs to be validated
     * @return - validation status
     */
    public boolean isValidationRequired();
    
    /** 
     * Return status indicating that the actual widget contained in this object is the widget that is being searched
     * for
     * @param source - The widget that is being searched for
     * @return - status indicating this object contains the matching widget.
     */
    public boolean isMatchingWidget(Widget source);
}
