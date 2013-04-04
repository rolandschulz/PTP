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

package org.eclipse.ptp.internal.rdt.ui.editor;

import java.util.List;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

public interface IRemoteInactiveHighlightingService {

	List<Position> computeInactiveHighlightingPositions(IDocument document, IWorkingCopy wc);
}
