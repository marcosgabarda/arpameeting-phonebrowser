package org.arpameeting.phonebrowser.red5;

import java.util.concurrent.ConcurrentHashMap;

import org.arpameeting.phonebrowser.conference.Call;
import org.arpameeting.phonebrowser.conference.Participant;
import org.arpameeting.phonebrowser.conference.ParticipantType;
import org.arpameeting.phonebrowser.conference.Room;
import org.arpameeting.phonebrowser.conference.RoomsManager;

import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.Red5;
import org.red5.server.api.service.IServiceCapableConnection;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.ISubscriberStream;

/**
 * Aplicación de Red5 para escuchar audio de una cuenta SIP.
 * 
 * @author Marcos Gabarda
 *
 */
public class Application extends MultiThreadedApplicationAdapter 
{
	/**
	 * 
	 */
	private ConcurrentHashMap<String, Room> listenerConnections = 
		new ConcurrentHashMap<String, Room>();
	
	/**
	 * 
	 */
	private ConcurrentHashMap<String, Participant> participantsConnections = 
		new ConcurrentHashMap<String, Participant>();

	/**
	 * Obtiene la cuenta SIP de obvservador.
	 * 
	 * @param roomId
	 * @return
	 */
	public String getSIPListenerAccount(String roomId)
	{
		IConnection conn = Red5.getConnectionLocal();
		System.out.println("Obtaining listener SIP...");
		RoomsManager roomsManager = RoomsManager.getInstance();
		Room room = roomsManager.getRoom(roomId);
		if (room == null) 
		{
			System.out.println("Sala no econtrada!");
			return null;
		}
		listenerConnections.put(conn.getClient().getId(), room);
		return room.getSIPListener();
	}
	
	/**
	 * 
	 * @param roomId
	 * @param participantId
	 * @return
	 */
	public String getSIPParticipant(String roomId, String participantId)
	{
		IConnection conn = Red5.getConnectionLocal();
		System.out.println("Obtaining participant SIP...");
		RoomsManager roomsManager = RoomsManager.getInstance();
		Room room = roomsManager.getRoom(roomId);
		if (room == null) 
		{
			System.out.println("Sala no econtrada!");
			return null;
		}
		Participant participant = room.getParticipant(participantId);
		if (participant == null) 
		{
			System.out.println("Participante no econtrado!");
			return null;
		}
		if (participant.getType() != ParticipantType.BROWSER) 
		{
			System.out.println("Participante incorrecto!");
			return null;
		}
		participantsConnections.put(conn.getClient().getId(), participant);
		return participant.getFlashSip();
	}
	
	/**
	 * 
	 * @param roomId
	 * @return
	 */
	public String getRoomNumber(String roomId)
	{
		System.out.println("Obtaining room number...");
		RoomsManager roomsManager = RoomsManager.getInstance();
		Room room = roomsManager.getRoom(roomId);
		if (room == null) 
		{
			return null;
		}
		return room.getNumber();
	}
	
	@Override
	public boolean appStart(IScope scope) 
	{
		log.info("Starting ArpaMeeting Phone-Browser");
		System.out.println("Scope: " + scope.getName());
		return true;
	}
	@Override
	public void appStop(IScope scope) 
	{
		log.info("Stoping ArpaMeeting Phone-Browser");
	}
	@Override
	public boolean appConnect(IConnection conn, Object[] params) 
	{
		log.info("Connect");
		IScope scope = conn.getScope();
		System.out.println("Connection to scope: " + scope.getName());
		
		RoomsManager roomsManager = RoomsManager.getInstance();
		Room room = roomsManager.getRoom(scope.getName());
		if (room != null)
		{
			room.addRed5Connection((IServiceCapableConnection)conn);
		}
		
		return true;
	}
	
	@Override
	public void appDisconnect(IConnection conn)
	{
		log.info("Disconnect");
		if (listenerConnections.containsKey(conn.getClient().getId()))
		{
			Room room = listenerConnections.get(conn.getClient().getId());
			System.out.println("Monitor de sala " + room.getId() + " desconectado");
			/**
			 * Hangup listener channel.
			 */
			room.hangUpListener();
		}
		if (participantsConnections.containsKey(conn.getClient().getId()))
		{
			Participant participant = participantsConnections.get(conn.getClient().getId());
			System.out.println("Participante " + participant.getId() + " desconectado");
			/**
			 * Hangup participant browser channel.
			 */
			Call call = participant.getCall();
			call.hangUp();
		}
	}
	
	@Override
	public void streamSubscriberStart(ISubscriberStream stream) {
		// TODO Auto-generated method stub
		super.streamSubscriberStart(stream);
	}
	
	@Override
	public void streamSubscriberClose(ISubscriberStream stream) {
		// TODO Auto-generated method stub
		super.streamSubscriberClose(stream);
	}
	
	@Override
	public void streamBroadcastStart(IBroadcastStream stream) {
		super.streamBroadcastStart(stream);
	}
	
	@Override
	public void streamBroadcastClose(IBroadcastStream stream) 
	{
		// TODO Auto-generated method stub
		super.streamBroadcastStart(stream);
	}
}