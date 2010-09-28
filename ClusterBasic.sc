// ©2009 Miguel Negr‹o
// GPLv2 - http://www.gnu.org/licenses/old-licenses/gpl-2.0.html


ClusterBasic {	
	var <items;
	
	//no arguments
	prCollectNoArgs{ |selector|			
		var return = items.collect(_.tryPerform(selector));
		"prcollectsimple";	
		^this.getReturnClusterObject(return);
	}
	
	//with arguments
	prCollectWithArgs{ |selector,argArray|		
		var return = items.collect{ |item,i| item.tryPerform(*([selector]++argArray[i]))};
		"!!!prCollectWithArgs";		
		^this.getReturnClusterObject(return);
	}		
	
	getReturnClusterObject{ |returnArray|
		var clusterClasses;
		if((returnArray[0].class == this.oclass) || (returnArray[0] == nil)){
			^this
		}{
			clusterClasses = ClusterBasic.allSubclasses;
			clusterClasses.remove(this.class);
			clusterClasses = clusterClasses.collect(_.oclass); 
			if(clusterClasses.includes(returnArray[0].class)){
				^("Cluster"++returnArray[0].class.asCompileString).compile.value.fromArray(returnArray)
			}{
				^ClusterArg(returnArray)
			}
		} 		
	}
		
	//expand a set of arguments into an array of size items.size
	prExpandArgs{ |args|
		("prExpand: "++args);	
		^ClusterBasic.expandArray(args,this);
	}

	prExpandCollect{ |selector,args|		
		if(args.size == 0){
			"args nil";
			^this.prCollectNoArgs(selector)
		}{	
			"args not nil";
			^this.prCollectWithArgs(selector,this.prExpandArgs(args))
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
		if(this.respondsTo(selector)){
			^this.prExpandCollect(selector,args)
		}{
			//if method not implemented than call Object's doesNotUnderstand
			^super.doesNotUnderstand(*([selector]++args));
		}
		
	} 
	
	respondsTo{ |selector|
		^items[0].respondsTo(selector)	
	}
	
	oclass{ ^this.class.oclass }
	
	dopost{ "items:".postln; items.do(_.postln) }
	
	printOn { arg stream;
		stream << this.class.asString << items ;
	}	
	
	// Class methods wrapping
	*fromArray{ |array|
		if(array.collect{ |x| x.classÊ}.as(Set).size>1){
			Error("ClusterBasic: "++array++" - Items should be all of the same class").throw
		};
	 	^super.newCopyArgs(array);
	 }
	 
	 *new{ |array|
	 	^this.fromArray(array);
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
	
	// is there a case beyond getters and setters for classvars that a class method is called with no arguments and no defaults ?
	*prCollectSimple{ |selector,referenceCluster|
		("prCollectSimple - this class: "++this.class);
		^ClusterArg([this.oclass.perform(selector)]);
	}
	
	*prCollectWithArgs{ |selector,argArray,referenceCluster|
		("prCollectWithArgs - this class: "++this.class);
		^referenceCluster.items.collect{ |item,i| this.oclass.perform(*([selector]++argArray[i]))};
	}	
	
	*prExpandCollect{ |selector,args|
		var  referenceCluster;
		("prExpandCollect - this class: "++this.class);
		if(args.size == 0){
			"*prExpandCollect - no arguments".postln;
			^this.prCollectSimple(selector)
		}{	
			referenceCluster = this.searchArrayForCluster(args);
			^this.prCollectWithArgs(selector,this.expandArray(args,referenceCluster),referenceCluster)
		}	
	}	

	*doesNotUnderstand{ arg selector...args;
		var cluster;
		("doesNotUnderstand - "++selector++" this class: "++this.class++" args is "++args).postln;
		if(this.oclass.class.findRespondingMethodFor(selector).notNil){
			^super.newCopyArgs(this.prExpandCollect(selector,args))
		}		
	} 
	
	*oclass{ 	^Object }
	
	clusterfy{ ^this }
	
	deCluster{ ^items[0] }
	
	clApplyF{ |func|
		^ClusterArg(items.collect{ |item| { func.(item) } })
	}	
	
	//explicit overloading of methods from Object
	
	changed { arg what ... moreArgs;
		^this.doesNotUnderstand(*([\changed,what]++moreArgs));
	}
	
	addDependant { arg dependant;
		^this.doesNotUnderstand(\addDependant,dependant);
	}
	
	removeDependant { arg dependant;
		^this.doesNotUnderstand(\removeDependant,dependant);
	}
	release {
		^this.doesNotUnderstand(\release)
	}
	releaseDependants {
		^this.doesNotUnderstand(\releaseDependants)
	}	
}

+ Object {

	clusterfy{
		if(ClusterBasic.allSubclasses.collect(_.oclass).includes(this.class)){
			^("Cluster"++this.class.asCompileString).compile.value.fromArray([this])	
		}{
			Error("object class is not compatible with Cluster server classes")
		}
	}


}