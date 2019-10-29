import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Schedule {

	// Diese zwei Listen sind zusammen der fertige Schedule:
	int[] jobListe; // genotype
	int[] schedule; // phänotype (Startzeitpunkte fuer Jobs der JobListe)
	
	public void initializeJobList(Job[] jobs) {
		// jobs (mit Nummer als Key) in HashMap speichern (zur schnelleren Suche eines Jobs anhand seiner Nummer)
		HashMap<Integer, Job> jobsMap = new HashMap<Integer, Job>();
		for (Job job : jobs) {
			jobsMap.put(job.nummer, job);
		}
		
		ArrayList<Job> eligibleJobs = new ArrayList<Job>();	// planbare Jobs (als TreeSet, damit immer kuerzester Job an erster Stelle))
		jobListe = new int[jobs.length];
		
		// Mit Dummy-Job starten
		int count = 0;
		jobListe[count] = jobs[0].nummer();					// Dummy-Job ist immer der 0te Job der Liste
		count++;
		ArrayList<Integer> nachfolgerAkt = jobs[0].nachfolger();
		for (int i = 0; i < nachfolgerAkt.size(); i++) {	// Nachfolgerjobs des Dummys planbar machen
			eligibleJobs.add(jobsMap.get(nachfolgerAkt.get(i)));
		}
		
		// Hauptschleife
		while(count != jobs.length) { 					// solange noch ungeplante Jobs
			Job min = Collections.min(eligibleJobs);	// kuerzesten planbaren Job auswaehlen (KOZ-Regel)
			jobListe[count] = min.nummer;				// Job einplanen
			count++;										
			eligibleJobs.remove(min);
			
			nachfolgerAkt = min.nachfolger();			// Nachfolger des Jobs betrachten
			
			for(int i = 0; i < nachfolgerAkt.size(); i++) {
				Job aktuellerNachfolgerJob = jobsMap.get(nachfolgerAkt.get(i));
				ArrayList<Integer> vorgaengerAkt = aktuellerNachfolgerJob.vorgaenger;
				boolean alleVorgaenger = true;
				for (int j = 0; j < vorgaengerAkt.size(); j++) { 	// die Vorgaenger anschaun
					boolean found = false;
					for(int k = 0; k < jobListe.length; k++) { 		// ist dieser Vorgaenger schon eingeplant (also in der jobListe)?
						if(jobListe[k] == vorgaengerAkt.get(j)) {
							found = true;
							break;
						}
					}
					if(!found) {	// Sobald ein Vorgaenger NICHT in jobListe gefunden, verlasse die Schleife
						alleVorgaenger = false;
						break;
					}
				}
				if(alleVorgaenger) {
					eligibleJobs.add(jobsMap.get(nachfolgerAkt.get(i)));
				}
			}
		}
	}

}
