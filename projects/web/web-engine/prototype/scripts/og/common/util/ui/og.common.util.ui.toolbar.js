/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.toolbar',
    dependencies: [],
    obj: function () {
        return function (obj) {
            var $new_obj = [], html, disabled_cl = 'OG-disabled',
                toolbar_tmpl = '\
                    <div class="OG-icon og-icon-tools-${id} og-js-${id} ${enabled}" data-tooltip="${tooltip}"\
                        data-tooltip-type="small">\
                      {{if label}} <span>${label}</span> {{/if}}\
                    </div>\
                    {{if divider}} <div class="og-divider"></div> {{/if}}';
            if (!obj) throw new Error('obj is a required input for toolbar');
            if (!obj.location) throw new Error('You need to supply a selector/location for a toolbar to be placed');
            obj.buttons.forEach(function (button) {
                if (og.app.READ_ONLY) button.enabled = disabled_cl;
                if (button.enabled === disabled_cl) button.level = 'off';
            });
            // must convert rendered template into a string
            html = $('<p/>').append($.tmpl(toolbar_tmpl, obj.buttons)).html();
            $(obj.location).html(html); // Add the buttons to the page
            og.common.util.ui.tooltip(obj.location);
            if (og.app.READ_ONLY) return; // if READ_ONLY, do not add handlers
            // Implement handlers
            $.each(($.extend(true, $new_obj, {'buttons': obj.buttons}, obj)).buttons, function (i, val) {
                $('.' + obj.location + ' .og-js-' + val.id).unbind('mousedown').bind('mousedown', val.handler);
            });
        };
    }
});