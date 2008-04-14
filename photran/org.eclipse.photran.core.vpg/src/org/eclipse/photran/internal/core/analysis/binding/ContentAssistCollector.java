package org.eclipse.photran.internal.core.analysis.binding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;

public class ContentAssistCollector
{
    protected HashMap<ScopingNode, ArrayList<Definition>> defs = new HashMap<ScopingNode, ArrayList<Definition>>();
    
    public ContentAssistCollector(IFortranAST ast)
    {
        ast.visitTopDownUsing(new NaiveDefCollector());
    }

    private final class NaiveDefCollector extends DefinitionCollector
    {
        private NaiveDefCollector()
        {
            super(null);
            
            this.vpg = null;
            this.file = null;
        }

        protected void markModuleExport(IFile file, String moduleName)
        {
        }

        protected void setScopeDefaultVisibilityToPrivate(ScopingNode scope)
        {
        }

        protected void setDefinition(Token ident, Definition def)
        {
        }

        Definition addDefinition(Token token, Definition.Classification classification, Type type)
        {
            if (token == null) return null;
            
            try
            {
                ScopingNode scope = token.getEnclosingScope();
                if (!defs.containsKey(scope))
                    defs.put(scope, new ArrayList<Definition>(16));
                
                Definition result = new Definition(token.getText(), token.getTokenRef(), classification, type);
                defs.get(scope).add(result);
                return result;
            }
            catch (Exception e)
            {
                throw new Error(e);
            }
        }

        void importDefinition(Definition definitionToImport, ScopingNode importIntoScope)
        {
        }

        List<PhotranTokenRef> bindAsParam(Token identifier)
        {
            return bind(identifier);
        }

        void bind(Token identifier, PhotranTokenRef toDefinition)
        {
        }

        void bindRenamedEntity(Token identifier, PhotranTokenRef toDefinition)
        {
        }
    }
}
