/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.searcher;

/**
 * This interface represents a search engine. It provides methods to perform
 * fast searches and long searches using this search engine.
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public interface Searcher {

	/**
	 * Performs a search for the value being searched by this search engine.
	 * 
	 */
	public void search() throws SearchFailedException;

}
