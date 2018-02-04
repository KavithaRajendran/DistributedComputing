
/**
 * @author Kavitha Rajendran - kxr161830
 * 		   Rajkumar Panneer selvam - rxp162130 
 * 
 * It is used to sent Terminate Message between the nodes 
 * which contains source, destination.
 */
public class TerminateMessage implements Message{

private int sourceID=-1;
	
	private int destinationID=-1;
	
	public  TerminateMessage(int source,int dest) {
		// TODO Auto-generated constructor stub
		sourceID=source;
		destinationID=dest;
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
