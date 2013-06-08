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

UGUI {
	
	var <unit;
	
	var <parent, <composite, <views, <controller;
	var <viewHeight = 14, <labelWidth = 80;
	var <>action;
	
	*new { |parent, bounds, unit|
		^super.newCopyArgs( unit ).init( parent, bounds );
	}
	
	init { |inParent, bounds|
		parent = inParent;
		if( parent.isNil ) { parent = Window( unit.defName ).front };
		this.makeViews( bounds );
	}
	
	*getHeight { |unit, viewHeight, margin, gap|
		viewHeight = viewHeight ? 14;
		margin = margin ?? {0@0};
		gap = gap ??  {4@4};
		^(margin.y * 2) + ( this.viewNumLines( unit ) * (viewHeight + gap.y) ) - gap.y;
	}
	
	*viewNumLines { |unit|
		^unit.argSpecs
			.select({|x|
				x.private.not
			})
			.collect({|x|
				if( unit[ x.name ].isKindOf( UMap ) ) {
					UMapGUI.viewNumLines( unit[ x.name ] );
				} {
					x.spec.viewNumLines
				};
			}).sum;
	}
	
	makeViews { |bounds|
		var margin = 0@0, gap = 4@4;
		
		if( bounds.isNil ) { 
			bounds = parent.asView.bounds.insetBy(4,4);
			if( parent.asView.class.name == \SCScrollTopView ) {
				bounds.width = bounds.width - 12;
			};
			if( parent.asView.class.name == \QScrollTopView ) {
				bounds.width = bounds.width - 20;
			};
		};
		bounds = bounds.asRect;
		bounds.height = this.class.getHeight( unit, viewHeight, margin, gap );
		controller = SimpleController( unit );
		
		if( unit.class == MassEditU ) {
			unit.connect;
		};
		
		composite = CompositeView( parent, bounds ).resize_(2);
		composite.addFlowLayout( margin, gap );
		composite.onClose = {
			if( unit.class == MassEditU ) {
				unit.disconnect;
			}; 
			controller.remove
		 };
		 
		 this.makeSubViews( bounds );
	}
	
	makeSubViews { |bounds|
		views = ();
		
		this.makeHeader(bounds);
		
		unit.args.pairsDo({ |key, value, i|
			var vw, argSpec;
			
			argSpec = unit.argSpecs[i/2];
			
			if( argSpec.private.not ) { // show only if not private
				if( value.isKindOf( UMap ) ) {
					vw = UMapGUI( composite, nil, value );
				} {
					vw = ObjectView( composite, nil, unit, key, 
						argSpec.spec, controller,
						if( [ \nonsynth, \init ].includes(argSpec.mode) ) { key ++ " (i)" }
					);
					
					vw.action = { action.value( this, key, value ); };
				};
				views[ key ] = vw;
			}
		
		});
		
		if( views.size == 0 ) {
			controller.remove;
		};
	}
	
	makeHeader { }
	
	resize_ { |resize| composite.resize_(resize) }
	reset { unit.reset }
	
	font_ { |font| views.values.do({ |vw| vw.font = font }); }
	viewHeight_ { |height = 16|
		views.values.do({ |vw| vw.view.bounds = vw.view.bounds.height_( height ) });
		composite.decorator.reFlow( composite );
	}
	labelWidth_ { |width=50|
		labelWidth = width;
		views.values.do(_.labelWidth_(width));
	}
	
	view { ^composite }
}

+ U {
	gui { |parent, bounds| ^UGUI( parent, bounds, this ) }
}