/*
\pulse

A band-limited pulse wave generator.
http://en.wikipedia.org/wiki/Sawtooth_wave

freq: frequency (Hz)
width: relative width (0-1) of the pulse. 0.5 means a square wave.
amp: amplitude (0-1)
lag: smoothing time for all parameters

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \pulse, { |freq = 440, width = 0.5, amp = 0.1, lag = 0|
	UMixOut.ar( 0, Pulse.ar( freq.lag( lag ), width.lag( lag ), amp.lag( lag ) ), 0, true ) 
} ).category_( \oscillator )
	.setSpec( \freq, FreqSpec( 2, 20000 ) )
	.setSpecMode( \amp, \normal )
	.setSpecMode( \width, \normal )
	.setSpec( \lag, [0,1,\lin].asSpec );