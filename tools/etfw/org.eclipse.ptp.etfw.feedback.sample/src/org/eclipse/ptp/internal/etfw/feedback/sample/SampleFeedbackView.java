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

package org.eclipse.ptp.internal.etfw.feedback.sample;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.etfw.feedback.views.SimpleTreeTableMarkerView;
import org.eclipse.ptp.internal.etfw.feedback.preferences.PreferenceConstants;

/**
 * Expose xlC compiler transformation information in an eclipse view
 * 
 * @author beth
 * 
 */
public class SampleFeedbackView extends SimpleTreeTableMarkerView {
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
			FeedbackIDs.FEEDBACK_ATTR_FUNCTION_CALLEE,
			IMarker.LINE_NUMBER, // Note: currently column 5 (index=4) must be an int value, presumably line number.
									// This restriction will be lifted and generalized later.
			/* IMarker.CHAR_START,IMarker.CHAR_END */
			FeedbackIDs.FEEDBACK_ATTR_DESC };
	/** To add/remove a column, modify this: (2) */
	private static String[] colNames = new String[] { "Type/Name", "File", "ID", "blank", "LineNo",
			/* "Char start","Char end" */"Description*" };
	/** To add/remove a column, modify this: (3) */
	private static int[] widths = new int[] { 160, 100, 50, 50, 80, 200 };

	protected IPreferenceStore preferenceStore;
	protected boolean maintainExpandCollapseState;

	/**
	 * Use the ctor that allows an arbitrary number of extra columns.
	 * This ctor is called (1). FIXME need to generalize the name "Xform" used here?
	 */
	public SampleFeedbackView() {
		super(Activator.getDefault(), "XForm", "XForms", attrNames, colNames, widths,
				Activator.MARKER_ID, "parent", CREATE_PARENT_NODES);
		preferenceStore = Activator.getDefault().getPreferenceStore();
		maintainExpandCollapseState = preferenceStore.getBoolean(PreferenceConstants.P_MAINTAIN_EXPAND_COLLAPSE_STATE);

	}

	/**
	 * Provide something for the "Info" popup action, based on the marker <br>
	 * Since we didn't use the Artifact, Artifact Manager, etc in the base class we need something to look useful here.
	 */
	@Override
	public String extractMarkerInfo(IMarker marker) {
		StringBuffer infoBuffer = new StringBuffer();
		String filename = marker.getResource().getName();
		String name = getStrAttr(marker, FeedbackIDs.FEEDBACK_ATTR_NAME);
		infoBuffer.append("\nThis information provided by SampleFeedbackView.extractMarkerInfo()");
		infoBuffer.append("\nFile name: ").append(filename);

		infoBuffer.append("\nLine number: ").append(getStrAttr(marker, IMarker.LINE_NUMBER));
		infoBuffer.append("\nName: ").append(name);
		String parent = getStrAttr(marker, FeedbackIDs.FEEDBACK_ATTR_PARENT);
		infoBuffer.append("\nParent (Item type): ").append(parent);
		infoBuffer.append("\nDescription: ").append(getStrAttr(marker, FeedbackIDs.FEEDBACK_ATTR_DESC));
		return infoBuffer.toString();
	}

	public String getStrAttr(IMarker marker, String attrName) {
		try {
			String str = marker.getAttribute(attrName).toString();

			return str;
		} catch (CoreException e) {
			System.out.println("Exception getting marker attr in CompilerXFormTreeTableView.getStrAttr() " + e.getMessage());
			return "*error*";
		}

	}

	/**
	 * 
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
