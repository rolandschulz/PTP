/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CHHistoryAction
 * Version: 1.3
 */

package org.eclipse.ptp.internal.rdt.ui.callhierarchy;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;


/**
 * Action used for the include browser forward / backward buttons
 */
public class CHHistoryAction extends Action {
	final static int LABEL_OPTIONS= 
		CElementBaseLabels.M_PARAMETER_TYPES | 
		CElementBaseLabels.ALL_FULLY_QUALIFIED |
		CElementBaseLabels.MF_POST_FILE_QUALIFIED;
	
	private RemoteCHViewPart fViewPart;
	private ICElement fElement;
	
	public CHHistoryAction(RemoteCHViewPart viewPart, ICElement element) {
        super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		fViewPart= viewPart;
		fElement= element;		
	
		String elementName= CElementBaseLabels.getElementLabel(element, LABEL_OPTIONS);
		setText(elementName);
		setImageDescriptor(getImageDescriptor(element));
	}
	
	private ImageDescriptor getImageDescriptor(ICElement elem) {
		CElementImageProvider imageProvider= new CElementImageProvider();
		ImageDescriptor desc= imageProvider.getBaseImageDescriptor(elem, 0);
		imageProvider.dispose();
		return desc;
	}
	
	/*
	 * @see Action#run()
	 */
	public void run() {
		fViewPart.setInput(fElement);
	}
	
}
