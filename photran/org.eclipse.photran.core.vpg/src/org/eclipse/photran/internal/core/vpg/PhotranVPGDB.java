package org.eclipse.photran.internal.core.vpg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.Activator;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.rephraserengine.core.vpg.db.caching.CachingDB;
import org.eclipse.rephraserengine.core.vpg.db.cdt.CDTDB;

public class PhotranVPGDB extends CachingDB<IFortranAST, Token, PhotranTokenRef, PhotranVPGDB.PhotranCDTDB, PhotranVPGLog>
{
    public PhotranVPGDB()
    {
        super(new PhotranCDTDB(), 500, 10000);
    }
    
    static class PhotranCDTDB extends CDTDB<IFortranAST, Token, PhotranTokenRef, PhotranVPGLog>
    {
        public PhotranCDTDB()
        {
            this(PhotranVPG.inTestingMode()
                 ? createTempFile()
                 : Activator.getDefault().getStateLocation().addTrailingSeparator().toOSString() + "photran50vpg");
        }
        
        private PhotranCDTDB(String filename)
        {
            super(filename);
            
            if (FortranPreferences.ENABLE_VPG_LOGGING.getValue())
                System.out.println("Using Photran VPG database " + filename);
        }

        private static String createTempFile()
        {
            try
            {
                File f = File.createTempFile("vpg", null);
                f.deleteOnExit();
                return f.getAbsolutePath();
            }
            catch (IOException e)
            {
                throw new Error(e);
            }
        }
    
        @Override
        protected long getModificationStamp(String filename)
        {
            if (getVPG().isVirtualFile(filename)) return Long.MIN_VALUE;
            
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
    
    public String describeEdgeType(int edgeType)
    {
        return super.describeEdgeType(edgeType);
    }

    public String describeAnnotationType(int annotationType)
    {
        return super.describeAnnotationType(annotationType);
    }

    public String describeToken(String filename, int offset, int length)
    {
        return super.describeToken(filename, offset, length);
    }

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
        if (!getVPG().isVirtualFile(filename))
            super.deleteAllEdgesAndAnnotationsFor(filename);
    }
}
