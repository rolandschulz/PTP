/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 * Class: org.eclipse.cdt.internal.ui.includebrowser.IBHistoryAction
 * Version: 1.5
 */

package org.eclipse.ptp.internal.rdt.ui.includebrowser;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;


/**
 * Action used for the include browser forward / backward buttons
 */
public class IBHistoryAction extends Action {

	private IBViewPart fViewPart;
	private ITranslationUnit fElement;
	
	public IBHistoryAction(IBViewPart viewPart, ITranslationUnit element) {
        super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		fViewPart= viewPart;
		fElement= element;		
		
		String elementName= CElementBaseLabels.getElementLabel(element, CElementBaseLabels.ALL_POST_QUALIFIED);
		setText(elementName);
		setImageDescriptor(getImageDescriptor(element));
	}
	
	private ImageDescriptor getImageDescriptor(ITranslationUnit elem) {
		CElementImageProvider imageProvider= new CElementImageProvider();
		ImageDescriptor desc= imageProvider.getBaseImageDescriptor(elem, 0);
		imageProvider.dispose();
		return desc;
	}
	
	/*
	 * @see Action#run()
	 */
	@Override
	public void run() {
		fViewPart.setInput(fElement);
	}
	
}
