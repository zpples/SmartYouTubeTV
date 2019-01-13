/**
 * Common event routines
 */

console.log("Scripts::Running script event_utils.js");

var EventUtils = {
    TAG: 'EventUtils',
    checkIntervalMS: 3000,
    listeners: {},

    init: function() {
        // do init here
    },

    /**
     * Calls listener.onKeyEvent(event) every time key event arrives
     */
    addKeyPressListener: function(listener, selector) {
        // TODO: provide more optimized routine
        if (!this.isPlayerInitialized()) {
            // player not initialized yet
            var $this = this;
            setTimeout(function() {
                $this.addKeyPressListener(listener, selector);
            }, $this.checkIntervalMS);
            return;
        }

        if (this.listeners[listener.onKeyEvent]) {
            console.log("EventUtils::this listener already added... do nothing");
            return;
        }

        this.listeners[listener.onKeyEvent] = true;

        var container = Utils.$(selector);
        console.log('EventUtils::addListener:keyup... ');
        container.addEventListener(DefaultEvents.KEY_UP, function(event) {
            listener.onKeyEvent(event);
        });
    },

    /**
     * Calls listener.onKeyEvent(event) every time key event arrives
     */
    addGlobalKeyPressListener: function(listener) {
        this.addKeyPressListener(listener, YouTubeConstants.APP_CONTAINER_SELECTOR);
    },

    /**
     * Calls listener.onKeyEvent(event) every time key event arrives
     */
    addPlayerKeyPressListener: function(listener) {
        console.log("EventUtils::addPlayerKeyPressListener");

        this.addKeyPressListener(listener, YouTubeConstants.PLAYER_EVENTS_RECEIVER_SELECTOR);
    },

    /**
     * Calls listener.onPlaybackEvent() every time player start to play an video
     */
    addPlaybackListener: function(listener) {
        // do every time when video loads:
        window.addEventListener(DefaultEvents.HASH_CHANGE, function(){
            var isPlayerOpened = window.location.hash.indexOf(YouTubeConstants.PLAYER_URL_KEY) != -1;
            if (isPlayerOpened) {
                Utils.postSmallDelayed(listener.onPlaybackEvent, listener); // video initialized with small delay
            }
        }, false);
    },

    delayUntilPlayerBeInitialized: function(fn) {
        var testFn = function() {
            return Utils.$(YouTubeConstants.PLAYER_PLAY_BUTTON_SELECTOR);
        };
        Utils.delayTillTestFnSuccess(fn, testFn);
    },

    isPlayerInitialized: function() {
        var elem = Utils.$(YouTubeConstants.PLAYER_EVENTS_RECEIVER_SELECTOR);
        return Utils.hasClass(elem, YouTubeConstants.PLAYER_CONTAINER_CLASS);
    },

    toSelector: function(el) {
        if (!el) {
            return null;
        }

        if (Utils.isString(el)) {
            return el;
        }

        if (Utils.isArray(el)) {
            return el;
        }

        if (!el.tagName) {
            return null;
        }

        var idPart = el.id ? '#' + el.id : '';
        var cls = el.className ? el.className.trim() : '';
        var classPart = cls ? '.' + cls.split(/[ ]+/).join('.') : '';
        return idPart + classPart;
    },

    triggerEvent: function(elementOrSelector, type, keyCode) {
        if (Utils.isArray(elementOrSelector)) {
            console.log("EventUtils::triggerEvent: arrays not supported: " + elementOrSelector);
            return;
        }

        var el = elementOrSelector;
        if (Utils.isSelector(elementOrSelector)) {
            el = Utils.$(elementOrSelector);
        }

        var elSelector = this.toSelector(el) ? this.toSelector(el) : elementOrSelector;

        if (!el) {
            console.warn("EventUtils::triggerEvent: unable to find " + elSelector);
            return;
        }

        console.log("EventUtils::triggerEvent: " + el + ' ' + elSelector + ' ' + type + ' ' + keyCode);

        this._triggerEvent(el, type, keyCode);
    },

    _triggerEvent: function(el, type, keyCode) {
        if ('createEvent' in document) {
            // modern browsers: Chrome, IE9+
            // HTMLEvents, KeyboardEvent
            // https://developer.mozilla.org/en-US/docs/Web/API/Document/createEvent#Notes
            var e = document.createEvent('HTMLEvents');
            e.keyCode = keyCode;
            e.initEvent(type, true, true);
            el.dispatchEvent(e);
        } else {
            // IE 8
            var e = document.createEventObject();
            e.keyCode = keyCode;
            e.eventType = type;
            el.fireEvent('on' + e.eventType, e);
        }
    },

    triggerEnter: function(elementOrSelector) {
        // simulate mouse/enter key press
        this.triggerEvent(elementOrSelector, DefaultEvents.KEY_UP, DefaultKeys.ENTER);
    },

    /**
     * Adds lister or waits till element be initialized
     * @param selectorOrElement desired element as selector
     * @param event desired event
     * @param handler callback
     */
    addListener: function(selectorOrElement, event, handler) {
        ListenerUtil.addListener(selectorOrElement, event, handler);
    },

    removeListener: function(selectorOrElement, event, handler) {
        ListenerUtil.removeListener(selectorOrElement, event, handler);
    },

    onLoad: function(callback) {
        if (!Utils.$(YouTubeSelectors.MAIN_LOADER)) {
            Log.d(this.TAG, 'app has been loaded');
            callback && callback();
            return;
        }

        var $this = this;
        setTimeout(function() {
            $this.onLoad(callback);
        }, 500);
    }
};

EventUtils.init();