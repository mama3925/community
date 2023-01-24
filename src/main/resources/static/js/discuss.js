function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId, "postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                //点赞成功,通过子节点得到span
                $(btn).children("span").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? "已赞" : "赞");

            } else {
                alert(data.msg);
            }
        }
    );
}