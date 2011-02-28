package org.arpameeting.phonebrowser.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.arpameeting.phonebrowser.conference.Room;
import org.arpameeting.phonebrowser.conference.RoomsManager;

@Path("/rooms")
public class RoomsResource 
{
	@Context
	Request request;
	@Context
	UriInfo uriInfo;
	
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public List<Room> getRooms()
	{
		RoomsManager roomsManager = RoomsManager.getInstance();
		List<Room> rooms = new ArrayList<Room>();
		rooms.addAll( roomsManager.getRooms().values() );
		return rooms;
	}
	
	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		RoomsManager roomsManager = RoomsManager.getInstance();
		int count = roomsManager.getRooms().size();
		return String.valueOf(count);
	}
	
	@GET
	@Path("{room}")
	public RoomResource getRoom(
			@PathParam("room") String room) 
	{
		return new RoomResource(uriInfo, request, room);
	}

}
