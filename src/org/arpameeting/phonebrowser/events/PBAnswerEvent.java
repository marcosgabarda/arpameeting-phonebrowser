package org.arpameeting.phonebrowser.events;

import org.arpameeting.phonebrowser.conference.Participant;

public class PBAnswerEvent extends PhoneBrowserEvent 
{
	//private Participant participant;
	
	public PBAnswerEvent(Participant participant) 
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
