/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchTextSelectionQuery;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.internal.rdt.ui.search.RemoteSearchQueryAdapter;

public class RemoteSearchTextSelectionQueryAdapter extends RemoteSearchQueryAdapter {

	public RemoteSearchTextSelectionQueryAdapter(ICIndexSubsystem subsystem, Scope scope, RemoteSearchTextSelectionQuery query) {
		super(subsystem, scope, query);
	}

	public String getLabel() {
		return super.getLabel() + " " + ((RemoteSearchTextSelectionQuery) fQuery).getSelection(); //$NON-NLS-1$
	}
}
