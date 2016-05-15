// Alex Foster

import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
public class UserInterface{

static BufferedReader bufferRead = null;
static CalendarManagerInterface RemCalendarManager = null;
static String owner = "";

 public static void main(String argv[]) {

	// invoke with java UserInterface username
	if(argv.length < 1){
		System.out.println("Usage: java UserInterface username");
		System.exit(0);
	}	
	
	owner = argv[0];

	if(owner.equals("public") || owner.equals("group") || owner.equals("open")){
		System.out.println("Cannot use 'public', 'group', or 'open' as username.");
		return;
	}

	System.setSecurityManager(new RMISecurityManager());
	
	// Get a remote reference to the RMIExampleImpl class
	String strName = "rmi://localhost/CalendarService";
	System.out.println("Client: Looking up " + strName + "...");
	try {
		RemCalendarManager = (CalendarManagerInterface)Naming.lookup(strName);	// get the CalendarManager object			
	} catch (Exception e) {
		System.out.println("Client: Exception thrown looking up " + strName + ", \n"+e.toString());
		System.exit(1);
	} 

	// 2 way communication
	try{
		Alert remoteClient = new Alert();
		Naming.rebind("AlertService", remoteClient);
	} catch(Exception e){
		System.out.println("Failed to register client object!");
	}


	boolean userVerification = false;
	try{
		userVerification = RemCalendarManager.registerUser(owner);
	} catch(Exception e){
		System.out.println("Remote exception in user interface.");
		e.printStackTrace();
		return;
	}

	if(!userVerification){
		System.out.println("User already online. Exiting.");
		System.exit(0);
	}

	System.out.println("\nWelcome to the Calendar application, "+owner+"!");
	while(true){
		System.out.println("Available commands: ScheduleEvent, RetrieveEvents, DeleteEvent, ViewUsers, Help, Exit");
		// listen to input
		String userInput = "";
		try{
 			bufferRead = new BufferedReader(new InputStreamReader(System.in));
	    		userInput = bufferRead.readLine();								// read full command
			if(userInput.equals("ScheduleEvent")){  			// ***** SCHEDULE EVENT *****
				scheduleEvent();	
			}	
			else if(userInput.equals("RetrieveEvents")){			// ***** RETRIEVE EVENTS *****
				retrieveEvents();
			}
			else if(userInput.equals("DeleteEvent")){			// ***** DELETE EVENT *****
				deleteEvent();
			}
			else if(userInput.equals("ViewUsers")){				// ***** VIEW USERS *****
				viewUsers();
			}
			else if(userInput.equals("Help")){					// ***** HELP *****
				System.out.println("Welcome to the Calendar application!");
			}
			else if(userInput.equals("Exit")){
				System.out.println("Goodbye!");
				try{
					RemCalendarManager.logout(owner);
				} catch(Exception e){
					System.out.println("Remote exception in user interface.");
					e.printStackTrace();
					return;
				}
				System.exit(0);
			}
		} catch(IOException e){
			System.out.println("IO error!");
			break;
		}
	}		// end while
 }			// end main

 public static void scheduleEvent() throws IOException{
	ArrayList<String> userlist = new ArrayList<String>();
	
	System.out.println("Event Description: ");
	String createEventDescription = bufferRead.readLine();
	System.out.println("Event Type (Private, Public, Group, Open):");
	String createEventType = bufferRead.readLine();
	if(!createEventType.equals("Private") && !createEventType.equals("Public") && 
		!createEventType.equals("Group") && !createEventType.equals("Open")){
		System.out.println("Event type must be 'Private', 'Public', 'Group', 'Open'. You entered: "+createEventType);
		return;
	}
	System.out.println("Event Start (0-23): ");
	String createEventStart = bufferRead.readLine();
	int createEventStartInt = 0;
	try{
		createEventStartInt = Integer.parseInt(createEventStart);	
	} catch(NumberFormatException e){ 
		System.out.println("Not a number.");
		return;
	}
	if(createEventStartInt < 0 || createEventStartInt > 23){
		System.out.println("Must be between 0 and 23.");
		return;
	}
	System.out.println("Event End (1-24): ");
	String createEventEnd = bufferRead.readLine();
	int createEventEndInt = 0;
	try{
		createEventEndInt = Integer.parseInt(createEventEnd);
	} catch(NumberFormatException e){
		System.out.println("Not a number.");
		return;
	}		
	if(createEventEndInt < 1 || createEventEndInt > 24 || createEventEndInt <= createEventStartInt){
			System.out.println("Must be between 1 and 24 and be greater than start time.");
			return;
	}
	if(createEventType.equals("Group")){
		System.out.println("Assign users of group event. Enter 'Done' to finish list.");
		String next = bufferRead.readLine();
		while(!next.equals("Done")){
			userlist.add(next);
			next = bufferRead.readLine();
		}	
	}
	boolean scheduleSuccess = false;
	try{
		scheduleSuccess = RemCalendarManager.scheduleEvent(owner, userlist, createEventDescription, createEventType, createEventStartInt, createEventEndInt);
 	} catch(Exception e){
		System.out.println("Remote exception in user interface.");
		e.printStackTrace();
		return;
	}
	if(scheduleSuccess){
		System.out.println("Successfully scheduled event.");
	}
	else{
		System.out.println("Unable to schedule event. Scheduling conflict or missing open event.");
	}
 }

 public static void retrieveEvents() throws IOException{
	System.out.println("Whose events would you like to see?");
	String userRequested = bufferRead.readLine();
	System.out.println("Start range of events (0-23):");
	String startRangeString = bufferRead.readLine();
	int startRangeInt = 0;
	try{
		startRangeInt = Integer.parseInt(startRangeString);
	} catch(NumberFormatException e){
		System.out.println("Not a number.");
		return;
	}
	if(startRangeInt < 0 || startRangeInt > 23){
		System.out.println("Must be in range 0-23.");
		return;
	}
	System.out.println("End range of events (1-24):");
	String endRangeString = bufferRead.readLine();
	int endRangeInt = 0;
	try{
		endRangeInt = Integer.parseInt(endRangeString);
	} catch(NumberFormatException e){
		System.out.println("Not a number");
		return;
	}
	if(endRangeInt < 1 || endRangeInt > 24 || endRangeInt <= startRangeInt){
		System.out.println("Must be in range 1-24 and be greater than start range.");
		return;
	}
	String events = "";
	try{
		events = RemCalendarManager.retrieveEvents(owner, userRequested, startRangeInt, endRangeInt);
	} catch(Exception e){
		System.out.println("Remote exception in user interface");
		e.printStackTrace();
		return;
	}
	System.out.println(events);
 }
 
 public static void deleteEvent() throws IOException{
	boolean result = false;
	System.out.println("Event ID to delete: ");
	String eventIdString = bufferRead.readLine();
	int eventIdInt = 0;
	try{
		eventIdInt = Integer.parseInt(eventIdString);
	} catch(NumberFormatException e){
		System.out.println("Not a number.");
		return;
	}
	try{
		result = RemCalendarManager.deleteEvent(owner, eventIdInt);
 	} catch(Exception e){
		System.out.println("Remote exception in user interface.");
		e.printStackTrace();
		return;
	}
	if(result){
		System.out.println("Event successfully deleted.");
	}
	else{
		System.out.println("Event unsuccessfully deleted. Does not exist or invalid permissions.");
	}
 }

 public static void viewUsers() throws IOException{
	String users = "Users:\n";
	try{
		users += RemCalendarManager.viewUsers();
	} catch(Exception e){
		System.out.println("Remote exception in user interface.");
		return;
	}
	System.out.println(users);
 }
 
} 
