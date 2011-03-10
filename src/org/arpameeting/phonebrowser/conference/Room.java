package org.arpameeting.phonebrowser.conference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.arpameeting.phonebrowser.events.PhoneBrowserEvent;
import org.arpameeting.phonebrowser.events.RoomObserver;
import org.arpameeting.phonebrowser.sip.AsteriskSIPServer;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.MeetMeRoom;
import org.asteriskjava.live.MeetMeUser;
import org.red5.server.api.service.IServiceCapableConnection;

/**
 * 
 * Sala de conferencias.
 * 
 * @author Marcos Gabarda
 *
 */
@XmlRootElement
public class Room
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

	private CallObserver observer;
	
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
		observer = new CallObserver(this);
		asteriskServer = new DefaultAsteriskServer(properties.getProperty("ami.host"), 
				properties.getProperty("ami.username"), 
				properties.getProperty("ami.password"));
		asteriskServer.addAsteriskServerListener(observer);
		asteriskServer.getManagerConnection().addEventListener(observer);
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
		ArrayList<MeetMeUser> meetMeUsers = new ArrayList<MeetMeUser>(getMeetMeRoom().getUsers());
		for (MeetMeUser meetMeUser : meetMeUsers)
		{
			meetMeUser.kick();
		}
		AsteriskSIPServer sipServer = AsteriskSIPServer.getInstance();
		sipServer.releaseUser(roomSIPListener);
		//managerConnection.logoff();
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
			call.setObserver(observer);
			call.setMonitornig(participant.isMonitoring());
			participant.setCall(call);
			Thread thread = new Thread(call, "thread");
			thread.start();
		}
		else if (participant.getType() == ParticipantType.PHONE)
		{
			System.out.println("Llamando a " + participant.getPhone() + "...");
			CallPhone call = new CallPhone(participant.getPhone(), this.meetRoomNumber, participant);
			call.setObserver(observer);
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
			asteriskServer.getChannelByName(getRoomChannelListener());
		if (asteriskChannel != null)
		{
			asteriskChannel.hangup();
		}
	}

	public void addRed5Connection(IServiceCapableConnection conn)
	{
		red5Connections.add(conn);
	}

	public void setMeetMeRoom(MeetMeRoom meetMeRoom) {
		this.meetMeRoom = meetMeRoom;
	}

	@XmlTransient
	public MeetMeRoom getMeetMeRoom() {
		return meetMeRoom;
	}

	public void setRoomChannelListener(String roomChannelListener) {
		this.roomChannelListener = roomChannelListener;
	}

	public String getRoomChannelListener() {
		return roomChannelListener;
	}
	
}
