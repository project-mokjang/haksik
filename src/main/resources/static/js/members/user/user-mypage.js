
    function processWithdrawal() {
    const password = document.getElementById("passwordInput").value;

    if(confirm("정말 탈퇴하시겠습니까?\n탈퇴 즉시 모든 데이터가 영구적으로 삭제되며 절대 복구할 수 없습니다.")) {
    fetch('/api/members/withdraw', {
    method: 'POST',
    body: JSON.stringify({ password: password }),
    headers: { 'Content-Type': 'application/json' }
})
    .then(response => response.json())
    .then(data => {
    if(data.success) {
    alert(data.message);
    window.location.href = '/api/view/login';
} else {
    alert(data.message);
}
});
}
}

    function openTermsModal() {
        document.getElementById('termsModal').style.display = 'flex';
    }

    //  약관 모달 닫기 (X 버튼 누르면 실행)
    function closeTermsModal() {
        document.getElementById('termsModal').style.display = 'none';
    }
