/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.analysis.symtab;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rephraserengine.core.util.TwoKeyHashMap;

/**
 * A simple, generic symbol table with nested scopes.
 * <p>
 * THIS IS PRELIMINARY AND EXPERIMENTAL.  IT IS NOT APPROPRIATE FOR PRODUCTION USE.
 * 
 * @author Jeff Overbey
 * 
 * @param <N> namespace type
 * @param <S> symbol type
 */
public class SymbolTable<N, S>
{
    /**
     * Settings that determine how this symbol table and its children will operate.
     */
    public static class Settings<S>
    {
        /** Constructor.  See {@link #errorEntry}. */
        public Settings() { this.errorEntry = null; }
        
        /** Constructor.  See {@link #errorEntry}. */
        public Settings(S errorEntry) { this.errorEntry = errorEntry; }
        
        /**
         * A (unique) error symbol which will be returned whenever lookup fails.
         * May be <code>null</code>.
         * <p>
         * Failure will always be determined by comparing symbols <i>pointerwise</i>
         * with this value.
         * <p>
         * The value of this field is set via the {@link Settings#Settings(Object)}
         * constructor.
         */
        public final S errorEntry;
        
        /**
         * If a lookup fails in this scope, this setting determines whether the
         * {@link SymbolTable#lookup(Object, String)} method should fail immediately
         * (<code>false</code>) or if it should attempt to look up the name in an
         * outer scope (<code>true</code>).
         */
        public boolean resolveInOuterScopeIfLookupFails = true;
        
        /**
         * This determines whether {@link SymbolTable#enter(Object, String, Object)}
         * should allow a symbol to be added if it already exists in an outer scope.
         * <p>
         * If {@link #resolveInOuterScopeIfLookupFails} is <code>false</code>, this
         * setting has no effect, since a lookup will never search an outer scope.
         */
        public boolean allowShadowing = true;
        
        /**
         * This determines whether symbol names are case-sensitive.  If
         * <code>false</code>, all symbol names will be converted to lower case.
         */
        public boolean caseSensitive = true;
    }
    
    /**
     * Settings for this symbol table hierarchy, including what symbol will be
     * returned when a lookup fails and what scoping rules should apply.
     */
    protected final Settings<S> settings;
    
    /**
     * Symbol table representing the outer scope, or <code>null</code> if this is the
     * outermost (global) scope.
     */
    protected final SymbolTable<N, S> parent;
    
    /**
     * Entries in the scope represented by this symbol table.  Maps a namespace and
     * a name to a symbol.
     */
    protected final TwoKeyHashMap<N, String, S> entries;
    
    /**
     * Named scopes contained in this symbol table.  Maps a scope name to a symbol table.
     */
    protected final Map<String, SymbolTable<N, S>> namedScopes;
    
    public SymbolTable()
    {
        this.settings = new Settings<S>();
        this.entries = new TwoKeyHashMap<N, String, S>();
        this.namedScopes = new HashMap<String, SymbolTable<N, S>>();
        this.parent = null;
    }
    
    public SymbolTable(Settings<S> settings)
    {
        assert settings != null;
        
        this.settings = settings;
        this.entries = new TwoKeyHashMap<N, String, S>();
        this.namedScopes = new HashMap<String, SymbolTable<N, S>>();
        this.parent = null;
    }
    
    public SymbolTable(SymbolTable<N, S> parent)
    {
        assert parent != null;
        
        this.settings = parent.settings;
        this.entries = new TwoKeyHashMap<N, String, S>();
        this.namedScopes = new HashMap<String, SymbolTable<N, S>>();
        this.parent = parent;
    }
    
    protected String canonicalize(String name)
    {
        assert name != null;
        
        if (settings.caseSensitive)
            return name;
        else
            return name.toLowerCase();
    }
    
    /**
     * Looks up the given name in the given namespace.
     * 
     * @param namespace
     * @param name
     * @return the corresponding symbol, or {@link Settings#errorEntry} if the symbol
     *         cannot be found
     */
    public S lookup(N namespace, String name)
    {
        assert namespace != null && name != null;
        
        name = canonicalize(name);
        
        if (entries.containsEntry(namespace, name))
            return entries.getEntry(namespace, name);
        else if (parent != null && settings.resolveInOuterScopeIfLookupFails)
            return parent.lookup(namespace, name);
        else
            return settings.errorEntry;
    }

    /**
     * @return <code>true</code> iff {@link #lookup(Object, String)} will succeed
     *         for the given namespace and name
     */
    public boolean contains(N namespace, String name)
    {
        assert namespace != null && name != null;
        
        return lookup(namespace, name) != settings.errorEntry;
    }
    
    /**
     * Adds an entry to the symbol type for the given name in the given namespace.
     * 
     * @param namespace
     * @param name
     * @param symbol
     * @return the symbol iff entry succeeds, or <code>null</code> if the symbol
     *         cannot be added because it will conflict with an existing symbol
     */
    public S enter(N namespace, String name, S symbol)
    {
        assert namespace != null && name != null && symbol != null;
        
        name = canonicalize(name);
        
        if (!canEnter(namespace, name))
            return null;
        else
            return entries.put(namespace, name, symbol);
    }
    
    /**
     * Adds an entry to the symbol type if there is not already a symbol for the
     * given name in the given namespace; otherwise, returns the existing symbol.
     * 
     * @param namespace
     * @param name
     * @param symbol
     * @return the symbol corresponding to the given name in the given namespace
     */
    public S ensure(N namespace, String name, S symbol)
    {
        assert namespace != null && name != null && symbol != null;
        
        name = canonicalize(name);
        
        if (!canEnter(namespace, name))
            return lookup(namespace, name);
        else
            return entries.put(namespace, name, symbol);
    }
    
    /**
     * @return <code>true</code> iff {@link #enter(Object, String, Object)} will
     *         succeed for the given namespace and name
     */
    public boolean canEnter(N namespace, String name)
    {
        assert namespace != null && name != null;
        
        name = canonicalize(name);
        
        if (entries.containsEntry(namespace, name))
            return false;
        else if (parent != null && settings.resolveInOuterScopeIfLookupFails)
            return settings.allowShadowing || parent.lookup(namespace, name) == settings.errorEntry;
        else
            return true;
    }
    
    /**
     * Returns a new symbol table for a child scope of this scope (i.e., a nested
     * scope).
     * <p>
     * Note that this does not modify this {@link SymbolTable} object; the correct
     * usage is <code>symtab = symtab.enterScope()</code>.
     * 
     * @return a new symbol table for a child scope of this scope
     */
    public SymbolTable<N, S> enterScope()
    {
        return new SymbolTable<N, S>(this);
    }
    
    /**
     * Returns a new symbol table for a child scope of this scope (i.e., a nested
     * scope).
     * <p>
     * Note that this does not modify this {@link SymbolTable} object; the correct
     * usage is <code>symtab = symtab.enterScope()</code>.
     * 
     * @return a new symbol table for a child scope of this scope
     */
    public SymbolTable<N, S> enterNamedScope(String name)
    {
        SymbolTable<N, S> namedScope = enterScope();
        namedScopes.put(name, namedScope);
        return namedScope;
    }
    
    /**
     * Returns the symbol table for the parent scope of this scope (i.e., the next
     * outermost scope).
     * <p>
     * Note that this does not modify this {@link SymbolTable} object; the correct
     * usage is <code>symtab = symtab.exitScope()</code>.
     * 
     * @return the symbol table for the parent scope of this scope, or
     *         <code>null</code> iff this symbol table has no parent (i.e., it
     *         represents the outermost scope)
     */
    public SymbolTable<N, S> exitScope()
    {
        return parent;
    }

    /**
     * Returns the symbol table for the outermost scope containing this scope (i.e.,
     * the ancester symbol table with a <code>null</code> parent symbol table).
     * <p>
     * Note that this does not modify this {@link SymbolTable} object; the most
     * common usage is <code>symtab.outermostScope().enter(...)</code>.
     * 
     * @return the symbol table for the parent scope of this scope
     */
    public SymbolTable<N, S> outermostScope()
    {
        if (parent == null)
            return this;
        else
            return parent.outermostScope();
    }
    
    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (N namespace : entries.keySet())
        {
            sb.append(namespace.toString().toUpperCase() + " NAMESPACE:\n");
            
            for (String name : entries.getAllEntriesFor(namespace).keySet())
                sb.append("    " + name + " -> " + entries.getEntry(namespace, name) + "\n");
            
            sb.append("\n");
        }

        if (!namedScopes.isEmpty())
        {
            sb.append("This symbol table contains the following named scopes:\n");
            
            for (String name : namedScopes.keySet())
            {
                sb.append("    ");
                sb.append(name);
                sb.append("\n");
            }
        }

        if (parent != null)
        {
            sb.append("\n\n==================== PARENT ====================\n");
            sb.append(parent.toString());
        }
        
        return sb.toString();
    }
}
