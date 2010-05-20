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
package org.eclipse.ptp.pldt.lapi;

import org.eclipse.ptp.pldt.common.ArtifactMarkingVisitor;

/**
 * @since 4.0
 */
public class LAPIArtifactMarkingVisitor extends ArtifactMarkingVisitor {
	public LAPIArtifactMarkingVisitor(String markerId) {
		super(markerId);
	}
}
