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
	
	HashMap<Integer, Job> jobMap;	// HashMap to get job by id
	
	public void initializeJobList(Job[] jobs) {
		jobMap = new HashMap<Integer, Job>();		// jobs in HashMap for faster search by id
		Arrays.stream(jobs).forEach(job -> jobMap.put(job.getId(), job));

		List<Job> eligibleJobs = new ArrayList<Job>();
		jobList = new int[jobs.length];
		
		// start with dummy-job and make his successors eligible
		int count = 0;
		jobList[count++] = jobs[0].getId();					// dummy is always first job in list
		for (int successorID : jobs[0].getSuccessors())
			eligibleJobs.add(jobMap.get(successorID));
		
		// main-loop: add jobs to jobList in valid order (until no more left)
		while(count != jobs.length) {
			Job min = Collections.min(eligibleJobs);		// choose shortest job
			jobList[count++] = min.getId();					// put it in jobList							
			eligibleJobs.remove(min);
			
			// look at its successors and make them eligible if their predecessors are all planned already
			for (int successorID : min.getSuccessors()) {
				boolean allPredecessorsPlanned = true;
				for (int predecessorID : jobMap.get(successorID).getPredecessors()) {
					boolean found = false;
					for (int plannedJobID : jobList) {
						if(plannedJobID == predecessorID) {
							found = true;
							break;
						}
					}
					if(!found) {
						allPredecessorsPlanned = false;
						break;
					}
				}
				if(allPredecessorsPlanned) 
					eligibleJobs.add(jobMap.get(successorID));
			}
		}
	}
	
	public void decodeJobList(Job[] jobs, Resource[] res){
		schedule = new int[jobList.length];
		
		// calculate maximum possible makespan of project
		int maxDuration = 0;
		for(int i = 0; i < jobs.length; i++)
			maxDuration += jobs[i].duration;
		
		// available resurces for each period
		int[][] resourcenTableau = new int[res.length][maxDuration];
		
		for(int i = 0; i < resourcenTableau.length; i++){
			for(int j = 0; j < resourcenTableau[i].length; j++)
				resourcenTableau[i][j] = res[i].maxAvailability;
		}
		
		// calculate starting times (job after job, in order of jobList)
		for(int i = 0; i < jobList.length; i++){
			Job j = jobMap.get(jobList[i]);
			
			int p1 = earliestPossibleStarttime(j, jobs);
			int p2 = starttime(j, p1, resourcenTableau);
			actualizeResources(j, resourcenTableau, p2);
			
			schedule[i] = p2;
		}
		
	}

	private int earliestPossibleStarttime(Job j, Job[] jobs) {
		// find max endTime of predecessors (Zero if there are no predecessors)
		int maxPredecessorEndTime = 0;
		for (int predecessorID : j.getPredecessors()) {
			for (int i = 0; i < jobList.length; i++) {
				if(predecessorID == jobList[i]) {
					int endTime = schedule[i] + jobMap.get(predecessorID).duration;
					if( endTime > maxPredecessorEndTime)
						maxPredecessorEndTime = endTime;
					break;
				}
			}
		}
		return maxPredecessorEndTime;
	}
	
	private int starttime(Job j, int p1, int[][] resourcenTableau) {
		// TODO: check available resource capacities and adjust starttime accordingly
		return 0;
	}
	
	private void actualizeResources(Job j, int[][] resourcenTableau, int p2) {
		// TODO: subtract used resources for given job (j) at given start-time (p2)
		
	}

}
