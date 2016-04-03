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
	<jsp:attribute name="header">
		<link href="resources/css/fileinput.min.css"  rel="stylesheet">
	</jsp:attribute>

	<jsp:attribute name="footer">
	<script>
		$(function() {
			$('#file-1').fileinput({
				showPreview : false,
				showUpload: false,
		        browseClass: "btn btn-primary",
				initialCaption: "上传论证报告",
			});
			$('#title').blur(function(){
				$.ajax({
					url:"project/checkProjectTitle",
					data: {name: $(this).val()},
		    	    type: "POST",
		    	    dataType: "text",
		    	    success:function(data){
		    	    	alert($(this).val())
		    	    	alert(data)
		    	    	if(data == 'false' ){
		    	    		$('#myalert').show();
		    	    		$('#title').focus();
		    	    	}else if (data == 'true') {
		    	    		$('#myalert').hide();
						}
		    	    	
		    	    }
				})
			})
			
			$('#opened').on('switchChange.bootstrapSwitch',function(event,state) {
	        	$('#myfileinput').slideToggle('slow');
			});
		})
	</script>
	<script src="resources/js/fileinput.min.js"></script>
	</jsp:attribute>
	<jsp:body>
	<ol class="breadcrumb">
  <li><a href="">主页</a></li>
  <li><a href="project/projectmanagement">毕设管理</a></li>
  <li class="active">修改题目信息</li>
</ol>
	<c:if test="${exception != null}">
		&nbsp;&nbsp;
		<div class="alert alert-danger alert-dismissable" role="alert">
			<button type="button" class="close" data-dismiss="alert" aria-label="Close">
				<span aria-hidden="true">&times;</span>
			</button>
			<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
			<strong>错误！</strong> ${exception }
		</div>
	</c:if>
	<form class="form-horizontal" action="project/updatetitle" method="POST" enctype="multipart/form-data">
					<div class="form-group">
						<label for="name" class="col-sm-2 col-md-2 control-label">题目</label>
						<div class="col-sm-10 col-md-3">
							<input type="text" class="form-control" placeholder="题目" required name="name" id="title" value="${title.name }">
						</div>
						<div class="col-sm-10 col-md-3" hidden id="myalert">
							<div class="alert alert-danger alert-dismissable" role="alert">
							  			<button type="button" class="close" data-dismiss="alert" aria-label="Close">
												<span aria-hidden="true">&times;</span>
										</button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
							  <strong>该题目已存在！</strong> 
							</div>
						</div>
					</div>
					
					<div class="form-group">
						<label for="major" class="col-sm-2 col-md-2 control-label">专业</label>
						<div class="col-sm-10 col-md-3">
							<input type="text" class="form-control" placeholder="专业" required name="major" value="软件工程">
						</div>
					</div>
					<div class="form-group">
						<label for="property" class="col-sm-2 col-md-2 control-label">题目性质</label>
						<div class="col-sm-10 col-md-3">
							<input type="text" class="form-control" placeholder="题目性质" name="property" value="${title.property }">
						</div>
					</div>
					<div class="form-group">
						<label for="objective" class="col-sm-2 col-md-2 control-label">立题依据</label>
						<div class="col-sm-10 col-md-8">
							<textarea class="form-control" rows="15" placeholder="立题依据" name="objective" id="editor_id">${title.objective }</textarea>
						</div>
					</div>
					<div class="form-group">
						<label for="opened" class="col-sm-2 col-md-2 control-label">是否上传论证报告</label>
						<div class="col-md-1">
							<input type="checkbox" data-toggle="switch" data-on-color="primary" data-off-color="default"
							name="isExist"  value="1" id="opened"/>
						</div>
					</div>
					
					<div class="form-group" id="myfileinput" hidden>
						<label for="objective" class="col-sm-2 col-md-2 control-label">论证报告</label>
						<div class="col-sm-10 col-md-8">
							<input id="file-1" type="file" name="uploadfile" multiple data-min-file-count="0" accept=".doc,.docx">
						</div>
					</div>
					<input type="hidden" name="id" value="${title.id }">	
					<div class="form-group">
						<div class="col-sm-2 col-md-2 control-label"></div>
						<div class="col-sm-10 col-md-3">
							<button type="submit" class="btn btn-primary btn-wide">提交</button>
							<button type="reset" class="btn btn-danger btn-wide" id="reset">重置</button>	
						</div>
					</div>
				</form>	
	<script charset="utf-8" src="resources/editor/kindeditor-all-min.js"></script>
	<script charset="utf-8" src="resources/editor/lang/zh-CN.js"></script>
	<script>

		var editor;
		KindEditor.ready(function(K) {
			editor = K.create('#editor_id', {
				resizeType : 1,
				allowPreviewEmoticons : false,
				allowImageUpload : false,
				items : [ 'justifyleft', 'justifycenter', 'justifyright','justifyfull', 'insertorderedlist','|', 
				          'formatblock', 'fontname', 'fontsize', '|',
				          'forecolor', 'hilitecolor', 'bold','italic', 'underline']
			});
		});
	</script>
    </jsp:body>
</myTemplate:template>