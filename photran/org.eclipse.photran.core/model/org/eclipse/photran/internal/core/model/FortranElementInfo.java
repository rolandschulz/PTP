package org.eclipse.photran.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.core.model.CElementInfo;
import org.eclipse.cdt.internal.core.model.SourceRange;

/**
 * Our own version of <code>CElementInfo</code>, which is basically identical to
 * <code>SourceManipulationInfo</code> except that we don't support
 * <code>SourceManipulation</code>
 * 
 * @see CElementInfo
 * 
 * @author joverbey
 */
public class FortranElementInfo extends CElementInfo
{
    // --------------------------------------------------------------------
    // Methods copied from SourceManipulationInfo
    // --------------------------------------------------------------------

    protected int fStartPos;

    protected int fLength;

    protected int fIdStartPos;

    protected int fIdLength;

    protected int fStartLine;

    protected int fEndLine;

    int modifiers;

    public FortranElementInfo(CElement element)
    {
        super(element);
        setIsStructureKnown(true);
        modifiers = 0;
    }

    public void setPos(int startPos, int length)
    {
        fStartPos = startPos;
        fLength = length;
    }

    public int getStartPos()
    {
        return fStartPos;
    }

    public int getLength()
    {
        return fLength;
    }

    public void setIdPos(int startPos, int length)
    {
        fIdStartPos = startPos;
        fIdLength = length;
    }

    public int getIdStartPos()
    {
        return fIdStartPos;
    }

    public int getIdLength()
    {
        return fIdLength;
    }

    public int getStartLine()
    {
        return fStartLine;
    }

    public int getEndLine()
    {
        return fEndLine;
    }

    public void setLines(int startLine, int endLine)
    {
        fStartLine = startLine;
        fEndLine = endLine;
    }

    public ISourceRange getSourceRange()
    {
        return new SourceRange(fStartPos, fLength, fIdStartPos, fIdLength, fStartLine, fEndLine);
    }

    /**
     * @see ISourceReference
     */
    public String getSource() throws CModelException
    {
        ITranslationUnit unit = getTranslationUnit();
        IBuffer buffer = unit.getBuffer();
        if (buffer == null) { return null; }
        int offset = fStartPos;
        int length = fLength;
        if (offset == -1 || length == 0) { return null; }
        try
        {
            return buffer.getText(offset, length);
        }
        catch (RuntimeException e)
        {
            return null;
        }

//		ITranslationUnit tu = getTranslationUnit();
//		if (tu != null) {
//			try {
//				IResource res = tu.getResource();
//				if (res != null && res instanceof IFile) {
//					StringBuffer buffer = Util.getContent((IFile)res);
//					return  buffer.substring(getElement().getStartPos(),
//							getElement().getStartPos() + getElement().getLength());
//				}
//			} catch (IOException e) {
//				throw new CModelException(e, ICModelStatusConstants.IO_EXCEPTION);
//			} catch (StringIndexOutOfBoundsException bound) {
//				// This is not good we screwed up the offset some how
//				throw new CModelException(bound, ICModelStatusConstants.INDEX_OUT_OF_BOUNDS);
//			}
//		}
//		return ""; //$NON-NLS-1$
	}

	/**
     * @see IMember
     */
    public ITranslationUnit getTranslationUnit()
    {
        ICElement celem = getElement();
        for (; celem != null; celem = celem.getParent())
        {
            if (celem instanceof ITranslationUnit) return (ITranslationUnit)celem;
        }
        return null;
    }
	
//	/**
//	 * return the element modifiers
//	 * @return int
//	 */
//	public int getModifiers(){
//		return modifiers;
//	}
//	
//	/**
//	 *  subclasses  should override
//	 */
//	public boolean hasSameContentsAs( FortranElementInfo otherInfo){
//		return (this.element.fType == otherInfo.element.fType);
//	}
}
