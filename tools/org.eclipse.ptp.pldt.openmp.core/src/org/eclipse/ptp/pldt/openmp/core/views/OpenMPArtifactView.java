/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.openmp.core.views;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ArtifactManager;
import org.eclipse.ptp.pldt.common.views.SimpleTableMarkerView;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;
import org.eclipse.ptp.pldt.openmp.core.messages.Messages;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * An OpenMP artifact view based on SimpleTableView <br>
 * Note that the ID must be unique.
 * 
 */
public class OpenMPArtifactView extends SimpleTableMarkerView {

	public OpenMPArtifactView() {
		// if you need to read icon images, etc. from the plug-in, be sure to
		// pass in an actual Plugin class instance for first arg
		// super(OpenMPPlugin.getDefault(), "OpenMP Artifact", "OpenMP Artifacts", "Construct", OpenMPPlugin.MARKER_ID);
		super(
				OpenMPPlugin.getDefault(),
				Messages.OpenMPArtifactView_OpenMP_Artifact,
				Messages.OpenMPArtifactView_OpenMP_Artifacts,
				Messages.OpenMPArtifactView_Construct,
				OpenMPPlugin.MARKER_ID);

	}

	/**
	 * Provide custom info for filling in the last column
	 */
	protected String getConstructStr(IMarker marker) throws CoreException {
		Integer temp = (Integer) marker.getAttribute(columnID_);
		if (temp != null) {
			Integer constructType = (Integer) temp;
			int i = constructType.intValue();
			String val = ""; //$NON-NLS-1$
			if (i < Artifact.CONSTRUCT_TYPE_NAMES.length)
				val = Artifact.CONSTRUCT_TYPE_NAMES[i];
			else
				val = "value is " + i; //$NON-NLS-1$

			/*
			 * val = "value is " + i; // BRT need a more robust lookup
			 * if (i == 0)
			 * val = "OpenMP Pragma";
			 * if (i == 1)
			 * val = "Function Call";
			 */
			return val;
		} else
			return " "; //$NON-NLS-1$
	}

	/**
	 * Make "show info" action to display artifact information
	 * This is the "show pragma region" action for OpenMP artifacts
	 */
	protected void makeShowInfoAction()
	{
		infoAction = new Action() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.IAction#run()
			 */
			@SuppressWarnings("restriction")
			public void run()
			{
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				IMarker marker = (IMarker) obj;
				if (marker == null) {
					MessageDialog.openInformation(null,
							org.eclipse.ptp.pldt.openmp.core.messages.Messages.OpenMPArtifactView_noSelection,
							org.eclipse.ptp.pldt.openmp.core.messages.Messages.OpenMPArtifactView_noArtifactSelected);
					return;
				}

				try {
					// Object o = artifactManager_.getArtifact((String)marker.getAttribute(IDs.ID));
					Object o = ArtifactManager.getArtifact(marker);
					if (o == null || !(o instanceof Artifact))
						return;
					Artifact artifact = (Artifact) o;
					Object p = artifact.getArtifactAssist();
					if (p == null || !(p instanceof PASTOMPPragma))
						return;
					PASTOMPPragma ompPragma = (PASTOMPPragma) p;
					IASTNode iRegion = ompPragma.getRegion();
					ASTNode region = (iRegion instanceof ASTNode ? (ASTNode) iRegion : null);
					if (region == null)
						return;

					// determine if we collected location information for this omp pragma
					String filename = ompPragma.getRegionFilename(); // region.getContainingFilename();
					if (filename == null)
						return;

					IResource r = ParserUtil.getResourceForFilename(filename);

					IEditorPart aPart = null;
					if (r != null) {
						try {
							aPart = EditorUtility.openInEditor(r);
						}
						catch (PartInitException pie) {
							return;
						}
						catch (CModelException e) {
							return;
						}
					}
					else {
						return;
					}

					if (aPart instanceof AbstractTextEditor)
					{
						int offset = ompPragma.getRegionOffset(); // ifl.getNodeOffset(); //locs[0].getNodeOffset();
						int length = ompPragma.getRegionLength(); // ifl.getNodeLength(); //region.getLength();
						// System.out.println("OMPAV: Pragma offset: "+offset+" length: "+length);
						((AbstractTextEditor) aPart).selectAndReveal(offset, length);
					}

				} catch (Exception e) {
					System.out.println("ATV.doubleclickAction: Error positioning editor page from marker line number"); //$NON-NLS-1$
					// showStatusMessage("Error positioning editor from marker line number", "error marker goto");
					e.printStackTrace();
				}

			}
		};

		infoAction.setText(Messages.OpenMPArtifactView_showPragmaRegion);
		infoAction.setToolTipText(Messages.OpenMPArtifactView_showRegionForSelected + thingname_);
		infoAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJS_INFO_TSK));

	}

}
