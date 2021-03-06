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
  <li class="active">用户监考设置</li>
</ol>


	<form class="form-horizontal" action="admin/setting/userinvisetting" method="POST">
	<div class="form-group">
			<label class="col-md-1 control-label">姓名</label>
			<label class="col-md-1 control-label">监考</label>
			<label class="col-md-1 control-label">特殊监考</label>
			<label class="col-md-1 control-label">监考推荐</label>
			</div>
		<c:forEach items="${inviusers }" var="u" varStatus="s">
		<div class="form-group">
			<label class="col-md-1 control-label">${u.user.name }</label>
			<label class="col-md-1 control-label">${u.invigilations.size() }</label>
			<div class="col-md-1"><input type="text" class="form-control" value="${u.sqecQuantity }" name="invqs"></div>
			<div class="col-md-1">
				<input type="checkbox" data-toggle="switch" data-on-color="primary" data-off-color="default"
							<c:if test="${u.enabledRecommend=='true' }">checked='checked'</c:if> name="checkeds"  value="${u.id }"/>
				</div>
			
		</div>
		</c:forEach>
		<div class="form-group">
	<div class="col-md-1">
			<button type="submit" class="btn btn-primary btn-wide">提交</button>
		</div>
	</div>
	</form>
	


		
		
	
			
    </jsp:body>
</myTemplate:template>