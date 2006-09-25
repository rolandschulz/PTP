package org.eclipse.photran.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.photran.internal.ui.editor.FortranPartitionScanner.Partition;
import org.eclipse.photran.internal.ui.preferences.FortranEditorPreferencePage;
import org.eclipse.photran.ui.FortranUIPlugin;

/**
 * The source viewer configuration for the Fortran editor.
 * 
 * A source viewer configuration describes what customizations (syntax
 * highlighting, content assist, formatting, double-click customizations,
 * auto-indent customizations) are provided by the editor.
 * 
 * @author joverbey
 * @author cheahcf
 */
public class FortranSourceViewerConfiguration extends SourceViewerConfiguration {
	protected AbstractFortranEditor editor;

	private PresentationReconciler reconciler;

	private FortranKeywordRuleBasedScanner scanner;

	FortranSourceViewerConfiguration(AbstractFortranEditor editor) {
		this.editor = editor;

	}

	// ----- TABS AND SPACES PREFERENCES ---------------------------------------

	/** 
	 * Copied from org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration
	 * Responsible for handling indentation on the currenly opened files
	 * and any new files that the user opens.
	 */
	
	//TODO: This can be an issue for fixed form that expects all tabs to be spaces.
	// And should not be left as tabs regardless of the preferences
	// A solution would be to have 2 sourceviewerconfigurations
	public String[] getIndentPrefixes(ISourceViewer sourceViewer,
			String contentType) {
		List list = new ArrayList();
		// prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces
		int tabWidth = getPreferenceStore().getInt(
				FortranEditorPreferencePage.TAB_WIDTH_PREF);
		boolean useSpaces = getPreferenceStore().getBoolean(
				FortranEditorPreferencePage.SPACES_FOR_TABS_PREF);
		for (int i = 0; i <= tabWidth; i++) {
			StringBuffer prefix = new StringBuffer();
			if (useSpaces) {
				for (int j = 0; j + i < tabWidth; j++)
					prefix.append(' ');
				if (i != 0)
					prefix.append('\t');
			} else {
				for (int j = 0; j < i; j++)
					prefix.append(' ');
				if (i != tabWidth)
					prefix.append('\t');
			}
			list.add(prefix.toString());
		}
		list.add(""); //$NON-NLS-1$
		return (String[]) list.toArray(new String[list.size()]);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTabWidth(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		return getPreferenceStore().getInt(FortranEditorPreferencePage.TAB_WIDTH_PREF);
	}

	/**
	 * 
	 * @return the global preference store
	 */
	protected IPreferenceStore getPreferenceStore() {
		return FortranUIPlugin.getDefault().getPreferenceStore();
	}
	
	// ----- AUTO-INDENTING STRATEGY ------------------------------------------
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {

		
		return new IAutoEditStrategy[] {/*new FortranAutoIndentStrategy(editor instanceof FortranFreeFormEditor)*/};
	}

	// ----- SYNTAX HIGHLIGHTING -----------------------------------------------

	/**
	 * Returns a list of the possible partitions' content types.
	 * 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return FortranPartitionScanner.getContentTypes();
	}

	/**
	 * Sets up rules for syntax highlighting.
	 * 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		reconciler = new PresentationReconciler();
		if (editor instanceof FortranFixedFormEditor || editor.getLinesOfCode() <= FortranFreeFormEditor.MAX_LINES_FOR_LEXER_BASED_SCANNER) {
			Partition[] partitionTypes = FortranPartitionScanner.getPartitionTypes();
			for (int i = 0; i < partitionTypes.length; i++) {
				Partition p = partitionTypes[i];
				FortranDamagerRepairer dr = new FortranDamagerRepairer(p.getTokenScanner());
				reconciler.setDamager(dr, p.getContentType());
				reconciler.setRepairer(dr, p.getContentType());
			}
			// return reconciler;
			// reconciler = new PresentationReconciler();
		} else {
			DefaultDamagerRepairer dr1 = new DefaultDamagerRepairer(getTagScanner());
			reconciler.setDamager(dr1, IDocument.DEFAULT_CONTENT_TYPE);
			reconciler.setRepairer(dr1, IDocument.DEFAULT_CONTENT_TYPE);
		}
		return reconciler;
	}

	private FortranKeywordRuleBasedScanner getTagScanner() {
        if (scanner == null)
            scanner = new FortranKeywordRuleBasedScanner();
        return scanner;
	}
}
