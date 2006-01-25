package org.eclipse.photran.internal.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.photran.internal.core.f95modelparser.ILexer;
import org.eclipse.photran.internal.core.f95modelparser.Lexer;
import org.eclipse.photran.internal.core.f95modelparser.Terminal;
import org.eclipse.photran.internal.core.f95modelparser.Token;
import org.eclipse.photran.internal.core.model.FortranModelBuilder;
import org.eclipse.photran.internal.ui.preferences.ColorPreferencePage;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Defines a lexer-based partition scanner for Fortran files.
 * 
 * Essentially, we run the lexer on the entire document, and each token becomes a single partition,
 * as does the space between tokens.
 * 
 * @author joverbey
 * @author nchen
 * @author cheahcf
 */
public final class FortranPartitionScanner implements IDocumentPartitioner {

	// ----- SET UP THE DIFFERENT TYPES OF PARTITIONS --------------------------

	static final String F90_STRING_CONSTANTS_PARTITION = "__fortran_string_constants";

	static final String F90_IDENTIFIER_PARTITION = "__fortran_identifier";

	static final String F90_KEYWORD_PARTITION = "__fortran_keyword";

	static final String F90_CODE_PARTITION = "__fortran_code";

	static final String F90_KEYWORD_PARTITION_WITH_INDENTATION_RIGHT = "__fortran_keyword_indentation_right";

	static final String F90_KEYWORD_PARTITION_WITH_INDENTATION_LEFT = "__fortran_keyword_indentation_left";

	private IDocument doc;

	private String filename;

	private ArrayList partitions = null;

	private boolean isFixedForm;

	public static class Partition {
		protected String contentType;

		protected ITokenScanner tokenScanner;

		protected org.eclipse.jface.text.rules.Token token;

		public Partition(String aContentType, TextAttribute color) {
			this.contentType = aContentType;

			// Default token scanner: return the entire partition
			// (there are no tokens within partitions)

			RuleBasedScanner rbs = new RuleBasedScanner();
			token = new org.eclipse.jface.text.rules.Token(color);
			rbs.setDefaultReturnToken(token);
			this.tokenScanner = rbs;
		}

		/**
		 * Each partition type has a unique name for its content type
		 * 
		 * @return the content type for this partition (a String)
		 */
		public final String getContentType() {
			return contentType;
		}

		/**
		 * @return a scanner that detects tokens within this type of partition.
		 */
		public final ITokenScanner getTokenScanner() {
			return tokenScanner;
		}

		/**
		 * This token is used to change the color
		 * 
		 * @return the token used for the current RuleBasedScanner
		 */
		public org.eclipse.jface.text.rules.Token getToken() {
			return token;
		}
	}

	// TODO-Nick: Refactor to be cleaner if there is a need for more types
	private static IPreferenceStore store = FortranUIPlugin.getDefault()
			.getPreferenceStore();

	private static Partition[] partitionTypes = new Partition[] {
		new Partition(
				F90_STRING_CONSTANTS_PARTITION,
				new TextAttribute(
						new Color(
								null,
								PreferenceConverter
								.getColor(
										store,
										ColorPreferencePage.F90_STRING_CONSTANTS_COLOR_PREF)))),
			new Partition(F90_IDENTIFIER_PARTITION, new TextAttribute(
					new Color(null, PreferenceConverter.getColor(store,
							ColorPreferencePage.F90_IDENTIFIER_COLOR_PREF)))),
			new Partition(F90_CODE_PARTITION, new TextAttribute(new Color(null,
					PreferenceConverter.getColor(store,
							ColorPreferencePage.F90_COMMENT_COLOR_PREF)))),
			new Partition(F90_KEYWORD_PARTITION, new TextAttribute(new Color(
					null, PreferenceConverter.getColor(store,
							ColorPreferencePage.F90_KEYWORD_COLOR_PREF)), null,
					SWT.BOLD)),
			new Partition(
					F90_KEYWORD_PARTITION_WITH_INDENTATION_RIGHT,
					new TextAttribute(
							new Color(null, PreferenceConverter.getColor(store,
									ColorPreferencePage.F90_KEYWORD_COLOR_PREF)),
							null, SWT.BOLD)),
			new Partition(
					F90_KEYWORD_PARTITION_WITH_INDENTATION_LEFT,
					new TextAttribute(
							new Color(null, PreferenceConverter.getColor(store,
									ColorPreferencePage.F90_KEYWORD_COLOR_PREF)),
							null, SWT.BOLD)) };

	IPropertyChangeListener colorPreferenceListener = new IPropertyChangeListener() {
		/*
		 * @see IPropertyChangeListener.propertyChange()
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (ColorPreferencePage.respondToPreferenceChange(event)) {
				updateColorPreferences();
				documentChanged(null);
			}
		}
	};

	private void updateColorPreferences() {
		partitionTypes[0].getToken().setData(
				new TextAttribute(new Color(null, PreferenceConverter.getColor(
						store,
						ColorPreferencePage.F90_STRING_CONSTANTS_COLOR_PREF))));
		partitionTypes[1]
				.getToken()
				.setData(
						new TextAttribute(
								new Color(
										null,
										PreferenceConverter
												.getColor(
														store,
														ColorPreferencePage.F90_IDENTIFIER_COLOR_PREF))));
		partitionTypes[2].getToken().setData(
				new TextAttribute(new Color(null, PreferenceConverter.getColor(
						store, ColorPreferencePage.F90_COMMENT_COLOR_PREF))));
		partitionTypes[3].getToken().setData(
				new TextAttribute(new Color(null, PreferenceConverter.getColor(
						store, ColorPreferencePage.F90_KEYWORD_COLOR_PREF)),
						null, SWT.BOLD));
		partitionTypes[4].getToken().setData(
				new TextAttribute(new Color(null, PreferenceConverter.getColor(
						store, ColorPreferencePage.F90_KEYWORD_COLOR_PREF)),
						null, SWT.BOLD));
		partitionTypes[5].getToken().setData(
				new TextAttribute(new Color(null, PreferenceConverter.getColor(
						store, ColorPreferencePage.F90_KEYWORD_COLOR_PREF)),
						null, SWT.BOLD));
		
	}

	/**
	 * Get a new FortranPartitionScanner Retains the doc if there is one
	 */
	public FortranPartitionScanner(String filename, boolean isFixedForm) {
		// JO: Take the document filename as a parameter so that we can
		// guess whether or not it's fixed form source

		this.filename = filename;
		this.isFixedForm = isFixedForm;
        
        FortranModelBuilder.forceFormat(filename, isFixedForm);

		FortranUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(colorPreferenceListener);
	}

	/**
	 * @return an array of FortranPartitionScanner.Partitions, each of which contains a name for its
	 *         content type, a predicate for detecting that type of partition, and a rule for
	 *         scanning tokens within that type of partition.
	 */
	static final Partition[] getPartitionTypes() {
		return partitionTypes;
	}

	/**
	 * @return an array of Strings which identify the content types of the various types of
	 *         partition for a photran file
	 */
	static final String[] getContentTypes() {
		String[] contentTypes = new String[partitionTypes.length];
		for (int i = 0; i < partitionTypes.length; i++)
			contentTypes[i] = partitionTypes[i].getContentType();
		return contentTypes;
	}

	/**
	 * @param terminal a terminal ID, from the Terminal class
	 * @return the name of the partition (one of the String constants above) corresponding to that
	 *         terminal
	 */
	private String mapTerminalToPartitionType(Terminal terminal) {
		if (terminal == Terminal.T_SCON)
			return F90_STRING_CONSTANTS_PARTITION;
		if (terminal == Terminal.T_IDENT)
			return F90_IDENTIFIER_PARTITION;
		if (ListOfWordsToIndent.checkIfKeywordNeedsRightIndentation(terminal))
			return F90_KEYWORD_PARTITION_WITH_INDENTATION_RIGHT;
		if (ListOfWordsToIndent.checkIfKeywordNeedsLeftIndentation(terminal))
			return F90_KEYWORD_PARTITION_WITH_INDENTATION_LEFT;
		return F90_KEYWORD_PARTITION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitioner#connect(org.eclipse.jface.text.IDocument)
	 */
	public void connect(IDocument document) {
		doc = document;
		documentChanged(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitioner#disconnect()
	 */
	public void disconnect() {
		doc = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitioner#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitioner#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public boolean documentChanged(DocumentEvent event) {
		// System.out.println("documentChanged");
		int endOfLastPartition = -1;
		// Get the text of the current document
		String documentText = doc.get();

		ArrayList newPartitions = new ArrayList();

		try {
			// Run it through the lexer
			ILexer lexer = Lexer.createLexer(new ByteArrayInputStream(doc.get()
					.getBytes()), this.filename, isFixedForm);

			// Create a partition for each token in the document, as well as
			// a partition for the space between tokens
			Token token;
			Terminal terminal;
			for (token = null, terminal = null; (terminal = (token = lexer
					.yylex()).getTerminal()) != Terminal.END_OF_INPUT;) {
				// JO: The fixed form lexer puts these in the stream
				// out of order, and they don't really matter to us
				// anyway since they're just whitespace
				if (terminal == Terminal.T_EOS)
					continue;

				int offset = token.getOffset();
				int length = token.getLength();
				String type = mapTerminalToPartitionType(terminal);
				// we know that the parser is initialized and is scanning!
				// but is it not returning the right offsets or the right types
				// System.err.println("offset: " + offset + " length: " + length
				// + " type: " + type + " text: " + token.getText());

				// Is there space between the last partition and this one?
				// Fill it!
				if (endOfLastPartition < offset) {
					int beginningOfGap = endOfLastPartition + 1;
					int lengthOfGap = offset - endOfLastPartition - 1;
					newPartitions.add(new TypedRegion(beginningOfGap,
							lengthOfGap, F90_CODE_PARTITION));
				}

				// And make a new partition
				newPartitions.add(new TypedRegion(offset, length, type));

				endOfLastPartition = offset + length - 1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			// Ignore lexer exceptions (e.g., unterminated string constant)
			e1.printStackTrace();
		}

		// Is there a gap between the last token and the end of the document?
		// Assume it's code...
		if (endOfLastPartition < documentText.length() - 1) {
			int beginningOfGap = endOfLastPartition + 1;
			int lengthOfGap = documentText.length() - endOfLastPartition - 1;
			newPartitions.add(new TypedRegion(beginningOfGap, lengthOfGap,
					F90_CODE_PARTITION));
		}

		// case for empty document

		if (newPartitions.size() == 0)
			newPartitions.add(new TypedRegion(0, 0, F90_CODE_PARTITION));

		// We should return true iff the partitioning has changed
		if (newPartitions.equals(partitions))
			return false;
		else {
			partitions = newPartitions;

			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getLegalContentTypes()
	 */
	public String[] getLegalContentTypes() {
		return getContentTypes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getContentType(int)
	 */
	public String getContentType(int offset) {
		int partitionNum = findRegionContainingOffset(offset);
		if (partitionNum >= 0)
			return ((ITypedRegion) partitions.get(partitionNum)).getType();
		else
			// Assume it's part of some code...
			return F90_CODE_PARTITION;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitioner#computePartitioning(int, int)
	 */
	public ITypedRegion[] computePartitioning(int offset, int length) {

		int firstRegion = findRegionContainingOffset(offset);
		int lastRegion = findRegionContainingOffset(offset + length - 1);

		if (firstRegion < 0)
			throw new Error("No region contains start offset " + offset + "!");
		if (lastRegion < 0)
			lastRegion = partitions.size() - 1;

		ITypedRegion[] ret = new ITypedRegion[lastRegion - firstRegion + 1];
		partitions.subList(firstRegion, lastRegion + 1).toArray(ret);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getPartition(int)
	 */
	public ITypedRegion getPartition(int offset) {
		int partitionNum = findRegionContainingOffset(offset);

		ITypedRegion lastPartition = (ITypedRegion) partitions.get(partitions
				.size() - 1);
		int lastOffsetOfLastPartition = lastPartition.getOffset()
				+ lastPartition.getLength() - 1;
		if (partitionNum < 0) {
			if (offset > lastOffsetOfLastPartition) {
				// Add a new partition to the end
				int newPartitionStart = lastOffsetOfLastPartition + 1;
				int newPartitionLength = offset - newPartitionStart + 1;
				partitions.add(new TypedRegion(newPartitionStart,
						newPartitionLength, F90_CODE_PARTITION));
				partitionNum = partitions.size() - 1;
			} else
				throw new Error("No region contains offset " + offset + "!");
		}

		return (ITypedRegion) partitions.get(partitionNum);

	}

	private int findRegionContainingOffset(int offset) {
		for (int i = 0; i < partitions.size(); i++) {
			ITypedRegion region = (ITypedRegion) partitions.get(i);
			int firstCharOffset = region.getOffset();
			int lastCharOffset = firstCharOffset + region.getLength() - 1;
			if (firstCharOffset <= offset && lastCharOffset >= offset)
				return i;
		}
		return -1;
	}
}
