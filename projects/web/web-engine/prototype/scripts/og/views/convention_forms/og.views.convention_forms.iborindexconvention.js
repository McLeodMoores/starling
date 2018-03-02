/*
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
$.register_module({
	name: 'og.views.convention_forms.iborindexconvention',
	dependencies: [
		'og.api.rest',
		'og.common.util.ui'
	],
	obj: function () {
		var ui = og.common.util.ui, forms = og.views.forms, api = og.api.rest, Form = ui.Form,
		indx = '<INDEX>',
		type_map = [
			[['0', indx].join('.'),									Form.type.STR],
			['name', 												Form.type.STR],
			['currency', 											Form.type.STR],
			['businessDayConvention',             		            Form.type.STR],
			['dayCount',      										Form.type.STR],
			['fixingCalendar',										Form.type.STR],
			['fixingPage',											Form.type.STR],
			['fixingTime',											Form.type.STR],
			['fixingTimeZone',										Form.type.STR],
			['isEOM',												Form.type.BOO],
			['regionCalendar',										Form.type.STR],
			['settlementDays',										Form.type.BYT],
			[['externalIdBundle', indx, '.ID', indx, 'Scheme'].join('.'), 	Form.type.STR],
//			[['externalIdBundle', indx, '.ID', indx, 'new_scheme'].join('.'), Form.type.STR],
			[['externalIdBundle', indx, '.ID', indx, 'Value'].join('.'),		Form.type.STR],
			['uniqueId',											Form.type.STR]
		].reduce(function (acc, val) { return acc[val[0]] = val[1], acc; }, {});
        var arr = function (obj) { return arr && $.isArray(obj) ? obj : typeof obj !== 'undefined' ? [obj] : [] };
		var constructor = function (config) {
            var load_handler = config.handler || $.noop, selector = config.selector,
            	loading = config.loading || $.noop, deleted = config.data.template_data.deleted, is_new = config.is_new,
            	orig_name = config.data.template_data.name,
            	resource_id = config.data.template_data.object_id,
            	save_new_handler = config.save_new_handler, save_handler = config.save_handler,
            	master = config.data.template_data.configJSON.data, externalIdBundle, attributes, sep = '~', convention_type = config.type,
            	form = new Form({
            		module: 'og.views.forms.ibor-index-convention_tash',
            		data: master, 
            		type_map:  type_map,
            		selector: selector,
            		extras: {
            			name: master.name,
            			currency: master.currency || (master.currency = 'USD')
            		},
            		processor: function (data) {
            			data.id = data.id.filter(function (v) { return v !== void 0;});
            			
            		}
            	}),
            	form_id = '#' + form.id,
            	new_identifier = function (row, idx) {
            		return new form.Block({
            			module: 'og.views.forms.external-identifiers_tash',
            			extras: { idx: idx }, //TODO possible schemes?
            		}).on('form:load', function () {
            			$(form_id + ' [name="' + ['id', idx, 'scheme'].join('.') + '"]').val(row.scheme);
            			$(form_id + ' [name="' + ['id', idx, 'value'].join('.') + '"]').val(row.value);
            			if (row.scheme !== 'Other') { $(form_id + ' [name="' + ['id', idx, 'new_scheme'].join('.') + '"]').attr('disabled', 'disabled') }
            		}).on('change', form_id + ' [name="' + ['id', idx, 'scheme'].join('.') + '"]', 
            				function (event) {
            					var $el = $(form_id + ' [name="' + ['id', idx, 'new_scheme'].join('.') + '"]'),
            					is_new_scheme = $(event.target).val() === 'Other';
            					if (is_new_scheme) { 
            						$el.removeAttr('disabled');
            					} else {
            						$el.attr('disabled', 'disabled');
            						$el.attr('value', '');
            					}
                		}
            		)
            	},
            	save_resource = function (result) {
            		var data = result.data, 
            		meta = result.meta,
            		as_new = result.extras.as_new;
            		if (as_new && (orig.name === data.name)) { return window.alert('Please select a new name.') };
            		api.conventions.put({
            			id: as_new ? void 0 : resource_id,
            			name: data.name,
            			json: JSON.stringify({ data: data, meta: meta }),
            			type: config_type,
            			loading: loading,
            			handler: as_new ? save_new_handler : save_handler
            		});
            	};
            	load_resource = function () {
            		var header = '\
            			<header class="OG-header-generic">\
            			<div class="OG-tools"></div>\
            			<h1>\
            			<span class="og-js-name">' + master.name + '</span>\
            			</h1>\
            			(*IBOR Index Convention)\
            			</header>\
            			';
            		$('.OG-layout-admin-details-center .ui-layout-header').html(header);
            		$(form_id);
            		$(form_id + ' select[name=currency]').val(master.currency);
            		$(form_id + ' select[name=dayCount]').val(master.dayCount);
            		$(form_id + ' select[name=businessDayConvention]').val(master.businessDayConvention);
            		//TODO shouldn't need these
            		$(form_id + ' input[name=fixingTime').val(master.fixingTime);
            		$(form_id + ' input[name=fixingTimeZone').val(master.fixingTimeZone);
            		$(form_id + ' input[name=fixingPage').val(master.fixingPage);
            		$(form_id + ' input[name=isEOM').val(master.isEOM ? 'checked' : '');
            		$(form_id + ' input[name=settlementDays').val(master.settlementDays.toString());
            		
            		setTimeout(load_handler.partial(form));
            	};
            	add_id_resource = function (event) {
            		var block = new_identifier({}, master.externalIdBundle.push({}) - 1);
            		block.html(function (html) { $(form_id + ' .og-js-external-ids').append($(html)), block.load(); });
            	};
            	remove_id_resource = function (event) {
            		var $el = $(event.target).parents('.og-js-external-ids:first');
            		master.id[$el.find('input').attr('name')] = void 0;
            		$el.remove();
            	};
            	holiday_handler = function (handler) {
                	api.holidays.get({ page: '*' }).pipe(function (result) {
                		handler(result.data.data.map(function (holiday) {
                			var split = holiday.split('|');
                			//TODO make currency type more readable
                			return !split[1] ? null : { value: split[1], text: split[1] + ' - ' + split[2] + ' Calendar'}                    			
                		}).filter(Boolean).sort(function (a, b) {
                			return a.text < b.text ? -1 : a === b ? 0 : 1;
                		}));
                	});
                };
            form.on('form:submit', save_resource)
            	.on('form:load', load_resource)
            	.on('click', form_id + ' .og-js-add', add_id_resource)
            	.on('click', form_id + ' .og-js-rem', remove_id_resource);
            form.children = [
            	// item_0
            	new form.Block({ module: 'og.views.forms.currency_tash' }), 
            	// item_1
                new ui.Dropdown({ 
                    form: form, 
                    placeholder: 'Please select...',
            		value: master.fixingCalendar ? master.fixingCalendar.split(sep)[1] : "",
                    //TODO 
                    processor: function (selector, data, errors) {
                        data.fixingCalendar = master.fixingCalendar.split(sep)[0] + sep + $(selector).val();
                    },
                    data_generator: holiday_handler
                }),
                // item_2
                new ui.Dropdown({ 
                    form: form, 
                    placeholder: 'Please select...',
            		value: master.regionCalendar ? master.regionCalendar.split(sep)[1] : "",
                    //TODO
                    processor: function (selector, data, errors) {
                        data.regionCalendar = master.regionCalendar.split(sep)[0] + sep + $(selector).val();
                    },
                    data_generator: holiday_handler
                }),
                // item_3
                new ui.Dropdown({
                	form: form, 
            		placeholder: 'Please select...',
                	resource: 'blotter.daycountconventions',
                	index: 'dayCount'
                }),
                // item_4
                new ui.Dropdown({
                	form: form, 
                	placeholder: 'Please select...',
                	resource: 'blotter.businessdayconventions',
                	index: 'businessDayConvention'
                }),
                // item_5
            	externalIdBundle = new form.Block({ template: '<ul class="og-awesome-list og-js-external-ids">{{{children}}}</ul>' })
            	// item_6
            	//TODO attributes
         	];
          	if ((master.externalIdBundle = arr(master.externalIdBundle)).length) { 
          		Array.prototype.push.apply(externalIdBundle.children, master.externalIdBundle.map(new_identifier)) 
          	};
           	form.dom();
		};
		constructor.type_map = type_map;
		return constructor;
	}
})