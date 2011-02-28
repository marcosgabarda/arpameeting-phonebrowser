package org.arpameeting.phonebrowser.events
{
	import flash.events.Event;
	
	public class PhoneBrowserManagerEvent extends Event
	{
		public static const PB_CONNECTED:String            = "PB_CONNECTED";
		public static const PB_ROOM_FINISHED:String        = "PB_ROOM_FINISHED";
		public static const PB_PARTICIPANT_FINISHED:String = "PB_PARTICIPANT_FINISHED";
		public static const PB_LISTENER_FINISHED:String    = "PB_LISTENER_FINISHED";
		public static const PB_PARTICIPANT_ERROR:String    = "PB_PARTICIPANT_ERROR";
		public static const PB_LISTENER_ERROR:String       = "PB_LISTENER_ERROR";
		public static const PB_ROOM_ERROR:String           = "PB_ROOM_ERROR";
		public static const PB_ERROR:String                = "PB_ERROR";
		
		public function PhoneBrowserManagerEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		
		override public function clone():Event
		{
			return new PhoneBrowserManagerEvent(type);
		}
	}
}