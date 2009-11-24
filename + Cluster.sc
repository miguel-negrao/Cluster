
+ Server{
	asCluster{
		^ClusterServer.new([this])
	}
}

+ Group{
	asClusterGroup{
		^ClusterGroup.fromArray([this])
	}
	
}

+ OSCBundle{
	asClusterBundle{
		^ClusterOSCBundle.fromArray([this])
	}
		
}

+ SynthDef{

	sendCluster{ |clusterServer|
		clusterServer.items.do{ |server|  this.send(server) };
	}
	
	laadCluster{ |clusterServer|
		clusterServer.items.do{ |server|  this.load(server) };
	}
	
	//unsyncronized
	playCluster{ |clustertarget,args,addAction=\addToTail|
		var synths = clustertarget.items.collect{ |target|
			target.postln;
			this.play(target,args,addAction).postln
		};
		^ClusterSynth.fromArray(synths);
		
		
	}


}

+ Array{
	//for a single array with n values corresponding to n servers
	asCluster{
		^ClusterArg(this)
	}
	
}

+ SyncCenter{

	*sendPosClusterBundle{ |delta = 1, clusterbundle, clusterserver|
		
		if(remoteCounts.size == servers.size){
			clusterbundle.sendPos(clusterserver,this.getSchedulingSampleCountSCluster(delta,clusterserver)) 
		}{
			"SyncCenter: Sorry, not synced yet".postln
		}

	}
	
	*getSchedulingSampleCountSCluster{ |delta = 1, clusterserver|
		
		var positions = clusterserver.items.collect{ |server|
			var serverIndex = servers.indexOf(server);
			this.getSchedulingSampleCount(delta,serverIndex);
		};
		^ClusterArg(positions)
	}
	
}
	

