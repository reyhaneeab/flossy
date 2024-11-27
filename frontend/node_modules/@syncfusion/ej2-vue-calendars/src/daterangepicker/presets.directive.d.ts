import { DefineVueDirective } from '@syncfusion/ej2-vue-base';
import { PresetsModel } from '@syncfusion/ej2-calendars';
export declare let PresetsDirective: any;
export declare const PresetsPlugin: {
    name: string;
    install(Vue: any): void;
};
/**
 * 'e-presets' directive represent a presets of VueJS daterangepicker
 * It must be contained in a daterangepicker component(`ej-daterangepicker`).
 * ```html
 * <ejs-daterangepicker id='range'>
 *   <e-presets>
 *   <e-preset label='Last Week' v-bind:start='startValue' v-bind:end='endValue'></e-preset>
 *   <e-preset label='Last Month' v-bind:start='startValue' v-bind:end='endValue'></e-preset>
 *   </e-presets>
 * </ejs-daterangepicker>
 * ```
 */
export declare let PresetDirective: DefineVueDirective<PresetsModel>;
export declare const PresetPlugin: {
    name: string;
    install(Vue: any): void;
};
