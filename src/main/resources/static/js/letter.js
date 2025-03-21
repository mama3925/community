$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();

	$.post(
		// 接口路径(与@RequestMapping(value = "/letter/send", method = RequestMethod.POST)路径一致)
		CONTEXT_PATH + "/letter/send",
		// 接口参数(与public String sendLetter(String toName, String content)参数一致)
		{"toName":toName, "content":content},
		function (data) {
			// 把JSON转换成JS对象
			data = $.parseJSON(data);
			// 与CommunityUtil.getJSONString(0,"msg")匹配--0：成功
			if (data.code == 0){
				$("#hintBody").text("发送成功！");
			}else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				location.reload();
			}, 2000);
		}
	);
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}