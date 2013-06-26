/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

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

UScoreEditor {

    classvar <clipboard;

	var <score;
	var <undoStates, <redoStates, maxUndoStates = 40;

	*new { |score|
		^super.newCopyArgs( score)
			.init
	}

	init {
		undoStates = List.new;
		redoStates = List.new;
	}

	*initClass {
	    clipboard = [];
	}

	*current { ^UScoreEditorGUI !? { |x| x.editor } }

	events { ^score.events }

	events_ { |events|
	    this.changeScore{score.events = events}
	}

	changeScore{ |action|
	    this.changed(\preparingToChangeScore);
	    this.storeUndoState;
	    action.value;
		score.changed(\something);
	}

	//--COPY/PASTE--

	*copy{ |events|
	    clipboard = events.collect( _.deepCopy );
	}

	paste { |pos|
	    var evsToPaste = clipboard.collect( _.deepCopy );
	    var minTime = evsToPaste.collect(_.startTime).minItem;
        score.addEventsToEmptyRegion(evsToPaste.collect{ |ev| ev.startTime = ev.startTime - minTime + pos});
        score.changed(\numEventsChanged);
        score.changed(\something);

    }

    pasteAtCurrentPos {
        this.paste(score.pos)
    }

	//--UNDO/REDO--
	storeUndoState {

		redoStates = List.new;
		undoStates.add( score.events.collect( _.duplicate ) );
		if(undoStates.size > maxUndoStates) {
			undoStates.removeAt(0);
		}

	}

	undo {

		if(undoStates.size > 0) {
			redoStates.add(score.events);
			score.events = undoStates.pop;
		};
		score.changed(\numEventsChanged);
		score.changed(\something);
		this.changed(\undo);

	}

	redo {

		if( redoStates.size > 0 ) {
			undoStates.add(score.events);
			score.events = redoStates.pop;
		};
		score.changed(\numEventsChanged);
		score.changed(\something);
		this.changed(\redo);

	}

	//--EVENT MANAGEMENT--

    <| { |events|
        this.changeScore({
            score <| events;
        });
        score.changed(\numEventsChanged);
    }

    addEvent{
        this.changeScore({
            score.addEventToEmptyTrack( UChain.default.startTime_(score.pos) )
        });
        score.changed(\numEventsChanged);
    }
    
    addMarker{
        this.changeScore({
            score.addEventToEmptyTrack( UMarker().startTime_(score.pos) )
        });
        score.changed(\numEventsChanged);
    }

	duplicateEvents{ |events|
		this.changeScore({
	        events.do({ |ev| score.addEventToCompletelyEmptyTrack( ev.duplicate ) } );
		});
		score.changed(\numEventsChanged);
	}

	deleteEvents { |events|
		if( events.size > 0 ) {	
			this.changeScore({
			    score.events_(score.events.select(events.includes(_).not));
			});
			score.changed(\numEventsChanged);
		};
	}

	disableEvents { |events|
		this.changeScore({
		    events.do( _.disable );
		})
	}

	enableEvents { |events|
		this.changeScore({
		    events.do( _.enable );
		})
	}

	toggleDisableEvents { |events|
	    this.changeScore({
	        events.do( _.toggleDisable );
	    })
	}

	enableAll {
	    this.changeScore({
		    this.enableEvents(score.events);
		})
	}

	soloEnableEvents { |events|
		this.changeScore({
            if( events.size > 0 ) {
                score.events.do({ |event|
                    if( events.includes( event ) ) {
                        event.enable
                    } {
                        event.disable
                    };
                });
            };
		})
	}

	folderFromEvents { |events|
		var folderEvents, folderStartTime;

		if( events.size > 0 ) {
		    this.changeScore({
                score.events_(score.events.select(events.includes(_).not));
                folderStartTime = events.sort[0].startTime;
                score.events_(score.events.add(
                    UScore(
                        *events.collect({ |event|
                            event.startTime_( event.startTime - folderStartTime )
                        })
                    ).startTime_(folderStartTime).track_(events[0].track)
                ));
            })
		};
		score.changed(\numEventsChanged);
	}

	unpackSelectedFolders{ |events|
		var folderEvents,newEvents;

		newEvents = [];

		if( events.size > 0 and: { folderEvents = events.select( _.isFolder );
				folderEvents.size > 0
				}
		) {
			this.changeScore({
                folderEvents.do({ |folderEvent|
                    newEvents = newEvents
                        ++ folderEvent.events.collect({ |item|
                            item.startTime_( item.startTime + folderEvent.startTime )
                        });
                    score.events.remove( folderEvent );
                });

                newEvents.do({ |evt|
                        score.addEventToEmptyTrack( evt );
                });

			});
			score.changed(\numEventsChanged);
		}
	}
	
	moveEvents { |events, amount = 0| // use for small steps
		if( events.size > 0 ) {	
			this.changeScore({	
				var minStartTime;
				if( amount.isNegative && { amount.abs > (events.collect(_.startTime ).minItem) }) {
					amount = 0; // don't move if negative amount exceeds first startTime
				};
				if( amount != 0 ) {
					this.changeScore({
						events.do({ |ev|
							ev.startTime = ev.startTime + amount;
						});
					});
				};
			});
		};
	}

	changeEventsTrack { |events, amount = 0|
		if( events.size > 0 ) {	
			this.changeScore({		
				var minStartTime;
				amount = amount.round(1);
				if( amount.isNegative ) { // don't allow negative tracks
					amount = amount.abs.min( events.collect(_.track).minItem ).neg;
				};
				if( amount != 0 ) {
					this.changeScore({
						events.do({ |ev|
							ev.track = ev.track + amount;
						});
					});
				};
			});
		};
	}

	cutEventsStart { |events,pos,isFolder=false,removeFadeIn = true|
        this.changeScore({
            events.select(_.isFolder.not).do{ |ev|
                ev.cutStart(pos, false, removeFadeIn)
            };
		})
	}

	cutEventsEnd { |events,pos, isFolder = false, removeFadeOut = false|
        this.changeScore({
            events.select(_.isFolder.not).do{ |ev|
                ev.cutEnd(pos, removeFadeOut)
            };
		})
	}

	splitEvents { |events, pos|
		var frontEvents, backEvents;

		this.changeScore({
            frontEvents = events.select(_.isFolder.not).select({ |event|
                var dur = event.dur;
                var start = event.startTime;
                (start < pos) && ((start + dur) > pos)
            });
            backEvents = frontEvents.collect(_.duplicate);
            score.events = score.events ++ backEvents;
            backEvents.do{ |ev|
                    ev.cutStart(pos, false, true)
                };
            frontEvents.do{ |ev|
                    ev.cutEnd(pos, true)
                };
		});
		score.changed(\numEventsChanged);
	}

    //perform editing operations at the current transport position
	trimEventsStartAtPos{ |events|
		this.cutEventsStart(events, score.pos);
	}

	trimEventsEndAtPos{ |events|
		this.cutEventsEnd(events, score.pos);
	}

	splitEventsAtPos { |events|
	    this.splitEvents(events, score.pos)
	}
	
	quantizeEvents { |events, amount = 0, useTempoMap = false|
		amount = amount ? 0;
		if( amount > 0 ) {
			this.changeScore({	
				if( useTempoMap ) {
					events.do({ |event|
						event.startTime = score.tempoMap.timeAtBeat(
							score.tempoMap.beatAtTime(
								event.startTime
							).round(amount)
						);
					});
				} {
					events.do({ |event|
						event.startTime = event.startTime.round(amount);
					});
				};
			})
		}
	}

}