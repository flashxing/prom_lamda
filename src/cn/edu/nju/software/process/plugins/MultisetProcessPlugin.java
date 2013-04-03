package cn.edu.nju.software.process.plugins;

import java.util.ArrayList;
import java.util.List;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

import cn.edu.nju.software.Models.WFMining;
import cn.edu.nju.software.Models.WFMultiset;
import cn.edu.nju.software.Models.WFPair;



/**
 * @Author xlx09
 * @Classname MultisetProcessPlugin
 * @Version 1.0.0
 * @Date 2013-3-20
 * @Todo This class is a Plugin in the Prom.Get the input
 * from Multiset and output the Petrinet.
 */
public class MultisetProcessPlugin {

	    /**
	     * no classVar
	     */


        @Plugin(
                name = "Lamuda Miner Process Multiset Plugin", 
                parameterLabels = { "Input Multiset Eventlog"}, 
                returnLabels = { "Output Processed Result" }, 
                returnTypes = { Petrinet.class }, 
                userAccessible = true, 
                help = "Input a multiset eventlog as the source"
        )
        @UITopiaVariant(
                affiliation = "njusoftware", 
                author = "andylee", 
                email = "my email"
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
        public Petrinet processMultiset(UIPluginContext context, MultisetList  multisetList ) {
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

