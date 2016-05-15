// Alex Foster

import java.rmi.*;
import java.util.*;
public interface CalendarManagerInterface extends Remote {
	boolean registerUser(String user) throws RemoteException;
	void logout(String user) throws RemoteException;
	String checkEvents() throws RemoteException;
	String retrieveEvents(String requestingUser, String requestedUser, int startTime, int endTime) throws RemoteException;
	boolean scheduleEvent(String requestingUser, ArrayList<String> requestedUsers, String eventDescription, String eventType, int start, int end) throws RemoteException;
	boolean deleteEvent(String requestingUser, int eventId) throws RemoteException;	
	String viewUsers() throws RemoteException;
	boolean addGroupEvent(Event event) throws RemoteException;
} 
