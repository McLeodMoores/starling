<#escape x as x?html>
{
  "types" : [
    <#list conventionDescriptionMap?keys as key>
      {"value" : "${key}", "name" : "${conventionDescriptionMap[key]}"}<#if key_has_next>,</#if>
    </#list>
  ],
  "groups" : [
    <#list conventionGroups?keys as groupKey>
    {
      "group" : "${groupKey}",
      "types" : [
        <#list conventionGroups[groupKey]?keys as typeKey>
          {"value" : "${typeKey}", "name" : "${conventionGroups[groupKey][typeKey]}"}<#if typeKey_has_next>,</#if>
        </#list>
      ]
    }<#if groupKey_has_next>,</#if>
    </#list>
  ]
}
</#escape>