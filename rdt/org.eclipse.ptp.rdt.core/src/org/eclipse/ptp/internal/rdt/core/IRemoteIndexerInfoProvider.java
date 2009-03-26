/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core;

import org.eclipse.cdt.internal.core.indexer.IStandaloneScannerInfoProvider;

/**
 * Provides necessary information to the remote indexer.
 */
public interface IRemoteIndexerInfoProvider extends IStandaloneScannerInfoProvider {

	String getLanguageID(String path);
	
	boolean isHeaderUnit(String path);
}
