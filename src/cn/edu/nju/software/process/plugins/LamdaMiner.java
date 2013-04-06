/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName LamdaMiner.java
 */
package cn.edu.nju.software.process.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.search.MultiThreadedSearcher;
import org.processmining.framework.util.search.NodeExpander;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

import cn.edu.nju.software.Models.EventLog;
import cn.edu.nju.software.Models.EventLogRelation;
import cn.edu.nju.software.Models.EventPair;
import cn.edu.nju.software.Models.Tuple;

/**
 * @Author xlx09
 * @Classname LamdaMiner
 * @Version 1.0.0
 * @Date 2013-3-22
 */
public class LamdaMiner implements NodeExpander<Tuple>{
	/**
     * the relation of the event log with post tasks
     */
    private EventLogRelation relation;

    private List<Integer> eventlist;

    @Plugin(
            name = "Lamda Miner", 
            parameterLabels = { "Input Event log with post tasks"}, 
            returnLabels = { "Output Processed Result" }, 
            returnTypes = { Petrinet.class }, 
            userAccessible = true, 
            help = "Input a event log with post tasks as the source"
    )
    @UITopiaVariant(
            affiliation = "software.nju.edu.cn", 
            author = "xlx09", 
            email = "xlx09@softeware.nju.edu.cn"
    )
    /**
     * @author xlx09
     * @param context
     * @param multisetList
     * @return
     * @return_typ Petrinet
     * @throws 
     * @version 1.0.0
     */
    public Petrinet doMining(UIPluginContext context, EventLog  log ) {
        this.relation = log.getRelation();
        this.relation.calRelation();
        //eventlist has all the event id。
        this.eventlist = new ArrayList<Integer>(log.getTaskNum());
        
        final Progress progress = context.getProgress();
		progress.setMinimum(0);
		progress.setMaximum(5);
		progress.setIndeterminate(false);
		
		final Stack<Tuple> stack = new Stack<Tuple>();
		
		// Initialize the tuples to the causal depencencies in the log
		for (EventPair<Integer, Integer> causal : relation.getCausalDependencies().keySet()) {
			if (progress.isCancelled()) {
				context.getFutureResult(0).cancel(true);
				return null;
			}
			if (!eventlist.contains(causal.getFirst()) || !eventlist.contains(causal.getSecond())) {
				continue;
			}
			Tuple tuple = new Tuple();
			tuple.leftPart.add(causal.getFirst());
			tuple.rightPart.add(causal.getSecond());
			tuple.maxRightIndex = eventlist.indexOf(causal.getSecond());
			tuple.maxLeftIndex = eventlist.indexOf(causal.getFirst());
			stack.push(tuple);
		}
		progress.inc();
		
		// Expand the tuples
		final List<Tuple> result = new ArrayList<Tuple>();

		MultiThreadedSearcher<Tuple> searcher = new MultiThreadedSearcher<Tuple>(this,
				MultiThreadedSearcher.BREADTHFIRST);

		searcher.addInitialNodes(stack);
		try {
			searcher.startSearch(context.getExecutor(), progress, result);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			context.getFutureResult(0).cancel(true);
			return null;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (progress.isCancelled()) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		// Add transitions 
		Map<String, Transition> class2transition = new HashMap<String, Transition>();
		Petrinet net = PetrinetFactory.newPetrinet("Petrinet from Eventlog, mined with LamdaMiner");
		context.getFutureResult(0).setLabel(net.getLabel());
		context.getFutureResult(1).setLabel("Initial Marking of " + net.getLabel());
		for (String eventname : log.getNameIdMap().keySet()) {
			Transition transition = net.addTransition(eventname);
			class2transition.put(eventname, transition);
		}
		progress.inc();
		
		int placeNum = 0;
		Map<Tuple, Place> tuple2place = new HashMap<Tuple, Place>();
		// Add places for each tuple
		for (Tuple tuple : result) {
			Place p = net.addPlace(tuple.toString());
			for (int eventClass : tuple.leftPart) {
				net.addArc(class2transition.get(eventClass), p);
			}
			for (int eventClass : tuple.rightPart) {
				net.addArc(p, class2transition.get(eventClass));
			}
			tuple2place.put(tuple, p);
		}
		progress.inc();

		Marking m = new Marking();
		// Add initial and final place
		Place pstart = net.addPlace("Start");
		for (int eventId : relation.getStartTraceInfo().keySet()) {
			String eventClass = log.getTasklist().get(eventId);
			net.addArc(pstart, class2transition.get(eventClass));
		}
		m.add(pstart);

		Place pend = net.addPlace("End");
		for (int eventId : relation.getEndTraceInfo().keySet()) {
			String eventClass = log.getTasklist().get(eventId);
			net.addArc(class2transition.get(eventClass), pend);
		}
		progress.inc();
	
		return net;
//    	int placeNum = 0;
//    	WFMultiset[] ms=multisetList.getMs();
//    	WFMining wfm = new WFMining();
//		wfm.mine(ms);
//		//生成起始活动和结束活动
//		Integer[] tis = wfm.Ti;
//		Integer[] tos = wfm.To;
//		//活动集，库所
//		String[] activitys = wfm.Tw;
//		WFPair[] pairs = wfm.Yw;
//		Petrinet resultNet = PetrinetFactory.newPetrinet("Output Petrinet");
//		List<Transition> transitions = new ArrayList<Transition>();
//		transitions.add(null);
//		for( int i = 1; i < activitys.length; i++ ){
//			Transition transition =resultNet.addTransition(activitys[i]);
//			transitions.add(transition);
//		}
//		//生成起始库所
//		for( int i = 0; i < tis.length; i++ ){
//			int index = tis[i];
//			Place place = resultNet.addPlace("p"+placeNum++);
//			resultNet.addArc( place,  transitions.get(index));
//		}
//		//连接库所left和right的变迁
//		for( int i = 0; i < pairs.length; i++ ){
//			Place place = resultNet.addPlace("p"+placeNum++);
//			WFPair tuple = pairs[i];
//			for( Integer leftIndex : tuple.left ){
//				resultNet.addArc( transitions.get(leftIndex), place);
//			}
//			for( Integer rightIndex : tuple.right ){
//				resultNet.addArc( place,  transitions.get(rightIndex));
//			}
//		}
//		//添加结束库所
//		for( int i = 0; i < tos.length; i++ ){
//			int index = tos[i];
//			Place place = resultNet.addPlace("p"+placeNum++);
//			resultNet.addArc( transitions.get(index), place );
//		}
//		return net;
    }

	/* (non-Javadoc)
	 * @see org.processmining.framework.util.search.NodeExpander#expandNode(java.lang.Object, org.processmining.framework.plugin.Progress, java.util.Collection)
	 */
	public Collection<Tuple> expandNode(Tuple toExpand, Progress progress,
			Collection<Tuple> unmodifiableResultCollection) {
		// TODO Auto-generated method stub
		Collection<Tuple> tuples = new HashSet<Tuple>();

		int startIndex = toExpand.maxLeftIndex + 1;
		for (int i = startIndex; i < eventlist.size(); i++) {

			if (progress.isCancelled()) {
				return tuples;
			}

			int toAdd = eventlist.get(i);

			if (canExpandLeft(toExpand, toAdd)) {
				// Ok, it is safe to add toAdd
				// to the left part of the tuple
				Tuple newTuple = toExpand.clone();
				newTuple.leftPart.add(toAdd);
				newTuple.maxLeftIndex = i;
				tuples.add(newTuple);
			}
		}

		startIndex = toExpand.maxRightIndex + 1;
		for (int i = startIndex; i < eventlist.size(); i++) {

			if (progress.isCancelled()) {
				return tuples;
			}

			int toAdd = eventlist.get(i);

			if (canExpandRight(toExpand, toAdd)) {
				// Ok, it is safe to add toAdd
				// to the right part of the tuple
				Tuple newTuple = toExpand.clone();
				newTuple.rightPart.add(toAdd);
				newTuple.maxRightIndex = i;
				tuples.add(newTuple);
			}

		}

		return tuples;
	}

	/* (non-Javadoc)
	 * @see org.processmining.framework.util.search.NodeExpander#processLeaf(java.lang.Object, org.processmining.framework.plugin.Progress, java.util.Collection)
	 */
	public void processLeaf(Tuple leaf, Progress progress, Collection<Tuple> resultCollection) {
		// TODO Auto-generated method stub
		synchronized (resultCollection) {
			Iterator<Tuple> it = resultCollection.iterator();
			boolean largerFound = false;
			while (!largerFound && it.hasNext()) {
				Tuple t = it.next();
				if (t.isSmallerThan(leaf)) {
					it.remove();
					continue;
				}
				largerFound = leaf.isSmallerThan(t);
			}
			if (!largerFound) {
				resultCollection.add(leaf);
			}
		}
	}
	
	private boolean canExpandLeft(Tuple toExpand, int toAdd) {
		// Check if the event class in toAdd has a causal depencendy 
		// with all elements of the rightPart of the tuple.
		for (int right : toExpand.rightPart) {
			if (!hasCausalRelation(toAdd, right)) {
				return false;
			}
		}

		// Check if the event class in toAdd does not have a relation 
		// with any of the elements of the leftPart of the tuple.
		for (int left : toExpand.leftPart) {
			if (hasRelation(toAdd, left)) {
				return false;
			}
		}

		return true;
	}

	private boolean canExpandRight(Tuple toExpand, int toAdd) {
		// Check if the event class in toAdd has a causal depencendy 
		// from all elements of the leftPart of the tuple.
		for (int left : toExpand.leftPart) {
			if (!hasCausalRelation(left, toAdd)) {
				return false;
			}
		}

		// Check if the event class in toAdd does not have a relation 
		// with any of the elements of the rightPart of the tuple.
		for (int right : toExpand.rightPart) {
			if (hasRelation(right, toAdd)) {
				return false;
			}
		}

		return true;
	}
	
	private boolean hasRelation(int from, int to) {
		if (from != to) {
			if (hasCausalRelation(from, to)) {
				return true;
			}
			if (hasCausalRelation(to, from)) {
				return true;
			}
		}
		if (relation.getParallelRelations().containsKey(new EventPair<Integer, Integer>(from, to))) {
			return true;
		}
		return false;

	}

	private boolean hasCausalRelation(int from, int to) {
		if (relation.getCausalDependencies().containsKey(new EventPair<Integer, Integer>(from, to))) {
			return true;
		}
		return false;

	}
}
