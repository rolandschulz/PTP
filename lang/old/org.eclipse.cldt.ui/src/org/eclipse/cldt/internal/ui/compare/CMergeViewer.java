package org.eclipse.cldt.internal.ui.compare;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 


import org.eclipse.swt.widgets.Composite;

import org.eclipse.cldt.internal.ui.text.FortranSourceViewerConfiguration;
import org.eclipse.cldt.internal.ui.text.FortranTextTools;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.TokenComparator;


import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;

public class CMergeViewer extends TextMergeViewer {
	
	private static final String TITLE= "CMergeViewer.title"; //$NON-NLS-1$
		
		
	public CMergeViewer(Composite parent, int styles, CompareConfiguration mp) {
		super(parent, styles, mp);
	}
	
	public String getTitle() {
		return FortranUIPlugin.getResourceString(TITLE);
	}


	protected ITokenComparator createTokenComparator(String s) {
		return new TokenComparator(s);
	}
	
	protected IDocumentPartitioner getDocumentPartitioner() {
		return FortranUIPlugin.getDefault().getTextTools().createDocumentPartitioner();
	}
		
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			FortranTextTools tools= FortranUIPlugin.getDefault().getTextTools();
			((SourceViewer)textViewer).configure(new FortranSourceViewerConfiguration(tools, null));
		}
	}
}
