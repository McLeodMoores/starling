<#escape x as x?html>
<@page title="Update - ${holidayDoc.name}" jquery=true aceXmlEditor=true>


<#-- SECTION Update holiday -->
<@section title="Update holiday">
  <@form method="PUT" action="${uris.holiday()}" id="updateForm">
  <p>
    <#-->#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${conventionDoc.name}"/></@rowin -->
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
