/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw.feedback.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.etfw.feedback.messages.messages"; //$NON-NLS-1$
	public static String FeedbackIDs_feedback;
	public static String FeedbackPreferencePage_maintainExpandCollapseState;
	public static String FeedbackPreferencePage_showNoItemsFoundDialog;
	public static String FeedbackPreferencePage_preferencesForFeedbackView;
	public static String FeedbackTreeTableMarkerView_description;
	public static String FeedbackTreeTableMarkerView_fileName;
	public static String FeedbackTreeTableMarkerView_lineNumber;
	public static String FeedbackTreeTableMarkerView_name;
	public static String FeedbackTreeTableMarkerView_parentItemType;
	public static String MarkerManager_solution;
	public static String MarkerManager_dontShowMeThisAgain;
	public static String MarkerManager_noFeedbackItemsFoundTitle;
	public static String MarkerManager_noFeedbackItemsFoundMessage;
	public static String PreferenceInitializer_defaultValue;
	public static String SimpleTreeTableMarkerView_areShownInThisView;
	public static String SimpleTreeTableMarkerView_areShownInThisViewPeriod;
	public static String SimpleTreeTableMarkerView_artifact;
	public static String SimpleTreeTableMarkerView_artifacts;
	public static String SimpleTreeTableMarkerView_collapseAll;
	public static String SimpleTreeTableMarkerView_collapseAllNodesInTheTree;
	public static String SimpleTreeTableMarkerView_constant;
	public static String SimpleTreeTableMarkerView_determineWhich;
	public static String SimpleTreeTableMarkerView_errorPositioningEditorFromMarkerLineNumber;
	public static String SimpleTreeTableMarkerView_expandAll;
	public static String SimpleTreeTableMarkerView_expandAllNodesInTheTree;
	public static String SimpleTreeTableMarkerView_filter;
	public static String SimpleTreeTableMarkerView_filterWhich;
	public static String SimpleTreeTableMarkerView_functionCall;
	public static String SimpleTreeTableMarkerView_information;
	public static String SimpleTreeTableMarkerView_no;
	public static String SimpleTreeTableMarkerView_NoFeedbackItemSelected;
	public static String SimpleTreeTableMarkerView_none;
	public static String SimpleTreeTableMarkerView_removeMarkers;
	public static String SimpleTreeTableMarkerView_selectAnItemInTheView;
	public static String SimpleTreeTableMarkerView_selected;
	public static String SimpleTreeTableMarkerView_showInfo;
	public static String SimpleTreeTableMarkerView_showInfoForSelected;
	public static String SimpleTreeTableMarkerView_value;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
