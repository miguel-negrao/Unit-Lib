UMapOut {
	
	*kr { |channelsArray, map = true|
		ReplaceOut.kr( 
			UMap.busOffset + \u_mapbus.kr(0), 
			if( map ) { \u_spec.asSpecMapKr( channelsArray ) } { channelsArray };
		);
		Udef.buildUdef.numChannels = channelsArray.asCollection.size;
		Udef.addBuildSpec(
			ArgSpec(\u_mapbus, 0, PositiveIntegerSpec(), true ) 
		);
		Udef.addBuildSpec(
			ArgSpec(\u_spec, [0,1,\lin].asSpec, ControlSpecSpec(), true ) 
		);
	}
	
}