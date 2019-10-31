/*
 * Einlese-Programm wurde von Studierenden der HFT Stuttgart entwickelt 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Job implements Comparable<Job>{

	int id;
	
	// successors; each element contains the job-id (int)
	ArrayList<Integer> successors;
	
	// predecessors; each element contains the job-id (int)
	ArrayList<Integer> predecessors;
	
	int duration;
	
	// needed resource capacities  
	// requiredResourceCapacities[0] --> capacities of resource R1
	// requiredResourceCapacities[1] --> capacities of resource R2
	// requiredResourceCapacities[2] --> capacities of resource R3
	// requiredResourceCapacities[3] --> capacities of resource R4
	int[] requiredResourceCapacities;
	

	public Job(int id, ArrayList<Integer> successors, int duration, int[] usedResources){
		this.id = id;
		this.successors = successors;
		this.duration = duration;
		this.requiredResourceCapacities = usedResources;
		this.predecessors = new ArrayList<Integer>();
	}
	
	public int getId(){
		return id;
	}
	
	public ArrayList<Integer> getSuccessors(){
		return successors;
	}
	public ArrayList<Integer> getPredecessors(){
		return predecessors;
	}
	public int getDuration(){
		return duration;
	}
	
	public int requiredResourceCapacity(int resourceId){
		if(resourceId >= 0 && resourceId <= 3)
			return requiredResourceCapacities[resourceId];
		else
			throw new IllegalArgumentException("Parameter muss zwischen 0 und 3 sein!");
	}
	
	public int numberOfSuccessors(){
		return successors.size();
	}
	
	public static Job getJob(Job[] jobs, int id){
		for(int i = 0; i < jobs.length; i++){
			if (id == jobs[i].id)
			{
				return jobs[i];
			}
		}
		return null;
	}
	
	public void calculatePredecessors(Job[] jobs){
		Arrays.stream(jobs).forEach( job -> {
			job.getSuccessors().stream().forEach( successorJobId -> {
				if(successorJobId == this.id) {
					predecessors.add(job.getId());
				}
			});
		});
	}
	
	public static Job[] read(File file) throws FileNotFoundException {
		
		Scanner scanner = new Scanner(file);
		Job[] jobs = new Job[0];
		int index = 0;
		
		ArrayList<ArrayList<Integer>> successors = new ArrayList<ArrayList<Integer>>();	
		boolean startJob = false;
		
		while(scanner.hasNext()) {
			String nextLine = scanner.nextLine();
			if(nextLine.equals("")){
				continue;
			}
			Scanner lineScanner = new Scanner(nextLine);
			String nextString = lineScanner.next();
			
			if (nextString.equals("jobs")) {
				boolean found = false;
				while(!found){
					if(lineScanner.next().equals("):")){
						int length = lineScanner.nextInt();
						jobs = new Job[length];
						for(int i = 0; i < jobs.length;i++){
							successors.add(new ArrayList<Integer>());
						}
						found = true;
					}
				}
				continue;
			} 	
			if(nextString.equals("jobnr.")){
				startJob = true;
			}
			if(startJob){
				try {
					lineScanner.next();
					if (lineScanner.hasNext()) {
						lineScanner.next();
						while (lineScanner.hasNext()) {
							int suc = Integer.valueOf(lineScanner.next());
							successors.get(index).add(suc);								
						}	
						index++;
						if(index == jobs.length){
							break;
						}
					}	
				} catch (NumberFormatException e) {}	
			}	
				
		}	
		index = 0;
		boolean startRequests = false;
		scanner = new Scanner(file);
		while(scanner.hasNext()) {
			String next = scanner.nextLine();
			
			if(next.equals("")){
				continue;
			}
			Scanner lineScanner = new Scanner(next);
			String nextString = lineScanner.next();
			if(!startRequests && lineScanner.hasNext()){
				if(lineScanner.next().equals("mode")){
					startRequests = true;
				}
			}
			if(startRequests){
				try {
					int nummer = Integer.valueOf(nextString);
					nextString = lineScanner.next();
					int[] res = new int[4];
					if (lineScanner.hasNext()) {
						int duration = Integer.valueOf(lineScanner.next());
						if (lineScanner.hasNext()) {
							nextString = lineScanner.next();
							res[0] = Integer.valueOf(nextString);
							if (lineScanner.hasNext()) {
								nextString = lineScanner.next();
								res[1] = Integer.valueOf(nextString);
								if (lineScanner.hasNext()) {
									nextString = lineScanner.next();
									res[2] = Integer.valueOf(nextString);
									if (lineScanner.hasNext()) {
										nextString = lineScanner.next();
										res[3] = Integer.valueOf(nextString);
									}
								}
							}
							
						}
						
						jobs[index] = new Job(nummer, successors.get(index),duration, res);
						index++;
						if(index == jobs.length){
							break;
						}
					}	
					
				} catch (NumberFormatException e) {}	
			}	
				
		}	
		return jobs;
	}

	@Override
	public int compareTo(Job other) {
		return this.duration - other.duration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Job other = (Job) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
}