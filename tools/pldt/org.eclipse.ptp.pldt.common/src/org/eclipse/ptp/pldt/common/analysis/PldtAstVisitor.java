/**********************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.messages.Messages;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This dom-walker helper collects interesting constructs (currently
 * function calls and constants), and adds markers to the source file for
 * C/C++ code. <br>
 * This base class encapsulates the common behaviors for both C and C++
 * code and for visitors looking for MPI, OpenMP, LAPI, etc. etc. types of artifacts
 * 
 * @author Beth Tibbitts
 * @since 4.0
 * 
 */
public abstract class PldtAstVisitor extends CASTVisitor {

	/**
	 * @since 4.0
	 */
	public static String ARTIFACT_CALL = "Artifact Call"; //$NON-NLS-1$
	/**
	 * @since 4.0
	 */
	public static String ARTIFACT_CONSTANT = "Artifact Constant"; //$NON-NLS-1$
	protected static String ARTIFACT_NAME = "Artifact Name"; //$NON-NLS-1$
	protected static String PREFIX = ""; //$NON-NLS-1$
	/**
	 * Note that this is not final beause it can be dynamically modified by user-enabled tracing:
	 * See CommonPlugin.getTraceOn();
	 */
	private static boolean traceOn = false;

	private static boolean dontAskToModifyIncludePathAgain = false;
	protected boolean allowPrefixOnlyMatch = true;

	/**
	 * List of include paths that we'll probably want to consider in the work that this visitor does.
	 * For example, only paths found in this list (specified in PLDT preferences) would be considered
	 * to be a path from which definitions of "Artifacts" would be found. <br>
	 * Note that this can now be dynamically modified during artifact analysis, thus no longer final
	 */
	private List<String> includes_;
	private final String fileName;
	private final ScanReturn scanReturn;

	/**
	 * 
	 * @param includes
	 *            list of include paths that we'll probably want to consider in
	 *            the work that this visitor does
	 * @param fileName
	 *            the name of the file that this visitor is visiting(?)
	 * @param prefixOnlyMatch
	 *            if true, then artifact is recognized if it starts with the plugin-specific prefix
	 *            (e.g. "MPI_" etc.) instead of forcing a lookup of the location of the header file
	 *            in which the API is found. This proves to be difficult for users to get right, so prefix-only
	 *            recognition of artifacts is allowed here.
	 * @param scanReturn
	 *            the ScanReturn object to which the artifacts that we find will
	 *            be appended.
	 */
	public PldtAstVisitor(List<String> includes, String fileName, boolean prefixOnlyMatch, ScanReturn scanReturn) {
		this.includes_ = includes;
		this.fileName = fileName;
		this.scanReturn = scanReturn;
		dontAskToModifyIncludePathAgain = false;
		this.allowPrefixOnlyMatch = prefixOnlyMatch;
		if (!traceOn)
			traceOn = CommonPlugin.getTraceOn();
		if (traceOn)
			System.out.println("PldtAstVisitor, traceOn=" + traceOn); //$NON-NLS-1$
	}

	/**
	 * Constructor without prefixOnlyMatch arg, assumes false
	 */
	public PldtAstVisitor(List<String> includes, String fileName, ScanReturn scanReturn) {
		this(includes, fileName, false, scanReturn);
	}

	/**
	 * Skip statements that are included.
	 */
	public int visit(IASTStatement statement) {
		if (preprocessorIncluded(statement)) {
			return ASTVisitor.PROCESS_SKIP;
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}

	/**
	 * Visit an ast node of type IASTExpression. Most things tend to fall into this visit method. <br>
	 * Version from MPI originally that seems best for all. Handles recognition within macro expansions
	 * */
	public int visit(IASTExpression expression) {
		if (expression instanceof IASTFunctionCallExpression) {
			IASTExpression astExpr = ((IASTFunctionCallExpression) expression)
					.getFunctionNameExpression();
			String signature = astExpr.getRawSignature();
			// note: getRawSig is the name BEFORE being processed by preprocessor!
			// but it seems to be empty if it's different.

			// can we get post-pre-processor name here?
			if (astExpr instanceof IASTIdExpression) {
				IASTName tempFN = ((IASTIdExpression) astExpr).getName();
				IBinding tempBIND = tempFN.resolveBinding();
				String tempNAME = tempBIND.getName();
				if (traceOn)
					System.out.println("MCAV name: " + tempNAME + " rawsig: " + signature); //$NON-NLS-1$ //$NON-NLS-2$
				// if e.g. preprocessor substitution used, use that for function name
				boolean preProcUsed = !signature.equals(tempNAME);
				if (preProcUsed) {
					signature = tempNAME;
				}
			}
			// still is IASTFunctionCallExpression
			if (matchesPrefix(signature)) {
				if (astExpr instanceof IASTIdExpression) {
					IASTName funcName = ((IASTIdExpression) astExpr).getName();
					// IBinding binding = funcName.resolveBinding();
					// String name=binding.getName();// name ok for stdMake
					processFuncName(funcName, astExpr);
				}
			}
		} else if (expression instanceof IASTLiteralExpression) {
			processMacroLiteral((IASTLiteralExpression) expression);
		}
		return PROCESS_CONTINUE;
	}

	/**
	 * Skip decls that are included.
	 * 
	 * @param declaration
	 * @return
	 */
	public int visit(IASTDeclaration declaration) {// called; both paths get taken
		if (preprocessorIncluded(declaration)) {
			return ASTVisitor.PROCESS_SKIP;
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}

	/**
	 * Process a function name from an expression and determine if it should be
	 * marked as an Artifact. If so, append it to the scanReturn object that
	 * this visitor is populating.
	 * 
	 * An artifact is a function name (or other identifier) that was found in the include path, or matched with prefix,
	 * as defined in the preferences.
	 * 
	 * @param astExpr
	 * @param funcName
	 */
	public void processFuncName(IASTName funcName, IASTExpression astExpr) {
		// IASTTranslationUnit tu = funcName.getTranslationUnit();
		String strName = funcName.toString();

		final boolean usePref = this.allowPrefixOnlyMatch;
		if ((usePref && matchesPrefix(strName)) || (!usePref && isArtifact(funcName))) { // brt C++ test 2/16/10
			SourceInfo sourceInfo = getSourceInfo(astExpr, Artifact.FUNCTION_CALL);
			if (sourceInfo != null) {
				if (traceOn)
					System.out.println("found artifact: " + funcName.toString()); //$NON-NLS-1$
				// Note: we're determining the artifact name twice. (also in chooseName())
				String artName = funcName.toString();
				String rawName = funcName.getRawSignature();
				// String bName=funcName.getBinding().getName();
				if (!artName.equals(rawName)) {
					if (rawName.length() == 0)
						rawName = "  "; //$NON-NLS-1$
					artName = artName + "  (" + rawName + ")"; // indicate orig pre-pre-processor value in parens //$NON-NLS-1$ //$NON-NLS-2$
					// note: currently rawName seems to always be empty.
				}
				scanReturn.addArtifact(new Artifact(fileName, sourceInfo
						.getStartingLine(), 1, artName, sourceInfo));

			}
		}
	}

	/**
	 * Look for artifacts in an IASTExpression
	 * @param astExpr
	 */
	public void processExprWithConstant(IASTExpression astExpr) {
		IASTName funcName = ((IASTIdExpression) astExpr).getName();
		// IASTTranslationUnit tu = funcName.getTranslationUnit();
		String strName = funcName.toString();
		final boolean usePref = this.allowPrefixOnlyMatch;// only to make next line short/readable
		if ((usePref && matchesPrefix(strName)) || (!usePref && isArtifact(funcName))) {
			SourceInfo sourceInfo = getSourceInfo(astExpr, Artifact.FUNCTION_CALL);
			if (sourceInfo != null) {
				// System.out.println("found MPI artifact: " + funcName.toString());
				scanReturn.addArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1,
						funcName.toString(), sourceInfo));
			}
		}
	}

	/**
	 * Determines if the funcName is an instance of the type of artifact in
	 * which we are interested. <br>
	 * An artifact is a function name that was found in the include path (e.g. MPI or OpenMP),
	 * (or identified by prefix-only match, which is now the default)
	 * as defined in the PLDT preferences.
	 * 
	 * @param funcName
	 */
	protected boolean isArtifact(IASTName funcName) {
		IBinding binding = funcName.resolveBinding();
		String name = binding.getName();
		String rawSig = funcName.getRawSignature();
		name = chooseName(name, rawSig);

		IASTTranslationUnit tu = funcName.getTranslationUnit();

		// Use index instead of full AST for the header file inclusion stuff
		// Without full AST, further introspection into APIs will need to
		// explicitly ask for it from the Index
		IName[] names = tu.getDeclarations(binding); // get from the index not ast of e.g. header files
		for (int i = 0; i < names.length; i++) {
			IName name2 = names[i];
			IASTFileLocation floc = name2.getFileLocation();
			if (floc == null) {
				if (traceOn)
					System.out.println("PldtAstVisitor  IASTFileLocn null for " + name2 + " (" + funcName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return false; // (e.g. 'ptr' )
			}
			String filename = floc.getFileName();
			IPath path = new Path(filename);
			if (isInIncludePath(path)) {
				// System.out.println("    found "+path+"  in artifact path (via index)!");
				return true;
			} else {
				if (traceOn) {
					System.out.println(name + " was found in " + path
							+ " but  PLDT preferences have been set to only include: " + includes_.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				}
				// add them here?
				if (allowIncludePathAdd()) {
					boolean addit = addIncludePath(path, name, dontAskToModifyIncludePathAgain);
					if (addit)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Choose how to distinguish between binding name, and raw signature.<br>
	 * Could be overridden by subclasses if, for example, a name with a prefix e.g. "MPI::foo" should be preferred over "foo".<br>
	 * Here, the default case is that we always choose the regular/binding name, unless it's empty, in which case we choose the
	 * rawSignature.
	 * 
	 * @param bindingName
	 * @param rawSignature
	 * @return
	 */
	protected String chooseName(String bindingName, String rawSignature) {
		String name = bindingName;
		if (bindingName.length() == 0) {
			name = rawSignature;
		}
		return name;
	}

	public void processMacroLiteral(IASTLiteralExpression expression) {
		IASTNodeLocation[] locations = expression.getNodeLocations();
		if ((locations.length == 1) && (locations[0] instanceof IASTMacroExpansion)) {// path taken &not
			// found a macro, does it come from the include path required to be "one of ours"?
			IASTMacroExpansion astMacroExpansion = (IASTMacroExpansion) locations[0];
			IASTPreprocessorMacroDefinition preprocessorMacroDefinition = astMacroExpansion
					.getMacroDefinition();
			// String shortName =
			// preprocessorMacroDefinition.getName().toString()+'='+literal;
			String shortName = preprocessorMacroDefinition.getName().toString();
			IASTNodeLocation[] preprocessorLocations = preprocessorMacroDefinition
					.getNodeLocations();
			while ((preprocessorLocations.length == 1)
					&& (preprocessorLocations[0] instanceof IASTMacroExpansion)) {
				preprocessorLocations = ((IASTMacroExpansion) preprocessorLocations[0])
						.getMacroDefinition().getNodeLocations();
			}

			if ((preprocessorLocations.length == 1)
					&& isInIncludePath(new Path(preprocessorLocations[0].asFileLocation()
							.getFileName()))) {
				SourceInfo sourceInfo = getSourceInfo(astMacroExpansion);
				if (sourceInfo != null) {
					scanReturn.addArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1, // column:
							shortName, sourceInfo));
				}

			}
		}
	}

	/**
	 * Is this path found in the include path in which we are interested?
	 * E.g. is it in the include path specified in PLDT preferences,
	 * which would identify it as an artifact of interest?
	 * 
	 * @param includeFilePath
	 *            under consideration
	 * @return true if this is found in the include path from PLDT preferences
	 */
	private boolean isInIncludePath(IPath includeFilePath) {
		if (includeFilePath == null)
			return false;
		for (String includeDir : includes_) {
			IPath includePath = new Path(includeDir);
			if (traceOn)
				System.out.println("PldtAstVisitor: is " + includeFilePath + " found in " + includeDir + "?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (includePath.isPrefixOf(includeFilePath))
				return true;
		}
		return false;
	}

	/**
	 * Get exact source location info for a function call
	 * 
	 * @param astExpr
	 * @param constructType
	 * @return
	 */
	private SourceInfo getSourceInfo(IASTExpression astExpr, int constructType) {
		SourceInfo sourceInfo = null;
		IASTNodeLocation[] locations = astExpr.getNodeLocations();
		if (locations.length == 1) {
			IASTFileLocation astFileLocation = null;
			if (locations[0] instanceof IASTFileLocation) {
				astFileLocation = (IASTFileLocation) locations[0];
			}
			// handle the case e.g. #define foo MPI_fn - recognize foo() as MPI_fn()
			else if (locations[0] instanceof IASTMacroExpansion) {
				IASTMacroExpansion me = (IASTMacroExpansion) locations[0];
				astFileLocation = me.asFileLocation();
			} // will it be a (new, replacing IASTMacroExpansion) IASTMacroExpansionLocation now??
			if (astFileLocation != null) {
				sourceInfo = new SourceInfo();
				sourceInfo.setStartingLine(astFileLocation.getStartingLineNumber());
				sourceInfo.setStart(astFileLocation.getNodeOffset());
				sourceInfo.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
				sourceInfo.setConstructType(constructType);
			}
		}
		return sourceInfo;
	}

	/**
	 * Get exact source location info for a constant originated from Macro
	 * expansion(s)
	 * 
	 * @param iASTMacroExpansion
	 *            represents the original macro
	 * @return
	 */
	private SourceInfo getSourceInfo(IASTMacroExpansion iASTMacroExpansion) {
		SourceInfo sourceInfo = null;
		IASTFileLocation iASTFileLocation = iASTMacroExpansion.asFileLocation();
		sourceInfo = new SourceInfo();
		sourceInfo.setStartingLine(iASTFileLocation.getStartingLineNumber());
		sourceInfo.setStart(iASTFileLocation.getNodeOffset());
		sourceInfo.setEnd(iASTFileLocation.getNodeOffset() + iASTFileLocation.getNodeLength());
		sourceInfo.setConstructType(Artifact.CONSTANT);

		return sourceInfo;
	}

	private boolean preprocessorIncluded(IASTNode astNode) {
		if (astNode.getFileLocation() == null)
			return false;
		String location = astNode.getFileLocation().getFileName();
		String tuFilePath = astNode.getTranslationUnit().getFilePath();
		return !location.equals(tuFilePath);
	}

	/**
	 * Look for artifacts within a IASTIdExpression
	 * @param expression
	 */
	public void processIdExprAsLiteral(IASTIdExpression expression) {
		IASTName name = expression.getName();
		String strName = name.toString();
		if ((this.allowPrefixOnlyMatch && matchesPrefix(strName)) || isArtifact(name)) {// brt C++ test 2/16/10
			SourceInfo sourceInfo = getSourceInfo(expression, Artifact.CONSTANT);
			if (sourceInfo != null) {
				scanReturn.addArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1, // column:
						name.toString(), sourceInfo));
			}
		}
	}

	/**
	 * allow dynamic adding to include path? Can be overridden by derived classes.
	 * 
	 * @return
	 */
	public boolean allowIncludePathAdd() {
		return !dontAskToModifyIncludePathAgain;
	}

	/**
	 * Replace the includes list in this visitor so the change will be recognized.
	 * 
	 * @param includes
	 */
	@SuppressWarnings("unchecked")
	protected void replaceIncludes(String includes) {
		includes_ = convertToList(includes);
	}

	/**
	 * Convert a string to a list with given delimiters
	 * @param stringList
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public List convertToList(String stringList)
	{
		StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r");//$NON-NLS-1$
		List dirs = new ArrayList();
		while (st.hasMoreElements()) {
			dirs.add(st.nextToken());
		}
		return dirs;
	}

	/**
	 * Add an include path to the prefs - probably found dynamically during analysis
	 * and requested to be added by the user <br>
	 * Note that the path will be to the actual file in which the name was found;
	 * the path that will be added to the prefs is the parent directory of that file.
	 * 
	 * @param path
	 * @param name
	 *            the name (function etc) that was found in the path
	 * @param dontAskAgain
	 *            initial value of toggle "don't ask again"
	 * 
	 * @returns whether the user chose to add the path or not
	 */
	public boolean addIncludePath(IPath path, String name/* , IPreferenceStore store, String id */, boolean dontAskAgain) {

		IPreferenceStore store = getPreferenceStore();
		String id = getIncludesPrefID();
		String type = getTypeName();
		boolean doitThisTime = false;

		if (store == null || id == null) {
			CommonPlugin.log(IStatus.ERROR, "PLDT: Visitor subclass does not implement getPreferenceStore() or " + //$NON-NLS-1$
					"getIncludesPrefID() to return non-null values."); //$NON-NLS-1$
			return false;
		}

		try {
			String value = store.getString(id);
			if (traceOn)
				System.out.println("value: " + value); //$NON-NLS-1$

			if (!dontAskAgain) {
				// probably inefficient string construction, but rarely called.
				String msg = Messages.PldtAstVisitor_20
						+ name
						+ Messages.PldtAstVisitor_21
						+ path.toString()
						+ Messages.PldtAstVisitor_22
						+ type
						+ Messages.PldtAstVisitor_23
						+ value
						+ Messages.PldtAstVisitor_24;
				String title = Messages.PldtAstVisitor_25 + type + Messages.PldtAstVisitor_26;
				boolean[] twoAnswers = askUI(title, msg, dontAskToModifyIncludePathAgain);
				doitThisTime = twoAnswers[0];
				dontAskAgain = twoAnswers[1];
				dontAskToModifyIncludePathAgain = dontAskAgain;
			}

			if (doitThisTime) {
				String s = java.io.File.pathSeparator;
				String parent = path.toFile().getParent();
				// add path separator (: or ; ?) if necessary
				if (!value.endsWith(s)) {
					value += s;
				}
				// add this new include path location to the value stored in
				// preferences, and add it to the value within this class as well.
				value += parent + s;
				store.putValue(id, value);
				replaceIncludes(value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doitThisTime;
	}

	/**
	 * needs to be overrridden for derived classes that need to dynamically update the pref store
	 * e.g. for the includes path. This type name is used for messages, etc.
	 * 
	 * @return artifact type name such as "MPI", "OpenMP" etc.
	 */
	protected String getTypeName() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * needs to be overrridden for derived classes that need to dynamically update the pref store
	 * e.g. for the includes path
	 * 
	 * @return
	 */
	protected String getIncludesPrefID() {
		return null;
	}

	/**
	 * needs to be overrridden for derived classes that need to dynamically update the pref store
	 * e.g. for the includes path
	 * 
	 * @return
	 */
	protected IPreferenceStore getPreferenceStore() {
		return null;
	}

	/**
	 * Dialog to ask a question in the UI thread, and return its answer plus a persistent
	 * setting for not asking the same question again.  Users get tired of the same old question!
	 * 
	 * @author beth tibbitts
	 * 
	 * @param title
	 * @param message
	 * @param dontAskAgain allows persistent setting to not ask this question again
	 * @return
	 */
	public boolean[] askUI(final String title, final String message, boolean dontAskAgain) {
		boolean[] twoAnswers = new boolean[2];
		RunGetAnswer runner = new RunGetAnswer(title, message, dontAskAgain);

		Display.getDefault().syncExec(runner);
		boolean answer = runner.getAnswer();
		dontAskAgain = runner.getDontAskAgain();
		twoAnswers[0] = answer;
		twoAnswers[1] = dontAskAgain;
		return twoAnswers;
	}

	/**
	 * Runnable used by askUI to ask a question in the UI thread
	 * 
	 * @author beth
	 * 
	 */
	class RunGetAnswer implements Runnable {
		boolean answer, dontAskAgain;
		String title, message;

		RunGetAnswer(String title, String message, boolean initialToggleState) {
			this.title = title;
			this.message = message;
			this.dontAskAgain = initialToggleState;
		}

		public void run() {
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow w = wb.getActiveWorkbenchWindow();
			Shell shell = w.getShell();
			if (shell == null) {
				Display display = CommonPlugin.getStandardDisplay();
				shell = display.getActiveShell();
			}

			// see also: openYesNoCancelQuestion
			String toggleMessage = Messages.PldtAstVisitor_28;
			IPreferenceStore store = null;
			String key = null;
			MessageDialogWithToggle md;
			md = MessageDialogWithToggle.openYesNoQuestion(shell, title, message, toggleMessage, dontAskAgain, store, key);
			int retCode = md.getReturnCode(); // yes=2
			answer = (retCode == 2);
			dontAskAgain = md.getToggleState();
		}

		public boolean getAnswer() {
			return answer;
		}

		public boolean getDontAskAgain() {
			return dontAskAgain;
		}
	}

	/**
	 * will be overridden where needed; note that for C code, the test for if
	 * the prefix matches has already been done before this is called so this
	 * test isn't necessary. In this case the subclass should implement it and always return true,
	 * but with increased use of "recognize artifact by prefix only" this becomes more important.
	 * FIXME improve this convoluted logic
	 * 
	 * @param name
	 * @return
	 * @since 4.0
	 */
	abstract public boolean matchesPrefix(String name);

}