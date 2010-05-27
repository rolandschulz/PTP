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
package org.eclipse.photran.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTPrintStmtNode;
import org.eclipse.photran.internal.core.parser.ASTStringConstNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * An Easter egg  :-)
 * <p>
 * When the cursor is moved over a statement like
 * <pre>
 *     print *, "Hi"
 * </pre>
 * (where "Hi" can be any literal string) and the user presses Ctrl+Alt+Command+Shift+6, the
 * statement is transformed into seven print statements using a banner font, i.e.,
 * <pre>
 *    print *, "HH    HH IIIIIIII "
 *    print *, "HH    HH    II    "
 *    print *, "HH    HH    II    "
 *    print *, "HHHHHHHH    II    "
 *    print *, "HH    HH    II    "
 *    print *, "HH    HH    II    "
 *    print *, "HH    HH IIIIIIII "
 * </pre>
 * <p>
 * The project must have analysis/refactoring enabled.
 * <p>
 * Since the key combination requires the Command key to be pressed, this is effectively available
 * only on Macs.
 *
 * @author Jeff Overbey
 */
public class EasterEgg extends FortranEditorRefactoring
{
    private ASTPrintStmtNode printStmt = null;
    private String string = null;

    @Override
    public String getName()
    {
        return "Easter Egg"; //$NON-NLS-1$
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        if (!canRunEasterEgg())
            status.addFatalError("The selected operation is not available."); // Bogus but common error message //$NON-NLS-1$
    }

    private boolean canRunEasterEgg()
    {
        if (!PhotranVPG.getInstance().doesProjectHaveRefactoringEnabled(fileInEditor)) return false;

        Token token = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (token == null) return false;

        printStmt = token.findNearestAncestor(ASTPrintStmtNode.class);
        if (printStmt == null) return false;

        if (printStmt.getOutputItemList() == null
            || printStmt.getOutputItemList().getSingleExpr() == null
            || !(printStmt.getOutputItemList().getSingleExpr() instanceof ASTStringConstNode))
            return false;

        ASTStringConstNode stringConst = (ASTStringConstNode)printStmt.getOutputItemList().getSingleExpr();
        if (stringConst.getSubstringRange() != null)
            return false;

        if (!(printStmt.getParent() instanceof IASTListNode)) return false;

        string = removeQuotes(stringConst.getStringConst().getText());
        return true;
    }

    private String removeQuotes(String string)
    {
        string = string.substring(1, string.length()-1);
        string = string.replaceAll("''", "'"); //$NON-NLS-1$ //$NON-NLS-2$
        string = string.replaceAll("\"\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
        return string;
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        try
        {
            ASTPrintStmtNode[] newStmts = new ASTPrintStmtNode[FONT_HEIGHT];
            for (int i = FONT_HEIGHT-1; i >= 0; i--)
            {
                newStmts[i] = (ASTPrintStmtNode)printStmt.clone();
                ASTStringConstNode stringConst = (ASTStringConstNode)newStmts[i].getOutputItemList().getSingleExpr();
                stringConst.getStringConst().setText("\"" + rasterize(i, string) + "\""); //$NON-NLS-1$ //$NON-NLS-2$

                if (i != 0)
                    removeAllWhitetextExceptIndentation(newStmts[i].findFirstToken());

                ((IASTListNode)printStmt.getParent()).insertAfter(printStmt, newStmts[i]);
            }
            printStmt.removeFromTree();

            this.addChangeFromModifiedAST(this.fileInEditor, pm);
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    private void removeAllWhitetextExceptIndentation(Token token)
    {
        String s = token.getWhiteBefore();
        int lf = s.lastIndexOf('\n');
        if (lf >= 0)
            token.setWhiteBefore(s.substring(lf+1));
    }

    private static final int FONT_WIDTH = 9;
    private static final int FONT_HEIGHT = 7;

    private static String[] font = new String[]
    {
    //12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678 12345678",
     "           AAAA   BBBBBBB    CCCCC  DDDDDD   EEEEEEEE FFFFFFFF   GGGGG  HH    HH IIIIIIII      JJJ KK    KK LL       MM    MM NNN   NN   OOOO   PPPPPPP    QQQQ   RRRRRRR   SSSSSSS TTTTTTTT UU    UU VV    VV WW    WW XX    XX YY    YY ZZZZZZZZ    11     222222   333333       444 55555555   6666   77777777  888888    9999     0000               !!      ????   ", //$NON-NLS-1$
     "          AA  AA  BB    BB  CC    C DD   DD  EE       FF        GG    G HH    HH    II          JJ KK   KK  LL       MMM  MMM NNNN  NN  OO  OO  PP    PP  QQ  QQ  RR    RR SS          TT    UU    UU VV    VV WW    WW  XX  XX   YY  YY       ZZ   1111    22    22 33    33     4444 55        66           77  88    88 99   99   00  00             !!!!    ??  ??  ", //$NON-NLS-1$
     "         AA    AA BB    BB CC       DD    DD EE       FF       GG       HH    HH    II          JJ KK  KK   LL       MM MM MM NN NN NN OO    OO PP    PP QQ    QQ RR    RR SS          TT    UU    UU VV    VV WW    WW   XXXX     YYYY       ZZ      11         22        33    44 44 55 555   66           77   88    88 99   999 00    00            !!!!        ??  ", //$NON-NLS-1$
     "         AAAAAAAA BBBBBB   CC       DD    DD EEEEE    FFFFF    GG  GGGG HHHHHHHH    II          JJ KKKK     LL       MM MM MM NN NN NN OO    OO PPPPPPP  QQ    QQ RRRRRRR   SSSSSS     TT    UU    UU VV    VV WW WW WW    XX       YY       ZZ       11      222       333     44  44 555   55 66 6666     77     888888   9999 99 00    00             !!       ??    ", //$NON-NLS-1$
     "         AA    AA BB    BB CC       DD    DD EE       FF       GG    GG HH    HH    II    JJ    JJ KK  KK   LL       MM    MM NN NN NN OO    OO PP       QQ  Q QQ RR  RR         SS    TT    UU    UU  VV  VV  WW WW WW   XXXX      YY      ZZ        11     22            33  4444444       55 666   66   77     88    88       99 00    00             !!       ??    ", //$NON-NLS-1$
     "         AA    AA BB    BB  CC    C DD   DD  EE       FF        GG   GG HH    HH    II    JJ    JJ KK   KK  LL       MM    MM NN  NNNN  OO  OO  PP        QQ  QQ  RR   RR        SS    TT    UU    UU   VVVV   WWW  WWW  XX  XX     YY     ZZ         11    22       33    33       44 5     55  66   66  77      88    88      99   00  00  ...                        ", //$NON-NLS-1$
     "         AA    AA BBBBBBB    CCCCC  DDDDDD   EEEEEEEE FF         GGGGG  HH    HH IIIIIIII  JJJJJ   KK    KK LLLLLLLL MM    MM NN   NNN   OOOO   PP         QQQQ Q RR    RR SSSSSSS     TT     UUUUUU     VV    WW    WW XX    XX    YY    ZZZZZZZZ 11111111 22222222  333333        44  555555    6666   77        888888    9999     0000   ...         !!       ??    ", //$NON-NLS-1$
    };

    private static int index(char ch)
    {
        switch (Character.toUpperCase(ch))
        {
            case 'A': return 1;
            case 'B': return 2;
            case 'C': return 3;
            case 'D': return 4;
            case 'E': return 5;
            case 'F': return 6;
            case 'G': return 7;
            case 'H': return 8;
            case 'I': return 9;
            case 'J': return 10;
            case 'K': return 11;
            case 'L': return 12;
            case 'M': return 13;
            case 'N': return 14;
            case 'O': return 15;
            case 'P': return 16;
            case 'Q': return 17;
            case 'R': return 18;
            case 'S': return 19;
            case 'T': return 20;
            case 'U': return 21;
            case 'V': return 22;
            case 'W': return 23;
            case 'X': return 24;
            case 'Y': return 25;
            case 'Z': return 26;
            case '1': return 27;
            case '2': return 28;
            case '3': return 29;
            case '4': return 30;
            case '5': return 31;
            case '6': return 32;
            case '7': return 33;
            case '8': return 34;
            case '9': return 35;
            case '0': return 36;
            case '.': return 37;
            case '!': return 38;
            case '?': return 39;
            default:  return 0;
        }
    }

    private static String rasterize(int line, String string)
    {
        StringBuilder sb = new StringBuilder(string.length() * FONT_WIDTH);
        for (int i = 0; i < string.length(); i++)
            sb.append(rasterize(line, string.charAt(i)));
        return sb.toString();
    }

    private static String rasterize(int line, char ch)
    {
        int start = index(ch) * FONT_WIDTH;
        int end = start + FONT_WIDTH;
        return font[line].substring(start, end);
    }
}
