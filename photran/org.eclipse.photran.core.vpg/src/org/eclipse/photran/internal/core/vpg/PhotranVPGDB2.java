package org.eclipse.photran.internal.core.vpg;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.rephraserengine.core.vpg.VPGLog;
import org.eclipse.rephraserengine.core.vpg.db.ram.RAMDB;

/**
 * Photran VPG database implemented as a persisted in-memory database.
 * 
 * @author Jeff Overbey
 */
public class PhotranVPGDB2 extends RAMDB<IFortranAST, Token, PhotranTokenRef>
{
    PhotranVPGDB2(PhotranVPGComponentFactory locator, File file, VPGLog<Token, PhotranTokenRef> log)
    {
        super(locator, file);
    }

    @Override
    public long getModificationStamp(String filename)
    {
        if (PhotranVPG.getInstance().isVirtualFile(filename)) return Long.MIN_VALUE;

        IFile ifile = PhotranVPG.getIFileForFilename(filename);
        return ifile == null ? Integer.MIN_VALUE : ifile.getLocalTimeStamp();
    }
    
    @Override protected Object readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        return in.readObject();
    }

//    @Override protected byte[] serialize(Serializable annotation) throws IOException
//    {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        PhotranVPGSerializer.serialize(annotation, out);
//        return out.toByteArray();
//    }
//
//    @Override protected Serializable deserialize(InputStream binaryStream) throws IOException, ClassNotFoundException
//    {
//        return PhotranVPGSerializer.deserialize(binaryStream);
//    }

    @Override
    public void deleteAllEdgesAndAnnotationsFor(String filename)
    {
        // module:whatever entries do not have edges, but they have
        // annotations.  However, these are populated when the
        // corresponding "real" file (whatever.f90) is parsed.
        // We should not delete them here, because populateVPG()
        // will not reconstruct them.
        if (!PhotranVPG.getInstance().isVirtualFile(filename))
            super.deleteAllEdgesAndAnnotationsFor(filename);
    }

    // HYPOTHETICAL UPDATING ///////////////////////////////////////////////////

    @Override public void enterHypotheticalMode() throws IOException
    {
        PhotranVPG.getProvider().moduleSymTabCache.clear();
        super.enterHypotheticalMode();
    }

    @Override public void leaveHypotheticalMode() throws IOException
    {
        PhotranVPG.getProvider().moduleSymTabCache.clear();
        super.leaveHypotheticalMode();
    }
}
