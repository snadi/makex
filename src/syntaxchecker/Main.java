/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package syntaxchecker;

import java.io.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import syntaxchecker.syntaxerrors.FileMissingInMakeError;
import syntaxchecker.syntaxerrors.KConfigMissingError;
import syntaxchecker.syntaxerrors.ObjectMissingError;

/**
 *
 * @author snadi
 */
public class Main {
    public static int dirCount =0;


    public static void checkDirectories(String dirPath, Vector<String> configEntries, PrintWriter outputPrinter, int level) {

        //do not analyze the staging directory to avoid skewing results, and to be consistent with later CSMR'12
        //and JSEP work
        if(dirPath.contains("staging"))
            return;

        dirCount++;
        try {

            //first check files in this directory
            SyntaxChecker syntaxChecker = new SyntaxChecker(dirPath, configEntries);
            syntaxChecker.validateDirectory();            
            syntaxChecker.printErrors(outputPrinter);
            

            FileFilter directoryFilter = new FileFilter() {

                public boolean accept(File pathname) {
                    return pathname.isDirectory() && !pathname.getName().startsWith(".");
                }
            };

            File parentDirectory = new File(dirPath);

            File[] directories = parentDirectory.listFiles(directoryFilter);
            if (directories == null) {
                return;
            }
            for (File dir : directories) {                
                //then check files in each sub directory
                checkDirectories(dir.getPath() + "/", configEntries, outputPrinter,level + 1);
            }


        } catch (Exception ex) {
            Logger.getLogger(SyntaxChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Vector<String> fillConfigEntries() {

        Vector<String> configEntries = new Vector<String>();
        BufferedReader inputReader = null;
        BufferedReader kconfigReader = null;

        try {
            File file = new File("archNames.txt");
         //   System.out.println("Reading from: " +file.getAbsolutePath());
            inputReader = new BufferedReader(new FileReader(file));

            while (inputReader.ready()) {
                String arch = inputReader.readLine().trim();

                kconfigReader = new BufferedReader(new FileReader("./KConfigFiles/" + arch + "-kconfig.txt"));


                while (kconfigReader.ready()) {
                    String line = kconfigReader.readLine();



                    if (Pattern.matches("config\\s+.*", line.trim()) || line.trim().startsWith("menuconfig")) {
                        String parts[] = line.trim().split("\\s");

                        int count = 1;
                        while (count < parts.length) {

                            if (parts[count].trim().length() > 0) {
                                break;
                            }

                            count++;
                        }

                        String configName = parts[count].trim();

                        if (!configEntries.contains(configName)) {
                            configEntries.add(configName);
                        }
                    }
                }

                kconfigReader.close();

            }


            System.out.println("Returned "+ configEntries.size());
            //parse the common directories first
            return configEntries;
        } catch (IOException ex) {
            Logger.getLogger(SyntaxChecker.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                inputReader.close();

            } catch (IOException ex) {
                Logger.getLogger(SyntaxChecker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private static Vector<String> getDefinedFeatures() throws Exception{

        Vector<String> definedFeatures = new Vector<String>();
        BufferedReader definedFeatureReader = new BufferedReader(new FileReader("definedFeatures"));
        while (definedFeatureReader.ready()) {
            definedFeatures.add(definedFeatureReader.readLine().trim());
        }
       return definedFeatures;
    }

    public static void main(String args[]) {
  long startTime = System.currentTimeMillis();


        try {
            PrintWriter outputPrinter = new PrintWriter(new File("anomalies"));
            Vector<String> configEntries = getDefinedFeatures();

            if (args.length > 0) {
                //check specific directory
                checkDirectories(args[0], configEntries, outputPrinter, 1);

            } else {
                checkDirectories("./", configEntries,outputPrinter, 1);
            }

//            System.out.println("INTERESTING");
//            System.out.println();
            System.out.println("Dodged: " + SyntaxChecker.dodgedCount);
//Collections.sort(SyntaxChecker.interestingIncludes);
//            for(String message : SyntaxChecker.interestingIncludes){
//                System.out.println(message);
//            }

            System.out.println("STATISTICS");
            System.out.println("checked: " + SyntaxChecker.filesChecked + " files ");
            System.out.println("------------");
            System.out.println(FileMissingInMakeError.getCounter() + " files missing in Makefiles");
            System.out.println(KConfigMissingError.getCounter() + " configs missing from Kconfig fiels");
            System.out.println(ObjectMissingError.getCounter() + " objects missing in Makefiles");
System.out.println("Saved : " + SyntaxChecker.numOfRemovedFiles + " files");
        //    System.out.println("in another directory: " + SyntaxChecker.inAnotherDirectory);
          //  System.out.println("Avg number of times a file is included: " + SyntaxChecker.sumOfSize/SyntaxChecker.numOfIncludedFiles);
System.out.println(SyntaxChecker.directoryWithNoMakeFile + " directories with no make files");
System.out.println(SyntaxChecker.noKbuildEither + " directories do not have kbuild either");

            System.out.println("directories: " + dirCount);
            System.out.println("Files checked: " + SyntaxChecker.filesChecked);
            System.out.println("Total anomalies: "+ SyntaxChecker.getCount());
            outputPrinter.close();
        } catch (Exception ex) {
            Logger.getLogger(SyntaxChecker.class.getName()).log(Level.SEVERE, null, ex);
        }

    long endTime = System.currentTimeMillis();
    System.out.println("Total time: "+ (endTime - startTime));
    }
}
