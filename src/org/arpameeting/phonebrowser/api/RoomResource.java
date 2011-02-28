package org.arpameeting.phonebrowser.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.arpameeting.phonebrowser.conference.Participant;
import org.arpameeting.phonebrowser.conference.ParticipantType;
import org.arpameeting.phonebrowser.conference.Room;
import org.arpameeting.phonebrowser.conference.RoomsManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.NotFoundException;

@Path("/room")
public class RoomResource
{
	@Context
	Request request;
	@Context
	UriInfo uriInfo;

	String room;
	
	public RoomResource()
	{
	}
	
	public RoomResource(UriInfo uriInfo, Request request, String room) 
	{
		this.uriInfo = uriInfo;
		this.request = request;
		this.room = room;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Room getRoom()
	{
		RoomsManager roomsManager = RoomsManager.getInstance();
		Room room = roomsManager.getRoom(this.room);
		if(room==null)
			throw new NotFoundException("No such Room.");
		return room;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Room getRoom(
			@PathParam("id") String id)
	{
		RoomsManager roomsManager = RoomsManager.getInstance();
		Room room = roomsManager.getRoom(id);
		if(room==null)
			throw new NotFoundException("No such Room.");
		return room;
	}
	
	/**
	 * Crea una nueva sala de conferencia y la inicia en el momento. Llama a 
	 * todos los participantes.
	 * 
	 * @param content
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Room newRoom(Document content)
	{
		RoomsManager roomsManager = RoomsManager.getInstance();
		Room room = roomsManager.newRoom();
		NodeList participants = content.getElementsByTagName("participant");
		for (int i = 0; i < participants.getLength(); i++)
		{
			Element participantElement = (Element) participants.item(i);
			/**
			 * Get name.
			 */
			String name = participantElement.getElementsByTagName("name").item(0).getTextContent();
			/**
			 * Get type and contact.
			 */
			String type                     = null;
			String contact                  = null;
			ParticipantType participantType = null;
			if (participantElement.getElementsByTagName("type").getLength() != 0)
			{
				type = participantElement.getElementsByTagName("type").item(0).getTextContent();
			}
			else
			{
				type = "PHONE";
			}
			if (type.toLowerCase().compareTo("phone") == 0)
			{
				contact = participantElement.getElementsByTagName("phone").item(0).getTextContent();
				participantType = ParticipantType.PHONE;
			}
			else if (type.toLowerCase().compareTo("sip") == 0)
			{
				contact = participantElement.getElementsByTagName("sip").item(0).getTextContent();
				participantType = ParticipantType.SIP;
			}
			else if (type.toLowerCase().compareTo("browser") == 0)
			{
				participantType = ParticipantType.BROWSER;
			}
			Participant participant = room.addParticipant(name, contact, participantType);
			if (participantElement.getElementsByTagName("recording").getLength() == 0 || 
					participantElement.getElementsByTagName("recording").item(0).getTextContent().compareTo("true") == 0)
			{
				participant.setMonitoring(true);
			}
			else
			{
				participant.setMonitoring(false);
			}
		}
		/**
		 * Añadir parámetro para gestionar eventos:
		 * "<callback>{url}</callback>"
		 */
		NodeList callbacks = content.getElementsByTagName("callback");
		for (int i = 0; i < callbacks.getLength(); i++)
		{
			Node callback = callbacks.item(i);
			String url = callback.getTextContent();
			room.addCallbackURL(url);
		}
		room.startConference();
		return room;
	}
	
	/*
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Room newRoom(JSONObject jsonEntity) throws JSONException
	{
		RoomsManager roomsManager = RoomsManager.getInstance();
		Room room = roomsManager.newRoom();
		JSONArray participants = jsonEntity.getJSONArray("participant");
		for (int i = 0; i < participants.length(); i++)
		{
			String name = participants.getJSONObject(i).getString("name");
			String phone = participants.getJSONObject(i).getString("phone");
			room.addParticipant(name, phone, ParticipantType.PHONE);
		}
		JSONArray callbacks = jsonEntity.getJSONArray("callback");
		for (int i = 0; i < callbacks.length(); i++)
		{
			String url = callbacks.getString(i);
			room.addCallbackURL(url);
		}
		
		room.startConference();
		return room;
	}
	*/
	
	@DELETE
	@Path("{id}")
	public void deleteRoom(@PathParam("id") String id)
	{
		RoomsManager roomsManager = RoomsManager.getInstance();
		roomsManager.deleteRoom(id);
	}
	
	@PUT
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Room addToRoom(@PathParam("id") String id,
			Document content)
	{
		RoomsManager roomsManager = RoomsManager.getInstance();
		Room room = roomsManager.getRoom(id);
		if (room != null)
		{
			NodeList participants = content.getElementsByTagName("participant");
			for (int i = 0; i < participants.getLength(); i++)
			{
				Element participant = (Element) participants.item(i);
				String name    = participant.getElementsByTagName("name").item(0).getTextContent();
				String contact = null;
				String type    = null;
				if (participant.getElementsByTagName("type") != null)
				{
					type = participant.getElementsByTagName("type").item(0).getTextContent();
				
				}
				else
				{
					type = "PHONE";
				}
				ParticipantType participantType = null;
				if (type.toLowerCase().compareTo("phone") == 0)
				{
					contact = participant.getElementsByTagName("phone").item(0).getTextContent();
					participantType = ParticipantType.PHONE;
				}
				else if (type.toLowerCase().compareTo("sip") == 0)
				{
					contact = participant.getElementsByTagName("sip").item(0).getTextContent();
					participantType = ParticipantType.SIP;
				}
				else if (type.toLowerCase().compareTo("browser") == 0)
				{
					participantType = ParticipantType.BROWSER;
				}
				room.addParticipant(name, contact, participantType);
			}
		}
		return room;
	}
	
}
