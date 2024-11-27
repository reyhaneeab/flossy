import { ComponentBase, DefineVueComponent } from '@syncfusion/ej2-vue-base';
import { DateRangePicker, DateRangePickerModel } from '@syncfusion/ej2-calendars';
export declare const properties: string[];
export declare const modelProps: string[];
export declare const testProp: any;
export declare const props: any, watch: any, emitProbs: any;
/**
 * Represents the Essential JS 2 VueJS DateRangePicker Component.
 * ```html
 * <ejs-daterangepicker v-bind:startDate='date' v-bind:endDate='date'></ejs-daterangepicker>
 * ```
 */
export declare let DateRangePickerComponent: DefineVueComponent<DateRangePickerModel>;
export declare type DateRangePickerComponent = typeof ComponentBase & {
    ej2Instances: DateRangePicker;
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
    getSelectedRange(): Object;
    hide(event?: Object | Object | Object): void;
    requiredModules(): Object[];
    show(element?: Object, event?: Object | Object | Object): void;
};
export declare const DateRangePickerPlugin: {
    name: string;
    install(Vue: any): void;
};
