

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class Test {
	
	public static void main (String[] args) throws FileNotFoundException{
//		Job[] jobs     = Job.read(new File("j1201_5.sm"));//best makespan=112
//		Resource[] res = Resource.read(new File("j1201_5.sm"));
//		Job[] jobs     = Job.read(new File("j12046_8.sm"));
//		Resource[] res = Resource.read(new File("j12046_8.sm"));
		Job[] jobs     = Job.read(new File("j12.sm"));
		Resource[] res = Resource.read(new File("j12.sm"));
		
		for(int i = 0; i < jobs.length; i++){
			jobs[i].calculatePredecessors(jobs);
		}
		
		auslesen(jobs);
		auslesen(res);
		
		Schedule s = new Schedule();
		
		s.initializeJobList(jobs);
		System.out.println("\njobListe: " + Arrays.toString(s.jobListe));
	}
	
	
	
	
	
	
	
	private static void auslesen(Job[] jobs) {
		int gesamtDauer = 0;
		for (int i = 0; i < jobs.length; i++){
			gesamtDauer += jobs[i].dauer();
			
			System.out.printf("Nummer: %3d | Nachfolger: %-15s | Vorgaenger: %-15s | Dauer: %2d | R1: %2d R2: %2d R3: %2d R4: %2d | %n",
					jobs[i].nummer(),
					jobs[i].nachfolger().toString(),
					jobs[i].vorgaenger().toString(),
					jobs[i].dauer(),
					jobs[i].verwendeteResource(0),
					jobs[i].verwendeteResource(1),
					jobs[i].verwendeteResource(2),
					jobs[i].verwendeteResource(3)
				);
		}
		System.out.println("T = " + gesamtDauer);
	}
	
	private static void auslesen(Resource[] resource) {
		for (int i = 0; i < resource.length; i++){
			System.out.print("Resource: " + resource[i].nummer()+"     |    ");
			System.out.println("Verfuegbarkeit: " + resource[i].maxVerfuegbarkeit());
		}
	}
	
	
}
