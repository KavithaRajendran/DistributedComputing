/**
 * @author Kavitha Rajendran 
 * 	   Rajkumar Panneer selvam  
 * 
 * It is used to sent Explore Message between the nodes 
 * which contains source, destination and distance from the root.
 *
 */
public class ExploreMessage implements Message {

	private int sourceID=-1;
	
	private int destinationID=-1;
	
	private int distance=-1;
	
	public  ExploreMessage(int source, int dest , int dist) {
		// TODO Auto-generated constructor stub
		sourceID=source;
		destinationID=dest;
		distance=dist;
		
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

	public int getDistance() {
		// TODO Auto-generated method stub
		return distance;
	}

}
