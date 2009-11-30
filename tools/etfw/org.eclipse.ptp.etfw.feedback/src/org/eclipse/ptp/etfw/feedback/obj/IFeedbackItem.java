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

package org.eclipse.ptp.etfw.feedback.obj;

import java.util.List;

/**
 * 
 * An IFeedbackItem has some standard info, like name, file, lineNo, etc
 * <br>
 * And some variable data, which will fill in the (variable number of) other columns.
 * @author beth tibbitts
 *
 */
public interface IFeedbackItem {

	
	public String getName();
	public List<IFeedbackItem>  getChildren();
	public boolean hasChildren();
	
	public String getFile();
	
	/**
	 * an IFeedbackItem can have either a lineno range (start/end)
	 * or a Character start/end.
	 * If useLineNo() returns true, use getStartingLineNo();
	 * If the lineNo range is more than on eline, getEndlingLineNo() will tell the last line.
	 * @return
	 */
	//public boolean useLineNo();
	
	public int getLineNoStart();
	//public int getLineNoEnd();

	/**
	 * unique id for this type of item e.g. to use as parent id
	 * @return
	 */
	public String getID();
	
	/** A longer description */
	public String getDescription();
	
	/** string which may be used to tell which item is parent; an artificial 'parent' node MAY be created for this */
	public String getParentID();
	
	
	
}
