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
	<jsp:body>
	<ol class="breadcrumb">
  <li><a href="">主页</a></li>
  <li><a href="admin/setting/usersetting">用户管理</a></li>
  <li class="active">修改权限</li>
</ol>
	
	<form class="form-horizontal" action="admin/setting/userauthsetting" method="POST">
		<div class="form-group">
			<label for="name" class="col-sm-2 col-md-1 control-label">管理员</label>
			<div class="col-sm-10 col-md-5">
				<select data-toggle="select" multiple="multiple" class="form-control multiselect multiselect-info" name="newAdmins">
					<c:forEach items="${users }" var="u">
						<option value="${u.id }" <c:if test="${u.userAuthority.id ==  adminId}">selected="selected"</c:if>>${u.name }	
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="form-group">
						<div class="col-sm-2 col-md-1 control-label"></div>
						<div class="col-sm-10 col-md-3">
							
							<button type="submit" class="btn btn-primary btn-wide">提交</button>			
						</div>
					</div>
	</form>
			
    </jsp:body>
</myTemplate:template>