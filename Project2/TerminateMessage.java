//package asynchronous;

public class TerminateMessage implements Message{

private int sourceID=-1;
	
	private int destinationID=-1;
	
	private int time;
	
	public  TerminateMessage(int source,int dest,int time) {
		// TODO Auto-generated constructor stub
		sourceID=source;
		destinationID=dest;
		this.time=time;
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

	@Override
	public int getTime() {
		// TODO Auto-generated method stub
		return time;
	}

}
