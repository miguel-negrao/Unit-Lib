/*
\lowPass

A resonant low-pass filter

freq: cutoff frequency.
rq: the reciprocal of Q. bandwidth / cutoffFreq
lag: smoothing time for the freq parameter.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \lowPass, { |freq = 440, rq = 1, lag = 0|
	var in;
	in = UIn.ar(0,1);
	in = BLowPass.ar( in, freq.lag(lag).clip(20,20000), rq );
	UOut.ar( 0, in );
})
	.category_( \filter )
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \rq, [ 0.01, 10, \exp, 0, 1 ].asSpec )
	.setSpec( \lag, [0,1,\lin].asSpec )