MappedSamRecords counts reads mapped to the Arabidopsis genome. It takes two maps (typicall BAM files as output from BWA - Samtools), one of reads mapped to the genome and on of reads mapped to the transcriptome.   
It then takes a GFF annotation file, a given search distance and includes reads mapped to the genome within the search area upstream and downstream of each gene (to account for in accurate gene calling).

The output is read counts for every Arabidopsis gene, e.g.   
```
ID	wtms_1   
at1g01010	22927   
at1g01020	29694   
at1g01030	404   
at1g01040	43127   
at1g01050	339158   
at1g01060	15756   
at1g01070	135845   
at1g01073	0   
at1g01080	27371  
``` 