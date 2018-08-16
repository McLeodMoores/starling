{
    "template_data": {
		<#if configJSON??>
      		"configJSON":${configJSON},
		</#if>
		<#if configXML??>
      		"configXML":"${configXML}",
		</#if>
		<#if deleted>
      		"deleted":"${configDoc.versionToInstant}",
		</#if>
      	"name":"${conventionDoc.name}",
      	"type":"${conventionDescription}",
      	"object_id":"${conventionDoc.uniqueId.objectId}",
      	"version_id":"${conventionDoc.uniqueId.version}"
    }
}
