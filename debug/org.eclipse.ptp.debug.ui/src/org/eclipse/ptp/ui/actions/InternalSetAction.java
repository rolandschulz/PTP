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
package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;

/**
 * @author Clement chu
 *
 */
public class InternalSetAction extends ParallelAction {
	private String set_id = "";
	private SetAction action = null;
	
	public InternalSetAction(String set_id, AbstractParallelElementView view, SetAction action) {
		this(set_id, view, IAction.AS_PUSH_BUTTON, action);
	}
	
	public InternalSetAction(String set_id, AbstractParallelElementView view, int style, SetAction action) {
		super("To Set: " + set_id, style, view);
		this.set_id = set_id;
		this.action = action;
	    setImageDescriptor(ImageUtil.ID_ICON_CREATESET_NORMAL);
	    setDisabledImageDescriptor(ImageUtil.ID_ICON_CREATESET_DISABLE);
	}
	
	public void run(IElement[] elements) {
		action.run(elements, set_id);
	}
}
