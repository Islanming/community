$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	//点击发布后隐藏弹出框
	$("#publishModal").modal("hide");

	//获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	//发送异步请求（post）
	jQuery.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title, "content":content},
		function(data){
			data = $.parseJSON(data);
			//在提示框中显示返回消息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//两秒后自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//发布成功则刷新页面
				if(data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	);

}