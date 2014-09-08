
UBufSndFile : BufSndFile {

	classvar <>all;
	var <>id;
	var <children;

	*new { |id = \default, path, startFrame = 0, endFrame, rate = 1, loop = false, useChannels| // only returns new object if doesn't exist
		var x = this.get(id) ?? { this.basicNew( id, path, startFrame, endFrame, rate, loop, useChannels ) };
		//"new id: % hash: %".format(id, x.hash).postln;
		^x
	}

	*basicNew { |id = \default, path, startFrame = 0, endFrame, rate = 1, loop = false, useChannels|
		//"basicNew".postln;
		^super.new(path, startFrame, endFrame, rate, loop, useChannels).id_( id ).addToAll;
	}

	*get { |id|
		//"get id: % returns %".format(id, all.detect({ |item| item.id === id }) ).postln;
		^all.detect({ |item| item.id === id })
	}


	*clear {
		all = nil
	}

	addChild { |obj|
		//"id: % hash: % adding child: % to children: of class %".format(this.id, this.hash, obj, children, children.class).postln;
		this.children_( children.add( obj ) );
		//"children is now %".format(children).postln;
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

	addToAll {
		all !? { all.removeAllSuchThat({ |item| item.id === this.id }); };
		all = all.asCollection.add( this );
		this.class.changed( \all, \add, this );
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
		^this.buffers.detect({ |item|
			item.server == server
		})
	}

	makeIfEmpty { |servers|
		servers = servers.asCollection;
		if( children.size == 0 ) {
			servers.collect({ |s|
				this.makeBuffer( s );
			})
		} {
			servers.collect({ |s|
				this.buffers.detect({ |item|
					item.server == s
				}) ?? {
					this.makeBuffer( s );
				};
			});
		}
	}

	freeIfEmpty {
		if( children.size == 0 ) {
			"UBufSndFile - freeing all buffers".postln;
			this.dispose;
		};
	}

	deepCopy {
		//"I'm being deep copied id:% hash%".format(id, this.hash).postln;
		^this
	}

}
