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

import java.util.List;

import org.eclipse.ptp.etfw.feedback.core.obj.IFeedbackItem;
import org.eclipse.ptp.etfw.feedback.core.obj.IFeedbackParser;

/**
 * Abstract class that may contain utility methods for parsing feedback xml
 * files.  
 * 
 * @author beth tibbitts
 * 
 */
abstract public class AbstractFeedbackParser implements IFeedbackParser {

	private MarkerManager mkrMgr;

	public void createMarkers(List<IFeedbackItem> items, String markerID) {
		System.out.println("create markers");
		if (mkrMgr == null) {
			mkrMgr = new MarkerManager();
		}
		mkrMgr.createMarkers(items,markerID);

	}

}
