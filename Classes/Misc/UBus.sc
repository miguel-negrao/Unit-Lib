/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2014 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
(
Udef.loadOnInit = true;
Udef(\test, { Out.ar(\bus.kr(0), SinOsc.ar([440,880])*0.1) }).setSpec(\bus, PositiveIntegerSpec() );
Udef(\out2, { Out.ar(0, In.ar(\bus.kr(0),2)) }).setSpec(\bus, PositiveIntegerSpec() );
)

(
x = UScore( UChain( [\test, [\bus, UBus(\a,2) ]] ).ugroup_('default'), UChain( [\out2, [\bus, UBus(\a,2)]]).ugroup_('default').addAction_(\addToTail) );
x.prepareAndStart
)

(
x = UScore(
	UChain( [\test, [\bus, UBus(\a,2) ]] ).ugroup_('a'),
	UChain( [\out2, [\bus, UBus(\a,2)]]).ugroup_('a').addAction_(\addToTail),
	UChain( [\test, [\bus, UBus(\b,2) ]] ).ugroup_('b'),
	UChain( [\out2, [\bus, UBus(\b,2)]]).ugroup_('b').addAction_(\addToTail)
);
x.prepareAndStart
)


x.stop
x.dispose

this needs to be redone such that:

all resources are held in global variables
how to set and reset the num channels ?
*/


UBus {

	classvar <>gBuses;
	classvar <>gChildren;
	classvar <>gNumChannels;
	classvar <>gType;

	var <id;

	*initClass {
		gBuses = Dictionary.new;
		gChildren = Dictionary.new;
		gNumChannels = Dictionary.new;
		gType = Dictionary.new;
	}

	*new { |id = \default, numChannels| // only returns new object if doesn't exist
		//var s = "new - % %".format(id,numChannels).postln;
		numChannels !? 	gNumChannels.put(id, _);
		gNumChannels[id] ?? { gNumChannels.put(id, 1) };
		gType[id] ?? { gType.put(id, \audio ) };
		^super.newCopyArgs( id )
	}


	*audio { |id = \default, numChannels| // only returns new object if doesn't exist
		//var s = "audio - % %".format(id,numChannels).postln;
		//var x = this.get(id) ?? { this.basicNew( id, \audio, numChannels ) };
		//"audio id: % hash: %".format(id, x.hash).postln;
		numChannels !? 	gNumChannels.put(id, _);
		gNumChannels[id] ?? { gNumChannels.put(id, 1) };
		gType.put(id, \audio );
		^super.newCopyArgs( id )

	}

	*control { |id = \default, numChannels| // only returns new object if doesn't exist

		//var s = "control - % %".format(id,numChannels).postln;
		//"control id: % hash: %".format(id, x.hash).postln;
		numChannels !? 	gNumChannels.put(id, _);
		gNumChannels[id] ?? { gNumChannels.put(id, 1) };
		gType.put(id, \control );
		^super.newCopyArgs( id )
	}

	numChannels{ ^gNumChannels[id] }
	numChannels_ { |n| gNumChannels.put(id, n) }

	type{ ^gType[id] }
	type_ { |aType| gType.put(id, aType) }

	children{ ^gChildren[id] }
	children_{ |obj| gChildren.put(id, obj) }

	buses{ ^gBuses[id] }
	buses_{ |xs| gBuses.put(id, xs) }

	addChild { |obj|
		//"id: % hash: % adding child: % to children: of class %".format(this.id, this.hash, obj, this.children, this.children.class).postln;
		this.children_( this.children.add( obj ) );
		//"children is now %".format(this.children).postln;
		this.changed( \addChild, obj );
	}

	removeChild { |obj|
		var res;
		//"id: % hash: % obj:% objhash: % children: %".format(this.id, this.hash, obj, obj.hash, this.children.collect{ |x| [x,x.hash] }).postln;
		this.children !? { |x|
			res = x.remove( obj );
			//"removed: %".format(res).postln;
			if( res.notNil ) { this.changed( \removeChild, obj ); };
		};
	}

	prepare { |servers, startPos = 0, action, unit|
	//"prepare: id: % hash: % servers: % this: % unit: %".format(this.id, this.hash, servers, this, unit).postln;
		this.makeIfEmpty( servers );
		this.addChild( unit );
		action.value;
	}

	disposeFor { |server, unit|
		//"dispose: id: % hash: % server: % unit:%".format(this.id, this.hash, server, unit).postln;
		this.removeChild( unit );
		this.freeIfEmpty;
	}

    asControlInputFor { |server, startPos = 0|
		var x = this.buses.detect({ |item|
			item.server == server
		});
		//"UBus % hash: % buses: % #asControlInputFor> %".format(this.id, this.hash, this.buses, x).postln;
		^x
	}

	makeBus { |server|
		//var d1 = "UBus#makeBus hash: % - id:%".format(this.hash,id).postln;
		//var d2 = 5.dumpBackTrace;
		var bus;

		bus = Bus.perform(this.type, server, this.numChannels);
		this.buses_( this.buses.add( bus ) );
		//"buses :%".format(this.buses).postln;
		this.changed( \start );
		^bus;
	}

	free { |target|
		if( this.buses.size > 0 ) {
			this.buses.do(_.free);
			//"freeing %".format(id).postln;
			this.buses_([]);
			this.changed( \end );
		};
	}


	makeIfEmpty { |servers|
		servers = servers.asCollection;
		if( this.children.size == 0 ) {
			servers.collect({ |s|
				this.makeBus( s );
			})
		} {
			servers.collect({ |s|
				this.buses.detect({ |item|
					item.server == s
				}) ?? {
					this.makeBus( s );
				};
			});
		}
	}

	freeIfEmpty {
		if( this.children.size == 0 ) {
			this.free;
		};
	}
}