package org.eclipse.ptp.rdt.ui.tests.navigation;

import java.net.URI;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.tests.text.EditorTestHelper;
import org.eclipse.cdt.ui.tests.text.selection.CPPSelectionTestsAnyIndexer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.internal.rdt.core.index.RemoteFastIndexer;
import org.eclipse.ptp.internal.rdt.ui.search.actions.OpenDeclarationsAction;
import org.eclipse.ptp.rdt.core.tests.ConnectionManager;
import org.eclipse.ptp.rdt.core.tests.RemoteTestProject;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;


@SuppressWarnings("restriction")

public class NavigationTests extends CPPSelectionTestsAnyIndexer {

	private static final int MAX_WAIT_TIME = 120000;

	/* I think a lot of the tests that fail do so because of two reasons:
	 * - Headers are not being skipped as they should be, 
	 *  
	 */
	private static final String[] TESTS_TO_RUN = {
		"testBug93281",
		"testBug207320",
		"testTemplateClassMethod_207320",
		//"testBasicDefinition", // FAIL cannot reproduce failure
		"testBasicTemplateInstance_207320",
		"testBug86829A",
		//"testCPPSpecDeclsDefs", // FAIL cannot reproduce failure
		"testBug168533",
		"testBug95225",
		//"testBug95202", // FAIL, should not fail when headers are skipped properly
		"testBug101287",
		"testBug102258",
		"testBug103323",
		"testBug78354",
		//"testBug103697", // FAIL trying to use local path which fails
		//"testBug108202", // FAIL
		//"testCNavigationInCppProject_bug183973", // FAIL
		//"testFuncWithTypedefForAnonymousStruct_190730", // FAIL
		//"testFuncWithTypedefForAnonymousEnum_190730", // FAIL
		"testMacroNavigation",
		"testMacroNavigation_Bug208300",
		"testIncludeNavigation",
		//"testNavigationCppCallsC", // FAIL
		//"testNavigationCCallsCpp", // FAIL
		"testNavigationInDefinedExpression_215906",
	};
	
	public static Test suite() {
		//return suite(NavigationTests.class);
		TestSuite suite = new TestSuite();
		for(String testName : TESTS_TO_RUN)
			suite.addTest(new NavigationTests(testName));
		return suite;
	}
	
	
	private static final String PROJECT_NAME = "navigation_test_project";
	
	@Override
	protected String getEditorID() {
		return "org.eclipse.ptp.rdt.ui.editor.CEditor";
	}

	@Override
	protected void waitUntilFileIsIndexed(IIndex index, IFile file, int maxmillis) throws Exception {
		Thread.sleep(10000);
        CCorePlugin.getIndexManager().joinIndexer(MAX_WAIT_TIME, new NullProgressMonitor());
	}

	public NavigationTests(String name) {
		super(name, RemoteFastIndexer.ID);
	}
	
	@Override
	protected IASTNode testF3(IFile file, int offset, int length) throws ParserException, CoreException {
		if (offset < 0)
			throw new ParserException("offset can not be less than 0 and was " + offset); //$NON-NLS-1$
		
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), getEditorID(), true); //$NON-NLS-1$
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof CEditor) {
        	CEditor editor= (CEditor) part;
    		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 500, 10);
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,length));
            
            final OpenDeclarationsAction action = (OpenDeclarationsAction) editor.getAction("OpenDeclarations"); //$NON-NLS-1$
            action.runSync();
			
        	// update the file/part to point to the newly opened IFile/IEditorPart
            part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(); 
            assertTrue (part instanceof CEditor);
            editor= (CEditor) part;
    		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 500, 10);

    		// the action above should highlight the declaration, so now retrieve it and use that selection to get the IASTName selected on the TU
            ISelection sel= editor.getSelectionProvider().getSelection();
            
            final IASTName[] result= {null};
            if (sel instanceof ITextSelection) {
            	final ITextSelection textSel = (ITextSelection)sel;
            	ITranslationUnit tu = (ITranslationUnit)editor.getInputCElement();
        		IStatus ok= ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_IF_OPEN, new NullProgressMonitor(), new ASTRunnable() {
        			public IStatus runOnAST(ILanguage language, IASTTranslationUnit ast) throws CoreException {
        				result[0]= ast.getNodeSelector(null).findName(textSel.getOffset(), textSel.getLength());
        				return Status.OK_STATUS;
        			}
        		});
        		assertTrue(ok.isOK());
				return result[0];
            }
        }
        
        return null;
    }
	

	protected void setUp() throws Exception {
		//super.setUp();
		
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference[] refs= page.getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			IViewReference viewReference = refs[i];
			page.setPartState(viewReference, IWorkbenchPage.STATE_RESTORED);
		}
		
		URI projectRootURI = ConnectionManager.getInstance().getWorkspaceURI(PROJECT_NAME);
		RemoteTestProject remoteProject = new RemoteTestProject(PROJECT_NAME, projectRootURI);
		
		ConnectionManager.getInstance().resetServiceModel(remoteProject.getName());
		ServiceModelManager.getInstance().printServiceModel();
		
		fCProject = remoteProject.getCProject();
	
		CCorePlugin.getIndexManager().setIndexerId(fCProject, RemoteFastIndexer.ID);
	}
	
	
	protected void tearDown() throws Exception {
		super.tearDown(); 
	}

}
