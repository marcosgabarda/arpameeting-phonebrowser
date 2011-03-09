package org.arpameeting.phonebrowser.conference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.arpameeting.phonebrowser.events.PhoneBrowserEvent;
import org.arpameeting.phonebrowser.events.RoomObserver;
import org.arpameeting.phonebrowser.sip.AsteriskSIPServer;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskQueueEntry;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.AsteriskServerListener;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.MeetMeRoom;
import org.asteriskjava.live.MeetMeUser;
import org.asteriskjava.live.internal.AsteriskAgentImpl;
import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.MeetMeTalkingEvent;
import org.red5.server.api.service.IServiceCapableConnection;

/**
 * 
 * Sala de conferencias.
 * 
 * @author Marcos Gabarda
 *
 */
@XmlRootElement
public class Room extends AbstractManagerEventListener implements AsteriskServerListener
{
	@XmlElement(name="number")
	private String id;
	
	/**
	 * Numero de sala.
	 */
	private String meetRoomNumber;
	
	@XmlElement(name="participant")
	private ArrayList<Participant> participants;
	
	/**
	 * 
	 */
	private ArrayList<RoomObserver> roomObservers;
	
	/**
	 * 
	 */
	private AsteriskServer asteriskServer;

	/**
	 * 
	 */
	private RoomState state;
	
	/**
	 * 
	 */
	private MeetMeRoom meetMeRoom = null;
	
	/**
     * 
     */
    private String roomSIPListener;
	
    /**
     * 
     */
    private String roomChannelListener;
    
    private ArrayList<IServiceCapableConnection> red5Connections = 
    	new ArrayList<IServiceCapableConnection>();
    
	/**
	 * 
	 */
	public Room() {}
	
	/**
	 * Constructor. 
	 * @param id Identificador de la sala.
	 */
	public Room(String id, String number)
	{
		this.id = id;
		this.meetRoomNumber = number;
		participants = new ArrayList<Participant>();
		roomObservers = new ArrayList<RoomObserver>();
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(System.getProperty("user.dir")
					+ File.separator + "webapps"
					+ File.separator + "phonebrowser"
					+ File.separator + "phonebrowser.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		asteriskServer = new DefaultAsteriskServer(properties.getProperty("ami.host"), 
				properties.getProperty("ami.username"), 
				properties.getProperty("ami.password"));
		asteriskServer.addAsteriskServerListener(this);
		asteriskServer.getManagerConnection().addEventListener(this);
		state = RoomState.IDLE;
		AsteriskSIPServer sipServer = AsteriskSIPServer.getInstance();
		roomSIPListener = sipServer.catchUser() + 
			"@" + sipServer.getHost() + ":" + sipServer.getPort();
	}
	
	/**
	 * 
	 * @param url
	 */
	public void addCallbackURL(String url)
	{
		RoomObserver roomObserver = new RoomObserver(url);
		roomObservers.add(roomObserver);
	}
	
	/**
	 * 
	 * @param event
	 */
	public void notifyEvent(PhoneBrowserEvent event)
	{
		for (RoomObserver observer : roomObservers)
		{
			observer.update(event);
		}
	}
	
	/**
	 * Añadir un participante en la conferencia.
	 * @param name Nombre del participante.
	 * @param phone Teléfono del participante.
	 */
	public Participant addParticipant(String name, String contact, 
			ParticipantType type)
	{
		Participant participant = new Participant(name, type, this);
		if (type == ParticipantType.SIP)
		{
			participant.setSip(contact);
			participants.add(participant);
			if (state == RoomState.OPEN)
			{
				createCall(participant);
			}
		}
		else if (type == ParticipantType.PHONE)
		{
			participant.setPhone(contact);
			participants.add(participant);
			if (state == RoomState.OPEN)
			{
				createCall(participant);
			}
		}
		else if (type == ParticipantType.BROWSER)
		{
			participants.add(participant);
		}
		System.out.println("Added " + type + " participant (" + contact + ")");
		return participant;
	}
	
	public Participant getParticipant(String participantId)
	{
		for (Participant participant : participants)
		{
			if (participant.getId().compareTo(participantId) == 0) 
				return participant;
		}
		return null;
	}
	
	/**
	 * 
	 * @return String Identificador de la sala.
	 */
	public String getId()
	{
		return id;
	}

	public String getNumber()
	{
		return meetRoomNumber;
	}
	
	
	public String getSIPListener()
	{
		return roomSIPListener;
	}
	
	/**
	 * 
	 */
	public void startConference()
	{
		state = RoomState.OPEN;
		for (Participant participant : participants)
		{
			createCall(participant);
		}
	}

	/**
	 * TODO Añadir CDATA a lo que devuelve.
	 * @return
	 */
	@XmlElement(name="audio")
	public String getAudioOutURL()
	{
		HostingManager hostingManager = HostingManager.getInstance();
		String src = hostingManager.host + ":" + hostingManager.port + "/" + 
					hostingManager.outApp + "/" + 
					hostingManager.outSwf + "?room=" + this.id;
		String html = src;
		return html;
	}
	
	public void finishConference()
	{
		state = RoomState.CLOSE;
		ArrayList<MeetMeUser> meetMeUsers = new ArrayList<MeetMeUser>(meetMeRoom.getUsers());
		for (MeetMeUser meetMeUser : meetMeUsers)
		{
			meetMeUser.kick();
		}
		AsteriskSIPServer sipServer = AsteriskSIPServer.getInstance();
		sipServer.releaseUser(roomSIPListener);
		//managerConnection.logoff();
	}

	@Override
	public void onNewAgent(AsteriskAgentImpl agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewAsteriskChannel(AsteriskChannel channel) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	@Override
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
		if (channel.startsWith("SIP/" + roomSIPListener.split("@")[0]))
		{
			roomChannelListener = channel;
		}
		
	}
	
	

	@Override
	public void onNewQueueEntry(AsteriskQueueEntry queue) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 * @param participant
	 */
	private void createCall(Participant participant)
	{
		if (participant.getType() == ParticipantType.SIP)
		{
			System.out.println("Llamando a " + participant.getSip() + "...");
			CallPhone call = new CallPhone(participant.getSip(), this.meetRoomNumber, participant);
			call.setMonitornig(participant.isMonitoring());
			participant.setCall(call);
			Thread thread = new Thread(call, "thread");
			thread.start();
		}
		else if (participant.getType() == ParticipantType.PHONE)
		{
			System.out.println("Llamando a " + participant.getPhone() + "...");
			CallPhone call = new CallPhone(participant.getPhone(), this.meetRoomNumber, participant);
			call.setMonitornig(participant.isMonitoring());
			participant.setCall(call);
			Thread thread = new Thread(call, "thread");
			thread.start();
		}
		else if (participant.getType() == ParticipantType.BROWSER)
		{
			CallBrowser call = new CallBrowser(participant);
			call.setMonitornig(participant.isMonitoring());
			participant.setCall(call);
		}
	}
	
	/**
	 * 
	 */
	public void hangUpListener()
	{
		AsteriskChannel asteriskChannel = 
			asteriskServer.getChannelByName(roomChannelListener);
		if (asteriskChannel != null)
		{
			asteriskChannel.hangup();
		}
	}

	public void addRed5Connection(IServiceCapableConnection conn)
	{
		red5Connections.add(conn);
	}
	
	@Override
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
	}
	
}
