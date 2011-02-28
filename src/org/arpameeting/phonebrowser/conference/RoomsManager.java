package org.arpameeting.phonebrowser.conference;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.ThreadSafe;

/**
 * Creación y gestión de las salas de conferencia.
 * 
 * 
 * Patterns: Sigleton.
 * 
 * @author Marcos Gabarda
 *
 */
@ThreadSafe
public class RoomsManager 
{
	/**
	 * 
	 */
    static private RoomsManager singletonRoomManager = new RoomsManager();

    /**
     * 
     */
    private ConcurrentHashMap<String, Room> rooms;
    
    /**
     * 
     */
    private ConcurrentHashMap<String, Boolean> roomNumbers;

    private SecureRandom random = new SecureRandom();
    
    /**
     * 
     */
    private RoomsManager()
    {
        rooms = new ConcurrentHashMap<String, Room>();
        roomNumbers = new ConcurrentHashMap<String, Boolean>();
    	for (int i = 1 ; i < 9999; i++)
    	{
    		String number = new Integer(i).toString();
        	int n  = 4 - number.length();
        	for (int j = 0 ; j < n; j++)
        	{
        		number = "0" + number;
        	}
        	roomNumbers.put(number, new Boolean(true));
    	}
    }

    /**
     * 
     * @return
     */
    static public RoomsManager getInstance()
    {
        return singletonRoomManager;
    }

    /**
     * Crea una nueva sala de conferencia.
     * @param id
     * @return Room
     */
    public Room newRoom()
    {
    	String id = new BigInteger(130, random).toString(32);
    	while (rooms.containsKey(id))
    		id = new BigInteger(130, random).toString(32);
    	
    	String roomNumber = null;
    	Set<String> numbers = roomNumbers.keySet();
    	for (String number: numbers)
    	{
    		if (roomNumbers.get(number))
    		{
    			roomNumber = number;
    			break;
    		}
    	}
    	if (roomNumber == null)
    	{
    		/**
    		 * TODO Error
    		 */
    		return null;
    	}
    	
        Room room = new Room(id, roomNumber);
        roomNumbers.put(roomNumber, new Boolean(false));
        rooms.put(room.getId(), room);
        return room;
    }

    /**
     * Obtiene una sala dado su id.
     * @param roomId
     * @return Room
     */
    public Room getRoom(String roomId)
    {
        return rooms.get(roomId);
    }
    
    public ConcurrentHashMap<String, Room> getRooms()
    {
    	return rooms;
    }
    
    /**
     * 
     * @param roomId
     * @return
     */
    public boolean deleteRoom(String roomId)
    {
    	Room room = getRoom(roomId);
    	if (room != null)
    	{
    		room.finishConference();
    		rooms.remove(room.getId());
    		roomNumbers.put(room.getNumber(), new Boolean(true));
    		return true;
    	}
    	return false;
    }
}
