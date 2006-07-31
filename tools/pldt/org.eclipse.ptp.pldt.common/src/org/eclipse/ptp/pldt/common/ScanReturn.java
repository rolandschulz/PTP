/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common;

import java.util.ArrayList;
import java.util.List;

/**
 * This object represents the collection of  artifacts (e.g. MPI or OpenMP artifacts) returned from analysis, 
 * which will eventually be displayed in
 * the associated view, etc. <br>
 * It can also contain other information such as return codes, generalized analysis results, parameters, etc. <br>
 * 
 * @author tibbitts
 * 
 */
public class ScanReturn
{

    private List<Artifact> artifacts = new ArrayList<Artifact>();
    
    public ScanReturn()
    {      
    }

    public boolean wasError()
    {
        return false;
    }

    public List getArtifactList()
    {
        return artifacts;
    }

    public void addArtifact(Artifact a)
    {
        artifacts.add(a);
    }

}
