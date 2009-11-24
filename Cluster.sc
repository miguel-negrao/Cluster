// ©2009 Miguel Negr‹o
// GPLv2 -http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
// library to use a synth or family of related synths playing simetrically in multiple servers/computers.
// usefull for multicomputer spatialization systems.

//when returning values the classes give out a ClusterArg, which is then recognized by the other classes as a valid argument to expand. 

ClusterArg : ClusterBasic{

	*new{Ê|array|
		^super.newCopyArgs(array)
	}
	
	oclass{ ^ClusterArg }
	
	doesNotUnderstand{ arg selector...args; 
		("ClusterArg is dumb, it doesn't understand "++selector) 
		^this.prExpandCollect(selector,args)
		
	}
	

}
 
ClusterServer : ClusterBasic{

	*oclass{ ^ Server }

	//different from Server
	*new{ |servers|
		^super.newCopyArgs(servers)
	}	
	
	asClusterGroup{
		^ClusterGroup.fromArray(this.prCollect(\asGroup).items)
	
	}
	
	asClusterServer{
		^this
	}

	addToSyncCenter{
		items.do(SyncCenter.add(_))
	}		

}
	

ClusterGroup : ClusterBasic{ 

	var clusterGroup;
	
	*oclass{ ^Group }
	
	*new { arg target, addAction=\addToHead;	
		^this.doesNotUnderstand(\basicNew,target, addAction).init(target)
	}
	
	*basicNew { arg server, nodeID;
		^this.doesNotUnderstand(\basicNew,server, nodeID).init(server)	}
	
	init{ |target|
		clusterGroup = target;
	}
	
	newMsg { arg clustertarget, addAction = \addToHead;	
		^this.doesNotUnderstand(\newMsg,clustertarget, addAction);
	}
	
	free { arg sendFlag=true;
		this.doesNotUnderstand(\free,sendFlag)
	}
	
	clusterServer{		
		^ClusterServer(items.collect{ |group| group.server })
	}
	
	asClusterGroup{ ^this }		

}

ClusterBus : ClusterBasic{

	var <clusterServer, <numChannels;
	
	*oclass{ ^ Bus }
	
	//explicitly implemented because of default number of channels
	*control { arg clusterServer,numChannels=1;		
		^super.control(clusterServer,numChannels).init(clusterServer,numChannels);
	}
		
	*audio { arg clusterServer,numChannels=1;		
		^super.audio(clusterServer,numChannels).init(clusterServer,numChannels);
	}
	
	init{ |aclusterServer,anumChannels|
		clusterServer = aclusterServer;
		numChannels = anumChannels
	}
	
	free{
		this.doesNotUnderstand(\free)
	}

}

ClusterBuffer : ClusterBasic{

	*new { arg server, numFrames, numChannels, bufnum;
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\new,server, numFrames, numChannels, bufnum)
	}
	
	*alloc { arg server, numFrames, numChannels = 1, completionMessage, bufnum;
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\alloc,server, numFrames, numChannels, completionMessage, bufnum)
	}
	
	*allocConsecutive { |numBufs = 1, server, numFrames, numChannels = 1, completionMessage,bufnum|
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\allocConsecutive,numBufs, server, numFrames, numChannels, completionMessage,bufnum)
	}
	
	*read { arg server,path,startFrame = 0,numFrames, action, bufnum;
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\read,server,path,startFrame ,numFrames, action, bufnum)
	}
	
	*readChannel { arg server,path,startFrame = 0,numFrames, channels, action, bufnum;
		server = server ? ClusterServer([Server.default]);
		^this.doesNotUnderstand(\readChannel,server,path,startFrame,numFrames, channels, action, bufnum)
	}
	
	read { arg argpath, fileStartFrame = 0, numFrames, bufStartFrame = 0, leaveOpen = false, action;
		this.doesNotUnderstand(\read,argpath, fileStartFrame, numFrames, bufStartFrame, leaveOpen, action)
	}			

	readChannel { arg argpath, fileStartFrame = 0, numFrames, bufStartFrame = 0, leaveOpen = false, channels, action;
		this.doesNotUnderstand(\readChannel,argpath, fileStartFrame, numFrames, bufStartFrame, leaveOpen, channels, action)
	}
	//implementation of methods that are common to Object and Buffer and thus doesNotUnderstand does not pick up
	//Buffer.methods.collect(_.name).asSet & Object.methods.collect(_.name).asSet
	free{
		this.doesNotUnderstand(\free)
	}
	
	numChannels{
		this.doesNotUnderstand(\numChannels)
	}	
	
	//for the methods not here, the user needs to provide the defaults himself
	
	*oclass{ ^ Buffer }

}


ClusterOSCBundle : ClusterBasic{

	*oclass{ ^ OSCBundle }
		
	*new{ |clusterServer|
		var items = clusterServer.items.collect{ OSCBundle.new };
		^super.newCopyArgs(items,clusterServer);
		
	}
	
	asClusterBundle{
		^this
	}
	
	dopost{
	
		items.do{ |bundle,i|
			("Bundle "++i++":");
			bundle.messages.dopost;
		}
	}

	
}


ClusterSynth : ClusterBasic{
	var <clusterGroup, <clusterServer;	
	
	*oclass{ ^ Synth }

	init{ |aclusterGroup, aclusterServer|
		clusterGroup = aclusterGroup;
		clusterServer = aclusterServer;
	}
	
	*new { arg defName, args, target, addAction=\addToHead;
		switch(target.class)
			{ClusterServer}{ ^this.doesNotUnderstand(\basicNew,defName, target.server).init(nil,target).prPlay(target,args,addAction) }
			{ClusterGroup}{ ^this.doesNotUnderstand(\basicNew,defName, target.server).init(target,nil).prPlay(target,args,addAction) };
			
	}
	
	prPlay{ |target,args,addAction|	
		var bundle;
		if(SyncCenter.ready){
				bundle = ClusterOSCBundle.new(target);
				bundle.add(this.newMsg(target,args,addAction));
				bundle.dopost;
				SyncCenter.sendPosClusterBundle(1,bundle,target.clusterServer)
		}	
	}
	
	*basicNew { arg defName, server, nodeID;
		^this.doesNotUnderstand(\basicNew,defName, server, nodeID).init(nil,server)
	}
	
	*newPaused { arg defName, args, target, addAction=\addToHead;
		switch(target.class)
			{ClusterServer}{ ^this.doesNotUnderstand(\newPaused,defName, args, target, addAction).init(nil,target) }
			{ClusterGroup}{ ^this.doesNotUnderstand(\newPaused,defName, args, target, addAction).init(target,nil) };
		
	}
	
	//unsynchronized play of the synths
	*newTest{ arg defName, args, target, addAction=\addToHead;
		var synths;
			
		synths = target.items.collect{ |target,i|
			Synth(this.prDefName(defName,i),args, target, addAction);
						
		};
		
		switch(target.class)
			{ClusterServer}{ ^super.newCopyArgs(synths,nil,target) }
			{ClusterGroup}{ ^super.newCopyArgs(synths,target,nil) };
	}	
	
	//methods with defaults for args
	run { arg flag=true;
		this.doesNotUnderstand(\run,flag)
	}
	
	runMsg { arg flag=true;
		^this.doesNotUnderstand(\runMsg,flag)
	}
	
	newMsg { arg target, args, addAction = \addToHead;
		^this.doesNotUnderstand(\newMsg,target, args, addAction)
	}
	
	free { arg sendFlag=true;
		this.doesNotUnderstand(\free,sendFlag)
	}
	
	*grain { arg defName, args, target, addAction=\addToHead;
		target = target ? ClusterServer([Server.default]);
		this.doesNotUnderstand(\grain,defName, args, target, addAction)
	}
	
	//not implemented in original Synth
	registerNodeWatcher{
		items.do{ |synth| NodeWatcher.register(synth)}
	}	
	
	unregisterNodeWatcher{
		items.do(NodeWatcher.unregister(_))
	}
	
	*prDefName{ |defName,i|
		if(defName.isString){
			^defName.asSymbol
		}{
			^if(defName.size==0){defName}{defName[i] }
		
		}
	
	}
	
	*fromArray{ |synths|
		^super.newCopyArgs(synths,
			ClusterGroup.fromGroups(synths.collect(_.group)),
			ClusterServer(synths.collect(_.server))
		)
	}


}

ClusterSynthDef : ClusterBasic{

	*oclass{ ^ SynthDef }
		
	*new { arg name, ugenGraphFuncs, rates, prependArgs, variants, clusterdata;
	
		/*var synthDefs = ugenGraphFuncs.collect{ |ugenGraphFunc,i|
			SynthDef.new(if(name.size==0){name}{name[i]}, ugenGraphFunc, rates, prependArgs, variants, clusterdata)
			};
			
		^super.newCopyArgs(synthDefs);
		*/
		^this.doesNotUnderstand(\new,name, ugenGraphFuncs, rates, prependArgs, variants, clusterdata)
	}

	
	
}


	
//still Needs fixing	
ClusterMonitor : ClusterBasic{

	*oclass{ ^ Monitor }
	
	*new{ |clusterServer|
		^super.newCopyArgs(clusterServer.items.collect{ Monitor.new});
	}
	
	playNToBundle{ |bundle, argOuts, argAmps, argIns, argVol, argFadeTime, inGroup, addAction, defName="system_link_audio_1"|
				
		argOuts = argOuts ? items.collect{ |monitor| (0..monitor.ins.size-1) }.asCluster;
		argAmps = argAmps ? items.collect{ |monitor| monitor.amps }.asCluster;
		argIns = argIns ? items.collect{ |monitor| monitor.ins }.asCluster;
		argFadeTime =  argFadeTime ? items.collect{ |monitor| monitor.fadeTime }.asCluster;
		
		^this.doesNotUnderstand(\playNToBundle,bundle, argOuts, argAmps, argIns, argVol, argFadeTime, inGroup, addAction, defName)
		
	}
	
	playToBundle{Ê|bundle, fromIndex, fromNumChannels=2, toIndex, toNumChannels, inGroup, multi = false, volume, inFadeTime, addAction|
		this.doesNotUnderstand(\playToBundle,bundle, fromIndex, fromNumChannels, toIndex, toNumChannels, inGroup, multi, volume, inFadeTime, addAction)
	}
	
	play { arg fromIndex, fromNumChannels=2, toIndex, toNumChannels,target, multi=false, volume, fadeTime=0.02, addAction;
		^this.doesNotUnderstand(\play,fromIndex, fromNumChannels, toIndex, toNumChannels,target, multi, volume, fadeTime, addAction)
	}
	
	newGroupToBundle { arg bundle, target, addAction=(\addToTail);
		this.doesNotUnderstand(\newGroupToBundle,bundle, target, addAction)
	}

}




