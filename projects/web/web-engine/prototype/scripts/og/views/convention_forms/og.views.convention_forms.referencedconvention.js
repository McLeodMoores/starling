$.register_module({
	name: 'og.views.convention_forms.ReferencedConvention',
	dependencies: ['og.common.util.ui.Form'],
	obj: function () {
		var module = this,
			Block = og.common.util.ui.Block,
			add_table = '.og-add-eid-table',
        	sep = '~', 
			template = Handlebars.compile('<tr class="og-js-eid-row">\
					<td class="id_key">{{{key}}}</td>\
					<td class="id_val">{{{value}}}</td>\
					</tr>');
		var ReferencedConvention = function (config) {
			var block = this,
				id = og.common.id('referencedConvention'),
				form = config.form,
				headingName;
			if (config.name) {
				headingName = config.name;
			} else {
				headingName = 'Underlying convention';
			}
			if (config.data) {
				vals = config.data.split(sep);
				eid_data = [{ key: vals[0], value: vals[1]}];
			} else {
				eid_data = {};
			}
			form.Block.call(block, {
				module: 'og.views.forms.convention-referenced-convention_tash',
				extras: { id: id, data: eid_data, name: headingName },
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
		};
		ReferencedConvention.prototype = new Block(null, { module: 'og.views.forms.convention-referenced-convention_tash' });
		return ReferencedConvention;
	}
});