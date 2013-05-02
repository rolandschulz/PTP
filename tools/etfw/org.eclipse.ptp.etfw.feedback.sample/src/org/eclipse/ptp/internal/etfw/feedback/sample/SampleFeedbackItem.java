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

package org.eclipse.ptp.internal.etfw.feedback.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackItem;
import org.eclipse.ptp.etfw.feedback.IFeedbackItem;

/**
 * @author Beth Tibbitts
 * 
 */
public class SampleFeedbackItem extends AbstractFeedbackItem implements IFeedbackItem {
	private String name;
	private String parentID;
	private String id;
	private String description;
	private String filename;
	private int lineNo;

	/**
	 * map of values by attribute name, that the view LabelProvider will want to ask for
	 */
	Map<String, String> map = new HashMap<String, String>();

	public SampleFeedbackItem(String name, String parentID, String id, String filename, int lineNo, String description) {
		this.name = name;
		this.parentID = parentID;
		this.id = id;
		this.filename = filename;
		this.lineNo = lineNo;
		this.description = description;

		map.put(FeedbackIDs.FEEDBACK_ATTR_NAME, name);
		map.put(FeedbackIDs.FEEDBACK_ATTR_FILENAME, filename);
		map.put(FeedbackIDs.FEEDBACK_ATTR_ID, id);
		map.put(IMarker.LINE_NUMBER, Integer.toString(lineNo));
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
		return this.lineNo;
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
