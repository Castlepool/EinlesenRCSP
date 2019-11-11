
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class Test {
	
	public static void main (String[] args) throws FileNotFoundException{
		makeSchedules("j12.sm"); 					// make schedule for our single test-dataset
//		makeSchedules("scheduling-datasets_j120");	// make schedule for all 600 j120-datasets
	}
	
	// method that makes and prints schedules from ".sm"-files
	// param can be a single ".sm"-file or a directory with ".sm"-files in it
	private static void makeSchedules(String directory) throws FileNotFoundException{
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
				long duration = System.nanoTime() - start;
				
				System.out.println("\njobList:  " + Arrays.toString(schedule.jobList) 
								+ "\nschedule: " + Arrays.toString(schedule.schedule)
								+ "\ninitializeJobList() and decodeJobList() took " + duration/1000 + " microseconds\n\n");
				
				start = System.nanoTime();
				schedule.shift();
				duration = System.nanoTime() - start;
				System.out.println("\njobList:  " + Arrays.toString(schedule.jobList) 
								+ "\nschedule: " + Arrays.toString(schedule.schedule)
								+ "\nshift() took " + duration/1000 + " microseconds\n\n");
			}
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
