/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.binding;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.PhotranVPGBuilder;
import org.eclipse.photran.core.vpg.util.Notification;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTOnlyNode;
import org.eclipse.photran.internal.core.parser.ASTRenameNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;

/**
 * Visits USE statements in an AST, marking a dependency in the VPG,
 * locating the used module, and importing declarations from it.
 * <p>
 * The user may provide module paths (via the Eclipse
 * project properties), which are applied when locating the module.
 * 
 * @author Jeff Overbey
 */
public class ModuleLoader extends BindingCollector
{
    // # R1107
    // <UseStmt> ::=
    // | <LblDef> T_USE <Name>                                        T_EOS
    // | <LblDef> T_USE <Name> T_COMMA <RenameList>                   T_EOS
    // | <LblDef> T_USE <Name> T_COMMA T_ONLY T_COLON ( <OnlyList> )? T_EOS
    //
    // <RenameList> ::=
    // |                        <Rename>
    // | @:<RenameList> T_COMMA <Rename>
    //
    // <OnlyList> ::=
    // |                      <Only>
    // | @:<OnlyList> T_COMMA <Only>
    //
    // # R1108
    // <Rename> ::= T_IDENT T_EQGREATERTHAN <UseName>
    //
    // # R1109
    // <Only> ::=
    // |                         <GenericSpec>
    // | T_IDENT T_EQGREATERTHAN <UseName>
    // |                         <UseName>
	
	private boolean shouldImportModules;
	private IFile fileContainingUseStmt;
	private IProgressMonitor progressMonitor;

	private ASTUseStmtNode useStmt;
	private Token moduleNameToken;
	private String moduleName;

	public ModuleLoader(IFile fileContainingUseStmt, IProgressMonitor progressMonitor)
	{
		this.vpg = (PhotranVPGBuilder)PhotranVPG.getInstance();

		this.shouldImportModules = true;
		this.fileContainingUseStmt = fileContainingUseStmt;
		this.progressMonitor = progressMonitor;
	}
	
    // # R1107
    // <UseStmt> ::=
    // <LblDef> T_USE <Name> T_EOS
    // | <LblDef> T_USE <Name> T_COMMA <RenameList> T_EOS
    // | <LblDef> T_USE <Name> T_COMMA T_ONLY T_COLON ( <OnlyList> )? T_EOS

    @Override public void visitASTUseStmtNode(ASTUseStmtNode node)
    {
        super.traverseChildren(node);
        
        try
        {
        	vpg.markFileAsImportingModule(fileContainingUseStmt, node.getName().getText());

	        if (this.shouldImportModules)
	        	loadModule(node);
        }
        catch (Exception e)
        {
        	throw new Error(e);
        }
    }

	
	private void loadModule(ASTUseStmtNode node) throws Exception
	{
		this.useStmt = node;
		this.moduleNameToken = useStmt.getName();
		this.moduleName = PhotranVPG.canonicalizeIdentifier(moduleNameToken.getText());
		
		progressMonitor.subTask("Loading module " + moduleName + "...");

		if (moduleExistsInFileContainingUseStmt())
		    bindToSymbolsIn(fileContainingUseStmt);
		else
	        findModuleInModulePaths();
    }

	private boolean moduleExistsInFileContainingUseStmt() throws Exception
    {
		return findModuleIn(fileContainingUseStmt) != null;
	}
    
    private ASTModuleNode findModuleIn(IFile file) throws Exception
    {
        ASTModuleNode result = null;
        try
        {
            vpg.acquireTransientAST(file).accept(new ASTVisitor()
            {
                @Override
                public void visitASTModuleNode(ASTModuleNode node)
                {
                    String thisModule = PhotranVPG.canonicalizeIdentifier(node.getModuleStmt().getModuleName().getModuleName().getText());
                    if (thisModule.equals(moduleName))
                    	throw new Notification(node);
                    
                    //traverseChildren(node);
                }
            });
        }
        catch (Notification n)
        {
            result = (ASTModuleNode)n.getResult();
        }
        return result;
    }
    
    private void findModuleInModulePaths() throws Exception
    {
    	List<IFile> files = vpg.findFilesThatExportModule(moduleName);
        if (files.isEmpty())
        {
            vpg.logError("There are no files that export a module named " + moduleName, useStmt.getName().getTokenRef());
            return;
        }

        files = applyModulePaths(files);
        if (files.isEmpty())
        {
            vpg.logError("The module " + moduleName + " could not be found in any of the"
            			+ " folders in the module paths for this project.  However, it was found in a folder not in the"
            			+ " module path.", useStmt.getName().getTokenRef());
            return;
        }
        
		for (IFile file : files)
        	bindToSymbolsIn(file);
    }

	private List<IFile> applyModulePaths(List<IFile> files)
    {
        String[] paths = SearchPathProperties.parseString(SearchPathProperties.getProperty(fileContainingUseStmt.getProject(),
                                                                                           SearchPathProperties.MODULE_PATHS_PROPERTY_NAME));
        if (paths.length == 0) return files; // Do not apply if property not set
        
        List<IFile> result = new LinkedList<IFile>();

        // Check in the directory with the file containing the USE statement first
        if (findModuleIn(PhotranVPG.getFilenameForIResource(fileContainingUseStmt.getParent()), files, result))
        	return result;

        // Then check in the user-specified module paths
        for (String path : paths)
            if (findModuleIn(path, files, result))
            	return result;

        return result; // May be empty
    }

	private boolean findModuleIn(String path, List<IFile> files, List<IFile> result)
	{
		for (IFile file : files)
		{
		    if (PhotranVPG.getFilenameForIResource(file.getParent()).startsWith(path))
		    {
		        result.add(file);
	            if (!result.isEmpty()) return true;
		    }
		}
		return false;
	}

//    private void bindToSymbolsIn(IFile file) throws Exception
//    {
//        ASTModuleNode moduleNode = findModuleIn(file);
//        if (moduleNode == null) return; // Shouldn't happen if VPG is up to date
//        
//        bind(useStmt.getName(), moduleNode.getRepresentativeToken());
//        
//        ScopingNode newScope = useStmt.getUseToken().getEnclosingScope();
//        
//        for (Definition def : moduleNode.getAllPublicDefinitions())
//            if (shouldImportDefinition(def))
//                importDefinition(def, newScope);
//        
//        bindIdentifiersInRenameList(useStmt.getRenameList(), moduleNode);
//        bindIdentifiersInOnlyList(useStmt.getOnlyList(), moduleNode);
//    }

    private void bindToSymbolsIn(IFile file) throws Exception
    {
        PhotranTokenRef moduleToken = vpg.getModuleTokenRef(moduleName);
        if (moduleToken == null) return; // Shouldn't happen if VPG is up to date
        
        bind(useStmt.getName(), moduleToken);
        
        ScopingNode newScope = useStmt.getUseToken().getEnclosingScope();
        
        List<Definition> moduleSymtab = vpg.getModuleSymbolTable(moduleName);
        for (Definition def : moduleSymtab)
            if (shouldImportDefinition(def))
                importDefinition(def, newScope);
        
        bindIdentifiersInRenameList(useStmt.getRenameList(), moduleSymtab);
        bindIdentifiersInOnlyList(useStmt.getOnlyList(), moduleSymtab);
    }

	private boolean shouldImportDefinition(Definition def)
	{
		IASTListNode<ASTRenameNode> renameList = useStmt.getRenameList();
		IASTListNode<ASTOnlyNode> onlyList = useStmt.getOnlyList();
		
		if (renameList == null && onlyList == null)
		{
			return true;
		}
		else if (renameList != null)
		{
			for (int i = 0; i < renameList.size(); i++)
			{
				String entityBeingRenamed = PhotranVPG.canonicalizeIdentifier(renameList.get(i).getName().getText());
				if (def.matches(entityBeingRenamed))
						return false;
			}
			
			return true;
		}
		else // (onlyList != null)
		{
	        for (int i = 0; i < onlyList.size(); i++)
	        {
	        	Token useName = onlyList.get(i).getName();
	        	String entityToImport = useName == null ? null : PhotranVPG.canonicalizeIdentifier(useName.getText());
	        	boolean isRenamed = onlyList.get(i).isRenamed();
	        	
	            if (def.matches(entityToImport) && !isRenamed) return true;
	        }
	        
	        return false;
		}
	}
	
	private void bindIdentifiersInRenameList(IASTListNode<ASTRenameNode> renameList, List<Definition> moduleSymtab) throws Exception
	{
		if (renameList == null) return;
		
		for (int i = 0; i < renameList.size(); i++)
        {
            Token newName = renameList.get(i).getNewName();
            Token oldName = renameList.get(i).getName();
            
            bindPossiblyRenamedIdentifier(newName, oldName, moduleSymtab);
        }
	}

	private void bindIdentifiersInOnlyList(IASTListNode<ASTOnlyNode> onlyList, List<Definition> moduleSymtab) throws Exception
	{
		if (onlyList == null) return;
		
		for (int i = 0; i < onlyList.size(); i++)
        {
            Token newName = onlyList.get(i).getNewName();
            Token oldName = onlyList.get(i).getName();
            
            if (oldName != null) bindPossiblyRenamedIdentifier(newName, oldName, moduleSymtab);
        }
	}

    private void bindPossiblyRenamedIdentifier(Token newName, Token oldName, List<Definition> moduleSymtab) throws Exception
    {
        List<PhotranTokenRef> definitionsInModule = new LinkedList<PhotranTokenRef>();
        String canonicalizedOldName = PhotranVPG.canonicalizeIdentifier(oldName.getText());
        for (Definition def : moduleSymtab)
            if (def.matches(canonicalizedOldName))
                definitionsInModule.add(def.getTokenRef());
        
        for (PhotranTokenRef def : definitionsInModule)
        {
            bindRenamedEntity(newName, def);
            bind(oldName, def);
        }
        
        Type type = definitionsInModule.size() == 1 ? vpg.getDefinitionFor(definitionsInModule.get(0)).getType() : Type.UNKNOWN;
        addDefinition(newName, Definition.Classification.RENAMED_MODULE_ENTITY, type);
    }

//    private void bindPossiblyRenamedIdentifier(Token newName, Token oldName, ASTModuleNode moduleNode) throws Exception
//    {
//        List<PhotranTokenRef> definitionsInModule = moduleNode.manuallyResolve(oldName);
//        
//        for (PhotranTokenRef def : definitionsInModule)
//        {
//            bindRenamedEntity(newName, def);
//            bind(oldName, def);
//        }
//        
//        Type type = definitionsInModule.size() == 1 ? vpg.getDefinitionFor(definitionsInModule.get(0)).getType() : Type.UNKNOWN;
//        addDefinition(newName, Definition.Classification.RENAMED_MODULE_ENTITY, type);
//    }
}
