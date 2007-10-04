/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi.actions;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseBase;
import org.eclipse.ptp.pldt.common.util.ViewActivater;
import org.eclipse.ptp.pldt.lapi.LAPIArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.lapi.LapiIDs;
import org.eclipse.ptp.pldt.lapi.LapiPlugin;
import org.eclipse.ptp.pldt.lapi.analysis.LapiCASTVisitor;


/**
 * 
 * Run analysis to create LAPI artifact markers. <br>
 * The analysis is done in the doMpiCallAnalysis() method
 * 
 * IObjectActionDelegate enables popup menu selection IWindowActionDelegate enables toolbar(or menu) selection
 */
public class RunAnalyseLAPI extends RunAnalyseBase
{
    /**
     * Constructor for the "Run Analysis" action
     */
    public RunAnalyseLAPI()
    {
        super("LAPI", new LAPIArtifactMarkingVisitor(LapiIDs.MARKER_ID),
        		LapiIDs.MARKER_ID);
    }

    /**
     * Returns LAPI analysis artifacts for file
     * 
     * @param file
     * @param includes_ LAPI include paths
     * @return
     */
    public ScanReturn doArtifactAnalysis(final ITranslationUnit tu, final List includes)
    {
    	final ScanReturn msr = new ScanReturn();
		final String fileName = tu.getElementName();
		ILanguage lang;
		try {
			lang = tu.getLanguage();
            
			IASTTranslationUnit atu = tu.getAST();
			if (lang.getId().equals(GCCLanguage.ID)) {// cdt40
				atu.accept(new LapiCASTVisitor(includes, fileName, msr));
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msr;
	}

    protected List getIncludePath()
    {
        return LapiPlugin.getDefault().getLapiIncludeDirs();
    }

    protected void activateArtifactView()
    {
        ViewActivater.activateView(LapiIDs.LAPI_VIEW_ID);
    }

	protected void activateProblemsView() {

	}

}