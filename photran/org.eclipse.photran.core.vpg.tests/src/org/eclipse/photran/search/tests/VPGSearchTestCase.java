package org.eclipse.photran.search.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Scanner;

import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.photran.internal.ui.search.ReferenceSearchResult;
import org.eclipse.photran.internal.ui.search.VPGSearchMatch;
import org.eclipse.photran.internal.ui.search.VPGSearchQuery;
import org.eclipse.photran.refactoring.tests.Activator;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.Match;

/**
 * A single test case for the Prototype Fortran Search
 * Parameters include filenames, scope, and the string to search for
 * 
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 */
public class VPGSearchTestCase extends BaseTestFramework
{
    private static final String DIR = "vpg-search-test-code";

    private static NullProgressMonitor pm = new NullProgressMonitor();
    
    protected String searchString;
    protected int searchFlags;
    protected ArrayList<VPGSearchMatch> matches;
    protected ArrayList<Match> initMatches;
    protected boolean isRegex;
    protected ArrayList<IResource> scope;

    public VPGSearchTestCase() {;}  // when JUnit invokes a subclass outside a test suite
    
    
    public VPGSearchTestCase(String searchString, int searchFlags, 
        ArrayList<Match> matches, boolean isRegex)
    {
        this.setName("test");
        this.searchString = searchString;
        this.searchFlags = searchFlags;
        this.isRegex = isRegex;
        this.matches = new ArrayList<VPGSearchMatch>(matches.size());
        scope = new ArrayList<IResource>();
        initMatches = matches;
        
        
    }
    
    
    private IFile getFile(String filename) throws Exception {
        Scanner sc = new Scanner(Activator.getDefault().getBundle().getResource(DIR + "/" + filename).openStream());
        sc.useDelimiter("\\A");
        String contents = sc.next();
        sc.close();
        return super.importFile(filename, contents);
    }
    
    @Override
    protected void setUp() throws Exception {
        if (searchString == null) return; // when JUnit invokes this outside a test suite
        super.setUp();
        convertMatches();
        createProject();
    }
   
    private void convertMatches() throws Exception { 
        for (Match m : initMatches)
        {
            IFile file = getFile((String)(m.getElement()));
            matches.add(new VPGSearchMatch(file, m.getOffset(), m.getLength()));    
           
            
        }   
    }
    
    private void createProject(){
        try{
            scope.add(getFile("foo.f90"));
            scope.add(getFile("implicitTest.f90")); 
        } catch (Exception e){
            System.out.println("Error in createProject(): "+e);
        }
    }
    
    private ReferenceSearchResult runQuery(VPGSearchQuery job){
        
        final ISearchResult result[]= new ISearchResult[1];
        
        IQueryListener listener= new IQueryListener() {
            public void queryAdded(ISearchQuery query) {}
            public void queryFinished(ISearchQuery query) {
                result[0]= query.getSearchResult();
            }
            public void queryRemoved(ISearchQuery query) {}
            public void queryStarting(ISearchQuery query) {}
        };
        
        NewSearchUI.addQueryListener(listener);
        NewSearchUI.runQueryInForeground(new IRunnableContext() {
            public void run(boolean fork, boolean cancelable,
                    IRunnableWithProgress runnable)
                    throws InvocationTargetException, InterruptedException {
                runnable.run(pm);
            }
        }, job);
        
        assertTrue(result[0] instanceof ReferenceSearchResult);
        return (ReferenceSearchResult)result[0];
    }

    public void test() throws Exception
    {
        if (searchString == null) return; // when JUnit invokes this outside a test suite
        
        VPGSearchQuery job = new VPGSearchQuery(scope, "Scope description", searchString, 
            searchFlags, isRegex);
        
        ReferenceSearchResult res = runQuery(job);
        int count = 0;
        for(Object obj : res.getElements()) {
            for (Match m : res.getMatches(obj)) {
                if (matches.contains((VPGSearchMatch)m))
                {
                    count++;
                }
                
            }
        }
        assertEquals(matches.size(),count);
    }
}
