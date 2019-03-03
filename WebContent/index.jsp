<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<html>
<head>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<base href="<%=basePath%>">
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,height=device-height,initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no">
<title>牡丹识别</title>
<script src="static/js/jquery-3.3.1.min.js"></script>
<!-- 最新版本的 Bootstrap 核心 CSS 文件 -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@3.3.7/dist/css/bootstrap.min.css">

<!-- 可选的 Bootstrap 主题文件（一般不用引入） -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@3.3.7/dist/css/bootstrap-theme.min.css">

<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@3.3.7/dist/js/bootstrap.min.js"></script>

<script src="static/js/tools.js"></script>
<script src="js/md5.js" type="text/javascript"></script>
<style>
</style>
</head>
<body background="static/花3.jpg" style="background-repeat:no-repeat;background-size:100% 100%;background-attachment: fixed;">
<br><br>
<input type="file" id="file" name="file" style="display:none" accept="image/*"/>
<p style="text-align:center"><img class="img-thumbnail" src="static/default.png" width="200px" height="200px" id="img"/></p>
<br><br>

<div style="text-align:center"><button class="btn btn-default btn-lg" onclick="file.click()"style=" margin:0 auto"><span class="glyphicon glyphicon-picture"></span>选择图片</button>
&nbsp;
<button class="btn btn-default btn-lg" id="btn_predict"><span class="glyphicon glyphicon-play"></span>开始识别</button><br></div>

<div style="text-align:center">
	<img src="static/loading.gif" width="50px" height="50px" id="img_loading" style="display:none"/>
	<div id="result_group" style="display:none">
		<div id="result"style=" font-size:30px"></div>
		<div class="btn-group" id="search_grop">
    		<button type="button" class="btn btn-default" id="baidu_search"><span class="glyphicon glyphicon-search"></span>百度搜索</button>
    		<button type="button" class="btn btn-default" id="taobao_search"><span class="glyphicon glyphicon-search"></span>淘宝搜索</button>
		</div>
	</div>
</div>
</body>
<script>

function hide_result() {
	$("#result_group").hide();
}

function show_result(result) {
	$("#result").text(result);
	$("#result_group").show();
}

$('#file').change(function(){

	hide_result();
	var files=this.files
	if (files && files.length > 0) {
		var file=files[0];
		if(file.size > 1024 * 1024 * 8) {
            alert('图片大小不能超过 8MB!');
            return false;
           
        }
		var URL = window.URL || window.webkitURL;// 获取 window 的 URL 工具
		$('#img').attr('src',URL.createObjectURL(file));
	}
});

$('#baidu_search').click(function(){
	window.open('https://www.baidu.com/s?wd='+$('#result').text(),'_blank');
})

$('#taobao_search').click(function(){
	window.open('https://s.taobao.com/search?q='+$('#result').text(),'_blank');
})

var predicting=false;

$('#btn_predict').click(function(){
	if (predicting) {
		alert('点击太频繁');
		return;
	}
	predicting=true
	var imgData=getBase64Image(document.getElementById("img"));
	
	var img_loading=$('#img_loading')
	img_loading.show();
	
	hide_result();
	predict(imgData, function(result) {
		if (result.code == 0) {
			//for (var t in result.result) {
			//	res.append('<li>'+result.result[t]+'</li>');
			//}
			
			show_result(result.result[0]);
		} else {
			alert(result.msg);
		}
		img_loading.hide();
		predicting=false;
	})
});

</script>
</html>