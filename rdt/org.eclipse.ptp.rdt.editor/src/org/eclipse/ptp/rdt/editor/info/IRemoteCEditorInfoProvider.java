/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.editor.info;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ptp.internal.rdt.editor.RemoteCEditor;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.IDocumentProvider;

public interface IRemoteCEditorInfoProvider {

	public static int INPUT_UNKNOWN = 0;
	public static int HANDLES_INPUT = 1;
	public static int INPUT_AMBIGUOUS = 2;

	/**
	 * Called when the editor is initialized to initialize any necessary values in the info provider
	 * @param remoteCEditor the editor being initialized
	 */
	public void initializeEditor(RemoteCEditor remoteCEditor);

	/**
	 * Called prior to the input being set in the editor.
	 * @param input the editor input
	 */
	public void preDoSetInput(IEditorInput input);

	/**
	 * Called after the input is set in the editor
	 * @param input the editor input
	 */
	public void postDoSetInput(IEditorInput input) throws CoreException;

	/**
	 * Indicates whether or not the specified editor input is applicable to this info provider
	 * @param input the editor input
	 * @return one of INPUT_UNKNOWN, HANDLES_INPUT or INPUT_AMBIGUOUS
	 */
	public int isApplicableEditorInput(IEditorInput input);

	/**
	 * Retrieves an alternate image to display
	 * @return	the image for the editor
	 */
	public Image getTitleImage();

	public IDocumentProvider getDocumentProvider(IDocumentProvider oldProvider);

	public String getTitle(IEditorInput input);

	public IEditorInput getAlternateInput(IEditorInput input);

	public String getTitleTooltip();

	public CContentOutlinePage getOutlinePage(RemoteCEditor remoteCEditor);

	public void doPostCreatePartControl(Composite parent);

	public Object getAdapter(Class required);

	public boolean doPrePerformSave(boolean overwrite);

	public void doPostPerformSave();

	public void createActions(IVerticalRuler iVerticalRuler);

	public void editorContextMenuAboutToShow(IMenuManager menu);

	public void dispose();

	public int getAnnotationRulerColumnWidth();

	public void helpRequested(HelpEvent e);

	public void createUndoRedoActions();

	public void doPreRevertToSaved();

	public void doPostRevertToSaved();

	public void aboutToBeReconciled();

	public ICElement getInputCElement();

	public boolean reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor);

	public boolean doPrePerformSaveAs(IProgressMonitor progressMonitor);

	public void doPostPerformSaveAs();

	public boolean validateState(IEditorInput input);

	public void rulerContextMenuAboutToShow(IMenuManager menu);

	public String[] collectContextMenuPreferencePages();

	public ActionGroup createSelectionSearchGroup(RemoteCEditor remoteCEditor);

	public ActionGroup createOpenViewActionGroup(RemoteCEditor remoteCEditor);

	public SourceViewerConfiguration getSourceViewerConfiguration(IPreferenceStore store, SourceViewerConfiguration sourceViewerConfiguration);

	public void fillActionBars(IActionBars actionBars);

	public boolean shouldProcessLocalParsingCompletions();

	public Boolean isSemanticHighlightingEnabled();

}
