// ©2009 Miguel Negr‹o
// GPLv2 - http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

// class to apply methods to all elements of the items array
// all items should be of type oclass

Cluster{

	var <items, <oclass;
	
	//simple do and collect without arg not array
	prDo{ |selector,args|
	
		items.do(_.tryPerform(*([selector]++args)));
	
	}
	
	prCollect{ |selector,args|
		
		var return = items.collect(_.tryPerform(*([selector]++args)));
		
		if(return[0].class == this.oclass){
			^this
		}{
			^ClusterArg(return)
		}
	
	}
	
	prCollectSimple{ |selector|
		
		var return = items.collect(_.tryPerform(selector));
		
		"prcollectsimple";	

		if(return[0].class == this.oclass){
			^this
		}{
			^ClusterArg(return)
		}
	}
		
	//simple do and collect with arg an array os size items.size
	prDoArray{ |selector,argArray|
	
		items.do{ |item,i| item.tryPerform(*([selector]++argArray[i]))}; 
	
	}
	
	prCollectArray{ |selector,argArray|
		
		var return = items.collect{ |item,i| item.tryPerform(*([selector]++argArray[i]))};
		"!!!prCollectArray";
		
		if((return[0].class == this.oclass) || (return[0] == nil)){
			^this
		}{
			^ClusterArg(return)
		} 
	
	}	
	
		
	//expand a set of arguments into an array of size items.size

	prExpand{ |args|
		("prExpand: "++args);	
		^Cluster.expandArray(args,this);
	}
	
	
	prExpandDo{ |selector,args|
	
		this.prDoArray(selector,this.prExpand(args))
	
	}
	
	prExpandCollect{ |selector,args|
	
		if(args.isNil){
			"args nil";
			^this.prCollectSimple(selector)
		}{	
			"args not nil";
			^this.prCollectArray(selector,this.prExpand(args))
		}
	
	}
	
	prCheckSize{ |clusterobject|
		if(clusterobject.items.size != items.size){
			Error("Cluster sizes mismatch. ClusterObject with size "++clusterobject.items.size++
			" vs ClusterObject with size "++items.size++" \n "++[clusterobject.items,items]).throw
		}
	}
	
	//default implementation of collected classes.
	// will work, but it's not possible to pass arguments by name. e.g. busnum: 3
	doesNotUnderstand{ arg selector...args;
		("does not understand- base class "++this.class++" selector "++selector);
		if(oclass.findRespondingMethodFor(selector).notNil){
			^this.prExpandCollect(selector,args)
		}
	} 
		
	dopost{ "items:".postln; items.do(_.postln) }
	
	printOn { arg stream;
		stream << oclass.asString << "(" << items << ")";
	}
	
	
	// Class methods wrapping
	*fromArray{ |array,oclass|
	 	^super.newCopyArgs(array,array[0].class);
	 }
	 
	 *new{ |array,oclass|
	 	^this.fromArray(array,array[0].class);
	 }
	 
	*expandArray{ |array,clusterObject|
	
		var recursF1,recursF2,finalF;
		
		recursF1 = { |array,clusterobject|
			array.collect{ |item|
				if(item.isArray && item.isString.not){
					recursF1.(item,clusterobject)
				}{
					if(item.class.superclasses.includes(ClusterBasic)){
						clusterObject.prCheckSize(item);
						switch(item.class)
							{ClusterBus}{ item.index }
							{ClusterBuffer}{ item.bufnum }
							{item}
					}{
						ClusterArg(clusterobject.items.collect{ item })
					}
				}
			}
		};	
				
		recursF2 = { |array,i|
			array.collect{ |item|
				if(item.isArray && item.isString.not){
					"is array";
					recursF2.(item,i)
				}{
					"is clusterarg";
					item.items[i]
				}
			}
		}; 
		
		finalF = { |array,clusterobject|
			clusterobject.items.collect{ |adas,i|
				("line "++i);
				recursF2.(array,i)
			}};
		
		^finalF.(recursF1.(array,clusterObject),	clusterObject)
	}
	
	*searchArrayForCluster{ |array|

		array.do{ |item|
			if(item.class.superclasses.includes(ClusterBasic)){
				^item
			}{
				if(item.isArray && item.isString.not){
					^this.searchArrayForCluster(item)
				}				
				
			}
		};
		Error("arguments must have at least one Cluster class instance").throw
	}

}
























