// haksik auth common js
function markVerified(buttonId) {
  const verifyBtn = document.getElementById(buttonId);
  if (!verifyBtn) return;
  verifyBtn.innerText = "완료";
  verifyBtn.classList.add("success");
}

function showError(id, message) {
  const error = document.getElementById(id);
  if (!error) return;
  error.innerText = message;
  error.classList.remove("hidden");
  error.classList.add("visible");
}

function hideError(id) {
  const error = document.getElementById(id);
  if (!error) return;
  error.classList.remove("visible");
  error.classList.add("hidden");
}
