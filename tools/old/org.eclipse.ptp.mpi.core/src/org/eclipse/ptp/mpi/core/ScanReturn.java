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

package org.eclipse.ptp.mpi.core;

import java.util.ArrayList;
import java.util.List;

/**
 * This object represents the collection of MPI artifacts returned from analysis, which will eventually be displayed in
 * the MPITableView, etc. <br>
 * It can also contain other information such as return codes, generalized analysis results, parameters, etc. <br>
 * 
 * @author tibbitts
 * 
 */
public class ScanReturn
{

    private List artifacts = new ArrayList();

    public boolean wasError()
    {
        return false;
    }

    public List getArtifactList()
    {
        return artifacts;
    }

    public void addMpiArtifact(Artifact a)
    {
        artifacts.add(a);
    }

}
