Udef('moogVCF', { |freq #[ 20, 20000 ], modSpeed = 1, modShape = 1, res = 0.5|
	var in;
	in = UIn.ar(0,1);
	in = MoogVCF.ar( in, SinOsc.ar( modSpeed ).pow( modShape ).exprange( *freq ), res );
	UOut.ar( 0, in );
}, [ 
	ArgSpec('freq', [ 20.0, 20000.0 ], 
		RangeSpec(20, 20000, 0, inf, 'exp', 0, [20.0,20000.0 ], " Hz") 
	), 
	ArgSpec( 'modSpeed', 1, [0.1, 20, 'exp', 0, 1].asSpec ),
	ArgSpec( 'modShape', 1, [0, 1, 'lin', 0, 1].asSpec ),
	ArgSpec('res', 0.5, [0, 1, 'linear', 0, 0.1].asSpec ),
], 'filter')