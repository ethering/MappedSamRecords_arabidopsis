package GFF;

import SAM.MappedSamRecords;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ethering
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            String mappedCdnafile = args[0];
            String gffFile = args[1];
            String mappedGenomeFile = args[2];
            String searchArea = args[3];
            String outFile = args[4];
            MappedSamRecords msr = new MappedSamRecords();
            GffHandler gffh = new GffHandler();
            HashMap<String, ContigPosition> cps = gffh.getContigPosistionsFromGff (gffFile, searchArea);
            HashMap<String, HashSet<String>> hm = msr.getMappedSamRecords(mappedCdnafile);
            hm = gffh.getMappedSamRecordsFromGff2(cps, mappedGenomeFile, hm, searchArea);
            File f = msr.getFinalExpressions(hm, outFile);
            
        }
        catch (IOException ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Exception ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
