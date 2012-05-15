

UMapped {

	currentValue {  } //output current value
	
	startMapping { |unit, key|
	
	}
	
	stopMapping { |unit, key|
	
	}
	
	asUMapped{ }

}

FPSignalUMapped : UMapped {
	var <signal, <useSpec, <func;
	
	*new{ |signal, useSpec= true| 
		^super.newCopyArgs( signal, useSpec )		
	}
	
	currentValue { ^signal.now }
	
	startMapping { |unit, key|
		func !? { |f| signal.stopDoing(f) };
		if( useSpec ) {
			func = { |v| unit.mapSet(key, v) }
		} {
			func = { |v| unit.set(key, v) }
		};
		signal.do(func);
	}
	
	stopMapping { |unit, key|
		"stop Mapping ".postln;
		signal.stopDoing(func);
		func = nil;
	}
}

AutomationUMapped : UMapped {
	//public vars
	var <timesValues, <delta;
	//private vars
	var key, func, player, controller;
	
	*new{ |array, delta = 0.1| ^super.newCopyArgs(array, delta) }
	
	currentValue { ^timesValues[0][1] }

	startMapping { |unit, unitkey|
		unit.set(unitkey, this.currentValue );
		key = unitkey;
		controller = SimpleController(unit)
			.put(\start, { "starting".postln; this.unitStarted(unit) })
			.put(\end, { "stoping".postln; this.unitStopped });		
	}
	
	stopMapping { |unit, key|
		controller.remove;
		player.stopDoing(func);
	}
	
	unitStarted { |unit|
		//player = EventPlayerES(timesValues);
		var times, values, interpolateFunc;
		#times, values = timesValues.flop;
		interpolateFunc = { |t| 
			values.blendAt( times.indexInBetween(t) )
		};
		player = interpolateFunc <%> TimerES(delta, times.last);
		func = { |v| unit.set(key, v) };
		player.do(func);
	}
	
	unitStopped { |unit| player.stopDoing(func) }
}

/*
AutomationUMapped2 : UMapped {
	//public vars
	var <timesValues, <delta, <otherSignal ;
	//private vars
	var key, func, player, controller;
	
	*new{ |array, delta = 0.1, otherSignal| 
		^super.newCopyArgs(array, delta, otherSignal) 
	}
	
	currentValue { ^timesValues[0][1] }

	startMapping { |unit, unitkey|
		unit.set(unitkey, this.currentValue );
		key = unitkey;
		controller = SimpleController(unit)
			.put(\start, { "starting".postln; this.unitStarted(unit) })
			.put(\end, { "stoping".postln; this.unitStopped });		
	}
	
	stopMapping { |unit, key|
		controller.remove;
	}
	
	unitStarted { |unit|
		//player = EventPlayerES(timesValues);
		var times, values, interpolateFunc, final, switch;
		#times, values = timesValues.flop;
		interpolateFunc = { |t| 
			values.blendAt( times.indexInBetween(t) )
		};
		player = interpolateFunc <%> TimerES(delta, times.last).hold(0.0);
		//outputs only one event the first time that otherSignal outputs something
		switch = otherSignal.inject(-1, { |state| state+1  }).takeWhile(_<2);
		final = switch.flatCollect({ otherSignal }, player );
		func = { |v| unit.set(key, v) };
		final.do(postln(_));
		final.do(func);
	}
	
	unitStopped { |unit| player.stopDoing(func) }
}
*/


+ FPSignal {

	asUMapped { |useSpec = true| ^FPSignalUMapped(this, useSpec) }
	
}
