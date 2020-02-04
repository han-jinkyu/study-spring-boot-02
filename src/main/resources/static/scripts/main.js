let main = {
    init: function () {
        let that = this;
        $('#btn-save').on('click', function() {
            that.save();
        });
    },
    save: function () {
        let data = {
            title: $('#title').val(),
            author: $('#author').val(),
            content: $('#content').val()
        };

        $.ajax({
            type: 'POST',
            url: '/posts',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function() {
            alert('글이 등록되었습니다!');
            location.reload();
        }).fail(function(err) {
            alert('등록 중 에러가 일어났습니다!');
            console.log(err);
        });
    }
};

main.init();