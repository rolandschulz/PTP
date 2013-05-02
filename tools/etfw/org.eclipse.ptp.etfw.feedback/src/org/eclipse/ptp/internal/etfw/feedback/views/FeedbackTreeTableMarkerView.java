/**********************************************************************
 * Copyright (c) 2009,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.etfw.feedback.views;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.etfw.feedback.views.SimpleTreeTableMarkerView;
import org.eclipse.ptp.internal.etfw.feedback.Activator;
import org.eclipse.ptp.internal.etfw.feedback.FeedbackIDs;
import org.eclipse.ptp.internal.etfw.feedback.messages.Messages;
import org.eclipse.ptp.internal.etfw.feedback.preferences.PreferenceConstants;

/**
 * Expose information (e.g. from an XML file) in an eclipse view
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There is no guarantee that
 * this API will work or that it will remain the same. We do not recommending using this API without consulting with the
 * etfw.feedback team.
 * 
 * UNUSED?
 * 
 * @author beth tibbitts
 * 
 */
public class FeedbackTreeTableMarkerView extends SimpleTreeTableMarkerView {
	/**
	 * Assure that parent nodes will get created as needed
	 */
	private static boolean CREATE_PARENT_NODES = false;// Hack for hpcst 10/30/09

	/**
	 * This list tells the view which marker attributes map to each column in the view.
	 * These are the default values if we don't get one in the constructor, but I'm not sure
	 * this works to use these.
	 */
	private static String[] attrNames = new String[] { FeedbackIDs.FEEDBACK_ATTR_NAME, FeedbackIDs.FEEDBACK_ATTR_FILENAME,
			FeedbackIDs.FEEDBACK_ATTR_FUNCTION, // aka Caller
			FeedbackIDs.FEEDBACK_ATTR_FUNCTION_CALLEE, IMarker.LINE_NUMBER, /* IMarker.CHAR_START,IMarker.CHAR_END */
			FeedbackIDs.FEEDBACK_ATTR_DESC, FeedbackIDs.FEEDBACK_ATTR_LOOP_ID };
	private static String[] colNames;// =new String[] {"Type/Name","File","Caller(Target)","Callee(Src)", "Caller Line",;
	// /*"Char start","Char end"*/ "Description", "Loop ID"};
	private static int[] widths = new int[] { 160, 100, 100, 100, 80, 200, 60 };

	protected IPreferenceStore preferenceStore;
	protected boolean maintainExpandCollapseState;

	/**
	 * Use the ctor that allows an arbitrary number of extra columns.
	 * 
	 * @since 6.0
	 */
	public FeedbackTreeTableMarkerView(Activator plugin, String markerID) {
		super(plugin, "XForm", "XForms", attrNames, colNames, widths, //$NON-NLS-1$ //$NON-NLS-2$
				markerID, "parent", CREATE_PARENT_NODES); //$NON-NLS-1$
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

		infoBuffer.append(Messages.FeedbackTreeTableMarkerView_fileName).append(filename);

		infoBuffer.append(Messages.FeedbackTreeTableMarkerView_lineNumber).append(getStrAttr(marker, IMarker.LINE_NUMBER));
		infoBuffer.append(Messages.FeedbackTreeTableMarkerView_name).append(name);
		String parent = getStrAttr(marker, FeedbackIDs.FEEDBACK_ATTR_PARENT);
		infoBuffer.append(Messages.FeedbackTreeTableMarkerView_parentItemType).append(parent);
		infoBuffer.append(Messages.FeedbackTreeTableMarkerView_description).append(
				getStrAttr(marker, FeedbackIDs.FEEDBACK_ATTR_DESC));
		return infoBuffer.toString();
	}

	public String getStrAttr(IMarker marker, String attrName) {
		try {
			String str = marker.getAttribute(attrName).toString();

			return str;
		} catch (CoreException e) {
			System.out.println("Exception getting marker attr in CompilerXFormTreeTableView.getStrAttr() " + e.getMessage()); //$NON-NLS-1$
			return "*error*"; //$NON-NLS-1$
		}

	}

	@Override
	public String removeSpaces(String s) {
		StringTokenizer st = new StringTokenizer(s, " ", false); //$NON-NLS-1$
		String t = ""; //$NON-NLS-1$
		while (st.hasMoreElements()) {
			t += st.nextElement();
		}
		return t;
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
