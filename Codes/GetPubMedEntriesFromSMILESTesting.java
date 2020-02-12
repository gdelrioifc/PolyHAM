import java.io.*;
import java.util.*;
import java.net.*;

public class GetPubMedEntriesFromSMILESTesting
{
 public static String getCID(String smile) throws Exception
  {
   String result="";

   int i=0;
   String line="";
   LinkedList cids = new LinkedList();
   BufferedReader reader = null;
   URL url = new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/smiles/"+smile+"/cids/TXT");
   HttpURLConnection conn = (HttpURLConnection)url.openConnection();
   conn.connect();
   if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
    {
     reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

     while((line=reader.readLine())!=null)
      {
       cids.add(line.trim());
      }
     reader.close();

     for(i=0; i<cids.size(); i++)
      {
       if(result.equals("")) result=(String)cids.get(i);
       else result=result+","+(String)cids.get(i);
      }
    }
   else
    {
     result="HTTP Connection Error";
    }
   return result;
  }// end getCID

 public static void saveMEDLINE2File(String fileName, String pubmedids) throws Exception
  {
   String currentDirectory = System.getProperty("user.dir"), line="";
   if(!currentDirectory.endsWith(File.separator)) currentDirectory=currentDirectory+File.separator;
   BufferedWriter outfile = new BufferedWriter(new FileWriter(currentDirectory+fileName+".medline"));
   BufferedReader reader = null;
   URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="+pubmedids+"&rettype=MEDLINE&retmod=text");
   HttpURLConnection conn = (HttpURLConnection)url.openConnection();
   conn.connect();
   if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
    {
     reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

     while((line=reader.readLine())!=null)
      {
       outfile.write(line+"\n");
      }
     reader.close();
     outfile.flush();
     outfile.close();
    }
   else
    {
     outfile.write("HTTP Connection Error");
     outfile.flush();
     outfile.close();
    }
  }// end saveMEDLINE2File

 public static String countTokens(String s) throws Exception
  {
   String result;

   String[] data=s.split(",");
   result=String.valueOf(data.length);

   return result;
  }// end countTokens

 public static String getPubMedID(String cid) throws Exception
  {
   String result="";

   String line="";
   URL url = new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+cid+"/xrefs/PubMedID/TXT");
   BufferedReader reader = null;
   HttpURLConnection conn = (HttpURLConnection)url.openConnection();
   conn.connect();
   if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
    {
     reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

     while((line=reader.readLine())!=null)
      {
       if(result.equals("")) result=line.trim();
       else result=result+","+line.trim();
      }
     reader.close();
    }
   else
    {
     result="HTTP Connection Error";
    }
   return result;
  }// end getPubMedID

 public static void printData(String f) throws Exception
  {
   int i=0;
   String line="", cid="", pubmedids="";
   String[] data=null, cids=null;
   BufferedReader infile = new BufferedReader(new FileReader(f));

   while((line=infile.readLine())!=null)
    {
     data =line.split(",");
     cid=getCID(data[1]);
System.err.println(data[1]+":"+cid);
     if(cid.indexOf(",")>-1)
      {
       cids=cid.split(",");
       for(i=0; i<cids.length; i++)
        {
         pubmedids=getPubMedID(data[i]);
         System.out.println(data[0]+" included "+countTokens(pubmedids)+" PubMedIDs.");
         saveMEDLINE2File(data[0],pubmedids);
         Thread.sleep(3000);
        }
      }
     else
      {
       if(!cid.equals(""))
        {
         pubmedids=getPubMedID(cid);
         System.out.println(data[0]+" included 1 PubMedID.");
         saveMEDLINE2File(data[0],pubmedids);
        }
      }
    }
   infile.close();
  }// end printData

 public static void main(String[] args) throws Exception
  {
   if(args.length==1)
    {
     printData(args[0]);
    }
   else
    {
     System.err.println("Usage:\njava GetPubMedEntriesFromSMILESTesting <infile>");
     System.err.println("<infile> is the file named hmdbmetab-metabolites.csv in this dir.");
     System.err.println("The program will get the SMILES listed in <infile> and get PubMedIDs related to each of them,");
     System.err.println("then will save in MEDLINE format each of the PubMedIDs to a file that will have the name of the");
     System.err.println("CID found in <infile> for each SMILES.");
     System.err.println("");
     System.err.println("");
    }
  }
}
