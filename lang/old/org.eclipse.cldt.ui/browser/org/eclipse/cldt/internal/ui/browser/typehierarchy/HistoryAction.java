/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.ui.browser.typehierarchy;

import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.internal.ui.ICHelpContextIds;
import org.eclipse.cldt.internal.ui.viewsupport.FortranElementImageProvider;
import org.eclipse.cldt.internal.ui.viewsupport.FortranElementLabels;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action used for the type hierarchy forward / backward buttons
 */
public class HistoryAction extends Action {

	private TypeHierarchyViewPart fViewPart;
	private ICElement fElement;
	
	public HistoryAction(TypeHierarchyViewPart viewPart, ICElement element) {
		super();
		fViewPart= viewPart;
		fElement= element;		
		
		String elementName= FortranElementLabels.getElementLabel(element, FortranElementLabels.ALL_POST_QUALIFIED | FortranElementLabels.M_PARAMETER_TYPES);
		setText(elementName);
		setImageDescriptor(getImageDescriptor(element));
				
		setDescription(TypeHierarchyMessages.getFormattedString("HistoryAction.description", elementName)); //$NON-NLS-1$
		setToolTipText(TypeHierarchyMessages.getFormattedString("HistoryAction.tooltip", elementName)); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, ICHelpContextIds.HISTORY_ACTION);
	}
	
	private ImageDescriptor getImageDescriptor(ICElement elem) {
		FortranElementImageProvider imageProvider= new FortranElementImageProvider();
		ImageDescriptor desc= imageProvider.getBaseImageDescriptor(elem, 0);
		imageProvider.dispose();
		return desc;
	}
	
	/*
	 * @see Action#run()
	 */
	public void run() {
		fViewPart.gotoHistoryEntry(fElement);
	}
	
}
