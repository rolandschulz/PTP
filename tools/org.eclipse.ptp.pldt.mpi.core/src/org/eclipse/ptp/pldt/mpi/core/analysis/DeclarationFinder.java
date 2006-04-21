/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core.analysis;

import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.ptp.pldt.mpi.core.util.DOMDisplaySearchNames;
import org.eclipse.search.ui.NewSearchUI;

/**
 * Works to run a DOM based name search utilizing the search engine.
 * 
 */

public class DeclarationFinder implements Runnable
{
    private CSearchResult         result;
    private DOMDisplaySearchNames query;

    public DeclarationFinder(DOMDisplaySearchNames query)
    {
        this.query = query;
    }

    public void run()
    {
        result = null;
        // IStatus status = NewSearchUI.runQueryInForeground(null, query);

        // if ((status != null) && status.isOK()) {
        // // expecting CSearchResult
        // result = (CSearchResult) query.getSearchResult();
        // }

        // try run in background to eliminate the flashing progress popups.
        NewSearchUI.runQueryInBackground(query);
        // wait till query finishes
        do {
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
            }
        } while (NewSearchUI.isQueryRunning(query));
        // expecting CSearchResult
        result = (CSearchResult) query.getSearchResult();
    }

    /**
     * @return Returns the result.
     */
    public CSearchResult getResult()
    {
        return result;
    }

}
