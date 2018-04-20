/*
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
$.register_module({
	name: 'og.common.util.ui.ExternalIdBundle',
	dependencies: ['og.common.util.ui.Form'],
	obj: function () {
		var module = this, prefix = "externalIdBundle", Block = og.common.util.ui.Block;
		var ExternalIdBundle = function (config) {
			var block = this,
				render,
				classes = config.classes || '',
				ids = {
					container: og.common.id(prefix),
					widget: og.common.id(prefix),
					row_existing_scheme: og.common.id(prefix),
					row_new_scheme: og.common.id(prefix)
				},
				data = config.data,
				data_index = config.index;
			var convert = function (input) {
				var length = 0, item, data = [], lcv;
				if (!input || typeof input === 'string') { return input || ''; }
				for (item in input) { if (+item + 0 === +item) { length += 1 }; }
				for (lcv = 0; lcv < length; lcv += 1) { data.push(item[lcv]); }
                return data.length > 1 ?
                        '[' + data.map(function (str) {return str.replace(/\,/g, '\\,');}).join(', ') + ']' : data.join('');
			};
			var deconvert = function (input, optional) {
                var empty = true, result, array = to_array(input);
                if (!optional && !array.length) return null;
                result = array
                    .reduce(function (acc, val, idx) {return (empty = 0), val ? (acc[idx] = val, acc) : acc;}, {});
                if (empty && !optional) return null;
                if (optional) result.optional = null;
                return result;
			};
			var to_array = function (input) {
                return input ?
                        /^\[(.*)\]$/.test(input) ? RegExp.$1.replace(/\\\,/g, '\0').split(/,\s*/)
                            .map(function (str) {return str.replace(/\0/g, ',');}) : [input]
                        : [];
			};
			config.form.Block.call(block, {
                extras: $.extend({classes: classes}, ids),
                processor: function (data) {
                    var indices = data_index.split('.'), last = indices.pop(), result = {},
                        $withs = $('#' + ids.widget + ' .og-js-existing-scheme');
                    if (!$('#' + ids.widget).length) return;
                    $withs.each(function (idx, el) {
                        var $el = $(el), optional = !!$el.find('input[type=checkbox]').filter(':checked').length,
                            key = $el.find('input.og-js-scheme').val();
                        if (!key) throw new Error(module.name + ': in a with constraint, type must be defined');
                        if (!result['with']) result['with'] = {};
                        result[existing_scheme][key] = deconvert($el.find('input.og-js-value').val(), optional);
                    });
                    indices.reduce(function (acc, level) {
                        return acc[level] && typeof acc[level] === 'object' ? acc[level] : (acc[level] = {});
                    }, data)[last] = result;
                }
			});
			block.on('form:load', function () {
                var item, $widget = $('#' + ids.widget), rows = {
                    existing_scheme : $('#' + ids.row_existing_scheme).remove().removeAttr('id'),
                };
                render = {
                    existing_scheme: function (datum, $replace, $after) {
                        var item, add = function (item) {
                            var $row = rows['with'].clone(), $inputs = $row.find('input'), value = convert(datum[item]);
                            $inputs[0].checked = 'checked', $inputs[2].value = item, $inputs[3].value = value;
                            if (datum[item] && typeof datum[item] === 'object' && ('optional' in datum[item]))
                                $inputs[4].checked = 'checked';
                            if (!$replace && !$after) return $widget.append($row);
                            if ($replace) return $replace.replaceWith($row);
                            if ($after) return $after.after($row);
                        };
                        for (item in datum) add(item);
                    },
                };
//                for (item in data) render[item](data[item]);
            })/*.on('change', '#' + ids.widget + ' input.og-js-radio', function (event) {
                var target = event.target, value = target.value;
                if (value === 'without' && $('#' + ids.widget + ' .og-js-without-field').length)
                    return alert('Sorry, but only one "without" constraint at a time.'), event.target.checked = '';
                render[value]({'with': {'': null}, without: ''}[value], $(target).closest('.og-js-row'));
            })*/.on('click', '#' + ids.widget + ' .og-js-rem', function (event) {
                $(event.target).closest('.og-js-row').remove();
            }).on('click', '#' + ids.container + ' .og-js-add', function (event) {render[existing_scheme]({'': null});});
		};
		ExternalIdBundle.prototype = new Block(null, { module: 'og.views.forms.external-id_tash'});
		return ExternalIdBundle;
	}
})