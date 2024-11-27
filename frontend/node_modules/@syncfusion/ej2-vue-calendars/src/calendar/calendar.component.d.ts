import { ComponentBase, DefineVueComponent } from '@syncfusion/ej2-vue-base';
import { Calendar, CalendarModel } from '@syncfusion/ej2-calendars';
export declare const properties: string[];
export declare const modelProps: string[];
export declare const testProp: any;
export declare const props: any, watch: any, emitProbs: any;
/**
 * Represents the Essential JS 2  VueJS Calendar Component.
 * ```html
 * <ejs-calendar v-bind:value='date'></ejs-calendar>
 * ```
 */
export declare let CalendarComponent: DefineVueComponent<CalendarModel>;
export declare type CalendarComponent = typeof ComponentBase & {
    ej2Instances: Calendar;
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
    addDate(dates: Object | Object[]): void;
    createContent(): void;
    currentView(): string;
    destroy(): void;
    navigateTo(view: Object, date: Object, isCustomDate?: boolean): void;
    removeDate(dates: Object | Object[]): void;
    requiredModules(): Object[];
};
export declare const CalendarPlugin: {
    name: string;
    install(Vue: any): void;
};
