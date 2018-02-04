import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * 
 * 
 * Node thread is started by main thread, waits until Main Thread releases it's semaphore token (By this way, it gets synchronized with other nodes).
 * Once it acquires the token, it starts execution depending on the role - root/non-root
 * Node has two inboxQueues myInbox, myInbox1 - which it reads both queues alternatively in every round.
 * Also it writes to the neighbor node's queues, alternatively in every round ( if it has to sent message).
 * 
 * @author Kavitha Rajendran - kxr161830
 * 		   Rajkumar Panneer selvam - rxp162130 
 * 
 * 
 *  */
public class Node extends Thread {

	private HashMap<Integer, Integer> neighboursWeightMaps = new HashMap<Integer, Integer>();

	private HashMap<Integer, BlockingQueue<Message>> neighboursInboxMaps = new HashMap<Integer, BlockingQueue<Message>>();
	
	private HashMap<Integer, BlockingQueue<Message>> neighboursInboxMaps1 = new HashMap<Integer, BlockingQueue<Message>>();

	/**
	 * Binary semaphore to manage rounds.
	 */
	private Semaphore signalSemaphore;

	private BlockingQueue<Message> myInbox = new LinkedBlockingQueue<Message>();

	private BlockingQueue<Message> myInbox1 = new LinkedBlockingQueue<Message>();
	
	private MainThread mainThread;

	private boolean isRoot;

	private int no_Of_Neighbour=0;
	
	private int no_of_Explore_Sent = 0;

	private Map<Integer, Integer> accepted_Children = new HashMap<Integer, Integer>(); // children NodeId,
	// distance;

	private int[] myParent = { -1, -1 }; // NodeId, distance;

	private int myCurrentDistanceFromRoot = Integer.MAX_VALUE;

	private int myId;

	public int getMyId() {
		return myId;
	}

	private int currentRound = 0;
	
	
	private boolean queueMap1; // if true , write to the inbox of neighbour. and read from my inbox1

	private boolean queueMap; // if true , write to the inbox1 of neighbour. and read from my inbox

	private boolean isTerminate;

	public Node(MainThread mt, String id, Semaphore mySemaphore,
			int[] myNeighbourMatrix, int[] idMatrix, boolean isRoot) {

		super(id);

		signalSemaphore = mySemaphore;
		
		mainThread = mt;
		this.isRoot = isRoot;
		myId = Integer.parseInt(id);

		for (int i = 0; i < myNeighbourMatrix.length; i++) {
			if ((myNeighbourMatrix[i] != -1)) {
				
				neighboursWeightMaps.put(idMatrix[i], myNeighbourMatrix[i]);
				no_Of_Neighbour++;
				
			}
		}
		mainThread.setProcessQueue((id), myInbox);

		mainThread.setProcessQueue((id)+"-", myInbox1);
	}

	public void run() {
		while (!isTerminate) {
			try {
				
				signalSemaphore.acquire();
				
				currentRound++;
				if(currentRound==1)
				{
					getNeighbourInboxQueues();
				}
				
				
				if(queueMap)
				{
				queueMap1=true;
				queueMap=false;
				}
				else
				{
					queueMap=true;
					queueMap1=false;
				}

				if (isRoot) {
					executeRoot();
				} else {
					executeNonRoot();
				}


			} catch (InterruptedException e) {

				System.out.println(" Node-ID " + this.getName()
						+ " interrupted. ");
			}

		}
		mainThread.decrement_aliveNodes();
		System.out.println(this);

	}

	private void getNeighbourInboxQueues() {

		for (Integer neighbourID : neighboursWeightMaps.keySet()) {
			neighboursInboxMaps.put(neighbourID,
					mainThread.getProcessQueue(neighbourID.toString()));
			neighboursInboxMaps1.put(neighbourID,
					mainThread.getProcessQueue(neighbourID.toString() +"-"));
			
		}

	}

	private void executeNonRoot() {
		checkInbox();
	}

	private void executeRoot() {
		myCurrentDistanceFromRoot=0;
		if (currentRound == 1) {
			sentExplore();

		}
		checkInbox();
	}

	private void sentExplore() {

		for (int dest : neighboursWeightMaps.keySet()) {
			Message message=null;
			if(isRoot && dest!=myId)
			{
				//Root
			 message = constructExploreMessage(myId, dest, 0);
			 accepted_Children.put(dest, neighboursWeightMaps.get(dest));

			 no_of_Explore_Sent++;
			}
			else
			{
				// Non root
				if(myParent[0]!=dest && myId!=dest)
				{
				 message = constructExploreMessage(myId, dest, myCurrentDistanceFromRoot);
				
				 accepted_Children.put(dest, neighboursWeightMaps.get(dest) + myCurrentDistanceFromRoot);
				 no_of_Explore_Sent++;
				}
			}
			
			while (message!=null && !sentMessage(dest, message)) {
				// try again until message is sent successfully;
			//	System.out.println(" Sending Explore to -- " + message.getDestinationID() + " From " + message.getSourceID());

			}
		}
	}

	private void checkInbox() {
		
		Queue<Message> queue = new LinkedList<Message>();
		if(!queueMap) {	
			myInbox.drainTo(queue);
		}
		else
		{
			myInbox1.drainTo(queue);
		}
	
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
					
					if(myCurrentDistanceFromRoot > dist + neighboursWeightMaps.get(message.getSourceID()) )
					{
						myCurrentDistanceFromRoot= dist + neighboursWeightMaps.get(message.getSourceID());
						updateParent(message);
						sentExplore();
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
					sentTerminateToMyChildren();	
					
				} else if (no_of_Explore_Sent==0) // if all reply received , accept my current parent.
				{ 
								
					sentMessage(
							myParent[0],
							constructReplyMessage(myId,
									myParent[0], true));
				}
				
				if(!((ReplyMessage) message).isAccepted()) 
				{
				
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
				sentTerminateToMyChildren();
			}
		}
	}

	private void sentTerminateToMyChildren() {
		
		for (int dest : accepted_Children.keySet()) {		
			sentMessage(
					dest,
					new TerminateMessage(myId,
							dest));
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

	private Message constructExploreMessage(int source, int dest, int dist) {
		Message message = new ExploreMessage(source, dest, dist);
		return message;
	}

	private Message constructReplyMessage(int source, int dest, boolean isAccept) {
		Message message = new ReplyMessage(source, dest, isAccept);
		return message;
	}

	private boolean sentMessage(int destID, Message message) {
		boolean sent = false;
		if(queueMap)
		{
		BlockingQueue<Message> queue = neighboursInboxMaps.get(destID);
		sent= queue.offer(message);
		
		}
		else if (queueMap1)
		{
			BlockingQueue<Message> queue = neighboursInboxMaps1.get(destID);
			sent= queue.offer(message);
			
		}
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
