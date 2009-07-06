/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.ptp.rdt.core.RDTLog;

/**
 * @author crecoskie
 *
 */
public class LocalCProjectFactory implements ICProjectFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.model.ICProjectFactory#getProjectForFile(java.lang.String)
	 */
	public ICProject getProjectForFile(String filename) {
		ITranslationUnit tu = null;
		try {
			tu = CoreModelUtil.findTranslationUnitForLocation(new Path(filename), null);
		} catch (CModelException e) {
			RDTLog.logError(e);
		}
		return tu.getCProject();
	}

}
