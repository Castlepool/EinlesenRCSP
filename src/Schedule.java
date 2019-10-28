import java.util.ArrayList;

public class Schedule {

	// Diese zwei Listen sind zusammen der fertige Schedule:
	int[] jobListe; // genotype
	int[] schedule; // phänotype (Startzeitpunkte fuer Jobs der JobListe)
	
	public void initializeJobList(Job[] jobs) {
		ArrayList<Job> eligibleJobs = new ArrayList<Job>(); // planbare Jobs
		jobListe = new int[jobs.length];
		
		// 1. Job to jobListe
		int count = 0;
		jobListe[count] = jobs[0].nummer(); // Dummy-Job ist immer der 0te Job der Liste
		count++;
		ArrayList<Integer> nachfolgerAkt = jobs[0].nachfolger();
		for (int i = 0; i < nachfolgerAkt.size(); i++) {
			eligibleJobs.add(Job.getJob(jobs, nachfolgerAkt.get(i)));
		}
		
		while(count != jobs.length) { // solange noch ungeplante Jobs
			Job min = eligibleJobs.get(0); // besser: kuerzesten Job auswaehlen (KOZ)
			
			jobListe[count] = min.nummer;
			
			count++;
			eligibleJobs.remove(min);
			nachfolgerAkt = min.nachfolger();
			for(int i = 0; i < nachfolgerAkt.size(); i++) {
				Job aktuellerNachfolgerJob = Job.getJob(jobs, nachfolgerAkt.get(i));
				ArrayList<Integer> vorgaengerAkt = aktuellerNachfolgerJob.vorgaenger;
				boolean alleVorgaenger = true;
				for (int j = 0; j < vorgaengerAkt.size(); j++) { // jeden Vorgaenger anschaun
					boolean found = false;
					for(int k = 0; k < jobListe.length; k++) { // ist dieser Vorgaenger schon eingeplant?
						if(jobListe[k] == vorgaengerAkt.get(j)) {
							found = true;
							break;
						}
					}
					if(!found) {
						alleVorgaenger = false;
						break;
					}
				}
				if(alleVorgaenger) {
					eligibleJobs.add(Job.getJob(jobs, nachfolgerAkt.get(i)));
				}
			}
		}
	}

}
