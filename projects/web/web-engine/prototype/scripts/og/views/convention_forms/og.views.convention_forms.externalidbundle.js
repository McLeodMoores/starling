$.register_module({
	name: 'og.views.convention_forms.ExternalIdBundle',
	dependencies: ['og.common.util.ui.Form'],
	obj: function () {
		var module = this,
			Block = og.common.util.ui.Block,
			add_table = '.og-add-table',
			template = Handlebars.compile('<tr class="og-js-eid-row">\
					<td class="id_key">{{{key}}}</td>\
					<td class="id_val">{{{value}}}</td>\
					<td><a class="OG-link-remove og-js-rem-eid" href="#">remove</a><td>\
					</tr>');
		var ExternalIdBundle = function (config) {
			var block = this,
				id = og.common.id('externalIdBundle'),
				form = config.form,
     			eid_data = config.data.ID ? 
   	             	Object.keys(config.data.ID).reduce(function (acc, val) {
   	              		return acc.concat({ key: config.data.ID[val]['Scheme'], value: config.data.ID[val]['Value'] });
   	               	}, []) : {};
			form.Block.call(block, {
				module: 'og.views.forms.convention-external-id-bundle_tash',
				extras: { id: id, data: eid_data },
				processor: function (data) {
					var eids = [],
						path = config.index.split('.'),
						last = path.pop();
				    $('.og-js-eid-row').each(function (i, e) {
				    	eids[i] = { 'Scheme': $(e).find('.id_key').html().trim(), 'Value': $(e).find('.id_val').html().trim() };
				    });
				    path.reduce(function (acc, val) { return acc[val]; }, data)[last] = { ID: eids };
				}
			});
			block.on('click', '#' + id + ' ' + add_table + ' .og-js-rem-eid', function (event) {
				event.preventDefault();
				$(event.target).closest(".og-js-eid-row").remove();
			}).on('click', '#' + id + ' .og-js-add-eid', function (event) {
				event.preventDefault();
                var row = $(event.target).closest(".og-js-add-eid-row"), //TODO don't use closest
                key = row.find('.id_key').val(),
                value = row.find('.id_val').val();
	            if (!key || !value) {
	                return;
	            }
	            $(add_table).prepend(template({key: key, value: value}));
	            row.find('[class^=id_]').val('');
	            $('.id_key').focus();
			});
		};
		ExternalIdBundle.prototype = new Block(null, { module: 'og.views.forms.convention-external-id-bundle_tash' });
		return ExternalIdBundle;
	}
});