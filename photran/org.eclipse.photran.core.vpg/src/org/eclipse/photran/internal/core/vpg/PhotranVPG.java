package org.eclipse.photran.internal.core.vpg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.FProjectNature;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Definition.Visibility;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTErrorConstructNode;
import org.eclipse.photran.internal.core.parser.ASTErrorProgramUnitNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTExternalNameListNode;
import org.eclipse.photran.internal.core.parser.ASTExternalStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IProgramUnit;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * Photran's Virtual Program Graph.
 *
 * @author Jeff Overbey
 */
//public class PhotranVPG extends EclipseVPG<IFortranAST, Token, PhotranTokenRef, PhotranVPGDB, PhotranVPGLog>
public class PhotranVPG extends EclipseVPG<IFortranAST, Token, PhotranTokenRef>
{
    private static PhotranVPG instance = null;

    public static PhotranVPG getInstance()
    {
        if (instance == null)
        {
            PhotranVPGComponentFactory locator = new PhotranVPGComponentFactory();
            
            if (/*inTestingMode() ||*/ FortranPreferences.ENABLE_VPG_LOGGING.getValue())
            {
                instance = new PhotranVPG(locator)
                {
                    @Override public void debug(String message, String filename)
                    {
                        System.out.println(message + " - " + lastSegmentOfFilename(filename)); //$NON-NLS-1$
                    }
                };
            }
            else
            {
                instance = new PhotranVPG(locator);
            }
        }
        
        return instance;
    }
    
    public static PhotranVPGWriter getProvider()
    {
        return getInstance().getVPGWriter();
    }

    //protected PhotranVPG(PhotranVPGNodeFactory locator, PhotranVPGDB db)
    protected PhotranVPG(PhotranVPGComponentFactory locator)
    {
        super(locator, Messages.PhotranVPG_PhotranIndexer, 2);
    }

    @Override public void start()
    {
        if (!FortranCorePlugin.inTestingMode()) super.start();
    }
    
    public static String canonicalizeIdentifier(String identifier)
    {
        return identifier.trim().toLowerCase().replaceAll("[ \t\r\n]", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }













    public ArrayList<Definition> findAllExternalSubprogramsNamed(String name)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();
        for (IFile file : findFilesThatExportSubprogram(name))
            result.addAll(findSubprograms(name, file));
        return result;
    }

    private ArrayList<Definition> findSubprograms(String name, IFile file)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();
        String cname = canonicalizeIdentifier(name);

        IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(file);
        if (ast != null)
        {
            ASTExecutableProgramNode node = ast.getRoot();
            for (IProgramUnit pu : node.getProgramUnitList())
            {
                PhotranTokenRef tr = attemptToMatch(cname, pu);
                if (tr != null)
                {
                    Definition d = getDefinitionFor(tr);
                    if (d != null) result.add(d);
                }
            }
        }

        return result;
    }

    private PhotranTokenRef attemptToMatch(String cname, IProgramUnit pu)
    {
        if (pu instanceof ASTSubroutineSubprogramNode)
            return attemptToMatch(cname, ((ASTSubroutineSubprogramNode)pu).getSubroutineStmt());
        else if (pu instanceof ASTFunctionSubprogramNode)
            return attemptToMatch(cname, ((ASTFunctionSubprogramNode)pu).getFunctionStmt());
        else
            return null;
    }

    private PhotranTokenRef attemptToMatch(String cname, ASTSubroutineStmtNode functionStmt)
    {
        return attemptToMatch(cname, functionStmt.getSubroutineName().getSubroutineName());
    }

    private PhotranTokenRef attemptToMatch(String cname, ASTFunctionStmtNode functionStmt)
    {
        return attemptToMatch(cname, functionStmt.getFunctionName().getFunctionName());
    }

    private PhotranTokenRef attemptToMatch(String cname, Token nameToken)
    {
            String thisSub = canonicalizeIdentifier(nameToken.getText());
            if (thisSub.equals(cname))
                return nameToken.getTokenRef();
            else
                return null;
    }
    
    public static ASTNodeWithErrorRecoverySymbols findFirstErrorIn(ASTExecutableProgramNode ast)
    {
        class V extends ASTVisitor
        {
            private ASTNodeWithErrorRecoverySymbols firstError = null;
            
            @Override public void visitASTErrorProgramUnitNode(ASTErrorProgramUnitNode node)
            {
                if (firstError == null)
                    firstError = node;
            }

            @Override public void visitASTErrorConstructNode(ASTErrorConstructNode node)
            {
                if (firstError == null)
                    firstError = node;
            }
        };
        
        V v = new V();
        ast.accept(v);
        return v.firstError;
    }

//    private ArrayList<Definition> mapDefinitions(ArrayList<PhotranTokenRef> tokenRefs)
//    {
//        ArrayList<Definition> result = new ArrayList<Definition>();
//        for (PhotranTokenRef tr : tokenRefs)
//        {
//            Definition def = getDefinitionFor(tr);
//            if (def != null)
//                result.add(def);
//        }
//        return result;
//    }

    public ArrayList<Definition> findAllDeclarationsInInterfacesForExternalSubprogram(String name)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();
        for (IFile file : findFilesThatImportSubprogram(name))
            result.addAll(findInterfaceSubprograms(name, file));
        return result;
    }

    private ArrayList<Definition> findInterfaceSubprograms(String name, IFile file)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();

        IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(file);
        if (ast != null)
            ast.accept(new InterfaceVisitor(result, canonicalizeIdentifier(name)));

        return result;
    }

    private final class InterfaceVisitor extends GenericASTVisitor
    {
        private final ArrayList<Definition> result;
        private final String canonicalizedName;

        public InterfaceVisitor(ArrayList<Definition> result, String canonicalizedName)
        {
            this.result = result;
            this.canonicalizedName = canonicalizedName;
        }

        @Override public void visitASTFunctionStmtNode(ASTFunctionStmtNode node)
        {
            addIfDefinedInInterface(attemptToMatch(canonicalizedName, node));
        }

        @Override public void visitASTSubroutineStmtNode(ASTSubroutineStmtNode node)
        {
            addIfDefinedInInterface(attemptToMatch(canonicalizedName, node));
        }

        private void addIfDefinedInInterface(PhotranTokenRef tr)
        {
            Definition def = tr == null ? null : getDefinitionFor(tr);
            if (def != null && def.isExternalSubprogramReferenceInInterfaceBlock())
                result.add(def);
        }
    }

    public ArrayList<Definition> findAllDeclarationsInExternalStmts(String name)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();
        for (IFile file : findFilesThatImportSubprogram(name))
            result.addAll(findExternalStmts(name, file));
        return result;
    }

    private ArrayList<Definition> findExternalStmts(String name, IFile file)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();

        IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(file);
        if (ast != null)
            ast.accept(new ExternalStmtVisitor(result, canonicalizeIdentifier(name)));

        return result;
    }

    private final class ExternalStmtVisitor extends GenericASTVisitor
    {
        private final ArrayList<Definition> result;
        private final String canonicalizedName;

        public ExternalStmtVisitor(ArrayList<Definition> result, String canonicalizedName)
        {
            this.result = result;
            this.canonicalizedName = canonicalizedName;
        }

        // # R1208
        // <ExternalStmt> ::=
        // <LblDef> T_EXTERNAL <ExternalNameList> T_EOS
        // | <LblDef> T_EXTERNAL T_COLON T_COLON <ExternalNameList> T_EOS
        //
        // <ExternalNameList> ::=
        // <ExternalName>
        // | @:<ExternalNameList> T_COMMA <ExternalName>

        @Override public void visitASTExternalStmtNode(ASTExternalStmtNode node)
        {
            super.traverseChildren(node);

            IASTListNode<ASTExternalNameListNode> list = node.getExternalNameList();
            for (int i = 0; i < list.size(); i++)
                add(attemptToMatch(canonicalizedName, list.get(i).getExternalName()));
        }

        private void add(PhotranTokenRef tr)
        {
            Definition def = tr == null ? null : getDefinitionFor(tr);
            if (def != null) // && def.getClassification().equals(Classification.EXTERNAL))
                result.add(def);
        }
    }

    private List<IFile> getOutgoingIFileDependenciesFrom(String targetFilename)
    {
        List<IFile> files = new LinkedList<IFile>();
        for (String filename : super.getOutgoingDependenciesFrom(targetFilename))
        {
            IFile file = getIFileForFilename(filename);
            if (file != null) files.add(file);
        }
        return files;
    }

    private List<IFile> getIncomingIFileDependenciesTo(String targetFilename)
    {
        List<IFile> files = new LinkedList<IFile>();
        for (String filename : super.getIncomingDependenciesTo(targetFilename))
            files.add(getIFileForFilename(filename));
        return files;
    }

    public List<IFile> findFilesThatExportSubprogram(String subprogramName)
    {
        return getOutgoingIFileDependenciesFrom("subprogram:" + canonicalizeIdentifier(subprogramName)); //$NON-NLS-1$
    }

    public List<IFile> findFilesThatImportSubprogram(String subprogramName)
    {
        return getIncomingIFileDependenciesTo("subprogram:" + canonicalizeIdentifier(subprogramName)); //$NON-NLS-1$
    }

    public List<IFile> findFilesThatExportModule(String moduleName)
    {
        return getOutgoingIFileDependenciesFrom("module:" + canonicalizeIdentifier(moduleName)); //$NON-NLS-1$
    }

    public List<IFile> findFilesThatImportModule(String moduleName)
    {
        return getIncomingIFileDependenciesTo("module:" + canonicalizeIdentifier(moduleName)); //$NON-NLS-1$
    }

    public List<IFile> findFilesThatUseCommonBlock(String commonBlockName)
    {
        // The unnamed common block is stored with the empty name as its name
        if (commonBlockName == null) commonBlockName = ""; //$NON-NLS-1$

        return getIncomingIFileDependenciesTo("common:" + canonicalizeIdentifier(commonBlockName)); //$NON-NLS-1$
    }

    public Iterable<String> listAllModules()
    {
        return listAllDependentFilenamesStartingWith("module:"); //$NON-NLS-1$
    }

    public Iterable<String> listAllSubprograms()
    {
        return listAllDependentFilenamesStartingWith("subprogram:"); //$NON-NLS-1$
    }

    public Iterable<String> listAllCommonBlocks()
    {
        return listAllFilenamesWithDependentsStartingWith("common:"); //$NON-NLS-1$
    }

    private Iterable<String> listAllDependentFilenamesStartingWith(String prefix)
    {
        /*
         * When there is a module "module1" declared in module1.f90, the VPG
         * will contain a dependency
         *
         *     module:module1    -----depends-on----->    module1.f90
         *
         * So we can determine all modules by searching the list of dependent
         * filenames.  Note that this will include every module that is
         * declared, even if it is never used.
         * 
         * This same procedure works for external subprograms as well.
         */

        TreeSet<String> result = new TreeSet<String>();
        for (String name : listAllDependentFilenames())
            if (name.startsWith(prefix))
                result.add(name.substring(prefix.length()));
        for (String name : listAllFilenamesWithDependents())
            if (name.startsWith(prefix))
                result.add(name.substring(prefix.length()));
        return result;
    }

    private Iterable<String> listAllFilenamesWithDependentsStartingWith(String prefix)
    {
        /*
         * When there is a common block "common1" declared in common1.f90, the VPG
         * will contain a dependency
         *
         *     common1.f90    -----depends-on----->    common:common1
         *
         * So we can determine all commons by searching the list of filenames
         * with dependencies.
         */

        TreeSet<String> result = new TreeSet<String>();
        for (String name : listAllFilenamesWithDependents())
            if (name.startsWith(prefix))
                result.add(name.substring(prefix.length()));
        return result;
    }

    public Definition getDefinitionFor(PhotranTokenRef tokenRef)
    {
        return tokenRef.getAnnotation(AnnotationType.DEFINITION_ANNOTATION_TYPE);
    }

    public Type getTypeFor(PhotranTokenRef tokenRef)
    {
        return tokenRef.getAnnotation(AnnotationType.TYPE_ANNOTATION_TYPE);
    }

    public Visibility getVisibilityFor(Definition def, ScopingNode visibilityInScope)
    {
        PhotranTokenRef targetScope = visibilityInScope.getRepresentativeToken();

        for (PhotranTokenRef privateScope : def.getTokenRef().followOutgoing(EdgeType.DEFINITION_IS_PRIVATE_IN_SCOPE_EDGE_TYPE))
            if (privateScope.equals(targetScope))
                return Visibility.PRIVATE;

        return Visibility.PUBLIC;
    }

    public PhotranTokenRef getModuleTokenRef(String moduleName)
    {
        String filename = "module:" + canonicalizeIdentifier(moduleName); //$NON-NLS-1$
        PhotranTokenRef tokenRef = getVPGNode(filename, 0, 0);
        //System.err.println("getModuleTokenRef(" + moduleName + ") returning " + db.getAnnotation(tokenRef, MODULE_TOKENREF_ANNOTATION_TYPE));
        return tokenRef.getAnnotation(AnnotationType.MODULE_TOKENREF_ANNOTATION_TYPE);
    }

    public List<Definition> getModuleSymbolTable(String moduleName)
    {
        return getProvider().getModuleSymbolTable(moduleName);
    }





    @Override
    public String describeEdgeType(int edgeType)
    {
        return getProvider().describeEdgeType(edgeType);
    }

    @Override
    public String describeAnnotationType(int annotationType)
    {
        return getProvider().describeAnnotationType(annotationType);
    }



    
    public boolean doesProjectHaveRefactoringEnabled(IFile file)
    {
        if (FortranCorePlugin.inTestingMode()) return true;

        String vpgEnabledProperty = new SearchPathProperties().getProperty(
            file,
            SearchPathProperties.ENABLE_VPG_PROPERTY_NAME);
        return vpgEnabledProperty != null && vpgEnabledProperty.equals("true"); //$NON-NLS-1$
    }










    @Override public String getSourceCodeFromAST(IFortranAST ast)
    {
        return ast.getRoot().toString();
    }








    @Override
    public boolean shouldProcessFile(IFile file)
    {
        return FortranCorePlugin.hasFortranContentType(file.getName()); 
    }

    @Override
    public boolean shouldProcessProject(IProject project)
    {
        try
        {
            if (!project.isAccessible()) return false;
            if (!project.hasNature(FProjectNature.F_NATURE_ID)) return false;
            return FortranCorePlugin.inTestingMode() || new SearchPathProperties().getProperty(project, SearchPathProperties.ENABLE_VPG_PROPERTY_NAME).equals("true"); //$NON-NLS-1$
        }
        catch (CoreException e)
        {
            throw new Error(e);
        }
    }

    public String describeWhyCannotProcessProject(IProject project)
    {
        try
        {
            if (!project.isAccessible())
                return Messages.bind(Messages.PhotranVPG_ProjectIsNotAccessible, project.getName());
            else if (!project.hasNature(FProjectNature.F_NATURE_ID))
                return Messages.bind(Messages.PhotranVPG_ProjectIsNotAFortranProject, project.getName());
            else if (!new SearchPathProperties().getProperty(project, SearchPathProperties.ENABLE_VPG_PROPERTY_NAME).equals("true")) //$NON-NLS-1$
                return Messages.bind(Messages.PhotranVPG_AnalysisRefactoringNotEnabled, project.getName());
            else
                return null;
        }
        catch (CoreException e)
        {
            throw new Error(e);
        }
    }

    public String describeWhyCannotProcessFile(IFile file)
    {
        if (file.getProject() == null)
            return Messages.bind(Messages.PhotranVPG_FileIsNotInAFortranProject, file.getName());
        else if (!shouldProcessProject(file.getProject()))
            return describeWhyCannotProcessProject(file.getProject());
        else if (!shouldProcessFile(file))
            return Messages.bind(
                Messages.PhotranVPG_NotAFortranSourceFile,
                file.getName(),
                file.getFileExtension());
        else
            return null;
    }

    @Override
    public boolean isVirtualFile(String filename)
    {
        return filename.startsWith("module:") || filename.startsWith("common:") || filename.startsWith("subprogram:"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }





    @Override
    public IFortranAST parse(final String filename)
    {
        return ((PhotranVPGWriter)getVPGWriter()).parse(filename);
    }
}