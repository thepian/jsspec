/**
 * JSSpec
 *
 * Copyright 2007 Alan Kang
 *  - mailto:jania902@gmail.com
 *  - http://jania.pe.kr
 *
 * Additional work and productisation Copyright 2007 Henrik Vendelbo
 *  - mailto: hvendelbo.dev@gmail.com
 *
 * Included works:
 *	cssQuery, version 2.0.2 (2005-08-19)
 *	Copyright: 2004-2005, Dean Edwards (http://dean.edwards.name/)
 *	License: http://creativecommons.org/licenses/LGPL/2.1/
 *
 * http://jania.pe.kr/aw/moin.cgi/JSSpec
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */

/**
 * Creates JSSpec for each window loading the script.
 * @param {DOMWindow} specWindow Takes the page window as parameter to ensure that the correct window is added to the scope, for consistency across browsers.
 */
(function (specWindow) {

	var JSSPEC_SCRIPT_NAME = "JSSpec.js";
	var JSSPEC_RUNNER_NAME = 'JSSpecRunner.html';
	var JSSPEC_HEIGHT = "45px";

	if (!specWindow.console) specWindow.console = { log:function(){}};

	var specPage = {
		specs: [],
		getSpecByContext : function(context) {
			for(var i = 0, l = this.specs.length; i < l; i++) {
				if(this.specs[i].context == context) return this.specs[i];
			}
			return null;
		},

		location: specWindow.location,
		specWindow: specWindow,
		scriptText: "",

		MAXPULSE: 80,
		INTERVAL: 100,
		stopPulse: function(runner) {
			if (this.interval) {
				specWindow.clearInterval(this.interval);
				console.log("pulse cleared "+this.interval);
				delete this.interval;
			}
		},
		startPulse: function(runner) {
			var specPage = this; // improves readability, and a fraction speed
			this.interval = specWindow.setInterval(runnerPulse,this.INTERVAL); // keep track of progress

			function runnerPulse() {
				if (runner.haltRun) return;

				runner.onRunnerStart();
				var now = new Date();
				var pulseStarted = now.valueOf();
				var pulseUntil = pulseStarted + specPage.MAXPULSE;

				var rl = runner.outstanding.length;
				while(rl > 0 && now.valueOf() < pulseUntil) {
					var executor = runner.outstanding[rl-1];
					if (!executor.todo) {
						runner.outstanding.pop();
					}
					else {
						executor.spec.started = now;
						executor.run();
						if (runner.finishPulse) { runner.finishPulse = false; return; }
					}

					now = new Date();
					rl = runner.outstanding.length;
				}
				if (rl <= 0) {
					specPage.stopPulse(runner);
					runner.onRunnerEnd();
				}
			}
		}
	}

	var Browser = {
		Trident: navigator.appName == "Microsoft Internet Explorer",
		TridentWin32 : typeof ActiveXObject != "undefined" && navigator.appName == "Microsoft Internet Explorer",
		Webkit: navigator.userAgent.indexOf('AppleWebKit/') > -1,
		Gecko: navigator.userAgent.indexOf('Gecko') > -1 && navigator.userAgent.indexOf('KHTML') == -1,
		Presto: navigator.appName == "Opera"
	}

	var JSSpec = {
		version: 0.1,
		options: {}
	};

	function EMPTY_FUNCTION() {}
	EMPTY_FUNCTION.empty = true;

	/**
	 * Mixin for strings
	 */
	function normalizeHtml() {
		var html = this;

		// Uniformize quotation, turn tag names and attribute names into lower case
		html = html.replace(/<(\/?)(\w+)([^>]*?)>/img, function(str, closingMark, tagName, attrs) {
			var sortedAttrs = JSSpec.util.sortHtmlAttrs(JSSpec.util.correctHtmlAttrQuotation(attrs).toLowerCase())
			return "<" + closingMark + tagName.toLowerCase() + sortedAttrs + ">"
		});

		// validation self-closing tags
		html = html.replace(/<(br|hr|img)([^>]*?)>/mg, function(str, tag, attrs) {
			return "<" + tag + attrs + " />";
		});

		// append semi-colon at the end of style value
		html = html.replace(/style="(.*?)"/mg, function(str, styleStr) {
			styleStr = JSSpec.util.sortStyleEntries(styleStr.strip()); // for Safari
			if(styleStr.charAt(styleStr.length - 1) != ';') styleStr += ";"

			return 'style="' + styleStr + '"'
		});

		// sort style entries

		// remove empty style attributes
		html = html.replace(/ style=";"/mg, "");

		// remove new-lines
		html = html.replace(/\r/mg, '');
		html = html.replace(/\n/mg, '');

		return html;
	}

	// Used to glue iframes to the browser frame
	function FrameGlue(affect) {
		this.affect = affect;
	}
	FrameGlue.prototype.onResize = function(el,sizes) {
		if (this.affect.width) {
			el.width = sizes.width - (Browser.Trident? 15 : 0);
			el.style.width = sizes.width - (Browser.Trident? 15 : 0);
		}
		if (this.affect.height) {
			el.height = sizes.height - Browser.Trident? parseInt(JSSPEC_HEIGHT) : 0;
			el.style.height = sizes.height - Browser.Trident? parseInt(JSSPEC_HEIGHT) : 0;
		}
	};

	/**
	 * Mixed in to the runner iframe to provide functions for updating the size.
	 * To get around IE positioning bugs it needs to be embedded into an enclosing DIV,
	 * that is also absolutely positioned.
	 */
	var runnerFrameMixin = {
		title : "JSSpec Runner",
		id : "jsspec_runner",
		name : "jsspec_runner",
		width : (Browser.Webkit || Browser.Trident)? "100%" : undefined,
		height : 0,
		border : 0,
		frameborder : 0,
		frameBorder : 0,
		allowtransparency: "true",
		scrolling : "no",
		state: { manipulateHost: true },

		setInitialAttributes: function() {
			this.jsspecGlue = new FrameGlue({width:true});
			this.setAttribute("style","display:block; position:absolute; height:0; top:0; width:100%; left:0; border:0 none; margin:0; padding:0;");
			this.style.display = "block";
			this.style.position = "absolute";
			this.style.zIndex = "10000";
		},
		/**
		 @param autototals hover|true|false
		*/
		showTotals : function(autototals) {
			var body = top.document.body;
			var cs = body.currentStyle || top.getComputedStyle(body,"");
			if (autototals == "hover") this.state.maniupulateHost = false;
			var manipulateHost = this.state.maniupulateHost;
			if (manipulateHost) {
				if (cs.position == "static") {
					body.style.position = "relative";
					body.style.margin = "0px";
				}
				body.style.paddingTop = JSSPEC_HEIGHT+" 0px 0px 0px";
				onWindowResize();// let clientHeight adjust
			}
			this.height = parseFloat(JSSPEC_HEIGHT);
			this.style.height = JSSPEC_HEIGHT;
			this.style.top = "0px";
			this.border = "0";
		},
		hideTotals : function() {
			this.height = "0";
			this.style.height = "0px";
			this.style.marginTop = "0px";
			onWindowResize();
		},

		addSpecFrame: function(link) {
			var iframe = top.document.createElement("IFRAME");
			var src = link.href || link.src;
			src += (/\?/.test(src)? '&' : '?') + "cacheBreaker=" + String(new Date().valueOf());
			iframe.src = src;
			iframe.jsspecGlue = new FrameGlue({width:true,height:true});

			iframe.setAttribute("style","display:block; position:absolute; height:0; top:0; margin-top:"+JSSPEC_HEIGHT+"; left:0; width:100%; border:0 none; margin:0; padding:0;");
			iframe.width = "100%";
			iframe.height = 0;
			iframe.style.top = "0px";
			iframe.style.marginTop = JSSPEC_HEIGHT;
			iframe.style.display = "block";
			iframe.style.position = "absolute";
			iframe.style.visibility = "hidden";
			try {
				top.document.body.insertBefore(iframe,this);
			} catch(ex) {
				debugger;
			}
		},

		addSpecLinks: function(runlinks) {
			runlinks = (runlinks === true || runlinks == "true" || runlinks == "1" || runlinks === 1 || runlinks == "all" || runlinks == "yes")? true : runlinks;

			var links = top.document.getElementsByTagName("LINK");
			for(var i=0,l=links.length;i<l;++i) {
				var link = links[i];
				if ((runlinks === true || runlinks == link.rev) && link.rel && (link.rel.toLowerCase() == "spec" || link.rel.toLowerCase() == "jsspec")) {
					this.addSpecFrame(link);
				}
			}
			var anchors = top.document.getElementsByTagName("A");
			for(var i=0,l=anchors.length;i<l;++i) {
				var anchor = anchors[i];
				if ((runlinks === true || runlinks == anchor.rev) && anchor.rel && (anchor.rel.toLowerCase() == "spec" || anchor.rel.toLowerCase() == "jsspec")) {
					this.addSpecFrame(anchor);
				}
			}
			onWindowResize();
		},

		showNewHeight : function(height,padding) {
			specWindow.setTimeout(function(){
				runnerIframe.height = height;
				runnerIframe.style.height = height + "px";
				top.document.body.style.paddingTop = (padding || parseInt(JSSPEC_HEIGHT)) + "px";
			},0);
		}
	};

	var runnerReadyHandle;
	var runnerIframe;


	function onWindowResize() {
		top.setTimeout(deferredWindowResize,15);
	}

	function deferredWindowResize() {
		var body = top.document.body;
		var bodyStyle = body.currentStyle || top.getComputedStyle(body,"");
		var de = top.document.documentElement || body;
		// Ignoring border styles as noone seems to apply it to body
		var sizes = {
			top: parseInt(bodyStyle.paddingTop || "0") + parseInt(bodyStyle.marginTop || "0"),
			bottom: parseInt(bodyStyle.paddingBottom || "0") + parseInt(bodyStyle.marginBottom || "0"),
			left: parseInt(bodyStyle.paddingLeft || "0") + parseInt(bodyStyle.marginLeft || "0"),
			right: parseInt(bodyStyle.paddingRight || "0") + parseInt(bodyStyle.marginRight || "0")
		};
		sizes.width = (de.clientWidth || de.offsetWidth) - sizes.left - sizes.right;
		sizes.height = 	(de.clientHeight || de.offsetHeight) - sizes.top - sizes.bottom;
		if (body.jsspecGlue && body.jsspecGlue.affect.width) {
			body.style.width = sizes.width + "px"; // track size of window
		}
		if (body.jsspecGlue && body.jsspecGlue.affect.height) {
			body.style.height = sizes.height + "px"; // track size of window
		}
		var iframes = body.getElementsByTagName("IFRAME");
		for(var i = iframes.length-1;i>=0;--i)
			if (iframes[i].jsspecGlue) iframes[i].jsspecGlue.onResize(iframes[i],sizes);
	}

	/**
	 * When DOM & Images have been loaded
	 */
	function onWindowLoad() {
		if (top == specWindow) {
			if (specWindow.addEventListener) specWindow.addEventListener("resize",onWindowResize,false);
			else if (specWindow.attachEvent) specWindow.attachEvent("onresize",onWindowResize);
			else specWindow.onresize = onWindowResize;
		}

		var jsspecPath = ""; // prefix for all jsspec resources
		var scriptOptions = "";

		for(var i=0,scripts=document.getElementsByTagName("HEAD")[0].childNodes;i<scripts.length;++i)
			if (scripts[i].tagName == "SCRIPT") {
				if (scripts[i].src) {
					var src = scripts[i].src;
					var path,search = "";
					var q = src.indexOf("?");
					path = (q==-1)? src : src.slice(0,q);
					search = (q==-1)? src : src.slice(q);
					var lastSlash = path.lastIndexOf('/');
					if (path.slice(lastSlash+1) == JSSPEC_SCRIPT_NAME) {
						jsspecPath = path.slice(0,lastSlash+1);
						scriptOptions = search;
					}
				} else {
					var src = scripts[i].text || scripts[i].textContent;
					if (/describe\(/.test(src)) {
						specPage.scriptText += src;
					}
				}
			}

		specPage.scriptOptions = {};
		var so = specPage.scriptOptions;
		if (scriptOptions) {
			var pairs = scriptOptions.slice(1).split("&");
			for(var i = 0; i < pairs.length; i++) {
				var tokens = pairs[i].split('=');
				tokens[1] = decodeURIComponent(tokens[1]);
				if (tokens[1] == "false") tokens[1] = false;
				if (tokens[1] == "0") tokens[1] = 0;
				so[tokens[0]] = tokens[1];
			}
			if (so.runnername) JSSPEC_RUNNER_NAME = so.runnername;
		}
		var topOptions = {};
		if (top.location.search) {
			var pairs = top.location.search.slice(1).split("&");
			for(var i = 0; i < pairs.length; i++) {
				var tokens = pairs[i].split('=');
				tokens[1] = decodeURIComponent(tokens[1]);
				if (tokens[1] == "false") tokens[1] = false;
				if (tokens[1] == "0") tokens[1] = 0;
				topOptions[tokens[0]] = tokens[1];
			}
		}
		var makeRunner = so.autorun || (so.autototals || so.autototals === undefined) || topOptions.autorun || topOptions.autototals;

		if (makeRunner) {
			top.document.body.setAttribute("jsspecGlue", new FrameGlue({})); // Set glue on body

			runnerIframe = top.document.getElementById(runnerFrameMixin.id);
			if (!runnerIframe) {
				runnerIframe = top.document.createElement("IFRAME");
				runnerIframe.src = jsspecPath + JSSPEC_RUNNER_NAME + scriptOptions;
				for(var name in runnerFrameMixin) {
					if (runnerFrameMixin[name] !== undefined) runnerIframe[name] = runnerFrameMixin[name];
				}
				runnerIframe.setInitialAttributes();
	//			top.document.body.insertBefore(runnerIframe,top.document.body.firstChild);
				top.document.body.appendChild(runnerIframe);
				runnerIframe.contentWindow.runnerIframe = runnerIframe;
			}

			if (!tryRunnerReady()) {
				runnerReadyHandle = specWindow.setInterval(tryRunnerReady,30);
			}
		}
	}

	/**
	 * @return true if runner was ready, and onRunnerReady was called
	 */
	function tryRunnerReady() {
		if (runnerIframe.contentWindow.JSSpec && runnerIframe.contentWindow.JSSpec.isReady) {
			if (runnerReadyHandle) specWindow.clearInterval(runnerReadyHandle);
			console.log("ready cleared "+runnerReadyHandle);
			onRunnerReady(runnerIframe.contentWindow,runnerIframe);
			specPage.handlerDone = true;
			return true;
		}
		return false;
	}
	var diff_match_patch;

	/**
	 * Is called when JSSpecRunner.html scripts have been parsed, but potentially before the onload event.
	 */
	function onRunnerReady(runnerWindow,iframe) {
		diff_match_patch = runnerWindow.diff_match_patch;
		JSSpec.util = runnerWindow.JSSpec.util;
		JSSpec.util.extendTypeMap(specWindow);
		JSSpec.util.typeMap[JSSpec.DSL.Subject] = "Subject";
		JSSpec.PropertyLengthMatcher.prototype.typeMap = JSSpec.util.typeMap;
		JSSpec.EqualityMatcher.typeMap = JSSpec.util.typeMap;
		JSSpec.PatternMatcher.prototype.typeMap = JSSpec.util.typeMap;
		JSSpec.DSL.Subject.prototype.typeMap = JSSpec.util.typeMap;

		JSSpec.options = runnerWindow.JSSpec.options;
		JSSpec.defaultOptions = runnerWindow.JSSpec.defaultOptions;
		JSSpec.describeOptions = runnerWindow.JSSpec.describeOptions;

		var jsspec_current_options = specWindow.document.getElementById("jsspec_current_options");
		if (jsspec_current_options) {
			jsspec_current_options.innerHTML = JSSpec.util.formatOptionsAsTable(JSSpec.options,JSSpec.defaultOptions,JSSpec.describeOptions);
		}

		JSSpec.Example.prototype.pageId = JSSpec.Spec.prototype.pageId = runnerWindow.JSSpec.specPageId++;

		if (JSSpec.options.autototals) {
			iframe.showTotals(JSSpec.options.autototals);
		} else {
			iframe.hideTotals();
		}
		if (JSSpec.options.rerun) {
			var spec = specPage.getSpecByContext(decodeURIComponent(JSSpec.options.rerun));
			specPage.specs = spec? [spec] : [];
		}
		if (top != specWindow || (JSSpec.options.autorun != "links" && JSSpec.options.run != "links")) {
			runnerWindow.JSSpec.runSpecs(specPage);
			console.log("queued specPage");
		}
	}


	/**
	 * Spec is a set of Examples in a specific context
	 */
	function Spec(context, entries, urlpath) {
		this.id = Spec.id++;
		this.context = context;
		this.urlpath = urlpath;

		this.filterEntriesByEmbeddedExpressions(entries);
		this.extractOutSpecialEntries(entries);
		this.examples = this.makeExamplesFromEntries(entries);
		this.examplesMap = this.makeMapFromExamples(this.examples);
	}
	JSSpec.Spec = Spec;

	Spec.id = 0;
	Spec.prototype.pageId = 0; //set when runner is ready

	Spec.prototype.getId = function() {
		return this.pageId + "_" + this.id;
	};

	Spec.prototype.getExamples = function() {
		return this.examples;
	};

	Spec.prototype.hasException = function() {
		return this.getTotalFailures() > 0 || this.getTotalErrors() > 0;
	};

	Spec.prototype.getTotalFailures = function() {
		var examples = this.examples;
		var failures = 0;
		for(var i = 0; i < examples.length; i++) {
			if(examples[i].isFailure()) failures++;
		}
		return failures;
	};

	Spec.prototype.getTotalErrors = function() {
		var examples = this.examples;
		var errors = 0;
		for(var i = 0; i < examples.length; i++) {
			if(examples[i].isError()) errors++;
		}
		return errors;
	};

	Spec.prototype.filterEntriesByEmbeddedExpressions = function(entries) {
		var isTrue;
		for(var name in entries) {
			var m = name.match(/\[\[(.+)\]\]/);
			if(m && m[1]) {
				eval("isTrue = (" + m[1] + ")");
				if(!isTrue) delete entries[name];
			}
		}
	};

	Spec.prototype.extractOutSpecialEntries = function(entries) {
		this.beforeEach = EMPTY_FUNCTION;
		this.beforeAll = EMPTY_FUNCTION;
		this.afterEach = EMPTY_FUNCTION;
		this.afterAll = EMPTY_FUNCTION;

		for(var name in entries) {
			if(name == 'before' || name == 'before each' || name == 'before_each') {
				this.beforeEach = entries[name];
			} else if(name == 'before all' || name == 'before_all') {
				this.beforeAll = entries[name];
			} else if(name == 'after' || name == 'after each' || name == 'after_each') {
				this.afterEach = entries[name];
			} else if(name == 'after all' || name == 'after_all') {
				this.afterAll = entries[name];
			}
		}

		delete entries['before'];
		delete entries['before each'];
		delete entries['before_each'];
		delete entries['before all'];
		delete entries['before_all'];
		delete entries['after'];
		delete entries['after each'];
		delete entries['after_each'];
		delete entries['after all'];
		delete entries['after_all'];
	};

	Spec.prototype.makeExamplesFromEntries = function(entries) {
		var examples = [];
		for(var name in entries) {
			examples.push(new Example(this, name, entries[name], this.beforeEach, this.afterEach));
		}
		return examples;
	};

	Spec.prototype.makeMapFromExamples = function(examples) {
		var map = {};
		for(var i = 0; i < examples.length; i++) {
			var example = examples[i];
			map[example.id] = examples[i];
		}
		return map;
	};

	Spec.prototype.getExampleById = function(id) {
		return this.examplesMap[id];
	};

	function enableErrorHandler(instance,baseException,executor) {
		specWindow.onerror = function(message, fileName, lineNumber) {

			baseException.fileName = fileName;
			baseException.lineNumber = lineNumber;

			instance.exception = baseException;
			executor.onException(instance,baseException);
			disableErrorHandler();
			return true;
		}
	}

	function disableErrorHandler() {
		delete specPage.secondPass;
		delete specWindow.onerror;
	}

	var currentSpec;
	var currentExample;

	/*
		@return Exception if failure, or null if success
	*/
	function testExampleMethod(methodName,executor) {
		if (this instanceof Spec) currentSpec = this;
		else currentExample = this;
		try {
			var result = this[methodName].call(this,arguments[2],arguments[3]);
			return null;
		}
		catch(ex) {
			ex.fileName2 = ex.fileName || ex.sourceURL;
			ex.lineNumber2 = ex.lineNumber || ex.line;
			return ex;
		}
	}

	function runSpecOrExampleMethod(methodName,executor) {
		if (this instanceof Spec) currentSpec = this;
		else currentExample = this;
		try {
			var result = this[methodName].call(this,arguments[2],arguments[3]);
			executor.onSuccess(this,result);
		}
		catch(ex) {
			ex.fileName2 = ex.fileName || ex.sourceURL;
			ex.lineNumber2 = ex.lineNumber || ex.line;

			if (!JSSpec.options.singlepass && !specPage.inDelayedPulse) {
				specPage.secondPass = true;
				if (!ex.lineNumber2) {
					if (!JSSpec.options.debugging) {
						enableErrorHandler(this,ex,executor);
						this[methodName].call(this);
					}
					else delete specPage.secondPass;
				} else {
					try {
						this[methodName].call(this);
					}
					catch(ex2) {
						delete specPage.secondPass;
						ex.fileName2 = ex2.fileName;
						ex.lineNumber2 = ex2.lineNumber;
					}
				}
			}
			this.exception = ex;
			executor.onException(this,ex);
			executor.onAfterException(this,ex);
		}
		return false;
	}

	Spec.prototype.run = runSpecOrExampleMethod;

	function hasNonEmptyFunction(name) {
		return (typeof this[name] == "function" && !this[name].empty);
	}
	Spec.prototype.hasNonEmptyFunction = hasNonEmptyFunction;

	/**
	 * Example
	 */
	function Example(spec, name, target, before, after) {
		this.spec = spec;
		this.id = Example.id++;
		this.name = name;
		this.target = target;
		this.before = before;
		this.after = after;

		this.delayed = []; // delayed Subject verification
	}
	JSSpec.Example = Example;

	Example.id = 0;
	Example.pageId = 0; // Set when runner is ready
	Example.prototype.getId = function() {
		return this.pageId + "_" + this.id;
	}
	Example.prototype.isFailure = function() {
		return this.exception && this.exception.type == "failure";
	};

	Example.prototype.isError = function() {
		return this.exception && !this.exception.type;
	};

	Example.prototype.run = runSpecOrExampleMethod;
	Example.prototype.test = testExampleMethod;
	Example.prototype.hasNonEmptyFunction = hasNonEmptyFunction;

	Example.prototype.delayedPulse = function(executor) {
		var now = new Date().getTime();
		var unfinished = 0;
		for(var i=0;i<this.delayed.length;++i) {
			var subject = this.delayed[i];
			if (!subject.done) {
				var limit = Math.max(subject.afterStamp||0,subject.withinStamp||0);
				if (now < limit) {
					// within clause, and not passed yet
					if (subject.withinStamp && !subject.withinPass) {
						var ex = subject.exception = this.test("_pulse",executor,subject);
						subject.withinPass = (ex == null);
						subject.done = (subject.withinPass && (!subject.afterStamp || subject.afterPass)); // within passed and no after clause
					}
					if (!subject.done) ++unfinished;
				} else {
					// after clause and no within failure
					if (subject.afterStamp && (subject.withinPass || !subject.withinStamp)) {
						var ex = subject.exception = this.test("_lastPulse",executor,subject);
						subject.afterPass = (ex == null);
					}
					subject.done = true; //subject.exception now reflects result of delayed tests
				}
			}
		}
		if (unfinished == 0) {
			for(var i=0;i<this.delayed.length;++i) {
				var subject = this.delayed[i];
				if (subject.exception) {
					this.exception = subject.exception;
					executor.onException(this,subject.exception);
					executor.onAfterException(this,subject.exception);
					return false;
				}
			}
			return false;
		}
		return "sleep";
	};

	Example.prototype._pulse = function(subject) {
		subject.pulse();
	}

	Example.prototype._lastPulse = function(subject) {
		subject.lastPulse();
	}

	/* Derived Error objects dont get lineNumber info */
	function ExampleFailure(message) {
		var e = new Error(message);
		e.type = "failure";
		e.name = "ExampleFailure";
		e.uploadMessage = message;
		return e;
	}

	/**
	 * IncludeMatcher
	 */
	function IncludeMatcher(actual, expected, condition) {
		this.actual = actual;
		this.expected = expected;
		this.condition = condition;
		this.match = false;
		this.explaination = this.makeExplain();
	}
	JSSpec.IncludeMatcher = IncludeMatcher;

	IncludeMatcher.createInstance = function(actual, expected, condition) {
		return new IncludeMatcher(actual, expected, condition);
	};

	IncludeMatcher.prototype.matches = function() {
		return this.match;
	};

	IncludeMatcher.prototype.explain = function() {
		return this.explaination;
	};

	IncludeMatcher.prototype.makeExplain = function() {
		if(typeof this.actual.length == 'undefined') {
			return this.makeExplainForNotArray();
		} else {
			return this.makeExplainForArray();
		}
	};

	IncludeMatcher.prototype.makeExplainForNotArray = function() {
		var sb = [];
		sb.push('<p>actual value:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.actual) + '</p>');
		sb.push('<p>should ' + (this.condition ? '' : 'not') + ' include:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.expected) + '</p>');
		sb.push('<p>but since it\s not an array, include or not doesn\'t make any sense.</p>');
		return sb.join("");
	};

	IncludeMatcher.prototype.makeExplainForArray = function() {
		var matches;
		if(this.condition) {
			for(var i = 0; i < this.actual.length; i++) {
				matches = JSSpec.EqualityMatcher.createInstance(this.expected, this.actual[i]).matches();
				if(matches) {
					this.match = true;
					break;
				}
			}
		} else {
			for(var i = 0; i < this.actual.length; i++) {
				matches = JSSpec.EqualityMatcher.createInstance(this.expected, this.actual[i]).matches();
				if(matches) {
					this.match = false;
					break;
				}
			}
		}

		if(this.match) return "";

		var sb = [];
		sb.push('<p>actual value:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.actual, false, this.condition ? null : i) + '</p>');
		sb.push('<p>should ' + (this.condition ? '' : 'not') + ' include:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.expected) + '</p>');
		return sb.join("");
	};

	/**
	 * PropertyLengthMatcher
	 */
	JSSpec.PropertyLengthMatcher = function(num, property, o, condition) {
		this.num = num;
		this.o = o;
		this.property = property;
		if((property == 'characters' || property == 'items') && typeof o.length != 'undefined') {
			this.property = 'length';
		}

		this.condition = condition;
		this.conditionMet = function(x) {
			if(condition == 'exactly') return x.length == num;
			if(condition == 'at least') return x.length >= num;
			if(condition == 'at most') return x.length <= num;

			throw new RangeError("Unknown condition '" + condition + "'");
		};
		this.match = false;
		this.explaination = this.makeExplain();
	};

	JSSpec.PropertyLengthMatcher.prototype.makeExplain = function() {
		if(this.typeMap[this.o.constructor] == 'String' && this.property == 'length') {
			this.match = this.conditionMet(this.o);
			return this.match ? '' : this.makeExplainForString();
		} else if(typeof this.o.length != 'undefined' && this.property == "length") {
			this.match = this.conditionMet(this.o);
			return this.match ? '' : this.makeExplainForArray();
		} else if(typeof this.o[this.property] != 'undefined' && this.o[this.property] != null) {
			this.match = this.conditionMet(this.o[this.property]);
			return this.match ? '' : this.makeExplainForObject();
		} else if(typeof this.o[this.property] == 'undefined' || this.o[this.property] == null) {
			this.match = false;
			return this.makeExplainForNoProperty();
		}

		this.match = true;
	};

	JSSpec.PropertyLengthMatcher.prototype.makeExplainForString = function() {
		var sb = [];

		var exp = this.num == 0 ?
			'be an <strong>empty string</strong>' :
			'have <strong>' + this.condition + ' ' + this.num + ' characters</strong>';

		sb.push('<p>actual value has <strong>' + this.o.length + ' characters</strong>:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.o) + '</p>');
		sb.push('<p>but it should ' + exp + '.</p>');

		return sb.join("");
	};

	JSSpec.PropertyLengthMatcher.prototype.makeExplainForArray = function() {
		var sb = [];

		var exp = this.num == 0 ?
			'be an <strong>empty array</strong>' :
			'have <strong>' + this.condition + ' ' + this.num + ' items</strong>';

		sb.push('<p>actual value has <strong>' + this.o.length + ' items</strong>:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.o) + '</p>');
		sb.push('<p>but it should ' + exp + '.</p>');

		return sb.join("");
	};

	JSSpec.PropertyLengthMatcher.prototype.makeExplainForObject = function() {
		var sb = [];

		var exp = this.num == 0 ?
			'be <strong>empty</strong>' :
			'have <strong>' + this.condition + ' ' + this.num + ' ' + this.property + '.</strong>';

		sb.push('<p>actual value has <strong>' + this.o[this.property].length + ' ' + this.property + '</strong>:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.o, false, this.property) + '</p>');
		sb.push('<p>but it should ' + exp + '.</p>');

		return sb.join("");
	};

	JSSpec.PropertyLengthMatcher.prototype.makeExplainForNoProperty = function() {
		var sb = [];

		sb.push('<p>actual value:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.o) + '</p>');
		sb.push('<p>should have <strong>' + this.condition + ' ' + this.num + ' ' + this.property + '</strong> but there\'s no such property.</p>');

		return sb.join("");
	};

	JSSpec.PropertyLengthMatcher.prototype.matches = function() {
		return this.match;
	};

	JSSpec.PropertyLengthMatcher.prototype.explain = function() {
		return this.explaination;
	};

	JSSpec.PropertyLengthMatcher.createInstance = function(num, property, o, condition) {
		return new JSSpec.PropertyLengthMatcher(num, property, o, condition);
	};

	function InstanceofMatcher(constructor,o) {
		//TODO
		this.explaination = "";
	}

	InstanceofMatcher.prototype.explain = function() {
		return this.explaination;
	}

	/**
	 * EqualityMatcher
	 */
	JSSpec.EqualityMatcher = {};

	JSSpec.EqualityMatcher.createInstance = function(expected, actual) {
		if(expected == null || actual == null) {
			return new JSSpec.NullEqualityMatcher(expected, actual);
		} else if(expected.constructor == actual.constructor) {
			if(this.typeMap[expected.constructor] == "String") {
				return new JSSpec.StringEqualityMatcher(expected, actual);
			} else if(this.typeMap[expected.constructor] == "Date") {
				return new JSSpec.DateEqualityMatcher(expected, actual);
			} else if(this.typeMap[expected.constructor] == "Number") {
				return new JSSpec.NumberEqualityMatcher(expected, actual);
			} else if(this.typeMap[expected.constructor] == "Array") {
				return new JSSpec.ArrayEqualityMatcher(expected, actual);
			} else if(this.typeMap[expected.constructor] == "Boolean") {
				return new JSSpec.BooleanEqualityMatcher(expected, actual);
			}
		}

		return new JSSpec.ObjectEqualityMatcher(expected, actual);
	};

	JSSpec.EqualityMatcher.basicExplain = function(expected, actual, expectedDesc, actualDesc) {
		var sb = [];

		sb.push(actualDesc || '<p>actual value:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(actual) + '</p>');
		sb.push(expectedDesc || '<p>should be:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(expected) + '</p>');

		return sb.join("");
	};

	JSSpec.EqualityMatcher.diffExplain = function(expected, actual) {
		var sb = [];

		sb.push('<p>diff:</p>');
		sb.push('<p style="margin-left:2em;">');

		var dmp = new diff_match_patch();
		var diff = dmp.diff_main(expected, actual);
		dmp.diff_cleanupEfficiency(diff);

		sb.push(JSSpec.util.inspect(dmp.diff_prettyHtml(diff), true));

		sb.push('</p>');

		return sb.join("");
	};

	/**
	 * BooleanEqualityMatcher
	 */
	JSSpec.BooleanEqualityMatcher = function(expected, actual) {
		this.expected = expected;
		this.actual = actual;
	};

	JSSpec.BooleanEqualityMatcher.prototype.explain = function() {
		var sb = [];

		sb.push('<p>actual value:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.actual) + '</p>');
		sb.push('<p>should be:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.expected) + '</p>');

		return sb.join("");
	};

	JSSpec.BooleanEqualityMatcher.prototype.matches = function() {
		return this.expected == this.actual;
	};

	/**
	 * NullEqualityMatcher
	 */
	JSSpec.NullEqualityMatcher = function(expected, actual) {
		this.expected = expected;
		this.actual = actual;
	};

	JSSpec.NullEqualityMatcher.prototype.matches = function() {
		return this.expected == this.actual && typeof this.expected == typeof this.actual;
	};

	JSSpec.NullEqualityMatcher.prototype.explain = function() {
		return JSSpec.EqualityMatcher.basicExplain(this.expected, this.actual);
	};

	JSSpec.DateEqualityMatcher = function(expected, actual) {
		this.expected = expected;
		this.actual = actual;
	};

	JSSpec.DateEqualityMatcher.prototype.matches = function() {
		return this.expected.getTime() == this.actual.getTime();
	};

	JSSpec.DateEqualityMatcher.prototype.explain = function() {
		var sb = [];

		sb.push(JSSpec.EqualityMatcher.basicExplain(this.expected, this.actual));
		sb.push(JSSpec.EqualityMatcher.diffExplain(this.expected.toString(), this.actual.toString()));

		return sb.join("");
	};

	/**
	 * ObjectEqualityMatcher
	 */
	JSSpec.ObjectEqualityMatcher = function(expected, actual) {
		this.expected = expected;
		this.actual = actual;
		this.match = this.expected == this.actual;
		this.explaination = this.makeExplain();
	};

	JSSpec.ObjectEqualityMatcher.prototype.matches = function() {return this.match};

	JSSpec.ObjectEqualityMatcher.prototype.explain = function() {return this.explaination};

	JSSpec.ObjectEqualityMatcher.prototype.makeExplain = function() {
		if(this.expected == this.actual) {
			this.match = true;
			return "";
		}

		if(JSSpec.util.isDomNode(this.expected)) {
			return this.makeExplainForDomNode();
		}

		var key, expectedHasItem, actualHasItem;

		for(key in this.expected) {
			expectedHasItem = this.expected[key] != null && typeof this.expected[key] != 'undefined';
			actualHasItem = this.actual[key] != null && typeof this.actual[key] != 'undefined';
			if(expectedHasItem && !actualHasItem) return this.makeExplainForMissingItem(key);
		}
		for(key in this.actual) {
			expectedHasItem = this.expected[key] != null && typeof this.expected[key] != 'undefined';
			actualHasItem = this.actual[key] != null && typeof this.actual[key] != 'undefined';
			if(actualHasItem && !expectedHasItem) return this.makeExplainForUnknownItem(key);
		}

		for(key in this.expected) {
			var matcher = JSSpec.EqualityMatcher.createInstance(this.expected[key], this.actual[key]);
			if(!matcher.matches()) return this.makeExplainForItemMismatch(key);
		}

		this.match = true;
	};

	JSSpec.ObjectEqualityMatcher.prototype.makeExplainForDomNode = function(key) {
		var sb = [];

		sb.push(JSSpec.EqualityMatcher.basicExplain(this.expected, this.actual));

		return sb.join("");
	};

	JSSpec.ObjectEqualityMatcher.prototype.makeExplainForMissingItem = function(key) {
		var sb = [];

		sb.push('<p>actual value has no item named <strong>' + JSSpec.util.inspect(key) + '</strong></p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.actual, false, key) + '</p>');
		sb.push('<p>but it should have the item whose value is <strong>' + JSSpec.util.inspect(this.expected[key]) + '</strong></p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.expected, false, key) + '</p>');

		return sb.join("");
	};

	JSSpec.ObjectEqualityMatcher.prototype.makeExplainForUnknownItem = function(key) {
		var sb = [];

		sb.push('<p>actual value has item named <strong>' + JSSpec.util.inspect(key) + '</strong></p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.actual, false, key) + '</p>');
		sb.push('<p>but there should be no such item</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.expected, false, key) + '</p>');

		return sb.join("");
	};

	JSSpec.ObjectEqualityMatcher.prototype.makeExplainForItemMismatch = function(key) {
		var sb = [];

		sb.push('<p>actual value has an item named <strong>' + JSSpec.util.inspect(key) + '</strong> whose value is <strong>' + JSSpec.util.inspect(this.actual[key]) + '</strong></p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.actual, false, key) + '</p>');
		sb.push('<p>but it\'s value should be <strong>' + JSSpec.util.inspect(this.expected[key]) + '</strong></p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.expected, false, key) + '</p>');

		return sb.join("");
	};




	/**
	 * ArrayEqualityMatcher
	 */
	JSSpec.ArrayEqualityMatcher = function(expected, actual) {
		this.expected = expected;
		this.actual = actual;
		this.match = this.expected == this.actual;
		this.explaination = this.makeExplain();
	};

	JSSpec.ArrayEqualityMatcher.prototype.matches = function() {return this.match};

	JSSpec.ArrayEqualityMatcher.prototype.explain = function() {return this.explaination};

	JSSpec.ArrayEqualityMatcher.prototype.makeExplain = function() {
		if(this.expected.length != this.actual.length) return this.makeExplainForLengthMismatch();

		for(var i = 0; i < this.expected.length; i++) {
			var matcher = JSSpec.EqualityMatcher.createInstance(this.expected[i], this.actual[i]);
			if(!matcher.matches()) return this.makeExplainForItemMismatch(i);
		}

		this.match = true;
	};

	JSSpec.ArrayEqualityMatcher.prototype.makeExplainForLengthMismatch = function() {
		return JSSpec.EqualityMatcher.basicExplain(
			this.expected,
			this.actual,
			'<p>but it should be <strong>' + this.expected.length + '</strong></p>',
			'<p>actual value has <strong>' + this.actual.length + '</strong> items</p>'
		);
	};

	JSSpec.ArrayEqualityMatcher.prototype.makeExplainForItemMismatch = function(index) {
		var postfix = ["th", "st", "nd", "rd", "th"][Math.min((index + 1) % 10,4)];

		var sb = [];

		sb.push('<p>' + (index + 1) + postfix + ' item (index ' + index + ') of actual value is <strong>' + JSSpec.util.inspect(this.actual[index]) + '</strong>:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.actual, false, index) + '</p>');
		sb.push('<p>but it should be <strong>' + JSSpec.util.inspect(this.expected[index]) + '</strong>:</p>');
		sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.expected, false, index) + '</p>');

		return sb.join("");
	};

	/**
	 * NumberEqualityMatcher
	 */
	JSSpec.NumberEqualityMatcher = function(expected, actual) {
		this.expected = expected;
		this.actual = actual;
	};

	JSSpec.NumberEqualityMatcher.prototype.matches = function() {
		if(this.expected == this.actual) return true;
	};

	JSSpec.NumberEqualityMatcher.prototype.explain = function() {
		return JSSpec.EqualityMatcher.basicExplain(this.expected, this.actual);
	};

	/**
	 * StringEqualityMatcher
	 */
	JSSpec.StringEqualityMatcher = function(expected, actual) {
		this.expected = expected;
		this.actual = actual;
	};

	JSSpec.StringEqualityMatcher.prototype.matches = function() {
		if(this.expected == this.actual) return true;
	};

	JSSpec.StringEqualityMatcher.prototype.explain = function() {
		var sb = [];

		sb.push(JSSpec.EqualityMatcher.basicExplain(this.expected, this.actual));
		sb.push(JSSpec.EqualityMatcher.diffExplain(this.expected, this.actual));
		return sb.join("");
	};

	/**
	 * PatternMatcher
	 */
	JSSpec.PatternMatcher = function(actual, pattern, condition) {
		this.actual = actual;
		this.pattern = pattern;
		this.condition = condition;
		this.match = false;
		this.explaination = this.makeExplain();
	};

	JSSpec.PatternMatcher.createInstance = function(actual, pattern, condition) {
		return new JSSpec.PatternMatcher(actual, pattern, condition);
	};

	JSSpec.PatternMatcher.prototype.makeExplain = function() {
		var sb;
		if(this.actual == null || this.typeMap[this.actual.constructor] != 'String') {
			sb = [];
			sb.push('<p>actual value:</p>');
			sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.actual) + '</p>');
			sb.push('<p>should ' + (this.condition ? '' : 'not') + ' match with pattern:</p>');
			sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.pattern) + '</p>');
			sb.push('<p>but pattern matching cannot be performed.</p>');
			return sb.join("");
		} else {
			this.match = this.condition == !!this.actual.match(this.pattern);
			if(this.match) return "";

			sb = [];
			sb.push('<p>actual value:</p>');
			sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.actual) + '</p>');
			sb.push('<p>should ' + (this.condition ? '' : 'not') + ' match with pattern:</p>');
			sb.push('<p style="margin-left:2em;">' + JSSpec.util.inspect(this.pattern) + '</p>');
			return sb.join("");
		}
	};

	JSSpec.PatternMatcher.prototype.matches = function() {
		return this.match;
	};

	JSSpec.PatternMatcher.prototype.explain = function() {
		return this.explaination;
	};

	/**
	 * Domain Specific Languages
	 */
	JSSpec.DSL = {};

	JSSpec.DSL.forString = {
		normalizeHtml : normalizeHtml
	};

	function Subject(target) {
		this.target = target;
	}
	JSSpec.DSL.Subject = Subject;

	Subject.prototype.getValue = function() {
		return this.target;
	};

	Subject.prototype.log = function(message) {
		if (console) console.log(message);
		return this;
	}

	//TODO Source description for failures. getSource blank for simple values, expression for complex.

	JSSpec.DSL.Subject.prototype.should_fail = function(message) {
		throw ExampleFailure(message);
	};

	JSSpec.DSL.Subject.prototype.should_be = function(expected) {
		var matcher = JSSpec.EqualityMatcher.createInstance(expected, this.getValue());
		if(!matcher.matches()) {
			throw ExampleFailure(matcher.explain());
		}
	};

	JSSpec.DSL.Subject.prototype.should_not_be = function(expected) {
		// TODO JSSpec.EqualityMatcher should support 'condition'
		var matcher = JSSpec.EqualityMatcher.createInstance(expected, this.getValue());
		if(matcher.matches()) {
			throw ExampleFailure("'" + this.getValue() + "' should not be '" + expected + "'");
		}
	};

	JSSpec.DSL.Subject.prototype.should_be_empty = function() {
		this.should_have(0, this.getType() == 'String' ? 'characters' : 'items');
	};

	JSSpec.DSL.Subject.prototype.should_not_be_empty = function() {
		this.should_have_at_least(1, this.getType() == 'String' ? 'characters' : 'items');
	};

	JSSpec.DSL.Subject.prototype.should_be_true = function() {
		this.should_be(true);
	};

	JSSpec.DSL.Subject.prototype.should_be_false = function() {
		this.should_be(false);
	};

	JSSpec.DSL.Subject.prototype.should_be_null = function() {
		this.should_be(null);
	};

	JSSpec.DSL.Subject.prototype.should_be_undefined = function() {
		this.should_be(undefined);
	};

	JSSpec.DSL.Subject.prototype.should_not_be_null = function() {
		this.should_not_be(null);
	};

	JSSpec.DSL.Subject.prototype.should_not_be_undefined = function() {
		this.should_not_be(undefined);
	};

	JSSpec.DSL.Subject.prototype._should_have = function(num, property, condition) {
		var matcher = JSSpec.PropertyLengthMatcher.createInstance(num, property, this.getValue(), condition);
		if(!matcher.matches()) {
			throw ExampleFailure(matcher.explain());
		}
	};
	JSSpec.DSL.Subject.prototype._should_have.nondelayed = true;

	JSSpec.DSL.Subject.prototype.should_have = function(num, property) {
		this._should_have(num, property, "exactly");
	};

	JSSpec.DSL.Subject.prototype.should_have_exactly = function(num, property) {
		this._should_have(num, property, "exactly");
	};

	JSSpec.DSL.Subject.prototype.should_have_at_least = function(num, property) {
		this._should_have(num, property, "at least");
	};

	JSSpec.DSL.Subject.prototype.should_have_at_most = function(num, property) {
		this._should_have(num, property, "at most");
	};

	JSSpec.DSL.Subject.prototype.should_include = function(expected) {
		var matcher = IncludeMatcher.createInstance(this.getValue(), expected, true);
		if(!matcher.matches()) {
			throw ExampleFailure(matcher.explain());
		}
	};

	JSSpec.DSL.Subject.prototype.should_not_include = function(expected) {
		var matcher = IncludeMatcher.createInstance(this.getValue(), expected, false);
		if(!matcher.matches()) {
			throw ExampleFailure(matcher.explain());
		}
	};

	JSSpec.DSL.Subject.prototype.should_match = function(pattern) {
		var matcher = JSSpec.PatternMatcher.createInstance(this.getValue(), pattern, true);
		if(!matcher.matches()) {
			throw ExampleFailure(matcher.explain());
		}
	}
	JSSpec.DSL.Subject.prototype.should_not_match = function(pattern) {
		var matcher = JSSpec.PatternMatcher.createInstance(this.getValue(), pattern, false);
		if(!matcher.matches()) {
			throw ExampleFailure(matcher.explain());
		}
	};

	//TODO recognise target type function

	JSSpec.DSL.Subject.prototype.getType = function() {
		var value = this.getValue();
		if(typeof value == 'undefined') {
			return 'undefined';
		} else if(value === null) {
			return 'null';
		} else if(this.typeMap[value.constructor]) {
			return this.typeMap[value.constructor];
		} else if(JSSpec.util.isDomNode(value)) {
			return 'DomNode';
		} else if(typeof value == 'function') {
			return "function";
		} else {
			return 'object';
		}
	};
	JSSpec.DSL.Subject.prototype.getType.nondelayed = true;

	Subject.prototype.after = function(secs) {
		var delayed = new DelayedSubject(this);
		delayed.afterStamp = new Date().getTime() + (secs * 1000);
		currentExample.delayed.push(delayed);
		return delayed;
	};
	Subject.prototype.after.nondelayed = true;

	Subject.prototype.within = function(secs,frequency) {
		var delayed = new DelayedSubject(this);
		delayed.withinStamp = new Date().getTime() + (secs * 1000);
		delayed.frequency = frequency;
		currentExample.delayed.push(delayed);
		return delayed;
	};
	Subject.prototype.within.nondelayed = true;

	function DelayedSubject(subject) {
		this.subject = subject;
		this.target = subject.target;
		this.example = currentExample;
		this.verify = [];
		this.withinStamp = 0;
		this.afterStamp = 0;
	}
	DelayedSubject.prototype = makeDelayedPrototype(Subject.prototype);

	function notSupported() {
		throw new ExampleException("Only one after/within expression is allowed per subject");
	}
	DelayedSubject.prototype.after = notSupported;
	DelayedSubject.prototype.within = notSupported;

	DelayedSubject.prototype.dummy = function() {
		var nothing = 0;
	};

	DelayedSubject.prototype.pulse = function() {
		specPage.inDelayedPulse = true;
		if (this.withinStamp) {
			for(var i=0;i<this.verify.length;++i) {
				var v = this.verify[i];
				v.func.apply(this.subject,v.args);
			}
		}
		delete specPage.inDelayedPulse;
	}

	DelayedSubject.prototype.lastPulse = function() {
		specPage.inDelayedPulse = true;
		for(var i=0;i<this.verify.length;++i) {
			var v = this.verify[i];
			v.func.apply(this.subject,v.args);
		}
		delete specPage.inDelayedPulse;
	}

	function makeDelayedCall(name,func) {
		return function() {
			this.verify.push({name:name,func:func,args:arguments});
			return this;
		}
	}

	/**
	 * Create a prototype with the same methods as the origin. Each method will push
	 * the call onto an internal stack.
	 */
	function makeDelayedPrototype(origin) {
		var proto = {};
		for(var funcName in origin) {
			var func = origin[funcName];
			if (!func.nondelayed) {
				proto[funcName] = makeDelayedCall(funcName,func);
			}
		}
		return proto;
	}
	/**
	 * Test the value and attributes of a DOM Element
	 * @param target Element
	 */
	function ElementSubject(target) {
		this.target = target;
		this.doc = this.target.ownerDocument || specWindow.document;
		this.inputType = null;
		if (this.target.tagName == "INPUT") {
			this.inputType = (this.target.type || "text").toLowerCase();
		}
		else if (this.target.tagName == "TEXTAREA") {
			this.inputType = "textarea";
		}
		else if (this.target.tagName == "BUTTON") {
			this.inputType = "button";
		}

	}
	ElementSubject.prototype = new Subject();

	function isVisible(el) {
		var p = el;
		while(p) {
			var style = p.currentStyle || getComputedStyle(el,"");
			if (style.visibility == "hidden") return false;
			if (style.display == "none") return false;
			p = p.offsetParent;
		}
		return true;
	}

	function getXY(el) {
        var p, pe, b, scroll, bd = (document.body || document.documentElement);

        if(el == bd){
            return [0, 0];
        }
/*
        if (el.getBoundingClientRect) {
            b = el.getBoundingClientRect();
            scroll = fly(document).getScroll();
            return [b.left + scroll.left, b.top + scroll.top];
        }
*/
        var x = 0, y = 0;

        p = el;
/*
        var hasAbsolute = fly(el).getStyle("position") == "absolute";
*/
        while (p) {

            x += p.offsetLeft;
            y += p.offsetTop;
/*
            if (!hasAbsolute && fly(p).getStyle("position") == "absolute") {
                hasAbsolute = true;
            }

            if (Ext.isGecko) {
                pe = fly(p);

                var bt = parseInt(pe.getStyle("borderTopWidth"), 10) || 0;
                var bl = parseInt(pe.getStyle("borderLeftWidth"), 10) || 0;


                x += bl;
                y += bt;


                if (p != el && pe.getStyle('overflow') != 'visible') {
                    x += bl;
                    y += bt;
                }
            }
*/
            p = p.offsetParent;
        }
/*
        if (Ext.isSafari && hasAbsolute) {
            x -= bd.offsetLeft;
            y -= bd.offsetTop;
        }

        if (Ext.isGecko && !hasAbsolute) {
            var dbd = fly(bd);
            x += parseInt(dbd.getStyle("borderLeftWidth"), 10) || 0;
            y += parseInt(dbd.getStyle("borderTopWidth"), 10) || 0;
        }

        p = el.parentNode;
        while (p && p != bd) {
            if (!Ext.isOpera || (p.tagName != 'TR' && fly(p).getStyle("display") != "inline")) {
                x -= p.scrollLeft;
                y -= p.scrollTop;
            }
            p = p.parentNode;
        }
*/
        return [x, y];
    };

	var eventToModule = {
		"DOMFocusIn":"UIEvent", "DOMFocusOut":"UIEvent", "DOMActivate":"UIEvent",

		"mousedown":"MouseEvent","mouseup":"MouseEvent","mouseover":"MouseEvent",
		"mousemove":"MouseEvent","mouseout":"MouseEvent", "click":"MouseEvent",

		"DOMSubtreeModified":"MutationEvent","DOMNodeInserted":"MutationEvent",
		"DOMNodeRemoved":"MutationEvent","DOMNodeRemovedFromDocument":"MutationEvent",
		"DOMNodeInsertedIntoDocument":"MutationEvent","DOMAttrModified":"MutationEvent",
		"DOMCharacterDataModified":"MutationEvent",

		"load":"Event", "unload":"Event", "resize":"Event", "scroll":"Event",
		"abort":"Event", "error":"Event", "select":"Event",
		"change":"Event", "submit":"Event",
		"reset":"Event", "focus":"Event", "blur":"Event",


		"":"Events"
	};
	/**
	 */
	function sendEvent(doc, target, ename, props) {
		if (doc.createEvent) {
			var e = doc.createEvent(eventToModule[ename] || "Events");
			e.initEvent(ename,true,false);
		}
		else if (doc.createEventObject) {
			var e = doc.createEventObject();
		}
		else throw new ExampleException("No support for synthetic events");

		for(var n in props) e[n] = props[n];;

		if (target.dispatchEvent) target.dispatchEvent(e); //DOM
		else if (target.fireEvent) target.fireEvent("on"+ename,e); //IE
	}

	// adopted from the script.actulo.us/unittest.js
	function simulateMouse(doc,element,eventName, props) {
		var options = {
			pointerX: 0, pointerY: 0,
			button:0, // 0=left 2=right
			ctrlKey: false, altKey: false, shiftKey: false, metaKey:false
		};
		if (props)
			for(var n in props) options[n] = props[n];;

		if (doc.createEventObject) { // IE
			options.clientX = options.pointerX;
			options.clientY = options.pointerY;
			var e = doc.createEventObject();
			for(var n in props) e[n] = options[n];
			element.fireEvent("on"+eventName,e)
		} else
		if (doc.createEvent) {
			var e = doc.createEvent("MouseEvents");
			if (e.initMouseEvent) { // DOM
				e.initMouseEvent(eventName,true,true,doc.defaultView,
					options.buttons, options.pointerX, options.pointerY, options.pointerX, options.pointerY,
					options.ctrlKey, options.altKey, options.shiftKey, options.metaKey, options.button, element);
			} else { // Safari
				//TODO add extra props
				for(var n in props) e[n] = options[n];
				e.initEvent(eventName, false, true);
			}
			element.dispatchEvent(e);
		}
		else throw new ExampleException("No support for synthetic events");
	}

	function simulateKey(doc, element, eventName, props) {
		var options = {
			keyCode:0, charCode:0,
			ctrlKey: false, altKey: false, shiftKey: false, metaKey:false
		};
		if (props)
			for(var n in props) options[n] = props[n];;

		if (doc.createEventObject) {
			var e = doc.createEventObject();
			//e.type = eventName;
			//for(var n in options) e[n] = options[n];
			e.ctrlKey = options.ctrlKey;
			e.altKey = options.altKey;
			e.shiftKey = options.shiftKey;
			e.metaKey = options.metaKey;
			e.keyCode = options.keyCode;
			element.fireEvent("on"+eventName,e);
		} else
		if (doc.createEvent && element.dispatchEvent) {
			if (window.KeyEvent) {
				var e = doc.createEvent("KeyEvents");
				e.initKeyEvent(eventName,true,true,doc.defaultView,
					options.ctrlKey, options.altKey, options.shiftKey, options.metaKey, options.keyCode, options.charCode );
			} else {
				var e = doc.createEvent('UIEvents');
				e.ctrlKey = options.ctrlKey;
				e.altKey = options.altKey;
				e.shiftKey = options.shiftKey;
				e.metaKey = options.metaKey;
				e.initUIEvent(eventName, true, true, doc.defaultView, 1);
				e.keyCode = options.keyCode;
				e.which = options.keyCode;
			}
			element.dispatchEvent(e);
		}
		else throw new ExampleException("No support for synthetic events");
	}

	ElementSubject.prototype.click = function() {
		simulateMouse(this.doc,this.target,"mouseover",{relatedTarget:this.doc.body});
		simulateMouse(this.doc,this.target,"mousedown",{});
		sendEvent(this.doc,this.target,"focus",{});
		simulateMouse(this.doc,this.target,"mouseup",{});
		simulateMouse(this.doc,this.target,"click",{});
		simulateMouse(this.doc,this.target,"mouseout",{relatedTarget:this.doc.body});
		return this;
	};

	ElementSubject.prototype.dblclick = function() {
		simulateMouse(this.doc,this.target,"mouseover",{relatedTarget:this.doc.body});
		simulateMouse(this.doc,this.target,"mousedown",{});
		sendEvent(this.doc,this.target,"focus",{});
		simulateMouse(this.doc,this.target,"mouseup",{});
		simulateMouse(this.doc,this.target,"click",{});
		simulateMouse(this.doc,this.target,"mousedown",{});
		simulateMouse(this.doc,this.target,"mouseup",{});
		simulateMouse(this.doc,this.target,"dblclick",{});
		simulateMouse(this.doc,this.target,"mouseout",{relatedTarget:this.doc.body});
		return this;
	};

	/**
	 * Simulate user input to a text input or textarea.
	 * @param text Input
	 * @param blank Blank the input before inputting?
	 */
	ElementSubject.prototype.input_text = function(text,blank) {
		if (this.inputType != "text" && this.inputType != "textarea") return; // not supported, ignore

		sendEvent(this.doc,this.target,"focus",{});
		if (Browser.Trident) {//this.target.value = this.target.value + text;
			var tr = this.target.createTextRange();
			if (blank) {
				tr.select();
				tr.text = "";
			}
			tr.collapse(false); // end of current text
			tr.select();
			tr.text = text;
		}
		else {
			var r = this.doc.createRange();
			if (blank) {
				if (this.inputType == "text") this.target.value = "";
				else if (this.inputType == "textarea") this.target.innerHTML = "";
				r.selectNodeContents(this.target);
			} else {
				r.selectNodeContents(this.target);
				r.collapse(false); // end of current text
			}
		}
		for(var i=0;i<text.length;++i) {
			simulateKey(this.doc,this.target,"keydown",{charCode:text.charCodeAt(i),keyCode:text.charCodeAt(i)});
			simulateKey(this.doc,this.target,"keyup",{charCode:text.charCodeAt(i),keyCode:text.charCodeAt(i)});
			simulateKey(this.doc,this.target,"keypress",{charCode:text.charCodeAt(i),keyCode:text.charCodeAt(i)});
		}
		//sendEvent(this.doc,this.target,"blur",{});
		sendEvent(this.doc,this.target,"change",{});

		return this;
	};

	var stateNames = {
		'check':'checked',
		'checked':'checked',
		'readOnly':'readonly',
		'readonly':'readonly',
		'disabled':'disabled',
		'disable':'disabled'
	};

	ElementSubject.prototype.input_state = function(state) {
		if (!this.inputType) return; // only for input fields

		for(var n in state) {
			var nm = stateNames[n];
			if (nm) {
				this.target[n] = state[n];
				if (n == "checked" && Browser.Trident) this.target.defaultChecked = state[n];
			}
		}
		// this.target.blur();
		sendEvent(this.doc,this.target,"change",{});
	};

	ElementSubject.prototype.keystrokes = function(keys) {
		//TODO support toggle checkbox/radio and textarea
		sendEvent(this.doc,this.target,"focus",{});
		if (this.inputType == "text") {
			if (Browser.Trident) {//this.target.value = this.target.value + keys;
				var tr = this.target.createTextRange();
				tr.collapse(false); // end of current text
				tr.select();
				tr.text = keys;
			}
		}
		for(var i=0;i<keys.length;++i) {
			simulateKey(this.doc,this.target,"keydown",{charCode:keys.charCodeAt(i),keyCode:keys.charCodeAt(i)});
			simulateKey(this.doc,this.target,"keyup",{charCode:keys.charCodeAt(i),keyCode:keys.charCodeAt(i)});
			simulateKey(this.doc,this.target,"keypress",{charCode:keys.charCodeAt(i),keyCode:keys.charCodeAt(i)});
		}
		//sendEvent(this.target,"blur",{});
		sendEvent(this.doc,this.target,"change",{});

		return this;
	};

	//TODO FocusManager tracking current focus and moving it around

	//TODO .input_select input_check input_unckeck, input_toggle
	//TODO .keystrokes insert before/after match text

	ElementSubject.prototype.submit = function() {
		sendEvent(this.doc,this.target,"submit",{});
	};

	//TODO select(optionValue,optionCaption)

	ElementSubject.prototype.getValue = function() {
		if (!this.target || this.target.nodeType != 1 || !this.target.ownerDocument) return null;
		if (this.inputType == "checkbox") return this.target.checked || this.target.defaultChecked;
		if (this.inputType == "text" || this.inputType == "textarea") return this.target.value;

		return this.target.innerHTML;
	};

	/**
	 * Window specific implemtation that queues specs on the window in which it was defined.
	 *
	 * @param {Object} context
	 * @param {Object} entries
	 */
	function describe(context, entries) {
		specPage.specs.push(new JSSpec.Spec(context, entries,specWindow.location.pathname));
	}
	specWindow.describe = describe;
	specWindow.behaviour_of = describe;
	JSSpec.DSL.describe = describe;

	// The following will be called after runnerWindow.onload so the DSL namespace will be available
	/**
	 * Window specific implementation that creates a subject
	 * @param {Object} target
	 */
	function value_of(target) {
		if(specPage.secondPass) return {};

		var subject = new Subject(target);
		return subject;
	}
	specWindow.value_of = value_of;
	specWindow.expect = value_of; // @deprecated
	JSSpec.DSL.value_of = value_of;

	/**
	 * Make a subject out of a given element. The subject has extra
	 * valuators that recognise DOM Elements and value attributes.<b>
	 * @param {String|Element} expression element, #id, css selector, or xpath
	 * @param {Document} doc Defaults to the document of the spec
	 */
	function value_of_element(expression,qualifiers,doc) {
		if(specPage.secondPass) return {};
		if (qualifiers && qualifiers.nodeType && qualifiers.nodeType == 9) { doc = qualifiers; qualifiers = undefined; }
		qualifiers = qualifiers || {};
		if (typeof expression == "string") {
			var el;
			if (expression == "") {
			}
			else if (expression.substring(0,1) == "#" && expression.indexOf(" ") == -1) {
				el = (doc || value_of_element.defaultDocument).getElementById(expression.substring(1));

			} else {
				var matches = cssQuery(expression,doc || value_of_element.defaultDocument);
				if (qualifiers.match !== undefined) {
					if (qualifiers.match instanceof RegExp) {
						for(var i=matches.length-1;i>=0;--i) {
							var t = matches[i].innerText || matches[i].textContent;
							if (!qualifiers.match.test(t)) matches.splice(i,1);
						}
					}
					else {
						for(var i=matches.length-1;i>=0;--i) {
							var t = matches[i].innerText || matches[i].textContent;
							if (!qualifiers.match == t) matches.splice(i,1);
						}
					}
				}
				if (qualifiers.visible) {
					for(var i=matches.length-1;i>=0;--i) {
						var matched = matches[i];
						if (!isVisible(matched)) matches.splice(i,1);
					}
				}
				if (qualifiers.point !== undefined) {
					var pointX = qualifiers.point[0], pointY = qualifiers.point[1];
					for(var i=matches.length-1;i>=0;--i) {
						var matched = matches[i];
						var xy = getXY(matched);
						var w = matched.offsetWidth, h = matched.offsetHeight;
						if (pointX < xy[0] || pointX >= xy[0]+w || pointY < xy[1] || pointY >= xy[1]+h)
							matches.splice(i,1);
					}
				}
				var j = qualifiers.index || 0;
				if (matches.length <= j) throw new Error('"'+expression+'" could not be resolved to an element at index '+j+".");
				el = matches[ j ];
			}
			if (el == null) throw new Error('"'+expression+'" could not be resolved to an element');
			return new ElementSubject(el);
		} else {
			if (!expression.nodeType) throw new Error("Object passed to value_of_element is neither a string or an Element");
			return new ElementSubject(expression);
		}
	}
	value_of_element.defaultDocument = specWindow.document;
	specWindow.value_of_element = value_of_element;

	// -----------------------------------------------------------------------
	// main query function
	// -----------------------------------------------------------------------

	var $COMMA = /\s*,\s*/;
	var cssQuery = function($selector, $$from) {
	try {
		var $match = [];
		var $useCache = arguments.callee.caching && !$$from;
		var $base = ($$from) ? ($$from.constructor == Array) ? $$from : [$$from] : [document];
		// process comma separated selectors
		var $$selectors = parseSelector($selector).split($COMMA), i;
		for (i = 0; i < $$selectors.length; i++) {
			// convert the selector to a stream
			$selector = _toStream($$selectors[i]);
			// faster chop if it starts with id (MSIE only)
			if (isMSIE && $selector.slice(0, 3).join("") == " *#") {
				$selector = $selector.slice(2);
				$$from = _msie_selectById([], $base, $selector[1]);
			} else $$from = $base;
			// process the stream
			var j = 0, $token, $filter, $arguments, $cacheSelector = "";
			while (j < $selector.length) {
				$token = $selector[j++];
				$filter = $selector[j++];
				$cacheSelector += $token + $filter;
				// some pseudo-classes allow arguments to be passed
				//  e.g. nth-child(even)
				$arguments = "";
				if ($selector[j] == "(") {
					while ($selector[j++] != ")" && j < $selector.length) {
						$arguments += $selector[j];
					}
					$arguments = $arguments.slice(0, -1);
					$cacheSelector += "(" + $arguments + ")";
				}
				// process a token/filter pair use cached results if possible
				$$from = ($useCache && cache[$cacheSelector]) ?
					cache[$cacheSelector] : select($$from, $token, $filter, $arguments);
				if ($useCache) cache[$cacheSelector] = $$from;
			}
			$match = $match.concat($$from);
		}
		delete cssQuery.error;
		return $match;
	} catch ($error) {
		cssQuery.error = $error;
		return [];
	}};

	// -----------------------------------------------------------------------
	// public interface
	// -----------------------------------------------------------------------

	cssQuery.toString = function() {
		return "function cssQuery() {\n  [version " + version + "]\n}";
	};

	// caching
	var cache = {};
	cssQuery.caching = false;
	cssQuery.clearCache = function($selector) {
		if ($selector) {
			$selector = _toStream($selector).join("");
			delete cache[$selector];
		} else cache = {};
	};

	// allow extensions
	var modules = {};
	var loaded = false;
	cssQuery.addModule = function($name, $script) {
		if (loaded) eval("$script=" + String($script));
		modules[$name] = new $script();;
	};

	// hackery
	cssQuery.valueOf = function($code) {
		return $code ? eval($code) : this;
	};

	// -----------------------------------------------------------------------
	// declarations
	// -----------------------------------------------------------------------

	var selectors = {};
	var pseudoClasses = {};
	// a safari bug means that these have to be declared here
	var AttributeSelector = {match: /\[([\w-]+(\|[\w-]+)?)\s*(\W?=)?\s*([^\]]*)\]/};
	var attributeSelectors = [];

	// -----------------------------------------------------------------------
	// selectors
	// -----------------------------------------------------------------------

	// descendant selector
	selectors[" "] = function($results, $from, $tagName, $namespace) {
		// loop through current selection
		var $element, i, j;
		for (i = 0; i < $from.length; i++) {
			// get descendants
			var $subset = getElementsByTagName($from[i], $tagName, $namespace);
			// loop through descendants and add to results selection
			for (j = 0; ($element = $subset[j]); j++) {
				if (thisElement($element) && compareNamespace($element, $namespace))
					$results.push($element);
			}
		}
	};

	// ID selector
	selectors["#"] = function($results, $from, $id) {
		// loop through current selection and check ID
		var $element, j;
		for (j = 0; ($element = $from[j]); j++) if ($element.id == $id) $results.push($element);
	};

	// class selector
	selectors["."] = function($results, $from, $className) {
		// create a RegExp version of the class
		$className = new RegExp("(^|\\s)" + $className + "(\\s|$)");
		// loop through current selection and check class
		var $element, i;
		for (i = 0; ($element = $from[i]); i++)
			if ($className.test($element.className)) $results.push($element);
	};

	// pseudo-class selector
	selectors[":"] = function($results, $from, $pseudoClass, $arguments) {
		// retrieve the cssQuery pseudo-class function
		var $test = pseudoClasses[$pseudoClass], $element, i;
		// loop through current selection and apply pseudo-class filter
		if ($test) for (i = 0; ($element = $from[i]); i++)
			// if the cssQuery pseudo-class function returns "true" add the element
			if ($test($element, $arguments)) $results.push($element);
	};

	// -----------------------------------------------------------------------
	// pseudo-classes
	// -----------------------------------------------------------------------

	pseudoClasses["link"] = function($element) {
		var $document = getDocument($element);
		if ($document.links) for (var i = 0; i < $document.links.length; i++) {
			if ($document.links[i] == $element) return true;
		}
	};

	pseudoClasses["visited"] = function($element) {
		// can't do this without jiggery-pokery
	};

	// -----------------------------------------------------------------------
	// DOM traversal
	// -----------------------------------------------------------------------

	// IE5/6 includes comments (LOL) in it's elements collections.
	// so we have to check for this. the test is tagName != "!". LOL (again).
	var thisElement = function($element) {
		return ($element && $element.nodeType == 1 && $element.tagName != "!") ? $element : null;
	};

	// return the previous element to the supplied element
	//  previousSibling is not good enough as it might return a text or comment node
	var previousElementSibling = function($element) {
		while ($element && ($element = $element.previousSibling) && !thisElement($element)) continue;
		return $element;
	};

	// return the next element to the supplied element
	var nextElementSibling = function($element) {
		while ($element && ($element = $element.nextSibling) && !thisElement($element)) continue;
		return $element;
	};

	// return the first child ELEMENT of an element
	//  NOT the first child node (though they may be the same thing)
	var firstElementChild = function($element) {
		return thisElement($element.firstChild) || nextElementSibling($element.firstChild);
	};

	var lastElementChild = function($element) {
		return thisElement($element.lastChild) || previousElementSibling($element.lastChild);
	};

	// return child elements of an element (not child nodes)
	var childElements = function($element) {
		var $childElements = [];
		$element = firstElementChild($element);
		while ($element) {
			$childElements.push($element);
			$element = nextElementSibling($element);
		}
		return $childElements;
	};

	// -----------------------------------------------------------------------
	// browser compatibility
	// -----------------------------------------------------------------------

	// all of the functions in this section can be overwritten. the default
	//  configuration is for IE. The functions below reflect this. standard
	//  methods are included in a separate module. It would probably be better
	//  the other way round of course but this makes it easier to keep IE7 trim.

	var isMSIE = true;

	var isXML = function($element) {
		var $document = getDocument($element);
		return (typeof $document.mimeType == "unknown") ?
			/\.xml$/i.test($document.URL) :
			Boolean($document.mimeType == "XML Document");
	};

	// return the element's containing document
	var getDocument = function($element) {
		return $element.ownerDocument || $element.document;
	};

	var getElementsByTagName = function($element, $tagName) {
		return ($tagName == "*" && $element.all) ? $element.all : $element.getElementsByTagName($tagName);
	};

	var compareTagName = function($element, $tagName, $namespace) {
		if ($tagName == "*") return thisElement($element);
		if (!compareNamespace($element, $namespace)) return false;
		if (!isXML($element)) $tagName = $tagName.toUpperCase();
		return $element.tagName == $tagName;
	};

	var compareNamespace = function($element, $namespace) {
		return !$namespace || ($namespace == "*") || ($element.scopeName == $namespace);
	};

	var getTextContent = function($element) {
		return $element.innerText;
	};

	function _msie_selectById($results, $from, id) {
		var $match, i, j;
		for (i = 0; i < $from.length; i++) {
			if ($match = $from[i].all.item(id)) {
				if ($match.id == id) $results.push($match);
				else if ($match.length != null) {
					for (j = 0; j < $match.length; j++) {
						if ($match[j].id == id) $results.push($match[j]);
					}
				}
			}
		}
		return $results;
	};

	// for IE5.0
	if (![].push) Array.prototype.push = function() {
		for (var i = 0; i < arguments.length; i++) {
			this[this.length] = arguments[i];
		}
		return this.length;
	};

	// -----------------------------------------------------------------------
	// query support
	// -----------------------------------------------------------------------

	// select a set of matching elements.
	// "from" is an array of elements.
	// "token" is a character representing the type of filter
	//  e.g. ">" means child selector
	// "filter" represents the tag name, id or class name that is being selected
	// the function returns an array of matching elements
	var $NAMESPACE = /\|/;
	function select($$from, $token, $filter, $arguments) {
		if ($NAMESPACE.test($filter)) {
			$filter = $filter.split($NAMESPACE);
			$arguments = $filter[0];
			$filter = $filter[1];
		}
		var $results = [];
		if (selectors[$token]) {
			selectors[$token]($results, $$from, $filter, $arguments);
		}
		return $results;
	};

	// -----------------------------------------------------------------------
	// parsing
	// -----------------------------------------------------------------------

	// convert css selectors to a stream of tokens and filters
	//  it's not a real stream. it's just an array of strings.
	var $STANDARD_SELECT = /^[^\s>+~]/;
	var $$STREAM = /[\s#.:>+~()@]|[^\s#.:>+~()@]+/g;
	function _toStream($selector) {
		if ($STANDARD_SELECT.test($selector)) $selector = " " + $selector;
		return $selector.match($$STREAM) || [];
	};

	var $WHITESPACE = /\s*([\s>+~(),]|^|$)\s*/g;
	var $IMPLIED_ALL = /([\s>+~,]|[^(]\+|^)([#.:@])/g;
	var parseSelector = function($selector) {
		return $selector
		// trim whitespace
		.replace($WHITESPACE, "$1")
		// e.g. ".class1" --> "*.class1"
		.replace($IMPLIED_ALL, "$1*$2");
	};

	var Quote = {
		toString: function() {return "'"},
		match: /^('[^']*')|("[^"]*")$/,
		test: function($string) {
			return this.match.test($string);
		},
		add: function($string) {
			return this.test($string) ? $string : this + $string + this;
		},
		remove: function($string) {
			return this.test($string) ? $string.slice(1, -1) : $string;
		}
	};

	var getText = function($text) {
		return Quote.remove($text);
	};

	var $ESCAPE = /([\/()[\]?{}|*+-])/g;
	function regEscape($string) {
		return $string.replace($ESCAPE, "\\$1");
	};

	cssQuery.addModule("css-standard", function() { // override IE optimisation

	// cssQuery was originally written as the CSS engine for IE7. It is
	//  optimised (in terms of size not speed) for IE so this module is
	//  provided separately to provide cross-browser support.

	// -----------------------------------------------------------------------
	// browser compatibility
	// -----------------------------------------------------------------------

	// sniff for Win32 Explorer
	isMSIE = Browser.TridentWin32;

	if (!isMSIE) {
		getElementsByTagName = function($element, $tagName, $namespace) {
			return $namespace ? $element.getElementsByTagNameNS("*", $tagName) :
				$element.getElementsByTagName($tagName);
		};

		compareNamespace = function($element, $namespace) {
			return !$namespace || ($namespace == "*") || ($element.prefix == $namespace);
		};

		isXML = document.contentType ? function($element) {
			return /xml/i.test(getDocument($element).contentType);
		} : function($element) {
			return getDocument($element).documentElement.tagName != "HTML";
		};

		getTextContent = function($element) {
			// mozilla || opera || other
			return $element.textContent || $element.innerText || _getTextContent($element);
		};

		function _getTextContent($element) {
			var $textContent = "", $node, i;
			for (i = 0; ($node = $element.childNodes[i]); i++) {
				switch ($node.nodeType) {
					case 11: // document fragment
					case 1: $textContent += _getTextContent($node); break;
					case 3: $textContent += $node.nodeValue; break;
				}
			}
			return $textContent;
		};
	}
	}); // addModule

	if (specWindow.addEventListener) specWindow.addEventListener("load",onWindowLoad,false);
	else if (specWindow.attachEvent) specWindow.attachEvent("onload",onWindowLoad);
	else specWindow.onload = onWindowLoad;
})(window);



