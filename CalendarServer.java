// Alex Foster

import java.rmi.*;
import java.sql.Timestamp;
import java.util.Date;
import java.lang.*;
public class CalendarServer{
 public static void main(String argv[]) {
	try {
		System.setSecurityManager(new RMISecurityManager());

		System.out.println("Server: Registering Calendar Service");
		CalendarManager remote = new CalendarManager();				
		Naming.rebind("CalendarService", remote);
		System.out.println("Server: Ready...");
	
		// Get a remote reference to the RMIExampleImpl class
		AlertInterface RemAlertInterface = null;
		String strName = "rmi://localhost/AlertService";
		System.out.println("Server: Looking up " + strName + "...");
		while(RemAlertInterface == null){
			try {
				RemAlertInterface = (AlertInterface)Naming.lookup(strName);     // loop until we have a client object          
			} catch (Exception e) {
				System.out.println("Still waiting for client object...");
				Thread.sleep(15000);
			}
		}
		System.out.println("AlertService ready.");

		while(true){
			String s = remote.checkEvents();
			if(!s.isEmpty()){
				System.out.println(s);
				RemAlertInterface.printAlert(s);
			}
			Thread.sleep(10000);
		}
 	}
 	catch (Exception e) {
		System.out.println("Server: Failed to register Calendar Service: " + e);
 	}
 }
} 
