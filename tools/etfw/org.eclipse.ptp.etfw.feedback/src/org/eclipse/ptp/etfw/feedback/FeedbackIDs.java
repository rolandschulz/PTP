/**********************************************************************
 * Copyright (c) 2009,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.feedback;

import org.eclipse.ptp.etfw.feedback.messages.Messages;

/**
 * Feedback view Plugin constants
 */
public interface FeedbackIDs
{
    // preference page name for XForm
    String FEEDBACK_PREF_NAME    = Messages.FeedbackIDs_feedback;
   
    /** View ID */
    String FEEDBACK_VIEW_ID = "org.eclipse.ptp.etfw.feedback.views.feedbackView"; //$NON-NLS-1$
    
    // attribute names
    String FEEDBACK_ATTR_NAME="name"; //$NON-NLS-1$
    String FEEDBACK_ATTR_ID="itemID"; //$NON-NLS-1$
    String FEEDBACK_ATTR_FILENAME="filename"; //$NON-NLS-1$
    String FEEDBACK_ATTR_PATHNAME="pathname"; //$NON-NLS-1$
    String FEEDBACK_ATTR_PARENT="parent"; //$NON-NLS-1$
    String FEEDBACK_ATTR_FUNCTION="functionCaller"; //$NON-NLS-1$
    String FEEDBACK_ATTR_FUNCTION_CALLEE="functionCallee"; // unused? others? //$NON-NLS-1$
    String FEEDBACK_ATTR_DESC="description"; //$NON-NLS-1$
 	String FEEDBACK_ATTR_LOOP_ID = "LoopId";    //attr name in xml file AND attr name in marker //$NON-NLS-1$
//    //Note: we use IMarker.LINE_NUMBER too





}
