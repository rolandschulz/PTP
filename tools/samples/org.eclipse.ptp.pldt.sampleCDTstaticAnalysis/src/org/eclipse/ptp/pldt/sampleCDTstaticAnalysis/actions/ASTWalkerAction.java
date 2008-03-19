package org.eclipse.ptp.pldt.sampleCDTstaticAnalysis.actions;

import java.util.Iterator;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This was initially built from the "Sample Action" plugin example.
 * 
 * This sample action walks an AST tree for a selected C source file.
 * Select a C source file in the Projects view and click the icon in the toolbar
 * to print AST walking information.
 * 
 * @see IWorkbenchWindowActionDelegate
 * 
 * @author Beth Tibbitts tibbitts@us.ibm.com
 */
public class ASTWalkerAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private IStructuredSelection selection;

	/**
	 * The constructor.
	 */
	public ASTWalkerAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		MessageDialog.openInformation(window.getShell(), "CdtAST2 Plug-in",
				"See Console for AST walking results");
		try {
			runSelectionExample(selection);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		}

	}

	public void runSelection(ISelection selection) {
		System.out.println("selection=" + selection);
		if (selection instanceof ITranslationUnit) {
			System.out.println("   ITranslationUnit");
		}
		if (selection instanceof ICElement) {
			System.out.println("ICElement");
			walkICElement((ICElement) selection);

		}
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			// System.out.println(" IStructuredSelection, contents:");

			for (Iterator iter = ss.iterator(); iter.hasNext();) {
				Object obj = (Object) iter.next();
				// It can be a Project, Folder, File, etc...
				if (obj instanceof IAdaptable) {
					IAdaptable iad = (IAdaptable) obj;
					final IResource res = (IResource) iad
							.getAdapter(IResource.class);
					System.out.println("     got resource: " + res);

					// ICElement covers folders and translationunits
					final ICElement ce = (ICElement) iad
							.getAdapter(ICElement.class);// cdt40
					System.out.println("     got ICElement: " + ce);

					ITranslationUnit tu = (ITranslationUnit) iad
							.getAdapter(ITranslationUnit.class);
					System.out.println("     got ITranslationUnit: " + tu);
					try {
						listFlatInfo(tu);
						walkITU(tu);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}// end for
		}
	}

	public void runSelectionExample(ISelection selection) throws CoreException {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			for (Iterator iter = ss.iterator(); iter.hasNext();) {
				Object obj = (Object) iter.next();
				// It can be a Project, Folder, File, etc...
				if (obj instanceof IAdaptable) {
					IAdaptable iad = (IAdaptable) obj;
					final IResource res = (IResource) iad
							.getAdapter(IResource.class);
					System.out.println("     got resource: " + res);

					// ICElement covers folders and translation units
					final ICElement ce = (ICElement) iad
							.getAdapter(ICElement.class);// cdt40
					System.out.println("     got ICElement: " + ce);

					ITranslationUnit tu = (ITranslationUnit) iad
							.getAdapter(ITranslationUnit.class);
					System.out.println("     got ITranslationUnit: " + tu);
					System.out.println("\n==========listFlatInfo():");
					listFlatInfo(tu);
					System.out.println("\n==========walkITU():");
					walkITU(tu);
					System.out.println("\n==========walkITU_AST():");
					walkITU_AST(tu);

				}
			}
		}
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void walkICElement(ICElement ice) {
		// ICElement ice = (ICElement)selection;
		String eName = ice.getElementName();
		System.out.println("ICElement name: " + eName);

	}

	/**
	 * Walk the ITranslationUnit with a visitor, which (only) visits ICElements
	 * 
	 * @param tu
	 * @throws CoreException
	 */
	private void walkITU(ITranslationUnit tu) throws CoreException {
		String tuName = tu.getElementName();
		System.out.println("ITranslationUnit name: " + tuName);
		tu.accept(new ICElementVisitor() {
			public boolean visit(ICElement element) throws CoreException {
				boolean visitChildren = true;
				System.out.println("Visiting: " + element.getElementName());
				return visitChildren;
			}
		});
		System.out.println("AST visitor for " + tuName);
		IASTTranslationUnit ast = tu.getAST();
		ast.accept(new MyASTVisitor());
	}

	/**
	 * Walk the IASTTranslationUnit's AST tree, which can visit many types of
	 * nodes
	 * 
	 * @param tu
	 * @throws CoreException
	 */
	private void walkITU_AST(ITranslationUnit tu) throws CoreException {
		System.out.println("AST visitor for " + tu.getElementName());
		IASTTranslationUnit ast = tu.getAST();
		System.out
				.println("\n=================MyASTVisitor2 also implements leave() \n");
		ast.accept(new MyASTVisitor2());
	}

	class MyASTVisitor extends ASTVisitor {

		MyASTVisitor() {
			this.shouldVisitStatements = true; // lots more
			this.shouldVisitDeclarations = true;
		}

		public int visit(IASTStatement stmt) { // lots more
			String sig = stmt.getRawSignature();
			if (sig.length() > 0)
				System.out.println("Visiting stmt: " + stmt.getRawSignature());
			else if (stmt instanceof IASTCompoundStatement) {
				IASTCompoundStatement cstmt = (IASTCompoundStatement) stmt;
				IASTStatement[] stmts = cstmt.getStatements();
				System.out.println("Visiting compound stmt with stmts: "
						+ stmts.length);
				for (IASTStatement st : stmts) {
					String rawSig = st.getRawSignature();

					if (rawSig.length() == 0) {
						System.out.println("   ->" + st);
						if (st instanceof CASTExpressionStatement) {
							CASTExpressionStatement es = (CASTExpressionStatement) st;
							IASTExpression exp = es.getExpression();
							if (exp instanceof IASTBinaryExpression) {
								IASTBinaryExpression bexp = (IASTBinaryExpression) exp;

								System.out.println("    binary exp: "
										+ bexp.getOperand1() + " "
										+ bexp.getOperator() + " "
										+ bexp.getOperand2());
							}
							String expStr = exp.getRawSignature();
							IType type = exp.getExpressionType();
						}
					} else {
						System.out.println("   ->" + rawSig);
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		public int visit(IASTDeclaration decl) {
			System.out.println("Visiting decl: " + decl.getRawSignature());
			return PROCESS_CONTINUE;
		}

	}


	/** Visitor that uses leave() to show nesting */
	class MyASTVisitor2 extends ASTVisitor {

		MyASTVisitor2() {
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;
			this.shouldVisitNames = true;
		}

		/**
		 * Print statement info: if altered by preprocessor, then
		 * getRawSignature() is empty; use alternate representation
		 */
		public int visit(IASTStatement stmt) {
			String sig = stmt.getRawSignature();
			if (sig.length() > 0)
				System.out.println("Visiting stmt: " + stmt.getRawSignature());
			else
				System.out.println("Visiting stmt: " + stmt.toString());
			return PROCESS_CONTINUE;
		}

		public int visit(IASTDeclaration decl) {
			System.out.println("Visiting decl: " + decl.getRawSignature());
			return PROCESS_CONTINUE;
		}

		public int visit(IASTName name) {
			String prtName = name.toString();
			if (prtName.length() == 0)
				prtName = name.getRawSignature(); // use pre pre-processor
			// value
			System.out.println("Visiting name: " + prtName);
			return PROCESS_CONTINUE;
		}

		public int leave(IASTStatement stmt) {
			System.out.println(" Leaving stmt: " + stmt.getRawSignature());
			return PROCESS_CONTINUE;
		}

		public int leave(IASTDeclaration decl) {
			System.out.println(" Leaving decl: " + decl.getRawSignature());
			return PROCESS_CONTINUE;
		}

		public int leave(IASTName name) {
			System.out.println(" Leaving name: " + name);// possibly empty
			return PROCESS_CONTINUE;
		}
	}

	/**
	 * List "flat info" type queries that return lists of things on the ast
	 * 
	 * @param tu
	 * @throws CoreException
	 */
	void listFlatInfo(ITranslationUnit tu) throws CoreException {
		IASTTranslationUnit ast = tu.getAST();

		System.out.println("AST for: " + ast.getContainingFilename());

		IASTPreprocessorStatement[] ppss = ast.getAllPreprocessorStatements();
		System.out.println("PreprocessorStmts: (omit /usr/...)");
		for (int i = 0; i < ppss.length; i++) {
			IASTPreprocessorStatement pps = ppss[i];
			String fn = pps.getContainingFilename();
			if (!fn.startsWith("/usr")) {
				System.out.println(i + "  PreprocessorStmt: " + lastpart(fn)
						+ " " + pps.getRawSignature());
			}
		}
		IASTDeclaration[] decls = ast.getDeclarations();
		System.out.println("Declarations: (omit /usr/...)");
		for (int i = 0; i < decls.length; i++) {
			IASTDeclaration decl = decls[i];
			String fn = decl.getContainingFilename();
			if (!fn.startsWith("/usr")) {
				System.out.println(i + "  Declaration: " + lastpart(fn) + " "
						+ decl.getRawSignature());
			}
		}
		IASTPreprocessorStatement[] idirs = ast.getIncludeDirectives();
		// recurses thru includes included by other includes...
		System.out.println("Include directives: ");
		for (int i = 0; i < idirs.length; i++) {
			IASTPreprocessorStatement idir = idirs[i];
			System.out.println(i + " include directive: " + idir);

		}

		IDependencyTree dt = ast.getDependencyTree();
		IASTInclusionNode[] ins = dt.getInclusions();
		// this lists only the includes immediately included by this src file
		System.out.println("Dependency tree/Include statements:");
		for (int i = 0; i < ins.length; i++) {
			IASTInclusionNode in = ins[i];
			IASTPreprocessorIncludeStatement is = in.getIncludeDirective();
			System.out.println(i + "  include stmt: " + is);
		}
		// note: comments are not available as AST Nodes but are available here.
		// (In spite of the existence of IASTComment class and visit(IASTComment) method.)
		// These are deprecated in CDT 5.0
		ast = tu.getAST(null,ITranslationUnit.AST_CREATE_COMMENT_NODES);
		IASTComment[] cmts = ast.getComments();
		System.out.println("Comments found: "+cmts.length);
		for (int i = 0; i < cmts.length; i++) {
			IASTComment comment = cmts[i];
			char[] cmtChar=comment.getComment();
			String cmtStr=String.valueOf(cmtChar);
			
			System.out.println(i + " Comment: [" + cmtStr + "] rawSig: ["+comment.getRawSignature()+"] line "
					+ comment.getFileLocation());

		}

	}

	/**
	 * Return last part of string pathname: e.g. a/b/c/foo.c returns foo.c
	 * 
	 * @param pathname
	 * @return
	 */
	String lastpart(String pathname) {
		int loc = pathname.lastIndexOf('/');
		return pathname.substring(loc + 1);
	}
}