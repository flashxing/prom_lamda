/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName LamdaMiner.java
 */
package cn.edu.nju.software.process.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

import cn.edu.nju.software.Models.EventLog;
import cn.edu.nju.software.Models.EventLogRelation;
import cn.edu.nju.software.Models.Tuple;
import cn.edu.nju.software.Models.WFMining;
import cn.edu.nju.software.Models.WFMultiset;
import cn.edu.nju.software.Models.WFPair;

/**
 * @Author xlx09
 * @Classname LamdaMiner
 * @Version 1.0.0
 * @Date 2013-3-22
 */
public class LamdaMiner {
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
        this.eventlist = new ArrayList<Integer>(log.getTasklist().size());
        
        final Progress progress = context.getProgress();
		progress.setMinimum(0);
		progress.setMaximum(5);
		progress.setIndeterminate(false);
		
		final Stack<Tuple<Integer> > stack = new Stack<Tuple<Integer> >();
		
		// Initialize the tuples to the causal depencencies in the log
		for (Pair<Integer, Integer> causal : relation.getCausalDependencies().keySet()) {
			if (progress.isCancelled()) {
				context.getFutureResult(0).cancel(true);
				return new Object[] { null };
			}
			if (!eventClasses.contains(causal.getFirst()) || !eventClasses.contains(causal.getSecond())) {
				continue;
			}
			Tuple tuple = new Tuple();
			tuple.leftPart.add(causal.getFirst());
			tuple.rightPart.add(causal.getSecond());
			tuple.maxRightIndex = eventClasses.indexOf(causal.getSecond());
			tuple.maxLeftIndex = eventClasses.indexOf(causal.getFirst());
			stack.push(tuple);
		}
		progress.inc();
    	int placeNum = 0;
    	WFMultiset[] ms=multisetList.getMs();
    	WFMining wfm = new WFMining();
		wfm.mine(ms);
		//生成起始活动和结束活动
		Integer[] tis = wfm.Ti;
		Integer[] tos = wfm.To;
		//活动集，库所
		String[] activitys = wfm.Tw;
		WFPair[] pairs = wfm.Yw;
		Petrinet resultNet = PetrinetFactory.newPetrinet("Output Petrinet");
		List<Transition> transitions = new ArrayList<Transition>();
		transitions.add(null);
		for( int i = 1; i < activitys.length; i++ ){
			Transition transition =resultNet.addTransition(activitys[i]);
			transitions.add(transition);
		}
		//生成起始库所
		for( int i = 0; i < tis.length; i++ ){
			int index = tis[i];
			Place place = resultNet.addPlace("p"+placeNum++);
			resultNet.addArc( place,  transitions.get(index));
		}
		//连接库所left和right的变迁
		for( int i = 0; i < pairs.length; i++ ){
			Place place = resultNet.addPlace("p"+placeNum++);
			WFPair tuple = pairs[i];
			for( Integer leftIndex : tuple.left ){
				resultNet.addArc( transitions.get(leftIndex), place);
			}
			for( Integer rightIndex : tuple.right ){
				resultNet.addArc( place,  transitions.get(rightIndex));
			}
		}
		//添加结束库所
		for( int i = 0; i < tos.length; i++ ){
			int index = tos[i];
			Place place = resultNet.addPlace("p"+placeNum++);
			resultNet.addArc( transitions.get(index), place );
		}
		return resultNet;
    }
}
