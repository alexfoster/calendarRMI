// Alex Foster

import java.util.*;
public class Event{	
	int eventId;
	String eventDescription;
	String eventType;
	String owner;
	int startTime;
	int endTime;
	ArrayList<String> users;

	public Event(String eventDescription, String eventType, String owner,
				int startTime, int endTime){
		this.eventDescription = eventDescription;
		this.eventType = eventType;
		this.owner = owner;
		this.startTime = startTime;
		this.endTime = endTime;
		this.users = new ArrayList<String>();				// only needed for group events	
	}

	public int getEventId(){
		return this.eventId;
	}

	public void setEventId(int id){
		this.eventId = id;
	}

	public String getEventDescription(){
		return this.eventDescription;
	}

	public String getEventType(){
		return this.eventType;
	}

	public String getOwner(){
		return this.owner;
	}

	public int getStartTime(){
		return this.startTime;
	}

	public int getEndTime(){
		return this.endTime;
	}

	public ArrayList<String> getUsers(){
		return this.users;
	}

	public void addUser(String user){
		this.users.add(user);
	}

	public String userString(){
		String returnString = "";
		for(String s : this.users){
			returnString += (s+" ");
		}
		return returnString;
	}

	public String toAbbrevString(){
		return "\nEvent "+this.eventId+"\n"+this.startTime+"-"+this.endTime+"\n";
	}

	@Override
	public String toString(){
		if(this.getEventType().equals("Group")){
			return "\nEvent "+this.eventId+" - "+this.eventType+"\n"+
				"Description: "+this.eventDescription+"\n"+
				"Owner: "+this.owner+"\n"+
				"Users: "+this.userString()+"\n"+
				"Start/end: "+this.startTime+"-"+this.endTime+"\n";
		}
		return "\nEvent "+this.eventId+" - "+this.eventType+"\n"+
				"Description: "+this.eventDescription+"\n"+
				"Owner: "+this.owner+"\n"+
				"Start/end: "+this.startTime+"-"+this.endTime+"\n";
	}
} 
