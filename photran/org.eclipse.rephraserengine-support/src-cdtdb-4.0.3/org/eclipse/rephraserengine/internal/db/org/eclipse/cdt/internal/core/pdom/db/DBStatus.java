/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.pdom.db;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.core.CCorePlugin;


/**
 * @author Doug Schaefer
 *
 */
public class DBStatus extends Status {

	/**
	 * @param exception
	 */
	public DBStatus(IOException exception) {
		super(IStatus.ERROR, CCorePlugin.PLUGIN_ID, 0, "IOException", exception); //$NON-NLS-1$
	}

}
