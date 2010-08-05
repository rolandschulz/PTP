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

import java.util.List;

import org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem;
import org.eclipse.ptp.etfw.feedback.obj.IFeedbackParser;

/**
 * Abstract class that may contain utility methods for parsing feedback xml
 * files.  
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. We do not recommending using this API without consulting with
 * the etfw.feedback team.
 * 
 * @author beth tibbitts
 * 
 */
abstract public class AbstractFeedbackParser implements IFeedbackParser {

	private MarkerManager mkrMgr;

	public void createMarkers(List<IFeedbackItem> items, String markerID) {
		//System.out.println("create markers");
		if (mkrMgr == null) {
			mkrMgr = new MarkerManager();
		}
		mkrMgr.createMarkers(items,markerID);

	}
	/**
	 * Find a source file in some fashion - presumed filename without absolute
	 * path information.  How do we know where to look?
	 * Ideas include same project as the xml file is found; same workspace; some path specified in preferences maybe?
	 * 
	 * @param filename
	 * @return fully qualified filename including path, or null if not found
	 */
	public String findSourceFile(String filename) {
		return null;
	}

}
