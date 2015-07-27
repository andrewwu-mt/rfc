<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="/struts-tags" prefix="s"%>

<s:action name="result-all" executeResult="false" />

<div class="content clear-fix">
	<div class="main-title container"><h1><a href="index">File Upload</a> | <a href="result-list">Result List</a></div>
	<div class="container c-95">
 		<table border="1" cellpadding="5">
 			<tr>
 				<th>Filename</th>
 			</tr>
			<s:iterator value="#request.filenameList" >
				<tr>
					<td><a href="result-download?fileName=<s:property  />"><s:property  /></a></td>
				</tr>
			</s:iterator>
 		</table>
   	</div>		
</div>
