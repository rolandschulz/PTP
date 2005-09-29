package org.eclipse.photran.internal.core.refactoring.preconditions;

import org.eclipse.photran.core.programrepresentation.Program;
import org.eclipse.photran.internal.core.f95parser.Terminal;
import org.eclipse.photran.internal.core.f95parser.Token;

public class TokenIsIdentifier extends AbstractPrecondition
{
    protected Token token;

    public TokenIsIdentifier(Program program, Token token)
    {
        super(program);

        this.token = token;
    }

    public boolean checkThisPrecondition()
    {
        if (token.getTerminal() == Terminal.T_IDENT)
            return true;
        else
        {
            setErrorMessage();
            return false;
        }
    }

    private void setErrorMessage()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(token.getText());
        sb.append(" is not an identifier ");
        sb.append(describeTokenPos(token));
        error = sb.toString();
    }
}
