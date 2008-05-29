/**********************************************************************
 * Copyright (c) 2006,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.openmp.core;



import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPError;

/**
 * Save specific information on OpenMP artifacts and problems
 * 
 * @author Donald P Pazel
 */
public class OpenMPScanReturn extends ScanReturn
{
    private LinkedList<OpenMPError> problems_ = new LinkedList<OpenMPError>();  
    
    /**
     * OpenMPScanReturn - constructor
     *
     */
    public OpenMPScanReturn()
    {
       super();   
    }

    /**
     * addOpenMPArtifact - add a pragma to the pragma list
     * @param a
     */
    public void addOpenMPArtifact(Artifact a)
    {
        addArtifact(a);
    }

    /**
     * getOpenMPList - get the pragma list
     * @return
     */
    public List<Artifact> getOpenMPList()
    {
        return getArtifactList();
    }
    

    /**
     * addProblemst - add a set of problems to the list
     * @param errors - LinkedList
     */
    public void addProblems(LinkedList<OpenMPError> errors)
    {
        if (errors.size()>0)  // 0 sized appends seem to add junk to problems_
          problems_.addAll(errors);
    }

    /**
     * getProblems - accessor to problems list
     * @return
     */
    public LinkedList<OpenMPError> getProblems()
    {
        return problems_;
    }

}
