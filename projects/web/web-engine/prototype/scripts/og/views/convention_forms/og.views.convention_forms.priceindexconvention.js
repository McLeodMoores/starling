/*
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
$.register_module({
	name: 'og.views.convention_forms.priceindexconvention',
	dependencies: [
		'og.api.rest',
		'og.common.util.ui'
	],
	obj: function () {
		var ui = og.common.util.ui, 
			forms = og.views.forms, 
			api = og.api.rest, 
			Form = ui.Form,
			ATTR = 'attributes',
			EIDS = 'externalIdBundle',
			INDX = '<INDEX>',
			EMPT = '<EMPTY>',
			type_map = [
				[['0', INDX].join('.'),								Form.type.STR],
				['name', 											Form.type.STR],
				['currency',  										Form.type.STR],
				['region',		 									Form.type.STR],
				['uniqueId',										Form.type.STR],
				[[EIDS, 'ID', INDX, 'Scheme'].join('.'),	 		Form.type.STR],
				[[EIDS, 'ID', INDX, 'Value'].join('.'),				Form.type.STR],
				[['id', EMPT, 'scheme'].join('.'),					Form.type.STR],
				[['id', EMPT, 'value'].join('.'),					Form.type.STR],
				[[ATTR, EMPT].join('.'),							Form.type.STR], 
				[[ATTR, INDX, 'Key'].join('.'),						Form.type.STR], 
				[[ATTR, INDX, 'Value'].join('.'),					Form.type.STR], 
				].reduce(function (acc, val) { return acc[val[0]] = val[1], acc; }, {});
        var arr = function (obj) { return arr && $.isArray(obj) ? obj : typeof obj !== 'undefined' ? [obj] : [] };
        var constructor = function (config) {
        	var load_handler = config.handler || $.noop,
	        	selector = config.selector,
	        	loading = config.loading || $.noop,
	        	deleted = config.data.template_data.deleted,
	        	is_new = config.is_new,
	        	orig_name = config.data.template_data.name,
	        	resource_id = config.data.template_data.object_id,
	        	save_new_handler = config.save_new_handler, 
	        	save_handler = config.save_handler,
	        	master = config.data.template_data.configJSON.data,
	        	convention_type = config.type,
	        	sep = '~', 
	        	form = new Form({
	        		module: 'og.views.forms.price-index-convention_tash',
	        		data: master,
	        		type_map: type_map,
	        		selector: selector,
	        		extras: {
	        			name: master.name,
	        			currency: master.currency || (master.currency = 'USD')
	        		},
	        		processor: function (data) {
	        			data.id = data.id.filter(function (v) { return v !== void 0; });
            			data[EIDS] = arr(data[EIDS]).filter(function (v) { return v !== void 0; });
            			data[ATTR] = arr(data[ATTR]).filter(function (v) { return v !== void 0; });
	        		}
	        	}),
	        	form_id = '#' + form.id,
        		save_resource = function (result) {
        			var data = result.data,
        				meta = result.meta,
        				as_new = result.extras.as_new;
        			if (as_new && (orig_name == data.name)) { return window.alert('Please select a new name.') };
        			if (!data.externalIdBundle.ID.length) { return window.alert('Please add at least one external identifier') }; 
       			api.conventions.put({
        				id: as_new ? void 0 : resource_id,
        				name: data.name,
        				json: JSON.stringify({ data: data, meta: meta }),
        				type: convention_type,
        				loading: loading,
        				handler: as_new ? save_new_handler : save_handler
        			});
        		},
        		load_resource = function () {
            		var header = '\
            			<header class="OG-header-generic">\
            			<div class="OG-tools"></div>\
            			<h1>\
            			<span class="og-js-name">' + master.name + '</span>\
            			</h1>\
            			  &nbsp(Price Index Convention)\
            			</header>\
            			';
            		$('.OG-layout-admin-details-center .ui-layout-header').html(header);
            		$(form_id);
            		setTimeout(load_handler.partial(form));
        		},
            	holiday_handler = function (handler) {
                    api.regions.get({page: '*'}).pipe(function (result) {
                        handler(result.data.data.map(function (region) {
                            var split = region.split('|');
                            return !split[3] ? null : {value: split[3], text: split[3] + ' - ' + split[1]}
                        }).filter(Boolean).sort(function (a, b) { // alphabetize
                            return a.text < b.text ? -1 : a === b ? 0 : 1;
                        }));
                    });
                };
            form.on('form:submit', save_resource)
            	.on('form:load', load_resource);
            form.children = [
            	// item_0
            	new form.Block({ 
            		module: 'og.views.forms.currency_tash' 
            	}).on('form:load', function () {
            		$(form_id + ' select[name=currency]').val(master.currency);
            	}),
            	// item_1
            	new ui.Dropdown({
            		form: form,
            		placeholder: 'Please select...',
            		value: master.region ? master.region.split(sep)[1] : "",
       				//TODO
            		processor: function (selector, data, errors) {
            			data.region = master.region.split(sep)[0] + sep + $(selector).val();
            		},
            		data_generator: holiday_handler
            	}),
            	// item_2
            	new og.views.convention_forms.ExternalIdBundle({
            		form: form,
            		data: master.externalIdBundle,
            		index: 'externalIdBundle',
            		name: "Price Index Identifiers"
            	}),
            	// item_3
            	new og.views.convention_forms.Attributes({
            		form: form,
            		attributes: master.attributes,
            		index: 'attributes'
            	})
            ];
            form.dom();
        };
        constructor.type_map = type_map;
        return constructor;
	}
})