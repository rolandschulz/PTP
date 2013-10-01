/**********************************************************************
 * Copyright (c) 2013 The Board of Trustees of the University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * 	   NCSA - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.etfw.feedback.perfsuite;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.etfw.feedback.views.SimpleTreeTableMarkerView;
import org.eclipse.ptp.internal.etfw.feedback.FeedbackIDs;
import org.eclipse.ptp.internal.etfw.feedback.preferences.PreferenceConstants;

/**
 * Expose PerfSuite profile result in an eclipse view
 * 
 * @author Rui Liu
 * 
 */
public class PerfSuiteFeedbackView extends SimpleTreeTableMarkerView {
	/**
	 * Assure that parent nodes will get created as needed: that is,
	 * we don't have IFeedbackItem objects for the parents. Parent "nodes"
	 * are created for each unique parentID, thus "categorizing" the items
	 * automatically
	 */
	private static boolean CREATE_PARENT_NODES = true;

	/**
	 * Attribute names, used to store info in the marker, corresponding to each column in the view.
	 * Use these constants or values of your own.
	 * To add/remove a column, modify this: (1)
	 */
	private static String[] attrNames = new String[] {
			FeedbackIDs.FEEDBACK_ATTR_NAME,
			FeedbackIDs.FEEDBACK_ATTR_FILENAME,
			FeedbackIDs.FEEDBACK_ATTR_ID,
			IMarker.LINE_NUMBER,
			FeedbackIDs.FEEDBACK_ATTR_DESC };
	/** To add/remove a column, modify this: (2) */
	private static String[] colNames = new String[] { "Module", "File", "Function", "LineNo", "Number of Samples" };
	/** To add/remove a column, modify this: (3) */
	private static int[] widths = new int[] { 320, 160, 160, 70, 70 };

	protected IPreferenceStore preferenceStore;
	protected boolean maintainExpandCollapseState;

	/**
	 * Use the ctor that allows an arbitrary number of extra columns.
	 * This ctor is called (1).
	 */
	public PerfSuiteFeedbackView() {
		super(Activator.getDefault(), "profile", "lines", attrNames, colNames, widths,
				Activator.MARKER_ID, "parent", CREATE_PARENT_NODES);
		preferenceStore = Activator.getDefault().getPreferenceStore();
		maintainExpandCollapseState = preferenceStore.getBoolean(PreferenceConstants.P_MAINTAIN_EXPAND_COLLAPSE_STATE);

	}

	/**
	 * Provide something for the "Info" popup action, based on the marker.<br>
	 * Since we didn't use the Artifact, Artifact Manager, etc in the base class we need something to look useful here.
	 */
	@Override
	public String extractMarkerInfo(IMarker marker) {
		StringBuffer infoBuffer = new StringBuffer();
		String filename = marker.getResource().getName();
		String modulename = getStrAttr(marker, FeedbackIDs.FEEDBACK_ATTR_NAME);
		String functionname = getStrAttr(marker, FeedbackIDs.FEEDBACK_ATTR_ID);
		infoBuffer.append("\nDetails:\n");
		infoBuffer.append("\nModule name: ").append(modulename);
		infoBuffer.append("\nFile name: ").append(filename);
		infoBuffer.append("\nFunction name: ").append(functionname);
		infoBuffer.append("\nLine number: ").append(getStrAttr(marker, IMarker.LINE_NUMBER));
		infoBuffer.append("\n\nNumber of samples: ").append(getStrAttr(marker, FeedbackIDs.FEEDBACK_ATTR_DESC));
		return infoBuffer.toString();
	}

	public String getStrAttr(IMarker marker, String attrName) {
		try {
			String str = marker.getAttribute(attrName).toString();
			return str;
		} catch (CoreException e) {
			System.out.println("Exception getting marker attr in PerfSuiteFeedbackView.getStrAttr() " + e.getMessage());
			return "*error*";
		}

	}

	/**
	 * Maintain the user choice of whether to expand or collapse all entries.
	 */
	@Override
	protected void maintainExpandCollapseStatus() {
		// re-query each time, in case prefs have changed
		maintainExpandCollapseState = preferenceStore.getBoolean(PreferenceConstants.P_MAINTAIN_EXPAND_COLLAPSE_STATE);
		if (maintainExpandCollapseState) {
			switch (expandCollapseStatus) {
			case EXPAND_COLLAPSE_EXPANDALL:
				expandAllAction.run();
				break;
			case EXPAND_COLLAPSE_COLLAPSEALL:
				collapseAllAction.run();
				break;
			case EXPAND_COLLAPSE_NONE:
				// do nothing: user hasn't done expandAll or collapseAll yet.
			}
		}
	}

}
