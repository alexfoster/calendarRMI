// Alex Foster

import java.util.*;
import java.io.*;

 public class Calendar implements CalendarInterface {

 public String user;
 public Hashtable<Integer, Event> eventTable;
 public int index = 0;
 public boolean userOnline = false;

 public Calendar(String user) { 
	this.user = user;
	eventTable = new Hashtable<Integer, Event>();	
 }

 // add event to calendar and handle any conflicts
 public boolean addEvent(Event event){	
	for(Integer i : eventTable.keySet()){
		if(eventTable.get(i).getStartTime() < event.getEndTime() && 
			eventTable.get(i).getEndTime() > event.getStartTime()){
			return false;
		}
	}
	eventTable.put(event.getEventId(), event);
 	return true;
 }

 public Hashtable<Integer, Event> getEvents(){
	return this.eventTable;
 }

 public Hashtable<Integer, Event> getEventsInRange(int start, int end){
	Hashtable<Integer, Event> subset = new Hashtable<Integer, Event>();
	for(Integer i : this.eventTable.keySet()){
		if(start <= this.eventTable.get(i).getStartTime() && end >= this.eventTable.get(i).getEndTime()){	// event is contained in range
			subset.put(i, this.eventTable.get(i));
		}
	}
	return subset;
 }

 public String eventsToString(String requestingUser, Hashtable<Integer, Event> events){
	String eventString = "";
	for(Integer i : events.keySet()){
		if(events.get(i).getEventType().equals("Group") && !events.get(i).getUsers().contains(requestingUser) && 
			!events.get(i).getOwner().equals(requestingUser)){			// if no access to group event, see abbreviated version
			eventString += events.get(i).toAbbrevString();
		}
		else{
			eventString += events.get(i).toString();
		}
	}
	return eventString;
 }

 public boolean getOnline(){
	return this.userOnline;
 }

 public void setOnline(boolean online){
	this.userOnline = online;
 }
} 
