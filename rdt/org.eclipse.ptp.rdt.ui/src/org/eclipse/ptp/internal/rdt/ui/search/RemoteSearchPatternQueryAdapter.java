/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchPatternQuery
 * Version: 1.34
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchPatternQuery;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchTextSelectionQuery;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;

public class RemoteSearchPatternQueryAdapter extends RemoteSearchQueryAdapter {
	
	private RemoteSearchPatternQueryAdapter() {
		throw new IllegalStateException();
	}
	
	public RemoteSearchPatternQueryAdapter(ICIndexSubsystem subsystem, Scope scope, RemoteSearchPatternQuery query) {
		super(subsystem, scope, query);
	}

	@Override
	public String getLabel() {
		if(fQuery == null) {
			return null;
		}
		
		return Messages.format(CSearchMessages.PDOMSearchPatternQuery_PatternQuery_labelPatternInScope, super.getLabel(), ((RemoteSearchPatternQuery) fQuery).getPattern(), ((RemoteSearchPatternQuery) fQuery).getScopeDescription()); 
	}
	
	@Override
	public String getResultLabel(int numMatches) {
		if(fQuery == null) {
			return org.eclipse.ptp.rdt.ui.messages.Messages.getString("RemoteSearchPatternQueryAdapter_0"); //$NON-NLS-1$
		}
		
		return getResultLabel(((RemoteSearchPatternQuery) fQuery).getPatternStr(), ((RemoteSearchPatternQuery) fQuery).getScopeDescription(), numMatches); 

	}
}
