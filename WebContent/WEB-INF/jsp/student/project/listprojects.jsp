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
		$(function() {
			$('#'+'${type}').attr('class','btn btn-danger');
			$('.selecttitle').click(function(){
				var current = $(this);
				var strs= new Array();
				strs = current.attr('href').split(",");
				$.post('student/project/selecttitle',{
					'titleId':strs[0],
					'teacherId':strs[1]},function(data){
						if(data=='success'){
							location.href = 'student/project/listtitles/' + '${type}' + '/1';
						}
				}) 
				return false;
			})
			
			$('.myspan').each(function(){
				$(this).parent('td').children('a').hide()
			})
			$('.selecttitle').next('a').hide();
			if('${currentPage}'=='1'){
				$('#previous').addClass('disabled');
				$('#previous').click(function(){
					return false;
				})
			}else{
				$('#previous').removeClass('disabled');
			}
			if('${currentPage}'=='${countPage}'){
				$('#next').addClass('disabled');
				$('#next').click(function(){
					return false;
				})
			}else{
				$('#next').removeClass('disabled');
			}
			
			$(".telphone").mouseenter(function(){
				$(this).popover('show');
			})
			$(".telphone").mouseleave(function(){
				$(this).popover('hide');
			})
			/* $(".telphone").mouseout(function(){
				$(this).children('a').popover('toggle');
			}) */
		})
	</script>
</jsp:attribute>
	<jsp:body>
	<ol class="breadcrumb">
  <li><a href="">主页</a></li>
  <li><a href="student/project/projectmanagement">毕设管理</a></li>
  <li class="active">题目信息</li>
</ol>
	<c:if test="${fileDetail!=null }">
		<div class="form-horizontal">
		<div class="form-group">
			<div class="col-sm-2 col-md-2 control-label">题目</div>
			<div class="col-sm-2 col-md-4">${fileDetail.title.name }</div>
		</div>
		<div class="form-group">
			<div class="col-sm-2 col-md-2 control-label">题目性质</div>
			<div class="col-sm-2 col-md-4">${fileDetail.title.property }</div>
		</div>
		<div class="form-group">
			<div class="col-sm-2 col-md-2 control-label">指导教师</div>
			<div class="col-sm-2 col-md-4">${fileDetail.title.teacher.user.name }</div>
		</div>
		<div class="form-group">
			<div class="col-sm-2 col-md-2 control-label">论证报告</div>
			<div class="col-sm-10 col-md-9">
				<a href="download/${fileDetail.directory }/${fileDetail.fileName }/">${fileDetail.fileName }</a>
			</div>
		</div>
		<div class="form-group">
			<div class="col-sm-2 col-md-2 control-label">题目内容</div>
			<div class="col-sm-10 col-md-10">${fileDetail.title.objective }</div>
		</div>
	</div>	
	</c:if>
	<c:if test="${fileDetail==null }">
		<c:if test="${selectedTitleDetail != null}">
			<div class="alert alert-success alert-dismissable" role="alert">
				<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
				<strong>已选题目：</strong> ${selectedTitleDetail.title.name }
			</div>
		</c:if>
		<c:forEach items="${teachers }" var="t">
			<a id="${t.id }" class="btn btn-primary" href="student/project/listtitles/${t.id }/1" role="button" style="margin-bottom: 2px;">${t.user.name }(${t.leadNum })</a>
		</c:forEach>
		<a id="-1" class="btn btn-primary" href="student/project/listtitles/-1/1" role="button">全部题目</a>
		<c:if test="${type==-1 }">
			<br>
				<c:if test="${currentPage*15>=count }">
					(${(currentPage-1)*15+1 } &nbsp;-&nbsp;${count }&nbsp;/&nbsp;${count })
				</c:if>
				<c:if test="${currentPage*15<count }">
					(${(currentPage-1)*15+1 }&nbsp;-&nbsp;${currentPage*15 }&nbsp;/&nbsp;${count })
				</c:if>
			<br>
		</c:if>
		<div class="table-responsive">
			<table class="table table-striped table-condensed table-hover">
			<thead>
				<tr>
					 <th>#</th>
	                  <th>题目</th>
	                  <th>指导教师</th>
	                  <th>论证报告</th>
	                  <th>已选学生</th>
	                  <th>已确认学生/操作</th>
				</tr>
				</thead>
				<tbody>
					<c:forEach items="${fileDetails }" var="p" varStatus="s">
							<tr>
								<td>${s.count+(currentPage-1)*15 }</td>
								<td><a href="project/projecttitle/${p.id }">${p.title.name }</a></td>
								<td >
									 <a class="telphone" tabindex="0" data-toggle="popover" data-trigger="focus" data-placement="top" title="联系方式" data-content="${p.title.teacher.user.phoneNumber }">${p.title.teacher.user.name }</a>
								</td>
								<td>
									<a href="download/${p.directory }/${p.fileName}/">论证报告</a>
								</td>
								<td><c:forEach items="${p.title.selectedTitleDetails }" var="t">${t.student.student.name }<br></c:forEach></td>
								<td>
										<c:forEach items="${p.title.selectedTitleDetails }" var="st">
											<c:if test="${st.confirmed == true }">
												<span class="label label-success myspan">${st.student.student.name }</span>
											</c:if>
											<c:if test="${st.confirmed == false }">
												<a href="${p.title.id },${p.title.teacher.user.id}" class="selecttitle" id="${p.title.teacher.user.id}">选择</a>
											</c:if>
										</c:forEach>
										<c:if test="${p.title.selectedTitleDetails.size()==0 }">
											<a href="${p.title.id },${p.title.teacher.user.id}" class="selecttitle" id="${p.title.teacher.user.id}">选择</a>
										</c:if>
								</td>
							</tr>
				</c:forEach>
				</tbody>
		</table>
		</div>
		<c:if test="${type==-1 }">
			<nav>
			  <ul class="pagination pagination-lg">
			    <li id="previous">
			      <a href="student/project/listtitles/-1/${currentPage-1 }" aria-label="Previous">
			        <span aria-hidden="true">&laquo;</span>
			      </a>
			    </li>
			    <c:forEach begin="1" end="${countPage }" var="c">
			    	<c:if test="${c==currentPage }">
			    		<li class="active"><a href="student/project/listtitles/-1/${c }">${c }</a></li>
			    	</c:if>
			    	<c:if test="${c!=currentPage }">
			    		<li><a href="student/project/listtitles/-1/${c }">${c }</a></li>
			    	</c:if>
			    </c:forEach>
			    <li id="next">
			      <a href="student/project/listtitles/-1/${currentPage+1 }" aria-label="Next">
			        <span aria-hidden="true">&raquo;</span>
			      </a>
			    </li>
			  </ul>
			</nav>
		</c:if>
	</c:if>
    </jsp:body>
</myTemplate:template>