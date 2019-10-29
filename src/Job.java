/*
 * Einlese-Programm wurde von Studierende der HFT Stuttgart entwickelt 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Job implements Comparable<Job>{

	// Number of a job
	int nummer;
	
	// successors; each element contains the job-number (int)
	ArrayList<Integer> nachfolger;
	
	// predecessors; each element contains the job-number (int)
	ArrayList<Integer> vorgaenger;
	
	// duration of a job
	int dauer;
	
	// needed resource capacities  
	// verwendeteResourcen[0] --> capacities of resource R1
	// verwendeteResourcen[1] --> capacities of resource R2
	// verwendeteResourcen[2] --> capacities of resource R3
	// verwendeteResourcen[3] --> capacities of resource R4
	int[] verwendeteResourcen;
	

	public Job(int nummer, ArrayList<Integer> nachfolger, int dauer, int[] verwendeteResourcen){
		this.nummer = nummer;
		this.nachfolger = nachfolger;
		this.dauer = dauer;
		this.verwendeteResourcen = verwendeteResourcen;
		this.vorgaenger = new ArrayList<Integer>();
	}
	
	public int nummer(){
		return nummer;
	}
	
	public ArrayList<Integer> nachfolger(){
		return nachfolger;
	}
	public ArrayList<Integer> vorgaenger(){
		return vorgaenger;
	}
	public int dauer(){
		return dauer;
	}
	
	public int verwendeteResource(int i){
		if(i >= 0 && i <= 3)
			return verwendeteResourcen[i];
		else
			throw new IllegalArgumentException("Parameter muss zwischen 0 und 3 sein!");
	}
	
	public int anzahlNachfolger(){
		return nachfolger.size();
	}
	
	public static Job getJob(Job[] jobs, int nummer){
		for(int i = 0; i < jobs.length; i++){
			if (nummer == jobs[i].nummer)
			{
				return jobs[i];
			}
		}
		return null;
	}
	public void calculatePredecessors(Job[] l){
		for(int i = 0; i < l.length; i++) {
			
			ArrayList<Integer> iNachfolger = l[i].nachfolger();
			
			for(int j = 0; j < iNachfolger.size(); j++) {
				if(iNachfolger.get(j) == this.nummer()) {
					vorgaenger.add(l[i].nummer());
				}
			}
		}
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
		return this.dauer - other.dauer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nummer;
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
		if (nummer != other.nummer)
			return false;
		return true;
	}
	
	
}