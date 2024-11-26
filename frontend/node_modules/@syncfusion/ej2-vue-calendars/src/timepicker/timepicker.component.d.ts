import { ComponentBase, DefineVueComponent } from '@syncfusion/ej2-vue-base';
import { TimePicker, TimePickerModel } from '@syncfusion/ej2-calendars';
export declare const properties: string[];
export declare const modelProps: string[];
export declare const testProp: any;
export declare const props: any, watch: any, emitProbs: any;
/**
 * Represents the Essential JS 2 VueJS TimePicker Component.
 * ```html
 * <ejs-timepicker v-bind:value='value'></ejs-timepicker>
 * ```
 */
export declare let TimePickerComponent: DefineVueComponent<TimePickerModel>;
export declare type TimePickerComponent = typeof ComponentBase & {
    ej2Instances: TimePicker;
    isVue3: boolean;
    isLazyUpdate: Boolean;
    plugins: any[];
    propKeys: string[];
    models: string[];
    hasChildDirective: boolean;
    tagMapper: {
        [key: string]: Object;
    };
    tagNameMapper: Object;
    setProperties(prop: any, muteOnChange: boolean): void;
    trigger(eventName: string, eventProp: {
        [key: string]: Object;
    }, successHandler?: Function): void;
    destroy(): void;
    focusIn(): void;
    focusOut(): void;
    hide(): void;
    requiredModules(): Object[];
    show(event?: Object | Object | Object): void;
};
export declare const TimePickerPlugin: {
    name: string;
    install(Vue: any): void;
};
