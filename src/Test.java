
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class Test {
	
	public static void main (String[] args) throws FileNotFoundException{
//		Job[] jobs     = Job.read(new File("j1201_5.sm"));//best makespan=112
//		Resource[] res = Resource.read(new File("j1201_5.sm"));
//		Job[] jobs     = Job.read(new File("j12046_8.sm"));
//		Resource[] res = Resource.read(new File("j12046_8.sm"));
		Job[] jobs     = Job.read(new File("j12.sm"));
		Resource[] res = Resource.read(new File("j12.sm"));
		
		Arrays.stream(jobs).forEach( job -> job.calculatePredecessors(jobs));
		
		printPretty(jobs);
		printPretty(res);
		
		Schedule s = new Schedule();
		s.initializeJobList(jobs);
		
		System.out.println("\njobList: " + Arrays.toString(s.jobList) 
						+  "\nschedule: " + Arrays.toString(s.schedule));
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
