<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="/struts-tags" prefix="s"%>

<div class="error_msg"><s:actionerror/></div>
<div class="content clear-fix">
	<div class="main-title container"><h1><a href="#">Data Upload</a></div>
	<div class="container c-95">
		<s:form action="compare-file" enctype="multipart/form-data" method="POST" theme="simple">
	 		<table>
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
