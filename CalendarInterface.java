// Alex Foster

import java.util.*;
public interface CalendarInterface {
	boolean addEvent(Event event);
	Hashtable<Integer, Event> getEvents();
	Hashtable<Integer, Event> getEventsInRange(int start, int end);
	String eventsToString(String requestingUser, Hashtable<Integer, Event> events);
	boolean getOnline();
	void setOnline(boolean online);
} 
