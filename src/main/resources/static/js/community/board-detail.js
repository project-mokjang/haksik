// board-detail.js

const postId = document.getElementById('postId').value;

let currentParentId = null;
let currentEditCommentId = null;

document.addEventListener('DOMContentLoaded', function () {
    bindBoardDetailEvents();
    fetchPostDetail();
});

// 이벤트 등록
function bindBoardDetailEvents() {
    const optionsBtn = document.getElementById('optionsBtn');
    const cancelReplyBtn = document.getElementById('cancelReplyBtn');
    const commentSubmitBtn = document.getElementById('commentSubmitBtn');
    const reportModal = document.getElementById('reportModal');
    const reportCloseBtn = document.getElementById('reportCloseBtn');

    if (optionsBtn) {
        optionsBtn.addEventListener('click', function (event) {
            event.stopPropagation();
            toggleOptions();
        });
    }

    if (cancelReplyBtn) {
        cancelReplyBtn.addEventListener('click', cancelReply);
    }

    if (commentSubmitBtn) {
        commentSubmitBtn.addEventListener('click', submitComment);
    }

    if (reportModal) {
        reportModal.addEventListener('click', function (event) {
            if (event.target === reportModal) {
                closeReportModal();
            }
        });
    }

    if (reportCloseBtn) {
        reportCloseBtn.addEventListener('click', closeReportModal);
    }

    document.querySelectorAll('.board-report-option').forEach(function (button) {
        button.addEventListener('click', function () {
            submitReport(this.dataset.reportReason);
        });
    });

    window.addEventListener('click', closeOptionsOnOutsideClick);
}

// 응답 body를 JSON으로 변환
async function safeJson(response) {
    try {
        return await response.json();
    } catch (e) {
        return null;
    }
}

// API 에러 메시지 조회
function getErrorMessage(result, fallback) {
    if (result && result.message) {
        return result.message;
    }

    if (result && result.data && result.data.message) {
        return result.data.message;
    }

    return fallback;
}

// 게시글 상세 조회
async function fetchPostDetail() {
    try {
        const response = await fetch('/api/posts/' + postId);

        if (!response.ok) {
            throw new Error('게시글 로드 실패');
        }

        const post = await response.json();

        renderPostDetail(post);
        renderComments(post.comments);
    } catch (error) {
        console.error(error);
        showToast('존재하지 않거나 삭제된 게시글입니다.', 'error');

        setTimeout(function () {
            location.href = '/api/view/community';
        }, 900);
    }
}

// 게시글 상세 표시
function renderPostDetail(post) {
    document.getElementById('categoryBadge').innerText = '[' + (post.category || '일반') + ']';
    document.getElementById('postTitle').innerText = post.title || '';
    document.getElementById('authorName').innerText = post.authorName || '';
    document.getElementById('postInfo').innerText =
        '조회 ' + (post.viewCount || 0) + ' · ' + getDateOnly(post.createdAt);
    document.getElementById('postContent').innerText = post.content || '';

    renderPostOptions(post);
    renderImageGallery(post.imageUrls);
}

// 게시글 옵션 표시
function renderPostOptions(post) {
    const dropdown = document.getElementById('optionsDropdown');

    if (!dropdown) {
        return;
    }

    dropdown.innerHTML = '';

    if (post.mine) {
        const editButton = document.createElement('button');
        editButton.type = 'button';
        editButton.innerText = '수정하기';
        editButton.addEventListener('click', editPost);

        const deleteButton = document.createElement('button');
        deleteButton.type = 'button';
        deleteButton.innerText = '삭제하기';
        deleteButton.classList.add('delete-text');
        deleteButton.addEventListener('click', deletePost);

        dropdown.appendChild(editButton);
        dropdown.appendChild(deleteButton);
        return;
    }

    const reportButton = document.createElement('button');
    reportButton.type = 'button';
    reportButton.innerText = '신고하기';
    reportButton.classList.add('delete-text');
    reportButton.addEventListener('click', function () {
        openReportModal('POST', postId);
    });

    dropdown.appendChild(reportButton);
}

// 이미지 갤러리 표시
function renderImageGallery(imageUrls) {
    const gallery = document.getElementById('imageGallery');

    if (!gallery) {
        return;
    }

    gallery.innerHTML = '';

    if (!imageUrls || imageUrls.length === 0) {
        return;
    }

    imageUrls.forEach(function (url) {
        const img = document.createElement('img');
        img.src = url;
        img.alt = '게시글 이미지';
        gallery.appendChild(img);
    });
}

// 댓글 목록 표시
function renderComments(comments) {
    const container = document.getElementById('commentContainer');
    const commentCount = document.getElementById('commentCount');

    if (!container) {
        return;
    }

    commentCount.innerText = comments ? comments.length : 0;
    container.innerHTML = '';

    if (!comments || comments.length === 0) {
        const empty = document.createElement('div');
        empty.className = 'board-comment-empty';
        empty.innerText = '첫 댓글을 남겨보세요!';
        container.appendChild(empty);
        return;
    }

    const topLevel = [];
    const subMap = {};

    comments.forEach(function (comment) {
        if (comment.parentCommentId) {
            if (!subMap[comment.parentCommentId]) {
                subMap[comment.parentCommentId] = [];
            }

            subMap[comment.parentCommentId].push(comment);
            return;
        }

        topLevel.push(comment);
    });

    topLevel.forEach(function (parentComment) {
        container.appendChild(createCommentElement(parentComment, false));

        if (subMap[parentComment.commentId]) {
            subMap[parentComment.commentId].forEach(function (replyComment) {
                container.appendChild(createCommentElement(replyComment, true));
            });
        }
    });
}

// 댓글 요소 생성
function createCommentElement(comment, isReply) {
    const item = document.createElement('div');
    item.className = isReply ? 'board-comment-item reply' : 'board-comment-item';

    const meta = document.createElement('div');
    meta.className = 'board-comment-meta';

    const author = document.createElement('span');
    author.className = 'board-comment-author';

    if (isReply) {
        const replyMark = document.createElement('span');
        replyMark.className = 'board-comment-reply-mark';
        replyMark.innerText = 'ㄴ';
        author.appendChild(replyMark);
    }

    author.appendChild(document.createTextNode(comment.authorName || '익명'));

    const date = document.createElement('span');
    date.className = 'board-comment-date';
    date.innerText = getDateOnly(comment.createdAt);

    meta.appendChild(author);
    meta.appendChild(date);

    const body = document.createElement('div');
    body.className = 'board-comment-body';
    body.innerText = comment.content || '';

    const actions = document.createElement('div');
    actions.className = 'board-comment-actions';

    if (!isReply) {
        const replyButton = createCommentActionButton('답글', 'green', function () {
            prepareReply(comment.commentId, comment.authorName || '익명');
        });

        actions.appendChild(replyButton);
    }

    if (comment.mine) {
        const editButton = createCommentActionButton('수정', '', function () {
            prepareEditComment(comment.commentId, comment.content || '');
        });

        const deleteButton = createCommentActionButton('삭제', 'red', function () {
            deleteComment(comment.commentId);
        });

        actions.appendChild(editButton);
        actions.appendChild(deleteButton);
    } else {
        const reportButton = createCommentActionButton('신고', 'red', function () {
            openReportModal('COMMENT', comment.commentId);
        });

        actions.appendChild(reportButton);
    }

    item.appendChild(meta);
    item.appendChild(body);
    item.appendChild(actions);

    return item;
}

// 댓글 액션 버튼 생성
function createCommentActionButton(text, colorClass, onClick) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'board-comment-action-btn';

    if (colorClass) {
        button.classList.add(colorClass);
    }

    button.innerText = text;
    button.addEventListener('click', onClick);

    return button;
}

// 답글 준비
function prepareReply(commentId, authorName) {
    currentParentId = commentId;
    currentEditCommentId = null;

    const replyIndicator = document.getElementById('replyIndicator');

    replyIndicator.classList.add('show');
    document.getElementById('replyTargetName').innerText = authorName;
    document.getElementById('commentInput').value = '';
    document.getElementById('commentInput').focus();
    document.getElementById('commentSubmitBtn').innerText = '답글';
}

// 댓글 수정 준비
function prepareEditComment(commentId, oldContent) {
    currentEditCommentId = commentId;
    currentParentId = null;

    const replyIndicator = document.getElementById('replyIndicator');

    replyIndicator.classList.add('show');
    document.getElementById('replyTargetName').innerText = '댓글 수정';
    document.getElementById('commentInput').value = oldContent;
    document.getElementById('commentInput').focus();
    document.getElementById('commentSubmitBtn').innerText = '수정';
}

// 답글/수정 취소
function cancelReply() {
    currentParentId = null;
    currentEditCommentId = null;

    document.getElementById('replyIndicator').classList.remove('show');
    document.getElementById('commentInput').value = '';
    document.getElementById('commentSubmitBtn').innerText = '등록';
}

// 댓글 등록/수정
async function submitComment() {
    const contentInput = document.getElementById('commentInput');
    const content = contentInput.value.trim();
    const anonYn = document.getElementById('anonComment').checked ? 'Y' : 'N';

    if (!content) {
        showToast('댓글 내용을 입력해주세요.', 'error');
        return;
    }

    try {
        if (currentEditCommentId) {
            await updateComment(content);
            return;
        }

        await createComment(content, anonYn);
    } catch (error) {
        console.error(error);
        showToast('댓글 처리 중 오류가 발생했습니다.', 'error');
    }
}

// 댓글 수정 요청
async function updateComment(content) {
    const response = await fetch('/api/comments/' + currentEditCommentId, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            content: content
        })
    });

    if (!response.ok) {
        showToast('댓글 수정에 실패했습니다.', 'error');
        return;
    }

    showToast('댓글이 수정되었습니다.', 'success');
    cancelReply();
    fetchPostDetail();
}

// 댓글 작성 요청
async function createComment(content, anonYn) {
    const payload = {
        content: content,
        anonymousYn: anonYn
    };

    if (currentParentId) {
        payload.parentCommentId = currentParentId;
    }

    const response = await fetch('/api/posts/' + postId + '/comments', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        showToast('댓글 등록에 실패했습니다.', 'error');
        return;
    }

    showToast('댓글이 등록되었습니다.', 'success');
    cancelReply();
    fetchPostDetail();
}

// 댓글 삭제
async function deleteComment(commentId) {
    if (!confirm('댓글을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch('/api/comments/' + commentId, {
            method: 'DELETE'
        });

        if (!response.ok) {
            showToast('삭제 권한이 없습니다.', 'error');
            return;
        }

        showToast('댓글이 삭제되었습니다.', 'success');
        fetchPostDetail();
    } catch (error) {
        console.error(error);
        showToast('댓글 삭제 중 오류가 발생했습니다.', 'error');
    }
}

// 게시글 수정 화면 이동
function editPost() {
    location.href = '/api/view/community/write?postId=' + postId;
}

// 게시글 삭제
async function deletePost() {
    if (!confirm('정말 이 게시글을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch('/api/posts/' + postId, {
            method: 'DELETE'
        });

        if (!response.ok) {
            showToast('삭제 권한이 없습니다.', 'error');
            return;
        }

        showToast('게시글이 삭제되었습니다.', 'success');

        setTimeout(function () {
            location.href = '/api/view/community';
        }, 900);
    } catch (error) {
        console.error(error);
        showToast('게시글 삭제 중 오류가 발생했습니다.', 'error');
    }
}

// 옵션 드롭다운 토글
function toggleOptions() {
    document.getElementById('optionsDropdown').classList.toggle('show');
}

// 옵션 드롭다운 외부 클릭 닫기
function closeOptionsOnOutsideClick(event) {
    if (event.target && event.target.classList.contains('board-detail-options-btn')) {
        return;
    }

    document.querySelectorAll('.board-detail-options-dropdown.show').forEach(function (dropdown) {
        dropdown.classList.remove('show');
    });
}

// 신고 모달 열기
function openReportModal(targetType, targetId) {
    document.getElementById('reportTargetType').value = targetType;
    document.getElementById('reportTargetId').value = targetId;

    document.querySelectorAll('.board-detail-options-dropdown.show').forEach(function (dropdown) {
        dropdown.classList.remove('show');
    });

    document.getElementById('reportModal').classList.add('show');
}

// 신고 모달 닫기
function closeReportModal() {
    document.getElementById('reportModal').classList.remove('show');
}

// 신고 요청
async function submitReport(reason) {
    const targetType = document.getElementById('reportTargetType').value;
    const targetId = document.getElementById('reportTargetId').value;

    if (!confirm("해당 내용을 '" + reason + "' 사유로 신고하시겠습니까?")) {
        return;
    }

    try {
        const response = await fetch('/api/reports', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                targetType: targetType,
                targetId: targetId,
                reason: reason
            })
        });

        const result = await safeJson(response);

        if (!response.ok) {
            showToast(getErrorMessage(result, '신고 처리에 실패했습니다.'), 'error');
            return;
        }

        showToast('신고가 정상적으로 접수되었습니다.', 'success');
        closeReportModal();
    } catch (error) {
        console.error(error);
        showToast('서버와 통신 중 오류가 발생했습니다.', 'error');
    }
}

// 날짜만 추출
function getDateOnly(dateTime) {
    if (!dateTime) {
        return '';
    }

    return dateTime.split('T')[0];
}