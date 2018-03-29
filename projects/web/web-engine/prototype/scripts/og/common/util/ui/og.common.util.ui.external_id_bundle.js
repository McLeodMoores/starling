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
				data_index = config.index
			var convert = function (input) {
				var length = 0, item, data = [], lcv;
				if (!input || typeof input === 'string') { return input || ''; }
				for (item in input) { if (+item + 0 === +item) { length += 1 }; }
				for (lcv = 0; lcv < length; lcv += 1) { data.push(item[lcv]); }
				//TODO trim
				return data;
			};
			var deconvert = function (input) {
				var empty = true, result, array = to_array(input);
			};
			var to_array = function (input) {
				
			};
			config.form.Block.call(block, {
				extras: $.extend({classes: classes}, ids),
				processor: function (data) {
					var indices = data_index.split('.'),
					last = indices.pop(), 
					result = {},
					$existingSchemes = $('#' + ids)
				}
			});
			block.on('form:load', function () {
				var item, $widget = $('#' + ids.widget), 
				rows = {
					// don't have any empty schemes
					row_existing_scheme: $('#' + ids.row_existing_scheme).remove().removeAttr('id')
				};
				render = {
					row_existing_scheme: function (input, $replace, $after) {
						var item, 
						add = function (item) {
							console.log(item);
						}
						for (item in input) add(item);
					}
				}
//				for (item in data) { render[item](data[item]) };
                for (item in data) {
                	console.log(data[item]);
                	render[item](data[item]);
                }
			}).on('change', '#' + ids.widget + ' select.og-js-scheme', function (event) {
				
			}).on('click', '#' + ids.widget + ' og-js-rem', function (event) {
				$(event.target).closest('.og-js-row').remove();
			}).on('click', '#' + ids.container + ' .og.js.add', function (event) { 
				render[row_new_scheme]( {'': null} ); 
			});
		};
		ExternalIdBundle.prototype = new Block(null, { module: 'og.views.forms.external-id_tash'});
		return ExternalIdBundle;
	}
})