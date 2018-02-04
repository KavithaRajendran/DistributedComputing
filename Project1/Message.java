
/**
 * @author Kavitha Rajendran - kxr161830
 * 		   Rajkumar Panneer selvam - rxp162130 
 * 
 * Message interface needs to be implemented by all messages need to be exchanged between nodes.
 */
public interface Message {
	
	public int getSourceID();
	
	public int getDestinationID();
	
}
