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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.rse.core.subsystems.IConnectorService;

public class RemoteInactiveHighlightingService extends AbstractRemoteService implements IRemoteInactiveHighlightingService {

	public RemoteInactiveHighlightingService(IConnectorService connectorService) {
		super(connectorService);
	}

	public RemoteInactiveHighlightingService(ICIndexSubsystem subsystem) {
		super(subsystem);
	}

	public List<Position> computeInactiveHighlightingPositions(IDocument document, IWorkingCopy workingCopy) {
		ICIndexSubsystem subsystem = getSubSystem();
		if(subsystem == null)
			return Collections.emptyList();

		subsystem.checkProject(workingCopy.getCProject().getProject(), new NullProgressMonitor());

		ITranslationUnit unit;
		try {
			unit = adaptWorkingCopy(workingCopy);
		} catch (CModelException e) {
			RDTLog.logError(e);
			return Collections.emptyList();
		}

		String result = subsystem.computeInactiveHighlightPositions(unit);
		return parsePositions(document, result);
	}

	private List<Position> parsePositions(IDocument document, String positionString) {
		if (positionString == null || positionString.length() == 0)
			return Collections.emptyList();

		String[] elements = positionString.split(","); //$NON-NLS-1$
		List<Position> positions = new ArrayList<Position>(elements.length / 3);

		for (int i = 0; i < elements.length; i += 3) {
			int start = Integer.parseInt(elements[i]);
			int end = Integer.parseInt(elements[i + 1]);
			boolean inclusive = Boolean.parseBoolean(elements[i + 2]);
			positions.add(createHighlightPosition(document, start, end, inclusive));
		}

		return positions;
	}

	private RemoteInactiveHighlightPosition createHighlightPosition(IDocument document, int startOffset, int endOffset, boolean inclusive) {
		try {
			if (document != null) {
				int start = document.getLineOfOffset(startOffset);
				int end = document.getLineOfOffset(endOffset);
				startOffset = document.getLineOffset(start);
				if (!inclusive) {
					endOffset = document.getLineOffset(end);
				}
			}
		} catch (BadLocationException x) {
			// concurrent modification?
		}
		return new RemoteInactiveHighlightPosition(startOffset, endOffset - startOffset, RemoteInactiveHighlighting.INACTIVE_CODE_KEY);
	}

}
