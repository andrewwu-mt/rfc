<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="/struts-tags" prefix="s"%>

<link rel="stylesheet" href="css/ui-lightness/jquery-ui-1.10.3.custom.css">
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/spin.js"></script>
	
<script>
$(document).ready(function(){
   $("#evalDate").datepicker({ 
		 dateFormat: 'yymmdd',
		 changeMonth: true,
		 changeYear: true,
		 yearRange: "-20:+1",
		 numberOfMonths: 1
	});
	
	
	// ==================== LOADING ====================== //

	var opts = {
	  lines: 15, // The number of lines to draw
	  length: 0, // The length of each line
	  width: 5, // The line thickness
	  radius: 25, // The radius of the inner circle
	  corners: 1, // Corner roundness (0..1)
	  rotate: 0, // The rotation offset
	  direction: 1, // 1: clockwise, -1: counterclockwise
	  color: '#000', // #rgb or #rrggbb or array of colors
	  speed: 1, // Rounds per second
	  trail: 100, // Afterglow percentage
	  shadow: false, // Whether to render a shadow
	  hwaccel: false, // Whether to use hardware acceleration
	  className: 'spinner', // The CSS class to assign to the spinner
	  zIndex: 2e9, // The z-index (defaults to 2000000000)
	  top: '50%', // Top position relative to parent
	  left: '50%' // Left position relative to parent
	};
	var target = document.getElementById('loading');
	var spinner = new Spinner(opts);
	var $loadingBg = $('#loading_wrapper');

	loading_hide();

	function loading_show(){
        spinner.spin(target);
    	$loadingBg.show();
	}

	function loading_hide(){
        spinner.spin();
    	$loadingBg.hide();
	}
	
	$(".submit").click(function(){
		loading_show();
		$("#form").submit();
	});
});
</script>

<div id="loading_wrapper" style="position: fixed;">
	<div id="loading"></div>
</div>

<div class="content clear-fix">
	<div class="main-title container"><h1><a href="index">File Upload</a> | <a href="result-list">Result List</a></div>
	<div class="container c-95">
		<s:form action="compare-file" enctype="multipart/form-data" method="POST" theme="simple" id="form">
	 		<table border="1">
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
					<td colspan="2"><div class="button-wrapper"><button type="button" class="submit">Submit</button></div></td>
				</tr>
	 		</table>
 		</s:form>
   	</div>		
</div>
