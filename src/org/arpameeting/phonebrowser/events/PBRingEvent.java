package org.arpameeting.phonebrowser.events;

import org.arpameeting.phonebrowser.conference.Participant;

public class PBRingEvent extends PhoneBrowserEvent 
{

	//private Participant participant;
	
	public PBRingEvent(Participant participant) 
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
