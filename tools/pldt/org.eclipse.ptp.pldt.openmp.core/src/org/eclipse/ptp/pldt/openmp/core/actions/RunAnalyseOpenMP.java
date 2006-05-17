package org.eclipse.ptp.pldt.openmp.core.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPAnalysisManager;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPError;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPErrorManager;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTNode;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.ui.pv.PvPlugin;
import org.eclipse.ptp.pldt.openmp.ui.pv.views.ProblemMarkerAttrIds;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseBase;
import org.eclipse.ptp.pldt.common.util.AnalysisUtil;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.common.util.ViewActivater;
import org.eclipse.ptp.pldt.openmp.core.OpenMPArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;
import org.eclipse.ptp.pldt.openmp.core.OpenMPScanReturn;
import org.eclipse.ptp.pldt.openmp.core.analysis.OpenMPCASTVisitor;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * 
 * Run analysis to create OpenMP artifact markers. <br>
 * The analysis is done in the doMpiCallAnalysis() method
 * 
 * 
 * IObjectActionDelegate enables popup menu selection IWindowActionDelegate enables toolbar(or menu) selection
 */
public class RunAnalyseOpenMP extends RunAnalyseBase
{
    private static final String OPENMP_DIRECTIVE  = "OpenMP directive";
    
    /**
     * Constructor for the "Run Analysis" action
     */
    public RunAnalyseOpenMP()
    {
        super("OpenMP", new OpenMPArtifactMarkingVisitor(OpenMPPlugin.MARKER_ID), OpenMPPlugin.MARKER_ID);
    }

    /**
     * Returns OpenMP analysis artifacts for file
     * 
     * @param file
     * @param includes OpenMP include paths
     * @return
     */
    public ScanReturn doArtifactAnalysis(final IFile file, final List /* of String */includes)
    {
        OpenMPScanReturn msr = new OpenMPScanReturn();
        final String fileName = file.getName();
        ParserLanguage lang = AnalysisUtil.getLanguageFromFile(file);
        IASTTranslationUnit astTransUnit = null;
        try {
            astTransUnit = CDOM.getInstance().getASTService().getTranslationUnit(file,
                    CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES));
            if (lang == ParserLanguage.C) {
                astTransUnit.accept(new OpenMPCASTVisitor(includes, fileName, msr));
            }
        } catch (IASTServiceProvider.UnsupportedDialectException e) {
        }
        // DPP should put code to recognize #pragmas etc. here
        
        processOpenMPPragmas(msr, astTransUnit, file);
        return msr;
    }
    
    protected void processOpenMPPragmas(OpenMPScanReturn    msr, 
    		                            IASTTranslationUnit astTransUnit,
    		                            IFile               iFile)
    { 
        OpenMPAnalysisManager   omgr = new OpenMPAnalysisManager(astTransUnit, iFile);
        PASTNode [] pList = omgr.getPAST();
        
        for(int i=0; i<pList.length; i++) {
            if (pList[i] instanceof PASTOMPPragma) {
                PASTOMPPragma pop = (PASTOMPPragma)pList[i];
                SourceInfo    si  = getSourceInfo(pop, 0);
                Artifact a = new Artifact(pop.getFilename(), 
                                          pop.getStartingLine(),
                                          pop.getStartLocation(),
                                          pop.getContent(),
                                          OPENMP_DIRECTIVE,
                                          si,
                                          pop);
                msr.addArtifact(a);
            }
        }
        
        msr.addProblems(OpenMPErrorManager.getCurrentErrorManager().getErrors());
    }
    

    /**
     * Get exact source locational info for a function call
     * 
     * @param pastNode
     * @param constructType
     * @return
     */
    private SourceInfo getSourceInfo(PASTNode pastNode, int constructType)
    {
        SourceInfo sourceInfo = null;
        IASTNodeLocation[] locations = pastNode.getNodeLocations();
        if (locations.length == 1) {
            IASTFileLocation astFileLocation = null;
            if (locations[0] instanceof IASTFileLocation) {
                astFileLocation = (IASTFileLocation) locations[0];
                sourceInfo = new SourceInfo();
                sourceInfo.setStartingLine(astFileLocation.getStartingLineNumber());
                sourceInfo.setStart(astFileLocation.getNodeOffset());
                sourceInfo.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
                sourceInfo.setConstructType(constructType);
            }
        }
        return sourceInfo;
    }


    protected List getIncludePath()
    {
        return OpenMPPlugin.getDefault().getIncludeDirs();
    }

    protected void activateArtifactView()
    {
        ViewActivater.activateView(OpenMPPlugin.VIEW_ID);
    }
    
    protected void activateProblemsView()
    {
        ViewActivater.activateView(PvPlugin.VIEW_ID);
    }
    
    /**
     * processResults - override from RunAnalyse base, to process both pragma artifacts and problems
     */
    protected void processResults(ScanReturn results, IResource resource)
    {
        assert(results instanceof OpenMPScanReturn);
        
        OpenMPScanReturn osr = (OpenMPScanReturn)results;
        
        // This is for the openmp pragma view
        List artifacts = osr.getOpenMPList();
        visitor.visitFile(resource, artifacts);
        
        // remove problems
        removeProblemMarkers(resource);
        
        // DPP - put in stuff for problems view
        // Just subclass scanreturn and create markers for problems view here
        List problems = osr.getProblems();
        try {
          for(Iterator i=problems.iterator(); i.hasNext();)
              processProblem((OpenMPError)i.next(), resource);
        }
        catch(CoreException e) {
            System.out.println("RunAnalysisOpenMP.processResults exception: "+e);
            e.printStackTrace();
        }
    }
    
    /**
     * processProblem - put a problem on the omp problems view
     * @param problem       - OpenMPError
     * @param resource      - IResource
     * @throws CoreException
     */
    private void processProblem(OpenMPError problem, IResource resource) throws CoreException
    {
        // build all the attributes
        Map attrs = new HashMap();
        attrs.put(ProblemMarkerAttrIds.DESCRIPTION, problem.getDescription());
        attrs.put(ProblemMarkerAttrIds.RESOURCE, problem.getFilename());
        attrs.put(ProblemMarkerAttrIds.INFOLDER, problem.getPath());
        attrs.put(ProblemMarkerAttrIds.LOCATION, new Integer(problem.getLineno()));
        // used to reference problem if need
        attrs.put(ProblemMarkerAttrIds.PROBLEMOBJECT, problem);
        
        // create the marker all at once, so get ONLY a single resourceChange event.
        MarkerUtilities.createMarker(resource, attrs, ProblemMarkerAttrIds.MARKER_ERROR_ID); 
        
    }
    

    /**
     * Remove the markers currently set on a resource.
     * 
     * @param resource - IResource
     */
    private void removeProblemMarkers(IResource resource)
    {
        try {
            resource.deleteMarkers(ProblemMarkerAttrIds.MARKER_ERROR_ID, false, IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
            System.out.println(e);
            System.out.println(e.toString());
            System.out.println("Problem deleting markers on OMP Problems: " + resource.getProjectRelativePath());
        }
    }


}