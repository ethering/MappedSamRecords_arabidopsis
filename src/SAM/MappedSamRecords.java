package SAM;

/*
 * Takes a SAM File, prints the header and then prints any entries where the
 * read or its mate (or both) are mapped
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

/**
 *
 * @author ethering
 */
public class MappedSamRecords
{

    public HashMap<String, HashSet<String>> getMappedSamRecords(String fileString)
    {
        
        File inFile = new File(fileString);
        HashMap<String, HashSet<String>> mappedReads = new HashMap<String, HashSet<String>>();
        final SAMFileReader reader = new SAMFileReader(inFile);
        reader.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);

        // Open an iterator for the particular sequence
        SAMRecordIterator iterator = reader.iterator();
        int mappedtoCdna = 0;

        while (iterator.hasNext())
        {
            SAMRecord samRecord = iterator.next();
            //is the read mapped?
            boolean unMapped = samRecord.getReadUnmappedFlag();
            
            //..if so...
            if (unMapped == false)
            {
                //get the read name
                String currentRead = samRecord.getReadName();
                //get the reference to which it's mapped
                String [] mappedToArray = samRecord.getReferenceName().split("\\.");
                String mappedTo = mappedToArray[0];
                //if the read is on the negative strand, create a reverse-strand gene 'R_'
                boolean onNegativeStrand = samRecord.getReadNegativeStrandFlag();
                if (onNegativeStrand)
                {
                    mappedTo = "R_".concat(mappedTo);
                }
                if (mappedReads.containsKey(currentRead))
                {
                    HashSet<String> al = mappedReads.get(currentRead);
                    al.add(mappedTo);
                    mappedReads.put(currentRead, al);
                    mappedtoCdna++;
                }
                else
                {
                    HashSet<String> al = new HashSet<String>();
                    al.add(mappedTo);
                    mappedReads.put(currentRead, al);
                    mappedtoCdna++;
                }
            }
        }
        System.out.println(mappedtoCdna + " reads mapped to cdna");
        return mappedReads;
    }

    public File getFinalExpressions(HashMap<String, HashSet<String>> readScores, String outfileString)
    {
        //input is the sam file and a hashmap of <Readname, occurences>
        File outFile = new File(outfileString);
        //a hashMap for printing the final values e.g. AT5G66930.3	9.666666666666666
        HashMap<String, Double> finalExpression = new HashMap<String, Double>();
        Iterator it = readScores.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pairs = (Map.Entry) it.next();
            //String readId = (String) pairs.getKey();
            HashSet<String> str = (HashSet<String>) pairs.getValue();
            Double readScore = 1 / Double.valueOf(str.size());

            Iterator strIt = str.iterator();
            while (strIt.hasNext())
            {
                String geneid = (String) strIt.next();
                if (finalExpression.containsKey(geneid))
                {
                    Double total = finalExpression.get(geneid);
                    finalExpression.put(geneid, total + readScore);
                }
                else
                {
                    finalExpression.put(geneid, readScore);
                }
            }
        }
        FileOutputStream out; // declare a file output object
        PrintStream p; // declare a print stream object

        try
        {
            out = new FileOutputStream(outFile);

            // Connect print stream to the output stream
            p = new PrintStream(out);

            Iterator mapit = finalExpression.entrySet().iterator();
            while (mapit.hasNext())
            {
                Map.Entry pairs = (Map.Entry) mapit.next();
                p.println(pairs.getKey() + "\t" + pairs.getValue());
            }

            p.close();
        }
        catch (Exception e)
        {
            System.err.println("Error writing to file");
        }
        return outFile;
    }
}