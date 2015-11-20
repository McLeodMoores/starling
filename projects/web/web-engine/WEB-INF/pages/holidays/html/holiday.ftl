<#escape x as x?html>
<@page title="Holiday - ${holidayDoc.name}" jquery=true aceXmlEditor=true>

<@section css="info" if=deleted>
  <p>This holiday has been deleted</p>
</@section>


<#-- SECTION Holiday output -->
<@section title="Holiday">
  <p>
    <@rowout label="Name">${holidayDoc.name}</@rowout>
    <@rowout label="Reference">${holidayDoc.uniqueId.value}, version ${holidayDoc.uniqueId.version}, <a href="${uris.holidayVersions()}">view history</a>
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
<#if holidayDoc.providerId?has_content>
    <@rowout label="Provider id">${holidayDoc.providerId.scheme.name} - ${holidayDoc.providerId.value}</@rowout>
</#if>
    <@rowout label="Type">${holiday.type}</@rowout>
<#if holiday.currencyISO?has_content>
    <@rowout label="Currency">${holiday.currencyISO} <a href="${regionUris.regionsByCurrency(holiday.currencyISO)}">view</a></@rowout>
</#if>
<#if holiday.regionId?has_content>
    <@rowout label="Region">${holiday.regionId.scheme.name} - ${holiday.regionId.value} <a href="${regionUris.regions(holiday.regionId)}">view</a></@rowout>
</#if>
<#if holiday.exchangeId?has_content>
    <@rowout label="Exchange">${holiday.exchangeId.scheme.name} - ${holiday.exchangeId.value} <a href="${exchangeUris.exchanges(holiday.exchangeId)}">view</a></@rowout>
</#if>
</@subsection>
<@subsection title="Detail" if=deleted>
    <@rowout label="Data"><textarea readonly style="width:650px;height:300px;">${holidayXml}</textarea></@rowout>
</@subsection>

</@section>

<#-- SECTION Update holiday -->
<@section title="Update holiday">
  <@form method="PUT" action="${uris.holiday()}" id="updateForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${holidayDoc.name}" /></@rowin>
    <#if err_holidayXmlMsg?has_content><div class="err">${err_holidayXmlMsg}</div></#if>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="holidayxml" id="holiday-xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="updateForm" inputId="holiday-xml" xmlValue="${holidayXml}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>

<#-- SECTION Delete holiday -->
<@section title="Delete holiday" if=!deleted>
  <@form method="DELETE" action="${uris.holiday()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.holidayVersions()}">History of this holiday</a><br />
    <a href="${uris.holidays()}">Holiday search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
