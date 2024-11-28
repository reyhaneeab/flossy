import { ComponentBase, gh, getProps, isExecute, vueDefineComponent } from '@syncfusion/ej2-vue-base';
import { isNullOrUndefined } from '@syncfusion/ej2-base';
import { isUndefined } from '@syncfusion/ej2-base';
import { Calendar } from '@syncfusion/ej2-calendars';
export var properties = ['isLazyUpdate', 'plugins', 'calendarMode', 'cssClass', 'dayHeaderFormat', 'depth', 'enablePersistence', 'enableRtl', 'enabled', 'firstDayOfWeek', 'isMultiSelection', 'keyConfigs', 'locale', 'max', 'min', 'serverTimezoneOffset', 'showTodayButton', 'start', 'value', 'values', 'weekNumber', 'weekRule', 'change', 'created', 'destroyed', 'navigated', 'renderDayCell'];
export var modelProps = ['value', 'values'];
export var testProp = getProps({ props: properties });
export var props = testProp[0], watch = testProp[1], emitProbs = Object.keys(watch);
emitProbs.push('modelchanged', 'update:modelValue');
for (var _i = 0, modelProps_1 = modelProps; _i < modelProps_1.length; _i++) {
    var props_1 = modelProps_1[_i];
    emitProbs.push('update:' + props_1);
}
/**
 * Represents the Essential JS 2  VueJS Calendar Component.
 * ```html
 * <ejs-calendar v-bind:value='date'></ejs-calendar>
 * ```
 */
export var CalendarComponent = vueDefineComponent({
    name: 'CalendarComponent',
    mixins: [ComponentBase],
    props: props,
    watch: watch,
    emits: emitProbs,
    model: { event: 'modelchanged' },
    provide: function () { return { custom: this.custom }; },
    data: function () {
        return {
            ej2Instances: new Calendar({}),
            propKeys: properties,
            models: modelProps,
            hasChildDirective: false,
            hasInjectedModules: true,
            tagMapper: {},
            tagNameMapper: {},
            isVue3: !isExecute,
            templateCollection: {},
        };
    },
    created: function () {
        this.ej2Instances._trigger = this.ej2Instances.trigger;
        this.ej2Instances.trigger = this.trigger;
        this.bindProperties();
        this.ej2Instances._setProperties = this.ej2Instances.setProperties;
        this.ej2Instances.setProperties = this.setProperties;
        this.ej2Instances.clearTemplate = this.clearTemplate;
        this.updated = this.updated;
    },
    render: function (createElement) {
        var h = !isExecute ? gh : createElement;
        var slots = null;
        if (!isNullOrUndefined(this.$slots.default)) {
            slots = !isExecute ? this.$slots.default() : this.$slots.default;
        }
        return h('div', slots);
    },
    methods: {
        clearTemplate: function (templateNames) {
            if (!templateNames) {
                templateNames = Object.keys(this.templateCollection || {});
            }
            if (templateNames.length && this.templateCollection) {
                for (var _i = 0, templateNames_1 = templateNames; _i < templateNames_1.length; _i++) {
                    var tempName = templateNames_1[_i];
                    var elementCollection = this.templateCollection[tempName];
                    if (elementCollection && elementCollection.length) {
                        for (var _a = 0, elementCollection_1 = elementCollection; _a < elementCollection_1.length; _a++) {
                            var ele = elementCollection_1[_a];
                            this.destroyPortals(ele);
                        }
                        delete this.templateCollection[tempName];
                    }
                }
            }
        },
        setProperties: function (prop, muteOnChange) {
            var _this = this;
            if (this.isVue3) {
                this.models = !this.models ? this.ej2Instances.referModels : this.models;
            }
            if (this.ej2Instances && this.ej2Instances._setProperties) {
                this.ej2Instances._setProperties(prop, muteOnChange);
            }
            if (prop && this.models && this.models.length) {
                Object.keys(prop).map(function (key) {
                    _this.models.map(function (model) {
                        if ((key === model) && !(/datasource/i.test(key))) {
                            if (_this.isVue3) {
                                _this.ej2Instances.vueInstance.$emit('update:' + key, prop[key]);
                            }
                            else {
                                _this.$emit('update:' + key, prop[key]);
                                _this.$emit('modelchanged', prop[key]);
                            }
                        }
                    });
                });
            }
        },
        trigger: function (eventName, eventProp, successHandler) {
            if (!isExecute) {
                this.models = !this.models ? this.ej2Instances.referModels : this.models;
            }
            if ((eventName === 'change' || eventName === 'input') && this.models && (this.models.length !== 0)) {
                var key = this.models.toString().match(/checked|value/) || [];
                var propKey = key[0];
                if (eventProp && key && !isUndefined(eventProp[propKey])) {
                    if (!isExecute) {
                        this.ej2Instances.vueInstance.$emit('update:' + propKey, eventProp[propKey]);
                        this.ej2Instances.vueInstance.$emit('modelchanged', eventProp[propKey]);
                        this.ej2Instances.vueInstance.$emit('update:modelValue', eventProp[propKey]);
                    }
                    else {
                        if (eventName === 'change' || (this.$props && !this.$props.isLazyUpdate)) {
                            this.$emit('update:' + propKey, eventProp[propKey]);
                            this.$emit('modelchanged', eventProp[propKey]);
                        }
                    }
                }
            }
            else if ((eventName === 'actionBegin' && eventProp.requestType === 'dateNavigate') && this.models && (this.models.length !== 0)) {
                var key = this.models.toString().match(/currentView|selectedDate/) || [];
                var propKey = key[0];
                if (eventProp && key && !isUndefined(eventProp[propKey])) {
                    if (!isExecute) {
                        this.ej2Instances.vueInstance.$emit('update:' + propKey, eventProp[propKey]);
                        this.ej2Instances.vueInstance.$emit('modelchanged', eventProp[propKey]);
                    }
                    else {
                        this.$emit('update:' + propKey, eventProp[propKey]);
                        this.$emit('modelchanged', eventProp[propKey]);
                    }
                }
            }
            if ((this.ej2Instances && this.ej2Instances._trigger)) {
                this.ej2Instances._trigger(eventName, eventProp, successHandler);
            }
        },
        custom: function () {
            this.updated();
        },
        addDate: function (dates) {
            return this.ej2Instances.addDate(dates);
        },
        createContent: function () {
            return this.ej2Instances.createContent();
        },
        currentView: function () {
            return this.ej2Instances.currentView();
        },
        destroy: function () {
            return this.ej2Instances.destroy();
        },
        navigateTo: function (view, date, isCustomDate) {
            return this.ej2Instances.navigateTo(view, date, isCustomDate);
        },
        removeDate: function (dates) {
            return this.ej2Instances.removeDate(dates);
        },
        requiredModules: function () {
            return this.ej2Instances.requiredModules();
        },
    }
});
export var CalendarPlugin = {
    name: 'ejs-calendar',
    install: function (Vue) {
        Vue.component(CalendarPlugin.name, CalendarComponent);
    }
};
