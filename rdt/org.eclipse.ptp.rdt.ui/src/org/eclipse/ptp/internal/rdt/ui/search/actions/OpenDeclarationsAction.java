/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction
 * Version: 1.73
 */

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ptp.internal.rdt.ui.editor.CEditor;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.swt.widgets.Display;

public class OpenDeclarationsAction extends SelectionParseAction implements ASTRunnable {
	public static boolean sIsJUnitTest = false;	
	public static boolean sAllowFallback= true;
	
	private static final int KIND_OTHER = 0;
	private static final int KIND_USING_DECL = 1;
	private static final int KIND_DEFINITION = 2;

	private class WrapperJob extends Job {
		WrapperJob() {
			super(CEditorMessages.getString("OpenDeclarations.dialog.title")); //$NON-NLS-1$
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				return performNavigation(monitor);
			}
			catch (CoreException e) {
				return e.getStatus();
			}
		}		
	}
	
	
	ITextSelection fTextSelection;
	private String fSelectedText;
	private IWorkingCopy fWorkingCopy;
	private IIndex fIndex;
	private IProgressMonitor fMonitor;

	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(CEditor editor) {
		super( editor );
		setText(CEditorMessages.getString("OpenDeclarations.label")); //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("OpenDeclarations.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("OpenDeclarations.description")); //$NON-NLS-1$
	}

	
	protected IStatus performNavigation(IProgressMonitor monitor) throws CoreException {
		clearStatusLine();

		fMonitor= monitor;
		fWorkingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		if (fWorkingCopy == null)
			return Status.CANCEL_STATUS;

		fIndex= CCorePlugin.getIndexManager().getIndex(fWorkingCopy.getCProject(),
				IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

		try {
			fIndex.acquireReadLock();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}

		try {
			return ASTProvider.getASTProvider().runOnAST(fWorkingCopy, ASTProvider.WAIT_YES, monitor, this);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
		if (ast == null) {
			return Status.OK_STATUS;
		}
		int selectionStart = fTextSelection.getOffset();
		int selectionLength = fTextSelection.getLength();

		final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
		IASTName searchName= nodeSelector.findEnclosingName(selectionStart, selectionLength);
		if (searchName != null) { // just right, only one name selected
			boolean found= false;
			final IASTNode parent = searchName.getParent();
			if (parent instanceof IASTPreprocessorIncludeStatement) {
				openInclude(((IASTPreprocessorIncludeStatement) parent));
				return Status.OK_STATUS;
			}
			IBinding binding = searchName.resolveBinding();
			if (binding != null && !(binding instanceof IProblemBinding)) {
				int isKind= KIND_OTHER;
				if (searchName.isDefinition()) {
					if (binding instanceof ICPPUsingDeclaration) {
						isKind= KIND_USING_DECL;
					} else {
						isKind= KIND_DEFINITION;
					}
				}
				IName[] declNames = findNames(fIndex, ast, isKind, binding);
				if (declNames.length == 0) {
					if (binding instanceof ICPPSpecialization) {
						// bug 207320, handle template instances
						IBinding specialized= ((ICPPSpecialization) binding).getSpecializedBinding();
						if (specialized != null && !(specialized instanceof IProblemBinding)) {
							declNames = findNames(fIndex, ast, KIND_DEFINITION, specialized);
						}
					} else if (binding instanceof ICPPMethod) {
						// bug 86829, handle implicit methods.
						ICPPMethod method= (ICPPMethod) binding;
						if (method.isImplicit()) {
							try {
								IBinding clsBinding= method.getClassOwner();
								if (clsBinding != null && !(clsBinding instanceof IProblemBinding)) {
									declNames= findNames(fIndex, ast, KIND_OTHER, clsBinding);
								}
							} catch (DOMException e) {
								// don't log problem bindings.
							}
						}
					}
				}
				if (navigateViaCElements(fWorkingCopy.getCProject(), fIndex, declNames)) {
					found= true;
				}
				else {
					// leave old method as fallback for local variables, parameters and 
					// everything else not covered by ICElementHandle.
					found = navigateOneLocation(declNames);
				}
			}
			if (!found && !navigationFallBack(ast)) {
				reportSymbolLookupFailure(new String(searchName.toCharArray()));
			}
			return Status.OK_STATUS;
		} 

		// Check if we're in an include statement
		if (searchName == null) {
			IASTNode node= nodeSelector.findEnclosingNode(selectionStart, selectionLength);
			if (node instanceof IASTPreprocessorIncludeStatement) {
				openInclude(((IASTPreprocessorIncludeStatement) node));
				return Status.OK_STATUS;
			}
		}
		if (!navigationFallBack(ast)) {
			reportSelectionMatchFailure();
		}
		return Status.OK_STATUS; 
	}

	private boolean navigationFallBack(IASTTranslationUnit ast) {
		// bug 102643, as a fall-back we look up the selected word in the index
		if (sAllowFallback && fSelectedText != null && fSelectedText.length() > 0) {
			try {
				final ICProject project = fWorkingCopy.getCProject();
				final char[] name = fSelectedText.toCharArray();
				List<ICElement> elems= new ArrayList<ICElement>();
				final IndexFilter filter = IndexFilter.getDeclaredBindingFilter(ast.getLinkage().getLinkageID(), false);
				IIndexMacro[] macros= fIndex.findMacros(name, filter, fMonitor);
				for (IIndexMacro macro : macros) {
					ICElement elem= IndexUI.getCElementForMacro(project, fIndex, macro);
					if (elem != null) {
						elems.add(elem);
					}
				}
				IIndexBinding[] bindings = fIndex.findBindings(name, false, filter, fMonitor);
				for (IBinding binding : bindings) {
					final IName[] names = findNames(fIndex, ast, KIND_OTHER, binding);
					convertToCElements(project, fIndex, names, elems);
				}
				return navigateCElements(elems);
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		}
		return false;
	}

	private void openInclude(IASTPreprocessorIncludeStatement incStmt) {
		String name = null;
		if (incStmt.isResolved())
			name = incStmt.getPath();

		if (name != null) {
			final IPath path = new Path(name);
			runInUIThread(new Runnable() {
				public void run() {
					try {
						open(path, 0, 0);
					} catch (CoreException e) {
						CUIPlugin.log(e);
					}
				}
			});
		} else {
			reportIncludeLookupFailure(new String(incStmt.getName().toCharArray()));
		}
	}

	private boolean navigateOneLocation(IName[] declNames) {
		for (int i = 0; i < declNames.length; i++) {
			IASTFileLocation fileloc = declNames[i].getFileLocation();
			if (fileloc != null) {

				final IPath path = new Path(fileloc.getFileName());
				final int offset = fileloc.getNodeOffset();
				final int length = fileloc.getNodeLength();

				runInUIThread(new Runnable() {
					public void run() {
						try {
							open(path, offset, length);
						} catch (CoreException e) {
							CUIPlugin.log(e);
						}
					}
				});
				return true;
			}
		}
		return false;
	}

	private boolean navigateViaCElements(ICProject project, IIndex index, IName[] declNames) {
		final ArrayList<ICElement> elements= new ArrayList<ICElement>();
		convertToCElements(project, index, declNames, elements);
		return navigateCElements(elements);
	}


	private void convertToCElements(ICProject project, IIndex index, IName[] declNames, List<ICElement> elements) {
		for (int i = 0; i < declNames.length; i++) {
			try {
				ICElement elem = getCElementForName(project, index, declNames[i]);
				if (elem instanceof ISourceReference) {
					elements.add(elem);
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
	}
	
	private boolean navigateCElements(final List<ICElement> elements) {
		if (elements.isEmpty()) {
			return false;
		}

		runInUIThread(new Runnable() {
			public void run() {
				ISourceReference target= null;
				if (elements.size() == 1) {
					target= (ISourceReference) elements.get(0);
				}
				else {
					if (sIsJUnitTest) {
						throw new RuntimeException("ambiguous input"); //$NON-NLS-1$
					}
					ICElement[] elemArray= elements.toArray(new ICElement[elements.size()]);
					target = (ISourceReference) OpenActionUtil.selectCElement(elemArray, getSite().getShell(),
							CEditorMessages.getString("OpenDeclarationsAction.dialog.title"), CEditorMessages.getString("OpenDeclarationsAction.selectMessage"), //$NON-NLS-1$ //$NON-NLS-2$
							CElementBaseLabels.ALL_DEFAULT | CElementBaseLabels.ALL_FULLY_QUALIFIED | CElementBaseLabels.MF_POST_FILE_QUALIFIED, 0);
				}
				if (target != null) {
					ITranslationUnit tu= target.getTranslationUnit();
					ISourceRange sourceRange;
					try {
						sourceRange = target.getSourceRange();
						if (tu != null && sourceRange != null) {
							open(tu.getLocation(), sourceRange.getIdStartPos(), sourceRange.getIdLength());
						}
					} catch (CoreException e) {
						CUIPlugin.log(e);
					}
				}
			}
		});
		return true;
	}

	private ICElementHandle getCElementForName(ICProject project, IIndex index, IName declName) 
	throws CoreException {
		if (declName instanceof IIndexName) {
			return IndexUI.getCElementForName(project, index, (IIndexName) declName);
		}
		if (declName instanceof IASTName) {
			IASTName astName = (IASTName) declName;
			IBinding binding= astName.resolveBinding();
			if (binding != null) {
				ITranslationUnit tu= IndexUI.getTranslationUnit(project, astName);
				if (tu != null) {
					IASTFileLocation loc= astName.getFileLocation();
					IRegion region= new Region(loc.getNodeOffset(), loc.getNodeLength());
					return CElementHandleFactory.create(tu, binding, astName.isDefinition(), region, 0);
				}
			}
			return null;
		}
		return null;
	}

	private IName[] findNames(IIndex index, IASTTranslationUnit ast, int isKind, IBinding binding) throws CoreException {
		IName[] declNames;
		if (isKind == KIND_DEFINITION) {
			declNames= findDeclarations(index, ast, binding);
		} else {
			declNames= findDefinitions(index, ast, isKind, binding);
		}

		if (declNames.length == 0) {
			if (isKind == KIND_DEFINITION) {
				declNames= findDefinitions(index, ast, isKind, binding);
			} else {
				declNames= findDeclarations(index, ast, binding);
			}
		}
		return declNames;
	}

	private IName[] findDefinitions(IIndex index, IASTTranslationUnit ast, int isKind, IBinding binding) throws CoreException {
		List<IASTName> declNames= new ArrayList<IASTName>();
		declNames.addAll(Arrays.asList(ast.getDefinitionsInAST(binding)));
		for (Iterator<IASTName> i = declNames.iterator(); i.hasNext();) {
			IASTName name= i.next();
			if (name.resolveBinding() instanceof ICPPUsingDeclaration) {
				i.remove();
			}
		}
		if (!declNames.isEmpty()) {
			return declNames.toArray(new IASTName[declNames.size()]);
		}

		// 2. Try definition in index
		return index.findNames(binding, IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
	}

	private IName[] findDeclarations(IIndex index, IASTTranslationUnit ast,
			IBinding binding) throws CoreException {
		IName[] declNames= ast.getDeclarationsInAST(binding);
		for (int i = 0; i < declNames.length; i++) {
			IName name = declNames[i];
			if (name.isDefinition()) 
				declNames[i]= null;
		}
		declNames= (IName[]) ArrayUtil.removeNulls(IName.class, declNames);
		if (declNames.length == 0) {
			declNames= index.findNames(binding, IIndex.FIND_DECLARATIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		}
		return declNames;
	}

	@Override
	public void run() {
		computeSelectedWord();
		if (fTextSelection != null) {
			new WrapperJob().schedule();
		}
	}

	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		}
		else {
			Display.getDefault().asyncExec(runnable);
		}
	}

	/**
	 * For the purpose of regression testing.
	 * @since 4.0
	 */
	public void runSync() throws CoreException {
		computeSelectedWord();
		if (fTextSelection != null) {
			performNavigation(new NullProgressMonitor());
		}
	}


	private void computeSelectedWord() {
		fTextSelection = getSelectedStringFromEditor();
		fSelectedText= null;
		if (fTextSelection != null) {
			if (fTextSelection.getLength() > 0) {
				fSelectedText= fTextSelection.getText();
			}
			else {
				IDocument document= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
				IRegion reg= CWordFinder.findWord(document, fTextSelection.getOffset());
				if (reg != null && reg.getLength() > 0) {
					try {
						fSelectedText= document.get(reg.getOffset(), reg.getLength());
					} catch (BadLocationException e) {
						RDTLog.logError(e);
					}
				}
			}
		}
	}
}

