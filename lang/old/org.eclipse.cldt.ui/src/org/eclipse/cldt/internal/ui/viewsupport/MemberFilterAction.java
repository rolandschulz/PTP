/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cldt.internal.ui.viewsupport;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.cldt.ui.actions.MemberFilterActionGroup;

/**
 * Action used to enable / disable method filter properties
 */

public class MemberFilterAction extends Action {

	private int fFilterProperty;
	private MemberFilterActionGroup fFilterActionGroup;
	
	public MemberFilterAction(MemberFilterActionGroup actionGroup, String title, int property, String contextHelpId, boolean initValue) {
		super(title);
		fFilterActionGroup= actionGroup;
		fFilterProperty= property;
		
		WorkbenchHelp.setHelp(this, contextHelpId);

		setChecked(initValue);
	}
	
	/**
	 * Returns this action's filter property.
	 */
	public int getFilterProperty() {
		return fFilterProperty;
	}
	
	/*
	 * @see Action#actionPerformed
	 */
	public void run() {	
		fFilterActionGroup.setMemberFilter(fFilterProperty, isChecked());
	}

}
