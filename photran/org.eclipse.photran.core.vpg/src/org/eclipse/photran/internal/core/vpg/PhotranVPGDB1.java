package org.eclipse.photran.internal.core.vpg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.rephraserengine.core.vpg.VPGLog;
import org.eclipse.rephraserengine.core.vpg.db.caching.CachingDB;
import org.eclipse.rephraserengine.core.vpg.db.cdt.CDTDB;

/**
 * Photran VPG database based on CDT's B-tree infrastructure and a caching decorator.
 * 
 * @author Jeff Overbey
 */
public class PhotranVPGDB1 extends CachingDB<IFortranAST, Token, PhotranTokenRef>
{
    public PhotranVPGDB1(PhotranVPGComponentFactory locator, File file, VPGLog<Token,PhotranTokenRef> log)
    {
        super(new PhotranCDTDB(locator, file, log), 500, 10000);
    }

    static class PhotranCDTDB extends CDTDB<IFortranAST, Token, PhotranTokenRef>
    {
        private PhotranCDTDB(PhotranVPGComponentFactory locator, File file, VPGLog<Token,PhotranTokenRef> log)
        {
            super(file, locator, log);
        }

        @Override
        protected long getModificationStamp(String filename)
        {
            if (PhotranVPG.getInstance().isVirtualFile(filename)) return Long.MIN_VALUE;

            IFile ifile = PhotranVPG.getIFileForFilename(filename);
            return ifile == null ? Integer.MIN_VALUE : ifile.getLocalTimeStamp();
        }

        @Override protected byte[] serialize(Serializable annotation) throws IOException
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PhotranVPGSerializer.serialize(annotation, out);
            return out.toByteArray();

//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            new ObjectOutputStream(out).writeObject(annotation);
//            return out.toByteArray();
        }

        @Override protected Serializable deserialize(InputStream binaryStream) throws IOException, ClassNotFoundException
        {
            return PhotranVPGSerializer.deserialize(binaryStream);

//            return (Serializable)new ObjectInputStream(binaryStream).readObject();
        }
    }

    @Override
    public String describeEdgeType(int edgeType)
    {
        return super.describeEdgeType(edgeType);
    }

    @Override
    public String describeAnnotationType(int annotationType)
    {
        return super.describeAnnotationType(annotationType);
    }

    @Override
    public String describeToken(String filename, int offset, int length)
    {
        return super.describeToken(filename, offset, length);
    }

    @Override
    public void clearDatabase()
    {
        super.db.clearDatabase();
    }

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
