package org.arpameeting.phonebrowser.events;

import org.arpameeting.phonebrowser.conference.Participant;

public class PBBusyEvent extends PhoneBrowserEvent 
{
	//private Participant participant;
	
	public PBBusyEvent(Participant participant) 
	{
		super();
		//this.participant = participant;
		addData("event", getEventName());
		addData("id", participant.getId());
		addData("name", participant.getId());
		
	}
	@Override
	public String getEventName() 
	{
		return this.getClass().getName();
	}

}
