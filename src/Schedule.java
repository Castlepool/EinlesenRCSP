import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Schedule {

	// these two list form the schedule:
	int[] jobList; // job-sequence
	int[] schedule; // starting-times of jobs in jobList
	
	public void initializeJobList(Job[] jobs) {
		HashMap<Integer, Job> jobMap = new HashMap<Integer, Job>();						// jobs in HashMap for faster search by id
		Arrays.stream(jobs).forEach(job -> jobMap.put(job.getId(), job));

		List<Job> eligibleJobs = Collections.synchronizedList(new ArrayList<Job>());	// synchronized list for safe parallel processing
		jobList = new int[jobs.length];
		
		// start with dummy-job and make his successors eligible
		int count = 0;
		jobList[count++] = jobs[0].getId();					// dummy is always first job in list
		jobs[0].getSuccessors().parallelStream().forEach( successorId -> eligibleJobs.add(jobMap.get(successorId)));
		
		// main-loop: add jobs to jobList in valid order (until no more left)
		while(count != jobs.length) {
			Job min = Collections.min(eligibleJobs);		// choose shortest job
			jobList[count++] = min.getId();					// put it in jobList							
			eligibleJobs.remove(min);
			
			// look at its successors and make them eligible if their predecessors are all planned already
			min.getSuccessors().parallelStream().forEach( successorId -> {
				boolean allPredecessorsPlanned = jobMap.get(successorId).getPredecessors().parallelStream().allMatch( predecessorId -> {
					return Arrays.stream(jobList).anyMatch( jobListId -> jobListId == predecessorId);
				});
				if(allPredecessorsPlanned) 
					eligibleJobs.add(jobMap.get(successorId));
			});
		}
	}
}
