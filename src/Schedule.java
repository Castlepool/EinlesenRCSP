import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Schedule {

	// these two lists form the schedule:
	int[] jobList; // job-sequence
	int[] schedule; // starting-times of jobs in jobList
	
	HashMap<Integer, Job> jobMap;
	
	public void initializeJobList(Job[] jobs) {
		jobMap = new HashMap<Integer, Job>();						// jobs in HashMap for faster search by id
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
	
	public void decodeJobList(Job[] jobs, Resource[] res){
		//calculate the starting times of the jobs in the order of jobList
		schedule = new int[jobList.length];
		
		//calculate the maximum possible makespan "maxDuration" of the project (you could also get that from the dataset-file when reading)
		int maxDuration = 0;//alt shift R
		for(int i = 0; i < jobs.length; i++){
			maxDuration += jobs[i].duration;
		}
		
		// available resurces for each period
		int[][] resourcenTableau = new int[res.length][maxDuration];
		
		for(int i = 0; i < resourcenTableau.length; i++){
			for(int j = 0; j < resourcenTableau[i].length; j++){
				resourcenTableau[i][j] = res[i].maxAvailability;
			}
		}
		
		for(int i = 0; i < jobList.length; i++){
			
			int nr = jobList[i];
						
			Job j = Job.getJob(jobs, nr);
			
			int p1 = earliestPossibleStarttime(j, jobs);
			int p2 = starttime(j, p1, resourcenTableau);
			actualizeResources(j, resourcenTableau, p2);
			
			schedule[i] = p2;
		}
		
	}

	private int earliestPossibleStarttime(Job j, Job[] jobs) {
		
		// find max endTime of predecessors
		int maxPredecessorEndTime = 0;
		for (int predecessorID : j.getPredecessors()) {
			for (int i = 0; i < jobList.length; i++) {
				if(predecessorID == jobList[i]) {
					int endTime = schedule[i] + jobMap.get(predecessorID).duration;
					if( endTime > maxPredecessorEndTime) {
						maxPredecessorEndTime = endTime;
					}
					break;
				}
			}
		}
		
		// TODO: check available resource capacities
		
		
		return maxPredecessorEndTime;
	}
	
	private int starttime(Job j, int p1, int[][] resourcenTableau) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void actualizeResources(Job j, int[][] resourcenTableau, int p2) {
		// TODO Auto-generated method stub
		
	}

}
