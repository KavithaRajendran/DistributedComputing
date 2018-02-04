//package asynchronous;

public class ReplyMessage implements Message  {
	
	private int sourceID=-1;
	
	private int destinationID=-1;
	
	private boolean isAccepted;
	
	private int time;
	
	 public ReplyMessage(int source, int dest, boolean isAccept, int time) {
		// TODO Auto-generated constructor stub
		 sourceID=source;
		 destinationID=dest;
		 isAccepted=isAccept;
		 this.time =time;
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



	@Override
	public int getTime() {
		// TODO Auto-generated method stub
		return time;
	}




	

	
}
