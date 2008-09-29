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
 * @author laggarcia
 * @since 3.0.0
 */
public class NoEngineSearcher implements Searcher {

	/**
	 * 
	 */
	public NoEngineSearcher() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.utils.searcher.Searcher#search()
	 */
	public void search() throws SearchFailedException {
		throw new SearchFailedException(SearcherMessages.noSearchEngine);
	}

}
