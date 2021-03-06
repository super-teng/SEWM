<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="myTemplate" tagdir="/WEB-INF/tags/"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<myTemplate:template>

	<jsp:body>
	<ol class="breadcrumb">
		<li><a href="">主页</a></li>
		<li class="active">用户管理</li>
	</ol>
	
	
		<form class="form-horizontal"  action="superadmin/initteachertitle" method="POST">
			<button type="submit" class="btn btn-primary btn-wide">初始化职称</button>
		</form>
		
		<form class="form-horizontal"  action="superadmin/inituserauthority" method="POST">
			<button type="submit" class="btn btn-primary btn-wide">初始化权限</button>
		</form>
		<form class="form-horizontal"  action="superadmin/inituser" method="POST">
			<button type="submit" class="btn btn-primary btn-wide">初始化用户</button>
		</form>
		<form class="form-horizontal"  action="superadmin/initspecinvitype" method="POST">
			<button type="submit" class="btn btn-primary btn-wide">初始化特殊监考类型</button>
		</form>
		<form class="form-horizontal"  action="superadmin/initinvistatustype" method="POST">
			<button type="submit" class="btn btn-primary btn-wide">初始化监考状态</button>
		</form>
		<form class="form-horizontal"  action="superadmin/initfiletasktype" method="POST">
			<button type="submit" class="btn btn-primary btn-wide">初始化任务文档格式</button>
		</form>
		<form class="form-horizontal"  action="superadmin/initfilestatus" method="POST">
			<button type="submit" class="btn btn-primary btn-wide">初始化任务状态</button>
		</form>
	</jsp:body>
</myTemplate:template>