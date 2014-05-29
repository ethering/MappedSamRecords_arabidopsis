package GFF;


//import BamParser.ContigPosition;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.biojava3.genome.parsers.gff.FeatureI;
import org.biojava3.genome.parsers.gff.FeatureList;
import org.biojava3.genome.parsers.gff.GFF3Reader;
import org.biojava3.genome.parsers.gff.Location;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ethering
 */
public class GffHandler
{    
    public HashMap<String, ContigPosition> getContigPosistionsFromGff (String gffFileString, String searchAreaString) throws IOException
    {
        HashMap<String, ContigPosition> cps = new HashMap<String, ContigPosition>();
        int searchArea = Integer.valueOf(searchAreaString);
        
        FeatureList fl = GFF3Reader.read(gffFileString);
        fl = new FeatureList(fl.selectByType("gene"));
        Iterator it = fl.iterator();
        while (it.hasNext())
        {
            FeatureI fi = (FeatureI) it.next();
            String chr = fi.seqname();
            Location loc = fi.location();
            int start = loc.bioStart();
            int end = loc.bioEnd();
            char strand = loc.bioStrand(); // '+' or '-'
            String geneid = fi.getAttribute("ID");
            int searchStart = start - searchArea;
            int searchEnd = end + searchArea;

            ContigPosition cp = new ContigPosition(chr, searchStart, searchEnd, strand);
            cps.put(geneid, cp);           
        }

        
        return cps;
    }

    public HashMap<String, HashSet<String>> getMappedSamRecordsFromGff2(HashMap<String, ContigPosition> cps, String samFileString, HashMap<String, HashSet<String>> mappedReads, String searchAreaString) throws IOException, Exception
    {

        File inFile = new File(samFileString);
        final SAMFileReader reader = new SAMFileReader(inFile);
        reader.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);

        SAMRecordIterator iterator = reader.iterator();
        int extraReadsMapped = 0;

        while (iterator.hasNext())
        {
            SAMRecord samRecord = iterator.next();
            int readstart = samRecord.getAlignmentStart();
            int readend = samRecord.getAlignmentEnd();

            String chr = samRecord.getReferenceName();
            String currentRead = samRecord.getReadName();
            
            Iterator cpsit = cps.entrySet().iterator();
            while (cpsit.hasNext())
            {
                Map.Entry pairs = (Map.Entry) cpsit.next();
                ContigPosition cp = (ContigPosition) pairs.getValue();
                String currentChr = cp.getContigid();
                
                if (currentChr.equalsIgnoreCase(chr))
                {
                    int cpstart = cp.getStart();
                    int cpend = cp.getEnd();
                    char strand = cp.getStrand();
                    if (readend >= cpstart && readend <= cpend || readstart <= cpend && readstart >= cpstart)
                    {
                        //the read overlaps with a feature
                        String mappedTo = (String) pairs.getKey();
                        boolean onNegativeStrand = samRecord.getReadNegativeStrandFlag();
                        if (onNegativeStrand)
                        {
                            if (strand == '-')
                            {
                                if (mappedReads.containsKey(currentRead))
                                {
                                    HashSet<String> al = mappedReads.get(currentRead);
                                    mappedTo = mappedTo.replaceFirst("R_", "");
                                    al.add(mappedTo);
                                    mappedReads.put(currentRead, al);
                                    extraReadsMapped++;
                                }
                                else
                                {
                                    HashSet<String> al = new HashSet<String>();
                                    mappedTo = mappedTo.replaceFirst("R_", "");
                                    al.add(mappedTo);
                                    mappedReads.put(currentRead, al);
                                    extraReadsMapped++;
                                }
                            }
                            else//strand == +
                            {
                                if (mappedReads.containsKey(currentRead))
                                {
                                    HashSet<String> al = mappedReads.get(currentRead);
                                    al.add(mappedTo);
                                    mappedReads.put(currentRead, al);
                                    extraReadsMapped++;
                                }
                                else
                                {
                                    HashSet<String> al = new HashSet<String>();
                                    al.add(mappedTo);
                                    mappedReads.put(currentRead, al);
                                    extraReadsMapped++;
                                }
                            }
                        }
                        else//it's on the positive strand
                        {
                            
                            if (strand == '+')
                            {
                                if (mappedReads.containsKey(currentRead))
                                {
                                    HashSet<String> al = mappedReads.get(currentRead);
                                    al.add(mappedTo);
                                    mappedReads.put(currentRead, al);
                                    extraReadsMapped++;
                                }
                                else
                                {
                                    HashSet<String> al = new HashSet<String>();
                                    al.add(mappedTo);
                                    mappedReads.put(currentRead, al);
                                    extraReadsMapped++;
                                }
                            }
                            else//strand == -
                            {
                                if (mappedReads.containsKey(currentRead))
                                {
                                    HashSet<String> al = mappedReads.get(currentRead);
                                    mappedTo = "R_".concat(mappedTo);
                                   
                                    al.add(mappedTo);
                                    mappedReads.put(currentRead, al);
                                    extraReadsMapped++;
                                }
                                else
                                {
                                    HashSet<String> al = new HashSet<String>();
                                    mappedTo = "R_".concat(mappedTo);
                                    al.add(mappedTo);
                                    mappedReads.put(currentRead, al);
                                    extraReadsMapped++;
                                }
                            }
                            
                        }
                    }
                }
            }
        }
        System.out.println(extraReadsMapped + " extra reads mapped to genome");
        return mappedReads;
    }
}
