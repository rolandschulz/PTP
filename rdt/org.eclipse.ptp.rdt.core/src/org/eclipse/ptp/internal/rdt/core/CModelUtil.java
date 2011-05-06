/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.corext.util.CModelUtil
 * Version: 1.17
 */
package org.eclipse.ptp.internal.rdt.core;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;

public class CModelUtil {

	/**
	 * Returns the translation unit the element belongs to or <code>null</code> if it does not.
	 */
	public static ITranslationUnit getTranslationUnit(ICElement elem) {
		while (elem != null) {
			if (elem instanceof ITranslationUnit) {
				return (ITranslationUnit) elem;
			}
			elem= elem.getParent();
		}
		return null;
	}

}
