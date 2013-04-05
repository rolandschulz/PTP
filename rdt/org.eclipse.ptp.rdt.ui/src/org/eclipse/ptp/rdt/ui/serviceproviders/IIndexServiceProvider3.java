/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.ptp.internal.rdt.ui.editor.IRemoteInactiveHighlightingService;

/**
 * @since 5.0
 */
public interface IIndexServiceProvider3 extends IIndexServiceProvider2 {

	/**
	 * @since 4.2
	 */
	public IRemoteInactiveHighlightingService getRemoteInactiveHighlightingService();
}
