//package asynchronous;

public class ExploreMessage implements Message {

	private int sourceID=-1;
	
	private int destinationID=-1;
	
	private int distance=-1;
	
	private int time;
	
	private int no_Of_Hops;
	
	public  ExploreMessage(int source, int dest , int dist,int time,int hop) {
		// TODO Auto-generated constructor stub
		sourceID=source;
		destinationID=dest;
		distance=dist;
		this.time=time;
		no_Of_Hops=hop;
		
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

	@Override
	public int getTime() {
		// TODO Auto-generated method stub
		return time;
	}

	public int getNo_Of_Hops() {
		return no_Of_Hops;
	}

	

}
