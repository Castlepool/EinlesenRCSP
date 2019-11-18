
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Test {
	
	public static void main (String[] args) throws IOException{
		makeSchedules("j12.sm"); 					// make schedule for our single test-dataset
		makeSchedules("instances");	// make schedule for all 600 j120-datasets
	}
	
	// method that makes and prints schedules from ".sm"-files
	// param can be a single ".sm"-file or a directory with ".sm"-files in it
	private static void makeSchedules(String directory) throws IOException{
		Job[] jobs;
		Resource[] resources;
		Schedule schedule;
		int scheduleCount = 1;
		
		File dir = new File(directory);
		File[] directoryListing = null;
		if(dir.isDirectory()) {
			directoryListing = dir.listFiles();
		}else {
			directoryListing = new File[1];
			directoryListing[0] = dir;
		}

		if (directoryListing != null) {
			
			File solutionOverview = new File("allSolutions.sol");
			BufferedWriter overviewWriter = new BufferedWriter(new FileWriter(solutionOverview));
			overviewWriter.write(String.format("%-16s | %-10s | %-10s | %s", "Instance" , "Makespan", "microsec", "percentage_deviation_from_best_solution\n"));
			int newBestSolutionCount = 0;
			
			for (File childFile : directoryListing) {		// make schedule for each dataset-file in directory
				if(!childFile.getName().endsWith(".sm")) continue;
				
				jobs   		= Job.read(childFile);
				resources 	= Resource.read(childFile);
				
				for (Job job : jobs) job.calculatePredecessors(jobs);
				
				System.out.println("Dataset-File " + scheduleCount++ + " (" + childFile.getName() + "):\n");
				printPretty(jobs);
				printPretty(resources);
				
				schedule = new Schedule();
				long start = System.nanoTime();
				schedule.initializeJobList(jobs);
				schedule.decodeJobList(jobs, resources);
				long duration1 = System.nanoTime() - start;
				
				System.out.println("\njobList:  " + Arrays.toString(schedule.jobList) 
								+ "\nschedule: " + Arrays.toString(schedule.schedule)
								+ "\ninitializeJobList() and decodeJobList() took " + duration1/1000 + " microseconds\n\n");
				
			    // shift
				start = System.nanoTime();
				schedule.shift();
				long duration2 = System.nanoTime() - start;
				System.out.println("\njobList:  " + Arrays.toString(schedule.jobList) 
								+ "\nschedule: " + Arrays.toString(schedule.schedule)
								+ "\nshift() took " + duration2/1000 + " microseconds\n\n");
				
				// print solution to file
			    StringBuilder builder = new StringBuilder();
			    for(int i = 0; i < schedule.jobList.length; i++) {
			    	builder.append(schedule.jobList[i] + " " + schedule.schedule[i] + "\n");
			    }
			    File file = new File("solutions\\" + childFile.getName().replaceAll("sm", "sol"));
			    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			    writer.write(builder.toString().substring(0, builder.toString().length() - 1));
			    writer.close();

			    // percent of best solution of all time
			    int bestSoltionEver = -1;
			    final Scanner scanner = new Scanner(new File("bestSolutionsEver.sol"));
			    while (scanner.hasNextLine()) {
			       String line[] = scanner.nextLine().split(" ");
			       if(line[0].startsWith(childFile.getName())) { 
			           // a match!
			           bestSoltionEver  = Integer.valueOf(line[1]);
			           break;
			       }
			    }
			    int ourMakespan = schedule.schedule[schedule.schedule.length-1];
			    double percentage = (bestSoltionEver==-1) ? 0 : (100.0 / bestSoltionEver * (ourMakespan-bestSoltionEver));
			    
			    // add solution to overview-file
			    overviewWriter.write(String.format("%-16s | %-10d | %-10d | %.2f", childFile.getName(), ourMakespan, duration1+duration2, percentage));
			    if(percentage < 0) {
			    	newBestSolutionCount++;
			    	overviewWriter.write("  NEW BEST MAKESPAN!");
			    }
			    overviewWriter.write("\n");
			    overviewWriter.write("new best makespans found: " + newBestSolutionCount + "\n");
			}
			overviewWriter.close();
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
	
	
}
