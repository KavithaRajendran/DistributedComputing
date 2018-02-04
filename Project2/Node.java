//package asynchronous;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class Node extends Thread {

	private HashMap<Integer, Integer> neighboursWeightMaps = new HashMap<Integer, Integer>();
	
	private HashMap<Integer, Integer> neighboursPreviousMaps = new HashMap<Integer, Integer>();

	private HashMap<Integer, BlockingQueue<Message>> neighboursQueueMaps = new HashMap<Integer, BlockingQueue<Message>>();
	
	private HashMap<Integer, BlockingQueue<Message>> neighboursQueueMaps1 = new HashMap<Integer, BlockingQueue<Message>>();

	private Semaphore signalSemaphore;

	private BlockingQueue<Message> myQueue = new LinkedBlockingQueue<Message>();

	private BlockingQueue<Message> myQueue1 = new LinkedBlockingQueue<Message>();
	
	private MainThread mainThread;

	private boolean isRoot;
	
	private int no_Of_Neighbour=0;

	private int no_of_Explore_Sent = 0;

	private Map<Integer, Integer> accepted_Children = new HashMap<Integer, Integer>(); // children NodeId,
	// distance;

	private int[] myParent = { -1, -1 }; // NodeId, distance;

	private int myCurrentDistanceFromRoot = Integer.MAX_VALUE;
	
	private int hopCountFromRoot= Integer.MAX_VALUE;

	private int myId;
	
	private 	Random random = new Random();

	public int getMyId() {
		return myId;
	}

	private int currentRound = 0;
	
	
	private boolean queueMap1;

	private boolean queueMap;

	private boolean isTerminate;

	public Node(MainThread mt, String id, Semaphore mySemaphore,
			int[] myNeighbourMatrix, int[] idMatrix, boolean isRoot) {

		super(id);

		signalSemaphore = mySemaphore;
		
		mainThread = mt;
		this.isRoot = isRoot;
		myId = Integer.parseInt(id);

		for (int i = 0; i < myNeighbourMatrix.length; i++) {
			if (myNeighbourMatrix[i] != -1) {
				neighboursWeightMaps.put(idMatrix[i], myNeighbourMatrix[i]);
				neighboursPreviousMaps.put(idMatrix[i], 0);
				no_Of_Neighbour++;
			}
		}

		mainThread.setProcessQueue((id), myQueue);

		mainThread.setProcessQueue((id)+"-", myQueue1);
	}

	public void run() {

		

		while (!isTerminate) {
			try {
		
				
				signalSemaphore.acquire();
				
				
				if(currentRound==0)
				{
					getNeighbourQueues();
				}
				
				
//				if(queueMap)
//				{
//				queueMap1=true;
//				queueMap=false;
//				}
//				else
//				{
//					queueMap=true;
//					queueMap1=false;
//				}

				if (isRoot) {
					executeRoot();
				} else {
					executeNonRoot();
				}
				currentRound++;
				setTime();

			} catch (InterruptedException e) {

				System.out.println(" Node-ID " + this.getName()
						+ " interrupted. ");
			}

		}
		mainThread.decrement_aliveNodes();
		System.out.println(this);


	}

	private void setTime() {
	
		for (int dest : neighboursPreviousMaps.keySet())
		{
			neighboursPreviousMaps.put(dest, currentRound);
		}
		
	}

	private void getNeighbourQueues() {

		for (Integer neighbourID : neighboursWeightMaps.keySet()) {
			neighboursQueueMaps.put(neighbourID,
					mainThread.getProcessQueue(neighbourID.toString()));
			neighboursQueueMaps1.put(neighbourID,
					mainThread.getProcessQueue(neighbourID.toString() +"-"));
			
		}

	}

	private void executeNonRoot() {

		checkInbox();
	
	}

	private void executeRoot() {
		myCurrentDistanceFromRoot=0;
		hopCountFromRoot=0;
		if (currentRound == 0) {
			sentExplore(0);

		}

		
			checkInbox();
		

	}

	private void sentExplore(int hop) {

		for (int dest : neighboursWeightMaps.keySet()) {
			Message message=null;
			if(isRoot && dest!=myId)
			{
			 message = constructExploreMessage(myId, dest, 0,0);
			 accepted_Children.put(dest, neighboursWeightMaps.get(dest));
//System.out.println( " Root sent explore to  " + dest);
			 no_of_Explore_Sent++;
			}
			else
			{
				// Non root
				if(myParent[0]!=dest && myId!=dest)
				{
				 message = constructExploreMessage(myId, dest, myCurrentDistanceFromRoot,hop);
				// System.out.println(" Node -id " + myId + " sending explore to " + dest);

				 accepted_Children.put(dest, neighboursWeightMaps.get(dest) + myCurrentDistanceFromRoot);
				 no_of_Explore_Sent++;
				}
				
					 
			}
			
			while (message!=null && !sentMessage(dest, message)) {
				// try again;
			//	System.out.println(" Sending Explore to -- " + message.getDestinationID() + " From " + message.getSourceID());

			}

			

			
		}
	}

	private void checkInbox() {
		
Queue<Message> queue = new LinkedList<Message>();
boolean isExist =true;
	//if(!queueMap)
		//{
			
		//myQueue.drainTo(queue);

		int currentSize=myQueue.size();
		while(currentSize > 0)
		{
		Message m = myQueue.remove();
		 if ( m!=null && m.getTime()== currentRound)
		 {
			 queue.add(m);
			// System.out.println( " Node -id " + myId + " Time of message " + currentRound);
		 }
		 else
		 {
			 myQueue.offer(m);
		 }
		 currentSize--;
		}
		
//		}
//		else
//		{
//			//myQueue1.drainTo(queue);
//			
//			while(isExist)
//			{
//			Message m = myQueue1.peek();
//			 if (m!=null && m.getTime()== currentRound)
//			 {
//				 queue.add(myQueue1.remove());
//			 }
//			 else
//			 {
//				 isExist=false;
//			 }
//			}
//		}
	
		while (!queue.isEmpty()) {
			Message message = queue.remove();

			if (message instanceof ExploreMessage) {
				if(isRoot)
				{
					
				sentMessage(
						message.getSourceID(),
						constructReplyMessage(message.getDestinationID(),
								message.getSourceID(), false));
				}
				else
				{
					// Non root
					int dist = ((ExploreMessage) message).getDistance();
					int hop = ((ExploreMessage) message).getNo_Of_Hops();
					if(hop < hopCountFromRoot)
					{
						myCurrentDistanceFromRoot= dist + neighboursWeightMaps.get(message.getSourceID());
						hopCountFromRoot=hop;
						updateParent(message);
						sentExplore(hop+1);
						
						if(no_Of_Neighbour==1 && no_of_Explore_Sent==0)		
						{		
							sentMessage(		
									myParent[0],		
									constructReplyMessage(myId,		
											myParent[0], true));		
						}
					}
					else
					{
//						System.out.println(" Sending reply to -- " + message.getDestinationID() + " From " + message.getSourceID());
						sentMessage(
								message.getSourceID(),
								constructReplyMessage(message.getDestinationID(),
										message.getSourceID(), false));
					}
					
				}
				
			} else if (message instanceof ReplyMessage ) {
				
				no_of_Explore_Sent--;
				
				if (no_of_Explore_Sent == 0 && isRoot) {
					
					mainThread.setFinalRound(true);// sent MT to terminate;
					sentTerminateToChildren();
					
					
				} else if (no_of_Explore_Sent==0){
//					sentMessage(
//							message.getSourceID(),
//							constructReplyMessage(message.getDestinationID(),
//									message.getSourceID(), true));
//					
				
					sentMessage(
							myParent[0],
							constructReplyMessage(myId,
									myParent[0], true));
				}
				
				if(!((ReplyMessage) message).isAccepted()){
				
					accepted_Children.remove(message.getSourceID());
				}
				else
				{
					accepted_Children.put(message.getSourceID(), neighboursWeightMaps.get(message.getSourceID()));
				}
				
				
				

			}
			else if(message instanceof TerminateMessage)
			{
				isTerminate=true;
				sentTerminateToChildren();
			}
		}
	}

	private void sentTerminateToChildren() {
		
		for (int dest : accepted_Children.keySet()) {
			
			int time=	random.nextInt(18)+1 + neighboursPreviousMaps.get(dest);
			sentMessage(
					dest,
					new TerminateMessage(myId,
							dest, time));
			neighboursPreviousMaps.put(dest, time);
			System.out.println( " Node - Id " + myId + " Terminate message to " + dest + " Time " + time);
		}
		isTerminate=true;
		
	}

	private void updateParent(Message message) {
		if(myParent[0]!=-1)
		{
			accepted_Children.remove(myParent[0]);
			sentMessage(
					myParent[0],
					constructReplyMessage(myId,
							myParent[0], false));
			
		}
		myParent[0]=message.getSourceID();
		myParent[1]= myCurrentDistanceFromRoot;
				
	}



	private Message constructExploreMessage(int source, int dest, int dist,int hop) {
		
	
		
			int time=	random.nextInt(18)+1 + neighboursPreviousMaps.get(dest);
			//System.out.println(" Explore Node Id " + myId  + " for dest  " + dest+ " Time " + time );
		Message message = new ExploreMessage(source, dest, dist,time,hop);
		neighboursPreviousMaps.put(dest, time);
		return message;
	}

	private Message constructReplyMessage(int source, int dest, boolean isAccept) {
		int time=	random.nextInt(18)+1 + neighboursPreviousMaps.get(dest);
		//System.out.println(" Reply Node Id " + myId  + " for dest  " + dest+ " Time " + time );

		Message message = new ReplyMessage(source, dest, isAccept,time);
		neighboursPreviousMaps.put(dest, time);
		
	
		return message;
	}

	private int getMyCurrentDistanceFromRoot() {
		return myCurrentDistanceFromRoot;
	}

	private void setMyCurrentDistanceFromRoot(int myCurrentDistanceFromRoot) {
		this.myCurrentDistanceFromRoot = myCurrentDistanceFromRoot;
	}

	private boolean sentMessage(int destID, Message message) {
		boolean sent = false;
	//	if(queueMap)
		//{
		BlockingQueue<Message> queue = neighboursQueueMaps.get(destID);
		sent= queue.offer(message);
		
	//	}
	//	else if (queueMap1)
		//{
			//BlockingQueue<Message> queue = neighboursQueueMaps1.get(destID);
			//sent= queue.offer(message);
			
		//}
		return sent;
	}

	@Override
	public String toString()
	{
StringBuffer str = new StringBuffer();
str.append("-------------------------------------------------------------------------\n");
		str.append(" Adjacency List of Node-ID " + this.getName() + " \n");
		
		
		
		if(!isRoot)
			str.append("My Parent " + myParent[0] + "\t" );
		else
			str.append(" I'm the Root \t");
		
		
		str.append(" My children ");
		for(int child: accepted_Children.keySet())
			str.append(child + "\t");
		str.append(" Distance from Root " + myCurrentDistanceFromRoot +"\n");
		str.append("-------------------------------------------------------------------------");
		return str.toString();
		
	}
}
