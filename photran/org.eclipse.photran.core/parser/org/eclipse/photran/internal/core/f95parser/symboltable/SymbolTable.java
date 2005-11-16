package org.eclipse.photran.internal.core.f95parser.symboltable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.photran.internal.core.f95parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;

/**
 * A Fortran 95 symbol table
 * 
 * Build a symbol table from a translation unit's parse tree by calling the factory method
 * <code>createSymbolTableFor</code>
 * 
 * Each translation unit (file) has a main symbol table. Each entry in the symbol table has the
 * potential to contain a child symbol table. So, for example, the main table may contain a single
 * entry for a module, and the module entry's child table will contain all of the functions,
 * subroutines, types, etc. declared in that module. And those may also have child symbol tables...
 * 
 * @author joverbey
 */
public class SymbolTable
{
    protected SymbolTable parentTable;

    protected SymbolTableEntry parentEntry;

    SymbolTable(SymbolTable parentTable, SymbolTableEntry parentEntry)
    {
        this.parentTable = parentTable;
        this.parentEntry = parentEntry;

        if (parentTable == null)
            implicitSpec = new ImplicitSpec(); // Default implicit rule
        else
            implicitSpec = parentTable.getImplicitSpec();
    }

    public SymbolTable getParentTable()
    {
        return parentTable;
    }

    public SymbolTableEntry getParentEntry()
    {
        return parentEntry;
    }

    /**
     * The IMPLICIT specification for this symbol table. Set to either a valid
     * <code>ImplicitSpec</code>, or <code>null</code> to indicate IMPLICIT NONE.
     */
    protected ImplicitSpec implicitSpec;

    /**
     * @return the IMPLICIT specification for this symbol table: either a valid
     *         <code>ImplicitSpec</code>, or <code>null</code> to indicate IMPLICIT NONE.
     */
    public ImplicitSpec getImplicitSpec()
    {
        return implicitSpec;
    }
    
    public boolean isImplicitNone()
    {
        return implicitSpec == null;
    }

    /**
     * Sets the IMPLICIT specification for this symbol table: either a valid
     * <code>ImplicitSpec</code>, or <code>null</code> to indicate IMPLICIT NONE.
     */
    public void setImplicitSpec(ImplicitSpec implicitSpec)
    {
        this.implicitSpec = implicitSpec;
    }

    /**
     * Sets the IMPLICIT specification for this symbol table by interpreting the given
     * T_xImplicitSpecList node from the parse tree. Set to <code>null</code> to indicate IMPLICIT
     * NONE.
     */
    public void setImplicitSpec(ParseTreeNode txImplicitSpecList)
    {
        if (txImplicitSpecList == null)
            this.implicitSpec = null;
        else
            // FIXME-Jeff: Parse implicit spec
            this.implicitSpec = new ImplicitSpec();
    }

    /**
     * Information about what modules are used in this scope (i.e., what USE statements are
     * contained in it)
     */
    protected Set/* <ModuleUse> */moduleUseInfo = new HashSet();

    /**
     * Adds information about what modules are used in this scope (i.e., what USE statements are
     * contained in it)
     * 
     * @param moduleUse
     */
    public void addModuleUse(ModuleUse moduleUse)
    {
        moduleUseInfo.add(moduleUse);
    }

    /**
     * @return information about what modules are used in this scope (i.e., what USE statements are
     *         contained in it)
     */
    public Set/* <ModuleUse> */getModuleUseInfo()
    {
        return moduleUseInfo;
    }

    /**
     * The entries in this symbol table (not including symbols imported from other modules)
     */
    protected Set/* <SymbolTableEntry> */entries = new HashSet();

    public void addEntry(SymbolTableEntry entry)
    {
        entries.add(entry);
    }

    /**
     * @param entry
     * @return true iff this specific symbol table contains an entry with the given name. Does not
     *         check parent tables.
     * 
     * Assumes that the <code>ModuleLoader</code> is ready for use (at a minimum,
     * <code>setInitializationRunnable</code> has been set)
     */
    boolean immediatelyContainsOrImportsEntryFor(String name)
    {
        String targetName = name.toLowerCase();
        for (Iterator it = entries.iterator(); it.hasNext();)
        {
            SymbolTableEntry thisEntry = (SymbolTableEntry)it.next();
            if (thisEntry.getIdentifier().getText().toLowerCase().equals(targetName)) return true;
        }

        // Not in here; is it an imported symbol?
        return importsEntry(name);
    }

    /**
     * @param entry
     * @return true iff this specific symbol table imports an entry with the given name from another
     *         module <i>from a USE statement in this specific symbol table.</i> Does not check
     *         parent tables.
     * 
     * Assumes that the <code>ModuleLoader</code> is ready for use (at a minimum,
     * <code>setInitializationRunnable</code> has been set)
     */
    private boolean importsEntry(String name)
    {
        // FIXME-Jeff: Implement this
        return false;
    }

    /**
     * @param entry
     * @return true iff this symbol table or one of its ancestors contains an entry with the given
     *         name.
     * 
     * Assumes that the <code>ModuleLoader</code> is ready for use (at a minimum,
     * <code>setInitializationRunnable</code> has been set)
     */
    public boolean hierarchyContainsOrImportsEntryFor(String name)
    {
        // FIXME-Jeff: If a main program contains a variable i, its contained subprograms see it as being in their scope! 
        
        if (immediatelyContainsOrImportsEntryFor(name))
            return true;
        else if (parentTable != null)
            return parentTable.immediatelyContainsOrImportsEntryFor(name);
        else
            return false;
    }

    /**
     * @param entry
     * @return the entry with the given name, or <code>null</code> if not found. Does not check
     *         parent tables.
     * 
     * Assumes that the <code>ModuleLoader</code> is ready for use (at a minimum,
     * <code>setInitializationRunnable</code> has been set)
     */
    SymbolTableEntry getImmediateEntryFor(String name)
    {
        String targetName = name.toLowerCase();
        for (Iterator it = entries.iterator(); it.hasNext();)
        {
            SymbolTableEntry thisEntry = (SymbolTableEntry)it.next();
            if (thisEntry.getIdentifier().getText().toLowerCase().equals(targetName))
                return thisEntry;
        }
        return null;
        // FIXME-Jeff: Check USE statements
    }

    /**
     * @param entry
     * @return the entry with the given name. If there is not one in this symbol table, its
     *         ancestors are checked. If no entry can be found, returns <code>null</code>.
     * 
     * Assumes that the <code>ModuleLoader</code> is ready for use (at a minimum,
     * <code>setInitializationRunnable</code> has been set)
     */
    public SymbolTableEntry getEntryInHierarchyFor(String name)
    {
        SymbolTableEntry symTblEntry = null;

        if ((symTblEntry = getImmediateEntryFor(name)) != null)
            return symTblEntry;
        else if (parentTable != null)
            return parentTable.getEntryInHierarchyFor(name);
        else
            return null;
    }

    /**
     * @param entry
     * @return the entry corresponding to the given node in the parse tree, or <code>null</code>
     *         if there is no entry with that node designated as the root. Does not check parent
     *         tables, since that wouldn't make sense.
     */
    public SymbolTableEntry getEntryCorrespondingTo(ParseTreeNode node)
    {
        for (Iterator it = entries.iterator(); it.hasNext();)
        {
            SymbolTableEntry thisEntry = (SymbolTableEntry)it.next();
            if (thisEntry.getCorrespondingParseTreeNode() == node) return thisEntry;
        }
        return null;
    }

    // /**
    // * Copies all of the entries from another symbol table into this one,
    // * labeling them as having been imported from <code>moduleName</code>.
    // * (<code>moduleName</code>) serves no purpose other than to label
    // * the import source.)
    // * @param otherTable
    // * @param moduleName
    // */
    // public void importEntriesFrom(SymbolTable otherTable, String moduleName)
    // {
    // for (Iterator it = otherTable.entries.iterator(); it.hasNext();)
    // {
    // SymbolTableEntry thisEntry = (SymbolTableEntry)it.next();
    // SymbolTableEntry clonedEntry = (SymbolTableEntry)thisEntry.clone();
    // clonedEntry.correspondingParseTreeNode = null;
    // clonedEntry.references = new HashSet();
    // clonedEntry.importedFrom = moduleName;
    // addEntry(clonedEntry);
    // }
    // }

    /**
     * @return true iff there are no entries in this symbol table
     */
    public boolean isEmpty()
    {
        return entries.isEmpty();
    }

    /**
     * Traverse the symbol tables in preorder via the given <code>SymbolTableVisitor</code>.
     * I.e., the first entry is visited, followed immediately by its child table, then the second
     * entry, then its child table, etc.
     * 
     * @param visitor
     */
    public void visitUsing(SymbolTableVisitor visitor)
    {
        visitor.preparingToVisitTable(this);
        for (Iterator it = entries.iterator(); it.hasNext();)
        {
            SymbolTableEntry entry = (SymbolTableEntry)it.next();
            entry.visitUsing(visitor);
        }
        visitor.doneVisitingTable(this);
    }

    /**
     * Traverse the symbol tables in preorder via the given <code>GenericSymbolTableVisitor</code>.
     * I.e., the first entry is visited, followed immediately by its child table, then the second
     * entry, then its child table, etc.
     * 
     * @param visitor
     */
    public void visitUsing(GenericSymbolTableVisitor visitor)
    {
        visitor.preparingToVisitTable(this);
        for (Iterator it = entries.iterator(); it.hasNext();)
        {
            SymbolTableEntry entry = (SymbolTableEntry)it.next();
            visitor.visit(entry);
            entry.getChildTable().visitUsing(visitor);
        }
        visitor.doneVisitingTable(this);
    }

    /**
     * Return a friendly listing of the contents of this symbol table.
     */
    public String toString()
    {
        return toString(0);
    }

    /**
     * Describe the contents of this symbol table.
     * 
     * @param indent Number of spaces to indent each line. (This is useful when displaying nested
     *            symbol tables.)
     * @return <code>String</code>
     */
    public String toString(int indent)
    {
        StringBuffer sb = new StringBuffer();

        // Write out implicit spec if top level table or if it differs from its
        // parent
        if (parentTable == null || implicitSpec != parentTable.getImplicitSpec())
        {
            for (int i = 0; i < indent; i++)
                sb.append(' ');
            if (implicitSpec == null)
                sb.append("* Implicit none\n");
            else
                sb.append("* Implicit enabled\n");
        }

        // Write out module import info
        for (Iterator it = moduleUseInfo.iterator(); it.hasNext();)
        {
            for (int i = 0; i < indent; i++)
                sb.append(' ');
            sb.append("* Use ");

            ModuleUse useInfo = (ModuleUse)it.next();
            sb.append(useInfo.toString());

            sb.append("\n");
        }

        // Write out entries
        for (Iterator it = entries.iterator(); it.hasNext();)
        {
            SymbolTableEntry entry = (SymbolTableEntry)it.next();
            sb.append(entry.toString(indent));
        }
        return sb.toString();
    }

    // ----- FACTORY METHOD ----------------------------------------------

    /**
     * Create a <code>SymbolTable</code> corresponding to the given parse tree.
     * <code>parseTree</code> is expected to be the root node from the parse of a translation unit
     * (file) -- it should not be a subtree of this.
     */
    public static SymbolTable createSymbolTableFor(ParseTreeNode parseTree) throws Exception
    {
        SymbolTable tbl = (new DeclarationCollector(parseTree)).getSymbolTable();
        Intrinsics.fill(tbl);
        return (new ReferenceCollector(tbl, parseTree)).getSymbolTable();
    }

    // ----- IMPLICITSPEC CLASS ------------------------------------------

    /**
     * An IMPLICIT specification.
     */
    public final static class ImplicitSpec
    {
        private SymbolTableType[] typeMap = {SymbolTableType.REAL,    // A
                                             SymbolTableType.REAL,    // B
                                             SymbolTableType.REAL,    // C
                                             SymbolTableType.REAL,    // D
                                             SymbolTableType.REAL,    // E
                                             SymbolTableType.REAL,    // F
                                             SymbolTableType.REAL,    // G
                                             SymbolTableType.REAL,    // H
                                             SymbolTableType.INTEGER, // I
                                             SymbolTableType.INTEGER, // J
                                             SymbolTableType.INTEGER, // K
                                             SymbolTableType.INTEGER, // L
                                             SymbolTableType.INTEGER, // M
                                             SymbolTableType.INTEGER, // N
                                             SymbolTableType.REAL,    // O
                                             SymbolTableType.REAL,    // P
                                             SymbolTableType.REAL,    // Q
                                             SymbolTableType.REAL,    // R
                                             SymbolTableType.REAL,    // S
                                             SymbolTableType.REAL,    // T
                                             SymbolTableType.REAL,    // U
                                             SymbolTableType.REAL,    // V
                                             SymbolTableType.REAL,    // W
                                             SymbolTableType.REAL,    // X
                                             SymbolTableType.REAL,    // Y
                                             SymbolTableType.REAL     // Z
        };

        /**
         * Indicates that non-declared identifiers beginning with <code>letter</code> should
         * implicitly have type <code>type</code>.
         * 
         * @param letter
         * @param type
         */
        public void setType(char letter, SymbolTableType type)
        {
            if (!Character.isLetter(letter)) throw new Error("Non-letter passed to setType");
            letter = Character.toUpperCase(letter);
            typeMap[letter - 'A'] = type;
        }

        /**
         * @param letter
         * @return the type that non-declared identifiers beginning with <code>letter</code>
         *         should have
         */
        public SymbolTableType getType(char letter)
        {
            if (!Character.isLetter(letter))
                return SymbolTableType.REAL;
            else
            {
                letter = Character.toUpperCase(letter);
                return typeMap[letter - 'A'];
            }
        }
    }

    // ----- MODULEUSE CLASS ---------------------------------------------

    /**
     * Information gathered from USE statements in this module.
     */
    public abstract static class ModuleUse
    {
        public ModuleUse(Token moduleName)
        {
            this.moduleName = moduleName;
        }

        protected Token moduleName;

        public Token getModuleName()
        {
            return moduleName;
        }

        protected Map/* <Token, Token> */importAndRename = new HashMap();

        public void importAndRename(Token from, Token to)
        {
            importAndRename.put(from, to);
        }

        public abstract String toString();

        public String describeRenames()
        {
            StringBuffer sb = new StringBuffer();
            for (Iterator it = importAndRename.keySet().iterator(); it.hasNext();)
            {
                Token from = (Token)it.next();
                Token to = (Token)importAndRename.get(from);

                sb.append(", ");
                sb.append(from.getText());
                sb.append("=>");
                sb.append(to.getText());
            }
            return sb.toString();
        }
    }

    /**
     * Corresponds to a USE statement without an ONLY clause. Everything in the imported module will
     * be used; if something should be renamed, that should be specified via
     * <code>importAndRename</code>.
     */
    public final static class ModuleUseAll extends ModuleUse
    {
        public ModuleUseAll(Token moduleName)
        {
            super(moduleName);
        }

        public String toString()
        {
            return moduleName.getText() + describeRenames();
        }
    }

    /**
     * Corresponds to a USE statement with an ONLY clause. Specify what to use via
     * <code>importWithoutRenaming</code>. If something should be renamed, that should be
     * specified via <code>importAndRename</code>.
     */
    public final static class ModuleUseOnly extends ModuleUse
    {
        public ModuleUseOnly(Token moduleName)
        {
            super(moduleName);
        }

        private Set/* <Token or xGenericSpec node> */importWithoutRenaming = new HashSet();

        public void importWithoutRenaming(Token identifier)
        {
            importWithoutRenaming.add(identifier);
        }

        public void importWithoutRenaming(ParseTreeNode genericSpec)
        {
            if (genericSpec.getRootNonterminal() != Nonterminal.XGENERICSPEC)
                throw new SymbolTableError(
                    "Tried to import non-generic-spec parse tree node into ModuleUseOnly!");

            importWithoutRenaming.add(genericSpec);
        }

        public String toString()
        {
            final StringBuffer sb = new StringBuffer();
            sb.append(moduleName.getText());
            sb.append(" **only**");
            for (Iterator it = importWithoutRenaming.iterator(); it.hasNext();)
            {
                sb.append(", ");

                Object imprt = it.next();
                if (imprt instanceof Token)
                    sb.append(((Token)imprt).getText());
                else
                {
                    ((ParseTreeNode)imprt).visitUsing(new GenericParseTreeVisitor()
                    {
                        public void visitToken(Token token)
                        {
                            sb.append(token.getText());
                        }
                    });
                }
            }
            sb.append(describeRenames());
            return sb.toString();
        }
    }

    /**
     * Causes the symbol table to &quot;forget&quot; what parse tree nodes are associated with its
     * entries and how many references there are to its entries.
     * 
     * This is intended to be used when entries are imported from one module into another via USE
     * statements. If this is not used, the symbol tables will hang on to large portions of the
     * parse trees of all of the files from which modules were imported.
     * 
     * @see ModuleImporter#getModuleEntryFromElsewhere(String)
     */
    public void stripParseTreesAndReferences()
    {
        this.visitUsing(new GenericSymbolTableVisitor()
        {
            public void visit(SymbolTableEntry entry)
            {
                entry.correspondingParseTreeNode = null;
                entry.references = null;
            }
        });
    }
}
