<#escape x as x?html>
<@page title="Add holiday" jquery=true aceXmlEditor=true>


<#-- SECTION Add holiday -->
<@section title="Add holiday">
  <@form method="POST" action="${uris.holidays()}" id="addForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${name}" /></@rowin>
    
    <#if err_typeMissing??><div class="err">The type must be entered</div></#if>
    <#if err_typeInvalid??><div class="err">The type is invalid</div></#if>
    <@rowin label="Type">
      <select name="type">
        <option value="" <#if type = ''>selected</#if>></option>
        <#list holidayDescriptionMap?keys as key><option value="${key}"<#if type = '${key}'> selected</#if>>${holidayDescriptionMap[key]}</option></#list>
      </select>
    </@rowin>
    
    <#if err_xmlMissing??><div class="err">The data must be entered</div></#if>
    <#if err_holidayXmlMsg?has_content><div class="err">${err_holidayXmlMsg}</div></#if>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="holidayxml" id="holiday-xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="addForm" inputId="holiday-xml" xmlValue="${holidayXml}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.holidays()}">Holiday home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
