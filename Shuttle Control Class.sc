Shuttle_Mode : HasAssignableGUIs {
	classvar >connected, >server, >toShuttleControl;
	var gateDur,
	gateOut,
	index,
	jitterMax,
	jitterMin,
	//jitterView,
	max,
	min,
	mode,
	parent,
	pauseBut,
	steady,
	steadyDur,
	//steadyView,
	valueGate,
	view;

	makeView {
		view = CompositeView(parent.view,Rect(0,20,parent.view.bounds.width,parent.view.bounds.height-20));
	}

	insertPauseButton {
		// PAUSE
		pauseBut = Button(view,Rect(0,0,80,20))
		.states_([["Paused",Color.black,Color.yellow],["Running",Color.black,Color.green]])
		.action_({
			arg b;
			//manager.win.bounds.postln;
			if(b.value == 0,{
				//"pause button pushed: paused".postln;
				this.pause;
			},{
				//"pause button pushed: run".postln;
				this.run;
			})
		})
		.addToggleRequestNew(
			"/shuttleControl/"++index+/+mode++"/pause",
			Rect(80,0,20,20),
			this,
			view
		);
	}

	insertJitterButton {
		var jitterView, steadyView;

		jitterMax = 20;
		jitterMin = 0.05;
		steady = true;
		steadyDur = 1;

		jitterView = CompositeView(view,Rect(80,20,90,40));
		steadyView = CompositeView(view,Rect(80,20,90,40));

		Button(view,Rect(0,20,60,40))
		.states_([["Steady",Color.white,Color.blue],["Jitter",Color.blue,Color.white]])
		.action_({
			arg b;
			if(b.value == 0,{
				steady = true;
				steadyView.visible_(true);
				jitterView.visible_(false);
			},{
				steady = false;
				steadyView.visible_(false);
				jitterView.visible_(true);
			});
		})
		.addToggleRequestNew(
			"/shuttleControl/"++index+/+mode++"/steadyBut",
			Rect(60,20,20,40),
			this,
			view
		);

		EZNumber(steadyView,Rect(0,0,70,40),"Dur",ControlSpec(0.05,20),{
			arg nb;
			steadyDur = nb.value;
		},1,labelWidth:30)
		.addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/steadyDur",
			ControlSpec(0.05,20),
			Rect(70,0,20,40),
			this,
			steadyView
		);

		EZNumber(jitterView,Rect(0,0,70,20),"max",ControlSpec(0.05,20),{
			arg nb;
			jitterMax = nb.value;
		},20,labelWidth:30)
		.addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/jitterMax",
			ControlSpec(0.05,20),
			Rect(70,0,20,20),
			this,
			jitterView
		);

		EZNumber(jitterView,Rect(0,20,70,20),"min",ControlSpec(0.05,20),{
			arg nb;
			jitterMin = nb.value;
		},0.05,labelWidth:30)
		.addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/jitterMin",
			ControlSpec(0.05,20),
			Rect(70,20,20,20),
			this,
			jitterView
		);
		jitterView.visible_(false);
	}

	insertGateInfo {
		var gateView;

		valueGate = false;

		Button(view,Rect(100,0,60,20))
		.states_([["Gate OFF",Color.black,Color.red],["Gate ON",Color.black,Color.green]])
		.action_({
			arg b;
			if(b.value == 1,{
				valueGate = true;
				gateView.visible_(true);
			},{
				valueGate = false;
				gateView.visible_(false);
			});
		})
		.addToggleRequestNew(
			"/shuttleControl/"++index+/+mode++"/gateToggle",
			Rect(160,0,20,20),
			this,
			view
		);

		gateView = CompositeView(view,Rect(180,0,200,20));

		EZNumber(gateView,Rect(0,0,80,20),"out",ControlSpec(1,16,step:1),{
			arg nb;
			gateOut = nb.value;
		},index,labelWidth:30)
		.addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/gateOut",
			ControlSpec(1,16,step:1),
			Rect(80,0,20,20),
			this,
			gateView
		);

		EZNumber(gateView,Rect(100,0,80,20),"dur",ControlSpec(0.05,2),{
			arg nb;
			gateDur = nb.value;
		},0.5,true,labelWidth:30)
		.addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/gateDur",
			ControlSpec(0.05,2),
			Rect(180,0,20,20),
			this,
			gateView
		);

		gateView.visible_(false);
	}

	insertMaxMin {
		var minNB, maxNB;
		maxNB = EZNumber(view,Rect(260,0,100,20),"Max",ControlSpec(-5,5),{
			arg nb;
			max = nb.value;
		},5,true,30);

		maxNB.addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/max",
			maxNB.controlSpec,
			Rect(360,0,20,20),
			this,
			view
		);

		minNB = EZNumber(view,Rect(260,20,100,20),"Min",ControlSpec(-5,5),{
			arg nb;
			min = nb.value;
		},-5,true,30);

		minNB.addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/min",
			minNB.controlSpec,
			Rect(360,20,20,20),
			this,
			view
		);
	}

	sendValue {
		arg value;
		//value.postln;
		//connected.postln;
		if(connected,{
			toShuttleControl.control(1,index,value);
		});
	}
}

Shuttle_Mode_Gate : Shuttle_Mode {
	var task;

	*new {
		arg index, parent;
		^super.new.init(index, parent);
	}

	init {
		arg index_, parent_;
		mode = "gate";
		index = index_;
		parent = parent_;

		this.makeView;
		this.insertPauseButton;
		this.insertGateInfo;
		this.insertJitterButton;

		task = Task({
			inf.do({
				//toShuttleControl.control(1,index,127);
				this.sendValue(127);
				//"Gate: 1".postln;
				AppClock.sched(gateDur,{
					//toShuttleControl.control(1,index,63);
					this.sendValue(63);
					//"Gate: 0".postln;
					nil;
				});

				if(steady,{
					steadyDur.wait;
				},{
					rrand(jitterMin,jitterMax).wait;
				});
			});
		});
	}

	show {
		view.visible_(true);
		if(pauseBut.value == 1,{
			this.run;
		});
	}

	hide {
		view.visible_(false);
		this.pause;
	}

	pause {
		//"gate paused".postln;
		task.pause;
	}

	run {
		//"gate run".postln;
		task.play;
	}
}

Shuttle_Mode_LFO : Shuttle_Mode {
	var synth, lfoTypes, task, freqNB, bus, lfoTypePum;

	*initClass {
		StartUp.defer {
			SynthDef(\sc_lfo,{
				arg type, freq, bus;
				var sig;
				sig = Select.kr(type,[
					SinOsc.kr(freq),
					LFTri.kr(freq),
					LFPulse.kr(freq,0,0.5,2,-1),
					LFSaw.kr(freq),
					LFSaw.kr(freq,0,-1),
					LFDNoise1.kr(freq).clip(-1.0,1.0),
					LFDNoise3.kr(freq).clip(-1.0,1.0),
					LFDNoise0.kr(freq).clip(-1.0,1.0)
				]);
				//sig.poll(label:"sig in synth def");
				Out.kr(bus,sig);
			}).writeDefFile;
		}
	}

	*new {
		arg index, parent;
		^super.new.init(index, parent);
	}

	show {
		view.visible_(true);
		if(pauseBut.value == 1,{
			this.run;
		});
	}

	hide {
		view.visible_(false);
		this.pause;
	}

	pause {
		task.pause;
		synth.run(false);
	}

	run {
		task.play;
		synth.run(true);
	}

	init {
		arg index_, parent_;
		mode = "lfo";
		index = index_;
		parent = parent_;

		this.makeView;
		this.insertPauseButton;
		this.insertMaxMin;

		bus = Bus.control(server).set(0);

		lfoTypes = [
			"Sine",
			"Triangle",
			"Square",
			"Saw +",
			"Saw -",
			"Noise (Linear Interp.)",
			"Noise (Poly. Interp.)",
			"Noise (S & H)"
		];

		task = Task({
			inf.do({
				bus.get({
					arg v;
					var tempMin, tempMax;
					/*"LFO Min: ".post; lfoMin.postln;
					"LFO Max: ".post; lfoMax.postln;*/
					tempMin = min.linlin(-5,5,0,127);
					tempMax = max.linlin(-5,5,0,127);
					/*"Temp Min: ".post; tempMin.postln;
					"Temp Max: ".post; tempMax.postln;*/
					v = v.linlin(-1,1,tempMin,tempMax).floor;
					//toShuttleControl.control(1,index,v);
					this.sendValue(v);
					//"LFO Value: ".post; v.postln;
					//"".postln;
				});
				0.05.wait;
			});
		});

		// build view
		StaticText(view,Rect(0,20,70,20)).string_("LFO Type:");
		lfoTypePum = PopUpMenu(view,Rect(70,20,130,20)).items_(lfoTypes)
		.action_({
			arg pum;
			synth.set(\type,pum.value);
		});

		freqNB = EZSlider(view,Rect(0,40,200,20),"Freq",ControlSpec(0.01,20,\exp),{
			arg sl;
			synth.set(\freq,sl.value);
		},0.2,labelWidth:30);

		freqNB.addHandleRequestNew(
			"/shuttleControl/"++index++"/lfo/freq",
			freqNB.controlSpec,
			Rect(200,40,20,20),
			this,
			view
		);

		/*		synth = Synth.newPaused(\sc_lfo,[
		\type,0,
		\freq,0.2,
		\bus,bus
		]);*/

		synth = Synth.newPaused(\sc_lfo,[
			\type,0,
			\freq,0.2,
			\bus,bus
		]);

		server.sync;

		synth.run(false);
	}

	save {
		var dict = super.save;
		// gotta save lfo type
		dict.put(\lfoType,lfoTypePum.value); //  this saves it as an integer!!!!!!
		^dict;
	}

	load {
		arg dict;

		lfoTypePum.valueAction_(dict.at(\lfoType));

		super.load(dict);
	}
}

Shuttle_Mode_Manual_Slider : Shuttle_Mode {
	var running;

	*new {
		arg index, parent;
		^super.new.init(index, parent);
	}

	show {
		view.visible_(true);
		this.run;
	}

	hide {
		view.visible_(false);
		this.pause;
	}

	pause {
		running = false;
	}

	run {
		running = true;
	}

	init {
		arg index_, parent_;
		mode = "manualSlider";
		index = index_;
		parent = parent_;
		running = false;

		this.makeView;
		//this.insertMaxMin;

		EZSlider(view,Rect(0,0,220,60),"value",ControlSpec(-5,5),{
			arg sl;
			if(running,{
				//toShuttleControl.control(1,index,sl.value.linlin(-5,5,0,127).floor);
				this.sendValue(sl.value.linlin(-5,5,0,127).floor);
			});
		},0,false).addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/value",
			ControlSpec(-5,5),
			Rect(220,0,20,60),
			this,
			view
		);
	}
}

Shuttle_Mode_Manual_Toggle : Shuttle_Mode {
	var running, manTogOff, manTogOn;

	*new {
		arg index, parent;
		^super.new.init(index, parent);
	}

	show {
		view.visible_(true);
		this.run;
	}

	hide {
		view.visible_(false);
		this.pause;
	}

	pause {
		running = false;
	}

	run {
		running = true;
	}

	init {
		arg index_, parent_;
		mode = "manualToggle";
		index = index_;
		parent = parent_;
		running = false;

		manTogOff = 0;
		manTogOn = 5;

		this.makeView;

		Button(view,Rect(0,0,240,60))
		.states_([["Off",Color.black,Color.red],["On",Color.black,Color.green]])
		.action_({
			arg b;
			if(running,{
				//toShuttleControl.control(1,index,b.value.linlin(0,1,manTogOff,manTogOn).linlin(-5,5,0,127).floor);
				this.sendValue(b.value.linlin(0,1,manTogOff,manTogOn).linlin(-5,5,0,127).floor);
			});
		})
		.addToggleRequestNew(
			"/shuttleControl/"++index++"/manualTog/value",
			Rect(240,0,20,60),
			this,
			view
		);

		EZNumber(view,Rect(260,0,100,30),"On Val",ControlSpec(-5,5),{
			arg nb;
			manTogOn = nb.value;
		},manTogOn,false)
		.addHandleRequestNew(
			"/shuttleControl/"++index++"/manualTog/onVal",
			ControlSpec(-5,5),
			Rect(360,0,20,30),
			this,
			view
		);

		EZNumber(view,Rect(260,30,100,30),"Off Val",ControlSpec(-5,5),{
			arg nb;
			manTogOff = nb.value;
		},manTogOff,false)
		.addHandleRequestNew(
			"/shuttleControl/"++index++"/manualTog/offVal",
			ControlSpec(-5,5),
			Rect(360,30,20,30),
			this,
			view
		);

	}
}

Shuttle_Mode_Trigger : Shuttle_Mode {
	var task;

	*new {
		arg index, parent;
		^super.new.init(index, parent);
	}

	show {
		view.visible_(true);
		if(pauseBut.value == 1,{
			this.run;
		});
	}

	hide {
		view.visible_(false);
		this.pause;
	}

	pause {
		task.pause;
	}

	run {
		task.play;
	}

	init {
		arg index_, parent_;
		mode = "trigger";
		index = index_;
		parent = parent_;

		this.makeView;
		this.insertPauseButton;
		this.insertJitterButton;

		task = Task({
			inf.do({

				//toShuttleControl.control(1,index,127);
				this.sendValue(127);
				0.05.wait;
				//toShuttleControl.control(1,index,63);
				this.sendValue(63);

				if(steady,{
					steadyDur.wait;
				},{
					rrand(jitterMin,jitterMax).wait;
				});
			});
		});
	}
}

Shuttle_Mode_Values : Shuttle_Mode {
	var	patternType,
	patternTypePum,
	patternTypes,
	randMax,
	randMin,
	randomValuesView,
	scale,
	scaleValuesView,
	task,
	valuesArray,
	valuesArrayTF,
	valuesPattern;

	*new {
		arg index, parent;
		^super.new.init(index, parent);
	}

	show {
		view.visible_(true);
		if(pauseBut.value == 1,{
			this.run;
		});
	}

	hide {
		view.visible_(false);
		this.pause;
	}

	pause {
		task.pause;
	}

	run {
		task.play;
	}

	init {
		arg index_, parent_;
		mode = "values";
		index = index_;
		parent = parent_;

		this.makeView;
		this.insertPauseButton;
		this.insertJitterButton;
		this.insertGateInfo;

		patternTypes = ["Up","Down","Ping Pong","Random"];
		patternType = patternTypes[0];
		valuesArray = [60];
		scale = true;
		randMax = 5;
		randMin = -5;

		Button(view,Rect(170,20,60,40))
		.states_([["Scale",Color.white,Color.blue],["Random",Color.blue,Color.white]])
		.action_({
			arg b;
			if(b.value == 0,{
				scale = true;
				scaleValuesView.visible_(true);
				randomValuesView.visible_(false);
			},{
				scale = false;
				scaleValuesView.visible_(false);
				randomValuesView.visible_(true);

			});
		})
		.addToggleRequestNew(
			"/shuttleControl/"++index+/+mode++"/scalesRand",
			Rect(230,20,20,40),
			this,
			view
		);

		scaleValuesView = CompositeView(view,Rect(250,20,130,40));//.background_(Color.clear);
		randomValuesView = CompositeView(view,Rect(250,20,130,40));//.background_(Color.clear);


		EZNumber(randomValuesView,Rect(0,0,110,20),"max",ControlSpec(-5,5),{
			arg nb;
			randMax = nb.value;
		},5,true,labelWidth:30)
		.addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/valMax",
			ControlSpec(-5,5),
			Rect(110,0,20,20),
			this,
			randomValuesView
		);

		EZNumber(randomValuesView,Rect(0,20,110,20),"min",ControlSpec(-5,5),{
			arg nb;
			randMin = nb.value;
		},-5,true,labelWidth:30)
		.addHandleRequestNew(
			"/shuttleControl/"++index+/+mode++"/valMin",
			ControlSpec(-5,5),
			Rect(110,20,20,20),
			this,
			randomValuesView
		);

		valuesArrayTF = TextField(scaleValuesView,Rect(0,0,130,20))
		.action_({
			arg tv;
			valuesArray = tv.value.interpret;
			this.updateValuesPattern;
			tv.value.postln;
		}).string_("[60]");

		patternTypePum = PopUpMenu(scaleValuesView,Rect(0,20,130,20))
		.items_(patternTypes)
		.action_({
			arg pum;
			patternType = patternTypes[pum.value];
			this.updateValuesPattern;
		});

		randomValuesView.visible_(false);

		task = Task({
			inf.do({
				var v;

				if(scale,{
					v = valuesPattern.next;
				},{
					v = rrand(randMax,randMin).linlin(-5,5,0,127).floor;
				});

				//toShuttleControl.control(1,index,v);
				this.sendValue(v);
				//"Value: ".post; v.postln;
				if(valueGate,{
					//toShuttleControl.control(1,gateOut,127);
					this.sendValue(127);
					//"Value Gate: 1".postln;
					AppClock.sched(gateDur,{
						//toShuttleControl.control(1,gateOut,63);
						this.sendValue(63);
						//"Value Gate: 0".postln;
						nil;
					});
				});

				if(steady,{
					steadyDur.wait;
				},{
					rrand(jitterMin,jitterMax).wait;
				});
			});
		});

		this.updateValuesPattern;
	}

	updateValuesPattern {
		// pattern tyeps:
		// up, down, pingpong, random
		patternType.switch(
			patternTypes[0],{
				valuesPattern = Pseq(valuesArray,inf).asStream;
			},
			patternTypes[1],{
				valuesPattern = Pseq(valuesArray.reverse,inf).asStream;
			},
			patternTypes[2],{
				valuesPattern = Pseq(valuesArray.mirror1,inf).asStream;
			},
			patternTypes[3],{
				valuesPattern = Pxrand(valuesArray,inf).asStream;
			},
		);
	}

	save {
		var dict;
		dict = super.save;

		// gotta save pattern type
		dict.put(\patternType,patternType);
		// gotta save pattern values array
		dict.put(\valuesArray,valuesArray);
		// gotta save if it's random values or values from a scale
		//dict.put(\scalesRandom,scalesRandomBut.value);

		^dict;
	}

	load {
		arg dict;

		valuesArrayTF.valueAction_(dict.at(\valuesArray));
		patternTypePum.valueAction_(dict.at(\patternType));
		//scalesRandomBut.valueAction_(dict.at(\scalesRandom));

		this.updateValuesPattern;

		super.load(dict);
	}
}

Shuttle_Controller {
	var index, server, color, manager, toShuttleControl, <view, modePum, modes, modeTypes;

	init {
		arg index_, manager_, midiOut_, color_, server_;
		server = server_;
		index = index_;
		color = color_;
		manager = manager_;
		toShuttleControl = midiOut_;

		modeTypes = [
			\LFO,
			\Manual_Slider,
			\Manual_Toggle,
			\Gate,
			\Trigger,
			\Values
		];

		view = CompositeView(manager.win,Rect(0,0,380,80));
		//view.decorator_(FlowLayout(view.bounds,0@0,0@0));
		view.background_(color);
		StaticText(view,Rect(0,0,20,20))
		.string_(index)
		.align_(\center)
		.stringColor_(Color.white)
		.background_(Color.black);

		// MODE
		modePum = PopUpMenu(view,Rect(20,0,360,20)).items_(modeTypes)
		.action_({
			arg pum;
			this.setMode(modeTypes[pum.value]);
		});

		modes = Dictionary.new;
		modes.put(\LFO,Shuttle_Mode_LFO(index,this));
		modes.put(\Manual_Slider,Shuttle_Mode_Manual_Slider(index,this));
		modes.put(\Manual_Toggle,Shuttle_Mode_Manual_Toggle(index,this));
		modes.put(\Gate,Shuttle_Mode_Gate(index,this));
		modes.put(\Trigger,Shuttle_Mode_Trigger(index,this));
		modes.put(\Values,Shuttle_Mode_Values(index,this));

		this.setMode(\Manual_Slider);
	}

	setMode {
		arg m;
		modePum.value_(modeTypes.indexOf(m));
		modes.keysValuesDo({
			arg key, val;
			if(key == m,{
				val.show;
			},{
				val.hide;
			});
		});
	}

	save {
		var dict = Dictionary.new;
		// gotta save mode (this one does not need to call on
		// the HasAssignableGUIs methods !!!!!!!!!!!!!!!!

		dict.put(\mode,modePum.value); // this saves it as an integer!

		modes.keysValuesDo({
			arg key, val;
			dict.put(key,val.save);
		});
		^dict;
	}

	load {
		arg dict;

		modes.keysValuesDo({
			arg key, value;
			modes.at(key).load(dict.at(key));
		});

		modePum.valueAction_(dict.at(\mode));
	}
}

Shuttle_Control_Manager {
	var winWidth = 792,
	winHeight = 684,
	<win,
	yellow,
	controllers,
	toShuttleControl,
	server;

	*new {
		arg server;
		^super.new.init(server);
	}

	*load {
		arg server, loadFile;
		if(PathName(loadFile).isFile,{
			^super.new.init(server,loadFile);
		},{
			"".warn;
			"---------------------------------".postln;
			"|                               |".postln;
			"| That is not a valid file path |".postln;
			"|       Shuttle Control         |".postln;
			"|                               |".postln;
			"---------------------------------".postln;
		});
	}

	init {
		arg server_, loadFile;
		var sources;
		server = server_;
		yellow = Color.new255(255,255,40);
		controllers = Dictionary.new;

		MIDIClient.init;
		sources = MIDIClient.sources.collect({
			arg m;
			m.name.asSymbol;
		});

		Shuttle_Mode.server_(server);

		if(sources.includes('Shuttle Control v3'),{
			toShuttleControl = MIDIOut.newByName("Shuttle Control v3","Shuttle Control v3");
			toShuttleControl.latency_(0);
			Shuttle_Mode.connected_(true);
			Shuttle_Mode.toShuttleControl_(toShuttleControl);
			"---Connected to Shuttle Control v3".postln;
		},{
			Shuttle_Mode.connected_(false);
			"---Shuttle Control v3 not detected!".warn;
		});

		win = Window("Shuttle Control",Rect(
			(Window.screenBounds.width-winWidth)/2,
			(Window.screenBounds.height-winHeight)/2,
			winWidth,
			winHeight
		));
		win.view.decorator_(FlowLayout(win.bounds,4@4,4@4));

		Task({
			16.do({
				arg i;
				var index;
				index = [1,9,2,10,3,11,4,12,5,13,6,14,7,15,8,16][i];
				controllers.put(index,Shuttle_Controller.new.init(index,this,toShuttleControl,yellow,server));
				if((i%2) == 1,{win.view.decorator.nextLine});
			});

			Button(win,Rect(0,0,100,20))
			.states_([["Save",Color.black,Color.white]])
			.action_({
				arg b;
				Dialog.savePanel({
					arg path;
					this.save.writeArchive(path);
				});
			});

			Button(win,Rect(0,0,100,20))
			.states_([["Load",Color.black,Color.new255(193,247,193)]])
			.action_({
				arg b;
				Dialog.openPanel({
					arg path;
					this.load(Object.readArchive(path));
				});
			});

			win.bounds_(win.view.decorator.used);
			win.front;

			if(loadFile.notNil,{
				this.load(Object.readArchive(loadFile));
			});
		},AppClock).play;
	}

	save {
		var dict = Dictionary.new;
		controllers.keysValuesDo({
			arg key, val;
			dict.put(key,val.save);
		});
		^dict;
	}

	load {
		arg dict;
		dict.keysValuesDo({
			arg key, val;
			controllers.at(key).load(val);
		});
	}
}