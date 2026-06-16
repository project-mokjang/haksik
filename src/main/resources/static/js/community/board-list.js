// board-list.js

let lastPostId = null;
let isLoading = false;
let isEnd = false;
let postObserver = null;

const CATEGORY_LABELS = {
    FREE: '자유',
    FOOD: '맛집추천',
    QUESTION: '질문',
    MEETUP: '만남'
};

document.addEventListener('DOMContentLoaded', function () {
    const boardTypeSelect = document.getElementById('boardType');
    const categorySelect = document.getElementById('category');
    const keywordInput = document.getElementById('keyword');
    const searchBtn = document.getElementById('searchBtn');
    const writePostBtn = document.getElementById('writePostBtn');

    if (boardTypeSelect) {
        boardTypeSelect.addEventListener('change', resetAndFetch);
    }

    if (categorySelect) {
        categorySelect.addEventListener('change', resetAndFetch);
    }

    if (keywordInput) {
        keywordInput.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                resetAndFetch();
            }
        });
    }

    if (searchBtn) {
        searchBtn.addEventListener('click', resetAndFetch);
    }

    if (writePostBtn) {
        writePostBtn.addEventListener('click', function () {
            location.href = '/api/view/community/write';
        });
    }

    window.addEventListener('click', closePostOptionDropdowns);

    initInfiniteScroll();
    resetAndFetch();
});

// 목록 초기화 후 게시글 조회
function resetAndFetch() {
    const postContainer = document.getElementById('postContainer');

    if (postContainer) {
        postContainer.innerHTML = '';
    }

    lastPostId = null;
    isEnd = false;

    fetchPosts();
}

// 게시글 목록 조회
async function fetchPosts() {
    if (isLoading || isEnd) {
        return;
    }

    isLoading = true;
    setLoading(true);

    const boardType = document.getElementById('boardType').value;
    const category = document.getElementById('category').value;
    const keyword = document.getElementById('keyword').value.trim();

    let url = '/api/posts?boardType=' + encodeURIComponent(boardType) + '&size=10';

    if (category) {
        url += '&category=' + encodeURIComponent(category);
    }

    if (keyword) {
        url += '&keyword=' + encodeURIComponent(keyword);
    }

    if (lastPostId) {
        url += '&lastPostId=' + lastPostId;
    }

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error('게시글 목록 조회 실패');
        }

        const posts = await response.json();

        if (!Array.isArray(posts)) {
            throw new Error('게시글 응답 데이터 형식 오류');
        }

        if (posts.length < 10) {
            isEnd = true;
        }

        renderPosts(posts);
    } catch (error) {
        console.error(error);

        if (typeof showToast === 'function') {
            showToast('게시글을 불러오지 못했습니다.', 'error');
        }
    } finally {
        isLoading = false;
        setLoading(false);
    }
}

// 로딩 표시 변경
function setLoading(visible) {
    const loading = document.getElementById('loading');

    if (!loading) {
        return;
    }

    loading.style.display = visible ? 'block' : 'none';
}

// 게시글 목록 표시
function renderPosts(posts) {
    const container = document.getElementById('postContainer');

    if (!container) {
        return;
    }

    posts.forEach(function (post) {
        const postCard = createPostCard(post);
        container.appendChild(postCard);
        lastPostId = post.postId;
    });
}

// 게시글 카드 생성
function createPostCard(post) {
    const li = document.createElement('li');
    li.className = 'board-post-card';

    li.addEventListener('click', function () {
        location.href = '/api/view/community/' + post.postId;
    });

    const head = document.createElement('div');
    head.className = 'board-post-head';

    const category = document.createElement('div');
    category.className = 'board-post-category';
    category.textContent = '[' + getCategoryLabel(post.category) + ']';

    head.appendChild(category);

    if (post.mine) {
        head.appendChild(createPostOptions(post.postId));
    }

    const title = document.createElement('div');
    title.className = 'board-post-title';
    title.textContent = post.title || '제목 없음';

    const meta = document.createElement('div');
    meta.className = 'board-post-meta';

    const author = document.createElement('span');
    author.textContent = post.authorName || '익명';

    const info = document.createElement('span');
    const dateStr = post.createdAt ? post.createdAt.split('T')[0] : '';
    info.textContent = '조회 ' + (post.viewCount || 0) + ' · ' + dateStr;

    meta.appendChild(author);
    meta.appendChild(info);

    li.appendChild(head);
    li.appendChild(title);
    li.appendChild(meta);

    return li;
}

// 게시글 수정/삭제 옵션 생성
function createPostOptions(postId) {
    const wrap = document.createElement('div');
    wrap.className = 'board-options-wrap';

    wrap.addEventListener('click', function (event) {
        event.stopPropagation();
    });

    const optionBtn = document.createElement('button');
    optionBtn.type = 'button';
    optionBtn.className = 'board-options-btn';
    optionBtn.textContent = '⋮';

    optionBtn.addEventListener('click', function () {
        togglePostOptions(postId);
    });

    const dropdown = document.createElement('div');
    dropdown.className = 'board-options-dropdown';
    dropdown.id = 'postDropdown-' + postId;

    const editBtn = document.createElement('button');
    editBtn.type = 'button';
    editBtn.textContent = '수정';

    editBtn.addEventListener('click', function () {
        editPost(postId);
    });

    const deleteBtn = document.createElement('button');
    deleteBtn.type = 'button';
    deleteBtn.textContent = '삭제';
    deleteBtn.className = 'delete-text';

    deleteBtn.addEventListener('click', function () {
        deletePost(postId);
    });

    dropdown.appendChild(editBtn);
    dropdown.appendChild(deleteBtn);

    wrap.appendChild(optionBtn);
    wrap.appendChild(dropdown);

    return wrap;
}

// 카테고리명 변환
function getCategoryLabel(category) {
    if (!category) {
        return '일반';
    }

    return CATEGORY_LABELS[category] || category;
}

// 옵션 드롭다운 토글
function togglePostOptions(postId) {
    const dropdown = document.getElementById('postDropdown-' + postId);

    if (!dropdown) {
        return;
    }

    document.querySelectorAll('.board-options-dropdown.show').forEach(function (item) {
        if (item !== dropdown) {
            item.classList.remove('show');
        }
    });

    dropdown.classList.toggle('show');
}

// 옵션 드롭다운 닫기
function closePostOptionDropdowns(event) {
    if (event.target && event.target.classList.contains('board-options-btn')) {
        return;
    }

    document.querySelectorAll('.board-options-dropdown.show').forEach(function (item) {
        item.classList.remove('show');
    });
}

// 게시글 삭제
async function deletePost(postId) {
    if (!confirm('정말 이 게시글을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch('/api/posts/' + postId, {
            method: 'DELETE'
        });

        if (!response.ok) {
            if (typeof showToast === 'function') {
                showToast('삭제 권한이 없습니다.', 'error');
            }
            return;
        }

        if (typeof showToast === 'function') {
            showToast('삭제되었습니다.', 'success');
        }

        resetAndFetch();
    } catch (error) {
        console.error(error);

        if (typeof showToast === 'function') {
            showToast('게시글 삭제 중 오류가 발생했습니다.', 'error');
        }
    }
}

// 게시글 수정 화면 이동
function editPost(postId) {
    location.href = '/api/view/community/write?postId=' + postId;
}

// 무한 스크롤 초기화
function initInfiniteScroll() {
    const target = document.getElementById('observerTarget');
    const scrollRoot = document.querySelector('.board-list-scroll');

    if (!target || !scrollRoot) {
        return;
    }

    postObserver = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                fetchPosts();
            }
        });
    }, {
        root: scrollRoot,
        rootMargin: '0px',
        threshold: 0.1
    });

    postObserver.observe(target);
}