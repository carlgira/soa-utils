package report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Aggregator
{
  public static void main(String[] args) throws Exception
  {
    if (args.length < 2)
      throw new IllegalArgumentException("Need at least two parameters");
    
    File output = new File(args[0]);
    BufferedWriter bw = new BufferedWriter(new FileWriter(output));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm");
    bw.write("<test-report date=\"" + sdf.format(new Date()) + "\">\n");
    for (int i=1; i<args.length; i++)
    {
      File input = new File(args[i]);
      if (input.exists())
      {
        bw.write("  <test>\n");
        BufferedReader br = new BufferedReader(new FileReader(input));
        String line = "";
        boolean keepReading = true;
        while (keepReading)
        {
          line = br.readLine();
          if (line != null)
          {
            if (!line.startsWith("<?xml"))
              bw.write(line + "\n");
          }
          else
            keepReading = false;
        }
        br.close();
        bw.write("  </test>\n");
      }
      else
        throw new RuntimeException("File " + args[i] + " not found.");
    }
    bw.write("</test-report>\n");
    bw.close();
  }
}
