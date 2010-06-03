/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.tests;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class RDTUITestPlugin extends AbstractUIPlugin {

	
	private static RDTUITestPlugin plugin;
	
	public RDTUITestPlugin() {
		super();
		plugin = this;
	}

	public static RDTUITestPlugin getDefault() {
		return plugin;
	}
}
