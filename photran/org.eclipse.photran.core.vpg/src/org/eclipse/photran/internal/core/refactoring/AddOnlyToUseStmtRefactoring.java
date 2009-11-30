/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTSeparatedListNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;


/**
 *
 * @author Kurt Hendle
 */
public class AddOnlyToUseStmtRefactoring extends SingleFileFortranRefactoring
{
    private String moduleName = null;
    private IProject projectInEditor = null;
    private int numEntitiesInList = 0;
    private ASTUseStmtNode useNode = null;
    private List<IFile> filesContainingModule = null;

    //private List<Definition> programEntities = new ArrayList<Definition>();
    private ArrayList<String> entitiesInProgram = new ArrayList<String>();

    private List<Definition> moduleEntities = new ArrayList<Definition>();
    private ArrayList<String> entitiesInModule = new ArrayList<String>();

    private List<Definition> existingOnlyList = new ArrayList<Definition>();
    private List<Definition> defsToAdd = new ArrayList<Definition>();
    private HashMap<Integer, String> entitiesToAdd = new HashMap<Integer, String>();

    private Set<PhotranTokenRef> allReferences = null;

    public AddOnlyToUseStmtRefactoring() {
    }

    public AddOnlyToUseStmtRefactoring(IFile file, ITextSelection selection)
    {
        initialize(file, selection);
    }

    public ArrayList<String> getModuleEntityList()
    {
        return entitiesInModule;
    }

    public void addToOnlyList(String name)
    {
        if(!entitiesToAdd.containsValue(name))
        {
            entitiesToAdd.put(numEntitiesInList, PhotranVPG.canonicalizeIdentifier(name));

            for(int i=0; i<moduleEntities.size(); i++)  //construct list of definitions
            {
                if(entitiesToAdd.get(numEntitiesInList).equals(moduleEntities.get(i).getCanonicalizedName()))
                    defsToAdd.add(moduleEntities.get(i));
            }

            numEntitiesInList++;
        }
    }

    public void removeFromOnlyList(String name)
    {
        for(int i=0; i<entitiesToAdd.size(); i++)
        {
            if(name.equalsIgnoreCase(entitiesToAdd.get(i)))
            {
                for(int j=0; j<moduleEntities.size(); j++)  //remove def from list
                {
                    if(entitiesToAdd.get(i).equalsIgnoreCase(moduleEntities.get(j).getCanonicalizedName()))
                    {
                        defsToAdd.remove(moduleEntities.get(j));
                        existingOnlyList.remove(moduleEntities.get(j));
                    }
                }

                entitiesToAdd.remove(i);
                numEntitiesInList--;
                return;
            }
        }
    }

    public HashMap<Integer, String> getNewOnlyList()
    {
        return entitiesToAdd;
    }

    public int getNumEntitiesInModule()
    {
        return moduleEntities.size();
    }

    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCheckInitialConditions(org.eclipse.ltk.core.refactoring.RefactoringStatus, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        moduleName = this.selectedRegionInEditor.getText();
        if(moduleName == null || moduleName.equals(""))
            fail("No module name selected.");

        findUseStmtNode();
        checkIfModuleExistsInProject();
        getModuleDeclaredEntities();
        getProgramDeclaredEntities();
        readExistingOnlyList();
    }

    private void findUseStmtNode() throws PreconditionFailure
    {
      //get the use statement node in case we need to add to the only list
        Token token = findEnclosingToken();
        if(token == null)
            fail("Please select the name of the module in the USE statement.");

        useNode = token.findNearestAncestor(ASTUseStmtNode.class);
        if(useNode == null)
            fail("Please select the name of the module in the USE statement.");
    }

    private void checkIfModuleExistsInProject() throws PreconditionFailure
    {
      //Check to see if the module exists in the project
        projectInEditor = this.fileInEditor.getProject();  //current project
        filesContainingModule = vpg.findFilesThatExportModule(moduleName);

        if(filesContainingModule.isEmpty() || filesContainingModule == null)
            fail("No files in this project contain the module - " + moduleName);
        else if(filesContainingModule.size() > 1)
            filterFileList();

       //check again after the filtering happens
        if(filesContainingModule.isEmpty() || filesContainingModule == null)
            fail("No files in this project contain the module - " + moduleName);

        if(filesContainingModule.size() > 1)
            fail("Multiple definitions of module " + moduleName + " exist in project.");
    }

    //same method used in CommonVarNamesRefactoring.java
    private void filterFileList() throws PreconditionFailure
    {
        if(projectInEditor == null) fail("Project does not exist!");

        //filter out files not in the project
        int i = 0;
        while(i < filesContainingModule.size())
        {
            if(filesContainingModule.get(i) == null
                || filesContainingModule.get(i).getProject() != projectInEditor)
                filesContainingModule.remove(i);   //shifts all elements left, don't increment i
            else
                i++;
        }
    }

    //modified from RenameRefactoring.java
    private Token findEnclosingToken() throws PreconditionFailure
    {
        Token selectedToken = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (selectedToken == null)
            fail("Please select a module name.");
        return selectedToken;
    }

    private void getProgramDeclaredEntities() throws PreconditionFailure
    {
        IFortranAST ast = vpg.acquirePermanentAST(this.fileInEditor);
        if(ast == null) return;

        DeclarationVisitor visitor = new DeclarationVisitor();
        ast.accept(visitor);
        //AST will be released later
    }

    private void getModuleDeclaredEntities() throws PreconditionFailure
    {
        //get module declaration and check if it has declared entities
        PhotranTokenRef moduleTokenRef = vpg.getModuleTokenRef(moduleName);
        if(moduleTokenRef == null)
            fail("No module with name " + moduleName + "found.");

        Token moduleToken = moduleTokenRef.findTokenOrReturnNull();
        if(moduleToken == null)
            fail("Module token could not be found.");

        ASTModuleNode moduleNode = moduleToken.findNearestAncestor(ASTModuleNode.class);
        if(moduleNode == null)
            fail("Module Node could not be found.");

        moduleEntities = moduleNode.getAllPublicDefinitions();
        if(moduleEntities.isEmpty())
            fail("Module contains no declared entities. No ONLY statement necessary.");
        else
        {
            for(int i=0; i<moduleEntities.size(); i++)
                entitiesInModule.add(moduleEntities.get(i).getCanonicalizedName());
        }
    }

    @SuppressWarnings("unchecked")
    private void readExistingOnlyList()
    {
        ASTSeparatedListNode existingOnlys = (ASTSeparatedListNode)useNode.getOnlyList();
        if(existingOnlys != null)
        {
            for(int i=0; i<existingOnlys.size(); i++)
            {
                entitiesToAdd.put(i,
                    PhotranVPG.canonicalizeIdentifier(existingOnlys.get(i).toString().trim()));
            }

            numEntitiesInList = entitiesToAdd.size();

            for(int i=0; i<moduleEntities.size(); i++)  //construct list of definitions
            {
                if(entitiesToAdd.containsValue(moduleEntities.get(i).getCanonicalizedName()))
                    existingOnlyList.add(moduleEntities.get(i));
            }
        }

        //FIXME add functionality to search file for existing uses of module vars
        //and automatically make them be added to the list
        IFortranAST ast = vpg.acquirePermanentAST(this.fileInEditor);
        if(ast == null) return;

        TokenVisitor visitor = new TokenVisitor();
        ast.accept(visitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCreateChange(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        //nothing to do here
    }

    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCheckFinalConditions(org.eclipse.ltk.core.refactoring.RefactoringStatus, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        pm.beginTask("Analyzing", IProgressMonitor.UNKNOWN);

        if(useNode == null)
            fail("No module name in a USE statement is selected.");

        pm.subTask("Parsing " + fileInEditor.getName());
        IFortranAST ast = vpg.acquirePermanentAST(fileInEditor);
        if(ast == null) return;

        pm.subTask("Checking for conflicts after addition");
        checkConflictingBindings(ast, pm, status);  //find conflicts

        pm.subTask("Inserting USE statement");
        createAndInsertUseStmt(ast);

        pm.subTask("Creating change object");
        addChangeFromModifiedAST(fileInEditor, pm);
        vpg.releaseAST(fileInEditor);

        pm.done();
    }


    /*
     * This method assumes that any existing only list is OK. Only checks for conflicting
     * bindings with NEW additions to only list.
     */
    private void checkConflictingBindings(IFortranAST ast, IProgressMonitor pm, RefactoringStatus status)
    {
        pm.subTask("Finding references");
        allReferences = findModuleEntityRefs(ast);
        //removeOriginalModuleRefs(); //possibly not needed - working without

        for(Definition def : defsToAdd)
        {
            checkForConflictingBindings(pm,
                new ConflictingBindingErrorHandler(status),
                def,
                allReferences,
                def.getCanonicalizedName());
        }
    }

  //Similar to ExtractProcedureRefactoring#localVariablesUsedIn
    private Set<PhotranTokenRef> findModuleEntityRefs(IFortranAST ast)
    {
        final Set<PhotranTokenRef> result = new HashSet<PhotranTokenRef>();
        final Collection<String> defNames = entitiesToAdd.values();
        ast.accept(new GenericASTVisitor()
        {
            @Override public void visitToken(Token token)
            {
                if (token.getTerminal() == Terminal.T_IDENT)
                {
                    for (Definition def : token.resolveBinding())
                    {
                        if (defNames.contains(def.getCanonicalizedName()))
                            result.addAll(def.findAllReferences(true));
                    }
                }
            }
        });

        return result;
    }

    //remove module refs outside project and in the original module definition
    // NOTE: not used for now, filtering conflicts working for now.
    @SuppressWarnings("unused")
    private void removeOriginalModuleRefs()
    {
        if(allReferences != null && allReferences.size() > 0)
        {
            HashSet<PhotranTokenRef> referencesToRemove = new HashSet<PhotranTokenRef>();

            for(PhotranTokenRef ref : allReferences)
            {
                IFile file = ref.getFile();
                IProject project = file.getProject();
                if(!projectInEditor.equals(project))   //ref is in a file not in the project
                    referencesToRemove.add(ref);
                //else if(filesContainingModule.contains(file))  //ref is the module file itself
                //    referencesToRemove.add(ref);
            }

            allReferences.removeAll(referencesToRemove);
        }
    }

    @SuppressWarnings("unchecked")
    private void createAndInsertUseStmt(IFortranAST ast)
    {
      //create the new only selection
        String newOnlyAdditions = " ";
        Collection<String> varNames = new TreeSet<String>(entitiesToAdd.values()); // JO -- Sort names
        Iterator iter = varNames.iterator();
        int counter = 0;

        while(iter.hasNext())
        {
            newOnlyAdditions += iter.next();
            if(counter < varNames.size()-1)
                newOnlyAdditions += ", ";
            counter++;
        }

        //construct the new USE node and replace the old one in the ast
        ASTUseStmtNode newStmtNode;
        if(entitiesToAdd.size() > 0)// && entitiesToAdd.size() < moduleEntities.size())
            newStmtNode = (ASTUseStmtNode)parseLiteralStatement("use " +
                useNode.getName().getText()+", only:" + newOnlyAdditions
                + System.getProperty("line.separator"));
        else
            newStmtNode = (ASTUseStmtNode)parseLiteralStatement("use " +
                useNode.getName().getText() + System.getProperty("line.separator"));

        ASTListNode body = (ASTListNode)useNode.getParent();
        body.replaceChild(useNode, newStmtNode);
        Reindenter.reindent(newStmtNode, ast);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ltk.core.refactoring.Refactoring#getName()
     */
    @Override
    public String getName()
    {
        return "Add ONLY Clause to USE Statement";
    }


    private final class DeclarationVisitor extends GenericASTVisitor
    {
        @Override public void visitASTEntityDeclNode(ASTEntityDeclNode node)
        {
            String name = node.getObjectName().getObjectName().getText();
            if(!entitiesInProgram.contains(name))
            {
                entitiesInProgram.add(name);
            }
        }
    }

    private final class TokenVisitor extends GenericASTVisitor
    {
        @Override public void visitToken(Token node)
        {
            String name = node.getText();
            if(entitiesInModule.contains(name))
                addToOnlyList(name);
        }
    }

    //borrowed (slightly modified) from RenameRefactoring.java
    private final class ConflictingBindingErrorHandler implements IConflictingBindingCallback
    {
        private final RefactoringStatus status;

        private ConflictingBindingErrorHandler(RefactoringStatus status) { this.status = status; }

        public void addConflictError(List<Conflict> conflictingDef)
        {
            for(Conflict conflict : conflictingDef)
            {
                //remove conflicts with the module itself
                IFile file = conflict.tokenRef.getFile();
                if(!filesContainingModule.contains(file) && file.getProject().equals(projectInEditor))
                {
                    String msg = "The name \"" + conflict.name + "\" conflicts with " + vpg.getDefinitionFor(conflict.tokenRef);
                    RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
                    status.addError(msg, context);
                }
            }
        }

        public void addConflictWarning(List<Conflict> conflictingDef)
        {
            for(Conflict conflict : conflictingDef)
            {
                //remove conflicts with the module itself
                IFile file = conflict.tokenRef.getFile();
                if(!filesContainingModule.contains(file) && file.getProject().equals(projectInEditor))
                {
                    String msg = "The name \"" + conflict.name + "\" might conflict with the name of an invoked subprogram";
                    RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
                    status.addWarning(msg, context);
                }
            }
        }

        public void addReferenceWillChangeError(String newName, Token reference)
        {
            //add error for names being added conflicting with declared names in program
            if(entitiesInProgram.contains(newName))
            {
                // The entity with the new name will shadow the definition to which this binding resolves
                status.addError("Adding \"" + newName + "\" to ONLY list"
                        + " would change the meaning of \"" + reference.getText() + "\" on line " + reference.getLine()
                        + " in " + reference.getTokenRef().getFilename(),
                        createContext(reference)); // Highlight problematic reference
            }
        }
    }
}
