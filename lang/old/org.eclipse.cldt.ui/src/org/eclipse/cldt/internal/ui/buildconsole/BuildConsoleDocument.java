/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cldt.internal.ui.buildconsole;

import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;

public class BuildConsoleDocument extends Document {

	public BuildConsoleDocument() {
		setTextStore(new ConsoleOutputTextStore(2500));	
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}
}
