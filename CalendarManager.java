// Alex Foster

import java.util.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.Date;

public class CalendarManager extends UnicastRemoteObject implements CalendarManagerInterface { 

 public Hashtable<String, Calendar> calendarTable = new Hashtable<String, Calendar>();
 public int nextEventId = 1;

 public CalendarManager() throws RemoteException{
	super();
	Calendar publicCalendar = new Calendar("public");			// initialize our manager with the public, group, and open calendars
	this.calendarTable.put("public", publicCalendar);
	Calendar groupCalendar = new Calendar("group");
	this.calendarTable.put("group", groupCalendar);
	Calendar openCalendar = new Calendar("open");
	this.calendarTable.put("open", openCalendar);
 }

 public boolean registerUser(String user) throws RemoteException{
	Calendar userCalendar = calendarTable.get(user);
	if(userCalendar != null && userCalendar.getOnline()){
		return false;
	}
	else if(userCalendar != null && !userCalendar.getOnline()){
		userCalendar.setOnline(true);
		return true;
	}
	else if(userCalendar == null){
		userCalendar = new Calendar(user);
		userCalendar.setOnline(true);
		calendarTable.put(user, userCalendar);
		return true;
	}
	else{
		return false;
	}
 }

 public void logout(String user) throws RemoteException{
	Calendar userCalendar = calendarTable.get(user);
	if(userCalendar != null){
		userCalendar.setOnline(false);
	}
 }

 public String checkEvents() throws RemoteException{
	// loop over all events in all calendars
	// if event is about to occur, send a string with event description and owner
	java.util.Calendar rightNow = java.util.Calendar.getInstance();
	int hour = rightNow.get(java.util.Calendar.HOUR_OF_DAY);
	String alertString = "";
	for(String s : calendarTable.keySet()){
		for(int i : calendarTable.get(s).getEvents().keySet()){
			Event myEvent = calendarTable.get(s).getEvents().get(i);
			int eventStart = myEvent.getStartTime() - 1;
			if(eventStart < 0){
				eventStart += 24;
			}
			if(hour == eventStart){
				alertString += "Alert! Event "+myEvent.getEventDescription()+" starting within the hour!\n";
			}
		}
	}
	return alertString;
 }

 // Retrieve calendar and return concat string of Events 
 public String retrieveEvents(String requestingUser, String requestedUser, int start, int end) throws RemoteException{
	String returnString = "";
	Calendar myCalendar = null;
	Calendar publicCalendar = calendarTable.get("public");
	Calendar groupCalendar = calendarTable.get("group");
	Calendar openCalendar = calendarTable.get("open");
	
	if((myCalendar = calendarTable.get(requestedUser)) == null){							// if invalid user, return error message
		 //returnString += "No such user: "+requestedUser+"\n";
	}

	// by default, not readable by other users
	String privateEvents = "";																
	// by default, readable by all users
	String publicEvents = publicCalendar.eventsToString(requestingUser, publicCalendar.getEventsInRange(start, end));		
	// full-read to group members, partial read to others
	String groupEvents = groupCalendar.eventsToString(requestingUser, groupCalendar.getEventsInRange(start, end));		
	// by default, readable by all users
	String openEvents = openCalendar.eventsToString(requestingUser, openCalendar.getEventsInRange(start, end));			

	if(myCalendar != null && requestingUser.equals(requestedUser)){
		privateEvents = myCalendar.eventsToString(requestingUser, myCalendar.getEventsInRange(start, end));		// requesting own events 
	}

	returnString += (privateEvents+publicEvents+groupEvents+openEvents);
	if(returnString.isEmpty()){
		returnString = "No events for given user or range or no permission to view them.";
	}
	return returnString;
 }

 public boolean scheduleEvent(String requestingUser, ArrayList<String> requestedUsers, String eventDescription,
							String eventType, int start, int end) throws RemoteException{
	if(requestedUsers.contains("public") || requestedUsers.contains("group") || 
		requestedUsers.contains("open")){		// invalid users
		return false;
	}

	Event event = new Event(eventDescription, eventType, requestingUser, start, end);

	event.setEventId(nextEventId);
	nextEventId++;
	System.out.println("next event id is now: "+nextEventId);

	Calendar myCalendar = calendarTable.get(requestingUser);
	if(myCalendar == null){								// create user if DNE
		myCalendar = new Calendar(requestingUser);
		calendarTable.put(requestingUser, myCalendar);
	}

	if(event.getEventType().equals("Private")){				// add it to requestingUser's calendar only
		boolean success = myCalendar.addEvent(event);
		return success;
	}
	else if(event.getEventType().equals("Public")){
		Calendar publicCalendar = calendarTable.get("public");
		boolean success = publicCalendar.addEvent(event);
		return success;	
	}
	else if(event.getEventType().equals("Group")){
		Calendar openCalendar = calendarTable.get("open");
		for(String s : requestedUsers){					// add users to group event
			event.addUser(s);
		}
		boolean success = addGroupEvent(event);				// more complex: must remove appropriate open group
		if(success){									// if successful event add, ensure all users exist
			for(String s : event.getUsers()){
				if(!calendarTable.containsKey(s)){
					calendarTable.put(s, new Calendar(s));
				}
			}
		}
		return success;
	}
	else if(event.getEventType().equals("Open")){
		Calendar openCalendar = calendarTable.get("open");
		boolean success = openCalendar.addEvent(event);
		return success;
	}
	else{
		return false;
	}
 }

 public boolean deleteEvent(String requestingUser, int eventId) throws RemoteException{
	System.out.println("Attempting to delete event: "+eventId);
	Calendar myCalendar = null;
	Calendar publicCalendar = calendarTable.get("public");
	Calendar groupCalendar = calendarTable.get("group");
	Calendar openCalendar = calendarTable.get("open");

	if((myCalendar = calendarTable.get(requestingUser)) == null){
		return false;
	}

	boolean successfulDeletion = false;

	if(myCalendar.getEvents().containsKey(eventId) && myCalendar.getEvents().get(eventId).getOwner().equals(requestingUser)){
		myCalendar.getEvents().remove(eventId);						// remove private events owned by user
		successfulDeletion = true;
	}
	if(publicCalendar.getEvents().containsKey(eventId) && publicCalendar.getEvents().get(eventId).getOwner().equals(requestingUser)){
		publicCalendar.getEvents().remove(eventId);					// remove public events owned by user
		successfulDeletion = true;
	}
	if(groupCalendar.getEvents().containsKey(eventId) && (groupCalendar.getEvents().get(eventId).getOwner().equals(requestingUser)
		|| groupCalendar.getEvents().get(eventId).getUsers().contains(requestingUser))){
		groupCalendar.getEvents().remove(eventId);					// remove group events if owner or user
		successfulDeletion = true;
	}
	if(openCalendar.getEvents().containsKey(eventId) && openCalendar.getEvents().get(eventId).getOwner().equals(requestingUser)){
		openCalendar.getEvents().remove(eventId);
		successfulDeletion = true;
	}
	return successfulDeletion;
 }

 public String viewUsers() throws RemoteException{
	ArrayList<String> users = new ArrayList<String>();
	for(String s : calendarTable.keySet()){							// get each calendar owner
		if(!s.equals("public") && !s.equals("group") && !s.equals("open")){
			users.add(s);
		}
		else if(!s.equals("group")){												// get owners of public+open events
			for(int i : calendarTable.get(s).getEvents().keySet()){
				users.add(calendarTable.get(s).getEvents().get(i).getOwner());
			}
		}
		else{
			for(int i : calendarTable.get(s).getEvents().keySet()){					// add all users and owners of each group event
				users.add(calendarTable.get(s).getEvents().get(i).getOwner());
				users.addAll(calendarTable.get(s).getEvents().get(i).getUsers());
			}
		}
	}
	users = new ArrayList<String>(new LinkedHashSet<String>(users));		// remove duplicates
	String listToString = "";
	for(String s : users){
		listToString += (s+"\n");
	}
	return listToString;
 }

 // check open events for availability, replace with group event if so
 public boolean addGroupEvent(Event event) throws RemoteException{	
	Calendar groupCalendar = calendarTable.get("group");
	Calendar openCalendar = calendarTable.get("open");
	for(int i : openCalendar.getEvents().keySet()){     
		if(event.getStartTime() < openCalendar.getEvents().get(i).getEndTime() && event.getStartTime() >= openCalendar.getEvents().get(i).getStartTime()
			&& event.getEndTime() <= openCalendar.getEvents().get(i).getEndTime() && event.getEndTime() > openCalendar.getEvents().get(i).getStartTime()){
			openCalendar.getEvents().remove(i);
			groupCalendar.addEvent(event);
			return true;
		}
	}
	return false;	
 }
} 
