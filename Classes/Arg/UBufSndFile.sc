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

TODO:

specs for this thing...

*/


UGlobalBufSndFileArg {
	var <id;
	//for internal use
	var <globalBufSndFile;

	*new{ |id| ^super.newCopyArgs(id) }

	prepare { |servers, startPos = 0, action, unit|
		globalBufSndFile = UGlobalBufSndFile.get(id);
		//We store the globalBufSndFile so that we can still free the buffers even if the global buffer gets changed meanwhile to another file
		if( globalBufSndFile.isNil ){ Error("No UGlobalBufSndFile loaded for id %".format(id)).throw };
		globalBufSndFile.prepare(servers, startPos, action, unit)
	}

	disposeFor { |server, unit|
		if( globalBufSndFile.isNil ){ Error("No UGlobalBufSndFile prepared for id %".format(id)).throw };
		globalBufSndFile.disposeFor(server, unit);
		globalBufSndFile = nil;
	}

    asControlInputFor { |server, startPos = 0|
		if( globalBufSndFile.isNil ){ Error("No UGlobalBufSndFile prepared for id %".format(id)).throw };
		^globalBufSndFile.asControlInputFor(server, startPos)
	}

	numChannelsForPlayBuf {
		globalBufSndFile = UGlobalBufSndFile.get(id);
		if( globalBufSndFile.isNil ){ Error("No UGlobalBufSndFile loaded for id %".format(id)).throw };
		^globalBufSndFile.bufSndFile.numChannelsForPlayBuf
    }

	asUGlobalBufSndFileArg {
		^this
	}

	path {
		globalBufSndFile = UGlobalBufSndFile.get(id);
		if( globalBufSndFile.isNil ){ Error("No UGlobalBufSndFile loaded for id %".format(id)).throw };
		^globalBufSndFile.bufSndFile.path
	}

	storeArgs{ ^[id] }
}


UGlobalBufSndFile {

	classvar <>all;

	var <id;
	var <bufSndFile;

	//private
	var <children;

	*initClass{
		all = Dictionary.new
	}

	//calling new always overwrites the global dictionary
	*new { |id = \default, path, startFrame = 0, endFrame, rate = 1, loop = false, useChannels|
		^super.newCopyArgs(id, BufSndFile.new(path, startFrame, endFrame, rate, loop, useChannels) ).addToAll;
	}

		//calling new always overwrites the global dictionary
	*newBasic { |id = \default, path, numFrames, numChannels, sampleRate = 44100, startFrame = 0, endFrame,
		rate = 1, loop = false |
		^super.newCopyArgs(id, BufSndFile.newBasic(path, numFrames, numChannels, sampleRate, startFrame, endFrame,
		rate, loop = false ) ).addToAll;
	}

	//calling new always overwrites the global dictionary
	addToAll {
		all.put(id, this)
	}

	*get { |id|
		//"get id: % returns %".format(id, all.detect({ |item| item.id === id }) ).postln;
		^all[id]
	}

	*clear {
		all = Dictionary.new
	}


	addChild { |obj|
		"id: % hash: % adding child: % to children: of class %".format(this.id, this.hash, obj, children, children.class).postln;
		this.children_( children.add( obj ) );
		"children is now %".format(children).postln;
		this.changed( \addChild, obj );
	}

	removeChild { |obj|
		var res;
		//"id: % hash: %".format(this.id, this.hash).postln;
		children !? {
			res = children.remove( obj );
			//"removed: %".format(res).postln;
			if( res.notNil ) { this.changed( \removeChild, obj ); };
		};
	}

	children_{ |obj|
		//"setting children in id: % hash: % to %".format(id, this.hash, obj).postln;
		children = obj
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
		var buffer = bufSndFile.buffers.detect({ |item|
			item.server == server
		});
		^[ buffer, bufSndFile.rate, bufSndFile.loop.binaryValue ]
	}

	makeIfEmpty { |servers|
		servers = servers.asCollection;
		if( children.size == 0 ) {
			servers.collect({ |s|
				bufSndFile.makeBuffer( s );
			})
		} {
			servers.collect({ |s|
				bufSndFile.buffers.detect({ |item|
					item.server == s
				}) ?? {
					bufSndFile.makeBuffer( s );
				};
			});
		}
	}

	freeIfEmpty {
		if( children.size == 0 ) {
			"UBufSndFile - freeing all buffers".postln;
			bufSndFile.dispose;
		};
	}

	deepCopy {
		"I'm being deep copied id:% hash%".format(id, this.hash).postln;
		^super.deepCopy
	}

	storeOn { arg stream;
		stream << this.class.name << ".newBasic(" <<* [ // use newBasic to prevent file reading
		    id.cs, bufSndFile.path.formatGPath.quote, bufSndFile.numFrames, bufSndFile.numChannels, bufSndFile.sampleRate,
			bufSndFile.startFrame, bufSndFile.endFrame, bufSndFile.rate, bufSndFile.loop
		]  <<")"
	}

}


UGlobalBufSndFileArgSpec : Spec {

	constrain { |value| ^value.asUGlobalBufSndFileArg }

	*testObject { |obj|
		^obj.class == UGlobalBufSndFileArg
	}

	default { ^UGlobalBufSndFileArg(\a) }

	map { |value| ^value }
	unmap { |value| ^value }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var skin;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		skin = RoundView.skin;

		bounds.isNil.if{bounds= 160@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;

		vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \symbolView ] = TextView( vws[ \view ], 100 @ bounds.height )
		.string_( this.default.id.asString )
		.resize_( 4 )
		.action_({ |o|
			vws[ \val ] = UGlobalBufSndFileArg( o.value.asSymbol );
			action.value( vws, vws[ \val ]);
		})
		.applySkin( RoundView.skin );

		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \symbolView ].string_( value.id.asString )
	}

}


+ Symbol {

	asUGlobalBufSndFileArg {
		^UGlobalBufSndFileArg(this)
	}

}

+ Object {

	asUGlobalBufSndFileArg {
		^UGlobalBufSndFileArg(\a)
	}

}