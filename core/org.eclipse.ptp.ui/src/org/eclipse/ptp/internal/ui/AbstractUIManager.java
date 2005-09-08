/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.listeners.IPaintListener;

/**
 * @author Clement chu
 *
 */
public abstract class AbstractUIManager implements IManager {
	protected IModelManager modelManager = null;
	protected String cur_set_id = "";
	protected List pListeners = new ArrayList(0);
	
	public void shutdown() {
		pListeners.clear();
		pListeners = null;
	}
	
	public void addPaintListener(IPaintListener pListener) {
		if (!pListeners.contains(pListener))
			pListeners.add(pListener);
	}
	
	public void removePaintListener(IPaintListener pListener) {
		if (pListeners.contains(pListener))
			pListeners.remove(pListener);
	}

	protected void firePaintListener(Object condition) {
		for (Iterator i=pListeners.iterator(); i.hasNext();) {
			((IPaintListener)i.next()).repaint(condition);
		}
	}
}
