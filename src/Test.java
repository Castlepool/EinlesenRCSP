
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Test {
	
	public static void main (String[] args) throws IOException{
		int iterationCount = 0;
//		makeSchedules("j12.sm"); 					// make schedule for our single test-dataset
		while(iterationCount < 2) {
			iterationCount++;
			System.out.println("\nITERATION " + iterationCount + ":\n");
			makeSchedules("instances");					// make schedule for all 600 j120-datasets
		}
		
		System.out.println("\nIterations made: " + iterationCount);
	}
	
	// method that makes and prints schedules from ".sm"-files
	// param can be a single ".sm"-file or a directory with ".sm"-files in it
	private static void makeSchedules(String directory) throws IOException{
		Job[] jobs;
		Resource[] resources;
		Schedule schedule;
		
		File dir = new File(directory);
		File[] directoryListing = null;
		if(dir.isDirectory()) {
			directoryListing = dir.listFiles();
		}else {
			directoryListing = new File[1];
			directoryListing[0] = dir;
		}

		if (directoryListing != null) {
			
			// generate File for makespan-overview
			File solutionOverview = new File("ourOverview.txt");
			BufferedWriter overviewWriter = new BufferedWriter(new FileWriter(solutionOverview));
			overviewWriter.write(String.format("%-16s | %-12s | %-10s | %s", "instance" , "our makespan", "microsec", "deviation from given makespans\n\n"));
			
			// best solutions so far
			String ourBestSolutionsString = new String(Files.readAllBytes(Paths.get("ourBestMakespans.sol")));
			String[] bestSolutionLines = ourBestSolutionsString.split("\r?\n|\r");
			
			int scheduleCount = 0; 					// count solved problems
			int betterSolutionCount = 0;			// count how many times our makespan was shorter than provided solutions
			double deviationsSum = 0.0;				// add up all deviations
			long calculationTimesSum = 0;			// add up all calculation-times
			
			System.out.println("calculating schedules, please wait ... \n\n");
			
			for (File childFile : directoryListing) {		// make schedule for each dataset-file in directory
				if(!childFile.getName().endsWith(".sm")) continue;
				scheduleCount++;
				
				jobs   		= Job.read(childFile);
				resources 	= Resource.read(childFile);
				
				for (Job job : jobs) job.calculatePredecessors(jobs);
				
//				System.out.println("Dataset-File " + scheduleCount + " (" + childFile.getName() + "):\n");
//				printPretty(jobs);
//				printPretty(resources);
				
				// calculate schedule
				schedule = new Schedule();
				long start = System.nanoTime();
				schedule.initializeJobList(jobs);
				schedule.decodeJobList(jobs, resources);
				long duration1 = System.nanoTime() - start;
				
			    // shift
				start = System.nanoTime();
				schedule.shift();
				long duration2 = System.nanoTime() - start;
//				System.out.println("\njobList:  " + Arrays.toString(schedule.jobList) 
//								+ "\nschedule: " + Arrays.toString(schedule.schedule)
//								+ "\ninitializeJobList() and decodeJobList() took " + duration1/1000 + " microseconds"
//								+ "\nshift() took " + duration2/1000 + " microseconds\n\n");
				
				generateSolutionFile(schedule, childFile.getName());

			    // percentage of deviation from provided makespan-list
			    int providedMakespan = findMakespanInFile(childFile.getName(), "providedMakespans.sol");
			    int ourSoFarBestMakespan = findMakespanInFile(childFile.getName(), "ourBestMakespans.sol");
			    int ourNewMakeSpan = schedule.schedule[schedule.schedule.length-1];
			    int ourMakespan = ourNewMakeSpan < ourSoFarBestMakespan ? ourNewMakeSpan : ourSoFarBestMakespan;
			    double percentage = (providedMakespan == -1) ? 0 : (100.0 / providedMakespan * (ourMakespan-providedMakespan));
			    
			    // add solution to overview-file
			    overviewWriter.write(String.format("%-16s | %-12d | %-10d | %.2f", childFile.getName(), ourMakespan, duration1+duration2, percentage));
			    // check if makespan shorter than in provided list
			    if(percentage < 0) {
			    	betterSolutionCount++;
			    	overviewWriter.write("  New Best Makespan!");
			    }
			    overviewWriter.write("\n");
			    
			    // remember makespan if new best one
			    if(ourMakespan < providedMakespan) {
			    	for(int i = 0; i < bestSolutionLines.length; i++) {
			    		if(bestSolutionLines[i].startsWith(childFile.getName())) {
			    			bestSolutionLines[i] = childFile.getName() + " " + ourMakespan;
			    		}
			    	}
			    }
			    
			    // remember some stuff for later
				deviationsSum += percentage;	
				calculationTimesSum += duration1/1000 + duration2/1000;
			    
			}
			overviewWriter.write("\n" + compareOurSolutionsToProvided());
			overviewWriter.write("average calculation time (shift included): " + calculationTimesSum/scheduleCount + " microseconds");
			overviewWriter.close();
			
			// generate File for our best solutions so far
			ourBestSolutionsString = String.join("\n", bestSolutionLines);
			File ourBestSolutions = new File("ourBestMakespans.sol");
			BufferedWriter ourSolutionsWriter = new BufferedWriter(new FileWriter(ourBestSolutions));
			ourSolutionsWriter.write(ourBestSolutionsString);
			ourSolutionsWriter.close();
			
			// print makespans
			Scanner input = new Scanner(new File("ourOverview.txt"));
			while (input.hasNextLine()) System.out.println(input.nextLine());
			
			System.out.println("\nAll " + scheduleCount + " schedules calculated.\nSolutions can be found in folder \"solutions\", "
					+ "Overview of our Makespans in file \"ourOverview.txt\""
					+ "\nThank's for your patience :)");
		}
	}

	private static void printPretty(Job[] jobs) {
		int totalDuration = 0;
		for (int i = 0; i < jobs.length; i++){
			totalDuration += jobs[i].getDuration();
			
			System.out.printf("id: %3d | successors: %-15s | predecessors: %-15s | duration: %2d | R1: %2d R2: %2d R3: %2d R4: %2d | %n",
					jobs[i].getId(),
					jobs[i].getSuccessors().toString(),
					jobs[i].getPredecessors().toString(),
					jobs[i].getDuration(),
					jobs[i].requiredResourceCapacity(0),
					jobs[i].requiredResourceCapacity(1),
					jobs[i].requiredResourceCapacity(2),
					jobs[i].requiredResourceCapacity(3)
				);
		}
		System.out.println("T = " + totalDuration);
	}
	
	private static void printPretty(Resource[] resource) {
		for (int i = 0; i < resource.length; i++){
			System.out.print("resourceId: " + resource[i].getId()+"     |    ");
			System.out.println("availability: " + resource[i].getMaxAvailability());
		}
	}
	
	private static void generateSolutionFile(Schedule schedule, String fileName) throws IOException {
		// print solution to file
	    StringBuilder builder = new StringBuilder();
	    for(int i = 0; i < schedule.jobList.length; i++) {
	    	builder.append(schedule.jobList[i] + " " + schedule.schedule[i] + "\n");
	    }
	    File file = new File("solutions\\" + fileName.replaceAll("sm", "sol"));
	    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	    writer.write(builder.toString().substring(0, builder.toString().length() - 1));
	    writer.close();
	}
	
	private static int findMakespanInFile(String instanceName, String filename) throws FileNotFoundException {
	    Scanner scanner = new Scanner(new File(filename));
	    while (scanner.hasNextLine()) {
	       String line[] = scanner.nextLine().split(" ");
	       if(line[0].startsWith(instanceName)) { 
	           // a match!
	           return Integer.valueOf(line[1]);
	       }
	    }
	    return -1;
	}
	
	private static String compareOurSolutionsToProvided() throws IOException {
		HashMap<String, Integer> ourBestSolutionsMap = createMap("ourBestMakespans.sol");
		HashMap<String, Integer> providedBestSolutionsMap = createMap("providedMakespans.sol");
		
		int numerOfComparisons = 0;
		int sameMakespanCounter = 0;
		int betterMakespanCounter = 0;
		double deviationsSum = 0.0;				// add up all deviations
		
		for (String key : ourBestSolutionsMap.keySet()) {
			if(providedBestSolutionsMap.containsKey(key)) {
				numerOfComparisons++;
				int deviation = ourBestSolutionsMap.get(key)-providedBestSolutionsMap.get(key);
				if(deviation == 0) sameMakespanCounter++;
				else if(deviation < 0) betterMakespanCounter++;
				double percentage = 100.0 / providedBestSolutionsMap.get(key) * (ourBestSolutionsMap.get(key)-providedBestSolutionsMap.get(key));
				deviationsSum += percentage;
			}
		}
		
		String comparisonResults = "comparedInstances: " + numerOfComparisons 
				+ "\nsame Makespans: " + sameMakespanCounter
				+ "\nbetter Makespans: " + betterMakespanCounter
				+ String.format("\naverage deviation from given solutions: %.2f percent\n", deviationsSum/numerOfComparisons);
		
		return comparisonResults;
	}
	
	private static HashMap<String, Integer> createMap(String filename) throws IOException {
		String solutionsString = new String(Files.readAllBytes(Paths.get(filename)));
		String[] solutionLines = solutionsString.split("\r?\n|\r");
		HashMap<String, Integer> ourBestSolutionMap = new HashMap<String, Integer>();
		for (String line : solutionLines) {
			if(line.startsWith("j")) {
				ourBestSolutionMap.put(line.split(" ")[0], Integer.parseInt(line.split(" ")[1]));
			}

		}
		return ourBestSolutionMap;
	}
	
}
