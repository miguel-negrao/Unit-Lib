UPatDef : FuncUMapDef {
	
	classvar <>all, <>defsFolders, <>userDefsFolder;
	classvar <>defaultCanUseUMapFunc;
	classvar <>currentUnit;
	
	*initClass{
		this.defsFolders = [ 
			this.filenameSymbol.asString.dirname.dirname.dirname +/+ "UPatDefs"
		];
		this.userDefsFolder = Platform.userAppSupportDir ++ "/UPatDefs/";
		defaultCanUseUMapFunc = { |unit, key, upatdef|
			unit.getSpec( key ).respondsTo( \asControlSpec ) && {
				unit.getDefault( key ).asControlInput.asCollection.size == upatdef.numChannels
			};
		};
	}
	
	doFunc { |unit|
		var res;
		this.class.currentUnit = unit;
		res = unit.stream.next;
		this.class.currentUnit = nil;
		if( valueIsMapped ) {
			unit.setArg( \value, unit.getSpec( \value ).map( res ) );
		} {
			unit.setArg( \value, res );
		};
	}
	
	makeStream { |unit|
		unit.stream = func.value( unit, *this.getStreamArgs( unit ) );
	}
	
	getStreamArgs { |unit|
		^unit.argSpecs.collect({ |item| 
			if( this.isMappedArg( item.name ) ) { 
				UPatArg( unit, item.name, item.spec ); 
			} {
				UPatArg( unit, item.name );
			};
		});
	}
	
	setSynth { |unit ...keyValuePairs|
		keyValuePairs.clump(2).do({ |item|
			if( [ \u_spec, \u_prepared ].includes( item[0] ).not ) {
				unit.unitSet;
			};
		});
	}

}

UPat : UMap {
	
	classvar >guiColor;
		
	var <>stream;
	
	*defClass { ^UPatDef }
	
	*guiColor { ^guiColor ?? { guiColor = Color.green(0.5).blend( Color.white, 0.8 ).alpha_(0.4) }; }
	
	init { |in, inArgs, inMod|
		super.init( in, inArgs ? [], inMod );
		this.makeStream;
	}
	
	makeStream {
		this.def.makeStream( this );
	}
	
	asUnitArg { |unit, key|
		var res;
		res = super.asUnitArg( unit, key );
		if( this === res ) {
			this.makeStream;
		};
		^res;
	}
	
	/*
	spec_ { |newSpec|
		super.spec_( newSpec );
		this.makeStream;
	}
	*/
	
	reset {
		stream.reset;
	}
}

UPatArg {
	var <>unit, <>key, <>spec;
	
	*new { |unit, key, spec|
		^super.newCopyArgs( unit, key, spec );	
	}
	
	next {
		var value;
		if( UPatDef.currentUnit.notNil && { unit !== UPatDef.currentUnit }) {
			value = UPatDef.currentUnit.get( key );
		} {
			value = unit.get( key );
		};
		if( value.isUMap.not && { spec.notNil } ) {
			^spec.unmap( value.next );
		} {
			^value.next;
		};
	}
	
	doesNotUnderstand { |selector ...args|
		^this.next.perform( selector, *args);
	}
}

+ UMap {
	next { ^this.asControlInput }
}