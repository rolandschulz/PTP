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

package org.eclipse.ptp.etfw.feedback;

import java.util.List;

import org.eclipse.core.resources.IFile;

/**
 * 
 * An IFeedbackItem has some standard info, like name, file, lineNo, etc <br>
 * And some variable data, which will fill in the (variable number of) other columns.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There is no guarantee that
 * this API will work or that it will remain the same. We do not recommending using this API without consulting with the
 * etfw.feedback team.
 * 
 * @author beth tibbitts
 * @since 6.0
 * 
 */
public interface IFeedbackItem {

	public String getName();

	public List<IFeedbackItem> getChildren();

	public boolean hasChildren();

	/**
	 * Return full filename, should include project name
	 * 
	 * @return
	 */
	public String getFile();

	/**
	 * @since 5.0
	 */
	public IFile getIFile();

	/**
	 * an IFeedbackItem can have either a lineno range (start/end)
	 * or a Character start/end.
	 * If useLineNo() returns true, use getStartingLineNo();
	 * If the lineNo range is more than on eline, getEndlingLineNo() will tell the last line.
	 * 
	 * @return
	 */
	// public boolean useLineNo();

	public int getLineNoStart();

	// public int getLineNoEnd();

	/**
	 * unique id for this type of item e.g. to use as parent id
	 * 
	 * @return
	 */
	public String getID();

	/** A longer description */
	public String getDescription();

	/** string which may be used to tell which item is parent; an artificial 'parent' node MAY be created for this */
	public String getParentID();

	/**
	 * allow for future expansion
	 * 
	 * @since 2.0
	 */
	public Object getObject();

	/** @since 5.0 */
	public String getAttr(String key);

	/** @since 5.0 */
	public Object getObject(String key);

}
