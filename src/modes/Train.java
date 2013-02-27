package modes;

import io.BasicTools;
import java.text.DecimalFormat;
import liblinear.WekaClassifier;
import liblinear.WekaLauncher;
import org.apache.commons.cli.CommandLine;

/**
 * 
 * @author Johannes Eichner
 * @version $Rev$
 * @since 1.0
 */
public class Train {

	private static boolean silent = false;

	private static String featureFile;
	private static String classResultsDir;
	private static String modelFileDir;
	
	private static int multiruns = 1;
	private static int folds = 4;
	private static boolean nestedCV = false;
	
	private static boolean multithreading = false;
	
	private static void parseArguments(CommandLine cmd) {
		
		if(cmd.hasOption("featureFile")) {
			featureFile = new String(cmd.getOptionValue("featureFile"));
		}
		
		if(cmd.hasOption("resultsFileDir")) {
			classResultsDir = new String(cmd.getOptionValue("resultsFileDir"));
		}
		
		if(cmd.hasOption("modelFileDir")) {
			modelFileDir = new String(cmd.getOptionValue("modelFileDir"));;
		}
		
		if(cmd.hasOption("multiruns")) {
			multiruns = new Integer(cmd.getOptionValue("multiruns"));
		}
		
		if(cmd.hasOption("folds")) {
			folds = new Integer(cmd.getOptionValue("folds"));
		}
		
		if(cmd.hasOption("nestedCV")) {
			nestedCV = true;
		} else {
			nestedCV = false;
		}
		
		if(cmd.hasOption("multiThreading")) {
			multithreading = true;
		}	
	}
	
	private static void compareClassifiers(String featureFile, String resultsDir, String modelFileDir, int numMultiruns, int numFolds, boolean nestedCV) {	
		
		WekaLauncher launcher = new WekaLauncher(featureFile, resultsDir, modelFileDir, true);
		launcher.setFolds(numFolds);
		launcher.setMultiruns(numMultiruns);
		launcher.setNestedCV(nestedCV);
		
		long start = System.currentTimeMillis();
		
		double[][] classResults = launcher.runWekaClassifier(multithreading);
			
		long end = System.currentTimeMillis();
				
		// determine best classifier
		double[] meanROC = BasicTools.getColMeans(classResults);
		int winIdx = BasicTools.getMaxIndex(meanROC);
		
		// write short report of classification
		if (!silent) {
			
			System.out.println("\n=======================");
			System.out.println("Classification results:");
			System.out.println("=======================");
			int paddingLength = 25;
			DecimalFormat df = new DecimalFormat();
			System.out.println(BasicTools.padRight("  Best classifier:", paddingLength) + WekaClassifier.ClassificationMethod.values()[winIdx].printName + "\n");
			System.out.println(BasicTools.padRight("  Classifier", paddingLength) + "ROC score");
			for (int i=0; i<meanROC.length; i++) {
				System.out.println("    " + BasicTools.padRight(WekaClassifier.ClassificationMethod.values()[i].printName + ":", paddingLength-2)  + df.format(meanROC[i]));
			}
			System.out.println("  \nRuntime of training: " + Math.round((end-start)/1000) + " sec.");
		}
	}
	
	public static void main(CommandLine cmd) {
	
		parseArguments(cmd);
		compareClassifiers(featureFile, classResultsDir, modelFileDir, multiruns, folds, nestedCV);
	}
} 
