/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.fdt.internal.ui.text.c.hover;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.CoreModel;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.ICProject;
import org.eclipse.fdt.core.model.ISourceReference;
import org.eclipse.fdt.core.model.ITranslationUnit;
import org.eclipse.fdt.core.model.IWorkingCopy;
import org.eclipse.fdt.core.search.BasicSearchResultCollector;
import org.eclipse.fdt.core.search.ICSearchConstants;
import org.eclipse.fdt.core.search.ICSearchScope;
import org.eclipse.fdt.core.search.IMatch;
import org.eclipse.fdt.core.search.OrPattern;
import org.eclipse.fdt.core.search.SearchEngine;
import org.eclipse.fdt.internal.ui.codemanipulation.StubUtility;
import org.eclipse.fdt.internal.ui.text.CCodeReader;
import org.eclipse.fdt.internal.ui.util.Strings;
import org.eclipse.fdt.ui.CUIPlugin;
import org.eclipse.fdt.ui.IWorkingCopyManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * CSourceHover
 */
public class CSourceHover extends AbstractCEditorTextHover implements ITextHoverExtension, IInformationProviderExtension2 {

	/**
	 * 
	 */
	public CSourceHover() {
		super();
	}

	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IEditorPart editor = getEditor();
		if (editor != null) {
			IEditorInput input= editor.getEditorInput();
			IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();				
			IWorkingCopy copy = manager.getWorkingCopy(input);
			if (copy == null) {
				return null;
			}
			
			String expression;
			try {
				expression = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
				expression = expression.trim();
				if (expression.length() == 0)
					return null;

				ICElement curr = copy.getElement(expression);
				if (curr == null) {
					// Try with the indexer
					curr = findMatches(expression);
					if (curr == null) {
						return null;
					}
				}
				String source= ((ISourceReference) curr).getSource();
				if (source == null || source.trim().length() == 0)
					return null;

				source= removeLeadingComments(source);
				String delim= null;

				try {
					delim= StubUtility.getLineDelimiterUsed(curr);
				} catch (CModelException e) {
					delim= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}

				String[] sourceLines= Strings.convertIntoLines(source);
				String firstLine= sourceLines[0];
				if (!Character.isWhitespace(firstLine.charAt(0)))
					sourceLines[0]= ""; //$NON-NLS-1$
				Strings.trimIndentation(sourceLines, getTabWidth());

				if (!Character.isWhitespace(firstLine.charAt(0)))
					sourceLines[0]= firstLine;

				source = Strings.concatenate(sourceLines, delim);
				return source;

			} catch (BadLocationException e) {
			} catch (CModelException e) {
			}
		}
		return null;
	}

	private static int getTabWidth() {
		return 4;
	}


	private String removeLeadingComments(String source) {
		CCodeReader reader= new CCodeReader();
		IDocument document= new Document(source);
		int i;
		try {
			reader.configureForwardReader(document, 0, document.getLength(), true, false);
			int c= reader.read();
			while (c != -1 && (c == '\r' || c == '\n')) {
				c= reader.read();
			}
			i= reader.getOffset();
			reader.close();
		} catch (IOException ex) {
			i= 0;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				CUIPlugin.getDefault().log(ex);
			}
		}

		if (i < 0)
			return source;
		return source.substring(i);
	}

	private ICElement findMatches(String name) {
		IEditorPart editor = getEditor();
		if (editor != null) {
			IEditorInput input= editor.getEditorInput();
			IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();				
			IWorkingCopy copy = manager.getWorkingCopy(input);

			if (copy != null) {
				try {
					BasicSearchResultCollector searchResultCollector = new BasicSearchResultCollector();			
					ICProject cproject = copy.getCProject();
					ICSearchScope scope = SearchEngine.createCSearchScope(new ICElement[]{cproject}, true);
					OrPattern orPattern = new OrPattern();
					orPattern.addPattern(SearchEngine.createSearchPattern( 
							name, ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS, false));
					orPattern.addPattern(SearchEngine.createSearchPattern( 
							name, ICSearchConstants.TYPE, ICSearchConstants.DEFINITIONS, false));
					orPattern.addPattern(SearchEngine.createSearchPattern( 
							name, ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, false));
					orPattern.addPattern(SearchEngine.createSearchPattern( 
							name, ICSearchConstants.MACRO, ICSearchConstants.DECLARATIONS, false));				
					orPattern.addPattern(SearchEngine.createSearchPattern( 
							name, ICSearchConstants.VAR, ICSearchConstants.DECLARATIONS, false));
					orPattern.addPattern(SearchEngine.createSearchPattern( 
							name, ICSearchConstants.FUNCTION, ICSearchConstants.DECLARATIONS, false));
					
					SearchEngine searchEngine = new SearchEngine();
					searchEngine.setWaitingPolicy(ICSearchConstants.FORCE_IMMEDIATE_SEARCH);
					searchEngine.search(CUIPlugin.getWorkspace(), orPattern, scope, searchResultCollector, true);
					
					Set set = searchResultCollector.getSearchResults();
					if (set != null && set.size() > 0 ) {
						IMatch[] matches = new IMatch[set.size()];
						set.toArray(matches);
						IResource resource = matches[0].getResource();
						if (resource != null) {
							ICElement celement = CoreModel.getDefault().create(resource);
							if (celement instanceof ITranslationUnit) {							
								return ((ITranslationUnit)celement).getElement(name);
							}
						}
					}
				}catch (InterruptedException e) {
					//
				} catch (CModelException e) {
					//
				}
			}
		}
		return null;
	}


	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new SourceViewerInformationControl(parent, getTooltipAffordanceString());
			}
		};
	}

	/*
	 * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
	 * @since 3.0
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int shellStyle= SWT.RESIZE;
				int style= SWT.V_SCROLL | SWT.H_SCROLL;				
				return new SourceViewerInformationControl(parent, shellStyle, style);
			}
		};
	}
}
