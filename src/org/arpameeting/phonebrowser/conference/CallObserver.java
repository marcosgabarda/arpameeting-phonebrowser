/**
 * 
 */
package org.arpameeting.phonebrowser.conference;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

import org.arpameeting.phonebrowser.events.PBRingEvent;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskQueueEntry;
import org.asteriskjava.live.AsteriskServerListener;
import org.asteriskjava.live.MeetMeUser;
import org.asteriskjava.live.internal.AsteriskAgentImpl;
import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.DialEvent;
import org.asteriskjava.manager.event.HangupEvent;

import org.asteriskjava.manager.event.StatusEvent;

/**
 * @author Marcos Gabarda
 *
 */
public class CallObserver extends AbstractManagerEventListener
	implements AsteriskServerListener
{

	/**
	 * Room.
	 */
	private Room room;
	
	/**
	 * Calls in a room.
	 */
	private ConcurrentHashMap<String, Call> calls;
	
	public CallObserver(Room room)
	{
		this.room = room; 
		calls = new ConcurrentHashMap<String, Call>();
	}
	
	public void addCall(Call call)
	{
		calls.put(call.getChannel(), call);
	}
	
	@Override
	public void onNewMeetMeUser(MeetMeUser meetMeUser)
	{
		if (room.getMeetMeRoom() == null && meetMeUser.getRoom().getRoomNumber().equals(room.getId()))
		{
			room.setMeetMeRoom(meetMeUser.getRoom());
		}
		String channel = meetMeUser.getChannel().getName();
		Call call  = calls.get(channel);
		if (call != null)
		{
			call.setUsernum(meetMeUser.getUserNumber());
			call.setStarted(Calendar.getInstance().getTime());
		}
		else
		{
			// TODO
		}
		
		/**
		 * Si el que se conecta es el listener.
		 */
		if (channel.startsWith("SIP/" + room.getSIPListener().split("@")[0]))
		{
			room.setRoomChannelListener(channel);
		}
		
	}
	
	@Override
	public void onNewAgent(AsteriskAgentImpl arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewAsteriskChannel(AsteriskChannel arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onNewQueueEntry(AsteriskQueueEntry arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * Dialing event.
	 */
	@Override
	protected void handleEvent(DialEvent event)
	{
		super.handleEvent(event);
		Call call  = calls.get(event.getChannel());
		if (call != null)
		{
			call.setCallState(CallState.DIALING);
			System.out.println("[" + call.getId() + "]" + " dialing " + call.getChannel());
			/**
			 * Notify.
			 */
			call.getParticipant().getRoom().notifyEvent(
					new PBRingEvent(call.getParticipant()));
		}
	}
	
	/**
	 * Status event.
	 */
	@Override
	protected void handleEvent(StatusEvent event)
	{
		super.handleEvent(event);
		Call call  = calls.get(event.getChannel());
		if (call != null)
		{
			System.out.println("[" + call.getId() + "]" + " new state: " + event.getChannelStateDesc());
		}
	}
	
	/**
	 * 
	 */
	@Override
	protected void handleEvent(HangupEvent event) 
	{
		super.handleEvent(event);
		Call call  = calls.get(event.getChannel());
		if (call != null)
		{
			System.out.println("[" + call.getId() + "]" + " hangup event in channel " + event.getChannel());
			call.hangUp();
		}
	}
	
	/**
	 * 
	 */
	/*@Override
	public void onNewMeetMeUser(MeetMeUser meetMeUser) 
	{
		if (meetMeRoom == null && meetMeUser.getRoom().getRoomNumber().equals(id))
		{
			meetMeRoom = meetMeUser.getRoom();
		}
		String channel = meetMeUser.getChannel().getName();
		System.out.println("New meetme user: " + channel);
		for (Participant participant : participants)
		{
			if (participant.getType() != ParticipantType.BROWSER)
			{
				String participantChannel = participant.getCall().getChannel();
				if (participantChannel != null && 
						participantChannel.equals(channel))
				{
					participant.getCall().setUsernum(meetMeUser.getUserNumber());
					participant.getCall().setStarted(Calendar.getInstance().getTime());
				}
			}
			else
			{
				if (channel.startsWith("SIP/" + participant.getFlashSip().split("@")[0]))
				{
					System.out.println("Linking "+ channel + " to SIP/" + participant.getFlashSip().split("@")[0]);
					participant.getCall().setUsernum(meetMeUser.getUserNumber());
					participant.getCall().setStarted(Calendar.getInstance().getTime());
					participant.getCall().setChannel(channel);
				}
			}
		}
		/**
		 * Si el que se conecta es el listener.
		 */
		/*if (channel.startsWith("SIP/" + roomSIPListener.split("@")[0]))
		{
			roomChannelListener = channel;
		}
		
	}*/
	
	/*@Override
	protected void handleEvent(MeetMeTalkingEvent event) 
	{
		super.handleEvent(event);
		String channel = event.getChannel();
		for (Participant participant : participants)
		{
			Call call = participant.getCall();
			if (call != null && call.getChannel() != null &&
					call.getChannel().compareTo(channel) == 0)
			{
				if (event.getStatus())
				{
					System.out.println(participant.getName() + " starts talking!");
				}
				else
				{
					System.out.println(participant.getName() + " stops talking!");
				}
			}
		}
	}*/
	
	/* (non-Javadoc)
	 * @see org.asteriskjava.live.OriginateCallback#onBusy(org.asteriskjava.live.AsteriskChannel)
	 */
	/*@Override
	public void onBusy(AsteriskChannel channel)
	{
		System.out.println("[" + call.getId() + "]" + " busy " + channel.getName());
		call.getParticipant().getRoom().notifyEvent(
				new PBBusyEvent(call.getParticipant()));
	}*/


	/* (non-Javadoc)
	 * @see org.asteriskjava.live.OriginateCallback#onFailure(org.asteriskjava.live.LiveException)
	 */
	/*@Override
	public void onFailure(LiveException exception)
	{
		call.setCallState(CallState.ILDE);
		System.out.println("failure");
	}*/

	/* (non-Javadoc)
	 * @see org.asteriskjava.live.OriginateCallback#onNoAnswer(org.asteriskjava.live.AsteriskChannel)
	 */
	/*@Override
	public void onNoAnswer(AsteriskChannel channel)
	{
		call.setCallState(CallState.NOANSWER);
		call.getParticipant().getRoom().notifyEvent(
				new PBNoAnswerEvent(call.getParticipant()));
		System.out.println("[" + call.getId() + "]" + " noanswer");
	}*/

	/* (non-Javadoc)
	 * @see org.asteriskjava.live.OriginateCallback#onSuccess(org.asteriskjava.live.AsteriskChannel)
	 */
	/*@Override
	public void onSuccess(AsteriskChannel channel)
	{
		call.setCallState(CallState.SUCCESS);
		System.out.println("[" + call.getId() + "]" + " success " + channel.getName());
		call.getParticipant().getRoom().notifyEvent(
				new PBAnswerEvent(call.getParticipant()));
		call.startRecording();
	}*/

	
	
	/*@Override
	protected void handleEvent(HangupEvent event) 
	{
		System.out.println("HangupEvent in channel " + event.getChannel());
		super.handleEvent(event);
		if (call.getChannel() != null && call.getChannel().compareTo(event.getChannel()) == 0)
		{
			call.hangUp();
		}
	}*/
	
	
}
