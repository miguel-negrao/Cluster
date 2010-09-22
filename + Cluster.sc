
+ Server{
	asClusterServer{
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
			this.play(target,args,addAction)
		};
		^ClusterSynth.fromArray(synths);
		
		
	}


}

+ Array{
	//for a single array with n values corresponding to n objects
	asClusterArg{
		^ClusterArg(this)
	}
	
	asCluster{
		^Cluster(this)
	}
	
}
