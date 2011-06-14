/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.internal.rdt.ui.scannerinfo;

import org.eclipse.cdt.ui.newui.AbstractPage;


/**
 * The class have the same functionality as superclass.
 * The only need to create it is distinguishing tabs.
 *
 */
public class RemoteScannerInfoPropertiesPage extends AbstractPage {

	protected boolean isSingle() {
		return false;
	}

}

