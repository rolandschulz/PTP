package org.eclipse.photran.internal.ui.editor;

import java.util.HashSet;

import org.eclipse.photran.internal.core.f95parser.Terminal;

/**
 * @author nchen
 * @author cheah
 * 
 * This returns an hashtable of words that require indentation in Fortran The
 * FortranPartitionScanner class would then make use of it as it checks to see
 * if we will auto-indent or not.
 * 
 */
public class ListOfWordsToIndent {

	/**
	 * @author cheah
	 * @author nchen
	 *
	 * This class stores a tuple
	 */
	static class Pair {
		public Object fFirst = null;

		public Object fSecond = null;

		public Pair(Object first, Object second) {
			fFirst = first;
			fSecond = second;
		}

		/*
		 * String accessors
		 */
		public String firstAsString() {
			return (String) fFirst;
		}

		public String secondAsString() {
			return (String) fSecond;
		}
	}

	// Augment this list as necessary
	private static Pair[] fortranKeywordsPairs = new Pair[] {
			new Pair(Terminal.T_PROGRAM, Terminal.T_ENDPROGRAM),
			new Pair(Terminal.T_FUNCTION, Terminal.T_ENDFUNCTION),
			new Pair(Terminal.T_SUBROUTINE, Terminal.T_ENDSUBROUTINE),
			new Pair(Terminal.T_MODULE, Terminal.T_ENDMODULE),
			new Pair(Terminal.T_BLOCKDATA, Terminal.T_ENDBLOCKDATA),
			new Pair(Terminal.T_TYPE, Terminal.T_ENDTYPE),
			new Pair(Terminal.T_FORALL, Terminal.T_ENDFORALL),
			new Pair(Terminal.T_WHERE, Terminal.T_ENDWHERE),
			new Pair(Terminal.T_ELSEWHERE, Terminal.T_ENDWHERE),
			new Pair(Terminal.T_IF, Terminal.T_ENDIF),
			new Pair(Terminal.T_ELSEIF, Terminal.T_ENDIF),
			new Pair(Terminal.T_SELECTCASE, Terminal.T_ENDSELECT),
			new Pair(Terminal.T_CASE, Terminal.T_END),
			new Pair(Terminal.T_DO, Terminal.T_ENDDO),
			new Pair(Terminal.T_INTERFACE, Terminal.T_ENDINTERFACE) };

	private static HashSet wordsToIndentRight = new HashSet();
	
	private static HashSet wordsToIndentLeft = new HashSet();
	
	private static ListOfWordsToIndent uniqueInstance;

	// Singleton
	private ListOfWordsToIndent() {
		//generate the hashtable from the list of keywords
		for (int index = 0; index < fortranKeywordsPairs.length; index++) {
			wordsToIndentRight.add((Terminal)(fortranKeywordsPairs[index].fFirst));
		}
		
		// add the one and only terminal to indent left
		wordsToIndentLeft.add((Terminal)Terminal.T_END);
	}

	/**
	 * Singleton implementation
	 * @return the unique instance
	 */
	public static ListOfWordsToIndent getListOfWordsToIndentInstance() {
		if (uniqueInstance == null)
			uniqueInstance = new ListOfWordsToIndent();

		return uniqueInstance;
	}

	/**
	 * Checks to see if the keyword causes indentation
	 * @param terminal
	 * @return
	 */
	public static boolean checkIfKeywordNeedsRightIndentation(Terminal terminal) {
		return getListOfWordsToIndentInstance().wordsToIndentRight
				.contains(terminal);
	}
	
	/**
	 * Checks to see if the keyword causes indentation
	 * @param terminal
	 * @return
	 */
	public static boolean checkIfKeywordNeedsLeftIndentation(Terminal terminal) {
		return getListOfWordsToIndentInstance().wordsToIndentLeft
				.contains(terminal);
	}
}
