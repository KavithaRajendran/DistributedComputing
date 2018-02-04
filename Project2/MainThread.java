//package asynchronous;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.regex.Pattern;

/**		
* @author Kavitha Rajendran - kxr161830		
* 		   Rajkumar Panneer selvam - rxp162130 		
*		
* 		
* MainThread receives and validates the input, 		
* also provides the information regarding the neighbors and its ID.		
* 		
* Round management is done through Binary Semaphore, in which each node has its own semaphore and the Main Thread maintains all node's semaphore in a Map.		
* Semaphore tokens is given by the Main Thread to all the nodes, when it assures all the nodes completed the previous round.		
*		
* Once it receives termination signal from the Root node, it runs until all nodes are terminated.		
*		
*/
public class MainThread {

	private ConcurrentHashMap<String, BlockingQueue> neighboursMaps= new ConcurrentHashMap<String, BlockingQueue>();
	
	private ArrayList<Node> nodes = new ArrayList<Node>();
	
	private void addNode ( Node node)
	{
		nodes.add(node);
	}

	@SuppressWarnings("unchecked")
	public  BlockingQueue<Message> getProcessQueue(String ID) {
		
		return neighboursMaps.get(ID);
	}
	
	public void setProcessQueue(String id, BlockingQueue<Message> queue)
	{
		
		neighboursMaps.put(id, queue);
	}
	
	

	private Map<Integer, Semaphore> signalSemaphore = new HashMap<Integer, Semaphore>();

	private boolean isRoundStart = true;

	private int no_of_Nodes;
	
	public boolean isRoundStart() {
		return isRoundStart;
	}

	public void setRoundStart(boolean isRoundStart) {
		this.isRoundStart = isRoundStart;
	}

	private AtomicBoolean isFinalRound= new AtomicBoolean(false);

	public boolean isFinalRound() {
		return isFinalRound.get();
	}

	public void setFinalRound(boolean isFinalRound) {
		this.isFinalRound.set(isFinalRound);
	}

	
	private int currentRound=0;

	private AtomicInteger no_of_aliveNodes= new AtomicInteger();
	
	public void   decrement_aliveNodes() {
		 no_of_aliveNodes.decrementAndGet();
	}

	public int getCurrentRound() {
		return currentRound;
	}

	private void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}

	public static void main(String[] args) throws java.io.FileNotFoundException{

		MainThread mt = new MainThread();
		//int n = 9; // no of processes
		//int[] id = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };// 1-D array id of the
													// process
		int n=Integer.MAX_VALUE;
		int rootID=Integer.MAX_VALUE;
		List<String> splitedNodeId = new ArrayList<String>();
		List<List<String>> splitedWeight = new ArrayList<List<String>>();
		//File inputFile = new File("src/UserInput");
		File inputFile = new File(args[0]);
		Scanner input = new Scanner(inputFile);
		
		/** Parsing input file **/
		while(input.hasNextLine()){
			String line = input.nextLine();
			if(Pattern.matches("# number of nodes",line))
			{
				if(input.hasNextLine()){
					//n=Integer.parseInt(input.nextLine());
					n=input.nextInt();
					System.out.println("Total number of  nodes in the network:"+n);
				}
			}
			if(Pattern.matches("# id of the leader",line))
			{
				if(input.hasNextLine())
					//rootID = Integer.parseInt(input.nextLine());
					rootID=input.nextInt();
					System.out.println("root node is:"+rootID);
			}
			if(Pattern.matches("# node IDs",line))
			{
				if(input.hasNextLine())
				{
					String str = (input.nextLine()).trim();
					splitedNodeId = Arrays.asList(str.split("\\s+"));
				}
			}
			if(Pattern.matches("# graph in adjacency matrix form",line))
			{
				line = input.nextLine();
					while(input.hasNextLine()) {
						line = (input.nextLine()).trim();
						splitedWeight.add(Arrays.asList(line.split("\\s+")));
						//System.out.println("Weight matrix is"+splitedWeight);
				}
			}
		}
		//System.out.println("Weight matrix size:"+splitedWeight.size());
		input.close();
		int id[] = new int[splitedNodeId.size()];
		int index =0;
		for(String s : splitedNodeId){
			id[index]=Integer.parseInt(s);
			//System.out.println(id[index]);
			index++;
		}
		
		/** Input Validation **/
		
		//Check node id is given for all nodes
		if(!(n==id.length)) {
			System.out.println("Number of nodes & number of node ids - are NOT matching\nCannot proceed..");
			return;
		}
				
		//Check all node ids are unique
		boolean duplicate = false;
		for(int i=0; i<n; i++){
			for(int j=0; j<n; j++){
				if((i!=j)&&(id[i]==id[j])){
					duplicate = true;
					break;
				}
			}
		}	
		if(duplicate){
			System.out.println("Nodes should have unique ids\nCannot proceed..");
			return;
		}
		
		//Check root node id is present in nodeId array
		int flag = 0;
		for(int k=0; k<id.length;k++) {
			if(id[k]==rootID){
				System.out.println("Given root node is part of node list");
				flag = 1;
			}
		}
		if (flag == 0) {
			System.out.println("Given root node is NOT present in list of nodes\nCannot proceed..");
			return;
		}
		
	
		
		//Check Weight matrix is n*n
		int weightMatrix[][] = new int[n][n];
		if(splitedWeight.size() != n) {
			System.out.println("Weight matrix not matching with list of nodes "+splitedWeight.size() );
			return;
		}
		else {
			for (int i=0; i<n; i++){
				List<String> temp = new ArrayList<String>();
				temp = splitedWeight.get(i);
				//System.out.println("temp is:"+temp);
				if(temp.size()!=n) {
					System.out.println("Given weight matrix is NOT right\nCannot proceed..");
					return;
				}
				else {
					int innerIndex =0;
					for(String s : temp){
						//System.out.println("S:"+s);
						weightMatrix[i][innerIndex]=Integer.parseInt(s);
						//System.out.println(weightMatrix[i][innerIndex]);
						innerIndex++;
					}
					//System.out.println(weightMatrix[i]);
				}
			}
		}
		System.out.println("weightMatrix:");
		for(int a=0;a<n;a++) {
			for(int b=0; b<n; b++) {
				if(weightMatrix[a][b]!=weightMatrix[b][a]){		
					System.out.println("Weight matrix is not a symmetric Matrix");		
					return;		
				}
				System.out.printf(" %d ",weightMatrix[a][b]);
			}
			System.out.println("\n");
		}
		
		//Starting Thread
		for (int i = 0; i < id.length; i++) {
			Semaphore sem = new Semaphore(0);
			
			Node nodeThread = new Node( mt, new Integer(id[i]).toString(), sem, weightMatrix[i], id, id[i]==rootID);
			mt.addToSemaphoreMap(id[i], sem);
			nodeThread.start();
			mt.addNode(nodeThread);
		}
		mt.no_of_Nodes=n;
		mt.no_of_aliveNodes.set(n);
		mt.manageRound();
	}

	private void addToSemaphoreMap(int id, Semaphore s) {

		signalSemaphore.put(id, s);
	}

	private void manageRound() {
		int jobsDone = 0;
		int i = 0;
		while (!isFinalRound.get()) {
			jobsDone = 0;

			for (Integer key : signalSemaphore.keySet()) {
				Semaphore sem = signalSemaphore.get(key);
				jobsDone = jobsDone + sem.getQueueLength();
			}
			if (jobsDone == no_of_Nodes  && !isFinalRound.get()) {
			
				//System.out.println(" Round " + i);
				i++;
				setCurrentRound(i);
				for (Integer key : signalSemaphore.keySet()) {
					Semaphore sem = signalSemaphore.get(key);
					sem.release(1);
				}

			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {

			}

		}
		


//		
	int allDone=0;
	int aliveNodes=0;
		while(allDone!=no_of_Nodes)
		{
			allDone=0;
			aliveNodes=0;
			for(Node node : nodes)
			{
				if(node.isAlive())
				{
					aliveNodes++;
					
					
				}
				else{
					allDone++;
				
				}
				
			}
			
			if(aliveNodes==no_of_aliveNodes.get())
			{
				for(Node node : nodes)
				{
			Semaphore sem = signalSemaphore.get(node.getMyId());
			sem.release(1);}}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {

			}
		}
		
		System.out.println(" All done. Main Thread Exit");
		
		
	}

}
