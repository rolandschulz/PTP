/**********************************************************************
 * Copyright (c) 2007,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.popup.actions;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.Utility;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable.BarrierInfo;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierAnalysisResults;
import org.eclipse.ptp.pldt.mpi.analysis.internal.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.messages.Messages;
import org.eclipse.ptp.pldt.mpi.analysis.view.MPIArtifactMarkingVisitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action to show the matching set for a barrier.<br>
 * Most methods in this class are borrowed from
 * org.eclipse.ptp.pldt.openmp.analysis.textview.ShowConcurrencyAction.java
 */

public class ShowMatchSet extends ActionDelegate
		implements IEditorActionDelegate {

	protected CEditor editor_ = null;
	protected MPIBarrierAnalysisResults results_ = null;
	private static final boolean traceOn = false;

	protected static final String TITLE = Messages.ShowMatchSet_showMatchSet;

	public ShowMatchSet() {
		if (traceOn)
			System.out.println("ShowMatchSet() constructed..."); //$NON-NLS-1$
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof CEditor)
			editor_ = (CEditor) targetEditor;
	}

	public void runWithEvent(IAction action, Event event) {
		TextSelection selection = null;
		if (editor_ != null &&
				editor_.getSelectionProvider().getSelection() instanceof TextSelection) {
			selection = (TextSelection) editor_.getSelectionProvider().getSelection();
		}

		if (selection != null) {
			showMatchSet(selection);
		}
		else
			showMessage(TITLE, Messages.ShowMatchSet_noSelectionsMade);
	}

	protected void showMessage(String title, String message) {
		MessageDialog.openInformation(getStandardDisplay().getActiveShell(), title, message);
	}

	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	protected void showMatchSet(TextSelection selection) {
		if (selection.getOffset() == 0 && selection.getLength() == 0) {
			showMessage(TITLE, Messages.ShowMatchSet_noSelectionsMande);
			return;
		}

		/* first clear all existing markers */
		IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();
		try {
			int depth = IResource.DEPTH_INFINITE;
			wsResource.deleteMarkers(IDs.matchingSetMarkerID, false, depth);

		} catch (CoreException e) {
			System.out.println("RM: exception deleting markers."); //$NON-NLS-1$
			e.printStackTrace();
		}

		results_ = MPIBarrierAnalysisResults.getAnalysisResults();
		if (results_ == null) {
			showMessage(TITLE, Messages.ShowMatchSet_noBarrierAnalysisHasBeenPerformed);
			return;
		}
		IEditorInput ieu = editor_.getEditorInput();
		IFile file = null;
		if (ieu instanceof IFileEditorInput) {
			file = ((IFileEditorInput) ieu).getFile(); // cdt40
		}
		// IFile file = editor_.getInputFile();//cdt31
		String fileName = file.getFullPath().toOSString();
		// String filename2 = Utility.getInputFile(editor_);
		/* This filename may not contain the FULL path information */

		BarrierInfo barrier = findSelectedBarrier(fileName, selection.getOffset());
		if (barrier == null) {
			showMessage(TITLE, Messages.ShowMatchSet_PleaseSelectABarrier);
			return;
		}

		removeMarkers(editor_.getDocumentProvider().getAnnotationModel(editor_.getEditorInput()));
		// refresh the view later and these deleted markers should disappear
		// Use common shared variable for markerID
		// String markerID = "org.eclipse.ptp.pldt.mpi.analysis.mpiBarrierMatchingSetMarker";
		MPIArtifactMarkingVisitor visitor = new MPIArtifactMarkingVisitor(IDs.matchingSetMarkerID);

		/* Display all matched barriers */
		// create the markers for all the matched barriers
		String parentName = "BarrierSetName"; // change to something that makes sense //$NON-NLS-1$
		for (Iterator i = barrier.getMatchingSet().iterator(); i.hasNext();) {
			BarrierInfo matchedBar = (BarrierInfo) i.next();
			showNode(matchedBar.getFunc().getFunctionNameExpression(), "org.eclipse.ptp.pldt.mpi.analysis.matchset"); //$NON-NLS-1$
			ScanReturn sr = new ScanReturn();
			/*
			 * SourceInfo sourceInfo = matchedBar.getSourceInfo();
			 * int col=1;
			 * String filename=matchedBar.getFileName();
			 * int line=sourceInfo.getStartingLine();
			 * String fn=matchedBar.getEnclosingFunc();
			 */
			// ArtifactWithParent a = new ArtifactWithParent(filename, line, col, fn,"Artifact Call",sourceInfo,parentName);
			// sr.addArtifact(a);
			visitor.visitFile(matchedBar.getResource(), sr.getArtifactList());
		}
		// Done creating markers, now show the view
		ViewActivator.activateView(IDs.matchingSetViewID);
	}

	protected BarrierInfo findSelectedBarrier(String filename, int offset) {
		BarrierTable table = results_.getBarrierTable();
		for (Enumeration e = table.getTable().elements(); e.hasMoreElements();) {
			ArrayList list = (ArrayList) e.nextElement();
			for (Iterator i = list.iterator(); i.hasNext();) {
				BarrierInfo bar = (BarrierInfo) i.next();
				if (traceOn)
					System.out.println(bar.getFileName());
				if (traceOn)
					System.out.println(bar.getSourceInfo().getStart());
				if (!bar.getFileName().endsWith(filename))
					continue;
				if (bar.getSourceInfo().getStart() == offset)
					return bar;
			}
		}
		return null;
	}

	/* Don Pazel's code Utility.removeConcurrencyMarkers() */
	protected void removeMarkers(IAnnotationModel am)
	{
		LinkedList ais = new LinkedList();
		for (Iterator ai = am.getAnnotationIterator(); ai.hasNext();) {
			ais.add(ai.next());
		}
		for (Iterator it = ais.iterator(); it.hasNext();) {
			Annotation a = (Annotation) it.next();
			if (a.getType().equals("org.eclipse.ptp.pldt.mpi.analysis.matchset")) { //$NON-NLS-1$
				am.removeAnnotation(a);
			}
		}
	}

	protected void showNode(IASTNode node, String markerType)
	{
		Utility.Location l = Utility.getLocation(node);

		IAnnotationModel am = editor_.getDocumentProvider().getAnnotationModel(editor_.getEditorInput());

		// We need to add an annotation type to the annotation painter (see SourceViewerDecorationSupport)
		Annotation a = new Annotation(markerType, true, "Hi"); //$NON-NLS-1$
		int end = l.high_ - l.low_ + 1;
		Position p = new Position(l.low_, end);
		am.addAnnotation(a, p);
	}

}
