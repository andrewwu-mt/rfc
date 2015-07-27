<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="/struts-tags" prefix="s"%>

	<link rel="stylesheet" href="css/ui-lightness/jquery-ui-1.10.3.custom.css">
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-ui.js"></script>
	
<script>
$(document).ready(function(){
   $("#evalDate").datepicker({ 
		 dateFormat: 'yymmdd',
		 changeMonth: true,
		 changeYear: true,
		 yearRange: "-20:+1",
		 numberOfMonths: 1
	});
});
</script>

<div class="content clear-fix">
	<div class="main-title container"><h1><a href="#">Data Upload</a></div>
	<div class="container c-95">
		<s:form action="compare-file" enctype="multipart/form-data" method="POST" theme="simple">
	 		<table>
	 			<tr>
	 				<td>Evaluation Date</td>
	 				<td><s:textfield id="evalDate" name="evalDate" required="true" /></td>
	 			</tr>
				<tr>
					<td>File EDM:</td>
					<td><s:file name="fileUpload" label="Select File EDM"/></td>
				</tr>
				<tr>
					<td>File Fubon:</td>
					<td><s:file name="fileUpload" label="Select File Fubon"/></td>
				</tr>
				<tr>
					<td colspan="2"><div class="button-wrapper"><button class="submit">Submit</button></div></td>
				</tr>
	 		</table>
 		</s:form>
   	</div>		
</div>
