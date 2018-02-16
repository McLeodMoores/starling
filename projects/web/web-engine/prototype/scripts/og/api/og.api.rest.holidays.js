/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.api.rest.holidays',
    dependencies: ['og.api.common', 'og.api.rest'],
    obj: function () {
        var common = og.api.common, api = og.api.rest, str = common.str, check = common.check;
        return { // all requests that begin with /holidays
            root: 'holidays',
            get: api.default_get.partial(['name', 'type', 'currency'], null),
            put: function (config) {
            	config = config || {};
//            	var root = this.root, method = [root], data = {}, meta, id = str(config.id),
            	console.log(config);
            },
            del: function (config) {
                config = config || {};
                var root = this.root, method = [root], meta,
                id = str(config.id), version = str(config.version);
                meta = check({
                    bundle: {method: root + '#del', config: config},
                    required: [{all_of: ['id']}]
                });
                meta.type = 'DELETE';
                method = method.concat(version ? [id, 'versions', version] : id);
                return api.request(method, {data: {}, meta: meta});
            }
        };
    }
});