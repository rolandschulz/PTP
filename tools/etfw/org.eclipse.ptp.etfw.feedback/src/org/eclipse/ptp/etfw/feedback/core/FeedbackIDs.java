/**********************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.feedback.core;

/**
 * Feedback view Plugin constants
 */
public interface FeedbackIDs
{
    // preference page name for XForm
    String FEEDBACK_PREF_NAME    = "Feedback";
    
    /** Marker ID - markers represent the info to display in the compiler xform view and hold the info we need */
     //String MARKER_ID       = "org.eclipse.ptp.etfw.feedback.core.XForm";
   
    /** View ID */
    String FEEDBACK_VIEW_ID = "org.eclipse.ptp.etfw.feedback.core.views.feedbackView";
    
    // attribute names
    String FEEDBACK_ATTR_NAME="name";
    String FEEDBACK_ATTR_ID="itemID";
    String FEEDBACK_ATTR_FILENAME="filename";
    String FEEDBACK_ATTR_PATHNAME="pathname";
    String FEEDBACK_ATTR_PARENT="parent";
    String FEEDBACK_ATTR_FUNCTION="functionCaller";
    String FEEDBACK_ATTR_FUNCTION_CALLEE="functionCallee";
    String FEEDBACK_ATTR_DESC="description";
	String FEEDBACK_ATTR_LOOP_ID = "LoopId";    //attr name in xml file AND attr name in marker
    //Note: we use IMarker.LINE_NUMBER too





}
