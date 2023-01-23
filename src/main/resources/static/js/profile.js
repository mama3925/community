$(function () {
    $(".follow-btn").click(follow);
});

function follow() {
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 关注TA
        $.post(
            CONTEXT_PATH + "/follow",
            // "entityId":$(btn).prev().val() 获取btn按钮上一个的值
            {"entityType": 3, "entityId": $(btn).prev().val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    } else {
        console.log(123);
        // 取消关注
        $.post(
            CONTEXT_PATH + "/unfollow",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    }
}
