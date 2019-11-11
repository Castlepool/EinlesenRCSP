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
	
	int[][] resourceTableau;
	
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
		resourceTableau = new int[res.length][maxDuration];
		
		for(int i = 0; i < resourceTableau.length; i++){
			for(int j = 0; j < resourceTableau[i].length; j++)
				resourceTableau[i][j] = res[i].maxAvailability;
		}
		
		// calculate starting times (job after job, in order of jobList)
		for(int i = 0; i < jobList.length; i++){
			Job j = jobMap.get(jobList[i]);
			
			int p1 = earliestPossibleStarttime(j, jobs);
			int p2 = starttime(j, p1);
			actualizeResources(j, p2);
			
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
	
	private int starttime(Job job, int p1) {
		// start at period p1 and check ressource-availability for whole job-duration
		// if not enough of a resource in period i, set p1 to i+1
		for(int i = p1; i < p1 + job.getDuration(); i++){
			for(int j = 0; j < resourceTableau.length; j++) {
				if(resourceTableau[j][i] < job.requiredResourceCapacity(j)) {
					p1 = i + 1;
					break;
				}
			}
		}
		return p1;
	}
	
	private void actualizeResources(Job job, int p2) {
		// subtract used resources for given job (j) starting from its start-time (p2)
		int jobEnd = p2 + job.getDuration();
		for(int i = p2; i < jobEnd; i++) {
			for(int j = 0; j < resourceTableau.length; j++)
				resourceTableau[j][i] -= job.requiredResourceCapacity(j);
		}
	}
	
	public void shift() {
		// calculate shifted startTimes (going backwards through jobList)
		for(int i = (jobList.length-2); i >= 0; i--) {
			// get minimum startTime of successors
			int minSuccessorStartTime = schedule[schedule.length-1];
			for(int successorID : jobMap.get(jobList[i]).getSuccessors()) {
				for(int j = 0; j < jobList.length; j++) {
					if(jobList[j] == successorID) {
						if(schedule[j] < minSuccessorStartTime)
							minSuccessorStartTime = schedule[j];
						break;
					}
				}
			}
			// free resources for job i temporaily
			freeResources(jobMap.get(jobList[i]), schedule[i]);
			
			// find lastes possible start time for job i
			int startTimeOfJob = starttimeInShift(jobMap.get(jobList[i]), minSuccessorStartTime);
			
			// add resources according to new startTime (might be same as before)
			addResources(jobMap.get(jobList[i]), startTimeOfJob);
			
			schedule[i] = startTimeOfJob;
		}
		
		// re-sort jobList and schedule based on new startTimes in schedule
		sort(schedule, jobList, 0, schedule.length-1);
		// normalize schedule-times
		int earliestStartTime = schedule[0];
		for (int i = 0; i < schedule.length; i++) {
			schedule[i] -= earliestStartTime;
		}
		// make sure that dummy job (id = 1) is first again
		for (int i = 0; i < jobList.length; i++) {
			if(jobList[i] == 1) {
				jobList[i] = jobList[0];
				jobList[0] = 1;
				break;
			}
		}
	}
	
	private int starttimeInShift(Job job, int endTime) {
		// start at period p1 and check ressource-availability for whole job-duration
		// if not enough of a resource in period i, set p1 to i+1
		for(int i = endTime; i >= endTime - job.getDuration(); i--){
			for(int j = 0; j < resourceTableau.length; j++) {
				if(resourceTableau[j][i] < job.requiredResourceCapacity(j)) {
					endTime = i - 1;
					break;
				}
			}
		}
		return endTime - job.getDuration();
	}
	
	private void freeResources(Job job, int oldStartTime) {
		// add freed resources for given job (j) starting from its old start-time
		int oldJobEnd = oldStartTime + job.getDuration();
		for(int i = oldStartTime; i < oldJobEnd; i++) {
			for(int j = 0; j < resourceTableau.length; j++)
				resourceTableau[j][i] += job.requiredResourceCapacity(j);
		}
	}
	
	private void addResources(Job job, int newStartTime) {
		// subtract used resources for given job (j) starting from its new start-time
		int newJobEnd = newStartTime + job.getDuration();
		for(int i = newStartTime; i < newJobEnd; i++) {
			for(int j = 0; j < resourceTableau.length; j++)
				resourceTableau[j][i] -= job.requiredResourceCapacity(j);
		}
	}
	
	// sort arr and arr2 based on arr (swap-operations applied on both arrays)
	// partion-function used by quicksort
	 static int partition(int arr[], int arr2[], int low, int high) { 
	     int pivot = arr[high];  
	     int i = (low-1); // index of smaller element 
	     for (int j=low; j<high; j++) 
	     { 
	         // If current element is smaller than the pivot 
	         if (arr[j] < pivot) 
	         { 
	             i++; 
	
	             // swap arr[i] and arr[j] 
	             int temp = arr[i]; 
	             arr[i] = arr[j]; 
	             arr[j] = temp; 
	             
	             // ALSO swap arr2[i] and arr2[j]
	             temp = arr2[i]; 
	             arr2[i] = arr2[j]; 
	             arr2[j] = temp;
	         } 
	     } 
	
	     // swap arr[i+1] and arr[high] (or pivot) 
	     int temp = arr[i+1]; 
	     arr[i+1] = arr[high]; 
	     arr[high] = temp; 
	     
	     // ALSO swap arr2[i+1] and arr2[high] (or pivot) 
	     temp = arr2[i+1]; 
	     arr2[i+1] = arr2[high]; 
	     arr2[high] = temp; 
	
	     return i+1; 
	 } 
	// quickSort 
	 static void sort(int arr[], int arr2[], int low, int high) { 
	     if (low < high) 
	     { 
	         /* pi is partitioning index, arr[pi] is  
	           now at right place */
	         int pi = partition(arr, arr2, low, high); 
	
	         // Recursively sort elements before 
	         // partition and after partition 
	         sort(arr, arr2, low, pi-1); 
	         sort(arr, arr2, pi+1, high); 
	     } 
	 }
}
