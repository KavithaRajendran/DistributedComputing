
/**
 * @author Kavitha Rajendran
 * 	   Rajkumar Panneer selvam 
 * 
 * Message interface needs to be implemented by all messages need to be exchanged between nodes.
 */
public interface Message {
	
	public int getSourceID();
	
	public int getDestinationID();
	
}
