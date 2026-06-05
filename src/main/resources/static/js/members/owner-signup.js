// haksik owner signup js

// 점주 회원가입 화면 API 주소와 화면 동작

// 점주 회원가입 요청 API
const OWNER_SIGNUP_URL = '/api/signup/owners';

// 사업자등록번호 중복 확인 API
// 컨트롤러 주소
const BUSINESS_NUMBER_CHECK_URL = '/api/signup/owners/check-business-number';

// 사업자등록번호 중복 확인 완료 여부
// 사용자가 사업자등록번호를 바꾸면 다시 false로 변경
let isBusinessNumberChecked = false;

/**
 * 화면 로딩이 끝난 뒤 실행되는 초기화 함수
 * - 회원가입 form submit 이벤트 등록
 * - 사업자등록번호 입력값 변경 시 중복확인 상태 초기화
 */
document.addEventListener('DOMContentLoaded', function () {
    const ownerSignupForm = document.getElementById('ownerSignupForm');

    // 현재 페이지에 점주 회원가입 form이 없으면 더 이상 실행하지 않음
    if (!ownerSignupForm) {
        return;
    }

    // form 제출 시 기본 submit 대신 fetch로 JSON 요청을 보냄
    ownerSignupForm.addEventListener('submit', submitOwnerSignup);

    const businessNumberInput = document.getElementById('businessNumber');

    if (businessNumberInput) {
        businessNumberInput.addEventListener('input', function () {
            // 사업자등록번호가 변경되면 기존 중복확인 결과는 더 이상 유효하지 않음
            isBusinessNumberChecked = false;

            const checkBtn = document.getElementById('businessNumberCheckBtn');

            if (checkBtn) {
                checkBtn.innerText = '중복';
                checkBtn.classList.remove('success');
            }
        });
    }
});

/**
 * 사업자등록번호 중복 확인
 * - 입력값이 비어 있으면 요청하지 않음
 * - 사용 가능한 번호라면 버튼 상태를 '완료'로 변경
 */
async function checkBusinessNumber() {
    const businessNumberInput = document.getElementById('businessNumber');

    if (!businessNumberInput || !businessNumberInput.value.trim()) {
        alert('사업자등록번호를 입력해주세요.');
        return;
    }

    const businessNumber = businessNumberInput.value.trim();

    try {
        const response = await fetch(
            `${BUSINESS_NUMBER_CHECK_URL}?businessNumber=${encodeURIComponent(businessNumber)}`
        );

        // 서버에서 400, 409, 500 등 실패 응답이 오면 사용 불가로 처리
        if (!response.ok) {
            alert('이미 사용 중인 사업자등록번호입니다.');
            return;
        }

        const result = await response.json();

        // ApiResponse로 감싸진 응답에서 실제 중복 확인 결과를 꺼냄
        const data = result.data || result;

        // DuplicateCheckResponse의 available 값이 true면 사용 가능
        if (data.available === true) {
            isBusinessNumberChecked = true;

            const checkBtn = document.getElementById('businessNumberCheckBtn');

            if (checkBtn) {
                checkBtn.innerText = '완료';
                checkBtn.classList.add('success');
            }

            alert('사용 가능한 사업자등록번호입니다.');
        } else {
            isBusinessNumberChecked = false;
            alert('이미 사용 중인 사업자등록번호입니다.');
        }
    } catch (error) {
        console.error('checkBusinessNumber error:', error);
        alert('사업자등록번호 중복 확인 중 오류가 발생했습니다.');
    }
}

/**
 * 점주 회원가입 최종
 * - 기본 form 제출을 막고 JSON 형식으로 서버에 요청
 * - 비밀번호 확인, 사업자등록번호 중복 확인 여부를 먼저 검사
 * - OwnerSignupRequest DTO 필드명에 맞춰 requestData를 구성
 */
async function submitOwnerSignup(event) {
    event.preventDefault();

    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;

    // 프론트에서 1차로 비밀번호 일치 여부를 검사
    if (password !== passwordConfirm) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    // 사업자등록번호 중복 확인을 완료하지 않으면 가입 요청을 보내지 않음
    if (!isBusinessNumberChecked) {
        alert('사업자등록번호 중복 확인을 먼저 완료해주세요.');
        return;
    }

    // 백엔드 OwnerSignupRequest DTO에 맞춘 요청 데이터
    const requestData = {
        loginId: document.getElementById('loginId').value.trim(),
        password: password,
        email: document.getElementById('email').value.trim(),
        ownerName: document.getElementById('ownerName').value.trim(),
        ownerPhone: document.getElementById('ownerPhone').value.trim(),
        businessNumber: document.getElementById('businessNumber').value.trim(),
        businessName: document.getElementById('businessName').value.trim()
    };

    try {
        const response = await fetch(OWNER_SIGNUP_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        });

        // 회원가입 성공 시 승인 대기 화면으로 이동
        if (response.ok) {
            window.location.href = '/api/view/owner-pending';
            return;
        }

        // 실패 응답일 경우 서버에서 내려준 에러 메시지를 우선 사용
        let errorMessage = '입력값을 확인해주세요.';

        try {
            const errorData = await response.json();

            if (errorData.message) {
                errorMessage = errorData.message;
            } else if (errorData.data && errorData.data.message) {
                errorMessage = errorData.data.message;
            }
        } catch (parseError) {
            console.error('error response parse failed:', parseError);
        }

        alert('점주 회원가입 실패: ' + errorMessage);
    } catch (error) {
        console.error('submitOwnerSignup error:', error);
        alert('서버와 통신 중 오류가 발생했습니다.');
    }
}