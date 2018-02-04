
/**
 * @author Kavitha Rajendran - kxr161830
 * 		   Rajkumar Panneer selvam - rxp162130 
 * 
 * It is used to sent Reply Message between the nodes 
 * which contains source, destination and accept/Reject (based on the boolean isAccepted).
 */
public class ReplyMessage implements Message  {
	
	private int sourceID=-1;
	
	private int destinationID=-1;
	
	private boolean isAccepted;
	
	public ReplyMessage(int source, int dest, boolean isAccept) {
		// TODO Auto-generated constructor stub
		 sourceID=source;
		 destinationID=dest;
		 isAccepted=isAccept;
	}

	public boolean isAccepted() {
		return isAccepted;
	}

	@Override
	public int getSourceID() {
		// TODO Auto-generated method stub
		return sourceID;
	}

	@Override
	public int getDestinationID() {
		// TODO Auto-generated method stub
		return destinationID;
	}
	
}
