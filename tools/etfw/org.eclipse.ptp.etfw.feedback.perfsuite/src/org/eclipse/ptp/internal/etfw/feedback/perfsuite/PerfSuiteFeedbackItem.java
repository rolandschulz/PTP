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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackItem;
import org.eclipse.ptp.etfw.feedback.IFeedbackItem;
import org.eclipse.ptp.internal.etfw.feedback.FeedbackIDs;

/**
 * @author Rui Liu
 * 
 */
public class PerfSuiteFeedbackItem extends AbstractFeedbackItem implements IFeedbackItem {
	private final String name; // module name
	private final String parentID;
	private final String id; // function name
	private final String description; // number of samples
	private final String filename; // file name
	private final long lineNo; // line number

	/**
	 * map of values by attribute name, that the view LabelProvider will want to ask for
	 */
	Map<String, String> map = new HashMap<String, String>();

	public PerfSuiteFeedbackItem(String name, String parentID, String id, String filename, long lineNo, String description) {
		this.name = name;
		this.parentID = parentID;
		this.id = id;
		this.filename = filename;
		this.lineNo = lineNo;
		this.description = description;

		map.put(FeedbackIDs.FEEDBACK_ATTR_NAME, name);
		map.put(FeedbackIDs.FEEDBACK_ATTR_FILENAME, filename);
		map.put(FeedbackIDs.FEEDBACK_ATTR_ID, id);
		map.put(IMarker.LINE_NUMBER, Long.toString(lineNo));
		map.put(FeedbackIDs.FEEDBACK_ATTR_DESC, description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#getChildren()
	 */
	public List<IFeedbackItem> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#getFile()
	 */
	public String getFile() {
		return filename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#getID()
	 */
	public String getID() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#getLineNoStart()
	 */
	public int getLineNoStart() {
		return (int) this.lineNo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#getParentID()
	 */
	public String getParentID() {
		return parentID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#hasChildren()
	 */
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getObject() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAttr(String key) {
		return map.get(key);
	}

	public Object getObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
