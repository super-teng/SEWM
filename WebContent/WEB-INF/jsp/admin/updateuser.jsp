<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="myTemplate" tagdir="/WEB-INF/tags/"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<myTemplate:template>
<jsp:attribute name="footer">
	<script>
	$(document).ready(function() {
		$('#select_userupdate').change(function() {
			if($(this).val() == 0) {
				$('#div_userupdate').hide();
				return;
			}
				
			$.ajax({
	    	    // The URL for the request
	    	    url: "admin/updateuserajax",
	    	 
	    	    // The data to send (will be converted to a query string)
	    	    data: {"userId": $(this).val()},
	    	    // Whether this is a POST or GET request
	    	    type: "GET",
	    	  
	    	    // The type of data we expect back
	    	    dataType : "html",
	    	    
	    	    beforeSend: function(jqXHR){
	    	    	$("#div_userupdate").show();
	    	    	$("#div_userupdate").html("<img src='resources/images/loading.gif' />"); 
	           }, 
	    	    // Code to run if the request succeeds;
	    	    // the response is passed to the function
	    	    success: function(json ) {
	    	    	$("#div_userupdate").html(json);
	    	    },
	    	 
	    	    // Code to run if the request fails; the raw request and
	    	    // status codes are passed to the function
	    	    error: function( xhr, status, errorThrown ) {
	    	        alert( "Status: " + status + "; Error: " + errorThrown);
	    	        console.log( "Error: " + errorThrown );
	    	        console.log( "Status: " + status );
	    	        console.dir( xhr );
	    	    },
	    	 
	    	    // Code to run regardless of success or failure
	    	    /*complete: function( xhr, status ) {
	    	        alert( "The request is complete!" );
	    	    }*/
	    	});
			
		});
	})
	</script>
</jsp:attribute>
	<jsp:body>
	<ol class="breadcrumb">
  <li><a href="">主页</a></li>
  <li><a href="admin/usersetting">用户管理</a></li>
  <li class="active">更新用户</li>
</ol>
	<form class="form-horizontal">
      <div class="form-group">
						<label for="select_userupdate" class="col-sm-2 col-md-1 control-label">用户</label>
						<div class="col-sm-10 col-md-3">
							<select data-toggle="select" class="select select-primary mrs mbm" name="userId" id="select_userupdate">
							<option value="0">用户</option>
							<c:forEach items="${users}" var="u">
								<option value="${u.id}">${u.name }</option>
							</c:forEach>
						</select>
						</div>
						</div>
						</form>
						<div id="div_userupdate" hidden="">
						
						</div>
						
						
    </jsp:body>
</myTemplate:template>